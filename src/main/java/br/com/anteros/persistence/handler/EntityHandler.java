package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.metadata.annotation.type.ScopeType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.proxy.LazyLoadProxyFactory;
import br.com.anteros.persistence.proxy.collection.SQLArrayList;
import br.com.anteros.persistence.proxy.collection.SQLHashSet;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.query.SQLQueryAnalyserAlias;

/**
 * Handler para criação de Objeto baseado em SELECT com Expressões.
 * 
 */
public class EntityHandler implements ResultSetHandler {
	protected Class<?> resultClass;
	protected transient EntityCacheManager entityCacheManager;
	protected SQLSession session;
	protected Cache transactionCache;
	protected EntityManaged entityManaged;
	protected Map<String, String> expressions;
	protected Map<SQLQueryAnalyserAlias, Map<String, String>> columnAliases;
	protected Object mainObject;
	protected LazyLoadProxyFactory proxyFactory;

	public EntityHandler(LazyLoadProxyFactory proxyFactory, Class<?> targetClass,
			EntityCacheManager entityCacheManager, Map<String, String> expressions,
			Map<SQLQueryAnalyserAlias, Map<String, String>> columnAliases, SQLSession session, Cache transactionCache) {
		this.resultClass = targetClass;
		this.session = session;
		this.expressions = expressions;
		this.entityCacheManager = entityCacheManager;
		this.transactionCache = transactionCache;
		this.proxyFactory = proxyFactory;
		this.columnAliases = columnAliases;
	}

	public EntityHandler(LazyLoadProxyFactory proxyFactory, Class<?> targetClazz,
			EntityCacheManager entityCacheManager, SQLSession session, Cache transactionCache) {
		this(proxyFactory, targetClazz, entityCacheManager, new LinkedHashMap<String, String>(),
				new LinkedHashMap<SQLQueryAnalyserAlias, Map<String, String>>(), session, transactionCache);
	}

	public Object handle(ResultSet resultSet) throws Exception {

		Set<String> columns = new HashSet<String>();
		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			if (columns.contains(resultSet.getMetaData().getColumnName(i).toUpperCase())) {
				throw new EntityHandlerException(
						"Não é possível instanciar objetos a partir de um ResultSet que contenha nomes de colunas duplicadas. Classe "
								+ resultClass.getName() + " coluna " + resultSet.getMetaData().getColumnName(i));
			}
			columns.add(resultSet.getMetaData().getColumnName(i).toUpperCase());
		}

		List<Object> result = null;
		/*
		 * Se o ResultSet não estiver vazio
		 */
		if (resultSet.next()) {
			/*
			 * Faz o loop para processar os registros do ResultSet
			 */
			do {
				/*
				 * Se a classe passada para o handler for uma entidade abstrata
				 * localiza no entityCache a classe correspondente ao
				 * discriminator colum
				 */

				Class<?> targetResultClass = resultClass;
				EntityCache entityCache = entityCacheManager.getEntityCache(resultClass);
				try {
					if (entityCache.hasDiscriminatorColumn()
							&& ReflectionUtils.isAbstractClass(entityCache.getEntityClass())) {
						String dsValue = resultSet.getString(getAliasColumnName(entityCache, entityCache
								.getDiscriminatorColumn().getColumnName()));
						entityCache = entityCacheManager.getEntityCache(resultClass, dsValue);
						targetResultClass = entityCache.getEntityClass();
					}
				} catch (Exception e) {
					throw new EntityHandlerException(
							"Para que seja criado o objeto da classe "
									+ entityCache.getEntityClass().getName()
									+ " será necessário adicionar no sql a coluna "
									+ entityCache.getDiscriminatorColumn().getColumnName()
									+ " que informe que tipo de classe será usada para instanciar o objeto. Verifique também se o discriminator value "
									+ resultSet.getString(getAliasColumnName(entityCache, entityCache
											.getDiscriminatorColumn().getColumnName()))
									+ " é o mesmo que está configurado na classe herdada.");
				}
				if (result == null)
					result = new ArrayList<Object>();
				/*
				 * Gera o objeto
				 */
				createObject(targetResultClass, resultSet, result);

			} while (resultSet.next());
		}

		return result;
	}

	/**
	 * Método responsável por criar o objeto.
	 * 
	 */
	protected Object createObject(Class<?> targetClass, ResultSet resultSet, List<Object> result) throws Exception {
		EntityCache entityCache = entityCacheManager.getEntityCache(targetClass);

		String uniqueId = getUniqueId(resultSet, entityCacheManager.getEntityCache(resultClass), null);

		mainObject = getObjectFromCache(entityCache, uniqueId, transactionCache);
		if (mainObject == null) {
			mainObject = targetClass.newInstance();
			result.add(mainObject);
		}
		entityManaged = session.getPersistenceContext().addEntityManaged(mainObject, false, false);

		/*
		 * Processa as expressões para gerar os fields do objeto
		 */
		for (String expression : expressions.keySet()) {
			String aliasPathWithColumn = expressions.get(expression);

			String columnName = "";
			String[] splitAlias = aliasPathWithColumn.split("\\.");
			if (splitAlias.length == 1) {
				columnName = splitAlias[splitAlias.length - 1];
			} else if (splitAlias.length > 1) {
				columnName = splitAlias[splitAlias.length - 1];
			}

			try {
				Object value = resultSet.getObject(columnName);
				if ((value instanceof Date) || (value instanceof java.sql.Date))
					value = resultSet.getTimestamp(columnName);

				processExpression(mainObject, targetClass, expression, value, resultSet, aliasPathWithColumn);
			} catch (SQLException ex) {
				throw new EntityHandlerException("Erro processando expressão " + expression + " na classe "
						+ targetClass.getName() + " coluna " + columnName + ". " + ex.getMessage());
			}

		}

		/*
		 * Adiciona o objeto no Cache da sessão ou da transação para evitar
		 * buscar o objeto novamente no mesmo processamento
		 */

		addObjectToCache(entityCache, mainObject, entityCache.getCacheUniqueId(mainObject));

		/*
		 * Preenche a árvore do objeto considerando as estratégias configuradas
		 * em cada field com ForeignKey e Fetch
		 */
		loadCollectionsAndRelationShip(mainObject, entityCache, resultSet);

		if (entityCache.isVersioned()) {
			entityManaged.setOriginalVersion(ObjectUtils.cloneObject(ReflectionUtils.getFieldValueByName(mainObject,
					entityCache.getVersionColumn().getField().getName())));
			entityManaged.setOldVersion(entityManaged.getOriginalVersion());
			entityManaged.setCurrentVersion(entityManaged.getOriginalVersion());
		}

		return mainObject;
	}

	protected EntityHandlerResult processExpression(Object targetObject, Class<?> targetClass, String expression,
			Object value, ResultSet resultSet, String aliasPath) throws Exception {

		/*
		 * Busca a EntityCache da classe
		 */
		EntityCache entityCache = entityCacheManager.getEntityCache(targetClass);

		Object newObject = null;
		/*
		 * Quebra a expressão em partes
		 */
		String[] tempExpression = expression.split("\\.");
		/*
		 * Quebra o caminho em partes do alias
		 */
		String[] tempAlias = aliasPath.split("\\.");

		/*
		 * Pega a primeira parte da expressão onde será iniciado o processamento
		 */
		String targetField = tempExpression[0];
		String alias = "";
		if (tempAlias.length > 1)
			alias = tempAlias[0];

		/*
		 * Pega o DescriptionColumn do field alvo
		 */
		DescriptionField descriptionField = entityCache.getDescriptionField(targetField);

		/*
		 * Se não encontrar o field
		 */
		if (descriptionField == null)
			return new EntityHandlerResult(targetObject);

		/*
		 * Se a expressão for apenas um objeto seta o valor no field
		 */
		if (tempExpression.length == 1) {
			/*
			 * if (descriptionField.getFieldClass() == resultClass) {
			 * descriptionField.setObjectValue(targetObject, mainObject); } else
			 */
			descriptionField.setObjectValue(targetObject, value);
			/*
			 * Guarda o valor na lista de valores anteriores
			 */
			FieldEntityValue fieldEntityValue = descriptionField.getSimpleColumn().getFieldEntityValue(targetObject);
			entityManaged.addOriginalValue(fieldEntityValue);
			entityManaged.addLastValue(fieldEntityValue);

			/*
			 * Adiciona o campo na lista de campos que poderão ser alterados. Se
			 * o campo não for buscado no select não poderá ser alterado.
			 */
			entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());

			/*
			 * Retorna o objeto com a expressão processada
			 */
			return new EntityHandlerResult(value != null, targetObject);
		} else {
			Object oldTargetObject = targetObject;
			if (descriptionField.isCollectionEntity()) {
				targetObject = descriptionField.getObjectValue(targetObject);
				if (targetObject == null) {
					if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class)) {
						targetObject = new SQLHashSet();
						descriptionField.setObjectValue(oldTargetObject, targetObject);
					} else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class)) {
						targetObject = new SQLArrayList();
						descriptionField.setObjectValue(oldTargetObject, targetObject);
					}
				}

				if (ReflectionUtils.isAbstractClass(descriptionField.getTargetClass())) {
					EntityCache tempEntityCache = entityCacheManager.getEntityCache(descriptionField.getTargetClass());
					try {
						String discriminator = resultSet.getString(getAliasColumnName(tempEntityCache, tempEntityCache
								.getDiscriminatorColumn().getColumnName()));
						EntityCache concreteEntityCache = entityCacheManager.getEntityCache(
								descriptionField.getTargetClass(), discriminator);

						String uniqueId = getUniqueId(resultSet, tempEntityCache, alias);

						if (uniqueId == null)
							return new EntityHandlerResult(targetObject);

						newObject = getObjectFromCache(concreteEntityCache, uniqueId, transactionCache);
						if (newObject == null) {
							newObject = concreteEntityCache.getEntityClass().newInstance();
							addObjectToCache(concreteEntityCache, newObject, uniqueId);
						}
					} catch (Exception e) {
						throw new EntityHandlerException("Para que seja criado o objeto da "
								+ descriptionField.getTargetClass().getName()
								+ " será necessário adicionar no sql a coluna "
								+ tempEntityCache.getDiscriminatorColumn().getColumnName()
								+ " que informe que tipo de classe será usada para instanciar o objeto.");
					}

				} else {
					EntityCache entityCacheTemp = entityCacheManager.getEntityCache(descriptionField.getTargetClass());
					if (entityCacheTemp != null) {
						String uniqueId = getUniqueId(resultSet, entityCacheTemp, alias);
						if (uniqueId == null)
							return new EntityHandlerResult(targetObject);

						newObject = getObjectFromCache(entityCacheTemp, uniqueId, transactionCache);
						if (newObject == null) {
							newObject = entityCacheTemp.getEntityClass().newInstance();
							addObjectToCache(entityCacheTemp, newObject, uniqueId);
						}
						session.getPersistenceContext().addEntityManaged(newObject, true, false);
					} else {
						newObject = descriptionField.getTargetClass().newInstance();
						/*
						 * Adiciona o campo na lista de campos que poderão ser
						 * alterados. Se o campo não for buscado no select não
						 * poderá ser alterado.
						 */
						entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
					}

				}

			} else {
				/*
				 * Se o objeto for nulo instância o objeto
				 */
				if (descriptionField.isNull(targetObject)) {
					if (ReflectionUtils.isAbstractClass(descriptionField.getField().getType())) {
						EntityCache tempEntityCache = entityCacheManager.getEntityCache(descriptionField.getField()
								.getType());
						try {
							String discriminator = resultSet.getString(getAliasColumnName(alias, tempEntityCache
									.getDiscriminatorColumn().getColumnName()));
							if (discriminator == null)
								return new EntityHandlerResult(targetObject);

							EntityCache concreteEntityCache = entityCacheManager.getEntityCache(descriptionField
									.getField().getType(), discriminator);
							String uniqueId = getUniqueId(resultSet, tempEntityCache, alias);
							if (uniqueId == null)
								return new EntityHandlerResult(targetObject);

							newObject = getObjectFromCache(concreteEntityCache, uniqueId, transactionCache);
							if (newObject == null) {
								newObject = concreteEntityCache.getEntityClass().newInstance();
								addObjectToCache(concreteEntityCache, newObject, uniqueId);
							}
						} catch (Exception e) {
							throw new EntityHandlerException("Para que seja criado o objeto da "
									+ descriptionField.getField().getType()
									+ " será necessário adicionar no sql a coluna "
									+ tempEntityCache.getDiscriminatorColumn().getColumnName()
									+ " que informe que tipo de classe será usada para instanciar o objeto.");
						}

					} else {
						EntityCache entityCacheTemp = entityCacheManager.getEntityCache(descriptionField.getField()
								.getType());
						if (entityCacheTemp != null) {
							String uniqueId = getUniqueId(resultSet, entityCacheTemp, alias);
							newObject = getObjectFromCache(entityCacheTemp, uniqueId, transactionCache);
							if (newObject == null) {
								newObject = entityCacheTemp.getEntityClass().newInstance();
								addObjectToCache(entityCacheTemp, newObject, uniqueId);
							}
							session.getPersistenceContext().addEntityManaged(newObject, true, false);
						} else {
							newObject = descriptionField.getField().getType().newInstance();
							/*
							 * Adiciona o campo na lista de campos que poderão
							 * ser alterados. Se o campo não for buscado no
							 * select não poderá ser alterado.
							 */
							entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
						}

					}
				} else {
					/*
					 * Caso já tenha sido criado pega o objeto do field
					 */
					newObject = descriptionField.getObjectValue(targetObject);
					/*
					 * Adiciona o campo na lista de campos que poderão ser
					 * alterados. Se o campo não for buscado no select não
					 * poderá ser alterado.
					 */
					entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
				}
			}

			/*
			 * Processa o restante da expressão usando recursividade
			 */
			EntityHandlerResult handlerResult = processExpression(newObject, newObject.getClass(),
					expression.substring(expression.indexOf(".") + 1, expression.length()), value, resultSet,
					aliasPath.substring(aliasPath.indexOf(".") + 1, aliasPath.length()));
			if (!handlerResult.modified)
				return new EntityHandlerResult(targetObject);

			if (targetObject instanceof Collection)
				((Collection) targetObject).add(newObject);

			value = handlerResult.value;
		}
		/*
		 * Seta o valor processado da expressão no field do objeto
		 */
		if (!(targetObject instanceof Collection))
			descriptionField.setObjectValue(targetObject, value);
		/*
		 * Retorna o objeto com as expressões processadas
		 */
		return new EntityHandlerResult(targetObject);
	}

	private String getAliasColumnName(EntityCache sourceEntityCache, String columnName) {
		for (SQLQueryAnalyserAlias queryAnalyserAlias : columnAliases.keySet()) {
			if (queryAnalyserAlias.getEntity().equals(sourceEntityCache)) {
				String alias = columnAliases.get(queryAnalyserAlias).get(columnName);
				String[] splitAlias = alias.split("\\.");
				if (splitAlias.length > 0)
					alias = splitAlias[splitAlias.length - 1];
				return (alias == null ? columnName : alias);
			}
		}
		return columnName;
	}

	private String getAliasColumnName(String sourceAlias, String columnName) {
		for (SQLQueryAnalyserAlias queryAnalyserAlias : columnAliases.keySet()) {
			if (queryAnalyserAlias.getAlias().equals(sourceAlias)) {
				String alias = columnAliases.get(queryAnalyserAlias).get(columnName);
				String[] splitAlias = alias.split("\\.");
				if (splitAlias.length > 1)
					alias = splitAlias[1];
				return (alias == null ? columnName : alias);
			}
		}
		return columnName;
	}

	protected void addObjectToCache(EntityCache entityCache, Object targetObject, String uniqueId) {
		/*
		 * Adiciona o objeto no Cache da sessão ou da transação para evitar
		 * buscar o objeto novamente no mesmo processamento
		 */
		if ((entityCache.getCacheScope().equals(ScopeType.TRANSACTION)) && (transactionCache != null)) {
			transactionCache.put(entityCache.getEntityClass().getName() + "_" + uniqueId, targetObject,
					entityCache.getMaxTimeCache());
		} else {
			session.getPersistenceContext().addObjectToCache(entityCache.getEntityClass().getName() + "_" + uniqueId,
					targetObject, entityCache.getMaxTimeCache());
		}
	}

	protected String getUniqueId(ResultSet resultSet, EntityCache entityCache, String alias) throws SQLException {
		List<DescriptionColumn> primaryKeyColumns = entityCache.getPrimaryKeyColumns();
		Map<String, Object> primaryKey = new TreeMap<String, Object>();
		int index;

		for (DescriptionColumn column : primaryKeyColumns) {
			if (alias != null)
				index = resultSet.findColumn(getAliasColumnName(alias, column.getColumnName()));
			else
				index = resultSet.findColumn(getAliasColumnName(entityCache, column.getColumnName()));
			/*
			 * Se a coluna não foi adicionada no sql é porque o objeto é parcial
			 * e provalmente o usuário não quer todos os valores. Sendo assim
			 * cria um novo objeto pois no cache não vai ter o que ele precisa.
			 */
			if (index < 0) {
				throw new SQLException("NÃO ACHOU COLUNA " + column.getColumnName());
			}
			primaryKey.put(column.getColumnName(), resultSet.getObject(index));
		}
		StringBuffer uniqueId = new StringBuffer("");
		for (String key : primaryKey.keySet()) {
			if (!"".equals(uniqueId.toString()))
				uniqueId.append("_");
			uniqueId.append(primaryKey.get(key));
		}
		String result = uniqueId.toString();
		if (result.equals("null"))
			return null;
		return uniqueId.toString();
	}

	protected Object getObjectFromCache(EntityCache targetEntityCache, String uniqueId, Cache transactionCache) {
		Object result = null;

		/*
		 * Se a classe for abstrata pega todas as implementações não abstratas e
		 * verifica se existe um objeto da classe + ID no entityCache
		 */
		if (ReflectionUtils.isAbstractClass(targetEntityCache.getEntityClass())) {
			EntityCache[] entitiesCache = session.getEntityCacheManager().getEntitiesBySuperClass(targetEntityCache);
			for (EntityCache entityCache : entitiesCache) {
				result = transactionCache.get(entityCache.getEntityClass().getName() + "_" + uniqueId);
				if (result != null)
					break;
				result = session.getPersistenceContext().getObjectFromCache(
						entityCache.getEntityClass().getName() + "_" + uniqueId);
				if (result != null)
					break;
			}
		} else {
			/*
			 * Caso não seja abstrata localiza classe+ID no entityCache
			 */
			result = transactionCache.get(targetEntityCache.getEntityClass().getName() + "_" + uniqueId);

			if (result == null)
				result = session.getPersistenceContext().getObjectFromCache(
						targetEntityCache.getEntityClass().getName() + "_" + uniqueId);
		}
		return result;
	}

	protected void loadCollectionsAndRelationShip(Object targetObject, EntityCache entityCache, ResultSet restultSet)
			throws Exception {
		/*
		 * Faz um loop nos fields que tenham sido configuradas com ForeignKey e
		 * 
		 * Fetch
		 */
		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			/*
			 * Gera o valor do field somente se o usuário não incluiu a
			 * expressão manualmente no sql. Se ele adicionou a expressão será
			 * criado o objeto e alimentado apenas com os dados da expressão no
			 * método processExpression
			 */
			if (descriptionField.isCollection() || descriptionField.isJoinTable() || descriptionField.isRelationShip()) {
				if (descriptionField.isNull(targetObject)
						&& !existsExpressionForProcessing(entityCache, descriptionField.getField().getName())) {
					/*
					 * Busca a EntityCache da classe destino do field
					 */
					EntityCache targetEntityCache = null;
					if (!descriptionField.isElementCollection() && !descriptionField.isJoinTable()) {
						targetEntityCache = entityCacheManager.getEntityCache(descriptionField.getTargetEntity()
								.getEntityClass());

						if (targetEntityCache == null)
							throw new EntityHandlerException("Para que seja criado o objeto da classe "
									+ descriptionField.getTargetEntity().getEntityClass().getName()
									+ " é preciso adicionar a Entity relacionada à classe na configuração da sessão. "
									+ (descriptionField.getDescriptionColumns() == null ? "" : "Coluna(s) "
											+ descriptionField));
					}

					Map<String, Object> columnKeyValue = new TreeMap<String, Object>();
					/*
					 * Se o DescriptionField for um FK guarda o valor da coluna
					 */
					try {
						if (descriptionField.hasDescriptionColumn()) {
							for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
								if (descriptionColumn.isForeignKey() && !descriptionColumn.isInversedJoinColumn())
									columnKeyValue.put(descriptionColumn.getReferencedColumnName(),
											restultSet.getObject(descriptionColumn.getColumnName()));
							}
						} else
							/*
							 * Se for um field anotado com @Fetch guarda a PK do
							 * pai
							 */
							columnKeyValue = session.getIdentifier(targetObject).getColumns();
					} catch (Exception ex) {
						/*
						 * Se não for um DescriptionField do tipo
						 * COLLECTION_TABLE, continua iteracao do proximo field.
						 */
						if (!descriptionField.isElementCollection() && !descriptionField.isJoinTable())
							continue;
					}

					/*
					 * Se a estratégia for EAGER busca os dados do field
					 */
					if (descriptionField.getFetchType() == FetchType.EAGER) {
						Object result = session.loadData(targetEntityCache, targetObject, descriptionField,
								columnKeyValue, transactionCache);
						descriptionField.getField().set(targetObject, result);
						FieldEntityValue fieldEntityValue = descriptionField.getFieldEntityValue(session, targetObject);
						entityManaged.addOriginalValue(fieldEntityValue);
						entityManaged.addLastValue(fieldEntityValue);
						entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
					} else {
						Object newObject = proxyFactory.createProxy(session, targetObject, descriptionField,
								targetEntityCache, columnKeyValue, transactionCache);
						descriptionField.getField().set(targetObject, newObject);
					}
				}
			}
		}
	}

	protected boolean existsExpressionForProcessing(EntityCache entityCache, String fieldName) {
		for (String expression : expressions.keySet()) {
			String[] tempExpression = expression.split("\\.");
			if (tempExpression.length > 0) {
				if (fieldName.equals(tempExpression[0]))
					return true;
			}
		}
		return false;
	}

	protected class EntityHandlerResult {
		public boolean modified = false;
		public Object value;

		public EntityHandlerResult() {
		}

		public EntityHandlerResult(Object value) {
			this.value = value;
		}

		public EntityHandlerResult(boolean modified, Object value) {
			this.value = value;
			this.modified = modified;
		}
	}

}