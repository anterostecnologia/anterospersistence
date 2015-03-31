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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import br.com.anteros.persistence.dsl.osql.types.IndexHint;
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
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorColumnPath;
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorValuePath;
import br.com.anteros.persistence.dsl.osql.types.path.PathBuilder;
import br.com.anteros.persistence.dsl.osql.types.path.SetPath;
import br.com.anteros.persistence.handler.ResultClassColumnInfo;
import br.com.anteros.persistence.handler.ResultClassDefinition;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

/**
 * Visitor que analisa as expressões da consulta processando os nomes path's criando junções e colunas que serão usadas
 * na serialização para SQL pela
 * 
 * @author edson
 *
 */
public class SQLAnalyser implements Visitor<Void, Void> {

	private static int MAKE_ALIASES = 1;
	private static int MAKE_COLUMNS = 2;
	private static final String COMMA = ", ";
	private QueryMetadata mainMetadata;
	private Map<Path<?>, EntityPath<?>> createdAliasesForDynamicJoins = new HashMap<Path<?>, EntityPath<?>>();
	private Map<Expression<?>, Set<SQLAnalyserColumn>> parsedPathsOnProjections = new LinkedHashMap<Expression<?>, Set<SQLAnalyserColumn>>();
	private Map<Expression<?>, Set<SQLAnalyserColumn>> parsedPathsOnOperations = new LinkedHashMap<Expression<?>, Set<SQLAnalyserColumn>>();
	private Map<Expression<?>, Set<SQLAnalyserColumn>> resultColumnsFromProjections = new LinkedHashMap<Expression<?>, Set<SQLAnalyserColumn>>();
	private Map<Path<?>, String> mappedByForPath = new HashMap<Path<?>, String>();
	private int level = MAKE_ALIASES;
	private List<Expression<?>> individualExpressions = new ArrayList<Expression<?>>();
	private SQLAnalyserColumn lastColumnAdded = null;
	private Set<QueryMetadata> allMetadatas = new LinkedHashSet<QueryMetadata>();

	protected enum Stage {
		SELECT, FROM, WHERE, GROUP_BY, HAVING, ORDER_BY
	}

	protected Stage stage = Stage.SELECT;

	private boolean inOperation = false;
	private boolean inSubQuery = false;
	private Boolean namedParameter = null;
	private Boolean hasParameters = false;

	private Map<Operation<?>, String> booleanDefinitions = new HashMap<Operation<?>, String>();
	private Expression<?> currentExpressionOnMakeColumns;
	private Configuration configuration;
	private Expression<?> union;

	public SQLAnalyser(QueryMetadata metadata, Configuration configuration, Expression<?> union) {
		this.mainMetadata = metadata;
		this.configuration = configuration;
		this.union = union;
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
					if ((expr.getOperator() == Ops.EQ) || (expr.getOperator() == Ops.NE) || (expr.getOperator() == Ops.GT) || (expr.getOperator() == Ops.GOE)
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
					booleanAsString.append(leftColumn.getAliasTableName()).append(".").append(leftColumn.getColumnName()).append(" ");
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

			/*
			 * Igualdade de 2 entidades
			 */
			if (isEntity(leftExpression) && (isEntity(rigthExpression))) {

				if (((Path<?>) leftExpression).getMetadata().getPathType() == PathType.COLLECTION_ANY) {
					leftExpression = ((Path<?>) leftExpression).getMetadata().getParent();
				}

				if (((Path<?>) rigthExpression).getMetadata().getPathType() == PathType.COLLECTION_ANY) {
					rigthExpression = ((Path<?>) rigthExpression).getMetadata().getParent();
				}

				Set<SQLAnalyserColumn> leftColumns = parsedPathsOnOperations.get(leftExpression);
				Set<SQLAnalyserColumn> rightColumns = parsedPathsOnOperations.get(rigthExpression);

				StringBuilder booleanAsString = new StringBuilder();
				boolean appendAnd = false;
				Iterator<SQLAnalyserColumn> itRightColumns = rightColumns.iterator();
				for (SQLAnalyserColumn leftColumn : leftColumns) {
					SQLAnalyserColumn rightColumn = itRightColumns.next();
					if (appendAnd)
						booleanAsString.append(" AND ");
					booleanAsString.append(leftColumn.getAliasTableName()).append(".").append(leftColumn.getColumnName()).append(" ");

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

					booleanAsString.append(" ").append(rightColumn.getAliasTableName()).append(".").append(rightColumn.getColumnName());
					appendAnd = true;
				}

				booleanDefinitions.put(expr, booleanAsString.toString());
			} else if ((leftExpression instanceof Path<?>) && (rigthExpression instanceof Constant<?>)
					&& (((Constant<?>) rigthExpression).getConstant() instanceof Boolean)) {
				analyzeIsTrueOperation(expr, leftExpression, rigthExpression);
			}
		}
	}

	protected void analyzeIsTrueOperation(Operation<?> expr, Expression<?> leftExpression, Expression<?> rigthExpression) {
		/*
		 * Igualdade com verdadeiro/falso = isTrue
		 */
		Set<SQLAnalyserColumn> leftColumns = parsedPathsOnOperations.get(leftExpression);
		if (leftColumns.size() > 1)
			throw new SQLAnalyserException(
					"Não é possível fazer comparações verdadeiras ou falsas em chaves compostas. Informe o campos individualmente. Expressão " + leftExpression);

		Path<?> entityPath = this.getAppropriateAliasByEntityPath((Path<?>) leftExpression);
		if (entityPath != null) {
			EntityCache entityCache = getEntityCacheByClass(entityPath.getType());
			DescriptionField descriptionField = entityCache.getDescriptionField(((Path<?>) leftExpression).getMetadata().getElement() + "");
			if (descriptionField != null) {

				Boolean value = (Boolean) ((Constant<?>) rigthExpression).getConstant();
				Object booleanValue = descriptionField.getBooleanValue(value);
				StringBuilder booleanAsString = new StringBuilder();
				booleanAsString.append(leftColumns.iterator().next().getAliasTableName()).append(".").append(leftColumns.iterator().next().getColumnName())
						.append(" = ").append(configuration.asLiteral(booleanValue));
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

		leftEntityCache = configuration.getEntityCacheManager().getEntityCache(sourceClass);
		return leftEntityCache != null;
	}

	@Override
	public Void visit(ParamExpression<?> expr, Void context) {
		if (namedParameter != null) {
			if (namedParameter != (!expr.isAnon())) {
				throw new SQLAnalyserException("Foram encontrados parâmetros nomeados e não nomeados. Use apenas um formato de parâmetros na consulta.");
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
			processColumns(expr, null, null);
		}
		return null;
	}

	/**
	 * Processa o caminho e monta os nomes das colunas que serão usadas na serialização do SQL.
	 * 
	 * @param path
	 *            Caminho
	 */
	private void processColumns(Path<?> path, Path<?> keyPath, String aliasTableName) {

		if (path.getMetadata().getParent() instanceof PathBuilder<?>) {
			Path<?> sourceOfTargetPath = ((PathBuilder<?>) path.getMetadata().getParent()).getSourceOfTargetPath(path);
			if (sourceOfTargetPath != null) {
				processColumns(sourceOfTargetPath, path, path.getMetadata().getParent().getMetadata().getName());
			}
			return;
		}

		if (path.getMetadata().getPathType() == PathType.COLLECTION_ANY) {
			Path<?> targetPath = path.getMetadata().getParent();
			processColumns(targetPath, keyPath, null);
			return;
		}

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

		if ((path.getMetadata().getPathType() == PathType.VARIABLE) && (this.configuration.getEntityCacheManager().isEntity(path.getType()))) {
			/*
			 * Ser for uma variável e for uma entidade Ex: tOrder = new TOrder("ORD")
			 */
			EntityCache sourceEntityCache = getEntityCacheByClass(path.getType());
			if (aliasTableName == null)
				aliasTableName = path.getMetadata().getName();
			/*
			 * Somente processa projeções customizadas se não estiver dentro de uma operação
			 */
			if ((!inOperation) && (((EntityPath<?>) path).getCustomProjection().size() > 0)) {
				for (Path<?> customPath : ((EntityPath<?>) path).getCustomProjection()) {
					if (customPath.equals(path))
						processAllFields((keyPath == null ? path : keyPath), ((EntityPath<?>) path).getExcludeProjection(), aliasTableName, sourceEntityCache,
								inOperation);
					else
						processColumns(customPath, (keyPath == null ? path : keyPath), null);
				}
			} else {
				processAllFields((keyPath == null ? path : keyPath), null, aliasTableName, sourceEntityCache, inOperation);
			}

		} else if ((path.getMetadata().getPathType() == PathType.PROPERTY)) {
			/*
			 * Ser for uma propriedade de uma Entidade. Ex. tOrder.dtInvoice ou tOrder.person ou tOrder.person.id. Se
			 * for uma propriedade da entidade, pega o pai mais adequado.
			 */
			Path<?> entityPath = this.getAppropriateAliasByEntityPath(path);
			if (aliasTableName == null)
				aliasTableName = entityPath.getMetadata().getName();
			Path<?> targetPath = path;
			/*
			 * Verifica se a propriedade é um ID então pega como caminho o pai dela
			 */
			EntityCache sourceEntityCache = getEntityCacheByClass(entityPath.getType());
			/*
			 * Se estiver numa operação e o caminho for chave primária troca o path para a entidade da chave
			 */
			if (inOperation && !path.getMetadata().getParent().getMetadata().isRoot()
					&& !(path.getMetadata().getParent().getMetadata().getPathType() == PathType.COLLECTION_ANY)) {
				EntityCache targetEntityCache = getEntityCacheByClass(path.getMetadata().getParent().getType());
				DescriptionField descriptionFieldPath = getDescriptionFieldByPath(targetEntityCache, path.getMetadata().getName() + "");
				if (descriptionFieldPath.isPrimaryKey() && !descriptionFieldPath.isCompositeId()) {
					targetPath = path.getMetadata().getParent();
				}
			}

			if (((targetPath instanceof EntityPath<?>) || (targetPath instanceof SetPath<?, ?>)) && (!inOperation)) {
				/*
				 * Projeta a chave da entidade na classe de origem
				 */
				if (targetPath instanceof EntityPath<?>) {
					Path<?> targetEntityPath = this.getAppropriateAliasByEntityPath(path.getMetadata().getParent());
					EntityCache targetEntityCache = getEntityCacheByClass(path.getMetadata().getParent().getType());
					DescriptionField descriptionField = getDescriptionFieldByPath(targetEntityCache, path.getMetadata().getName() + "");
					processSingleField((keyPath == null ? path : keyPath), targetEntityPath.getMetadata().getName(), descriptionField, true);
				}

				/*
				 * Somente processa projeções customizadas se não estiver dentro de uma operação
				 */
				EntityPath<?> targetEntityPath = null;
				if (targetPath instanceof SetPath<?, ?>) {
					targetEntityPath = (EntityPath<?>) (((SetPath<?, ?>) targetPath).getAny());
				} else {
					targetEntityPath = ((EntityPath<?>) targetPath);
				}

				if (targetEntityPath.getCustomProjection().size() > 0) {
					for (Path<?> customPath : targetEntityPath.getCustomProjection()) {
						if (customPath.equals(targetEntityPath))
							processAllFields((keyPath == null ? path : keyPath), targetEntityPath.getExcludeProjection(), aliasTableName, sourceEntityCache,
									inOperation);
						else
							processColumns(customPath, (keyPath == null ? path : keyPath), null);
					}
				} else
					processAllFields((keyPath == null ? path : keyPath), targetEntityPath.getExcludeProjection(), aliasTableName, sourceEntityCache,
							inOperation);
			} else {
				DescriptionField descriptionField = getDescriptionFieldByPath(sourceEntityCache, targetPath.getMetadata().getElement() + "");
				processSingleField((keyPath == null ? path : keyPath), aliasTableName, descriptionField, true);
			}

		} else if (path.getMetadata().getPathType() == PathType.VARIABLE) {
			/*
			 * Demais casos incluindo aliases de colunas. Se o estágio da análise for MAKE_COLUMNS e havia sido
			 * adicionado uma coluna associa o alias atribuido pelo usuário sobrepondo o gerado e marca que o usuário
			 * atribui um alias.
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
	 * Retorna o DescriptionField da entidade baseado no seu nome. Se a entidade for abstrata procura em todas as
	 * classes concretas.
	 * 
	 * @param entityCache
	 *            Entidade
	 * @param fieldName
	 *            Nome do campo
	 * @return DescriptionField encontrado ou null se não existe
	 */
	protected DescriptionField getDescriptionFieldByPath(EntityCache entityCache, String fieldName) {
		EntityCache caches[] = { entityCache };
		/*
		 * Se for uma classe abstrata pega as classes concretas
		 */
		if (entityCache.isAbstractClass())
			caches = configuration.getEntityCacheManager().getEntitiesBySuperClass(entityCache);
		for (EntityCache cache : caches) {
			DescriptionField result = cache.getDescriptionField(fieldName);
			if (result != null) {
				return result;
			}
		}
		throw new SQLSerializerException("O campo " + fieldName + " não foi encontrado na classe " + entityCache.getEntityClass() + ". ");
	}

	/**
	 * Retorna a entidade do cache baseado na classe.
	 * 
	 * @param sourceClass
	 *            Classe
	 * @return Entidade
	 */
	protected EntityCache getEntityCacheByClass(Class<?> sourceClass) {
		EntityCache result = this.configuration.getEntityCacheManager().getEntityCache(sourceClass);
		if (result == null)
			throw new SQLSerializerException("A classe " + sourceClass + " não foi encontrada na lista de entidades gerenciadas.");
		return result;
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
	protected boolean processSingleField(Path<?> keyPath, String alias, DescriptionField descriptionField, boolean makeAlias) {

		if (descriptionField.isSimple()) {
			String aliasColumnName = "";
			if (makeAlias)
				aliasColumnName = configuration.makeNextAliasName(alias);
			lastColumnAdded = new SQLAnalyserColumn(alias, descriptionField.getSimpleColumn().getColumnName(), aliasColumnName, descriptionField, 0);
			if (inOperation)
				getColumnListOperation(keyPath).add(lastColumnAdded);
			else
				getColumnListProjection(keyPath).add(lastColumnAdded);

			if (level == MAKE_COLUMNS) {
				Set<SQLAnalyserColumn> columnsFromProjection = getResultColumnsFromProjection(currentExpressionOnMakeColumns);
				columnsFromProjection.add(lastColumnAdded);
			}

			return true;
		} else if (descriptionField.isRelationShip() || (descriptionField.isCollectionEntity())) {
			List<DescriptionColumn> columns = descriptionField.getDescriptionColumns();
			if (descriptionField.isCollectionEntity()) {
				DescriptionField mappedByField = descriptionField.getTargetEntity().getDescriptionField(descriptionField.getMappedBy());
				columns = mappedByField.getDescriptionColumns();
			}
			for (DescriptionColumn column : columns) {
				String aliasColumnName = "";
				if (makeAlias)
					aliasColumnName = configuration.makeNextAliasName(alias);
				lastColumnAdded = new SQLAnalyserColumn(alias, column.getColumnName(), aliasColumnName, descriptionField, 0);

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
	protected void processAllFields(Path<?> keyPath, Set<Path<?>> excludeProjection, String alias, EntityCache sourceEntityCache, boolean onlyPrimaryKey) {
		EntityCache caches[] = { sourceEntityCache };
		/*
		 * Se for uma classe abstrata pega as classes concretas
		 */
		if (sourceEntityCache.isAbstractClass())
			caches = configuration.getEntityCacheManager().getEntitiesBySuperClass(sourceEntityCache);

		Set<String> distinctFields = new HashSet<String>();
		for (EntityCache entityCache : caches) {

			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				/*
				 * Se for para gerar somente campos da chave primária da entidade ou se o campo faz parte da lista de
				 * exclusão
				 */
				if (onlyPrimaryKey) {
					if (mappedByForPath.containsKey(keyPath)) {
						if (!mappedByForPath.get(keyPath).equals(descriptionField.getField().getName()))
							continue;
					} else {
						if (!descriptionField.isPrimaryKey() || hasPathForDescriptionFieldToExclude(excludeProjection, descriptionField))
							continue;
					}
				} else {
					/*
					 * Não processa nenhum tipo de coleção e caminhos excluídos da projeção
					 */
					if (descriptionField.isAnyCollection() || hasPathForDescriptionFieldToExclude(excludeProjection, descriptionField))
						continue;
				}

				if (!distinctFields.contains(descriptionField.getField().getName())) {
					processSingleField(keyPath, alias, descriptionField, true);
					distinctFields.add(descriptionField.getField().getName());
				}
			}
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
	private boolean hasPathForDescriptionFieldToExclude(Set<Path<?>> excludeProjection, DescriptionField descriptionField) {
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
		boolean oldInOperation = inOperation;
		this.inSubQuery = true;
		Stage oldStage = stage;
		this.inOperation = false;
		this.allMetadatas.add(expr.getMetadata());
		try {
			if (level == MAKE_ALIASES) {
				try {
					processExpressions(expr.getMetadata(), MAKE_ALIASES);
				} catch (Exception e) {
					throw new SQLAnalyserException(e);
				}

			} else if (level == MAKE_COLUMNS) {
				try {
					processExpressions(expr.getMetadata(), MAKE_COLUMNS);
					makeIndexHints(expr.getMetadata());
				} catch (Exception e) {
					throw new SQLAnalyserException(e);
				}
			}
		} finally {
			this.inOperation = oldInOperation;
			this.stage = oldStage;
			this.inSubQuery = false;
		}
		return null;
	}

	@Override
	public Void visit(TemplateExpression<?> expr, Void context) {
		if (this.level == MAKE_COLUMNS) {

			for (Object arg : expr.getArgs()) {
				if (arg instanceof Expression<?>)
					((Expression<?>) arg).accept(this, null);
			}

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
		this.individualExpressions = SQLAnalyser.extractIndividualColumnsExpression(mainMetadata);
		this.allMetadatas.clear();
		this.allMetadatas.add(mainMetadata);
		processExpressions(mainMetadata, MAKE_ALIASES);
		processExpressions(mainMetadata, MAKE_COLUMNS);
		makeIndexHints(mainMetadata);
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
	 * Processa as espressões que contenham projeções customizadas
	 * 
	 * @param expr
	 *            expressão a ser processada
	 * @throws Exception
	 */
	private void processCustomProjection(Expression<?> expr) throws Exception {
		if (expr instanceof EntityPath<?>) {
			Set<Path<?>> customProjection = ((EntityPath<?>) expr).getCustomProjection();
			if (customProjection.size() > 0) {
				for (Path<?> path : customProjection) {
					if (path instanceof EntityPath<?>) {
						processCustomProjection(path);
					} else {
						if (!(path.equals(expr)))
							makePossibleJoins(path, null);
					}
				}
			} else {
				makePossibleJoins((Path<?>) expr, null);
			}
		}
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
		final List<Expression<?>> select = SQLAnalyser.extractIndividualColumnsExpression(metadata);
		final Predicate where = metadata.getWhere();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();
		final List<Expression<?>> groupBy = metadata.getGroupBy();
		List<JoinExpression> joins = metadata.getJoins();

		/*
		 * Processa apenas o Select criando aliases colunas e junções
		 */
		stage = Stage.SELECT;
		if (!hasUnion() || inSubQuery) {
			for (Expression<?> expr : select) {
				currentExpressionOnMakeColumns = expr;
				expr.accept(this, null);
				processCustomProjection(expr);
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
				if (expr.getCondition() != null) {
					expressionsJoins.add(expr.getCondition());
				} else if ((expr.getTarget() != null) && (expr.getTarget() instanceof Operation<?>)) {
					expressionsJoins.add(expr.getTarget());
				}
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

		if (!hasUnion() || inSubQuery) {
			stage = Stage.GROUP_BY;
			if (groupBy != null) {
				for (Expression<?> expr : groupBy) {
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

		if (metadata == mainMetadata) {
			if (union != null) {
				union.accept(this, null);
			}
		}

		if (level == MAKE_COLUMNS) {
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
	}

	/**
	 * Verifica se é possível criar uma junção para um caminho utilizado nas expressões.
	 * 
	 * @param expr
	 *            Expressão.
	 * @throws Exception
	 *             Exceção ocorrida durante criação da junção.
	 */
	protected void makePossibleJoins(Path<?> expr, Path<?> owner) throws Exception {
		if ((expr instanceof DiscriminatorValuePath) || (expr instanceof DiscriminatorColumnPath) || (expr.getMetadata().isRoot())
				|| (expr instanceof SetPath<?, ?>))
			return;
		makePossibleJoins(expr.getMetadata().getParent(), (owner == null ? expr : owner));

		EntityCache entityCache = this.configuration.getEntityCacheManager().getEntityCache(expr.getType());

		boolean isEntity = (entityCache != null);
		boolean isExpressionMain = (owner == null);
		boolean isOwnerOfExpressionMain = (!isExpressionMain) && (owner.getMetadata().getParent() == expr);
		boolean isPrimaryKey = false;

		if (isOwnerOfExpressionMain) {
			Object element = owner.getMetadata().getElement();
			DescriptionField descriptionField = entityCache.getDescriptionField(element + "");
			isPrimaryKey = (descriptionField != null) && (descriptionField.isPrimaryKey() && !descriptionField.isCompositeId());
		}

		/*
		 * Se for uma entidade e não for pai ou a expressão principal (expression main = onde inicia o processamento
		 * recursivo)
		 */
		boolean firstCondition = (isEntity && !isExpressionMain && !isOwnerOfExpressionMain);
		/*
		 * Se for uma entidade e for a expressão principal e o estágio for diferente do FROM e WHERE.
		 */
		boolean secondCondition = (isEntity && isExpressionMain && (stage != Stage.FROM) && (stage != Stage.WHERE));
		/*
		 * Se for uma entidade e não for a expressão principal e for o pai da expressão principal e a mesma não for
		 * chave primária.
		 */
		boolean thirdCondition = (isEntity && !isExpressionMain && isOwnerOfExpressionMain && !isPrimaryKey);
		/*
		 * Se for uma entidade e não for a expressão principal e for o pai da expressão e o estágio for diferente de
		 * FROM e where.
		 */
		boolean fourthCondition = (isEntity && !isExpressionMain && isOwnerOfExpressionMain && (((stage != Stage.FROM) && (stage != Stage.WHERE)) || (expr
				.getMetadata().getPathType() == PathType.COLLECTION_ANY)));

		if ((firstCondition) || (secondCondition) || (thirdCondition) || (fourthCondition)) {

			Path<?> exprOwner = ((expr.getMetadata().getPathType() == PathType.COLLECTION_ANY) ? (SetPath) expr.getMetadata().getParent() : expr);

			if (!createdAliasesForDynamicJoins.containsKey(exprOwner)) {
				String alias = configuration.makeNextAliasTableName();
				EntityPath<?> newPath = null;
				BooleanExpression boExpression = null;
				if (expr.getMetadata().getPathType() == PathType.COLLECTION_ANY) {
					SetPath parent = (SetPath) expr.getMetadata().getParent();
					EntityPath<?> entityPath = (EntityPath<?>) getAppropriateAliasByEntityPath(parent.getMetadata().getParent());
					EntityCache sourceEntityCache = getEntityCacheByClass(entityPath.getType());
					DescriptionField descriptionField = getDescriptionFieldByPath(sourceEntityCache, parent.getMetadata().getName());
					newPath = (EntityPath<?>) ReflectionUtils.invokeConstructor(parent.getQueryType(), alias);
					mappedByForPath.put(newPath, descriptionField.getMappedBy());
					boExpression = (BooleanExpression) ReflectionUtils.invokeMethod(expr, "eq", newPath);
				} else {
					newPath = (EntityPath<?>) ReflectionUtils.invokeConstructor(expr.getClass(), alias);
					boExpression = (BooleanExpression) ReflectionUtils.invokeMethod(expr, "eq", newPath);
				}

				QueryMetadata metadata = getMetadataFromPath(expr);
				if (metadata == null)
					throw new SQLAnalyserException("Não foi possível criar a junção dinâmica " + expr);

				metadata.addJoin(JoinType.LEFTJOIN, newPath);
				metadata.addJoinCondition(boExpression);
				createdAliasesForDynamicJoins.put(exprOwner, newPath);
			}
		}
	}

	private QueryMetadata getMetadataFromPath(Path<?> expr) {
		Path<?> root = expr.getRoot();
		for (QueryMetadata metadata : allMetadatas) {
			for (JoinExpression joinExpression : metadata.getJoins()) {
				if (joinExpression.getTarget() instanceof EntityPath<?>) {
					if (((EntityPath<?>) joinExpression.getTarget()).getMetadata().getName().equals(root.getMetadata().getName()))
						return metadata;
				}
			}
		}
		return null;
	}

	/**
	 * Lista de aliases gerados na análise das expressões.
	 * 
	 * @return
	 */
	public Map<Path<?>, EntityPath<?>> getAliasesForJoins() {
		return createdAliasesForDynamicJoins;
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

		EntityCache entityCache = null;
		if (path instanceof SetPath<?, ?>) {
			entityCache = this.configuration.getEntityCacheManager().getEntityCache(((SetPath<?, ?>) path).getElementType());
		} else {
			entityCache = this.configuration.getEntityCacheManager().getEntityCache(path.getType());
		}
		boolean isEntity = (entityCache != null);

		EntityPath<?> result = null;
		if (isEntity) { // PAP.pessoa
			if (inOperation) {
				result = createdAliasesForDynamicJoins.get(path.getMetadata().getParent());
				if (result == null)
					result = (EntityPath<?>) path.getMetadata().getParent(); // PAP
			} else {
				result = createdAliasesForDynamicJoins.get(path);
			}
		} else { // PAP.usuarioSistema.id
			if (path.getMetadata().getParent().getMetadata().isRoot())
				result = (EntityPath<?>) path.getMetadata().getParent();
			else {

				EntityCache entityCacheOwner = this.configuration.getEntityCacheManager().getEntityCache(path.getMetadata().getParent().getType());
				if (entityCacheOwner == null)
					throw new SQLSerializerException("A classe " + path.getMetadata().getParent().getType()
							+ " não foi encontrada na lista de entidades gerenciadas.");

				DescriptionField descriptionField = entityCacheOwner.getDescriptionField(path.getMetadata().getName() + "");
				if (descriptionField == null)
					throw new SQLSerializerException("O campo " + path.getMetadata().getName() + " não foi encontrado na classe "
							+ entityCacheOwner.getEntityClass() + ". ");

				if ((inOperation) && (descriptionField.isPrimaryKey() && !descriptionField.isCompositeId())) {
					result = createdAliasesForDynamicJoins.get(path.getMetadata().getParent().getMetadata().getParent());
					if (result == null) {
						result = (EntityPath<?>) path.getMetadata().getParent().getMetadata().getParent(); // PAP
					}
				} else {
					if (path.getMetadata().getPathType() == PathType.COLLECTION_ANY) {
						SetPath parent = (SetPath) path.getMetadata().getParent();
						result = createdAliasesForDynamicJoins.get(parent);
					} else if (path.getMetadata().getParent().getMetadata().getPathType() == PathType.COLLECTION_ANY) {
						SetPath parent = (SetPath) path.getMetadata().getParent().getMetadata().getParent();
						result = createdAliasesForDynamicJoins.get(parent);
					} else {
						result = createdAliasesForDynamicJoins.get(path.getMetadata().getParent());
					}
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
		int index = 1;
		for (Expression<?> expr : select) {
			if (expr instanceof FactoryExpression) {
				FactoryExpression<?> factory = ((FactoryExpression<?>) expr);
				Class<?> resultClass = factory.getType();
				Set<ResultClassColumnInfo> columns = new LinkedHashSet<ResultClassColumnInfo>();
				for (Expression<?> exprFact : factory.getArgs()) {
					Set<SQLAnalyserColumn> tempColumns = resultColumnsFromProjections.get(exprFact);
					if (tempColumns != null) {
						for (SQLAnalyserColumn column : tempColumns) {
							ResultClassColumnInfo newColumnInfo = new ResultClassColumnInfo(column.getAliasTableName(), column.getColumnName(),
									column.getAliasColumnName(), column.getDescriptionField(), index);
							columns.add(newColumnInfo);
							index++;
						}
					}
				}
				result.add(new ResultClassDefinition(resultClass, columns));
			} else {
				Set<ResultClassColumnInfo> columns = new LinkedHashSet<ResultClassColumnInfo>();
				Set<SQLAnalyserColumn> tempColumns = resultColumnsFromProjections.get(expr);
				if (columns != null) {
					for (SQLAnalyserColumn column : tempColumns) {
						ResultClassColumnInfo newColumnInfo = new ResultClassColumnInfo(column.getAliasTableName(), column.getColumnName(),
								column.getAliasColumnName(), column.getDescriptionField(), index);
						columns.add(newColumnInfo);
						index++;
					}
				}
				if (columns != null) {
					ResultClassDefinition classDefinition = new ResultClassDefinition(expr.getType(), columns);
					result.add(classDefinition);
				}
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

	protected void makeIndexHints(QueryMetadata metadata) {
		if ((metadata.getIndexHints() != null) && (metadata.getIndexHints().size() > 0)) {
			List<IndexHint> _indexes = new ArrayList<IndexHint>();
			for (IndexHint index : metadata.getIndexHints()) {
				if (index.hasAliasPath() && !index.getAliasPath().getMetadata().isRoot()) {
					if (createdAliasesForDynamicJoins.containsKey(index.getAliasPath())) {
						_indexes.add(new IndexHint(createdAliasesForDynamicJoins.get(index.getAliasPath()).getMetadata().getName(), index.getIndexName()));
					}
				} else
					_indexes.add(new IndexHint(index.getAlias(), index.getIndexName()));
			}
			if (_indexes.size() > 0) {
				String indexHint = configuration.getIndexHint(_indexes.toArray(new IndexHint[] {}));
				if (!StringUtils.isEmpty(indexHint)) {
					metadata.addFlag(new QueryFlag(configuration.getIndexHintPosition(), indexHint));
				}
			}
		}
	}

	public Expression<?> getUnion() {
		return union;
	}

	public void setUnion(Expression<?> union) {
		this.union = union;
	}

	public boolean hasUnion() {
		return union != null;
	}

	public Set<QueryMetadata> getAllMetadatas() {
		return Collections.unmodifiableSet(allMetadatas);
	}
}
