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
 * Visitor que analisa as expressões da consulta processando os nomes path's criando junções e colunas que serão usadas
 * na serialização para SQL pela classe {@link SQLSerializer}.
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
	private Map<Integer, Set<SQLAnalyserColumn>> columns = new HashMap<Integer, Set<SQLAnalyserColumn>>();
	private int nextAliasTableName = 0;
	private int nextAliasColumnName = 0;
	private int level = MAKE_ALIASES;
	private List<Expression<?>> individualExpressions = new ArrayList<Expression<?>>();
	private SQLAnalyserColumn lastColumnAdded = null;

	private boolean inOperation = false;
	private Boolean namedParameter = null;
	private Boolean hasParameters = false;
	private int currentIndex;

	private Map<Operation<?>, String> booleanDefinitions = new HashMap<Operation<?>, String>();

	public SQLAnalyser(AbstractOSQLQuery<?> query) {
		this.query = query;
	}

	@Override
	public Void visit(Constant<?> expr, Void context) {
		if (this.level == MAKE_COLUMNS) {
			Set<SQLAnalyserColumn> columnsForPath = getColumnsListForPath(currentIndex);
			lastColumnAdded = new SQLAnalyserColumn("", "", "", null);
			columnsForPath.add(lastColumnAdded);
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

			if (expr.getType() == Boolean.class) {
				if ((expr.getOperator() == Ops.EQ) || (expr.getOperator() == Ops.NE) || (expr.getOperator() == Ops.GT)
						|| (expr.getOperator() == Ops.GOE) || (expr.getOperator() == Ops.LT) || (expr.getOperator() == Ops.LOE)) {
					analyzeEqualsOperation(expr);
				} else if ((expr.getOperator() == Ops.IS_NOT_NULL) || (expr.getOperator() == Ops.IS_NULL)) {
					analyzeNullOperation(expr);
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
				List<DescriptionColumn> leftColumns = getDescriptionColumnsFromExpression(leftExpression);
				String leftAlias = getAliasFromExpression(leftExpression);
				StringBuilder booleanAsString = new StringBuilder();
				boolean appendAnd = false;
				for (int i = 0; i < leftColumns.size(); i++) {
					if (appendAnd)
						booleanAsString.append(" AND ");
					booleanAsString.append(leftAlias).append(".").append(leftColumns.get(i).getColumnName());
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
				List<DescriptionColumn> leftColumns = getDescriptionColumnsFromExpression(leftExpression);
				String leftAlias = getAliasFromExpression(leftExpression);
				String rightAlias = getAliasFromExpression(rigthExpression);
				List<DescriptionColumn> rightColumns = getDescriptionColumnsFromExpression(rigthExpression);
				StringBuilder booleanAsString = new StringBuilder();
				boolean appendAnd = false;
				for (int i = 0; i < leftColumns.size(); i++) {
					if (appendAnd)
						booleanAsString.append(" AND ");
					booleanAsString.append(leftAlias).append(".").append(leftColumns.get(i).getColumnName()).append(" ");

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

					booleanAsString.append(" ").append(rightAlias).append(".").append(rightColumns.get(i).getColumnName());
					appendAnd = true;
				}

				booleanDefinitions.put(expr, booleanAsString.toString());
			}
		}
	}

	protected String getAliasFromExpression(Expression<?> expression) {
		if (expression instanceof EntityPath<?>) {
			if ((expression instanceof Path<?>) && (aliases.get((Path<?>) expression) != null)) {
				return aliases.get((Path<?>) expression).getMetadata().getName();
			}
			return ((EntityPath<?>) expression).getMetadata().getName();
		} else if (expression instanceof Path<?>) {
			if (((Path<?>) expression).getMetadata().isRoot()) {
				return ((Path<?>) expression).getMetadata().getName();
			} else {
				Path<?> parent = ((Path<?>) expression).getMetadata().getParent();
				if (!(parent.getMetadata().isRoot()) && (aliases.get(parent) != null)) {
					return aliases.get(parent).getMetadata().getName();
				}
				return parent.getMetadata().getName();
			}
		} else
			throw new RuntimeException("Este erro não deve ocorrer");
	}

	protected List<DescriptionColumn> getDescriptionColumnsFromExpression(Expression<?> expression) {
		List<DescriptionColumn> result = null;
		if (expression instanceof EntityPath<?>) {
			if (((Path<?>) expression).getMetadata().isRoot()) {
				EntityCache entityCache = query.getSession().getEntityCacheManager()
						.getEntityCache(this.getClassByEntityPath((EntityPath<?>) expression));
				result = entityCache.getPrimaryKeyColumns();
			} else {
				Class<?> sourceClass = ((Path<?>) expression).getMetadata().getParent().getType();
				EntityCache entityCache = query.getSession().getEntityCacheManager().getEntityCache(sourceClass);
				DescriptionField descriptionField = entityCache.getDescriptionField(((Path<?>) expression).getMetadata().getElement() + "");
				result = descriptionField.getDescriptionColumns();
			}
		} else if (expression instanceof Path<?>) {
			Class<?> sourceClass = null;
			if (((Path<?>) expression).getMetadata().isRoot()) {
				sourceClass = ((Path<?>) expression).getType();
				EntityCache entityCache = query.getSession().getEntityCacheManager().getEntityCache(sourceClass);
				result = entityCache.getPrimaryKeyColumns();
			} else {
				sourceClass = ((Path<?>) expression).getMetadata().getParent().getType();
				EntityCache entityCache = query.getSession().getEntityCacheManager().getEntityCache(sourceClass);
				DescriptionField descriptionField = entityCache.getDescriptionField(((Path<?>) expression).getMetadata().getElement() + "");
				result = descriptionField.getDescriptionColumns();
			}

		} else
			throw new RuntimeException("Este erro não deve ocorrer");
		return result;
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
				makePossibleJoins(expr);
			} catch (Exception e) {
				throw new SQLAnalyserException(e.getMessage(), e);
			}
		} else if (level == MAKE_COLUMNS) {
			processColumns(expr);
		}
		return null;
	}

	/**
	 * Processa o caminho e monta os nomes das colunas que serão usadas na serialização do SQL.
	 * 
	 * @param path
	 *            Caminho
	 */
	private void processColumns(Path<?> path) {
		/*
		 * Ser for uma variável e for uma entidade
		 */
		if ((path.getMetadata().getPathType() == PathType.VARIABLE) && (path instanceof EntityPath<?>)) {
			EntityCache sourceEntityCache = this.query.getSession().getEntityCacheManager()
					.getEntityCache(getClassByEntityPath((EntityPath<?>) path));
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + getClassByEntityPath((EntityPath<?>) path)
						+ " não foi encontrada na lista de entidades gerenciadas.");

			String alias = path.getMetadata().getName();
			if (((EntityPath<?>) path).getCustomProjection().size() > 0) {
				for (Path<?> customPath : ((EntityPath<?>) path).getCustomProjection()) {
					if (customPath.equals(path))
						processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
					else
						processColumns(customPath);
				}
			} else
				processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
		} else if (path.getMetadata().getPathType() == PathType.PROPERTY) {
			/*
			 * Se for uma propriedade
			 */
			EntityPath<?> entityPath = this.getAliasByEntityPath(path);
			String alias = entityPath.getMetadata().getName();

			EntityCache sourceEntityCache = this.query.getSession().getEntityCacheManager().getEntityCache(getClassByEntityPath(entityPath));
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + getClassByEntityPath(entityPath)
						+ " não foi encontrada na lista de entidades gerenciadas.");
			/*
			 * Se o caminho for uma entidade processa os campos da entidade gerando colunas. Se foi configurado
			 * projeções customizadas ou para serem excluidas processa apenas os caminhos filtrados.
			 */
			if (path instanceof EntityPath<?>) {
				if (((EntityPath<?>) path).getCustomProjection().size() > 0) {
					for (Path<?> customPath : ((EntityPath<?>) path).getCustomProjection()) {
						if (customPath.equals(path))
							processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
						else
							processColumns(customPath);
					}
				} else
					processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
			} else {
				/*
				 * Processa apenas a propriedade
				 */
				DescriptionField descriptionField = sourceEntityCache.getDescriptionField(path.getMetadata().getName());
				if (descriptionField == null)
					throw new SQLSerializerException("O campo " + path.getMetadata().getName() + " não foi encontrado na classe "
							+ getClassByEntityPath(entityPath) + ". ");
				processDescriptionField(currentIndex, alias, descriptionField, true);
			}
		} else if (path.getMetadata().getPathType() == PathType.VARIABLE) {
			/*
			 * Se o estágio da análise for MAKE_COLUMNS e havia sido adicionado uma coluna associa o alias atribuido
			 * pelo usuário sobrepondo o gerado e marca que o usuário atribui um alias.
			 */
			if (level == MAKE_COLUMNS) {
				if (lastColumnAdded != null) {
					lastColumnAdded.setAliasColumnName(path.getMetadata().getName());
					lastColumnAdded.setUserAliasDefined(true);
				}
			}
		}

	}

	/**
	 * Processa o campo da entidade e gera um coluna para o mesmo. Processa apenas campos simples e relacionamentos.
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
	protected boolean processDescriptionField(Integer index, String alias, DescriptionField descriptionField, boolean makeAlias) {
		Set<SQLAnalyserColumn> columnsForPath = getColumnsListForPath(index);
		if (descriptionField.isSimple()) {
			String aliasColumnName = "";
			if (makeAlias)
				aliasColumnName = makeNextAliasName(alias);
			lastColumnAdded = new SQLAnalyserColumn(alias, descriptionField.getSimpleColumn().getColumnName(), aliasColumnName, descriptionField);
			columnsForPath.add(lastColumnAdded);
			return true;
		} else if (descriptionField.isRelationShip()) {
			for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
				String aliasColumnName = "";
				if (makeAlias)
					aliasColumnName = makeNextAliasName(alias);
				lastColumnAdded = new SQLAnalyserColumn(alias, column.getColumnName(), aliasColumnName, descriptionField);
				columnsForPath.add(lastColumnAdded);
			}
			return true;
		}
		return false;
	}

	/**
	 * Retorna uma lista de colunas para um indice dentro das expressões armazenadas no cache. Se não encontrar retorna
	 * uma nova lista.
	 * 
	 * @param index
	 *            Índice
	 * @return Lista de colunas
	 */
	private Set<SQLAnalyserColumn> getColumnsListForPath(Integer index) {
		Set<SQLAnalyserColumn> result = null;
		if (columns.containsKey(index))
			result = columns.get(index);
		if (result == null) {
			result = new LinkedHashSet<SQLAnalyserColumn>();
			columns.put(index, result);
		}

		return result;
	}

	/**
	 * Retorna a lista de colunas para o indice dentro das expressões no formato String.
	 * 
	 * @param index
	 *            Índice
	 * @return Lista de colunas
	 */
	private List<String> getColumnsForPathAsString(Integer index) {
		List<String> result = new ArrayList<String>();
		Set<SQLAnalyserColumn> columns = getColumnsListForPath(index);
		for (SQLAnalyserColumn column : columns) {
			result.add((StringUtils.isEmpty(column.getAliasColumnName()) ? column.getColumnName() : column.getAliasColumnName()));
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
	protected void processAllDescriptionFields(EntityPath<?> entityPath, String alias, EntityCache sourceEntityCache) {

		List<Path<?>> excludeProjection = entityPath.getExcludeProjection();
		for (DescriptionField descriptionField : sourceEntityCache.getDescriptionFields()) {
			/*
			 * Não processa nenhum tipo de coleção e caminhos excluídos da projeção
			 */
			if (descriptionField.isAnyCollection() || hasPathForDescriptionFieldToExclude(excludeProjection, descriptionField))
				continue;

			processDescriptionField(currentIndex, alias, descriptionField, true);
		}
	}

	/**
	 * Retorna se um caminho for excluído da projeção de uma entidade através do método
	 * {@link EntityPath#excludeProjection(Path...)}
	 * 
	 * @param excludeProjection
	 * @param descriptionField
	 * @return Verdadeiro se o campo faz parte da lista de exclusão.
	 */
	private boolean hasPathForDescriptionFieldToExclude(List<Path<?>> excludeProjection, DescriptionField descriptionField) {
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
			Set<SQLAnalyserColumn> columnsForPath = getColumnsListForPath(currentIndex);
			lastColumnAdded = new SQLAnalyserColumn("", "", "", null);
			columnsForPath.add(lastColumnAdded);
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
	}

	/**
	 * Extrai a lista de expressões de forma individualizada para serem processadas. Expressões contidas em fábricas de
	 * expressões serão retornadas de forma individual.
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
	 * Processa lista de expressões individuais de acordo como nível(estágio) da análise.
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
		final List<JoinExpression> joins = metadata.getJoins();

		/*
		 * Processa apenas o Select criando aliases colunas e junções
		 */
		if ((this.level == MAKE_ALIASES) || (this.level == MAKE_COLUMNS)) {
			currentIndex = 0;
			for (Expression<?> expr : select) {
				expr.accept(this, null);
				if (expr instanceof EntityPath<?>) {
					List<Path<?>> customProjection = ((EntityPath<?>) expr).getCustomProjection();
					for (Path<?> path : customProjection) {
						if (!(path.equals(expr)))
							makePossibleJoins(path);
					}
					for (Path<?> path : customProjection) {
						if (!(path.equals(expr)))
							processColumns(path);
					}

				}
				currentIndex++;
			}
		}
		/*
		 * Processa demais cláusulas somente criando os aliases das colunas
		 */
		if (this.level == MAKE_ALIASES) {
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
			if (where != null)
				where.accept(this, null);
			if (having != null)
				having.accept(this, null);
			if (groupBy != null) {
				currentIndex = 0;
				for (Expression<?> expr : select) {
					expr.accept(this, null);
					currentIndex++;
				}
			}
			if (orderBy != null) {
				for (OrderSpecifier<?> ord : orderBy) {
					ord.getTarget().accept(this, null);
				}
			}
		}
	}

	/**
	 * Verifica se é possível criar uma junção para um caminho utilizado nas expressões.
	 * 
	 * @param expr
	 *            Expressão.
	 * @throws Exception
	 *             Exceção ocorrida durante criação da junção.
	 */
	protected void makePossibleJoins(Path<?> expr) throws Exception {
		if ((expr instanceof DiscriminatorValuePath) || (expr instanceof DiscriminatorColumnPath) || (expr.getMetadata().isRoot()))
			return;
		makePossibleJoins(expr.getMetadata().getParent());
		if (expr instanceof EntityPath) {
			if (!aliases.containsKey(expr)) {
				String alias = makeNextAliasTableName();
				EntityPath<?> newPath = (EntityPath<?>) ReflectionUtils.invokeConstructor(expr.getClass(), alias);
				BooleanExpression boExpression = (BooleanExpression) ReflectionUtils.invokeMethod(expr, "eq", newPath);
				query.leftJoin(newPath).on(boExpression);
				aliases.put(expr, newPath);

				analyzeEqualsOperation((BooleanOperation) boExpression);
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
	public EntityPath<?> getAliasByEntityPath(Path<?> path) {
		EntityPath<?> result = null;
		if (path instanceof EntityPath<?>) {
			result = aliases.get(path);
			if (result == null)
				result = (EntityPath<?>) path.getMetadata().getParent();
		} else {
			result = aliases.get(path.getMetadata().getParent());
			if (result == null)
				result = (EntityPath<?>) path.getMetadata().getParent();
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
	 * Gera um alias aleatório para um alias de uma tabela para uso em uma coluna.
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
	 * Ajusta o nome do alias gerado para a coluna para não ultrapassar o máximo de caracteres permitido pelo dialeto do
	 * banco de dados.
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

	public Map<Integer, Set<SQLAnalyserColumn>> getColumns() {
		return columns;
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
	 * Retorna uma lista de definições das classes de resultado que será usado na montagem dos objetos.
	 * 
	 * @return Lista de definições das classes
	 */
	public List<ResultClassDefinition> getResultClassDefinitions() {
		List<Expression<?>> select = getIndividualExpressions();
		List<ResultClassDefinition> result = new ArrayList<ResultClassDefinition>();
		int index = 0;
		for (Expression<?> expr : select) {
			if (expr instanceof FactoryExpression) {
				FactoryExpression<?> factory = ((FactoryExpression<?>) expr);
				Class<?> resultClass = factory.getType();
				List<String> columns = new ArrayList<String>();
				for (Expression<?> exprFact : factory.getArgs()) {
					columns.addAll(getColumnsForPathAsString(index));
					index++;
				}
				result.add(new ResultClassDefinition(resultClass, getColumnsListForPath(index)));
			} else {
				result.add(new ResultClassDefinition(expr.getType(), getColumnsListForPath(index)));
				index++;
			}
		}
		return result;
	}

	/**
	 * Retorna se a análise concluíu que os usuário utilizou parâmetros nomeados.
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

}
