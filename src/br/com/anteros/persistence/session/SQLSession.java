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
package br.com.anteros.persistence.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
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
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.type.LockModeType;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

public interface SQLSession {

	public <T> T selectOne(String sql, Class<T> resultClass) throws Exception;

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass) throws Exception;

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception;

	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception;

	public <T> T selectOne(String sql, Class<T> resultClass, int timeOut) throws Exception;

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, int timeOut) throws Exception;

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut) throws Exception;
	
	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut) throws Exception;
	
	public <T> T selectOne(String sql, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> T selectOne(String sql, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;

	public <T> T selectOne(String sql, Object[] parameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;

	public <T> T selectOne(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;
	
	public <T> T selectOne(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;

	public <T> T selectOneNamedQuery(String name, Class<T> resultClass) throws Exception;

	public <T> T selectOneNamedQuery(String name, Class<T> resultClass, int timeOut) throws Exception;

	public <T> T selectOneProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName, Class<T> resultClass)
			throws Exception;

	public <T> T selectOneProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName, Class<T> resultClass,
			int timeOut) throws Exception;

	public <T> List<T> selectList(String sql, Class<T> resultClass) throws Exception;

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass) throws Exception;

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception;

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception;
	
	public <T> List<T> selectList(String sql, Class<T> resultClass, int timeOut) throws Exception;

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, int timeOut) throws Exception;

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut)
			throws Exception;

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut) throws Exception;
	
	public <T> List<T> selectList(String sql, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception;

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, LockModeType lockMode) throws Exception;
	
	public <T> List<T> selectList(String sql, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;

	public <T> List<T> selectList(String sql, Object[] parameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;

	public <T> List<T> selectList(String sql, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode)
			throws Exception;

	public <T> List<T> selectList(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut, LockModeType lockMode) throws Exception;

	public <T> SQLSessionResult selectListAndResultSet(String sql, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut) throws Exception;

	public <T> List<T> selectListProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			Class<T> resultClass) throws Exception;

	public <T> List<T> selectListProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			Class<T> resultClass, int timeOut) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, Object[] parameters, Class<T> resultClass) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, Object[] parameters, Class<T> resultClass, int timeOut) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, Class<T> resultClass) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, Class<T> resultClass, int timeOut) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, Map<String, Object> namedParameter, Class<T> resultClass) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, Map<String, Object> namedParameter, Class<T> resultClass, int timeOut)
			throws Exception;

	public <T> List<T> selectListNamedQuery(String name, NamedParameter[] namedParameter, Class<T> resultClass) throws Exception;

	public <T> List<T> selectListNamedQuery(String name, NamedParameter[] namedParameter, Class<T> resultClass, int timeOut)
			throws Exception;


	public Object loadData(EntityCache entityCacheTarget, Object owner, DescriptionField descriptionFieldOwner, Map<String, Object> columnKeyTarget,
			Cache transactionCache) throws IllegalAccessException, Exception;

	public Object select(String sql, ResultSetHandler handler) throws Exception;

	public Object select(String sql, Object[] parameter, ResultSetHandler handler) throws Exception;

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler) throws Exception;

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler) throws Exception;

	public Object select(String sql, ResultSetHandler handler, int timeOut) throws Exception;

	public Object select(String sql, Object[] parameter, ResultSetHandler handler, int timeOut) throws Exception;

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler, int timeOut) throws Exception;

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler, int timeOut) throws Exception;

	public <T> List<T> selectProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			ResultSetHandler handler) throws Exception;

	public <T> List<T> selectProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName,
			ResultSetHandler handler, int timeOut) throws Exception;

	public <T> T selectId(Identifier<T> id) throws Exception;

	public <T> T selectId(Identifier<T> id, int timeOut) throws Exception;
	
	public <T> T selectId(Identifier<T> id, LockModeType lockMode) throws Exception;

	public <T> T selectId(Identifier<T> id, int timeOut, LockModeType lockMode) throws Exception;
	
	public void lock(Object entity, LockModeType mode);
	
	public void lock(Object entity, LockModeType mode, int timeout);

    public void lock(Object entity);

    public void lockAll(Collection<?> entities, LockModeType mode);
    
    public void lockAll(Collection<?> entities, LockModeType mode, int timeout);

    public void lockAll(Collection<?> entities);

    public void lockAll(Object[] entities, LockModeType mode);
    
    public void lockAll(Object[] entities, LockModeType mode, int timeout);

    public void lockAll(Object... entities);

	public ResultSet executeQuery(String sql) throws Exception;

	public ResultSet executeQuery(String sql, Object[] parameter) throws Exception;

	public ResultSet executeQuery(String sql, Map<String, Object> parameter) throws Exception;

	public ResultSet executeQuery(String sql, NamedParameter[] parameter) throws Exception;

	public ResultSet executeQuery(String sql, int timeOut) throws Exception;

	public ResultSet executeQuery(String sql, Object[] parameter, int timeOut) throws Exception;

	public ResultSet executeQuery(String sql, Map<String, Object> parameter, int timeOut) throws Exception;

	public ResultSet executeQuery(String sql, NamedParameter[] parameter, int timeOut) throws Exception;
	
	public void executeDDL(String ddl) throws Exception;

	public ProcedureResult executeProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName) throws Exception;

	public ProcedureResult executeProcedure(CallableType type, String name, Object[] inputParameters, String[] outputParametersName, int timeOut)
			throws Exception;

	public Object save(Object object) throws Exception;

	public void save(Object[] object) throws Exception;

	public void save(Class<?> clazz, String[] columns, String[] values) throws Exception;

	public void remove(Object object) throws Exception;

	public void remove(Object[] object) throws Exception;

	public void removeAll(Class<?> clazz) throws Exception;

	public long update(String sql) throws Exception;

	public long update(String sql, Object[] params) throws Exception;

	public long update(String sql, NamedParameter[] params) throws Exception;

	public void beginTransaction() throws Exception;

	public void commit() throws Exception;

	public void rollback() throws Exception;

	public void flush() throws Exception;

	public void forceFlush(Set<String> tableNames) throws Exception;

	public void close() throws Exception;

	public void onBeforeExecuteCommit(Connection connection) throws Exception;

	public void onBeforeExecuteRollback(Connection connection) throws Exception;

	public void onAfterExecuteCommit(Connection connection) throws Exception;

	public void onAfterExecuteRollback(Connection connection) throws Exception;

	public EntityCacheManager getEntityCacheManager();

	public DatabaseDialect getDialect();

	public Connection getConnection() throws Exception;

	public AbstractSQLRunner getRunner() throws Exception;

	public SQLPersistenceContext getPersistenceContext();

	public <T> Identifier<T> getIdentifier(T owner) throws Exception;

	public <T> Identifier<T> createIdentifier(Class<T> clazz) throws Exception;

	public void addListener(SQLSessionListener listener);

	public void removeListener(SQLSessionListener listener);

	public List<SQLSessionListener> getListeners();

	public List<CommandSQL> getCommandQueue();

	public Map<Object, Map<DescriptionColumn, IdentifierPostInsert>> getCacheIdentifier();

	public void setFormatSql(boolean sql);

	public void setShowSql(boolean sql);

	public boolean isShowSql();

	public String clientId();

	public void setClientId(String clientId);

	public boolean isFormatSql();

	public <T> SQLQuery<T> createSQLQuery(String sql);

	public void removeTable(String tableName) throws Exception;
	
	public void enableLockMode() throws Exception;
	
	public void disableLockMode() throws Exception;
	
	public EntityHandler createNewEntityHandler(Class<?> resultClass, Map<String, String> expressions,
			Cache transactionCache) throws Exception;
	
	public boolean isProxyObject(Object object) throws Exception;
	
	public boolean proxyIsInitialized(Object object) throws Exception;
	
	public void savePoint(String savepoint) throws Exception;

	public void rollbackToSavePoint(String savepoint) throws Exception;
	
	public SQLQueryAnalyzer getSQLQueryAnalyzer();

	public <T> T cloneEntityManaged(Object object) throws Exception;

	/**
	 * Remove todas as instâncias dos objetos da classe passada por parâmetro
	 * gerenciadas pela sessão
	 * 
	 * @param object
	 */
	public void evict(Class class0);

	/**
	 * Limpa o cache de entidades gerenciadas da sessão
	 */
	public void evictAll();
}
