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
import java.sql.SQLException;
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
import br.com.anteros.persistence.session.SQLPersister;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.type.LockModeType;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.transaction.Transaction;
import br.com.anteros.persistence.transaction.TransactionFactory;

public class SQLSessionImpl implements SQLSession {

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
	private LazyLoadProxyFactory proxyFactory = new LazyLoadProxyFactoryImpl();
	private SQLQueryAnalyzer sqlQueryAnalyzer;
	private TransactionFactory transactionFactory;
	private Transaction transaction;

	public final int DEFAULT_CACHE_SIZE = 1000;
	private String clientId;

	public SQLSessionImpl(SQLSessionFactory sessionFactory, Connection connection,
			EntityCacheManager entityCacheManager, AbstractSQLRunner queryRunner, DatabaseDialect dialect,
			boolean showSql, boolean formatSql, int queryTimeout, TransactionFactory transactionFactory)
			throws Exception {
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
		this.queryTimeout = queryTimeout;
		this.transactionFactory = transactionFactory;
	}

	public <T> T selectOne(String sql, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();

		SQLQuery<T> query = createSQLQuery(sql);
		T t = query.resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectOne();
		return t;
	}

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(parameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(timeOut).selectOne();
	}

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut)
			throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(timeOut).selectOne();
	}

	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut)
			throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(timeOut).selectOne();
	}

	public <T> T selectOne(String sql, Class<T> resultClass) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(0).selectOne();
	}

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(parameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(queryTimeout)
				.selectOne();
	}

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(0).selectOne();
	}

	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(0).selectOne();
	}

	public <T> T selectOneNamedQuery(String name, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectOneNamedQuery(name, resultClass, queryTimeout);
	}

	public <T> T selectOneNamedQuery(String name, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		flush();
		List<T> result = selectListNamedQuery(name, resultClass, timeOut);
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	public <T> T selectOneProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectOneProcedure(type, name, inputParameters, outputParametersName, resultClass, queryTimeout);
	}

	public <T> T selectOneProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		flush();
		List<T> result = selectListProcedure(type, name, inputParameters, outputParametersName, resultClass, timeOut);
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	public <T> List<T> selectList(String sql, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.resultClass(resultClass).showSql(showSql).formatSql(formatSql).timeOut(timeOut).selectList();
	}

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(parameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(timeOut).selectList();
	}

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut)
			throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(timeOut).selectList();
	}

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut)
			throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery(sql);
		return query.setParameters(namedParameter).resultClass(resultClass).showSql(showSql).formatSql(formatSql)
				.timeOut(timeOut).selectList();
	}

	public <T> List<T> selectList(String sql, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectList(sql, resultClass, queryTimeout);
	}

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectList(sql, parameter, resultClass, queryTimeout);
	}

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass)
			throws Exception {
		errorIfClosed();
		return selectList(sql, namedParameter, resultClass, queryTimeout);
	}

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectList(sql, namedParameter, resultClass, queryTimeout);
	}

	public Object loadData(EntityCache entityCacheTarget, Object owner, final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws IllegalAccessException, Exception {
		errorIfClosed();
		return createSQLQuery("").loadData(entityCacheTarget, owner, descriptionFieldOwner, columnKeyTarget,
				transactionCache);
	}

	public Object select(String sql, ResultSetHandler handler, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).resultSetHandler(handler).showSql(showSql).formatSql(formatSql).timeOut(timeOut)
				.select();
	}

	public Object select(String sql, Object[] parameter, ResultSetHandler handler, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(parameter).resultSetHandler(handler).showSql(showSql)
				.formatSql(formatSql).timeOut(timeOut).select();
	}

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler, int timeOut)
			throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(namedParameter).resultSetHandler(handler).showSql(showSql)
				.formatSql(formatSql).timeOut(timeOut).select();
	}

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler, int timeOut)
			throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(namedParameter).resultSetHandler(handler).showSql(showSql)
				.formatSql(formatSql).timeOut(timeOut).select();
	}

	public Object select(String sql, ResultSetHandler handler) throws Exception {
		errorIfClosed();
		return select(sql, handler, queryTimeout);
	}

	public Object select(String sql, Object[] parameter, ResultSetHandler handler) throws Exception {
		errorIfClosed();
		return select(sql, parameter, handler, queryTimeout);
	}

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler) throws Exception {
		errorIfClosed();
		return select(sql, namedParameter, handler, queryTimeout);
	}

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler) throws Exception {
		errorIfClosed();
		return select(sql, namedParameter, handler, queryTimeout);
	}

	public <T> List<T> selectProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, ResultSetHandler handler) throws Exception {
		errorIfClosed();
		flush();
		return selectProcedure(type, name, inputParameters, outputParametersName, handler, queryTimeout);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> selectProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, ResultSetHandler handler, int timeOut) throws Exception {
		errorIfClosed();
		flush();
		List<T> result = (List<T>) queryRunner.queryProcedure(this, dialect, type, name, handler, inputParameters,
				outputParametersName, showSql, timeOut, clientId);
		return result;
	}

	public <T> T selectId(Identifier<T> id) throws Exception {
		errorIfClosed();
		return selectId(id, queryTimeout);
	}

	public <T> T selectId(Identifier<T> id, int timeOut) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = this.createSQLQuery("");
		return query.identifier(id).selectId();
	}

	public ResultSet executeQuery(String sql, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).showSql(showSql).formatSql(formatSql).timeOut(timeOut).executeQuery();
	}

	public ResultSet executeQuery(String sql, Object[] parameter, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(parameter).showSql(showSql).formatSql(formatSql).timeOut(timeOut)
				.executeQuery();
	}

	public ResultSet executeQuery(String sql, Map<String, Object> parameter, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(parameter).showSql(showSql).formatSql(formatSql).timeOut(timeOut)
				.executeQuery();
	}

	public ResultSet executeQuery(String sql, NamedParameter[] parameter, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(parameter).showSql(showSql).formatSql(formatSql).timeOut(timeOut)
				.executeQuery();
	}

	public ResultSet executeQuery(String sql) throws Exception {
		errorIfClosed();
		return executeQuery(sql, queryTimeout);
	}

	public ResultSet executeQuery(String sql, Object[] parameter) throws Exception {
		errorIfClosed();
		return executeQuery(sql, parameter, queryTimeout);
	}

	public ResultSet executeQuery(String sql, Map<String, Object> parameter) throws Exception {
		errorIfClosed();
		return executeQuery(sql, parameter, queryTimeout);
	}

	public ResultSet executeQuery(String sql, NamedParameter[] parameter) throws Exception {
		errorIfClosed();
		return executeQuery(sql, parameter, queryTimeout);
	}

	public ProcedureResult executeProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName) throws Exception {
		errorIfClosed();
		return executeProcedure(type, name, inputParameters, outputParametersName, queryTimeout);
	}

	public ProcedureResult executeProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, int timeOut) throws Exception {
		errorIfClosed();
		return queryRunner.executeProcedure(this, dialect, type, name, inputParameters, outputParametersName, showSql,
				timeOut, clientId);
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

	public <T> List<T> selectListNamedQuery(String name, Object[] parameters, Class<T> resultClass) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery("");
		return query.namedQuery(name).setParameters(parameters).resultClass(resultClass).selectListNamedQuery();
	}

	public <T> List<T> selectListNamedQuery(String name, Object[] parameters, Class<T> resultClass, int timeOut)
			throws Exception {
		errorIfClosed();
		return selectListNamedQuery(name, parameters, resultClass, timeOut);
	}

	public <T> List<T> selectListNamedQuery(String name, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectListNamedQuery(name, new Object[] {}, resultClass, queryTimeout);
	}

	public <T> List<T> selectListNamedQuery(String name, Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		return selectListNamedQuery(name, new Object[] {}, resultClass, timeOut);
	}

	public <T> List<T> selectListNamedQuery(String name, Map<String, Object> namedParameter, Class<T> resultClass,
			int timeOut) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery("");
		return query.namedQuery(name).setParameters(namedParameter).resultClass(resultClass).timeOut(timeOut)
				.selectListNamedQuery();
	}

	public <T> List<T> selectListNamedQuery(String name, Map<String, Object> namedParameter, Class<T> resultClass)
			throws Exception {
		errorIfClosed();
		return selectListNamedQuery(name, namedParameter, resultClass);
	}

	public <T> List<T> selectListNamedQuery(String name, NamedParameter[] namedParameter, Class<T> resultClass)
			throws Exception {
		errorIfClosed();
		return selectListNamedQuery(name, namedParameter, resultClass, queryTimeout);
	}

	public <T> List<T> selectListNamedQuery(String name, NamedParameter[] namedParameter, Class<T> resultClass,
			int timeOut) throws Exception {
		errorIfClosed();
		SQLQuery<T> query = createSQLQuery("");
		return query.namedQuery(name).setParameters(namedParameter).resultClass(resultClass).timeOut(timeOut)
				.selectListNamedQuery();
	}

	public <T> List<T> selectListProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, Class<T> resultClass) throws Exception {
		errorIfClosed();
		return selectListProcedure(type, name, inputParameters, outputParametersName, resultClass, queryTimeout);
	}

	public <T> List<T> selectListProcedure(CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, Class<T> resultClass, int timeOut) throws Exception {
		SQLQuery<T> query = createSQLQuery("");
		errorIfClosed();
		return query.callableType(type).procedureOrFunctionName(name).setParameters(inputParameters)
				.outputParametersName(outputParametersName).resultClass(resultClass).timeOut(timeOut)
				.selectListProcedure();
	}

	public <T> Identifier<T> getIdentifier(T owner) throws Exception {
		errorIfClosed();
		return Identifier.create(this, owner);
	}

	public <T> Identifier<T> createIdentifier(Class<T> clazz) throws Exception {
		errorIfClosed();
		return Identifier.create(this, clazz);
	}

	public <T> SQLSessionResult selectListAndResultSet(String sql, NamedParameter[] namedParameter,
			Class<T> resultClass, int timeOut) throws Exception {
		errorIfClosed();
		return createSQLQuery(sql).setParameters(namedParameter).resultClass(resultClass).timeOut(timeOut)
				.selectListAndResultSet();
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

	public <T> SQLQuery<T> createSQLQuery(String sql) {
		errorIfClosed();
		SQLQuery<T> query = new SQLQueryImpl<T>(this);
		query.sql(sql);
		return query;
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

	public void enableLockMode() throws Exception {
		errorIfClosed();
		throw new Exception("Método não implementado.");

	}

	public void disableLockMode() throws Exception {
		errorIfClosed();
		throw new Exception("Método não implementado.");
	}

	public void executeDDL(String ddl) throws Exception {
		errorIfClosed();
		getRunner().executeDDL(getConnection(), ddl, showSql, formatSql, "");
	}

	public EntityHandler createNewEntityHandler(Class<?> resultClass, Map<String, String> expressions,
			Cache transactionCache) throws Exception {
		errorIfClosed();
		return new EntityHandler(proxyFactory, resultClass, getEntityCacheManager(), expressions, this,
				transactionCache);
	}

	public <T> T selectOne(String sql, Class<T> resultClass, LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, LockModeType lockMode)
			throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, LockModeType lockMode)
			throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, LockModeType lockMode)
			throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, int timeOut, LockModeType lockMode)
			throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectId(Identifier<T> id, LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectId(Identifier<T> id, int timeOut, LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, Class<T> resultClass, LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, LockModeType lockMode)
			throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, Class<T> resultClass, int timeOut, LockModeType lockMode)
			throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, int timeOut,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut,
			LockModeType lockMode) throws Exception {
		errorIfClosed();
		return null;
	}

	public void lock(Object entity, LockModeType mode) {
		errorIfClosed();
	}

	public void lock(Object entity, LockModeType mode, int timeout) {
		errorIfClosed();
	}

	public void lock(Object entity) {
		errorIfClosed();
	}

	public void lockAll(Collection<?> entities, LockModeType mode) {
		errorIfClosed();
	}

	public void lockAll(Collection<?> entities, LockModeType mode, int timeout) {
		errorIfClosed();
	}

	public void lockAll(Collection<?> entities) {
		errorIfClosed();
	}

	public void lockAll(Object[] entities, LockModeType mode) {
		errorIfClosed();
	}

	public void lockAll(Object[] entities, LockModeType mode, int timeout) {
		errorIfClosed();
	}

	public void lockAll(Object... entities) {
		errorIfClosed();
	}

	public boolean isProxyObject(Object object) throws Exception {
		return proxyFactory.isProxyObject(object);
	}

	public boolean proxyIsInitialized(Object object) throws Exception {
		return proxyFactory.proxyIsInitialized(object);
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
		// TODO Auto-generated method stub
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

	public SQLQueryAnalyzer getSQLQueryAnalyzer() {
		return this.sqlQueryAnalyzer;
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

}
