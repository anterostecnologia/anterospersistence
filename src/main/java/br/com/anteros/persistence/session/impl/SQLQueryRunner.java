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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.SQLFormatter;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.parameter.OutputNamedParameter;
import br.com.anteros.persistence.parameter.SubstitutedParameter;
import br.com.anteros.persistence.schema.definition.StoredParameterSchema;
import br.com.anteros.persistence.schema.definition.StoredProcedureSchema;
import br.com.anteros.persistence.schema.definition.type.StoredParameterType;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;

/**
 * Classe responsável pela execução de SQL via JDBC no Banco de Dados
 * 
 * @author Edson Martins - Anteros
 * 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SQLQueryRunner extends AbstractSQLRunner {

	public int[] batch(Connection connection, String sql, Object[][] parameters) throws Exception {
		PreparedStatement statement = null;
		int[] rows = null;
		try {
			statement = this.prepareStatement(connection, sql);

			for (int i = 0; i < parameters.length; i++) {
				this.fillStatement(statement, parameters[i]);
				statement.addBatch();
			}
			rows = statement.executeBatch();

		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, "");
		} finally {
			close(statement);
		}
		return rows;
	}

	public int[] batch(String sql, Object[][] parameters) throws Exception {
		Connection connection = this.prepareConnection();
		try {
			return this.batch(connection, sql, parameters);
		} finally {
			close(connection);
		}
	}

	public Object query(Connection connection, String sql, ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Object result = null;

		try {
			statement = this.prepareStatement(connection, sql);
			if (timeOut > 0)
				statement.setQueryTimeout(timeOut);

			this.fillStatement(statement, parameters);
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(statement.executeQuery());
			result = resultSetHandler.handle(resultSet);
		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			try {
				close(resultSet);
			} finally {
				close(statement);
			}
		}
		return result;
	}

	public Object query(Connection connection, String sql, ResultSetHandler resultSetHandler, NamedParameter[] parameters, boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {

		ResultSet resultSet = null;
		Object result = null;
		NamedParameterStatement statement = null;
		try {
			statement = new NamedParameterStatement(connection, sql, parameters);
			if (timeOut > 0)
				statement.getStatement().setQueryTimeout(timeOut);

			for (NamedParameter param : parameters) {
				if (!(param instanceof SubstitutedParameter))
					statement.setObject(param.getName(), param.getValue());
			}
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(statement.executeQuery());
			result = resultSetHandler.handle(resultSet);
		} catch (SQLException e) {
			statement = null;
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			try {
				close(resultSet);
			} finally {
				close(statement);
				statement = null;
			}
		}
		return result;
	}

	
	public SQLSessionResult queryWithResultSet(Connection connection, String sql, ResultSetHandler resultSetHandler, NamedParameter[] parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
		SQLSessionResult result = new SQLSessionResult();
		ResultSet resultSet = null;
		NamedParameterStatement statement = null;
		try {
			statement = new NamedParameterStatement(connection, sql, parameters);
			if (timeOut > 0)
				statement.getStatement().setQueryTimeout(timeOut);

			for (NamedParameter param : parameters) {
				if (!(param instanceof SubstitutedParameter))
					statement.setObject(param.getName(), param.getValue());
			}
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(statement.executeQuery());
			Object resultHandler = resultSetHandler.handle(resultSet);
			result.setResultSet(resultSet);
			result.setResultList(((List) resultHandler));
		} catch (SQLException e) {
			statement = null;
			this.rethrow(e, sql, parameters, clientId);
		}

		return result;
	}

	
	public SQLSessionResult queryWithResultSet(Connection connection, String sql, ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
		SQLSessionResult result = new SQLSessionResult();
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			statement = this.prepareStatement(connection, sql);
			if (timeOut > 0)
				statement.setQueryTimeout(timeOut);

			this.fillStatement(statement, parameters);
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(statement.executeQuery());
			Object resultHandler = null;
			if (resultSetHandler != null)
				resultHandler = resultSetHandler.handle(resultSet);

			if (resultHandler == null)
				resultHandler = Collections.EMPTY_LIST;

			if (!(resultHandler instanceof List)) {
				if (resultHandler instanceof Collection)
					resultHandler = new ArrayList<Object>((Collection<?>) resultHandler);
				else {
					resultHandler = Arrays.asList(resultHandler);
				}
			}
			result.setResultSet(resultSet);
			result.setResultList((List<?>) resultHandler);
		} catch (SQLException e) {
			close(statement);
			this.rethrow(e, sql, parameters, clientId);
		}

		return result;
	}

	public Object query(Connection connection, String sql, ResultSetHandler resultSetHandler, Map<String, Object> parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
		ResultSet resultSet = null;
		Object result = null;
		NamedParameterStatement statement = null;
		try {
			statement = new NamedParameterStatement(connection, sql, null);
			if (timeOut > 0)
				statement.getStatement().setQueryTimeout(timeOut);

			Iterator<String> it = parameters.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				statement.setObject(key, parameters.get(key));
			}

			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(statement.executeQuery());
			result = resultSetHandler.handle(resultSet);
		} catch (SQLException e) {
			close(statement);
			statement = null;
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			try {
				close(resultSet);
			} finally {
				close(statement);
			}
		}
		return result;
	}

	public Object queryProcedure(SQLSession session, DatabaseDialect dialect, CallableType type, String name, ResultSetHandler resultSetHandler,
			NamedParameter[] parameters, boolean showSql, int timeOut, String clientId) throws Exception {
		CallableStatement statement = null;
		ResultSet resultSet = null;
		Object result = null;
		try {
			String[] split = name.split("\\(");
			List<NamedParameter> newParameters = adjustNamedParametersStoredProcedure(session, split[0], parameters, type);
			statement = dialect.prepareCallableStatement(session.getConnection(), type, split[0], newParameters.toArray(new NamedParameter[] {}), timeOut,
					showSql, clientId);

			if (type == CallableType.FUNCTION) {
				if (statement.execute()) {
					Object object = statement.getObject(1);
					if (object instanceof ResultSet)
						resultSet = (ResultSet) object;
				}
			} else {
				if (statement.execute())
					resultSet = statement.getResultSet();
			}
			if (resultSet != null)
				result = resultSetHandler.handle(resultSet);

		} catch (SQLException e) {
			this.rethrow(e, "", new Object[] {}, clientId);

		} finally {
			try {
				close(resultSet);
			} finally {
				close(statement);
			}
		}

		return result;
	}

	protected List<NamedParameter> adjustNamedParametersStoredProcedure(SQLSession session, String spName, NamedParameter[] parameters, CallableType type)
			throws Exception {
		Set<StoredProcedureSchema> storedProcedures = null;
		if (type == CallableType.FUNCTION) {
			storedProcedures = new LinkedHashSet<StoredProcedureSchema>(session.getDialect().getStoredFunctions(session.getConnection(), spName, true));
		} else {
			storedProcedures = session.getDialect().getStoredProcedures(session.getConnection(), spName, true);
		}

		if (storedProcedures.size() == 0) {
			throw new SQLQueryException("Procedimento/função " + spName + " não encontrado.");
		}
		StoredProcedureSchema storedProcedure = storedProcedures.iterator().next();
		List<NamedParameter> newParameters = new ArrayList<NamedParameter>();

		int size = (parameters == null ? 0 : parameters.length);
		if (type == CallableType.FUNCTION)
			size++;

		if (((parameters == null) && (storedProcedure.getParameters().size() > 1)) || ((storedProcedure.getParameters().size() != size))) {
			throw new SQLQueryException("Número de parâmetros informados para execução do procedimento/função " + spName + " incorretos.");
		}

		for (StoredParameterSchema p : storedProcedure.getParameters()) {
			if ((p.getParameterType() == StoredParameterType.RETURN_VALUE) || (p.getParameterType() == StoredParameterType.RETURN_RESULTSET)) {
				newParameters.add(new OutputNamedParameter("RESULT", p.getParameterType(), p.getDataTypeSql()));
			} else {
				NamedParameter namedParameter = NamedParameter.getNamedParameterByName(parameters, p.getName());
				if (namedParameter == null) {
					throw new SQLQueryException("Parâmetro " + p.getName()
							+ " não encontrado na lista de parâmetros informados para execução do procedimento/função " + spName);
				}
				if (((p.getParameterType() == StoredParameterType.OUT) || (p.getParameterType() == StoredParameterType.IN_OUT))
						&& !(namedParameter instanceof OutputNamedParameter)) {
					throw new SQLQueryException("Parâmetro " + p.getName()
							+ " é um parâmetro de saída. Use OuputNamedParameter para parâmetros deste tipo. Procedimento/função " + spName);
				}

				if (namedParameter instanceof OutputNamedParameter) {
					((OutputNamedParameter) namedParameter).setDataTypeSql(p.getDataTypeSql());
				}

				newParameters.add(namedParameter);
			}
		}
		return newParameters;
	}

	public ProcedureResult executeProcedure(SQLSession session, DatabaseDialect dialect, CallableType type, String name, NamedParameter[] parameters,
			boolean showSql, int timeOut, String clientId) throws Exception {
		CallableStatement statement = null;
		ProcedureResult result = new ProcedureResult();
		try {
			String[] split = name.split("\\(");
			log.debug("Preparando CallableStatement " + split[0] + " ##" + clientId);
			List<NamedParameter> newParameters = adjustNamedParametersStoredProcedure(session, split[0], parameters, type);

			statement = dialect.prepareCallableStatement(session.getConnection(), type, split[0], newParameters.toArray(new NamedParameter[] {}), timeOut,
					showSql, clientId);

			if (type == CallableType.FUNCTION) {
				if (statement.execute()) {
					Object object = statement.getObject(1);
					if (object instanceof ResultSet)
						result.setResultSet((ResultSet) object);
				}
			} else {
				statement.execute();
				result.setResultSet(statement.getResultSet());
			}
			if (NamedParameter.hasOutputParameters(newParameters)) {
				int i = 1;
				for (NamedParameter p : newParameters) {
					if (p instanceof OutputNamedParameter) {
						result.getOutputParameters().put(p.getName(), statement.getObject(i));
					}
					i++;
				}
			}

		} catch (SQLException e) {
			this.rethrow(e, "", new Object[] {}, clientId);
		}

		if (result.getResultSet() == null) {
			statement.close();
		}

		return result;

	}

	public Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, List<SQLSessionListener> listeners,
			String clientId) throws Exception {
		return this.query(conn, sql, resultSetHandler, (Object[]) null, showSql, formatSql, 0, listeners, clientId);
	}

	public Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		return this.query(conn, sql, resultSetHandler, (Object[]) null, showSql, formatSql, timeOut, listeners, clientId);
	}

	public Object query(String sql, ResultSetHandler rsh, Object[] parameters, boolean showSql, boolean formatSql, List<SQLSessionListener> listeners,
			String clientId) throws Exception {
		Connection connection = this.prepareConnection();
		try {
			return this.query(connection, sql, rsh, parameters, showSql, formatSql, 0, listeners, clientId);
		} finally {
			close(connection);
		}
	}

	public Object query(String sql, ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		Connection conn = this.prepareConnection();
		try {
			return this.query(conn, sql, resultSetHandler, parameters, showSql, formatSql, timeOut, listeners, clientId);
		} finally {
			close(conn);
		}
	}

	public Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, List<SQLSessionListener> listeners, String clientId)
			throws Exception {
		return this.query(sql, resultSetHandler, (Object[]) null, showSql, formatSql, listeners, clientId);
	}

	public Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners,
			String clientId) throws Exception {
		return this.query(sql, resultSetHandler, (Object[]) null, showSql, formatSql, timeOut, listeners, clientId);
	}

	public ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters, boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		return executeQuery(connection, sql, parameters, showSql, formatSql, 0, listeners, clientId);
	}

	public ResultSet executeQuery(Connection connection, String sql, boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners,
			String clientId) throws Exception {
		return executeQuery(connection, sql, (Object[]) null, showSql, formatSql, timeOut, listeners, clientId);
	}

	public ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		ResultSet result = null;
		NamedParameterStatement statement = null;
		try {
			statement = new NamedParameterStatement(connection, sql, parameters);
			for (NamedParameter namedParameter : parameters)
				statement.setObject(namedParameter.getName(), namedParameter.getValue());
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}
			result = this.wrap(statement.executeQuery());
		} catch (SQLException e) {
			statement = null;
			this.rethrow(e, sql, parameters, clientId);
		} finally {
		}

		return result;
	}

	public ResultSet executeQuery(Connection connection, String sql, Object[] parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = this.prepareStatement(connection, sql);
			if (timeOut > 0)
				statement.setQueryTimeout(timeOut);
			this.fillStatement(statement, parameters);
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}
			result = this.wrap(statement.executeQuery());
		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);
		}
		return result;
	}

	public ResultSet executeQuery(Connection connection, String sql, Map<String, Object> parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		ResultSet resultSet = null;
		NamedParameterStatement statement = null;
		try {
			statement = new NamedParameterStatement(connection, sql, null);
			if (timeOut > 0)
				statement.getStatement().setQueryTimeout(timeOut);

			Iterator<String> it = parameters.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				statement.setObject(key, parameters.get(key));
			}

			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(statement.executeQuery());

		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			close(statement);
			statement = null;
		}

		return resultSet;
	}

	public int update(Connection connection, String sql, List<SQLSessionListener> listeners) throws Exception {
		return this.update(connection, sql, (Object[]) null, listeners);
	}

	public int update(Connection connection, String sql, Object[] parameters, List<SQLSessionListener> listeners) throws Exception {

		return this.update(connection, sql, parameters, false, listeners, "");
	}

	public int update(Connection connection, String sql, NamedParameter[] parameters, List<SQLSessionListener> listeners) throws Exception {

		return this.update(connection, sql, parameters, false, listeners, "");
	}

	public int update(Connection connection, String sql, Object[] parameters, boolean showSql, List<SQLSessionListener> listeners, String clientId)
			throws Exception {

		PreparedStatement statement = null;
		int rows = 0;

		try {
			statement = this.prepareStatement(connection, sql);
			this.fillStatement(statement, parameters);

			if (showSql) {
				showSQLAndParameters(sql, parameters, true, listeners, clientId);
			}

			rows = statement.executeUpdate();

		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			close(statement);
		}

		return rows;
	}

	public int update(Connection connection, String sql, Object[] parameters, IdentifierPostInsert identifierPostInsert, String identitySelectString,
			boolean showSql, List<SQLSessionListener> listeners, String clientId) throws Exception {
		PreparedStatement statement = null;
		PreparedStatement statementGeneratedKeys = null;
		ResultSet rsGeneratedKeys;
		int rows = 0;
		try {
			statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			this.fillStatement(statement, parameters);
			if (showSql) {
				showSQLAndParameters(sql, parameters, true, listeners, clientId);
			}

			rows = statement.executeUpdate();

			rsGeneratedKeys = statement.getGeneratedKeys();
			if (rsGeneratedKeys.next()) {
				identifierPostInsert.setGeneratedValue(rsGeneratedKeys);
				close(rsGeneratedKeys);
			} else {
				close(rsGeneratedKeys);
				if ((identitySelectString != null) && ("".equals(identitySelectString))) {
					statementGeneratedKeys = connection.prepareStatement(identitySelectString);
					rsGeneratedKeys = statementGeneratedKeys.executeQuery();
					if (rsGeneratedKeys.next())
						identifierPostInsert.setGeneratedValue(rsGeneratedKeys);
					close(rsGeneratedKeys);
				}
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			close(statement);
			close(statementGeneratedKeys);
		}

		return rows;
	}

	public int update(Connection connection, String sql, NamedParameter[] parameters, boolean showSql, List<SQLSessionListener> listeners, String clientId)
			throws Exception {
		NamedParameterStatement statement = null;
		int rows = 0;
		try {
			statement = new NamedParameterStatement(connection, sql, parameters);
			for (NamedParameter namedParameter : parameters) {
				statement.setObject(namedParameter.getName(), namedParameter.getValue());
			}
			if (showSql) {
				showSQLAndParameters(sql, parameters, true, listeners, clientId);
			}

			rows = statement.executeUpdate();
		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			close(statement);
			statement = null;
		}

		return rows;
	}

	public int update(Connection connection, String sql, NamedParameter[] parameters, IdentifierPostInsert identifierPostInsert, String identitySelectString,
			boolean showSql, List<SQLSessionListener> listeners, String clientId) throws Exception {
		NamedParameterStatement statement = null;
		PreparedStatement stmtGeneratedKeys = null;
		ResultSet rsGeneratedKeys;
		int rows = 0;
		try {
			statement = new NamedParameterStatement(connection, sql, parameters, Statement.RETURN_GENERATED_KEYS);
			for (NamedParameter namedParameter : parameters) {
				statement.setObject(namedParameter.getName(), namedParameter.getValue());
			}
			if (showSql) {
				showSQLAndParameters(sql, parameters, true, listeners, clientId);
			}

			rows = statement.executeUpdate();

			rsGeneratedKeys = statement.getStatement().getGeneratedKeys();
			if (rsGeneratedKeys.next()) {
				identifierPostInsert.setGeneratedValue(rsGeneratedKeys);
				close(rsGeneratedKeys);
			} else {
				close(rsGeneratedKeys);
				if ((identitySelectString != null) && ("".equals(identitySelectString))) {
					stmtGeneratedKeys = connection.prepareStatement(identitySelectString);
					rsGeneratedKeys = stmtGeneratedKeys.executeQuery();
					if (rsGeneratedKeys.next()) {
						identifierPostInsert.setGeneratedValue(rsGeneratedKeys);
					}
					close(rsGeneratedKeys);
				}
			}

		} catch (SQLException e) {
			this.rethrow(e, sql, parameters, clientId);

		} finally {
			if (statement != null)
				close(statement.getStatement());
			close(stmtGeneratedKeys);
			statement = null;
			stmtGeneratedKeys = null;
		}

		return rows;
	}

	public ResultSet executeQuery(Connection connection, String sql, boolean showSql, boolean formatSql, String clientId) throws Exception {
		if (showSql)
			log.debug("Sql-> " + (formatSql == true ? SQLFormatter.format(sql) : sql) + " ##" + clientId);
		return connection.prepareStatement(sql).executeQuery();
	}

	@Override
	public int update(Connection connection, String sql, Object parameter, List<SQLSessionListener> listeners) throws Exception {
		return update(connection, sql, new Object[] { parameter }, false, listeners, "");
	}

	@Override
	public void executeDDL(Connection connection, String ddl, boolean showSql, boolean formatSql, String clientId) throws Exception {
		if (showSql)
			log.debug("DDL-> " + (formatSql == true ? SQLFormatter.format(ddl) : ddl) + " ##" + clientId);
		connection.prepareStatement(ddl).executeUpdate();
	}

	protected void showSQLAndParameters(String sql, Object[] parameters, boolean formatSql, List<SQLSessionListener> listeners, String clientId) {
		String sqlFormatted = (formatSql == true ? SQLFormatter.format(sql) : sql);
		System.out.println("Sql-> " + sqlFormatted + " ##" + clientId);

		if ((parameters != null) && (parameters.length > 0)) {
			StringBuilder sb = new StringBuilder("Parameters -> ");
			boolean append = false;
			for (Object p : parameters) {
				if (append)
					sb.append(", ");
				sb.append(p + "");
				append = true;
			}
			System.out.println(sb.toString() + " ##" + clientId);
		}

		if (listeners != null)
			for (SQLSessionListener listener : listeners)
				listener.onExecuteSQL(sqlFormatted, parameters);
	}

	protected void showSQLAndParameters(String sql, NamedParameter[] parameters, boolean formatSql, List<SQLSessionListener> listeners, String clientId) {
		String sqlFormatted = (formatSql == true ? SQLFormatter.format(sql) : sql);
		System.out.println("Sql-> " + sqlFormatted + " ##" + clientId);
		if ((parameters != null) && (parameters.length > 0)) {
			StringBuilder sb = new StringBuilder("Parâmetros -> ");
			boolean append = false;
			for (NamedParameter p : parameters) {
				if (append)
					sb.append(", ");
				sb.append(p.toString());
				append = true;
			}
			System.out.println(sb.toString() + " ##" + clientId);
		}

		if (listeners != null) {
			for (SQLSessionListener listener : listeners)
				listener.onExecuteSQL(sqlFormatted, parameters);
		}
	}

	protected void showSQLAndParameters(String sql, Map<String, Object> parameters, boolean formatSql, List<SQLSessionListener> listeners, String clientId) {
		String sqlFormatted = (formatSql == true ? SQLFormatter.format(sql) : sql);
		System.out.println("Sql-> " + sqlFormatted + " ##" + clientId);
		if ((parameters != null) && (parameters.size() > 0)) {
			StringBuilder sb = new StringBuilder("Parâmetros -> ");
			boolean append = false;
			for (String param : parameters.keySet()) {
				if (append)
					sb.append(", ");
				param += "=" + parameters.get(param);
				sb.append(param);
				append = true;
			}
			System.out.println(sb.toString() + " ##" + clientId);

			if (listeners != null) {
				for (SQLSessionListener listener : listeners)
					listener.onExecuteSQL(sqlFormatted, parameters);
			}
		}
	}

}
