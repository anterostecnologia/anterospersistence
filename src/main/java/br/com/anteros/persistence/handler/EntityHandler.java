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
package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.proxy.LazyLoadFactory;
import br.com.anteros.persistence.proxy.collection.DefaultSQLList;
import br.com.anteros.persistence.proxy.collection.DefaultSQLSet;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.lock.LockScope;
import br.com.anteros.persistence.session.query.ExpressionFieldMapper;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyserAlias;
import br.com.anteros.persistence.util.AnterosPersistenceHelper;

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
	protected Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases;
	protected Object mainObject;
	protected Object objectToRefresh;
	protected LazyLoadFactory proxyFactory;
	protected boolean allowDuplicateObjects = false;
	protected int firstResult, maxResults;
	protected boolean readOnly = false;
	protected Map<Object, String> aliasesCache = new HashMap<Object, String>();
	private Map<String, Integer> cacheAliasIndex = new HashMap<String, Integer>();
	private boolean isIncompleteKey;
	private List<ExpressionFieldMapper> expressionsFieldMapper;
	private LockOptions lockOptions;

	public EntityHandler(LazyLoadFactory proxyFactory, Class<?> targetClass, EntityCacheManager entityCacheManager,
			List<ExpressionFieldMapper> expressionsFieldMapper, Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases, SQLSession session,
			Cache transactionCache, boolean allowDuplicateObjects, int firstResult, int maxResults, boolean readOnly, LockOptions lockOptions) {
		this.resultClass = targetClass;
		this.session = session;
		this.entityCacheManager = entityCacheManager;
		this.transactionCache = transactionCache;
		this.proxyFactory = proxyFactory;
		this.columnAliases = columnAliases;
		this.allowDuplicateObjects = allowDuplicateObjects;
		this.firstResult = firstResult;
		this.readOnly = readOnly;
		this.expressionsFieldMapper = expressionsFieldMapper;
		this.lockOptions = lockOptions;
	}

	public EntityHandler(LazyLoadFactory proxyFactory, Class<?> targetClazz, EntityCacheManager entityCacheManager, SQLSession session,
			Cache transactionCache, boolean allowDuplicateObjects, int firstResult, int maxResults) {
		this(proxyFactory, targetClazz, entityCacheManager, new ArrayList<ExpressionFieldMapper>(),
				new LinkedHashMap<SQLQueryAnalyserAlias, Map<String, String[]>>(), session, transactionCache, allowDuplicateObjects, firstResult,
				maxResults, false, LockOptions.NONE);
	}

	/**
	 * Processa o ResultSet e cria os objetos
	 */
	public Object handle(ResultSet resultSet) throws Exception {
		List<Object> result = null;
		try {
			Set<String> columns = new HashSet<String>();
			/*
			 * Verifica a existência de colunas duplicadas
			 */
			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				String columnName = resultSet.getMetaData().getColumnLabel(i);
				if (columns.contains(columnName)) {
					throw new EntityHandlerException(
							"Não é possível instanciar objetos a partir de um ResultSet que contenha nomes de colunas duplicadas. Classe "
									+ resultClass.getName() + " coluna " + resultSet.getMetaData().getColumnName(i));
				}
				columns.add(resultSet.getMetaData().getColumnLabel(i).toUpperCase());
			}

			/*
			 * Se o ResultSet não estiver vazio
			 */
			int numberOfRow = 0;
			int numberOfRecords = 0;
			if (resultSet.next()) {
				/*
				 * Faz o loop para processar os registros do ResultSet
				 */
				do {
					if (numberOfRow >= firstResult) {
						if ((maxResults == 0) || (numberOfRecords < maxResults)) {
							/*
							 * Se a classe passada para o handler for uma entidade abstrata localiza no entityCache a
							 * classe correspondente ao discriminator colum
							 */

							Class<?> targetResultClass = resultClass;
							EntityCache entityCache = entityCacheManager.getEntityCache(resultClass);
							try {
								/*
								 * Verifica se a classe é abstrat e busca o tipo da classe concreta
								 */
								if (entityCache.hasDiscriminatorColumn() && ReflectionUtils.isAbstractClass(entityCache.getEntityClass())) {
									String dsValue = resultSet.getString(getAliasColumnName(entityCache, entityCache.getDiscriminatorColumn()
											.getColumnName()));
									entityCache = entityCacheManager.getEntityCache(resultClass, dsValue);
									targetResultClass = entityCache.getEntityClass();
								} else if (entityCache.hasDiscriminatorColumn()) {
									String dsValue = resultSet.getString(getAliasColumnName(entityCache, entityCache.getDiscriminatorColumn()
											.getColumnName()));
									if (!entityCache.getDiscriminatorValue().equals(dsValue)) {
										continue;
									}
								}
							} catch (Exception e) {
								throw new EntityHandlerException(
										"Para que seja criado o objeto da classe "
												+ entityCache.getEntityClass().getName()
												+ " será necessário adicionar no sql a coluna "
												+ entityCache.getDiscriminatorColumn().getColumnName()
												+ " que informe que tipo de classe será usada para instanciar o objeto. Verifique também se o discriminator value "
												+ resultSet.getString(getAliasColumnName(entityCache, entityCache.getDiscriminatorColumn()
														.getColumnName())) + " é o mesmo que está configurado na classe herdada.");
							}
							if (result == null)
								result = new ArrayList<Object>();
							/*
							 * Cria o objeto
							 */
							createObject(targetResultClass, resultSet, result);
							numberOfRecords++;
						}
					}
					numberOfRow++;

				} while (resultSet.next());
			}
		} catch (SQLException ex) {
			throw new EntityHandlerException("Erro processando handler para criação da classe " + resultClass.getName() + ". " + ex.getMessage());
		}

		return result;
	}

	/**
	 * Método responsável por criar o objeto.
	 * 
	 * @param targetClass
	 *            Classe para criação do objeto
	 * @param resultSet
	 *            Resultado do SQL.
	 * @param result
	 *            Objeto criado
	 * @return
	 * @throws Exception
	 */
	protected Object createObject(Class<?> targetClass, ResultSet resultSet, List<Object> result) throws Exception {
		EntityCache entityCache = entityCacheManager.getEntityCache(targetClass);

		/*
		 * Busca chave do objeto no resultSet
		 */
		String uniqueId = getUniqueId(resultSet, entityCacheManager.getEntityCache(resultClass), null);
		/*
		 * Se foi atribuido um objeto para ser atualizado usa o objeto e não cria um novo
		 */
		if (objectToRefresh != null) {
			mainObject = objectToRefresh;
			result.add(mainObject);
			objectToRefresh = null;
		} else {
			/*
			 * Verifica se o objeto já foi criado e está no cache
			 */
			mainObject = getObjectFromCache(entityCache, uniqueId, transactionCache);
			if (mainObject == null) {
				/*
				 * Se não encontrou no cache cria uma nova instância
				 */
				mainObject = targetClass.newInstance();
				result.add(mainObject);
			} else {
				/*
				 * Se o objeto já foi criado e foi configurado para criar objetos duplicados adiciona na lista.
				 */
				if (allowDuplicateObjects)
					result.add(mainObject);
			}
		}

		/*
		 * Adiciona o objeto como sendo uma entidade gerenciada no contexto
		 */
		entityManaged = session.getPersistenceContext().addEntityManaged(mainObject, readOnly, false);
		entityManaged.setLockMode(lockOptions.getLockMode());

		/*
		 * Processa as expressões para gerar os fields do objeto
		 */
		for (ExpressionFieldMapper expression : expressionsFieldMapper) {
			expression.execute(session, resultSet, entityManaged, mainObject, transactionCache);
		}
		/*
		 * Adiciona o objeto no Cache da sessão ou da transação para evitar buscar o objeto novamente no mesmo
		 * processamento
		 */

		addObjectToCache(entityCache, mainObject, entityCache.getCacheUniqueId(mainObject));

		/*
		 * Preenche a árvore do objeto considerando as estratégias configuradas em cada field com ForeignKey e Fetch
		 */
		loadCollectionsRelationShipAndLob(mainObject, entityCache, resultSet);

		if (entityCache.isVersioned()) {
			entityManaged.setOriginalVersion(ObjectUtils.cloneObject(ReflectionUtils.getFieldValueByName(mainObject, entityCache.getVersionColumn()
					.getField().getName())));
			entityManaged.setOldVersion(entityManaged.getOriginalVersion());
			entityManaged.setCurrentVersion(entityManaged.getOriginalVersion());
		}

		return mainObject;
	}

	/**
	 * Retorna o alias da coluna de uma entidade no resultSet.
	 * 
	 * @param sourceEntityCache
	 *            Entidade
	 * @param columnName
	 *            Nome da coluna
	 * @return Alias da coluna
	 */
	private String getAliasColumnName(EntityCache sourceEntityCache, String columnName) {
		String result = aliasesCache.get(sourceEntityCache.getEntityClass().getName() + ":" + columnName);
		if (result != null)
			return result;
		for (SQLQueryAnalyserAlias queryAnalyserAlias : columnAliases.keySet()) {
			if (queryAnalyserAlias.getEntity().equals(sourceEntityCache)
					|| (ReflectionUtils.isExtendsClass(queryAnalyserAlias.getEntity().getEntityClass(), sourceEntityCache.getEntityClass()))) {
				String[] alias = null;
				for (String column : columnAliases.get(queryAnalyserAlias).keySet()) {
					if (column.equalsIgnoreCase(columnName)) {
						alias = columnAliases.get(queryAnalyserAlias).get(column);
						break;

					}
				}
				result = (alias == null || alias.length == 0 ? columnName : alias[alias.length - 1]);
				aliasesCache.put(sourceEntityCache.getEntityClass().getName() + ":" + columnName, result);
				return result;
			}
		}
		return columnName;
	}

	/**
	 * Retorna o alias da coluna de uma entidade no resultSet.
	 * 
	 * @param sourceAlias
	 *            Nome do alias da tabela
	 * @param columnName
	 *            Nome da coluna
	 * @return Alias da coluna
	 */
	private String getAliasColumnName(String sourceAlias, String columnName) {
		String result = aliasesCache.get(sourceAlias + ":" + columnName);
		if (result != null)
			return result;
		for (SQLQueryAnalyserAlias queryAnalyserAlias : columnAliases.keySet()) {
			if (queryAnalyserAlias.getAlias().equals(sourceAlias)) {
				String[] alias = null;
				for (String column : columnAliases.get(queryAnalyserAlias).keySet()) {
					if (column.equalsIgnoreCase(columnName)) {
						alias = columnAliases.get(queryAnalyserAlias).get(column);
						break;
					}
				}
				result = (alias == null || alias.length <= 1 ? columnName : alias[1]);
				aliasesCache.put(sourceAlias + ":" + columnName, result);
				return result;
			}
		}
		return columnName;
	}

	/**
	 * Adiciona um objeto no cache da transação SQL ou da persistência
	 * 
	 * @param entityCache
	 *            Entidade
	 * @param targetObject
	 *            Objeto
	 * @param uniqueId
	 *            Chave do objeto
	 */
	private void addObjectToCache(EntityCache entityCache, Object targetObject, String uniqueId) {
		/*
		 * Adiciona o objeto no cache da sessão ou da transação para evitar buscar o objeto novamente no mesmo
		 * processamento
		 */
		if ((entityCache.getCacheScope().equals(ScopeType.TRANSACTION)) && (transactionCache != null)) {
			transactionCache.put(entityCache.getEntityClass().getName() + "_" + uniqueId, targetObject, entityCache.getMaxTimeCache());
		} else {
			session.getPersistenceContext().addObjectToCache(entityCache.getEntityClass().getName() + "_" + uniqueId, targetObject,
					entityCache.getMaxTimeCache());
		}
	}

	/**
	 * Retorna a chave única do objeto buscando os valores no resultSet.
	 * 
	 * @param resultSet
	 *            Resultado do SQL.
	 * @param entityCache
	 *            Entidade
	 * @param alias
	 *            Alias da tabela
	 * @return Chave única da entidade
	 * @throws SQLException
	 */
	private String getUniqueId(ResultSet resultSet, EntityCache entityCache, String alias) throws SQLException {
		Integer index;
		String aliasColumnName = null;

		StringBuilder uniqueIdTemp = new StringBuilder("");
		boolean appendSeparator = false;
		for (DescriptionColumn column : entityCache.getPrimaryKeyColumns()) {
			/*
			 * Busca o alias do nome da coluna
			 */
			if (alias != null) {
				aliasColumnName = getAliasColumnName(alias, column.getColumnName());
			} else {
				aliasColumnName = getAliasColumnName(entityCache, column.getColumnName());
			}

			/*
			 * Busca índice da coluna dentro do resultSet
			 */
			index = cacheAliasIndex.get(aliasColumnName);
			if (index == null) {
				index = resultSet.findColumn(aliasColumnName);
				cacheAliasIndex.put(aliasColumnName, index);
			}
			if (index < 0) {
				/*
				 * Esta exception não deverá ocorrer nunca pois as colunas estão sendo parseadas pela análise do SQL. Se
				 * isto ocorrer pode ser um erro na análise.
				 */
				throw new SQLException("NÃO ACHOU COLUNA " + column.getColumnName());
			}
			/*
			 * Concatena o valor da coluna na chave do objeto
			 */
			if (appendSeparator)
				uniqueIdTemp.append("_");
			uniqueIdTemp.append(resultSet.getObject(index));
			appendSeparator = true;
		}
		/*
		 * Retorna o chave única. Se for uma string "null" retorna como nula
		 */
		return (uniqueIdTemp.toString().equals("null") ? null : uniqueIdTemp.toString());
	}

	/**
	 * Busca um objeto no cache usando a Entidade e a chave para localização.
	 * 
	 * @param session
	 *            Sessão
	 * @param targetEntityCache
	 *            Entidade
	 * @param uniqueId
	 *            Chave primária do objeto
	 * @param transactionCache
	 *            Cache da transação
	 * @return Objeto correspondente a entidade e chave informada ou nulo caso não exista no cache.
	 */
	private Object getObjectFromCache(EntityCache targetEntityCache, String uniqueId, Cache transactionCache) {
		Object result = null;

		/*
		 * Se a classe for abstrata pega todas as implementações não abstratas e verifica se existe um objeto da classe
		 * + ID no Cache
		 */
		if (ReflectionUtils.isAbstractClass(targetEntityCache.getEntityClass())) {
			EntityCache[] entitiesCache = session.getEntityCacheManager().getEntitiesBySuperClass(targetEntityCache);
			for (EntityCache entityCache : entitiesCache) {
				result = transactionCache.get(entityCache.getEntityClass().getName() + "_" + uniqueId);
				if (result != null)
					break;
				result = session.getPersistenceContext().getObjectFromCache(entityCache.getEntityClass().getName() + "_" + uniqueId);
				if (result != null)
					break;
			}
		} else {
			/*
			 * Caso não seja abstrata localiza classe+ID no Cache
			 */
			result = transactionCache.get(targetEntityCache.getEntityClass().getName() + "_" + uniqueId);

			if (result == null)
				result = session.getPersistenceContext().getObjectFromCache(targetEntityCache.getEntityClass().getName() + "_" + uniqueId);
		}
		return result;
	}

	/**
	 * Carrega as coleções, relacionamentos e Lob que não foram carregados no SQL. Adota o padrão definido no mapeamento
	 * para criar objetos ou criar proxies para os campos.
	 * 
	 * @param targetObject
	 *            Objeto alvo
	 * @param entityCache
	 *            Entidade
	 * @param resultSet
	 *            Resultado do SQL.
	 * @throws Exception
	 */
	protected void loadCollectionsRelationShipAndLob(Object targetObject, EntityCache entityCache, ResultSet resultSet) throws Exception {
		/*
		 * Faz um loop apenas nos fields que tenham sido configuradas com ForeignKey, Lob e Fetch. Os demais campos
		 * simples já foram processados.
		 */
		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			/*
			 * Gera o valor do field somente se o usuário não incluiu a expressão manualmente no sql. Se ele adicionou a
			 * expressão será criado o objeto e alimentado apenas com os dados da expressão no método processExpression
			 */
			if (descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable() || descriptionField.isRelationShip() || descriptionField.isLob()) {
				Object assignedValue = descriptionField.getObjectValue(targetObject);

				/*
				 * Verifica se há necessidade de processar o field. Se o field possuir uma expressão ou já tiver um
				 * valor não é necessário processá-lo. Caso o valor seja uma chave e estiver incompleta, irá buscar o
				 * objeto novamente.
				 */
				if (checkNeedsProcessDescriptionField(entityCache, descriptionField, assignedValue)) {
					EntityCache targetEntityCache = getTargetEntityCacheByDescriptionField(entityCache, descriptionField);

					/*
					 * Busca os valores da chave do objeto
					 */
					Map<String, Object> columnKeyValue = getColumnKeyValue(targetObject, entityCache, resultSet, descriptionField);

					/*
					 * Se não foi possível obter a chave do field no resultSet é porque o usuário não quer que este
					 * field seja carregado.
					 */
					if (columnKeyValue != null) {

						/*
						 * Se a estratégia for EAGER busca os dados do field, se for LAZY cria um proxy.
						 */
						FetchType fetchType = descriptionField.getFetchType();
						/*
						 * Se estiver no android e não for uma coleção então assume EAGER pois no Android não é possível
						 * usar ferramentas de manipulação de bytecode e assim não é possível implementar lazy em alguns
						 * casos.
						 */
						if (AnterosPersistenceHelper.androidIsPresent() && !(descriptionField.isAnyCollectionOrMap()))
							fetchType = FetchType.EAGER;

						/* Se for um campo LOB assume o que estiver configurado. */
						if (descriptionField.isLob())
							fetchType = descriptionField.getFetchType();

						/*
						 * Somente busca relacionamentos pois LOB já é criado junto com os demais campos. Somente cria o
						 * LOB se for LAZY. Ai cria como um proxy.
						 */
						if (fetchType == FetchType.EAGER) {
							if (!descriptionField.isLob()) {
								Object result = null;
								/*
								 * Se a chave estiver incompleta, carrega o objeto completo novamente.
								 */
								if (isIncompleteKey) {
									StringBuilder sb = new StringBuilder("");
									sb.append(targetEntityCache.getEntityClass().getName());
									for (String key : columnKeyValue.keySet()) {
										if (!"".equals(sb.toString())) {
											sb.append("_");
										}
										sb.append(columnKeyValue.get(key));
									}
									/*
									 * Remove o objeto do cache
									 */
									String uniqueId = sb.toString();
									transactionCache.remove(uniqueId);
									/*
									 * Cria a query e busca novamente o objeto completo
									 */
									SQLQuery query = session.createQuery("");
									/*
									 * Extende o lock para as coleções e tabelas de junção
									 */
									if ((descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable())
											&& (lockOptions.getLockScope() == LockScope.EXTENDED)) {
										LockOptions lockOpts = LockOptions.copy(lockOptions, new LockOptions());
										lockOpts.clearAliasesToLock();
										query.setLockOptions(lockOpts);
									} else
										query.setLockOptions(LockOptions.NONE);

									result = query.loadData(targetEntityCache, targetObject, descriptionField, columnKeyValue, transactionCache);

									EntityCache fieldentEntityCache = session.getEntityCacheManager()
											.getEntityCache(descriptionField.getFieldClass());
									fieldentEntityCache.setPrimaryKeyValue(result, assignedValue);
									result = assignedValue;
								} else {
									/*
									 * Busca o objeto que será atribuido ao campo do objeto alvo
									 */
									SQLQuery query = session.createQuery("");

									/*
									 * Extende o lock para as coleções e tabelas de junção
									 */
									if ((descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable())
											&& (lockOptions.getLockScope() == LockScope.EXTENDED)) {
										LockOptions lockOpts = LockOptions.copy(lockOptions, new LockOptions());
										lockOpts.clearAliasesToLock();
										query.setLockOptions(lockOptions);
									} else
										query.setLockOptions(LockOptions.NONE);

									query.setReadOnly(readOnly);
									result = query.loadData(targetEntityCache, targetObject, descriptionField, columnKeyValue, transactionCache);
								}

								descriptionField.setObjectValue(targetObject, result);
								/*
								 * Se a consulta não for somente leitura armazena os valores dos campos.
								 */
								if (entityManaged.getStatus() != EntityStatus.READ_ONLY) {
									FieldEntityValue fieldEntityValue = descriptionField.getFieldEntityValue(session, targetObject);
									entityManaged.addOriginalValue(fieldEntityValue);
									entityManaged.addLastValue(fieldEntityValue);
									entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
								}
							}
						} else {
							/*
							 * Se for LAZY cria apenas o proxy.
							 */
							LockOptions lockOpts = LockOptions.copy(lockOptions, new LockOptions());
							lockOpts.clearAliasesToLock();
							Object newObject = proxyFactory.createProxy(session, targetObject, descriptionField, targetEntityCache, columnKeyValue,
									transactionCache, (lockOptions.getLockScope() == LockScope.EXTENDED ? lockOpts : LockOptions.NONE));
							descriptionField.getField().set(targetObject, newObject);

							if (entityManaged.getStatus() != EntityStatus.READ_ONLY) {
								FieldEntityValue value = descriptionField.getFieldEntityValue(session, targetObject);
								entityManaged.addOriginalValue(value);
								entityManaged.addLastValue(value);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Verifica se um determinado campo em uma entidade tem necessidade de ser processado para criar o objeto. No caso de
	 * chaves estrangeiras que não foram trazidas no SQL e objetos com chave parcial devem ser considerados como
	 * necessário criar o objeto do campo novamente.
	 * 
	 * @param entityCache
	 *            Entidade
	 * @param descriptionField
	 *            Campo
	 * @param assignedValue
	 *            Valor atribuído ao campo atual
	 * @return Verdadeiro se há necessidade de criar o objeto e atribuir ao campo
	 * @throws Exception
	 */
	private boolean checkNeedsProcessDescriptionField(EntityCache entityCache, DescriptionField descriptionField, Object assignedValue)
			throws Exception {
		Boolean process = (assignedValue == null);
		Boolean existsExpression = existsExpressionForProcessing(entityCache, descriptionField.getField().getName());
		isIncompleteKey = false;

		if (assignedValue != null) {
			/*
			 * Processa se for uma collection que não seja herança de DefaultSQLList our DefaultSQLSet
			 */
			if (descriptionField.isAnyCollectionOrMap() && ((assignedValue instanceof DefaultSQLList) || (assignedValue instanceof DefaultSQLSet)))
				process = false;
			else if (descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable()) {
				process = true;
			} else if (descriptionField.isRelationShip()) {
				/*
				 * Processa se a chave estiver incompleta
				 */
				EntityCache fieldentEntityCache = session.getEntityCacheManager().getEntityCache(descriptionField.getFieldClass());
				if (fieldentEntityCache != null) {
					/*
					 * Verifica se a chave do objeto está incompleta. Isto ocorre em casos com chave composta.
					 */
					isIncompleteKey = fieldentEntityCache.isIncompletePrimaryKeyValue(assignedValue);
					if (!isIncompleteKey) {
						process = false;
					} else {
						process = true;
						existsExpression = false;
					}
				}

			}
		}

		return process && !existsExpression;
	}

	/**
	 * Retorna a entidade destino de um campo em uma determinada entidade
	 * 
	 * @param entityCache
	 *            Entidade de origem
	 * @param descriptionField
	 *            Campo da entidade origem
	 * @return Entidade destino
	 * @throws EntityHandlerException
	 */
	private EntityCache getTargetEntityCacheByDescriptionField(EntityCache entityCache, DescriptionField descriptionField)
			throws EntityHandlerException {
		EntityCache targetEntityCache = null;
		if (descriptionField.isLob()) {
			targetEntityCache = entityCache;
		} else {
			/*
			 * Busca a EntityCache da classe destino do field
			 */
			if (!descriptionField.isElementCollection() && !descriptionField.isJoinTable()) {
				targetEntityCache = entityCacheManager.getEntityCache(descriptionField.getTargetEntity().getEntityClass());

				/*
				 * Não encontrou a classe no dicionário gera uma exceção avisando.
				 */
				if (targetEntityCache == null)
					throw new EntityHandlerException("Para que seja criado o objeto da classe "
							+ descriptionField.getTargetEntity().getEntityClass().getName()
							+ " é preciso adicionar a Entity relacionada à classe na configuração da sessão. "
							+ (descriptionField.getDescriptionColumns() == null ? "" : "Coluna(s) " + descriptionField));
			}
		}
		return targetEntityCache;
	}

	/**
	 * Retorna os valores da chave de um determinado campo da entidade no resultSet.
	 * 
	 * @param targetObject
	 *            Objeto alvo
	 * @param entityCache
	 *            Entidade
	 * @param resultSet
	 *            Resultado do SQL
	 * @param descriptionField
	 *            Campo da entidade
	 * @return Mapa com os valores da chave
	 * @throws EntityHandlerException
	 */
	private Map<String, Object> getColumnKeyValue(Object targetObject, EntityCache entityCache, ResultSet resultSet, DescriptionField descriptionField)
			throws EntityHandlerException {

		/*
		 * Se o DescriptionField for um FK guarda o valor da coluna. Apenas monta o objeto da chave estrangeira caso o
		 * usuário tenha incluido as colunas da chave no SQL.
		 */
		String columnName = "";
		try {
			Map<String, Object> columnKeyValue = new TreeMap<String, Object>();
			/*
			 * Se o campo possuí uma ou mais colunas e não é um Lob.
			 */
			if (descriptionField.hasDescriptionColumn() && !(descriptionField.isLob())) {
				for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
					/*
					 * Se o campo é uma chave estrangeira e não é uma inversedJoinColumn
					 */
					if (descriptionColumn.isForeignKey() && !descriptionColumn.isInversedJoinColumn()) {
						columnName = descriptionColumn.getColumnName();
						String aliasColumnName = getAliasColumnName(entityCache, descriptionColumn.getColumnName());
						int index = resultSet.findColumn(aliasColumnName);
						if (index < 0) {
							return null;
						}
						/*
						 * Adiciona o valor no mapa para retornar
						 */
						columnKeyValue.put(descriptionColumn.getReferencedColumnName(), resultSet.getObject(aliasColumnName));
					}
				}
			} else
				/*
				 * Se for um campo anotado com @Fetch guarda a PK do pai
				 */
				columnKeyValue = session.getIdentifier(targetObject).getColumns();
			return columnKeyValue;
		} catch (Exception ex) {
			/*
			 * Se não for um DescriptionField do tipo COLLECTION_TABLE, continua iteracao do proximo field.
			 */
			if (!descriptionField.isElementCollection() && !descriptionField.isJoinTable() && isIncompleteKey) {
				throw new EntityHandlerException("Para que seja criado o objeto do tipo "
						+ descriptionField.getTargetEntity().getEntityClass().getSimpleName() + " que será atribuído ao campo "
						+ descriptionField.getField().getName() + " na classe " + entityCache.getEntityClass() + " é preciso adicionar a coluna "
						+ columnName + " da tabela " + entityCache.getTableName() + " no sql. Erro " + ex.getMessage());
			}
		}
		return null;
	}

	/**
	 * Retorna se existe uma expressão para ser processada para um determinado campo na entidade.
	 * 
	 * @param entityCache
	 *            Entidade
	 * @param fieldName
	 *            Nome do campo
	 * @return Verdadeiro se encontrou a expressão associada ao campo da entidade
	 */
	private boolean existsExpressionForProcessing(EntityCache entityCache, String fieldName) {
		String result = aliasesCache.get(entityCache.getEntityClass().getName() + ":" + fieldName);
		if (result != null)
			return true;

		for (ExpressionFieldMapper expression : expressionsFieldMapper) {
			if (fieldName.equals(expression.getDescriptionField().getField().getName().equalsIgnoreCase(fieldName))) {
				/*
				 * Armazena no cache para acelerar a próxima busca
				 */
				aliasesCache.put(entityCache.getEntityClass().getName() + ":" + fieldName, "S");
				return true;
			}
		}
		return false;
	}

	/**
	 * Atribui um objeto para ser atualizado. Se for atribuido um objeto não será criado uma nova instência para a
	 * classe de resultado.
	 * 
	 * @param objectToRefresh
	 *            Objeto a ser atualizado
	 * @throws EntityHandlerException
	 */
	public void setObjectToRefresh(Object objectToRefresh) throws EntityHandlerException {
		if (objectToRefresh == null)
			return;

		if (!(objectToRefresh.getClass().equals(resultClass))) {
			throw new EntityHandlerException("Classe do objeto para refresh " + objectToRefresh.getClass() + " difere da classe de resultado "
					+ resultClass);
		}
		this.objectToRefresh = objectToRefresh;
	}
}