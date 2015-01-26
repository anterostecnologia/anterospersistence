/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.EntityHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.proxy.JavassistLazyLoadFactory;
import br.com.anteros.persistence.proxy.LazyLoadFactory;
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
import br.com.anteros.persistence.session.query.TypedSQLQuery;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.transaction.Transaction;
import br.com.anteros.persistence.transaction.TransactionFactory;

public class SQLSessionImpl implements SQLSession {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(SQLSession.class);

	public static int FIRST_RECORD = 0;
	private EntityCacheManager entityCacheManager;
	private Connection connection;
	private DatabaseDialect dialect;
	private AbstractSQLRunner queryRunner;
	private boolean showSql;
	private boolean formatSql;
	private int queryTimeout = 0;
	private SQLPersistenceContext persistenceContext;
	private List<CommandSQL> commandQueue = new ArrayList<CommandSQL>();
	private SQLSessionFactory sessionFactory;
	private List<SQLSessionListener> listeners = new ArrayList<SQLSessionListener>();
	private Map<Object, Map<DescriptionColumn, IdentifierPostInsert>> cacheIdentifier = new LinkedHashMap<Object, Map<DescriptionColumn, IdentifierPostInsert>>();
	private SQLPersister persister;
	private LazyLoadFactory lazyLoadFactory = new JavassistLazyLoadFactory();
	private TransactionFactory transactionFactory;
	private Transaction transaction;
	private LockManager lockManager;

	private String clientId;

	public SQLSessionImpl(SQLSessionFactory sessionFactory, Connection connection, EntityCacheManager entityCacheManager,
			AbstractSQLRunner queryRunner, DatabaseDialect dialect, boolean showSql, boolean formatSql, int queryTimeout, int lockTimeout,
			TransactionFactory transactionFactory) throws Exception {
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
		
		String lockTimeoutSql = dialect.getSetLockTimeoutString(lockTimeout);
		if (!StringUtils.isEmpty(lockTimeoutSql)){
			connection.prepareStatement(lockTimeoutSql).execute();
		}
	}

	public long update(String sql) throws Exception {
		errorIfClosed();
		return queryRunner.update(this.getConnection(), sql, listeners);
	}

	public long update(String sql, Object[] params) throws Exception {
		errorIfClosed();
		return queryRunner.update(this.getConnection(), sql, params, listeners);
	}

	public long update(String sql, NamedParameter[] params) throws Exception {
		errorIfClosed();
		return queryRunner.update(this.getConnection(), sql, params, listeners);
	}

	public Object save(Object object) throws Exception {
		errorIfClosed();
		return persister.save(this, object);
	}

	public void save(Object[] object) throws Exception {
		errorIfClosed();
		persister.save(this, object);
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
		return showSql;
	}

	public void setShowSql(boolean showSql) {
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
		errorIfClosed();
		synchronized (commandQueue) {
			for (CommandSQL command : commandQueue)
				command.execute();
			commandQueue.clear();
		}
	}

	public void forceFlush(Set<String> tableNames) throws Exception {
		errorIfClosed();
		if (tableNames != null) {
			synchronized (commandQueue) {
				List<CommandSQL> commandsToRemove = new ArrayList<CommandSQL>();
				for (CommandSQL command : commandQueue) {
					if (tableNames.contains(command.getTargetTableName().toUpperCase())) {
						command.execute();
						commandsToRemove.add(command);
					}
				}
				for (CommandSQL command : commandsToRemove)
					commandQueue.remove(command);
			}
		}
	}

	public void close() throws Exception {
		for (SQLSessionListener listener : listeners)
			listener.onClose(this);
		synchronized (commandQueue) {
			commandQueue.clear();
		}
		connection.close();
		LOG.debug("Fechou session " + this);
	}

	public void onBeforeExecuteCommit(Connection connection) throws Exception {
		flush();
	}

	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		if (this.getConnection() == connection) {
			synchronized (commandQueue) {
				commandQueue.clear();
			}
		}
	}

	public void onAfterExecuteCommit(Connection connection) throws Exception {

	}

	public void onAfterExecuteRollback(Connection connection) throws Exception {

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

	public List<CommandSQL> getCommandQueue() {
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
		getRunner().executeDDL(getConnection(), ddl, showSql, formatSql, "");
	}

	public EntityHandler createNewEntityHandler(Class<?> resultClass, List<ExpressionFieldMapper> expressionsFieldMapper,
			Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases, Cache transactionCache, boolean allowDuplicateObjects,
			Object objectToRefresh, int firstResult, int maxResults, boolean readOnly, LockOptions lockOptions) throws Exception {
		errorIfClosed();
		EntityHandler handler = new EntityHandler(lazyLoadFactory, resultClass, getEntityCacheManager(), expressionsFieldMapper, columnAliases, this,
				transactionCache, allowDuplicateObjects, firstResult, maxResults, readOnly, lockOptions);
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
	public <T> T find(Class<T> entityClass, Object id, boolean readOnly) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		if (id instanceof Identifier) {
			if (!((Identifier<?>) id).getClazz().equals(entityClass)) {
				throw new SQLSessionException("Objeto ID é do tipo Identifier porém de uma classe diferente da classe " + entityClass.getName());
			} else
				return find((Identifier<T>) id, readOnly);
		}
		Identifier<T> identifier = Identifier.create(this, entityClass);
		identifier.setIdIfPossible(id);
		return find(identifier, readOnly);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object id, Map<String, Object> properties, boolean readOnly) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		T result = find(entityClass, id, readOnly);
		entityCache.setObjectValues(result, properties);
		return result;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object id, LockOptions lockOptions, boolean readOnly) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		if (id instanceof Identifier) {
			if (!((Identifier<?>) id).getClazz().equals(entityClass)) {
				throw new SQLSessionException("Objeto ID é do tipo Identifier porém de uma classe diferente da classe " + entityClass.getName());
			} else
				return find((Identifier<T>) id, readOnly);
		}
		Identifier<T> identifier = Identifier.create(this, entityClass);
		identifier.setIdIfPossible(id);
		return find(identifier, lockOptions, readOnly);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object id, LockOptions lockOptions, Map<String, Object> properties, boolean readOnly) throws Exception {
		errorIfClosed();
		EntityCache entityCache = entityCacheManager.getEntityCache(entityClass);
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entityClass.getName());
		}
		T result = find(entityClass, id, lockOptions, readOnly);
		entityCache.setObjectValues(result, properties);
		return result;
	}

	@Override
	public <T> T find(Identifier<T> id, boolean readOnly) throws Exception {
		errorIfClosed();
		SQLQuery query = createQuery("");
		query.setReadOnly(readOnly);
		return (T) query.identifier(id).getSingleResult();
	}

	@Override
	public <T> T find(Identifier<T> id, LockOptions lockOptions, boolean readOnly) throws Exception {
		errorIfClosed();
		SQLQuery query = createQuery("");
		query.setReadOnly(readOnly);
		query.setLockOptions(lockOptions);
		return (T) query.identifier(id).getSingleResult();
	}

	@Override
	public <T> T find(Identifier<T> id, Map<String, Object> properties, boolean readOnly) throws Exception {
		errorIfClosed();
		T result = find(id, readOnly);
		id.getEntityCache().setObjectValues(result, properties);
		return result;
	}

	@Override
	public <T> T find(Identifier<T> id, Map<String, Object> properties, LockOptions lockOptions, boolean readOnly) throws Exception {
		errorIfClosed();
		T result = find(id, lockOptions, readOnly);
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
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) throws Exception {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
		EntityCache entityCache = entityCacheManager.getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier);
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
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier, lockOptions);
	}

	@Override
	public void refresh(Object entity, LockOptions lockOptions, Map<String, Object> properties) throws Exception {
		errorIfClosed();
		if (entity == null)
			return;

		persistenceContext.detach(entity);
		EntityCache entityCache = entityCacheManager.getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}
		Identifier<Object> identifier = Identifier.create(this, entity, true);
		find(identifier, lockOptions);
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
	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, LockOptions lockOptions) throws Exception {
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
	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, Object parameters, LockOptions lockOptions) throws Exception {
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
		return new SQLQueryImpl<T>(this).resultClass(resultClass).timeOut(queryTimeout).namedQuery(name).showSql(showSql).formatSql(formatSql);
	}

	@Override
	public <T> TypedSQLQuery<T> createNamedQuery(String name, Class<T> resultClass, Object parameters) throws Exception {
		errorIfClosed();
		return new SQLQueryImpl<T>(this).resultClass(resultClass).namedQuery(name).setParameters(parameters).showSql(showSql).formatSql(formatSql);
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
	public SQLQuery createStoredProcedureQuery(String procedureName, CallableType type, Object parameters) throws Exception {
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
	public <T> TypedSQLQuery<T> createStoredProcedureQuery(String procedureName, CallableType type, Class<T> resultClass) throws Exception {
		errorIfClosed();
		TypedSQLQuery<T> result = new StoredProcedureSQLQueryImpl(this, resultClass, type);
		result.procedureOrFunctionName(procedureName);
		result.timeOut(queryTimeout);
		result.showSql(showSql);
		result.formatSql(formatSql);
		return result;
	}

	@Override
	public <T> TypedSQLQuery<T> createStoredProcedureQuery(String procedureName, CallableType type, Class<T> resultClass, Object[] parameters)
			throws Exception {
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
	public <T> TypedSQLQuery<T> createStoredProcedureNamedQuery(String name, Class<T> resultClass, Object[] parameters) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) throws Exception {
		return find(entityClass, primaryKey, false);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) throws Exception {
		return find(entityClass, primaryKey, properties, false);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions) throws Exception {
		return find(entityClass, primaryKey, lockOptions, false);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions, Map<String, Object> properties) throws Exception {
		return find(entityClass, primaryKey, lockOptions, properties, false);
	}

	@Override
	public <T> T find(Identifier<T> id) throws Exception {
		return find(id, false);
	}

	@Override
	public <T> T find(Identifier<T> id, LockOptions lockOptions) throws Exception {
		return find(id, lockOptions, false);
	}

	@Override
	public <T> T find(Identifier<T> id, Map<String, Object> properties) throws Exception {
		return find(id, properties, false);
	}

	@Override
	public <T> T find(Identifier<T> id, Map<String, Object> properties, LockOptions lockOptions) throws Exception {
		return find(id, properties, lockOptions, false);
	}

	@Override
	public String applyLock(String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception {
		return lockManager.applyLock(this, sql, resultClass, lockOptions);
	}

}
