/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.Operation;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathType;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpression;
import br.com.anteros.persistence.dsl.osql.types.Visitor;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanOperation;
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorColumnPath;
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorValuePath;
import br.com.anteros.persistence.handler.ResultClassDefinition;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

/**
 * Visitor que analisa as expressões da consulta processando os nomes path's
 * criando junções e colunas que serão usadas na serialização para SQL pela
 * classe {@link SQLSerializer}.
 * 
 * @author edson
 *
 */
public class SQLAnalyser implements Visitor<Void, Void> {

	private static int MAKE_ALIASES = 1;
	private static int MAKE_COLUMNS = 2;
	private static final String COMMA = ", ";
	private AbstractOSQLQuery<?> query;
	private Map<Path<?>, EntityPath<?>> aliases = new HashMap<Path<?>, EntityPath<?>>();
	private Map<Expression<?>, Set<SQLAnalyserColumn>> parsedPathsOnProjections = new LinkedHashMap<Expression<?>, Set<SQLAnalyserColumn>>();
	private Map<Expression<?>, Set<SQLAnalyserColumn>> parsedPathsOnOperations = new LinkedHashMap<Expression<?>, Set<SQLAnalyserColumn>>();
	private Map<Expression<?>, Set<SQLAnalyserColumn>> resultColumnsFromProjections = new LinkedHashMap<Expression<?>, Set<SQLAnalyserColumn>>();
	private int nextAliasTableName = 0;
	private int nextAliasColumnName = 0;
	private int level = MAKE_ALIASES;
	private List<Expression<?>> individualExpressions = new ArrayList<Expression<?>>();
	private SQLAnalyserColumn lastColumnAdded = null;

	protected enum Stage {
		SELECT, FROM, WHERE, GROUP_BY, HAVING, ORDER_BY
	}

	protected Stage stage = Stage.SELECT;

	private boolean inOperation = false;
	private Boolean namedParameter = null;
	private Boolean hasParameters = false;

	private Map<Operation<?>, String> booleanDefinitions = new HashMap<Operation<?>, String>();
	private Expression<?> currentExpressionOnMakeColumns;

	public SQLAnalyser(AbstractOSQLQuery<?> query) {
		this.query = query;
	}

	@Override
	public Void visit(Constant<?> expr, Void context) {
		if (this.level == MAKE_COLUMNS) {
			if (!inOperation) {
				Set<SQLAnalyserColumn> columns = getColumnListProjection(expr);
				lastColumnAdded = new SQLAnalyserColumn("", "", "", null, 0);
				columns.add(lastColumnAdded);
			}
		}
		return null;
	}

	@Override
	public Void visit(FactoryExpression<?> expr, Void context) {
		for (Expression<?> arg : expr.getArgs()) {
			arg.accept(this, null);
		}
		return null;
	}

	@Override
	public Void visit(Operation<?> expr, Void context) {

		try {
			inOperation = true;
			lastColumnAdded = null;
			for (Expression<?> arg : expr.getArgs()) {
				arg.accept(this, null);
			}

			if (level == MAKE_COLUMNS) {
				if (expr.getType() == Boolean.class) {
					if ((expr.getOperator() == Ops.EQ) || (expr.getOperator() == Ops.NE)
							|| (expr.getOperator() == Ops.GT) || (expr.getOperator() == Ops.GOE)
							|| (expr.getOperator() == Ops.LT) || (expr.getOperator() == Ops.LOE)) {
						analyzeEqualsOperation(expr);
					} else if ((expr.getOperator() == Ops.IS_NOT_NULL) || (expr.getOperator() == Ops.IS_NULL)) {
						analyzeNullOperation(expr);
					}
				}
			}

		} finally {
			inOperation = false;
		}
		return null;
	}

	private void analyzeNullOperation(Operation<?> expr) {
		if (!booleanDefinitions.containsKey(expr)) {
			Expression<?> leftExpression = expr.getArg(0);
			if (isEntity(leftExpression)) {
				Set<SQLAnalyserColumn> leftColumns = parsedPathsOnOperations.get(leftExpression);
				StringBuilder booleanAsString = new StringBuilder();
				boolean appendAnd = false;
				for (SQLAnalyserColumn leftColumn : leftColumns) {
					if (appendAnd)
						booleanAsString.append(" AND ");
					booleanAsString.append(leftColumn.getAliasTableName()).append(".")
							.append(leftColumn.getColumnName()).append(" ");
					if (expr.getOperator() == Ops.IS_NOT_NULL) {
						booleanAsString.append(" is not null ");
					} else if (expr.getOperator() == Ops.IS_NULL) {
						booleanAsString.append(" is null ");
					}
					appendAnd = true;
				}
				booleanDefinitions.put(expr, booleanAsString.toString());
			}

		}

	}

	private void analyzeEqualsOperation(Operation<?> expr) {
		if (!booleanDefinitions.containsKey(expr)) {
			Expression<?> leftExpression = expr.getArg(0);
			Expression<?> rigthExpression = expr.getArg(1);

			if (isEntity(leftExpression) && (isEntity(rigthExpression))) {

				Set<SQLAnalyserColumn> leftColumns = parsedPathsOnOperations.get(leftExpression);
				Set<SQLAnalyserColumn> rightColumns = parsedPathsOnOperations.get(rigthExpression);

				StringBuilder booleanAsString = new StringBuilder();
				boolean appendAnd = false;
				Iterator<SQLAnalyserColumn> itRightColumns = rightColumns.iterator();
				for (SQLAnalyserColumn leftColumn : leftColumns) {
					SQLAnalyserColumn rightColumn = itRightColumns.next();
					if (appendAnd)
						booleanAsString.append(" AND ");
					booleanAsString.append(leftColumn.getAliasTableName()).append(".")
							.append(leftColumn.getColumnName()).append(" ");

					if (expr.getOperator() == Ops.EQ) {
						booleanAsString.append("=");
					} else if (expr.getOperator() == Ops.NE) {
						booleanAsString.append("<>");
					} else if (expr.getOperator() == Ops.GT) {
						booleanAsString.append(">");
					} else if (expr.getOperator() == Ops.GOE) {
						booleanAsString.append(">=");
					} else if (expr.getOperator() == Ops.LT) {
						booleanAsString.append("<");
					} else if (expr.getOperator() == Ops.LOE) {
						booleanAsString.append("<=");
					}

					booleanAsString.append(" ").append(rightColumn.getAliasTableName()).append(".")
							.append(rightColumn.getColumnName());
					appendAnd = true;
				}

				booleanDefinitions.put(expr, booleanAsString.toString());
			}
		}
	}

	protected boolean isEntity(Expression<?> expression) {
		EntityCache leftEntityCache = null;
		Class<?> sourceClass = null;
		if (expression instanceof EntityPath)
			sourceClass = this.getClassByEntityPath((EntityPath<?>) expression);
		else
			sourceClass = expression.getType();

		leftEntityCache = query.getSession().getEntityCacheManager().getEntityCache(sourceClass);
		return leftEntityCache != null;
	}

	@Override
	public Void visit(ParamExpression<?> expr, Void context) {
		if (namedParameter != null) {
			if (namedParameter != (!expr.isAnon())) {
				throw new SQLAnalyserException(
						"Foram encontrados parâmetros nomeados e não nomeados. Use apenas um formato de parâmetros na consulta.");
			}
		}
		this.namedParameter = !expr.isAnon();
		this.hasParameters = true;
		return null;
	}

	@Override
	public Void visit(Path<?> expr, Void context) {

		if (level == MAKE_ALIASES) {
			try {
				makePossibleJoins(expr, null);
			} catch (Exception e) {
				throw new SQLAnalyserException(e.getMessage(), e);
			}
		} else if (level == MAKE_COLUMNS) {
			processColumns(expr, null);
		}
		return null;
	}

	/**
	 * Processa o caminho e monta os nomes das colunas que serão usadas na
	 * serialização do SQL.
	 * 
	 * @param path
	 *            Caminho
	 */
	private void processColumns(Path<?> path, Path<?> keyPath) {
		if (keyPath == null) {
			if (inOperation) {
				if (parsedPathsOnOperations.containsKey(path))
					return;
			} else {
				if (parsedPathsOnProjections.containsKey(path))
					return;
			}
		}

		if ((path instanceof DiscriminatorColumnPath) || (path instanceof DiscriminatorValuePath)) {
			return;
		}

		if ((path.getMetadata().getPathType() == PathType.VARIABLE)
				&& (this.query.getSession().getEntityCacheManager().isEntity(path.getType()))) {
			/*
			 * Ser for uma variável e for uma entidade Ex: tOrder = new
			 * TOrder("ORD")
			 */
			EntityCache sourceEntityCache = getEntityCacheByPath(path.getType());
			String aliasTableName = path.getMetadata().getName();
			/*
			 * Somente processa projeções customizadas se não estiver dentro de
			 * uma operação
			 */
			if ((!inOperation) && (((EntityPath<?>) path).getCustomProjection().size() > 0)) {
				for (Path<?> customPath : ((EntityPath<?>) path).getCustomProjection()) {
					if (customPath.equals(path))
						processAllFields((keyPath == null ? path : keyPath),
								((EntityPath<?>) path).getExcludeProjection(), aliasTableName, sourceEntityCache,
								inOperation);
					else
						processColumns(customPath, path);
				}
			} else {
				processAllFields((keyPath == null ? path : keyPath), null, aliasTableName, sourceEntityCache,
						inOperation);
			}

		} else if (path.getMetadata().getPathType() == PathType.PROPERTY) { // Ser
																			// for
																			// uma
																			// propriedade
																			// de
																			// uma
																			// Entidade.
																			// Ex.
																			// tOrder.dtInvoice
																			// ou
																			// tOrder.person
																			// ou
																			// tOrder.person.id
			/*
			 * Se for uma propriedade da entidade, pega o pai adequado
			 */
			Path<?> entityPath = this.getAppropriateAliasByEntityPath(path);
			String aliasTableName = entityPath.getMetadata().getName();
			Path<?> targetPath = path;
			/*
			 * Verifica se a propriedade é um ID então pega como caminho do pai
			 * dela
			 */
			EntityCache sourceEntityCache = getEntityCacheByPath(entityPath.getType());
			/*
			 * Se estiver numa operação e o caminho for chave primária troca o
			 * path para a entidade da chave
			 */
			if (inOperation && !path.getMetadata().getParent().getMetadata().isRoot()) {
				EntityCache targetEntityCache = getEntityCacheByPath(path.getMetadata().getParent().getType());
				DescriptionField descriptionFieldPath = getDescriptionFieldByPath(targetEntityCache, path.getMetadata()
						.getName() + "");
				if (descriptionFieldPath.isPrimaryKey() && !descriptionFieldPath.isCompositeId()) {
					targetPath = path.getMetadata().getParent();
				}
			}

			if ((targetPath instanceof EntityPath<?>) && (!inOperation)) {
				/*
				 * Somente processa projeções customizadas se não estiver dentro
				 * de uma operação
				 */
				if ((!inOperation) && (((EntityPath<?>) targetPath).getCustomProjection().size() > 0)) {
					for (Path<?> customPath : ((EntityPath<?>) targetPath).getCustomProjection()) {
						if (customPath.equals(targetPath))
							processAllFields((keyPath == null ? path : keyPath),
									((EntityPath<?>) targetPath).getExcludeProjection(), aliasTableName,
									sourceEntityCache, inOperation);
						else
							processColumns(customPath, path);
					}
				} else
					processAllFields((keyPath == null ? path : keyPath), null, aliasTableName, sourceEntityCache,
							inOperation);
			} else {
				DescriptionField descriptionField = getDescriptionFieldByPath(sourceEntityCache, targetPath
						.getMetadata().getName());
				processSingleField((keyPath == null ? path : keyPath), aliasTableName, descriptionField, true);
			}

		} else if (path.getMetadata().getPathType() == PathType.VARIABLE) { // Demais
																			// caso
																			// incluindo
																			// aliases
																			// de
																			// colunas.
			/*
			 * Se o estágio da análise for MAKE_COLUMNS e havia sido adicionado
			 * uma coluna associa o alias atribuido pelo usuário sobrepondo o
			 * gerado e marca que o usuário atribui um alias.
			 */
			if (level == MAKE_COLUMNS) {
				if (lastColumnAdded != null) {
					lastColumnAdded.setAliasColumnName(path.getMetadata().getName());
					lastColumnAdded.setUserAliasDefined(true);
				}
			}
		}
	}

	protected DescriptionField getDescriptionFieldByPath(EntityCache entityCache, String fieldName) {
		DescriptionField result = entityCache.getDescriptionField(fieldName);
		if (result == null)
			throw new SQLSerializerException("O campo " + fieldName + " não foi encontrado na classe "
					+ entityCache.getEntityClass() + ". ");
		return result;
	}

	protected EntityCache getEntityCacheByPath(Class<?> sourceClass) {
		EntityCache result = this.query.getSession().getEntityCacheManager().getEntityCache(sourceClass);
		if (result == null)
			throw new SQLSerializerException("A classe " + sourceClass
					+ " não foi encontrada na lista de entidades gerenciadas.");
		return result;
	}

	/**
	 * Processa o campo da entidade e gera um coluna para o mesmo. Processa
	 * apenas campos simples e relacionamentos.
	 * 
	 * @param index
	 *            Indice dentro da lista de expressões.
	 * @param alias
	 *            alias da tabela
	 * @param descriptionField
	 *            campo da entidade
	 * @param makeAlias
	 *            criar um alias?
	 * @return verdadeiro se conseguiu criar a coluna
	 */
	protected boolean processSingleField(Path<?> keyPath, String alias, DescriptionField descriptionField,
			boolean makeAlias) {

		if (descriptionField.isSimple()) {
			String aliasColumnName = "";
			if (makeAlias)
				aliasColumnName = makeNextAliasName(alias);
			lastColumnAdded = new SQLAnalyserColumn(alias, descriptionField.getSimpleColumn().getColumnName(),
					aliasColumnName, descriptionField, 0);
			if (inOperation)
				getColumnListOperation(keyPath).add(lastColumnAdded);
			else
				getColumnListProjection(keyPath).add(lastColumnAdded);

			if (level == MAKE_COLUMNS) {
				Set<SQLAnalyserColumn> columnsFromProjection = getResultColumnsFromProjection(currentExpressionOnMakeColumns);
				columnsFromProjection.add(lastColumnAdded);
			}

			return true;
		} else if (descriptionField.isRelationShip()) {
			for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
				String aliasColumnName = "";
				if (makeAlias)
					aliasColumnName = makeNextAliasName(alias);
				lastColumnAdded = new SQLAnalyserColumn(alias, column.getColumnName(), aliasColumnName,
						descriptionField, 0);

				if (inOperation)
					getColumnListOperation(keyPath).add(lastColumnAdded);
				else
					getColumnListProjection(keyPath).add(lastColumnAdded);

				if (level == MAKE_COLUMNS) {
					Set<SQLAnalyserColumn> columnsFromProjection = getResultColumnsFromProjection(currentExpressionOnMakeColumns);
					columnsFromProjection.add(lastColumnAdded);
				}

			}
			return true;
		}
		return false;
	}

	private Set<SQLAnalyserColumn> getColumnListProjection(Expression<?> expr) {
		Set<SQLAnalyserColumn> result = null;
		if (parsedPathsOnProjections.containsKey(expr))
			result = parsedPathsOnProjections.get(expr);
		if (result == null) {
			result = new LinkedHashSet<SQLAnalyserColumn>();
			parsedPathsOnProjections.put(expr, result);
		}

		return result;
	}

	private Set<SQLAnalyserColumn> getColumnListOperation(Expression<?> expr) {
		Set<SQLAnalyserColumn> result = null;
		if (parsedPathsOnOperations.containsKey(expr))
			result = parsedPathsOnOperations.get(expr);
		if (result == null) {
			result = new LinkedHashSet<SQLAnalyserColumn>();
			parsedPathsOnOperations.put(expr, result);
		}

		return result;
	}

	private Set<SQLAnalyserColumn> getResultColumnsFromProjection(Expression<?> expr) {
		Set<SQLAnalyserColumn> result = null;
		if (resultColumnsFromProjections.containsKey(expr))
			result = resultColumnsFromProjections.get(expr);
		if (result == null) {
			result = new LinkedHashSet<SQLAnalyserColumn>();
			resultColumnsFromProjections.put(expr, result);
		}

		return result;
	}

	/**
	 * Processa todos os campos de caminho (entidade) e gera as colunas.
	 * 
	 * @param entityPath
	 *            Entidade
	 * @param alias
	 *            alias da tabela
	 * @param sourceEntityCache
	 *            Representação da entidade no dicionário
	 */
	protected void processAllFields(Path<?> keyPath, Set<Path<?>> excludeProjection, String alias,
			EntityCache sourceEntityCache, boolean onlyPrimaryKey) {

		for (DescriptionField descriptionField : sourceEntityCache.getDescriptionFields()) {
			/*
			 * Se for para gerar somente campos da chave primária da entidade ou
			 * se o campo faz parte da lista de exclusão
			 */
			if (onlyPrimaryKey) {
				if (!descriptionField.isPrimaryKey()
						|| hasPathForDescriptionFieldToExclude(excludeProjection, descriptionField))
					continue;
			} else {
				/*
				 * Não processa nenhum tipo de coleção e caminhos excluídos da
				 * projeção
				 */
				if (descriptionField.isAnyCollection()
						|| hasPathForDescriptionFieldToExclude(excludeProjection, descriptionField))
					continue;
			}

			processSingleField(keyPath, alias, descriptionField, true);
		}
	}

	/**
	 * Retorna se um caminho for excluído da projeção de uma entidade através do
	 * método {@link EntityPath#excludeProjection(Path...)}
	 * 
	 * @param excludeProjection
	 * @param descriptionField
	 * @return Verdadeiro se o campo faz parte da lista de exclusão.
	 */
	private boolean hasPathForDescriptionFieldToExclude(Set<Path<?>> excludeProjection,
			DescriptionField descriptionField) {
		if (excludeProjection == null)
			return false;
		for (Path<?> path : excludeProjection) {
			if (path.getMetadata().getName().equals(descriptionField.getField().getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Void visit(SubQueryExpression<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(TemplateExpression<?> expr, Void context) {
		if (this.level == MAKE_COLUMNS) {

			Set<SQLAnalyserColumn> columns = null;
			if (inOperation) {
				columns = getColumnListOperation(expr);
			} else {
				columns = getColumnListProjection(expr);
			}
			lastColumnAdded = new SQLAnalyserColumn("", "", "", null, 0);
			columns.add(lastColumnAdded);
		}
		return null;
	}

	/**
	 * Realiza a análise das expressões.
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		QueryMetadata metadata = query.getMetadata();
		this.individualExpressions = extractIndividualColumnsExpression(metadata);
		processExpressions(metadata, MAKE_ALIASES);
		processExpressions(metadata, MAKE_COLUMNS);

		System.out.println("PROJEÇÃO");
		System.out.println("-------------------------------------------");
		for (Expression<?> p : parsedPathsOnProjections.keySet()) {
			for (SQLAnalyserColumn s : parsedPathsOnProjections.get(p)) {
				System.out.println(p + " -> " + s);
			}
		}

		System.out.println();
		System.out.println("OPERACAO");
		System.out.println("-------------------------------------------");
		for (Expression<?> p : parsedPathsOnOperations.keySet()) {
			for (SQLAnalyserColumn s : parsedPathsOnOperations.get(p)) {
				System.out.println(p + " -> " + s);
			}
		}

		System.out.println("-------------------------------------------");

	}

	/**
	 * Extrai a lista de expressões de forma individualizada para serem
	 * processadas. Expressões contidas em fábricas de expressões serão
	 * retornadas de forma individual.
	 * 
	 * @param metadata
	 *            Metadata contendo as expressões
	 * @return Lista de expressões.
	 */
	public static List<Expression<?>> extractIndividualColumnsExpression(QueryMetadata metadata) {
		List<Expression<?>> select = metadata.getProjection();
		List<Expression<?>> sqlSelect;
		if (select.size() == 1) {
			final Expression<?> first = select.get(0);
			if (first instanceof FactoryExpression) {
				sqlSelect = ((FactoryExpression<?>) first).getArgs();
			} else {
				sqlSelect = (List) select;
			}
		} else {
			sqlSelect = new ArrayList<Expression<?>>(select.size());
			for (Expression<?> selectExpr : select) {
				if (selectExpr instanceof FactoryExpression) {
					sqlSelect.addAll(((FactoryExpression<?>) selectExpr).getArgs());
				} else {
					sqlSelect.add(selectExpr);
				}
			}
		}

		return sqlSelect;
	}

	/**
	 * Processa lista de expressões individuais de acordo como nível(estágio) da
	 * análise.
	 * 
	 * @param metadata
	 *            Metadata contendo expressões.
	 * @param level
	 *            Nível/estágio
	 * @throws Exception
	 *             Exceção ocorrida durante processamento
	 */
	protected void processExpressions(QueryMetadata metadata, int level) throws Exception {
		this.level = level;
		final List<Expression<?>> select = this.individualExpressions;
		final Predicate where = metadata.getWhere();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();
		final List<Expression<?>> groupBy = metadata.getGroupBy();
		List<JoinExpression> joins = metadata.getJoins();

		/*
		 * Processa apenas o Select criando aliases colunas e junções
		 */
		stage = Stage.SELECT;
		for (Expression<?> expr : select) {
			currentExpressionOnMakeColumns = expr;
			expr.accept(this, null);
			if (expr instanceof EntityPath<?>) {
				Set<Path<?>> customProjection = ((EntityPath<?>) expr).getCustomProjection();
				for (Path<?> path : customProjection) {
					if (!(path.equals(expr)))
						makePossibleJoins(path, null);
				}
			}
		}
		currentExpressionOnMakeColumns = null;
		/*
		 * Processa demais cláusulas somente criando os aliases das colunas
		 */
		stage = Stage.FROM;
		if (joins != null) {
			List<Expression<?>> expressionsJoins = new ArrayList<Expression<?>>();
			for (JoinExpression expr : joins) {
				if (expr.getCondition() != null)
					expressionsJoins.add(expr.getCondition());
			}
			for (Expression<?> exprJoin : expressionsJoins) {
				exprJoin.accept(this, null);
			}
		}
		stage = Stage.WHERE;
		if (where != null)
			where.accept(this, null);
		stage = Stage.HAVING;
		if (having != null)
			having.accept(this, null);
		stage = Stage.GROUP_BY;
		if (groupBy != null) {
			for (Expression<?> expr : select) {
				expr.accept(this, null);
			}
		}
		stage = Stage.ORDER_BY;
		if (orderBy != null) {
			for (OrderSpecifier<?> ord : orderBy) {
				ord.getTarget().accept(this, null);
			}
		}
	}

	/**
	 * Verifica se é possível criar uma junção para um caminho utilizado nas
	 * expressões.
	 * 
	 * @param expr
	 *            Expressão.
	 * @throws Exception
	 *             Exceção ocorrida durante criação da junção.
	 */
	protected void makePossibleJoins(Path<?> expr, Path<?> owner) throws Exception {
		if ((expr instanceof DiscriminatorValuePath) || (expr instanceof DiscriminatorColumnPath)
				|| (expr.getMetadata().isRoot()))
			return;
		makePossibleJoins(expr.getMetadata().getParent(), (owner == null ? expr : owner));

		EntityCache entityCache = this.query.getSession().getEntityCacheManager().getEntityCache(expr.getType());

		boolean isEntity = (entityCache != null);
		boolean isExpressionMain = (owner == null);
		boolean isOwnerOfExpressionMain = (!isExpressionMain) && (owner.getMetadata().getParent() == expr);
		boolean isPrimaryKey = false;

		if (isOwnerOfExpressionMain) {
			Object element = owner.getMetadata().getElement();
			DescriptionField descriptionField = entityCache.getDescriptionField(element + "");
			isPrimaryKey = (descriptionField != null)
					&& (descriptionField.isPrimaryKey() && !descriptionField.isCompositeId());
		}

		/*
		 * Se for uma entidade e não for pai ou a expressão principal
		 * (expression main = onde inicia o processamento recursivo)
		 */
		boolean firstCondition = (isEntity && !isExpressionMain && !isOwnerOfExpressionMain);
		/*
		 * Se for uma entidade e for a expressão principal e o estágio for
		 * diferente do FROM e WHERE.
		 */
		boolean secondCondition = (isEntity && isExpressionMain && (stage != Stage.FROM) && (stage != Stage.WHERE));
		/*
		 * Se for uma entidade e não for a expressão principal e for o pai da
		 * expressão principal e a mesma não for chave primária.
		 */
		boolean thirdCondition = (isEntity && !isExpressionMain && isOwnerOfExpressionMain && !isPrimaryKey);
		/*
		 * Se for uma entidade e não for a expressão principal e for o pai da
		 * expressão e o estágio for diferente de FROM e where.
		 */
		boolean fourthCondition = (isEntity && !isExpressionMain && isOwnerOfExpressionMain && (stage != Stage.FROM) && (stage != Stage.WHERE));

		if ((firstCondition) || (secondCondition) || (thirdCondition) || (fourthCondition)) {
			if (!aliases.containsKey(expr)) {
				String alias = makeNextAliasTableName();
				EntityPath<?> newPath = (EntityPath<?>) ReflectionUtils.invokeConstructor(expr.getClass(), alias);
				BooleanExpression boExpression = (BooleanExpression) ReflectionUtils.invokeMethod(expr, "eq", newPath);
				query.leftJoin(newPath).on(boExpression);
				aliases.put(expr, newPath);
			}
		}
	}

	/**
	 * Lista de aliases gerados na análise das expressões.
	 * 
	 * @return
	 */
	public Map<Path<?>, EntityPath<?>> getAliases() {
		return aliases;
	}

	/**
	 * Retorna a entidade(alias) de um caminho.
	 * 
	 * @param path
	 *            Caminho
	 * @return Alias encontrado
	 */
	public Path<?> getAppropriateAliasByEntityPath(Path<?> path) {

		if (path.getMetadata().isRoot())
			return path;

		EntityCache entityCache = this.query.getSession().getEntityCacheManager().getEntityCache(path.getType());
		boolean isEntity = (entityCache != null);

		EntityPath<?> result = null;
		if (isEntity) { // PAP.pessoa
			if (inOperation) {
				result = aliases.get(path.getMetadata().getParent());
				if (result == null)
					result = (EntityPath<?>) path.getMetadata().getParent(); // PAP
			} else {
				result = aliases.get(path);
			}
		} else { // PAP.usuarioSistema.id
			if (path.getMetadata().getParent().getMetadata().isRoot())
				result = (EntityPath<?>) path.getMetadata().getParent();
			else {

				EntityCache entityCacheOwner = this.query.getSession().getEntityCacheManager()
						.getEntityCache(path.getMetadata().getParent().getType());
				if (entityCacheOwner == null)
					throw new SQLSerializerException("A classe " + path.getMetadata().getParent().getType()
							+ " não foi encontrada na lista de entidades gerenciadas.");

				DescriptionField descriptionField = entityCacheOwner.getDescriptionField(path.getMetadata().getName()
						+ "");
				if (descriptionField == null)
					throw new SQLSerializerException("O campo " + path.getMetadata().getName()
							+ " não foi encontrado na classe " + entityCacheOwner.getEntityClass() + ". ");

				if ((inOperation) && (descriptionField.isPrimaryKey() && !descriptionField.isCompositeId())) {
					result = aliases.get(path.getMetadata().getParent().getMetadata().getParent());
					if (result == null) {
						result = (EntityPath<?>) path.getMetadata().getParent().getMetadata().getParent(); // PAP
					}
				} else {
					result = aliases.get((EntityPath<?>) path.getMetadata().getParent());
					if (result == null)
						result = (EntityPath<?>) path.getMetadata().getParent();
				}
			}

		}

		return result;
	}

	/**
	 * Retorna a classe de uma caminho.
	 * 
	 * @param path
	 *            Caminho
	 * @return Classe
	 */
	public Class<?> getClassByEntityPath(EntityPath<?> path) {
		Type mySuperclass = path.getClass().getGenericSuperclass();
		return (Class<?>) ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
	}

	/**
	 * Gera um alias aleatório para uso nas tabelas do SQL.
	 * 
	 * @return
	 */
	private String makeNextAliasTableName() {
		nextAliasTableName++;
		String result = "TB_" + String.valueOf(nextAliasTableName);
		return result;
	}

	/**
	 * Gera um alias aleatório para um alias de uma tabela para uso em uma
	 * coluna.
	 * 
	 * @param alias
	 *            Alias da tabela
	 * @return Alias para coluna gerado.
	 */
	public String makeNextAliasName(String alias) {
		nextAliasColumnName++;
		return adpatAliasColumnName(alias) + "_COL_" + String.valueOf(nextAliasColumnName);
	}

	/**
	 * Ajusta o nome do alias gerado para a coluna para não ultrapassar o máximo
	 * de caracteres permitido pelo dialeto do banco de dados.
	 * 
	 * @param aliasColumnNamePrefix
	 *            Prefixo do nome da coluna
	 * @return Alias ajustado
	 */
	private String adpatAliasColumnName(String aliasColumnNamePrefix) {
		int maximumNameLength = query.getSession().getDialect().getMaxColumnNameSize() - 8;
		String result = adjustName(aliasColumnNamePrefix);

		if (result.length() > maximumNameLength) {
			result = StringUtils.removeAllButAlphaNumericToFit(aliasColumnNamePrefix, maximumNameLength);
			if (result.length() > maximumNameLength) {
				String onlyAlphaNumeric = StringUtils.removeAllButAlphaNumericToFit(aliasColumnNamePrefix, 0);
				result = StringUtils.shortenStringsByRemovingVowelsToFit(onlyAlphaNumeric, "", maximumNameLength);
				if (result.length() > maximumNameLength) {
					String shortenedName = StringUtils.removeVowels(onlyAlphaNumeric);
					if (shortenedName.length() >= maximumNameLength) {
						result = StringUtils.truncate(shortenedName, maximumNameLength);
					} else {
						result = StringUtils.truncate(shortenedName, maximumNameLength - shortenedName.length());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Ajusta o nome fazendo algumas correções.
	 * 
	 * @param name
	 *            Nome
	 * @return Nome ajustado
	 */
	private String adjustName(String name) {
		String adjustedName = name;
		if (adjustedName.indexOf(' ') != -1 || adjustedName.indexOf('\"') != -1 || adjustedName.indexOf('`') != -1) {
			StringBuilder buff = new StringBuilder();
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c != ' ' && c != '\"' && c != '`') {
					buff.append(c);
				}
			}
			adjustedName = buff.toString();
		}
		return adjustedName;
	}

	/**
	 * Retorna a lista de expressões individuais usadas na análise.
	 * 
	 * @return Lista de expressões.
	 */
	public List<Expression<?>> getIndividualExpressions() {
		return individualExpressions;
	}

	/**
	 * Retorna uma lista de definições das classes de resultado que será usado
	 * na montagem dos objetos.
	 * 
	 * @return Lista de definições das classes
	 */
	public List<ResultClassDefinition> getResultClassDefinitions() {
		List<Expression<?>> select = getIndividualExpressions();
		List<ResultClassDefinition> result = new ArrayList<ResultClassDefinition>();
		int index = 1;
		for (Expression<?> expr : select) {
			if (expr instanceof FactoryExpression) {
				FactoryExpression<?> factory = ((FactoryExpression<?>) expr);
				Class<?> resultClass = factory.getType();
				Set<SQLAnalyserColumn> columns = new LinkedHashSet<SQLAnalyserColumn>();
				for (Expression<?> exprFact : factory.getArgs()) {
					Set<SQLAnalyserColumn> tempColumns = resultColumnsFromProjections.get(exprFact);
					if (tempColumns != null) {
						for (SQLAnalyserColumn column : tempColumns) {
							column.setColumnIndex(index);
							columns.add(column);
							index++;
						}
					}
				}
				result.add(new ResultClassDefinition(resultClass, columns));
			} else {
				Set<SQLAnalyserColumn> columns = resultColumnsFromProjections.get(expr);
				if (columns != null) {
					for (SQLAnalyserColumn column : columns) {
						column.setColumnIndex(index);
						index++;
					}
				}
				result.add(new ResultClassDefinition(expr.getType(), columns));
			}
		}
		return result;
	}

	/**
	 * Retorna se a análise concluíu que os usuário utilizou parâmetros
	 * nomeados.
	 * 
	 * @return
	 */
	public boolean isNamedParameter() {
		return (namedParameter == null ? false : namedParameter);
	}

	/**
	 * Retorna número do próximo alias de coluna.
	 * 
	 * @return Próximo número
	 */
	public int getNextAliasColumnName() {
		return nextAliasColumnName;
	}

	public Boolean hasParameters() {
		return hasParameters;
	}

	public Map<Operation<?>, String> getBooleanDefinitions() {
		return booleanDefinitions;
	}

	public Map<Expression<?>, Set<SQLAnalyserColumn>> getParsedPathsOnProjections() {
		return parsedPathsOnProjections;
	}

	public Map<Expression<?>, Set<SQLAnalyserColumn>> getParsedPathsOnOperations() {
		return parsedPathsOnOperations;
	}

	public Map<Expression<?>, Set<SQLAnalyserColumn>> getResultColumnsFromProjections() {
		return resultColumnsFromProjections;
	}

}
