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
package br.com.anteros.persistence.session.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.EntityHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.EntityListener;
import br.com.anteros.persistence.metadata.annotation.EventType;
import br.com.anteros.persistence.metadata.annotation.PostPersist;
import br.com.anteros.persistence.metadata.annotation.PostRemove;
import br.com.anteros.persistence.metadata.annotation.PostUpdate;
import br.com.anteros.persistence.metadata.annotation.PostValidate;
import br.com.anteros.persistence.metadata.annotation.PrePersist;
import br.com.anteros.persistence.metadata.annotation.PreRemove;
import br.com.anteros.persistence.metadata.annotation.PreUpdate;
import br.com.anteros.persistence.metadata.annotation.PreValidate;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.metadata.identifier.IdentifierGenerator;
import br.com.anteros.persistence.metadata.identifier.IdentifierGeneratorFactory;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.proxy.JavassistLazyLoadFactory;
import br.com.anteros.persistence.proxy.LazyLoadFactory;
import br.com.anteros.persistence.session.FindParameters;
import br.com.anteros.persistence.session.SQLPersister;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockManager;
import br.com.anteros.persistence.session.lock.LockManagerJDBC;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.session.query.ExpressionFieldMapper;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyserAlias;
import br.com.anteros.persistence.session.query.ShowSQLType;
import br.com.anteros.persistence.session.query.TypedSQLQuery;
import br.com.anteros.persistence.sql.command.BatchCommandSQL;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.command.PersisterCommand;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.transaction.Transaction;
import br.com.anteros.persistence.transaction.TransactionFactory;

@SuppressWarnings("unchecked")
public class SQLSessionImpl implements SQLSession {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(SQLSession.class);

	public static int FIRST_RECORD = 0;
	private EntityCacheManager entityCacheManager;
	private Connection connection;
	private DatabaseDialect dialect;
	private AbstractSQLRunner queryRunner;
	private ShowSQLType[] showSql = { ShowSQLType.NONE };
	private boolean formatSql;
	private int queryTimeout = 0;
	private SQLPersistenceContext persistenceContext;
	private ConcurrentLinkedQueue<PersisterCommand> commandQueue = new ConcurrentLinkedQueue<PersisterCommand>();
	private SQLSessionFactory sessionFactory;
	private List<SQLSessionListener> listeners = new ArrayList<SQLSessionListener>();
	private Map<Object, Map<DescriptionColumn, IdentifierPostInsert>> cacheIdentifier = new LinkedHashMap<Object, Map<DescriptionColumn, IdentifierPostInsert>>();
	private SQLPersister persister;
	private LazyLoadFactory lazyLoadFactory = new JavassistLazyLoadFactory();
	private TransactionFactory transactionFactory;
	private Transaction transaction;
	private LockManager lockManager;
	private int batchSize = 0;
	private int currentBatchSize = 0;
	private Map<String, NextValControl> cacheSequenceNumbers = new HashMap<String, SQLSessionImpl.NextValControl>();
	private ConcurrentLinkedQueue<Event> eventList = new ConcurrentLinkedQueue<>();

	private String clientId;
	private Object tenantId;
	private Object companyId;

	private boolean validationActive;
	private boolean notifyListenersEnabled = true;

	private CloudFileManager externalFileManager;

	private boolean flushing = false;

	private boolean enableImageCompression;

	public SQLSessionImpl(SQLSessionFactory sessionFactory, Connection connection,
			EntityCacheManager entityCacheManager, AbstractSQLRunner queryRunner, DatabaseDialect dialect,
			ShowSQLType[] showSql, boolean formatSql, int queryTimeout, int lockTimeout,
			TransactionFactory transactionFactory, int batchSize, boolean validationActive,
			CloudFileManager fileManager, boolean enableImageCompression) throws Exception {
		this.entityCacheManager = entityCacheManager;
		this.connection = connection;
		if (connection != null)
			this.connection.setAutoCommit(false);
		this.dialect = dialect;
		this.showSql = showSql;
		this.formatSql = formatSql;
		this.sessionFactory = sessionFactory;
		this.persistenceContext = new SQLPersistenceContextImpl(this, entityCacheManager);
		this.persister = new SQLPersisterImpl();
		this.queryRunner = queryRunner;
		this.queryTimeout = queryTimeout;
		this.transactionFactory = transactionFactory;
		this.lockManager = new LockManagerJDBC();
		this.batchSize = batchSize;
		this.validationActive = validationActive;
		this.externalFileManager = fileManager;
		this.enableImageCompression = enableImageCompression;

		String lockTimeoutSql = dialect.getSetLockTimeoutString(lockTimeout);
		if (!StringUtils.isEmpty(lockTimeoutSql)) {
			connection.prepareStatement(lockTimeoutSql).execute();
		}
	}

	public long update(String sql) throws Exception {
		errorIfClosed();
		return queryRunner.update(this, sql, listeners);
	}

	public long update(String sql, Object[] params) throws Exception {
		errorIfClosed();
		return queryRunner.update(this, sql, params, listeners);
	}

	public long update(String sql, NamedParameter[] params) throws Exception {
		errorIfClosed();
		return queryRunner.update(this, sql, params, listeners);
	}

	public Object save(Object object) throws Exception {
		errorIfClosed();
		return persister.save(this, object);
	}

	@Override
	public Object save(Object object, Class<?>... groups) throws Exception {
		errorIfClosed();
		return persister.save(this, object, groups);
	}

	@Override
	public void saveInBatchMode(Object object, int batchSize) throws Exception {
		errorIfClosed();
		this.currentBatchSize = batchSize;
		persister.save(this, object, batchSize);
	}

	@Override
	public void saveInBatchMode(Object[] object, int batchSize) throws Exception {
		errorIfClosed();
		this.currentBatchSize = batchSize;
		for (Object obj : object)
			persister.save(this, obj, batchSize);
	}

	public void save(Object[] object) throws Exception {
		errorIfClosed();
		for (Object obj : object)
			persister.save(this, obj);
	}

	public void save(Collection<?> object) throws Exception {
		errorIfClosed();
		for (Object obj : object)
			persister.save(this, obj);
	}

	public void remove(Object object) throws Exception {
		errorIfClosed();
		persister.remove(this, object);
	}

	public void remove(Object[] object) throws Exception {
		errorIfClosed();
		persister.remove(this, object);
	}

	public DatabaseDialect getDialect() {
		return dialect;
	}

	public Connection getConnection() {
		return connection;
	}

	public AbstractSQLRunner getRunner() throws Exception {
		return queryRunner;
	}

	public void setDialect(DatabaseDialect dialect) {
		this.dialect = dialect;
	}

	public EntityCacheManager getEntityCacheManager() {
		return entityCacheManager;
	}

	public void setEntityCacheManager(EntityCacheManager entityCacheManager) {
		this.entityCacheManager = entityCacheManager;
	}

	public boolean isShowSql() {
		return showSql != null;
	}

	public ShowSQLType[] getShowSql() {
		return showSql;
	}

	public void setShowSql(ShowSQLType... showSql) {
		this.showSql = showSql;
	}

	public boolean isFormatSql() {
		return formatSql;
	}

	public void setFormatSql(boolean formatSql) {
		this.formatSql = formatSql;
	}

	public SQLPersistenceContext getPersistenceContext() {
		return persistenceContext;
	}

	public void flush() throws Exception {
		try {
			flushing = true;
			errorIfClosed();
			while (!commandQueue.isEmpty()) {
				if (getCurrentBatchSize() > 0) {
					if (commandQueue.size() > 0)
						new BatchCommandSQL(this, commandQueue.toArray(new CommandSQL[] {}), getCurrentBatchSize(),
								this.getShowSql()).execute();
					commandQueue.clear();
				} else {
					PersisterCommand command = commandQueue.poll();
					try {
						command.execute();
					} catch (SQLException ex) {
						if (command instanceof CommandSQL)
							throw this.getDialect().convertSQLException(ex, "Erro enviando comando sql.",
									((CommandSQL) command).getSql());
						else {
							throw ex;
						}
					}
				}

			}	
			if (!this.eventList.isEmpty()) {				
				ConcurrentLinkedQueue<Event> eventListToProcess = new ConcurrentLinkedQueue<>();
				eventListToProcess.addAll(eventList);
				eventList.clear();			
				eventListToProcess.stream().forEach(event -> this.internalNotifyListeners(event.getEventType(), event.getOldObject(), event.getNewObject()));
				eventListToProcess.clear();			
			}
			
		} finally {
			flushing = false;
		}
	}

	public void forceFlush(Set<String> tableNames) throws Exception {
		errorIfClosed();
		if (tableNames != null) {
			boolean foundCommand = false;
			for (PersisterCommand command : commandQueue) {
				if (command instanceof CommandSQL) {
					if (tableNames.contains(((CommandSQL) command).getTargetTableName().toUpperCase())) {
						foundCommand = true;
						break;
					}
				}
			}
			if (foundCommand) {
				flush();
			}
		}
	}

	public void close() throws Exception {
		this.eventList.clear();
		persistenceContext.evictAll();
		persistenceContext.clearCache();
		persistenceContext = null;
		for (SQLSessionListener listener : listeners)
			listener.onClose(this);
		commandQueue.clear();
		currentBatchSize = 0;
		if (connection != null && !connection.isClosed()) {
			connection.rollback();
			connection.close();
		}

		connection = null;
		LOG.debug("Fechou session " + this);
	}

	public void onBeforeExecuteCommit(Connection connection) throws Exception {
		flush();
	}

	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		if (this.getConnection() == connection) {
			commandQueue.clear();
			this.eventList.clear();
		}
	}

	public void onAfterExecuteCommit(Connection connection) throws Exception {
		currentBatchSize = 0;
	}

	public void onAfterExecuteRollback(Connection connection) throws Exception {
		currentBatchSize = 0;
	}

	public AbstractSQLRunner getQueryRunner() {
		errorIfClosed();
		return queryRunner;
	}

	public void setQueryRunner(AbstractSQLRunner queryRunner) {
		errorIfClosed();
		this.queryRunner = queryRunner;
	}

	public <T> Identifier<T> getIdentifier(T owner) throws Exception {
		errorIfClosed();
		return Identifier.create(this, owner);
	}

	public <T> Identifier<T> createIdentifier(Class<T> clazz) throws Exception {
		errorIfClosed();
		return Identifier.create(this, clazz);
	}

	public void addListener(SQLSessionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeListener(SQLSessionListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	public List<SQLSessionListener> getListeners() {
		return listeners;
	}

	public ConcurrentLinkedQueue<PersisterCommand> getCommandQueue() {
		return commandQueue;
	}

	public Map<Object, Map<DescriptionColumn, IdentifierPostInsert>> getCacheIdentifier() {
		return cacheIdentifier;
	}

	public String clientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void save(Class<?> clazz, String[] columns, String[] values) throws Exception {
		errorIfClosed();
		throw new Exception("Método não suportado.");
	}

	public void removeAll(Class<?> clazz) throws Exception {
		errorIfClosed();
		throw new Exception("Método não suportado.");
	}

	public void removeTable(String tableName) throws Exception {
		errorIfClosed();
		throw new Exception("Método não suportado.");
	}

	public void enablelockOptions() throws Exception {
		errorIfClosed();
		throw new Exception("Método não implementado.");
	}

	public void disablelockOptions() throws Exception {
		errorIfClosed();
		throw new Exception("Método não implementado.");
	}

	public void executeDDL(String ddl) throws Exception {
		errorIfClosed();
		getRunner().executeDDL(this, ddl, showSql, formatSql, "");
	}

	public EntityHandler createNewEntityHandler(Class<?> resultClass, Set<ExpressionFieldMapper> expressionsFieldMapper,
			Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases, Cache transactionCache,
			boolean allowDuplicateObjects, Object objectToRefresh, int firstResult, int maxResults, boolean readOnly,
			LockOptions lockOptions, String fieldsToForceLazy) throws Exception {
		errorIfClosed();
		EntityHandler handler = new EntityHandler(lazyLoadFactory, resultClass, getEntityCacheManager(),
				expressionsFieldMapper, columnAliases, this, transactionCache, allowDuplicateObjects, firstResult,
				maxResults, readOnly, lockOptions, fieldsToForceLazy);
		handler.setObjectToRefresh(objectToRefresh);
		return handler;
	}

	public void lock(Object entity, LockOptions lockOptions) throws Exception {
		errorIfClosed();
		lockManager.lock(this, entity, lockOptions);
	}

	public void lockAll(Collection<?> entities, LockOptions lockOptions) throws Exception {
		if (entities != null) {
			lockAll(entities.toArray(new Object[] {}), lockOptions);
		}
	}

	public void lockAll(Object[] entities, LockOptions lockOptions) throws Exception {
		errorIfClosed();
		for (Object entity : entities) {
			lock(entity, lockOptions);
		}
	}

	public boolean isProxyObject(Object object) throws Exception {
		return lazyLoadFactory.isProxyObject(object);
	}

	public boolean proxyIsInitialized(Object object) throws Exception {
		return lazyLoadFactory.proxyIsInitialized(object);
	}

	public void savePoint(String savepoint) throws Exception {
		errorIfClosed();
		throw new Exception("Método não implementado.");
	}

	public void rollbackToSavePoint(String savepoint) throws Exception {
		errorIfClosed();
		throw new Exception("Método não implementado.");
	}

	public <T> T cloneEntityManaged(Object object) throws Exception {
		return null;
	}

	public void evict(Class object) {
		errorIfClosed();
		persistenceContext.evict(object);
	}

	public void evictAll() {
		errorIfClosed();
		persistenceContext.evictAll();
	}

	public boolean isClosed() throws Exception {
		return getConnection() == null || getConnection().isClosed();
	}

	public void setClientInfo(String clientInfo) throws SQLException {
		errorIfClosed();
		getDialect().setConnectionClientInfo(getConnection(), clientInfo);
	}

	public String getClientInfo() throws SQLException {
		errorIfClosed();
		return getDialect().getConnectionClientInfo(getConnection());
	}

	@Override
	public Transaction getTransaction() throws Exception {
		if (transaction == null) {
			transaction = transactionFactory.createTransaction(getConnection(), getPersistenceContext());
		}
		return transaction;
	}

	@Override
	public SQLSessionFactory getSQLSessionFactory() {
		return sessionFactory;
	}

	@Override
	public void clear() throws Exception {
		internalClear();
	}

	private void internalClear() {
		persistenceContext.evictAll();
		persistenceContext.clearCache();
	}

	protected void errorIfClosed() {
		try {
			if (isClosed()) {
				throw new SQLSessionException("Sessão está fechada!");
			}
		} catch (Exception ex) {
			throw new SQLSessionException("Sessão está fechada!", ex);
		}
	}

	@Override
	public <T> T find(FindParameters<T> params) throws Exception {
		if (params.getEntityClass() != null) {
			EntityCache entityCache = entityCacheManager.getEntityCache(params.getEntityClass());
			if (entityCache == null) {
				throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. "
						+ params.getEntityClass().getName());
			}
			if (!entityCache.isVersioned()) {
				params.lockOptions(LockOptions.NONE);
			}
		}
		if (params.getEntityClass() != null && params.getId() != null && params.getLockOptions() != null
				&& params.getProperties() != null) {
			return (T) this.find(params.getEntityClass(), params.getId(), params.getLockOptions(),
					params.getProperties(), params.isReadOnly(), params.getFieldsToForceLazy(),
					params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		} else if (params.getEntityClass() != null && params.getId() != null && params.getLockOptions() != null) {
			return (T) this.find(params.getEntityClass(), params.getId(), params.getLockOptions(), params.isReadOnly(),
					params.getFieldsToForceLazy(), params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		} else if (params.getEntityClass() != null && params.getId() != null && params.getProperties() != null) {
			return (T) this.find(params.getEntityClass(), params.getId(), params.getProperties(), params.isReadOnly(),
					params.getFieldsToForceLazy(), params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		} else if (params.getEntityClass() != null && params.getId() != null) {
			return (T) this.find(params.getEntityClass(), params.getId(), params.isReadOnly(),
					params.getFieldsToForceLazy(), params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		} else if (params.getIdentifier() != null && params.getProperties() != null
				&& params.getLockOptions() != null) {
			return (T) this.find(params.getIdentifier(), params.getProperties(), params.getLockOptions(),
					params.isReadOnly(), params.getFieldsToForceLazy(), params.isIgnoreTenantId(),
					params.isIgnoreCompanyId());
		} else if (params.getIdentifier() != null && params.getProperties() != null) {
			return (T) this.find(params.getIdentifier(), params.getProperties(), params.isReadOnly(),
					params.getFieldsToForceLazy(), params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		} else if (params.getIdentifier() != null && params.getLockOptions() != null) {
			return (T) this.find(params.getIdentifier(), params.getLockOptions(), params.isReadOnly(),
					params.getFieldsToForceLazy(), params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		} else if (params.getIdentifier() != null) {
			return (T) this.find(params.getIdentifier(), params.isReadOnly(), params.getFieldsToForceLazy(),
					params.isIgnoreTenantId(), params.isIgnoreCompanyId());
		}
		return null;

	}

	public <T> T find(Class<T> entityClass, Object id, boolean readOnly, String fieldsToForceLazy,
			boolean ignoreTenantId, boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		if (id instanceof Identifier) {
			if (!((Identifier<?>) id).getClazz().equals(entityClass)) {
				throw new SQLSessionException("Objeto ID é do tipo Identifier porém de uma classe diferente da classe "
						+ entityClass.getName());
			} else
				return find((Identifier<T>) id, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
		}
		Identifier<T> identifier = Identifier.create(this, entityClass);
		identifier.setIdIfPossible(id);
		return find(identifier, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
	}

	public <T> T find(Class<T> entityClass, Object id, Map<String, Object> properties, boolean readOnly,
			String fieldsToForceLazy, boolean ignoreTenantId, boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		T result = find(entityClass, id, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
		entityCache.setObjectValues(result, properties);
		return result;
	}

	public <T> T find(Class<T> entityClass, Object id, LockOptions lockOptions, boolean readOnly,
			String fieldsToForceLazy, boolean ignoreTenantId, boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		if (id instanceof Identifier) {
			if (!((Identifier<?>) id).getClazz().equals(entityClass)) {
				throw new SQLSessionException("Objeto ID é do tipo Identifier porém de uma classe diferente da classe "
						+ entityClass.getName());
			} else
				return find((Identifier<T>) id, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
		}
		Identifier<T> identifier = Identifier.create(this, entityClass);
		identifier.setIdIfPossible(id);
		return find(identifier, lockOptions, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
	}

	public <T> T find(Class<T> entityClass, Object id, LockOptions lockOptions, Map<String, Object> properties,
			boolean readOnly, String fieldsToForceLazy, boolean ignoreTenantId, boolean ignoreCompanyId)
			throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		T result = find(entityClass, id, lockOptions, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
		entityCache.setObjectValues(result, properties);
		return result;
	}

	public <T> T find(Identifier<T> id, boolean readOnly, String fieldsToForceLazy, boolean ignoreTenantId,
			boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		SQLQuery query = createQuery("");
		query.setReadOnly(readOnly);
		query.setFieldsToForceLazy(fieldsToForceLazy);
		query.ignoreCompanyId(ignoreCompanyId);
		query.ignoreTenantId(ignoreTenantId);
		List<?> result = query.identifier(id).getResultList();
		if ((result == null) || (result.size() == 0)) {
			return null;
		}
		return (T) result.get(0);
	}

	public <T> T find(Identifier<T> id, LockOptions lockOptions, boolean readOnly, String fieldsToForceLazy,
			boolean ignoreTenantId, boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		SQLQuery query = createQuery("");
		query.setReadOnly(readOnly);
		query.setFieldsToForceLazy(fieldsToForceLazy);
		query.ignoreCompanyId(ignoreCompanyId);
		query.ignoreTenantId(ignoreTenantId);
		query.setLockOptions(lockOptions);
		List<?> result = query.identifier(id).getResultList();
		if ((result == null) || (result.size() == 0)) {
			return null;
		}
		return (T) result.get(0);
	}

	public <T> T find(Identifier<T> id, Map<String, Object> properties, boolean readOnly, String fieldsToForceLazy,
			boolean ignoreTenantId, boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		T result = find(id, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
		id.getEntityCache().setObjectValues(result, properties);
		return result;
	}

	public <T> T find(Identifier<T> id, Map<String, Object> properties, LockOptions lockOptions, boolean readOnly,
			String fieldsToForceLazy, boolean ignoreTenantId, boolean ignoreCompanyId) throws Exception {
		errorIfClosed();
		T result = find(id, lockOptions, readOnly, fieldsToForceLazy, ignoreTenantId, ignoreCompanyId);
		id.getEntityCache().setObjectValues(result, properties);
		return result;
	}

	@Override
	public void refresh(Object entity) throws Exception {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
		EntityCache entityCache = entityCacheManager.getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier, false, null, false, false);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) throws Exception {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
		EntityCache entityCache = entityCacheManager.getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier, false, null, false, false);
		identifier.getEntityCache().setObjectValues(entity, properties);
	}

	@Override
	public void refresh(Object entity, LockOptions lockOptions) throws Exception {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
		EntityCache entityCache = entityCacheManager.getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier, lockOptions, false, null, false, false);
	}

	@Override
	public void refresh(Object entity, LockOptions lockOptions, Map<String, Object> properties) throws Exception {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
		EntityCache entityCache = entityCacheManager.getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException(
					"Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier, lockOptions, false, null, false, false);
		identifier.getEntityCache().setObjectValues(entity, properties);
	}

	@Override
	public void detach(Object entity) {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
	}

	@Override
	public SQLQuery createQuery(String sql) throws Exception {
		return createQuery(sql, new LockOptions().setLockMode(LockMode.NONE));
	}

	@Override
	public SQLQuery createQuery(String sql, Object parameters) throws Exception {
		return createQuery(sql, parameters, new LockOptions().setLockMode(LockMode.NONE));
	}

	@Override
	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass) throws Exception {
		return createQuery(sql, resultClass, new LockOptions().setLockMode(LockMode.NONE));
	}

	@Override
	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, Object parameters) throws Exception {
		return createQuery(sql, resultClass, parameters, new LockOptions().setLockMode(LockMode.NONE));
	}

	@Override
	public SQLQuery createQuery(String sql, LockOptions lockOptions) throws Exception {
		errorIfClosed();
		SQLQuery result = new SQLQueryImpl(this);
		result.timeOut(queryTimeout);
		result.sql(sql);
		result.showSql(showSql);
		result.formatSql(formatSql);
		result.setLockOptions(lockOptions);
		return result;
	}

	@Override
	public SQLQuery createQuery(String sql, Object parameters, LockOptions lockOptions) throws Exception {
		errorIfClosed();
		SQLQuery result = new SQLQueryImpl(this);
		result.sql(sql);
		result.setParameters(parameters);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		result.setLockOptions(lockOptions);
		return result;
	}

	@Override
	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, LockOptions lockOptions)
			throws Exception {
		errorIfClosed();
		TypedSQLQuery<T> result = new SQLQueryImpl<T>(this, resultClass);
		result.sql(sql);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		result.setLockOptions(lockOptions);
		return result;
	}

	@Override
	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, Object parameters,
			LockOptions lockOptions) throws Exception {
		errorIfClosed();
		TypedSQLQuery<T> result = new SQLQueryImpl<T>(this, resultClass);
		result.timeOut(queryTimeout);
		result.sql(sql);
		result.showSql(showSql);
		result.formatSql(formatSql);
		result.setParameters(parameters);
		result.setLockOptions(lockOptions);
		return result;
	}

	@Override
	public SQLQuery createNamedQuery(String name) throws Exception {
		errorIfClosed();
		SQLQuery result = new SQLQueryImpl(this);
		result.namedQuery(name);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public SQLQuery createNamedQuery(String name, Object parameters) throws Exception {
		errorIfClosed();
		SQLQuery result = new SQLQueryImpl(this);
		result.namedQuery(name);
		result.setParameters(parameters);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public <T> TypedSQLQuery<T> createNamedQuery(String name, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return new SQLQueryImpl<T>(this, resultClass).timeOut(queryTimeout).namedQuery(name).showSql(showSql)
				.formatSql(formatSql);
	}

	@Override
	public <T> TypedSQLQuery<T> createNamedQuery(String name, Class<T> resultClass, Object parameters)
			throws Exception {
		errorIfClosed();
		return new SQLQueryImpl<T>(this, resultClass).namedQuery(name).setParameters(parameters).showSql(showSql)
				.formatSql(formatSql);
	}

	@Override
	public SQLQuery createStoredProcedureQuery(String procedureName, CallableType type) throws Exception {
		errorIfClosed();
		SQLQuery result = new StoredProcedureSQLQueryImpl(this, type);
		result.procedureOrFunctionName(procedureName);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public SQLQuery createStoredProcedureQuery(String procedureName, CallableType type, Object parameters)
			throws Exception {
		errorIfClosed();
		SQLQuery result = new StoredProcedureSQLQueryImpl(this, type);
		result.procedureOrFunctionName(procedureName);
		result.setParameters(parameters);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public <T> TypedSQLQuery<T> createStoredProcedureQuery(String procedureName, CallableType type,
			Class<T> resultClass) throws Exception {
		errorIfClosed();
		TypedSQLQuery<T> result = new StoredProcedureSQLQueryImpl(this, resultClass, type);
		result.procedureOrFunctionName(procedureName);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public <T> TypedSQLQuery<T> createStoredProcedureQuery(String procedureName, CallableType type,
			Class<T> resultClass, Object[] parameters) throws Exception {
		errorIfClosed();
		TypedSQLQuery<T> result = new StoredProcedureSQLQueryImpl(this, resultClass, type);
		result.procedureOrFunctionName(procedureName);
		result.timeOut(queryTimeout);
		result.setParameters(parameters);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public SQLQuery createStoredProcedureNamedQuery(String name) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLQuery createStoredProcedureNamedQuery(String name, Object parameters) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> TypedSQLQuery<T> createStoredProcedureNamedQuery(String name, Class<T> resultClass) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> TypedSQLQuery<T> createStoredProcedureNamedQuery(String name, Class<T> resultClass, Object[] parameters)
			throws Exception {
		throw new UnsupportedOperationException();
	}

//	@Override
//	public <T> T find(Class<T> entityClass, Object primaryKey) throws Exception {
//		return find(entityClass, primaryKey, false);
//	}

//	@Override
//	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) throws Exception {
//		return find(entityClass, primaryKey, properties, false);
//	}

//	@Override
//	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions) throws Exception {
//		return find(entityClass, primaryKey, lockOptions, false);
//	}

//	@Override
//	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions, Map<String, Object> properties)
//			throws Exception {
//		return find(entityClass, primaryKey, lockOptions, properties, false);
//	}

//	@Override
//	public <T> T find(Identifier<T> id) throws Exception {
//		return find(id, false);
//	}

//	@Override
//	public <T> T find(Identifier<T> id, LockOptions lockOptions) throws Exception {
//		return find(id, lockOptions, false);
//	}

//	@Override
//	public <T> T find(Identifier<T> id, Map<String, Object> properties) throws Exception {
//		return find(id, properties, false);
//	}

//	@Override
//	public <T> T find(Identifier<T> id, Map<String, Object> properties, LockOptions lockOptions) throws Exception {
//		return find(id, properties, lockOptions, false);
//	}

	@Override
	public String applyLock(String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception {
		return lockManager.applyLock(this, sql, resultClass, lockOptions);
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}

	@Override
	public void batchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	private int getCurrentBatchSize() {
		if (batchSize > 0)
			return batchSize;
		return currentBatchSize;
	}

	@Override
	public boolean validationIsActive() {
		return validationActive;
	}

	@Override
	public void activateValidation() {
		this.validationActive = true;
	}

	@Override
	public void deactivateValidation() {
		this.validationActive = false;
	}

	@Override
	public boolean hasNextValFromCacheSequence(String sequenceName) {
		if (!cacheSequenceNumbers.containsKey(sequenceName))
			return false;
		return (cacheSequenceNumbers.get(sequenceName).hasNextVal());
	}

	@Override
	public void storeNextValToCacheSession(String sequenceName, Long firstValue, Long lastValue) {
		cacheSequenceNumbers.put(sequenceName, new NextValControl(firstValue - 1, lastValue));
	}

	@Override
	public Long getNextValFromCacheSequence(String sequenceName) {
		if (!cacheSequenceNumbers.containsKey(sequenceName))
			return null;
		return (cacheSequenceNumbers.get(sequenceName).getNextVal());
	}

	private class NextValControl {

		private Long lastValue;
		private Long value;

		public NextValControl(Long firstValue, Long lastValue) {
			this.lastValue = lastValue;
			this.value = firstValue;
		}

		public boolean hasNextVal() {
			return (value + 1 <= lastValue);
		}

		public Long getNextVal() {
			value++;
			return value;

		}
	}

	@Override
	public void forceGenerationIdentifier(Object entity) throws Exception {
		if (entity == null)
			return;
		EntityCache entityCache = this.getEntityCacheManager().getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException("Objeto não pode ser salvo pois a classe " + entity.getClass().getName()
					+ " não foi localizada no cache de Entidades.");
		}

		if (!this.getIdentifier(entity).hasIdentifier()) {
			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				if (!descriptionField.isAnyCollectionOrMap() && !descriptionField.isVersioned()
						&& !descriptionField.isJoinTable()) {
					for (DescriptionColumn columnModified : descriptionField.getDescriptionColumns()) {
						if (columnModified.isPrimaryKey() && columnModified.hasGenerator()) {
							IdentifierGenerator identifierGenerator = IdentifierGeneratorFactory.createGenerator(this,
									columnModified);
							if (!(identifierGenerator instanceof IdentifierPostInsert)) {
								/*
								 * Gera o próximo número da sequência e seta na entidade
								 */
								ReflectionUtils.setObjectValueByFieldName(entity, columnModified.getField().getName(),
										identifierGenerator.generate());
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (!this.isClosed())
			this.close();
		super.finalize();
	}

	@Override
	public int[] batch(String sql, Object[][] params) throws Exception {
		return queryRunner.batch(this, sql, params, showSql, formatSql, listeners, "");
	}

	@Override
	public void validate(Object object) throws Exception {
		persister.getValidator().validateBean(object);
	}

	@Override
	public void validate(Object object, Class<?>... groups) throws Exception {
		persister.getValidator().validateBean(object, groups);
	}

	@Override
	public void invalidateConnection() throws SQLException {
		if (this.connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
		}

		this.connection = this.getSQLSessionFactory().getDataSource().getConnection();

	}

	@Override
	public void setTenantId(Object value) {
		this.tenantId = value;
	}

	@Override
	public Object getTenantId() {
		if (this.tenantId instanceof String && StringUtils.isEmpty(this.tenantId.toString())) {
			return null;
		}
		return this.tenantId;
	}

	@Override
	public void setCompanyId(Object value) {
		this.companyId = value;
	}

	@Override
	public Object getCompanyId() {
		return companyId;
	}

	public CloudFileManager getExternalFileManager() {
		return externalFileManager;
	}

	@Override
	public void notifyListeners(EventType eventType, Object oldObject, Object newObject) throws Exception {
		if (newObject == null)
			return;
		if (notifyListenersEnabled) {			
			if (eventType.equals(EventType.PostPersist) || eventType.equals(EventType.PostRemove) || eventType.equals(EventType.PostUpdate)) {
				eventList.add(new Event(eventType,oldObject,newObject));
			} else {			
				internalNotifyListeners(eventType, oldObject, newObject);
			}
		}
	}

	protected void internalNotifyListeners(EventType eventType, Object oldObject, Object newObject) {
		EntityCache entityCache = entityCacheManager.getEntityCache(newObject.getClass());
		if (entityCache.getEntityListeners().size() > 0) {
			for (EntityListener listener : entityCache.getEntityListeners()) {
				if (listener.getEventType().equals(eventType)) {
					if (listener.getMethod().getParameterCount() == 1) {
						try {
							ReflectionUtils.invokeMethod(listener.getTargetObject(), listener.getMethod().getName(), newObject);
						} catch (Exception ex) {
							ReflectionUtils.handleReflectionException(ex);
						}
					} else if (listener.getMethod().getParameterCount() == 2) {
						try {
							ReflectionUtils.invokeMethod(listener.getTargetObject(), listener.getMethod().getName(), new Object[] {oldObject, newObject});
						} catch (Exception ex) {
							ReflectionUtils.handleReflectionException(ex);
						}
					}
				}
			}
		}

		if (entityCache.getMethodListeners().size() > 0) {
			for (Method mt : entityCache.getMethodListeners().keySet()) {
				EventType ev = entityCache.getMethodListeners().get(mt);
				if (ev.equals(eventType)) {
					if (mt.getParameterCount() == 0) {
						ReflectionUtils.invokeMethod(mt, newObject);
					} else if (mt.getParameterCount() == 1) {
						ReflectionUtils.invokeMethod(mt, oldObject, newObject);
					}
				}
			}
		}
	}

	@Override
	public void disableNotifyListeners() {
		this.notifyListenersEnabled = false;
	}

	@Override
	public void enableNotifyListeners() {
		this.notifyListenersEnabled = true;
	}

	@Override
	public void registerEventListener(Object listener, Class<?>... entities) throws Exception {
		Set<String> cls = new HashSet<String>();
		cls.add(listener.getClass().getName());
		Method[] methods = ReflectionUtils.getAllMethodsAnnotatedWith(cls,
				new Class[] { PrePersist.class, PostPersist.class, PreUpdate.class, PostUpdate.class, PreRemove.class,
						PostRemove.class, PreValidate.class, PostValidate.class });
		for (Class<?> entity : entities) {
			EntityCache[] entityCaches = getEntityCacheManager().getEntitiesBySuperClassIncluding(entity);
			for (EntityCache entityCache : entityCaches) {
				for (Method mt : methods) {
					if (mt.isAnnotationPresent(PrePersist.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PrePersist));
					} else if (mt.isAnnotationPresent(PostPersist.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PostPersist));
					} else if (mt.isAnnotationPresent(PreUpdate.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PreUpdate));
					} else if (mt.isAnnotationPresent(PostUpdate.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PostUpdate));
					} else if (mt.isAnnotationPresent(PreRemove.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PreRemove));
					} else if (mt.isAnnotationPresent(PostRemove.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PostRemove));
					} else if (mt.isAnnotationPresent(PreValidate.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PreValidate));
					} else if (mt.isAnnotationPresent(PostValidate.class)) {
						entityCache.getEntityListeners().add(EntityListener.of(listener, mt, EventType.PostValidate));
					}
				}
			}
		}

	}

	@Override
	public void removeEventListener(Object listener) {
		getEntityCacheManager().getEntityListeners().remove(listener);
	}

	public boolean isEnableImageCompression() {
		return enableImageCompression;
	}

	public void setEnableImageCompression(boolean enableImageCompression) {
		this.enableImageCompression = enableImageCompression;
	}
	
	
	class Event {
		private EventType eventType;
		private Object oldObject;
		private Object newObject;
		public Event(EventType eventType, Object oldObject, Object newObject) {
			super();
			this.eventType = eventType;
			this.oldObject = oldObject;
			this.newObject = newObject;
		}
		public EventType getEventType() {
			return eventType;
		}
		public void setEventType(EventType eventType) {
			this.eventType = eventType;
		}
		public Object getOldObject() {
			return oldObject;
		}
		public void setOldObject(Object oldObject) {
			this.oldObject = oldObject;
		}
		public Object getNewObject() {
			return newObject;
		}
		public void setNewObject(Object newObject) {
			this.newObject = newObject;
		}		
		
		
	}

}
