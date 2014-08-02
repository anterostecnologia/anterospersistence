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

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.BeanHandler;
import br.com.anteros.persistence.handler.ElementCollectionHandler;
import br.com.anteros.persistence.handler.ElementMapHandler;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.descriptor.DescriptionNamedQuery;
import br.com.anteros.persistence.metadata.descriptor.type.FieldType;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.parameter.NamedParameterParserResult;
import br.com.anteros.persistence.proxy.collection.SQLArrayList;
import br.com.anteros.persistence.proxy.collection.SQLHashMap;
import br.com.anteros.persistence.proxy.collection.SQLHashSet;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.cache.SQLCache;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.type.LockModeType;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.sql.command.Select;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;
import br.com.anteros.persistence.transaction.impl.TransactionException;
import br.com.anteros.persistence.util.SQLParserUtil;

@SuppressWarnings("all")
public class SQLQueryImpl<T> implements SQLQuery<T> {
	private SQLSession session;
	private Class<?> resultClass;
	private Identifier identifier;
	private boolean showSql;
	private boolean formatSql;
	private ResultSetHandler handler;
	private Map<Integer, NamedParameter> namedParameters = new TreeMap<Integer, NamedParameter>();
	private Map<Integer, Object> parameters = new TreeMap<Integer, Object>();
	public final int DEFAULT_CACHE_SIZE = 1000;
	private String sql;
	public static int FIRST_RECORD = 0;
	private int timeOut = 0;
	private CallableType callableType;
	private String procedureName;
	private String[] outputParametersName = new String[] {};
	private String namedQuery;
	private LockModeType lockMode;

	public SQLQueryImpl(SQLSession session) {
		this.session = session;
	}

	public SQLQuery<T> resultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
		return this;
	}

	public SQLQuery<T> identifier(Identifier<?> identifier) {
		this.identifier = identifier;
		return this;
	}

	public SQLSession getSession() {
		return session;
	}

	public SQLQuery<T> sql(String sql) {
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

		NamedParameterParserResult parse = NamedParameterStatement.parse(sql, null);
		paramCount = 0;
		for (NamedParameter namedParameter : parse.getNamedParameters()) {
			paramCount++;
			namedParameters.put(paramCount, namedParameter);
		}

		return this;
	}

	public SQLQuery<T> showSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}

	public SQLQuery<T> formatSql(boolean formatSql) {
		this.formatSql = formatSql;
		return this;
	}

	public SQLQuery<T> resultSetHandler(ResultSetHandler handler) {
		this.handler = handler;
		return this;
	}

	public SQLQuery<T> clear() {
		namedParameters.clear();
		sql = "";

		return this;
	}

	public SQLQuery<T> setParameters(NamedParameter[] parameters) throws Exception {
		for (NamedParameter parameter : parameters) {
			for (Integer index : namedParameters.keySet()) {
				NamedParameter np = namedParameters.get(index);
				if (np.getName().equals(parameter.getName())) {
					namedParameters.put(index, parameter);
					break;
				}
			}
		}
		return this;
	}

	public SQLQuery<T> setParameters(Object[] parameters) throws Exception {
		if (parameters.length != this.parameters.size())
			throw new SQLQueryException("Número de parâmetros diferente do número encontrado na instrução SQL.");
		for (int i = 0; i < parameters.length; i++)
			this.parameters.put(i + 1, parameters[i]);
		return this;
	}

	public SQLQuery<T> setParameters(Map<String, Object> parameters) throws Exception {
		if (parameters.size() != this.parameters.size())
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

	public SQLQuery<T> setInteger(int parameterIndex, int value) throws Exception {
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

	public SQLQuery<T> setString(int parameterIndex, String value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setLong(int parameterIndex, long value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setNull(int parameterIndex) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, null);
		return this;
	}

	public SQLQuery<T> setDate(int parameterIndex, Date value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setDateTime(int parameterIndex, Date value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setObject(int parameterIndex, Object object) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, object);
		return this;
	}

	public SQLQuery<T> setBlob(int parameterIndex, InputStream inputStream) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, inputStream);
		return this;
	}

	public SQLQuery<T> setBlob(int parameterIndex, byte[] bytes) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, bytes);
		return this;
	}

	public SQLQuery<T> setBoolean(int parameterIndex, boolean value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setDouble(int parameterIndex, double value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setFloat(int parameterIndex, float value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setBigDecimal(int parameterIndex, BigDecimal value) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, value);
		return this;
	}

	public SQLQuery<T> setInteger(String parameterName, int value) throws Exception {
		set(parameterName, value);
		return this;
	}

	protected void set(String parameterName, Object value) throws SQLQueryException {
		if (!namedParameters.containsKey(parameterName))
			throw new SQLQueryException("Parâmetro " + parameterName + " não encontrado.");
		for (Integer index : namedParameters.keySet()) {
			NamedParameter np = namedParameters.get(index);
			if (np.getName().equals(parameterName)) {
				np.setValue(value);
				break;
			}
		}
	}

	public SQLQuery<T> setString(String parameterName, String value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setLong(String parameterName, long value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setNull(String parameterName) throws Exception {
		set(parameterName, null);
		return this;
	}

	public SQLQuery<T> setDate(String parameterName, Date value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setDateTime(String parameterName, Date value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setObject(String parameterName, Object object) throws Exception {
		set(parameterName, object);
		return this;
	}

	public SQLQuery<T> setBlob(String parameterName, InputStream inputStream) throws Exception {
		set(parameterName, inputStream);
		return this;
	}

	public SQLQuery<T> setBlob(String parameterName, byte[] bytes) throws Exception {
		set(parameterName, bytes);
		return this;
	}

	public SQLQuery<T> setBoolean(String parameterName, boolean value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setDouble(String parameterName, double value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setFloat(String parameterName, float value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public SQLQuery<T> setBigDecimal(String parameterName, BigDecimal value) throws Exception {
		set(parameterName, value);
		return this;
	}

	public List<T> selectList() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (resultClass == null)
			throw new SQLQueryException("Informe a Classe para executar a consulta.");

		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));

		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
		ResultSetHandler handler;

		SQLQueryAnalyzer analyzer = session.getSQLQueryAnalyzer();
		analyzer.analyze(sql, resultClass);

		SQLCache transactionCache = new SQLCache(DEFAULT_CACHE_SIZE);
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
				handler = session.createNewEntityHandler(resultClass, analyzer.getExpressions(), analyzer.getColumnAliases(), transactionCache);
			}

			if (this.parameters.size() > 0)
				result = (List) session.getRunner().query(session.getConnection(), analyzer.getParsedSQL(), handler,
						parameters.values().toArray(),
						showSql, formatSql, timeOut, session.getListeners(), session.clientId());
			else if (this.namedParameters.size() > 0)
				result = (List) session.getRunner().query(session.getConnection(), analyzer.getParsedSQL(), handler,
						namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql, timeOut,
						session.getListeners(),
						session.clientId());
			else
				result = (List) session.getRunner().query(session.getConnection(), analyzer.getParsedSQL(), handler,
						showSql, formatSql, timeOut,
						session.getListeners(), session.clientId());

		} finally {
			transactionCache.clear();
			session.getPersistenceContext().clearCache();
		}

		if (result == null)
			return Collections.EMPTY_LIST;
		return result;
	}

	public T selectId() throws Exception {
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
		SQLQuery<T> query = session.createSQLQuery(select.toStatementString());
		return query.setParameters(params.toArray(new NamedParameter[] {}))
				.resultClass(identifier.getClazz()).selectOne();
	}

	public T selectOne() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		if (resultClass == null)
			throw new SQLQueryException("Informe a Classe para executar a consulta.");
		List<T> result = selectList();
		if ((result != null) && (result.size() > 0))
			return result.get(0);
		return null;
	}

	public T select() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		Object result = null;
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		if (this.parameters.size() > 0)
			result = session.getRunner().query(session.getConnection(), sql, handler, parameters.values().toArray(),
					showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().query(session.getConnection(), sql, handler,
					namedParameters.values().toArray(new NamedParameter[] {}),
					showSql, formatSql, timeOut, session.getListeners(), session.clientId());
		else
			result = session.getRunner().query(session.getConnection(), sql, handler, showSql, formatSql, timeOut,
					session.getListeners(),
					session.clientId());

		return (T) result;
	}

	public ResultSet executeQuery() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		ResultSet result = null;
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		if (this.parameters.size() > 0)
			result = session.getRunner().executeQuery(session.getConnection(), sql, parameters.values().toArray(),
					showSql, formatSql, timeOut,
					session.getListeners(), session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().executeQuery(session.getConnection(), sql,
					namedParameters.values().toArray(new NamedParameter[] {}),
					showSql, formatSql, timeOut, session.getListeners(), session.clientId());
		else
			result = session.getRunner().executeQuery(session.getConnection(), sql, showSql, formatSql, timeOut,
					session.getListeners(),
					session.clientId());
		return result;
	}

	public Object loadData(EntityCache entityCacheTarget, Object owner, DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget,
			Cache transactionCache) throws IllegalAccessException, Exception {
		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		Object result = null;
		StringBuffer sb = new StringBuffer("");
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
			if (FetchMode.ONE_TO_MANY == descriptionFieldOwner.getModeType())
				result = selectFromMappedBy(descriptionFieldOwner, columnKeyTarget, transactionCache);
			else if (FetchMode.FOREIGN_KEY == descriptionFieldOwner.getModeType())
				result = selectFromForeignKey(entityCacheTarget, descriptionFieldOwner, columnKeyTarget,
						transactionCache);
			else if (FetchMode.SELECT == descriptionFieldOwner.getModeType())
				result = selectFromSelect(owner, descriptionFieldOwner, transactionCache, result);
			else if (FetchMode.ELEMENT_COLLECTION == descriptionFieldOwner.getModeType())
				result = selectFromElementCollection(descriptionFieldOwner, columnKeyTarget, result);
			else if (FetchMode.MANY_TO_MANY == descriptionFieldOwner.getModeType())
				result = selectFromJoinTable(descriptionFieldOwner, columnKeyTarget, transactionCache);

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
					Object newValue = new SQLHashSet();
					((SQLHashSet) newValue).addAll((List) result);
					result = newValue;
				} else if (ReflectionUtils
						.isImplementsInterface(descriptionFieldOwner.getField().getType(), List.class)) {
					/*
					 * Se o tipo da lista no field do objeto implementa List
					 * cria um SQLArrayList
					 */
					Object newValue = new SQLArrayList();
					((SQLArrayList) newValue).addAll((List) result);
					result = newValue;
				}
			} else if (result instanceof Map) {
				/**
				 * Se o tipo do field do Objeto é um Map
				 */
				Map newValue = new SQLHashMap();
				newValue.putAll((Map) result);
				result = newValue;

			} else {
				/*
				 * Se result for um objeto diferente de lista
				 */
				EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(result);

				/*
				 * Caso o objeto possa ser gerenciado(objeto completo ou parcial
				 * que tenha sido buscado id no sql) adiciona o objeto no cache
				 */
				if (entityManaged != null)
					transactionCache.put(entityManaged.getEntityCache().getEntityClass().getName() + "_" + uniqueId,
							result);
			}

		} else {
			if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), Set.class))
				result = new SQLHashSet();
			else if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), List.class))
				result = new SQLArrayList();
			else if (ReflectionUtils.isImplementsInterface(descriptionFieldOwner.getField().getType(), Map.class))
				result = new SQLHashMap();
		}
		return result;
	}

	private Object selectFromJoinTable(final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache)
			throws Exception {
		Object result;
		EntityCache targetEntityCache = descriptionFieldOwner.getTargetEntity();
		EntityCache fromEntityCache = session.getEntityCacheManager().getEntityCache(
				descriptionFieldOwner.getField().getDeclaringClass());

		/*
		 * Adiciona todas colunas da Entidade alvo
		 */
		Select select = new Select(session.getDialect());

		/*
		 * Gera os aliases para as tabelas
		 */
		targetEntityCache.generateAliasTableName();
		fromEntityCache.generateAliasTableName();
		descriptionFieldOwner.generateAliasTableName();

		select.addTableName(fromEntityCache.getTableName() + " " + fromEntityCache.getAliasTableName());
		select.addTableName(targetEntityCache.getTableName() + " " + targetEntityCache.getAliasTableName());
		select.addTableName(descriptionFieldOwner.getTableName() + " " + descriptionFieldOwner.getAliasTableName());

		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
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
				select.addCondition(fromEntityCache.getAliasTableName() + "." + column.getColumnName(), "=", ":P"
						+ column.getColumnName());
				params.add(new NamedParameter("P" + column.getReferencedColumnName(), columnKeyTarget.get(column
						.getReferencedColumnName())));

				appendOperator = true;
			}
		}

		/*
		 * Adiciona no WHERE colunas da entidade de Origem
		 */
		DescriptionColumn referencedColumn;
		for (DescriptionColumn column : fromEntityCache.getPrimaryKeyColumns()) {
			if (appendOperator)
				select.and();
			referencedColumn = descriptionFieldOwner.getDescriptionColumnByReferencedColumnName(column.getColumnName());
			select.addWhereToken(fromEntityCache.getAliasTableName() + "." + column.getColumnName() + " = "
					+ descriptionFieldOwner.getAliasTableName() + "." + referencedColumn.getColumnName());

			appendOperator = true;
		}

		/*
		 * Adiciona no WHERE colunas da entidade de Destino
		 */
		for (DescriptionColumn column : targetEntityCache.getPrimaryKeyColumns()) {
			if (appendOperator)
				select.and();
			referencedColumn = descriptionFieldOwner.getDescriptionColumnByReferencedColumnName(column.getColumnName());
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

		result = selectListToLoadData(select.toStatementString(), params.toArray(new NamedParameter[] {}),
				descriptionFieldOwner.getTargetEntity()
						.getEntityClass(), transactionCache);
		return result;
	}

	private Object selectFromElementCollection(final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Object result)
			throws Exception {
		/*
		 * Se for um ELEMENT_COLLETION
		 */
		if (descriptionFieldOwner.getFieldType() == FieldType.COLLECTION_TABLE) {
			Select select = new Select(session.getDialect());
			select.addTableName(descriptionFieldOwner.getTableName());
			ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
			boolean appendOperator = false;

			EntityCache mappedByEntityCache = descriptionFieldOwner.getTargetEntity();

			for (DescriptionColumn descriptionColumn : mappedByEntityCache.getPrimaryKeyColumns()) {
				if (appendOperator)
					select.and();
				select.addCondition(descriptionColumn.getColumnName(), "=", ":P" + descriptionColumn.getColumnName());
				String columnName = (descriptionColumn.getReferencedColumnName() == null
						|| "".equals(descriptionColumn.getReferencedColumnName()) ? descriptionColumn
						.getColumnName() : descriptionColumn.getReferencedColumnName());
				params.add(new NamedParameter("P" + columnName, columnKeyTarget.get(columnName)));
				appendOperator = true;
			}
			if (descriptionFieldOwner.hasOrderByClause())
				select.setOrderByClause(descriptionFieldOwner.getOrderByClause());

			result = session.createSQLQuery(select.toStatementString())
					.setParameters(params.toArray(new NamedParameter[] {}))
					.resultSetHandler(new ElementCollectionHandler(descriptionFieldOwner)).select();

		} else if (descriptionFieldOwner.getFieldType() == FieldType.COLLECTION_MAP_TABLE) {
			Select select = new Select(session.getDialect());
			select.addTableName(descriptionFieldOwner.getTableName());
			ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
			boolean appendOperator = false;
			for (DescriptionColumn descriptionColumn : descriptionFieldOwner.getPrimaryKeys()) {
				if (descriptionColumn.isForeignKey()) {
					if (appendOperator)
						select.and();
					select.addCondition(descriptionColumn.getColumnName(), "=",
							":P" + descriptionColumn.getColumnName());
					params.add(new NamedParameter("P" + descriptionColumn.getReferencedColumnName(), columnKeyTarget
							.get(descriptionColumn
									.getReferencedColumnName())));
					appendOperator = true;
				}
			}
			if (descriptionFieldOwner.hasOrderByClause())
				select.setOrderByClause(descriptionFieldOwner.getOrderByClause());

			result = session.createSQLQuery(select.toStatementString())
					.setParameters(params.toArray(new NamedParameter[] {}))
					.resultSetHandler(new ElementMapHandler(descriptionFieldOwner)).select();

		}
		return result;
	}

	private Object selectFromSelect(Object owner, final DescriptionField descFieldOwner, Cache transactionCache,
			Object result)
			throws IllegalAccessException, InvocationTargetException, Exception {
		/*
		 * Pega o SQL
		 */
		StringBuffer select = new StringBuffer("");
		select.append(descFieldOwner.getStatement());
		/*
		 * Faz o parse dos parâmetros x fields do objeto atual setando os
		 * valores
		 */
		List<NamedParameter> lstParams = new ArrayList<NamedParameter>();
		NamedParameterParserResult resultParams = NamedParameterStatement.parse(select.toString(), null);
		for (String keySel : resultParams.getParsedParams().keySet()) {
			Object value = ReflectionUtils.getFieldValueByName(owner, keySel);
			if (value != null)
				lstParams.add(new NamedParameter(keySel, value));
		}
		/*
		 * Se o resultado exigido for do tipo SIMPLE seleciona os dados pelo
		 * método selectOneToLazyLoad
		 */
		if (FieldType.SIMPLE.equals(descFieldOwner.getFieldType())) {
			result = selectOneToLazyLoad(select.toString(), lstParams.toArray(), descFieldOwner.getTargetEntity()
					.getEntityClass(), transactionCache);
		} else if (FieldType.SIMPLE == descFieldOwner.getFieldType()) {
			/*
			 * Se o resultado exigido for do tipo COLLECTION seleciona os dados
			 * pelo método selectListToLazyLoad
			 */
			result = this.selectListToLazyLoad(select.toString(), lstParams.toArray(), descFieldOwner.getTargetEntity()
					.getEntityClass(),
					transactionCache);
		}
		return result;
	}

	private Object selectFromForeignKey(EntityCache targetEntityCache, final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws Exception {
		for (Object value : columnKeyTarget.values()) {
			if (value == null)
				return null;
		}
		Object result;
		/*
		 * Monta o SQL
		 */
		Select select = new Select(session.getDialect());
		select.addTableName(targetEntityCache.getTableName());
		String tempWhere = "";
		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean appendOperator = false;
		for (DescriptionColumn column : targetEntityCache.getPrimaryKeyColumns()) {
			if (appendOperator)
				select.and();
			select.addCondition(column.getColumnName(), "=", ":P" + column.getColumnName());
			params.add(new NamedParameter("P" + column.getColumnName(), columnKeyTarget.get(column.getColumnName())));
			appendOperator = true;
		}
		if (descriptionFieldOwner.hasOrderByClause())
			select.setOrderByClause(descriptionFieldOwner.getOrderByClause());

		/*
		 * Seleciona os dados
		 */
		result = selectOneToLazyLoad(select.toStatementString(), params.toArray(new NamedParameter[] {}),
				descriptionFieldOwner.getTargetEntity()
						.getEntityClass(), transactionCache);
		return result;
	}

	public Object selectOneToLazyLoad(String sql, NamedParameter[] namedParameter, Class<?> resultClass,
			Cache transactionCache) throws Exception {
		List result = selectListToLoadData(sql, namedParameter, resultClass, transactionCache);
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	public Object selectOneToLazyLoad(String sql, Object[] parameter, Class<?> resultClass, Cache transactionCache)
			throws Exception {
		List result = selectListToLazyLoad(sql, parameter, resultClass, transactionCache);
		if (result != null)
			return result.get(FIRST_RECORD);
		return null;
	}

	private Object getObjectFromCache(EntityCache targetEntityCache, String uniqueId, Cache transactionCache) {
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

	private Object selectFromMappedBy(final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache)
			throws Exception {
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
		Select select = new Select(session.getDialect());
		select.addTableName(mappedByEntityCache.getTableName());

		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean appendOperator = false;
		if (mappedByDescriptionColumn != null) {
			for (DescriptionColumn descriptionColumn : mappedByDescriptionColumn) {
				if (appendOperator)
					select.and();
				select.addCondition(descriptionColumn.getColumnName(), "=", ":P" + descriptionColumn.getColumnName());
				params.add(new NamedParameter("P" + descriptionColumn.getColumnName(), columnKeyTarget
						.get(descriptionColumn
								.getReferencedColumnName())));
				appendOperator = true;
			}
		}
		if (descriptionFieldOwner.hasOrderByClause())
			select.setOrderByClause(descriptionFieldOwner.getOrderByClause());
		/*
		 * Seleciona os dados
		 */
		result = selectListToLoadData(select.toStatementString(), params.toArray(new NamedParameter[] {}),
				descriptionFieldOwner.getTargetEntity()
						.getEntityClass(), transactionCache);
		return result;
	}

	private <T> List<T> selectListToLoadData(String sql, NamedParameter[] namedParameter, Class<?> resultClass,
			Cache transactionCache)
			throws Exception {

		ResultSetHandler handler;
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);

		if (entityCache == null)
			handler = new BeanHandler(resultClass);
		else {
			if (sql.toLowerCase().indexOf(" " + entityCache.getTableName().toLowerCase()) < 0) {
				throw new SQLException("A tabela " + entityCache.getTableName() + " da classe " + resultClass.getName()
						+ " não foi localizada no SQL informado. Não será possível executar a consulta.");
			}
			SQLQueryAnalyzer analyzer = session.getSQLQueryAnalyzer();
			analyzer.analyze(sql, resultClass);
			handler = session.createNewEntityHandler(resultClass, analyzer.getExpressions(), analyzer.getColumnAliases(), transactionCache);
		}

		List result = (List) session.getRunner().query(session.getConnection(), sql, handler, namedParameter, showSql,
				formatSql, 0,
				session.getListeners(), session.clientId());

		if (result == null)
			return Collections.EMPTY_LIST;

		return result;
	}

	public <T> List<T> selectListToLazyLoad(String sql, Object[] parameter, Class<?> resultClass, Cache transactionCache)
			throws Exception {
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
				SQLQueryAnalyzer analyzer = session.getSQLQueryAnalyzer();
				analyzer.analyze(sql, resultClass);
				handler = session.createNewEntityHandler(resultClass, analyzer.getExpressions(), analyzer.getColumnAliases(), transactionCache);
			}

			result = (List) session.getRunner().query(session.getConnection(), sql, handler, parameter, showSql,
					formatSql, 0,
					session.getListeners(), session.clientId());
		} finally {
		}

		if (result == null)
			return Collections.EMPTY_LIST;

		return result;
	}

	public SQLQuery<T> setClob(int parameterIndex, InputStream inputStream) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, inputStream);
		return this;
	}

	public SQLQuery<T> setClob(int parameterIndex, byte[] bytes) throws Exception {
		validateParameterIndex(parameterIndex);
		parameters.put(parameterIndex, bytes);
		return this;
	}

	public SQLQuery<T> setClob(String parameterName, InputStream inputStream) throws Exception {
		set(parameterName, inputStream);
		return this;
	}

	public SQLQuery<T> setClob(String parameterName, byte[] bytes) throws Exception {
		set(parameterName, bytes);
		return this;
	}

	public SQLQuery<T> timeOut(int seconds) {
		this.timeOut = seconds;
		return this;
	}

	public SQLQuery<T> callableType(CallableType type) {
		this.callableType = type;
		return this;
	}

	public SQLQuery<T> procedureOrFunctionName(String procedureName) {
		this.procedureName = procedureName;
		return this;
	}

	public SQLQuery<T> outputParametersName(String[] outputParametersName) {
		this.outputParametersName = outputParametersName;
		return this;
	}

	public SQLQuery<T> namedQuery(String name) {
		this.namedQuery = name;
		return this;
	}

	public T selectOneNamedQuery() throws Exception {
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
		DescriptionNamedQuery namedQuery = entityCache.getDescriptionNamedQuery(this.namedQuery);
		if (namedQuery == null)
			return null;
		this.sql = namedQuery.getQuery();
		return selectOne();
	}

	public T selectOneProcedure() throws Exception {
		List<T> result = selectListProcedure();
		if ((result != null) && (result.size() > 0))
			return result.get(FIRST_RECORD);
		return null;
	}

	public List<T> selectProcedure() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo de objeto para execução é um PROCEDIMENTO ou uma FUNÇÃO.");

		session.flush();

		List result = null;

		if (this.parameters.size() > 0)
			result = (List) session.getRunner().queryProcedure(session, session.getDialect(), callableType,
					procedureName, handler,
					parameters.values().toArray(), outputParametersName, showSql, timeOut, session.clientId());
		else if (this.namedParameters.size() > 0)
			result = (List) session.getRunner().queryProcedure(session, session.getDialect(), callableType,
					procedureName, handler,
					namedParameters.values().toArray(new NamedParameter[] {}), outputParametersName, showSql, timeOut,
					session.clientId());
		else
			result = (List) session.getRunner().queryProcedure(session, session.getDialect(), callableType,
					procedureName, handler, new Object[] {},
					outputParametersName, showSql, timeOut, session.clientId());
		return result;
	}

	public ProcedureResult executeProcedure() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo de objeto para execução é um PROCEDIMENTO ou uma FUNÇÃO.");

		session.flush();

		ProcedureResult result = null;

		if (this.parameters.size() > 0)
			result = session.getRunner().executeProcedure(session, session.getDialect(), callableType, procedureName,
					parameters.values().toArray(),
					outputParametersName, showSql, timeOut, session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().executeProcedure(session, session.getDialect(), callableType, procedureName,
					namedParameters.values().toArray(new NamedParameter[] {}), outputParametersName, showSql, timeOut,
					session.clientId());
		else
			result = session.getRunner().executeProcedure(session, session.getDialect(), callableType, procedureName,
					new Object[] {},
					outputParametersName, showSql, timeOut, session.clientId());
		return result;
	}

	public List<T> selectListNamedQuery() throws Exception {
		EntityCache cache = session.getEntityCacheManager().getEntityCache(resultClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(this.namedQuery);
		if (namedQuery == null)
			return null;
		this.sql = namedQuery.getQuery();
		return selectList();
	}

	public List<T> selectListProcedure() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		if (resultClass == null)
			throw new SQLQueryException("Informe a Classe para executar a consulta.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo para execução é um PROCEDIMENTO ou FUNÇÃO.");

		session.flush();
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
		ResultSetHandler handler = null;

		List result = null;

		if (this.parameters.size() > 0)
			result = (List) session.getRunner().queryProcedure(session, session.getDialect(), callableType,
					procedureName, handler,
					parameters.values().toArray(), outputParametersName, showSql, timeOut, session.clientId());
		else if (this.namedParameters.size() > 0)
			result = (List) session.getRunner().queryProcedure(session, session.getDialect(), callableType,
					procedureName, handler,
					namedParameters.values().toArray(new NamedParameter[] {}), outputParametersName, showSql, timeOut,
					session.clientId());
		else
			result = (List) session.getRunner().queryProcedure(session, session.getDialect(), callableType,
					procedureName, handler, new Object[] {},
					outputParametersName, showSql, timeOut, session.clientId());
		return result;
	}

	public SQLSessionResult selectListAndResultSet() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (resultClass == null)
			throw new SQLQueryException("Informe a Classe para executar a consulta.");

		session.forceFlush(SQLParserUtil.getTableNames(sql, session.getDialect()));
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
		ResultSetHandler handler;
		SQLQueryAnalyzer analyzer = session.getSQLQueryAnalyzer();
		analyzer.analyze(sql, resultClass);

		SQLCache transactionCache = new SQLCache(DEFAULT_CACHE_SIZE);
		SQLSessionResult result = null;
		try {
			if (entityCache == null)
				handler = new BeanHandler(resultClass);
			else {
				if (sql.toLowerCase().indexOf(" " + entityCache.getTableName().toLowerCase()) < 0) {
					throw new SQLException("A tabela " + entityCache.getTableName() + " da classe "
							+ resultClass.getName()
							+ " não foi localizada no SQL informado. Não será possível executar a consulta. SQL-> "
							+ sql);
				}
				handler = session.createNewEntityHandler(resultClass, analyzer.getExpressions(), analyzer.getColumnAliases(), transactionCache);
			}

			if (this.parameters.size() > 0)
				result = session.getRunner().queryWithResultSet(session.getConnection(), analyzer.getParsedSQL(),
						handler,
						parameters.values().toArray(), showSql, formatSql, timeOut, session.getListeners(),
						session.clientId());
			else if (this.namedParameters.size() > 0)
				result = session.getRunner().queryWithResultSet(session.getConnection(), analyzer.getParsedSQL(),
						handler,
						namedParameters.values().toArray(new NamedParameter[] {}), showSql, formatSql, timeOut,
						session.getListeners(),
						session.clientId());
			else
				result = session.getRunner().queryWithResultSet(session.getConnection(), analyzer.getParsedSQL(),
						handler,
						new NamedParameterParserResult[] {}, showSql, formatSql, timeOut, session.getListeners(),
						session.clientId());

		} finally {
			transactionCache.clear();
			session.getPersistenceContext().clearCache();
		}
		return result;
	}

	public SQLQuery<T> setLockMode(LockModeType lockMode) {
		this.lockMode = lockMode;
		return this;
	}

	public LockModeType getLockMode() {
		return lockMode;
	}

}
