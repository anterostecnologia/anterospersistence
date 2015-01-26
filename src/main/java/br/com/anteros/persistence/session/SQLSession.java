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
package br.com.anteros.persistence.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.handler.EntityHandler;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.session.query.ExpressionFieldMapper;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyserAlias;
import br.com.anteros.persistence.session.query.TypedSQLQuery;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.transaction.Transaction;

public interface SQLSession {

	/*
	 * Localiza um objeto pela sua chave
	 */
	public <T> T find(Class<T> entityClass, Object primaryKey) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions, Map<String, Object> properties) throws Exception;

	public <T> T find(Identifier<T> id) throws Exception;

	public <T> T find(Identifier<T> id, LockOptions lockOptions) throws Exception;

	public <T> T find(Identifier<T> id, Map<String, Object> properties) throws Exception;

	public <T> T find(Identifier<T> id, Map<String, Object> properties, LockOptions lockOptions) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, boolean readOnly) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties, boolean readOnly) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions, boolean readOnly) throws Exception;

	public <T> T find(Class<T> entityClass, Object primaryKey, LockOptions lockOptions, Map<String, Object> properties, boolean readOnly)
			throws Exception;

	public <T> T find(Identifier<T> id, boolean readOnly) throws Exception;

	public <T> T find(Identifier<T> id, LockOptions lockOptions, boolean readOnly) throws Exception;

	public <T> T find(Identifier<T> id, Map<String, Object> properties, boolean readOnly) throws Exception;

	public <T> T find(Identifier<T> id, Map<String, Object> properties, LockOptions lockOptions, boolean readOnly) throws Exception;

	/*
	 * Atualiza o objeto com dados do banco descartando alterações na transação atual
	 */
	public void refresh(Object entity) throws Exception;

	public void refresh(Object entity, Map<String, Object> properties) throws Exception;

	public void refresh(Object entity, LockOptions lockOptions) throws Exception;

	public void refresh(Object entity, LockOptions lockOptions, Map<String, Object> properties) throws Exception;

	/*
	 * Bloqueia o objeto
	 */
	public void lock(Object entity, LockOptions lockOptions) throws Exception;

	/*
	 * Bloqueia a lista de objetos
	 */
	public void lockAll(Collection<?> entities, LockOptions lockOptions) throws Exception;

	public void lockAll(Object[] entities, LockOptions lockOptions) throws Exception;

	/*
	 * Desconecta o objeto do contexto de persistência
	 */
	public void detach(Object entity);

	/**
	 * Remove todas as instâncias dos objetos da classe passada por parâmetro gerenciadas pela sessão
	 * 
	 * @param object
	 */
	public void evict(Class class0);

	/**
	 * Limpa o cache de entidades gerenciadas da sessão
	 */
	public void evictAll();

	/*
	 * Cria uma query
	 */
	public SQLQuery createQuery(String sql) throws Exception;

	public SQLQuery createQuery(String sql, Object parameters) throws Exception;

	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass) throws Exception;

	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, Object parameters) throws Exception;

	public SQLQuery createQuery(String sql, LockOptions lockOptions) throws Exception;

	public SQLQuery createQuery(String sql, Object parameters, LockOptions lockOptions) throws Exception;

	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, LockOptions lockOptions) throws Exception;

	public <T> TypedSQLQuery<T> createQuery(String sql, Class<T> resultClass, Object parameters, LockOptions lockOptions) throws Exception;

	/*
	 * Cria uma query nomeada
	 */
	public SQLQuery createNamedQuery(String name) throws Exception;

	public SQLQuery createNamedQuery(String name, Object parameters) throws Exception;

	public <T> TypedSQLQuery<T> createNamedQuery(String name, Class<T> resultClass) throws Exception;

	public <T> TypedSQLQuery<T> createNamedQuery(String name, Class<T> resultClass, Object parameters) throws Exception;


	/*
	 * Cria uma query stored procedure
	 */
	public SQLQuery createStoredProcedureQuery(String procedureName, CallableType type) throws Exception;

	public SQLQuery createStoredProcedureQuery(String procedureName, CallableType type, Object parameters) throws Exception;

	public <T> TypedSQLQuery<T> createStoredProcedureQuery(String procedureName, CallableType type, Class<T> resultClass) throws Exception;

	public <T> TypedSQLQuery<T> createStoredProcedureQuery(String procedureName, CallableType type, Class<T> resultClass, Object[] parameters)
			throws Exception;

	/*
	 * Cria uma query stored procedure nomeada
	 */
	public SQLQuery createStoredProcedureNamedQuery(String name) throws Exception;

	public SQLQuery createStoredProcedureNamedQuery(String name, Object parameters) throws Exception;

	public <T> TypedSQLQuery<T> createStoredProcedureNamedQuery(String name, Class<T> resultClass) throws Exception;

	public <T> TypedSQLQuery<T> createStoredProcedureNamedQuery(String name, Class<T> resultClass, Object[] parameters) throws Exception;

	public Object save(Object object) throws Exception;

	public void save(Object[] object) throws Exception;

	public void save(Class<?> clazz, String[] columns, String[] values) throws Exception;

	public void remove(Object object) throws Exception;

	public void remove(Object[] object) throws Exception;

	public void removeAll(Class<?> clazz) throws Exception;

	public long update(String sql) throws Exception;

	public long update(String sql, Object[] params) throws Exception;

	public long update(String sql, NamedParameter[] params) throws Exception;

	public void flush() throws Exception;

	public void forceFlush(Set<String> tableNames) throws Exception;

	public void close() throws Exception;

	public void onBeforeExecuteCommit(Connection connection) throws Exception;

	public void onBeforeExecuteRollback(Connection connection) throws Exception;

	public void onAfterExecuteCommit(Connection connection) throws Exception;

	public void onAfterExecuteRollback(Connection connection) throws Exception;

	public EntityCacheManager getEntityCacheManager();

	public DatabaseDialect getDialect();

	public Connection getConnection();

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

	public void removeTable(String tableName) throws Exception;

	public EntityHandler createNewEntityHandler(Class<?> resultClass, List<ExpressionFieldMapper> expressionsFieldMapper,
			Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases, Cache transactionCache, boolean allowDuplicateObjects,
			Object objectToRefresh, int firstResult, int maxResults, boolean readOnly, LockOptions lockOptions) throws Exception;

	public boolean isProxyObject(Object object) throws Exception;

	public boolean proxyIsInitialized(Object object) throws Exception;

	public void savePoint(String savepoint) throws Exception;

	public void rollbackToSavePoint(String savepoint) throws Exception;

	public <T> T cloneEntityManaged(Object object) throws Exception;

	public boolean isClosed() throws Exception;

	public void setClientInfo(String clientInfo) throws SQLException;

	public String getClientInfo() throws SQLException;

	public Transaction getTransaction() throws Exception;

	public SQLSessionFactory getSQLSessionFactory();

	public void clear() throws Exception;

	public void executeDDL(String ddl) throws Exception;
	
	public String applyLock(String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception;

}
