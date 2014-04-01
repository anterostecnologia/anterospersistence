/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.session.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.handler.EntityHandler;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.proxy.LazyLoadProxyFactory;
import br.com.anteros.persistence.proxy.LazyLoadProxyFactoryImpl;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLPersistenceContext;
import br.com.anteros.persistence.session.SQLPersister;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.type.LockModeType;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

public class SQLSessionImpl implements SQLSession {

	public static int FIRST_RECORD = 0;
	private EntityCacheManager entityCacheManager;
	private Connection connection;
	private DatabaseDialect dialect;
	private AbstractSQLRunner queryRunner;
	private boolean showSql;
	private boolean formatSql;
	private SQLPersistenceContext persistenceContext;
	private List<CommandSQL> commandQueue = new ArrayList<CommandSQL>();
	private SQLSessionFactory sessionFactory;
	private List<SQLSessionListener> listeners = new ArrayList<SQLSessionListener>();
	private Map<Object, Map<DescriptionColumn, IdentifierPostInsert>> cacheIdentifier = new LinkedHashMap<Object, Map<DescriptionColumn, IdentifierPostInsert>>();
	private SQLPersister persister;
	private LazyLoadProxyFactory proxyFactory = new LazyLoadProxyFactoryImpl();
	private SQLQueryAnalyzer sqlQueryAnalyzer;

	public final int DEFAULT_CACHE_SIZE = 1000;
	private String clientId;

	public SQLSessionImpl(SQLSessionFactory sessionFactory, Connection connection, EntityCacheManager entityCacheManager,
			AbstractSQLRunner queryRunner, DatabaseDialect dialect, boolean showSql, boolean formatSql) throws Exception {
		this.entityCacheManager = entityCacheManager;
		this.connection = connection;
		if (connection != null)
			this.connection.setAutoCommit(false);
		this.dialect = dialect;
		this.showSql = showSql;
		this.formatSql = formatSql;
		this.sessionFactory = sessionFactory;
		this.persistenceContext = new SQLPersistenceContextImpl(this, entityCacheManager, DEFAULT_CACHE_SIZE);
		this.persister = new SQLPersisterImpl();
		this.queryRunner = queryRunner;
		this.sqlQueryAnalyzer = new SQLQueryAnalyzer(this);
	}

	@Override
	public <T> T selectOne(String sql, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(parameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, Class<T> resultClass) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(0).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(parameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(0).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(0).selectOne();
	}

	@Override
	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(0).selectOne();
	}

	@Override
	public <T> T selectOneNamedQuery(String name, Class<T> resultClass) throws Exception {
		return selectOneNamedQuery(name, resultClass, 0);
	}

	@Override
	public <T> T selectOneNamedQuery(String name, Class<T> resultClass, int timeOut) throws Exception {
		flush();
		List<T> result = selectListNamedQuery(name, resultClass, timeOut);
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	@Override
	public <T> T selectOneProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName, Class<T> resultClass)
			throws Exception {
		return selectOneProcedure(type, name, inputParameters, outputParametersName, resultClass, 0);
	}

	@Override
	public <T> T selectOneProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName, Class<T> resultClass,
			int timeOut) throws Exception {
		flush();
		List<T> result = selectListProcedure(type, name, inputParameters, outputParametersName, resultClass, 0);
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectList();
	}

	@Override
	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(parameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectList();
	}

	@Override
	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectList();
	}

	@Override
	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectList();
	}

	@Override
	public <T> List<T> selectList(String sql, Class<T> resultClass) throws Exception {
		return selectList(sql, resultClass, 0);
	}

	@Override
	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass) throws Exception {
		return selectList(sql, parameter, resultClass, 0);
	}

	@Override
	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception {
		return selectList(sql, namedParameter, resultClass, 0);
	}

	@Override
	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception {
		return selectList(sql, namedParameter, resultClass, 0);
	}

	public Object loadData(EntityCache entityCacheTarget, Object owner, final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws IllegalAccessException, Exception {
		return createSQLQuery("").loadData(entityCacheTarget, owner, descriptionFieldOwner, columnKeyTarget, transactionCache);
	}

	@Override
	public Object select(String sql, ResultSetHandler handler, int timeOut) throws Exception {
		return createSQLQuery(sql).resultSetHandler(handler).showSql(showSql).formatSql(formatSql).timeOut(timeOut).select();
	}

	@Override
	public Object select(String sql, Object[] parameter, ResultSetHandler handler, int timeOut) throws Exception {
		return createSQLQuery(sql).setParameters(parameter).resultSetHandler(handler).showSql(showSql).formatSql(formatSql).timeOut(timeOut).select();
	}

	@Override
	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler, int timeOut) throws Exception {
		return createSQLQuery(sql).setParameters(namedParameter).resultSetHandler(handler).showSql(showSql).formatSql(formatSql).timeOut(timeOut)
				.select();
	}

	@Override
	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler, int timeOut) throws Exception {
		return createSQLQuery(sql).setParameters(namedParameter).resultSetHandler(handler).showSql(showSql).formatSql(formatSql).timeOut(timeOut)
				.select();
	}

	@Override
	public Object select(String sql, ResultSetHandler handler) throws Exception {
		return select(sql, handler, 0);
	}

	@Override
	public Object select(String sql, Object[] parameter, ResultSetHandler handler) throws Exception {
		return select(sql, parameter, handler, 0);
	}

	@Override
	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler) throws Exception {
		return select(sql, namedParameter, handler, 0);
	}

	@Override
	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler) throws Exception {
		return select(sql, namedParameter, handler, 0);
	}

	@Override
	public <T> List<T> selectProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			ResultSetHandler handler) throws Exception {
		flush();
		return selectProcedure(type, name, inputParameters, outputParametersName, handler, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> selectProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			ResultSetHandler handler, int timeOut) throws Exception {
		flush();
		List<T> result = (List<T>) queryRunner.queryProcedure(this, dialect, type, name, handler, inputParameters, outputParametersName, showSql,
				timeOut, clientId);
		return result;
	}

	@Override
	public <T> T selectId(Identifier<T> id) throws Exception {
		return selectId(id, 0);
	}

	@Override
	public <T> T selectId(Identifier<T> id, int timeOut) throws Exception {
		SQLQuery<T> query = this.createSQLQuery("");
		return query.identifier(id).selectId();
	}

	@Override
	public ResultSet executeQuery(String sql, int timeOut) throws Exception {
		return createSQLQuery(sql).showSql(showSql).formatSql(formatSql).timeOut(timeOut).executeQuery();
	}

	@Override
	public ResultSet executeQuery(String sql, Object[] parameter, int timeOut) throws Exception {
		return createSQLQuery(sql).setParameters(parameter).showSql(showSql).formatSql(formatSql).timeOut(timeOut).executeQuery();
	}

	@Override
	public ResultSet executeQuery(String sql, Map<String, Object> parameter, int timeOut) throws Exception {
		return createSQLQuery(sql).setParameters(parameter).showSql(showSql).formatSql(formatSql).timeOut(timeOut).executeQuery();
	}

	@Override
	public ResultSet executeQuery(String sql, NamedParameter[] parameter, int timeOut) throws Exception {
		return createSQLQuery(sql).setParameters(parameter).showSql(showSql).formatSql(formatSql).timeOut(timeOut).executeQuery();
	}

	@Override
	public ResultSet executeQuery(String sql) throws Exception {
		return executeQuery(sql, 0);
	}

	@Override
	public ResultSet executeQuery(String sql, Object[] parameter) throws Exception {
		return executeQuery(sql, parameter, 0);
	}

	@Override
	public ResultSet executeQuery(String sql, Map<String, Object> parameter) throws Exception {
		return executeQuery(sql, parameter, 0);
	}

	@Override
	public ResultSet executeQuery(String sql, NamedParameter[] parameter) throws Exception {
		return executeQuery(sql, parameter, 0);
	}

	@Override
	public ProcedureResult executeProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName) throws Exception {
		return executeProcedure(type, name, inputParameters, outputParametersName, 0);
	}

	@Override
	public ProcedureResult executeProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName, int timeOut)
			throws Exception {
		return queryRunner.executeProcedure(this, dialect, type, name, inputParameters, outputParametersName, showSql, timeOut, clientId);
	}

	@Override
	public long update(String sql) throws Exception {
		return queryRunner.update(this.getConnection(), sql, listeners);
	}

	@Override
	public long update(String sql, Object[] params) throws Exception {
		return queryRunner.update(this.getConnection(), sql, params, listeners);
	}

	@Override
	public long update(String sql, NamedParameter[] params) throws Exception {
		return queryRunner.update(this.getConnection(), sql, params, listeners);
	}

	@Override
	public Object save(Object object) throws Exception {
		return persister.save(this, object);
	}

	@Override
	public void save(Object[] object) throws Exception {
		persister.save(this, object);
	}

	@Override
	public void remove(Object object) throws Exception {
		persister.remove(this, object);
	}

	@Override
	public void remove(Object[] object) throws Exception {
		persister.remove(this, object);
	}

	@Override
	public void commit() throws Exception {
		if (getConnection() != null) {
			if (!getConnection().getAutoCommit()) {
				getPersistenceContext().onBeforeExecuteCommit(getConnection());
				getConnection().commit();
				getPersistenceContext().onAfterExecuteCommit(getConnection());
			}
		}
	}

	@Override
	public void rollback() throws Exception {
		if (getConnection() != null) {
			if (!getConnection().getAutoCommit()) {
				getPersistenceContext().onBeforeExecuteRollback(getConnection());
				getConnection().rollback();
				getPersistenceContext().onAfterExecuteRollback(getConnection());
			}
		}
	}

	@Override
	public DatabaseDialect getDialect() {
		return dialect;
	}

	public Connection getConnection() throws Exception {
		if (connection == null)
			return sessionFactory.getCurrentConnection();
		else
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

	@Override
	public SQLPersistenceContext getPersistenceContext() {
		return persistenceContext;
	}

	@Override
	public void flush() throws Exception {
		synchronized (commandQueue) {
			for (CommandSQL command : commandQueue)
				command.execute();
			commandQueue.clear();
		}
	}

	public void forceFlush(Set<String> tableNames) throws Exception {
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

	@Override
	public void close() throws Exception {
		synchronized (commandQueue) {
			commandQueue.clear();
		}
	}

	@Override
	public void onBeforeExecuteCommit(Connection connection) throws Exception {
		if (this.getConnection() == connection)
			flush();
	}

	@Override
	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		if (this.getConnection() == connection) {
			synchronized (commandQueue) {
				commandQueue.clear();
			}
		}
	}

	@Override
	public void onAfterExecuteCommit(Connection connection) throws Exception {

	}

	@Override
	public void onAfterExecuteRollback(Connection connection) throws Exception {

	}

	public AbstractSQLRunner getQueryRunner() {
		return queryRunner;
	}

	public void setQueryRunner(AbstractSQLRunner queryRunner) {
		this.queryRunner = queryRunner;
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, Object[] parameters, Class<T> resultClass) throws Exception {
		SQLQuery<T> query = createSQLQuery("");
		return query.namedQuery(name).setParameters(parameters).resultClass(resultClass).selectListNamedQuery();
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, Object[] parameters, Class<T> resultClass, int timeOut) throws Exception {
		return selectListNamedQuery(name, parameters, resultClass, timeOut);
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, Class<T> resultClass) throws Exception {
		return selectListNamedQuery(name, new Object[] {}, resultClass, 0);
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, Class<T> resultClass, int timeOut) throws Exception {
		return selectListNamedQuery(name, new Object[] {}, resultClass, timeOut);
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery("");
		return query.namedQuery(name).setParameters(namedParameter).resultClass(resultClass).timeOut(timeOut).selectListNamedQuery();
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception {
		return selectListNamedQuery(name, namedParameter, resultClass);
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception {
		return selectListNamedQuery(name, namedParameter, resultClass, 0);
	}

	@Override
	public <T> List<T> selectListNamedQuery(String name, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery("");
		return query.namedQuery(name).setParameters(namedParameter).resultClass(resultClass).timeOut(timeOut).selectListNamedQuery();
	}

	@Override
	public <T> List<T> selectListProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			Class<T> resultClass) throws Exception {
		return selectListProcedure(type, name, inputParameters, outputParametersName, resultClass, 0);
	}

	@Override
	public <T> List<T> selectListProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery("");
		return query.callableType(type).procedureOrFunctionName(name).setParameters(inputParameters).outputParametersName(outputParametersName)
				.resultClass(resultClass).timeOut(timeOut).selectListProcedure();
	}

	@Override
	public <T> Identifier<T> getIdentifier(T owner) throws Exception {
		return Identifier.create(this, owner);
	}

	@Override
	public <T> Identifier<T> createIdentifier(Class<T> clazz) throws Exception {
		return Identifier.create(this, clazz);
	}

	@Override
	public <T> SQLSessionResult selectListAndResultSet(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut)
			throws Exception {
		return createSQLQuery(sql).setParameters(namedParameter).resultClass(resultClass).timeOut(timeOut).selectListAndResultSet();
	}

	@Override
	public void addListener(SQLSessionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removeListener(SQLSessionListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	public List<SQLSessionListener> getListeners() {
		return listeners;
	}

	@Override
	public <T> SQLQuery<T> createSQLQuery(String sql) {
		SQLQuery<T> query = new SQLQueryImpl<T>(this);
		query.sql(sql);
		return query;
	}

	@Override
	public List<CommandSQL> getCommandQueue() {
		return commandQueue;
	}

	@Override
	public Map<Object, Map<DescriptionColumn, IdentifierPostInsert>> getCacheIdentifier() {
		return cacheIdentifier;
	}

	@Override
	public String clientId() {
		return clientId;
	}

	@Override
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void save(Class<?> clazz, String[] columns, String[] values) throws Exception {
		throw new Exception("Método não suportado.");
	}

	@Override
	public void removeAll(Class<?> clazz) throws Exception {
		throw new Exception("Método não suportado.");
	}

	@Override
	public void beginTransaction() throws Exception {
		throw new Exception("Método não suportado.");
	}

	@Override
	public void removeTable(String tableName) throws Exception {
		throw new Exception("Método não suportado.");
	}

	@Override
	public void enableLockMode() throws Exception {
		throw new Exception("Método não implementado.");

	}

	@Override
	public void disableLockMode() throws Exception {
		throw new Exception("Método não implementado.");
	}

	@Override
	public void executeDDL(String ddl) throws Exception {
		getRunner().executeDDL(connection, ddl, showSql, formatSql, "");
	}

	@Override
	public EntityHandler createNewEntityHandler(Class<?> resultClass, Map<String, String> expressions, Cache transactionCache) throws Exception {
		return new EntityHandler(proxyFactory, resultClass, getEntityCacheManager(), expressions, this, transactionCache);
	}

	@Override
	public <T> T selectOne(String sql, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectId(Identifier<T> id, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectId(Identifier<T> id, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode)
			throws Exception {
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode)
			throws Exception {
		return null;
	}

	@Override
	public void lock(Object entity, LockModeType mode) {

	}

	@Override
	public void lock(Object entity, LockModeType mode, int timeout) {

	}

	@Override
	public void lock(Object entity) {

	}

	@Override
	public void lockAll(Collection<?> entities, LockModeType mode) {

	}

	@Override
	public void lockAll(Collection<?> entities, LockModeType mode, int timeout) {

	}

	@Override
	public void lockAll(Collection<?> entities) {

	}

	@Override
	public void lockAll(Object[] entities, LockModeType mode) {

	}

	@Override
	public void lockAll(Object[] entities, LockModeType mode, int timeout) {

	}

	@Override
	public void lockAll(Object... entities) {

	}

	@Override
	public boolean isProxyObject(Object object) throws Exception {
		return proxyFactory.isProxyObject(object);
	}

	@Override
	public boolean proxyIsInitialized(Object object) throws Exception {
		return proxyFactory.proxyIsInitialized(object);
	}

	@Override
	public void savePoint(String savepoint) throws Exception {
		throw new Exception("Método não implementado.");
	}

	@Override
	public void rollbackToSavePoint(String savepoint) throws Exception {
		throw new Exception("Método não implementado.");
	}

	@Override
	public <T> T cloneEntityManaged(Object object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void evict(Class object) {
		persistenceContext.evict(object);
	}

	@Override
	public void evictAll() {
		persistenceContext.evictAll();
	}

	@Override
	public SQLQueryAnalyzer getSQLQueryAnalyzer() {
		return this.sqlQueryAnalyzer;
	}

}
