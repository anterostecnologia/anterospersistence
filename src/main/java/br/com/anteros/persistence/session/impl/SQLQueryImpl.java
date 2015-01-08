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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.BeanHandler;
import br.com.anteros.persistence.handler.ElementCollectionHandler;
import br.com.anteros.persistence.handler.ElementMapHandler;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.descriptor.DescriptionNamedQuery;
import br.com.anteros.persistence.metadata.descriptor.type.FieldType;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.parameter.NamedParameterList;
import br.com.anteros.persistence.parameter.NamedParameterParserResult;
import br.com.anteros.persistence.proxy.collection.DefaultSQLList;
import br.com.anteros.persistence.proxy.collection.DefaultSQLMap;
import br.com.anteros.persistence.proxy.collection.DefaultSQLSet;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.cache.PersistenceMetadataCache;
import br.com.anteros.persistence.session.cache.SQLCache;
import br.com.anteros.persistence.session.lock.type.LockModeType;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzerException;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzerResult;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.session.query.SQLQueryNoResultException;
import br.com.anteros.persistence.session.query.SQLQueryNonUniqueResultException;
import br.com.anteros.persistence.session.query.TypedSQLQuery;
import br.com.anteros.persistence.sql.command.Select;
import br.com.anteros.persistence.sql.lob.AnterosBlob;
import br.com.anteros.persistence.sql.lob.AnterosClob;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;
import br.com.anteros.persistence.util.SQLParserUtil;

@SuppressWarnings("all")
public class SQLQueryImpl<T> implements TypedSQLQuery<T>, SQLQuery {
	protected SQLSession session;
	private Class<?> resultClass;
	protected Identifier identifier;
	protected boolean showSql;
	protected boolean formatSql;
	protected ResultSetHandler handler;
	protected Map<Integer, NamedParameter> namedParameters = new TreeMap<Integer, NamedParameter>();
	protected Map<Integer, Object> parameters = new TreeMap<Integer, Object>();
	public final int DEFAULT_CACHE_SIZE = 100000;
	private String sql;
	public static int FIRST_RECORD = 0;
	protected int timeOut = 0;
	private String namedQuery;
	protected LockModeType lockMode;
	protected boolean allowDuplicateObjects = false;
	private int firstResult;
	private int maxResults;
	private boolean readOnly = false;
	private int amountOfInstantiatedObjects = 0;

	public SQLQueryImpl(SQLSession session) {
		this.session = session;
	}

	public SQLQueryImpl(SQLSession session, Class<?> resultClass) {
		this.session = session;
		this.resultClass = resultClass;
	}

	public TypedSQLQuery<T> resultClass(Class<?> resultClass) {
		this.setResultClass(resultClass);
		return this;
	}

	public TypedSQLQuery<T> identifier(Identifier<?> identifier) {
		this.identifier = identifier;
		return this;
	}

	public SQLSession getSession() {
		return session;
	}

	public TypedSQLQuery<T> sql(String sql) {
		this.sql = sql;

		boolean inQuotes = false;
		int paramCount = 0;

		for (int i = 0; i < sql.length(); ++i) {
			int c = sql.charAt(i);
			if (c == '\'')
				inQuotes = !inQuotes;
			if (c == '?' && !inQuotes) {
				paramCount++;
				parameters.put(paramCount, null);
			}
		}

		NamedParameterParserResult parserResult = (NamedParameterParserResult) PersistenceMetadataCache.getInstance()
				.get("NamedParameters:" + sql);
		if (parserResult == null) {
			parserResult = NamedParameterStatement.parse(sql, null);
			PersistenceMetadataCache.getInstance().put("NamedParameters:" + sql, parserResult);
		}
		paramCount = 0;
		for (NamedParameter namedParameter : parserResult.getNamedParameters()) {
			paramCount++;
			namedParameters.put(paramCount, namedParameter);
		}

		return this;
	}

	public TypedSQLQuery<T> showSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}

	public TypedSQLQuery<T> formatSql(boolean formatSql) {
		this.formatSql = formatSql;
		return this;
	}

	public TypedSQLQuery<T> resultSetHandler(ResultSetHandler handler) {
		this.handler = handler;
		return this;
	}

	public TypedSQLQuery<T> clear() {
		namedParameters.clear();
		sql = "";

		return this;
	}

	public TypedSQLQuery<T> setParameters(NamedParameter[] parameters) throws Exception {
		if (parameters.length != this.namedParameters.size())
			throw new SQLQueryException("Número de parâmetros diferente do número encontrado na instrução SQL.");
		for (NamedParameter parameter : parameters) {
			boolean found = false;
			for (Integer index : namedParameters.keySet()) {
				NamedParameter np = namedParameters.get(index);
				if (np.getName().equals(parameter.getName())) {
					namedParameters.put(index, parameter);
					found = true;
					break;
				}
			}
			if (!found)
				throw new SQLQueryException("Parâmetro " + parameter.getName()
						+ " não encontrado. Verifique se o parâmetro existe ou se o SQL já foi definido.");
		}
		return this;
	}

	public TypedSQLQuery<T> setParameters(Object[] parameters) throws Exception {
		if (parameters.length != this.parameters.size())
			throw new SQLQueryException("Número de parâmetros diferente do número encontrado na instrução SQL.");
		for (int i = 0; i < parameters.length; i++)
			this.parameters.put(i + 1, parameters[i]);

		return this;
	}

	public TypedSQLQuery<T> setParameters(Map<String, Object> parameters) throws Exception {
		if (parameters.size() != this.namedParameters.size())
			throw new SQLQueryException("Número de parâmetros diferente do número encontrado na instrução SQL.");

		for (String parameterName : parameters.keySet()) {
			Object value = parameters.get(parameterName);
			for (Integer index : namedParameters.keySet()) {
				NamedParameter np = namedParameters.get(index);
				if (np.getName().equals(parameterName)) {
					namedParameters.put(index, new NamedParameter(parameterName, value));
					break;
				}
			}
		}
		return this;
	}

	public TypedSQLQuery<T> setInteger(int parameterIndex, int value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	protected void validateParameterIndex(int parameterIndex) throws SQLQueryException {
		if (parameters.size() == 0) {
			throw new SQLQueryException("Instrução SQL não possuí parâmetros.");
		}
		if (parameterIndex < 0 || parameterIndex > parameters.size() - 1) {
			throw new SQLQueryException("Índice do parâmetro não existe: " + parameterIndex + " no SQL: " + sql);
		}
	}

	public TypedSQLQuery<T> setString(int parameterIndex, String value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setLong(int parameterIndex, long value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setNull(int parameterIndex) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, null);
		return this;
	}

	public TypedSQLQuery<T> setDate(int parameterIndex, Date value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setDateTime(int parameterIndex, Date value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setObject(int parameterIndex, Object object) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, object);
		return this;
	}

	public TypedSQLQuery<T> setBlob(int parameterIndex, InputStream inputStream) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, inputStream);
		return this;
	}

	public TypedSQLQuery<T> setBlob(int parameterIndex, byte[] bytes) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, bytes);
		return this;
	}

	public TypedSQLQuery<T> setBoolean(int parameterIndex, boolean value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setDouble(int parameterIndex, double value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setFloat(int parameterIndex, float value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setBigDecimal(int parameterIndex, BigDecimal value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public TypedSQLQuery<T> setInteger(String parameterName, int value) throws Exception {
		set(parameterName, value);
		return this;
	}

	protected void set(String parameterName, Object value) throws SQLQueryException {
		boolean found = false;
		for (Integer index : namedParameters.keySet()) {
			NamedParameter np = namedParameters.get(index);
			if (np.getName().equals(parameterName)) {
				np.setValue(value);
				found = true;
				break;
			}
		}
		if (!found)
			throw new SQLQueryException("Parâmetro " + parameterName + " não encontrado.");

	}

	public TypedSQLQuery<T> setString(String parameterName, String value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setLong(String parameterName, long value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setNull(String parameterName) throws Exception {
		set(parameterName, null);
		return this;
	}

	public TypedSQLQuery<T> setDate(String parameterName, Date value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setDateTime(String parameterName, Date value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setObject(String parameterName, Object object) throws Exception {
		set(parameterName, object);
		return this;
	}

	public TypedSQLQuery<T> setBlob(String parameterName, InputStream inputStream) throws Exception {
		set(parameterName, inputStream);
		return this;
	}

	public TypedSQLQuery<T> setBlob(String parameterName, byte[] bytes) throws Exception {
		set(parameterName, bytes);
		return this;
	}

	public TypedSQLQuery<T> setBoolean(String parameterName, boolean value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setDouble(String parameterName, double value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setFloat(String parameterName, float value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public TypedSQLQuery<T> setBigDecimal(String parameterName, BigDecimal value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public List getResultList() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		/*
		 * Se for uma query nomeada
		 */
		if (this.getNamedQuery() != null) {
			EntityCache cache = session.getEntityCacheManager().getEntityCache(getResultClass());
			DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(this.getNamedQuery());
			if (namedQuery == null)
				throw new SQLQueryException("Query nomeada " + this.getNamedQuery() + " não encontrada.");
			this.sql = namedQuery.getQuery();
		}
		List result = null;
		/*
		 * Se o usuário setou um Handler específico. Processa o resultSet com o
		 * handler e devolve o objeto em forma de lista caso o resultado já não
		 * seja uma lista
		 */
		if (handler != null) {
			Object resultObject = getResultObjectByCustomHandler();
			if (resultObject instanceof Collection)
				result = new ArrayList((Collection) resultObject);
			else {
				result = new ArrayList();
				result.add(resultObject);
			}
		} else {
			/*
			 * Processa o resultSet usando o EntityHandler para criar os objetos
			 */
			result = getResultListByEntityHandler(null);
		}

		if (result == null)
			return Collections.EMPTY_LIST;
		return result;
	}

	protected List getResultListByEntityHandler(Object objectToRefresh) throws Exception, SQLQueryAnalyzerException,
			SQLException {
		List result;
		if (getResultClass() == null)
			throw new SQLQueryException("Informe a Classe para executar a consulta.");

		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));

		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(getResultClass());
		ResultSetHandler handler = null;

		SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance()
				.get(getResultClass().getName()+":"+sql);
		if (analyzerResult == null) {
			analyzerResult = new SQLQueryAnalyzer(session.getEntityCacheManager(), session.getDialect()).analyze(sql, getResultClass());
			PersistenceMetadataCache.getInstance().put(getResultClass().getName()+":"+sql, analyzerResult);
		}

		SQLCache transactionCache = new SQLCache(DEFAULT_CACHE_SIZE);

		try {
			if (entityCache == null)
				handler = new BeanHandler(getResultClass());
			else {
				if (sql.toLowerCase().indexOf(entityCache.getTableName().toLowerCase()) < 0) {
					throw new SQLException("A tabela " + entityCache.getTableName() + " da classe "
							+ getResultClass().getName()
							+ " não foi localizada no SQL informado. Não será possível executar a consulta.");
				}
				handler = session.createNewEntityHandler(getResultClass(), analyzerResult.getExpressions(),
						analyzerResult.getColumnAliases(), transactionCache, allowDuplicateObjects, objectToRefresh,
						firstResult, maxResults, readOnly);
			}

			if (this.parameters.size() > 0)
				result = (List) session.getRunner().query(session.getConnection(), analyzerResult.getParsedSql(),
						handler, parameters.values().toArray(), showSql, formatSql, timeOut, session.getListeners(),
						session.clientId());
			else if (this.namedParameters.size() > 0)
				result = (List) session.getRunner().query(session.getConnection(), analyzerResult.getParsedSql(),
						handler, namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql,
						timeOut, session.getListeners(), session.clientId());
			else
				result = (List) session.getRunner().query(session.getConnection(), analyzerResult.getParsedSql(),
						handler, showSql, formatSql, timeOut, session.getListeners(), session.clientId());

			if (handler!=null)
				amountOfInstantiatedObjects=handler.getAmountOfInstantiatedObjects();
		} finally {
			transactionCache.clear();
			session.getPersistenceContext().clearCache();
		}
		return result;
	}

	protected T getSingleResultId() throws Exception {
		if (identifier == null)
			throw new SQLQueryException("Informe o Identicador para executar a consulta.");

		Select select = new Select(session.getDialect());
		select.addTableName(identifier.getEntityCache().getTableName());
		Map<String, Object> columns = identifier.getColumns();
		List<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean appendOperator = false;
		for (String column : columns.keySet()) {
			if (appendOperator)
				select.and();
			select.addCondition(column, "=", ":P" + column);
			params.add(new NamedParameter("P" + column, columns.get(column)));
			appendOperator = true;
		}
		this.sql(select.toStatementString());
		this.resultClass(identifier.getClazz());
		this.setParameters(params.toArray(new NamedParameter[] {}));
		Object objectToRefresh = null;
		if (identifier.isOnlyRefreshOwner()) {
			objectToRefresh = identifier.getOwner();
		}
		List<T> resultList = getResultListByEntityHandler(objectToRefresh);
		if ((resultList != null) && (resultList.size() > 0))
			return resultList.get(0);
		else {
			if (identifier.isOnlyRefreshOwner()) {
				throw new SQLQueryException("Objeto não encontrado. Não foi possível realizar o refresh. ");
			}
		}
		return null;
	}

	public T getSingleResult() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (identifier != null) {
			return getSingleResultId();
		} else if (this.getNamedQuery() != null) {
			EntityCache entityCache = session.getEntityCacheManager().getEntityCache(getResultClass());
			DescriptionNamedQuery namedQuery = entityCache.getDescriptionNamedQuery(this.getNamedQuery());
			if (namedQuery == null)
				throw new SQLQueryException("Query nomeada " + this.getNamedQuery() + " não encontrada.");
			this.sql = namedQuery.getQuery();
		}
		/*
		 * Se o usuário setou um Handler específico. Processa o resultSet com o
		 * handler e devolve o objeto.
		 */
		Object result = null;
		if (handler != null) {
			result = getResultObjectByCustomHandler();
		} else {
			if (getResultClass() == null)
				throw new SQLQueryException("Informe a classe para executar a consulta.");
			List<T> resultList = getResultList();
			
			if ((resultList == null) || (resultList.size() == 0))
				throw new SQLQueryNoResultException();

			if (resultList.size() > 1)
				throw new SQLQueryNonUniqueResultException();

			if ((resultList != null) && (resultList.size() > 0))
				result = resultList.get(0);
		}
		return (T) result;

	}

	protected Object getResultObjectByCustomHandler() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		Object result = null;
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		if (this.parameters.size() > 0)
			result = session.getRunner().query(session.getConnection(), sql, handler, parameters.values().toArray(),
					showSql, formatSql, timeOut, session.getListeners(), session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().query(session.getConnection(), sql, handler,
					namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());
		else
			result = session.getRunner().query(session.getConnection(), sql, handler, showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());

		return result;
	}

	protected SQLSessionResult getResultObjectAndResultSetByCustomHandler() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		SQLSessionResult result = null;
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));

		if (this.parameters.size() > 0)
			result = session.getRunner().queryWithResultSet(session.getConnection(), sql, handler,
					parameters.values().toArray(), showSql, formatSql, timeOut, session.getListeners(),
					session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().queryWithResultSet(session.getConnection(), sql, handler,
					namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());
		else
			result = session.getRunner().queryWithResultSet(session.getConnection(), sql, handler,
					new NamedParameterParserResult[] {}, showSql, formatSql, timeOut, session.getListeners(),
					session.clientId());
		return result;
	}

	public ResultSet executeQuery() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		ResultSet result = null;
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		if (this.parameters.size() > 0)
			result = session.getRunner().executeQuery(session.getConnection(), sql, parameters.values().toArray(),
					showSql, formatSql, timeOut, session.getListeners(), session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().executeQuery(session.getConnection(), sql,
					namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());
		else
			result = session.getRunner().executeQuery(session.getConnection(), sql, showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());
		return result;
	}

	public Object loadData(EntityCache entityCacheTarget, Object owner, DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws IllegalAccessException, Exception {
		Object result = null;
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));

		StringBuilder sb = new StringBuilder("");
		boolean keyIsNull = false;
		for (String key : columnKeyTarget.keySet()) {
			if (columnKeyTarget.get(key) == null)
				keyIsNull = true;
			else
				keyIsNull = false;
			if (!"".equals(sb.toString())) {
				sb.append("_");
			}
			sb.append(columnKeyTarget.get(key));
		}
		if (keyIsNull)
			return result;
		String uniqueId = sb.toString();

		/*
		 * Localiza o objeto no Cache se encontrar seta o objeto no field
		 */
		if (descriptionFieldOwner.hasDescriptionColumn() && !descriptionFieldOwner.isElementCollection()
				&& !descriptionFieldOwner.isJoinTable())
			result = getObjectFromCache(entityCacheTarget, uniqueId, transactionCache);

		/*
		 * Senão encontrar o objeto no entityCache executa a estratégia
		 * configurada e seta o resultado do sql no field
		 */
		if (result == null) {
			if (descriptionFieldOwner.isLob()) {
				result = getResultToLob(owner, descriptionFieldOwner, columnKeyTarget);
			} else if (FetchMode.ONE_TO_MANY == descriptionFieldOwner.getModeType())
				result = getResultFromMappedBy(descriptionFieldOwner, columnKeyTarget, transactionCache);
			else if (FetchMode.FOREIGN_KEY == descriptionFieldOwner.getModeType())
				result = getResultFromForeignKey(entityCacheTarget, descriptionFieldOwner, columnKeyTarget,
						transactionCache);
			else if (FetchMode.SELECT == descriptionFieldOwner.getModeType())
				result = getResultFromSelect(owner, descriptionFieldOwner, transactionCache, result);
			else if (FetchMode.ELEMENT_COLLECTION == descriptionFieldOwner.getModeType())
				result = getResultFromElementCollection(descriptionFieldOwner, columnKeyTarget, result);
			else if (FetchMode.MANY_TO_MANY == descriptionFieldOwner.getModeType())
				result = getResultFromJoinTable(descriptionFieldOwner, columnKeyTarget, transactionCache);

		}

		/*
		 * Se localizou um objeto ou lista seta no field
		 */
		if (result != null) {
			/*
			 * Se o objeto result for uma lista
			 */
			if (result instanceof Collection) {
				/*
				 * Se o tipo da lista no field do objeto implementa a interface
				 * Set cria um SQLHashSet
				 */
				if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), Set.class)) {
					Object newValue = new DefaultSQLSet();
					((DefaultSQLSet) newValue).addAll((List) result);
					result = newValue;
				} else if (ReflectionUtils
						.isImplementsInterface(descriptionFieldOwner.getField().getType(), List.class)) {
					/*
					 * Se o tipo da lista no field do objeto implementa List
					 * cria um SQLArrayList
					 */
					Object newValue = new DefaultSQLList();
					((DefaultSQLList) newValue).addAll((List) result);
					result = newValue;
				}
			} else if (result instanceof Map) {
				/**
				 * Se o tipo do field do Objeto é um Map
				 */
				Map newValue = new DefaultSQLMap();
				newValue.putAll((Map) result);
				result = newValue;

			} else {
				if (!(descriptionFieldOwner.isLob())) {
					/*
					 * Se result for um objeto diferente de lista e não for um
					 * LOB
					 */
					EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(result);

					/*
					 * Caso o objeto possa ser gerenciado(objeto completo ou
					 * parcial que tenha sido buscado id no sql) adiciona o
					 * objeto no cache
					 */
					if (entityManaged != null)
						transactionCache.put(
								entityManaged.getEntityCache().getEntityClass().getName() + "_" + uniqueId, result);
				}
			}
		} else {
			if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), Set.class))
				result = new DefaultSQLSet();
			else if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), List.class))
				result = new DefaultSQLList();
			else if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), Map.class))
				result = new DefaultSQLMap();
		}
		return result;
	}

	private Object getResultToLob(Object owner, DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget) throws Exception {
		EntityCache entityCache = descriptionFieldOwner.getEntityCache();
		Select select = new Select(session.getDialect());
		select.addTableName(entityCache.getTableName() + " " + entityCache.getAliasTableName());
		select.addColumn(entityCache.getAliasTableName() + "."
				+ descriptionFieldOwner.getSimpleColumn().getColumnName());

		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean appendOperator = false;
		for (DescriptionColumn descriptionColumn : entityCache.getPrimaryKeyColumns()) {
			if (appendOperator)
				select.and();
			select.addCondition(descriptionColumn.getColumnName(), "=", ":P" + descriptionColumn.getColumnName());
			String columnName = (descriptionColumn.getReferencedColumnName() == null
					|| "".equals(descriptionColumn.getReferencedColumnName()) ? descriptionColumn.getColumnName()
					: descriptionColumn.getReferencedColumnName());
			params.add(new NamedParameter("P" + columnName, columnKeyTarget.get(columnName)));
			appendOperator = true;
		}

		ResultSet resultSet = session.createQuery(select.toStatementString())
				.setParameters(params.toArray(new NamedParameter[] {})).executeQuery();
		if (resultSet.next()) {
			Object object = resultSet.getObject(1);
			if (descriptionFieldOwner.getFieldClass().equals(java.sql.Blob.class)) {
				byte[] bytes = (byte[]) ObjectUtils.convert(object, byte[].class);
				return new AnterosBlob(bytes);
			} else if (descriptionFieldOwner.getFieldClass().equals(java.sql.Clob.class)) {
				String value = (String) ObjectUtils.convert(object, String.class);
				return new AnterosClob(value);
			} else if (descriptionFieldOwner.getFieldClass().equals(java.sql.NClob.class)) {
				String value = (String) ObjectUtils.convert(object, String.class);
				return new AnterosClob(value);
			}
		}

		return null;
	}

	private Object getResultFromJoinTable(final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws Exception {
		Object result;
		EntityCache targetEntityCache = descriptionFieldOwner.getTargetEntity();
		EntityCache fromEntityCache = session.getEntityCacheManager().getEntityCache(
				descriptionFieldOwner.getField().getDeclaringClass());

		String sql = descriptionFieldOwner.getStatement();
		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();

		/*
		 * Se o SQL não foi configurado no statement do field cria o select
		 */
		if (StringUtils.isEmpty(sql)) {
			String sqlKey = "JOIN_TABLE_" + descriptionFieldOwner.getEntityCache().getEntityClass().getName() + "_"
					+ descriptionFieldOwner.getField().getName();
			sql = (String) PersistenceMetadataCache.getInstance().get(sqlKey);

			if (StringUtils.isEmpty(sql)) {
				/*
				 * Adiciona todas colunas da Entidade alvo
				 */
				Select select = new Select(session.getDialect());

				select.addTableName(targetEntityCache.getTableName() + " " + targetEntityCache.getAliasTableName());
				select.addTableName(descriptionFieldOwner.getTableName() + " "
						+ descriptionFieldOwner.getAliasTableName());

				boolean appendOperator = false;

				for (DescriptionColumn column : targetEntityCache.getDescriptionColumns())
					select.addColumn(targetEntityCache.getAliasTableName() + "." + column.getColumnName());

				/*
				 * Monta cláusula WHERE
				 */
				for (DescriptionColumn column : descriptionFieldOwner.getPrimaryKeys()) {
					if (!column.isInversedJoinColumn()) {
						if (appendOperator)
							select.and();
						select.addCondition(descriptionFieldOwner.getAliasTableName() + "." + column.getColumnName(),
								"=", ":P" + column.getColumnName());
						params.add(new NamedParameter("P" + column.getColumnName(), columnKeyTarget.get(column
								.getColumnName())));

						appendOperator = true;
					}
				}

				/*
				 * Adiciona no WHERE colunas da entidade de Destino
				 */
				DescriptionColumn referencedColumn;
				for (DescriptionColumn column : targetEntityCache.getPrimaryKeyColumns()) {
					if (appendOperator)
						select.and();
					referencedColumn = descriptionFieldOwner.getDescriptionColumnByReferencedColumnName(column
							.getColumnName());
					select.addWhereToken(targetEntityCache.getAliasTableName() + "." + column.getColumnName() + " = "
							+ descriptionFieldOwner.getAliasTableName() + "." + referencedColumn.getColumnName());

					appendOperator = true;
				}

				/*
				 * Se possuir @Order, adiciona SELECT
				 */
				if (descriptionFieldOwner.hasOrderByClause()) {
					select.setOrderByClause(descriptionFieldOwner.getOrderByClause());
				}
				sql = select.toStatementString();

				PersistenceMetadataCache.getInstance().put(sqlKey, sql);
			} else {
				for (DescriptionColumn column : descriptionFieldOwner.getPrimaryKeys()) {
					if (!column.isInversedJoinColumn()) {
						params.add(new NamedParameter("P" + column.getColumnName(), columnKeyTarget.get(column
								.getColumnName())));
					}
				}
			}
		} else {
			NamedParameterParserResult parserResult = (NamedParameterParserResult) PersistenceMetadataCache
					.getInstance().get("NamedParameters:" + sql);
			if (parserResult == null) {
				parserResult = NamedParameterStatement.parse(sql, null);
				PersistenceMetadataCache.getInstance().put("NamedParameters:" + sql, parserResult);
			}
			for (NamedParameter parameter : parserResult.getNamedParameters()) {
				Object value = columnKeyTarget.get(parameter.getName());
				if (value == null) {
					throw new SQLException(
							"O parâmetro "
									+ parameter.getName()
									+ " informado no sql do campo "
									+ descriptionFieldOwner.getField().getName()
									+ " da classe "
									+ descriptionFieldOwner.getEntityCache().getEntityClass()
									+ " não corresponde a nenhuma uma coluna do objeto. Use apenas parâmetros com os nomes das colunas do objeto. ");
				}
				parameter.setValue(value);
				params.add(parameter);
			}
		}

		result = getResultListToLoadData(sql, params.toArray(new NamedParameter[] {}), descriptionFieldOwner
				.getTargetEntity().getEntityClass(), transactionCache);
		return result;
	}

	private Object getResultFromElementCollection(final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Object result) throws Exception {
		/*
		 * Se for um ELEMENT_COLLECTION
		 */
		String sql = null;
		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
		EntityCache mappedByEntityCache = descriptionFieldOwner.getTargetEntity();
		if (descriptionFieldOwner.getFieldType() == FieldType.COLLECTION_TABLE) {
			String sqlKey = "COLLECTION_TABLE_" + descriptionFieldOwner.getEntityCache().getEntityClass().getName()
					+ "_" + descriptionFieldOwner.getField().getName();
			sql = (String) PersistenceMetadataCache.getInstance().get(sqlKey);
			if (StringUtils.isEmpty(sql)) {
				Select select = new Select(session.getDialect());
				select.addTableName(descriptionFieldOwner.getTableName());
				boolean appendOperator = false;

				for (DescriptionColumn descriptionColumn : mappedByEntityCache.getPrimaryKeyColumns()) {
					if (appendOperator)
						select.and();
					select.addCondition(descriptionColumn.getColumnName(), "=",
							":P" + descriptionColumn.getColumnName());
					String columnName = (descriptionColumn.getReferencedColumnName() == null
							|| "".equals(descriptionColumn.getReferencedColumnName()) ? descriptionColumn
							.getColumnName() : descriptionColumn.getReferencedColumnName());
					params.add(new NamedParameter("P" + columnName, columnKeyTarget.get(columnName)));
					appendOperator = true;
				}
				if (descriptionFieldOwner.hasOrderByClause())
					select.setOrderByClause(descriptionFieldOwner.getOrderByClause());

				sql = select.toStatementString();
				PersistenceMetadataCache.getInstance().put(sqlKey, sql);
			} else {
				for (DescriptionColumn descriptionColumn : mappedByEntityCache.getPrimaryKeyColumns()) {
					String columnName = (descriptionColumn.getReferencedColumnName() == null
							|| "".equals(descriptionColumn.getReferencedColumnName()) ? descriptionColumn
							.getColumnName() : descriptionColumn.getReferencedColumnName());
					params.add(new NamedParameter("P" + columnName, columnKeyTarget.get(columnName)));
				}
			}

			result = session.createQuery(sql).setParameters(params.toArray(new NamedParameter[] {}))
					.resultSetHandler(new ElementCollectionHandler(descriptionFieldOwner)).getSingleResult();

		} else if (descriptionFieldOwner.getFieldType() == FieldType.COLLECTION_MAP_TABLE) {
			String sqlKey = "COLLECTION_MAP_TABLE" + descriptionFieldOwner.getEntityCache().getEntityClass().getName()
					+ "_" + descriptionFieldOwner.getField().getName();
			sql = (String) PersistenceMetadataCache.getInstance().get(sqlKey);
			if (StringUtils.isEmpty(sql)) {
				Select select = new Select(session.getDialect());
				select.addTableName(descriptionFieldOwner.getTableName());
				boolean appendOperator = false;
				for (DescriptionColumn descriptionColumn : descriptionFieldOwner.getPrimaryKeys()) {
					if (descriptionColumn.isForeignKey()) {
						if (appendOperator)
							select.and();
						select.addCondition(descriptionColumn.getColumnName(), "=",
								":P" + descriptionColumn.getColumnName());
						params.add(new NamedParameter("P" + descriptionColumn.getReferencedColumnName(),
								columnKeyTarget.get(descriptionColumn.getReferencedColumnName())));
						appendOperator = true;
					}
				}
				if (descriptionFieldOwner.hasOrderByClause())
					select.setOrderByClause(descriptionFieldOwner.getOrderByClause());
				sql = select.toStatementString();
				PersistenceMetadataCache.getInstance().put(sqlKey, sql);
			} else {
				for (DescriptionColumn descriptionColumn : descriptionFieldOwner.getPrimaryKeys()) {
					params.add(new NamedParameter("P" + descriptionColumn.getReferencedColumnName(), columnKeyTarget
							.get(descriptionColumn.getReferencedColumnName())));
				}
			}

			result = session.createQuery(sql).setParameters(params.toArray(new NamedParameter[] {}))
					.resultSetHandler(new ElementMapHandler(descriptionFieldOwner)).getSingleResult();

		}
		return result;
	}

	private Object getResultFromSelect(Object owner, final DescriptionField descFieldOwner, Cache transactionCache,
			Object result) throws IllegalAccessException, InvocationTargetException, Exception {
		/*
		 * Pega o SQL
		 */
		StringBuilder select = new StringBuilder("");
		select.append(descFieldOwner.getStatement());
		/*
		 * Faz o parse dos parâmetros x fields do objeto atual setando os
		 * valores
		 */
		List<NamedParameter> lstParams = new ArrayList<NamedParameter>();
		NamedParameterParserResult namedParameterParseResult = (NamedParameterParserResult) PersistenceMetadataCache
				.getInstance().get(select.toString());
		if (namedParameterParseResult == null) {
			namedParameterParseResult = NamedParameterStatement.parse(select.toString(), null);
			PersistenceMetadataCache.getInstance().put(select.toString(), namedParameterParseResult);
		}
		for (String keySel : namedParameterParseResult.getParsedParams().keySet()) {
			Object value = ReflectionUtils.getFieldValueByName(owner, keySel);
			if (value != null)
				lstParams.add(new NamedParameter(keySel, value));
		}
		/*
		 * Se o resultado exigido for do tipo SIMPLE seleciona os dados pelo
		 * método selectOneToLazyLoad
		 */
		if (FieldType.SIMPLE.equals(descFieldOwner.getFieldType())) {
			result = getResultOneToLazyLoad(namedParameterParseResult.getParsedSql(), lstParams.toArray(),
					descFieldOwner.getTargetEntity().getEntityClass(), transactionCache);
		} else if (FieldType.SIMPLE == descFieldOwner.getFieldType()) {
			/*
			 * Se o resultado exigido for do tipo COLLECTION seleciona os dados
			 * pelo método selectListToLazyLoad
			 */
			result = this.getResultListToLazyLoad(namedParameterParseResult.getParsedSql(), lstParams.toArray(),
					descFieldOwner.getTargetEntity().getEntityClass(), transactionCache);
		}
		return result;
	}

	private Object getResultFromForeignKey(EntityCache targetEntityCache, final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws Exception {
		for (Object value : columnKeyTarget.values()) {
			if (value == null)
				return null;
		}
		Object result;
		/*
		 * Monta o SQL
		 */
		String sql = descriptionFieldOwner.getStatement();
		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();

		if (StringUtils.isEmpty(sql)) {
			String sqlKey = "FOREIGN_KEY_" + targetEntityCache.getEntityClass().getName() + "_"
					+ descriptionFieldOwner.getField().getName();
			sql = (String) PersistenceMetadataCache.getInstance().get(sqlKey);

			if (StringUtils.isEmpty(sql)) {
				Select select = new Select(session.getDialect());
				select.addTableName(targetEntityCache.getTableName());
				String tempWhere = "";
				boolean appendOperator = false;
				for (DescriptionColumn column : targetEntityCache.getPrimaryKeyColumns()) {
					if (appendOperator)
						select.and();
					select.addCondition(column.getColumnName(), "=", ":P" + column.getColumnName());
					params.add(new NamedParameter("P" + column.getColumnName(), columnKeyTarget.get(column
							.getColumnName())));
					appendOperator = true;
				}
				if (descriptionFieldOwner.hasOrderByClause())
					select.setOrderByClause(descriptionFieldOwner.getOrderByClause());
				sql = select.toStatementString();
				PersistenceMetadataCache.getInstance().put(sqlKey, sql);
			} else {
				for (DescriptionColumn column : targetEntityCache.getPrimaryKeyColumns()) {
					params.add(new NamedParameter("P" + column.getColumnName(), columnKeyTarget.get(column
							.getColumnName())));
				}
			}
		} else {
			NamedParameterParserResult parserResult = (NamedParameterParserResult) PersistenceMetadataCache
					.getInstance().get("NamedParameters:" + sql);
			if (parserResult == null) {
				parserResult = NamedParameterStatement.parse(sql, null);
				PersistenceMetadataCache.getInstance().put("NamedParameters:" + sql, parserResult);
			}
			for (NamedParameter parameter : parserResult.getNamedParameters()) {
				Object value = columnKeyTarget.get(parameter.getName());
				if (value == null) {
					throw new SQLException(
							"O parâmetro "
									+ parameter.getName()
									+ " informado no sql do campo "
									+ descriptionFieldOwner.getField().getName()
									+ " da classe "
									+ descriptionFieldOwner.getEntityCache().getEntityClass()
									+ " não corresponde a nenhuma uma coluna do objeto. Use apenas parâmetros com os nomes das colunas do objeto. ");
				}
				parameter.setValue(value);
				params.add(parameter);
			}
		}

		/*
		 * Seleciona os dados
		 */
		result = getResultOneToLazyLoad(sql, params.toArray(new NamedParameter[] {}), descriptionFieldOwner
				.getTargetEntity().getEntityClass(), transactionCache);
		return result;
	}

	public Object getResultOneToLazyLoad(String sql, NamedParameter[] namedParameter, Class<?> resultClass,
			Cache transactionCache) throws Exception {
		List result = getResultListToLoadData(sql, namedParameter, resultClass, transactionCache);
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	public Object getResultOneToLazyLoad(String sql, Object[] parameter, Class<?> resultClass, Cache transactionCache)
			throws Exception {
		List result = getResultListToLazyLoad(sql, parameter, resultClass, transactionCache);
		if (result != null)
			return result.get(FIRST_RECORD);
		return null;
	}

	protected Object getObjectFromCache(EntityCache targetEntityCache, String uniqueId, Cache transactionCache) {
		Object result = null;
		if (transactionCache != null) {

			/*
			 * Se a classe for abstrata pega todas as implementações não
			 * abstratas e verifica se existe um objeto da classe + ID no
			 * entityCache
			 */
			if (ReflectionUtils.isAbstractClass(targetEntityCache.getEntityClass())) {
				EntityCache[] entitiesCache = session.getEntityCacheManager().getEntitiesBySuperClassIncluding(
						targetEntityCache);
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
		}
		return result;
	}

	private Object getResultFromMappedBy(final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws Exception {
		Object result;
		/*
		 * Pega o field pelo nome do mappedBy na classe do field atual
		 */
		Field mappedByField = ReflectionUtils.getFieldByName(descriptionFieldOwner.getTargetEntity().getEntityClass(),
				descriptionFieldOwner.getMappedBy());
		/*
		 * Pega a EntityCache da classe e descriptionColumn
		 */
		EntityCache mappedByEntityCache = descriptionFieldOwner.getTargetEntity();
		/*
		 * Pega o(s) DescriptionColumn(s) da coluna para pegar o ColumnName que
		 * será usado no sql
		 */
		DescriptionColumn[] mappedByDescriptionColumn = mappedByEntityCache.getDescriptionColumns(mappedByField
				.getName());
		/*
		 * Monta o SQL
		 */
		String sql = descriptionFieldOwner.getStatement();
		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();

		if (StringUtils.isEmpty(sql)) {
			String sqlKey = "MAPPED_BY_" + descriptionFieldOwner.getEntityCache().getEntityClass().getName() + "_"
					+ descriptionFieldOwner.getField().getName();
			sql = (String) PersistenceMetadataCache.getInstance().get(sqlKey);
			if (StringUtils.isEmpty(sql)) {
				Select select = new Select(session.getDialect());
				select.addTableName(mappedByEntityCache.getTableName(), "TAB");

				boolean appendOperator = false;
				if (mappedByDescriptionColumn != null) {
					for (DescriptionColumn descriptionColumn : mappedByDescriptionColumn) {
						if (appendOperator)
							select.and();
						select.addCondition("TAB." + descriptionColumn.getColumnName(), "=",
								":P" + descriptionColumn.getColumnName());
						params.add(new NamedParameter("P" + descriptionColumn.getColumnName(), columnKeyTarget
								.get(descriptionColumn.getReferencedColumnName())));
						appendOperator = true;
					}
				}
				if (descriptionFieldOwner.hasOrderByClause())
					select.setOrderByClause(descriptionFieldOwner.getOrderByClause());
				sql = select.toStatementString();
				PersistenceMetadataCache.getInstance().put(sqlKey, sql);
			} else {
				if (mappedByDescriptionColumn != null) {
					for (DescriptionColumn descriptionColumn : mappedByDescriptionColumn) {
						params.add(new NamedParameter("P" + descriptionColumn.getColumnName(), columnKeyTarget
								.get(descriptionColumn.getReferencedColumnName())));
					}
				}
			}
		} else {
			NamedParameterParserResult parserResult = (NamedParameterParserResult) PersistenceMetadataCache
					.getInstance().get("NamedParameters:" + sql);
			if (parserResult == null) {
				parserResult = NamedParameterStatement.parse(sql, null);
				PersistenceMetadataCache.getInstance().put("NamedParameters:" + sql, parserResult);
			}
			for (NamedParameter parameter : parserResult.getNamedParameters()) {
				Object value = columnKeyTarget.get(parameter.getName());
				if (value == null) {
					throw new SQLException(
							"O parâmetro "
									+ parameter.getName()
									+ " informado no sql do campo "
									+ descriptionFieldOwner.getField().getName()
									+ " da classe "
									+ descriptionFieldOwner.getEntityCache().getEntityClass()
									+ " não corresponde a nenhuma uma coluna do objeto. Use apenas parâmetros com os nomes das colunas do objeto. ");
				}
				parameter.setValue(value);
				params.add(parameter);
			}
		}
		/*
		 * Seleciona os dados
		 */
		result = getResultListToLoadData(sql, params.toArray(new NamedParameter[] {}), descriptionFieldOwner
				.getTargetEntity().getEntityClass(), transactionCache);
		return result;
	}

	private <T> List<T> getResultListToLoadData(String sql, NamedParameter[] namedParameter, Class<?> resultClass,
			Cache transactionCache) throws Exception {

		ResultSetHandler handler;
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);

		if (entityCache == null)
			handler = new BeanHandler(resultClass);
		else {
			if (sql.toLowerCase().indexOf(" " + entityCache.getTableName().toLowerCase()) < 0) {
				throw new SQLException("A tabela " + entityCache.getTableName() + " da classe " + resultClass.getName()
						+ " não foi localizada no SQL informado. Não será possível executar a consulta.");
			}

			SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance()
					.get(resultClass.getName()+":"+sql);
			if (analyzerResult == null) {
				analyzerResult = new SQLQueryAnalyzer(session.getEntityCacheManager(), session.getDialect()).analyze(sql, resultClass);
				PersistenceMetadataCache.getInstance().put(resultClass.getName()+":"+sql, analyzerResult);
			}

			handler = session
					.createNewEntityHandler(resultClass, analyzerResult.getExpressions(),
							analyzerResult.getColumnAliases(), transactionCache, false, null, firstResult, maxResults,
							readOnly);
			sql = analyzerResult.getParsedSql();
		}

		List result = (List) session.getRunner().query(session.getConnection(), sql, handler, namedParameter, showSql,
				formatSql, 0, session.getListeners(), session.clientId());

		if (handler!=null)
			amountOfInstantiatedObjects=handler.getAmountOfInstantiatedObjects();
		
		if (result == null)
			return Collections.EMPTY_LIST;

		return result;
	}

	public <T> List<T> getResultListToLazyLoad(String sql, Object[] parameter, Class<?> resultClass,
			Cache transactionCache) throws Exception {
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
		ResultSetHandler handler;

		List result = null;
		try {
			if (entityCache == null)
				handler = new BeanHandler(resultClass);
			else {
				if (sql.toLowerCase().indexOf(" " + entityCache.getTableName().toLowerCase()) < 0) {
					throw new SQLException("A tabela " + entityCache.getTableName() + " da classe "
							+ resultClass.getName()
							+ " não foi localizada no SQL informado. Não será possível executar a consulta.");
				}

				SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance()
						.get(getResultClass().getName()+":"+sql);
				if (analyzerResult == null) {
					analyzerResult = new SQLQueryAnalyzer(session.getEntityCacheManager(), session.getDialect()).analyze(sql, resultClass);
					PersistenceMetadataCache.getInstance().put(resultClass.getName()+":"+sql, analyzerResult);
				}
				sql = analyzerResult.getParsedSql();
				handler = session.createNewEntityHandler(resultClass, analyzerResult.getExpressions(),
						analyzerResult.getColumnAliases(), transactionCache, false, null, firstResult, maxResults,
						readOnly);
			}

			result = (List) session.getRunner().query(session.getConnection(), sql, handler, parameter, showSql,
					formatSql, 0, session.getListeners(), session.clientId());
			
			if (handler!=null)
				amountOfInstantiatedObjects=handler.getAmountOfInstantiatedObjects();
		} finally {
		}

		if (result == null)
			return Collections.EMPTY_LIST;

		return result;
	}

	public TypedSQLQuery<T> setClob(int parameterIndex, InputStream inputStream) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, inputStream);
		return this;
	}

	public TypedSQLQuery<T> setClob(int parameterIndex, byte[] bytes) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, bytes);
		return this;
	}

	public TypedSQLQuery<T> setClob(String parameterName, InputStream inputStream) throws Exception {
		set(parameterName, inputStream);
		return this;
	}

	public TypedSQLQuery<T> setClob(String parameterName, byte[] bytes) throws Exception {
		set(parameterName, bytes);
		return this;
	}

	public TypedSQLQuery<T> timeOut(int seconds) {
		this.timeOut = seconds;
		return this;
	}

	public TypedSQLQuery<T> allowDuplicateObjects(boolean allowDuplicateObjects) {
		this.allowDuplicateObjects = allowDuplicateObjects;
		return this;
	}

	public TypedSQLQuery<T> namedQuery(String name) {
		this.setNamedQuery(name);
		return this;
	}

	public SQLSessionResult getResultListAndResultSet() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		/*
		 * Se o usuário setou um Handler específico. Processa o resultSet com o
		 * handler e devolve o objeto em forma de lista caso o resultado já não
		 * seja uma lista
		 */
		SQLSessionResult result = null;
		if (handler != null) {
			result = getResultObjectAndResultSetByCustomHandler();
		} else {
			/*
			 * Processa o resultSet usando o EntityHandler para criar os objetos
			 */
			result = getResultObjectAndResultSetByEntityHandler();
		}
		return result;
	}

	protected SQLSessionResult getResultObjectAndResultSetByEntityHandler() throws Exception,
			SQLQueryAnalyzerException, SQLException {
		SQLSessionResult result;
		if (getResultClass() == null)
			throw new SQLQueryException("Informe a classe para executar a consulta.");

		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(getResultClass());
		ResultSetHandler handler;

		SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance()
				.get(getResultClass().getName()+":"+sql);
		if (analyzerResult == null) {
			analyzerResult = new SQLQueryAnalyzer(session.getEntityCacheManager(), session.getDialect()).analyze(sql, getResultClass());
			PersistenceMetadataCache.getInstance().put(getResultClass().getName()+":"+sql, analyzerResult);
		}

		SQLCache transactionCache = new SQLCache(DEFAULT_CACHE_SIZE);
		try {
			if (entityCache == null)
				handler = new BeanHandler(getResultClass());
			else {
				if (sql.toLowerCase().indexOf(" " + entityCache.getTableName().toLowerCase()) < 0) {
					throw new SQLException("A tabela " + entityCache.getTableName() + " da classe "
							+ getResultClass().getName()
							+ " não foi localizada no SQL informado. Não será possível executar a consulta. SQL-> "
							+ sql);
				}
				handler = session.createNewEntityHandler(getResultClass(), analyzerResult.getExpressions(),
						analyzerResult.getColumnAliases(), transactionCache, allowDuplicateObjects, null, firstResult,
						maxResults, readOnly);
			}

			if (this.parameters.size() > 0)
				result = session.getRunner().queryWithResultSet(session.getConnection(), analyzerResult.getParsedSql(),
						handler, parameters.values().toArray(), showSql, formatSql, timeOut, session.getListeners(),
						session.clientId());
			else if (this.namedParameters.size() > 0)
				result = session.getRunner().queryWithResultSet(session.getConnection(), analyzerResult.getParsedSql(),
						handler, namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql,
						timeOut, session.getListeners(), session.clientId());
			else
				result = session.getRunner().queryWithResultSet(session.getConnection(), analyzerResult.getParsedSql(),
						handler, new NamedParameterParserResult[] {}, showSql, formatSql, timeOut,
						session.getListeners(), session.clientId());

			if (handler!=null)
				amountOfInstantiatedObjects = handler.getAmountOfInstantiatedObjects();
		} finally {
			transactionCache.clear();
			session.getPersistenceContext().clearCache();
		}
		return result;
	}

	public TypedSQLQuery<T> setLockMode(LockModeType lockMode) {
		this.lockMode = lockMode;
		return this;
	}

	public LockModeType getLockMode() {
		return lockMode;
	}

	@Override
	public TypedSQLQuery<T> setParameters(Object parameters) throws Exception {
		if (parameters == null)
			return this;

		if (parameters instanceof NamedParameter[]) {
			setParameters((NamedParameter[]) parameters);
		} else if (parameters instanceof Map) {
			setParameters((Map<String, Object>) parameters);
		} else if (parameters instanceof Object[]) {
			setParameters((Object[]) parameters);
		} else if (parameters instanceof NamedParameter) {
			setParameters(new NamedParameter[] { (NamedParameter) parameters });
		} else if (parameters instanceof NamedParameterList) {
			setParameters(((NamedParameterList) parameters).values());
		} else
			throw new SQLQueryException("Formato para setParameters inválido. Use NamedParameter[], Map ou Object[].");

		return this;
	}

	public String getNamedQuery() {
		return namedQuery;
	}

	public void setNamedQuery(String namedQuery) {
		this.namedQuery = namedQuery;
	}

	public String getSql() {
		return sql;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public void setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
	}

	@Override
	public void refresh(Object entity) throws Exception {
		session.refresh(entity);
	}

	@Override
	public TypedSQLQuery<T> setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	@Override
	public TypedSQLQuery<T> setFirstResult(int firstResult) {
		this.firstResult = firstResult;
		return this;
	}

	@Override
	public TypedSQLQuery setReadOnly(boolean readOnlyObjects) {
		this.readOnly = readOnlyObjects;
		return this;
	}

	@Override
	public Object getOutputParameterValue(int position) {
		throw new SQLQueryException("Método somente usado em Procedimentos/Funções.");
	}

	@Override
	public Object getOutputParameterValue(String parameterName) {
		throw new SQLQueryException("Método somente usado em Procedimentos/Funções.");
	}

	@Override
	public ProcedureResult execute() throws Exception {
		throw new SQLQueryException("Método somente usado em Procedimentos/Funções.");
	}

	@Override
	public TypedSQLQuery<T> procedureOrFunctionName(String procedureName) {
		throw new SQLQueryException("Método somente usado em Procedimentos/Funções.");
	}

	@Override
	public TypedSQLQuery<T> namedStoredProcedureQuery(String name) {
		throw new SQLQueryException("Método somente usado em Procedimentos/Funções.");
	}

	@Override
	public int getAmountOfInstantiatedObjects() {
		return amountOfInstantiatedObjects;
	}

}
