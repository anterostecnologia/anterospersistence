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

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.query.AbstractSQLRunner;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;
import br.com.anteros.persistence.util.SQLFormatter;

/**
 * Classe responsável pela execução de SQL via JDBC no Banco de Dados
 * 
 * @author Edson Martins - Anteros
 * 
 */
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

	public Object query(Connection connection, String sql, ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
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

	public Object query(Connection connection, String sql, ResultSetHandler resultSetHandler, NamedParameter[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {

		ResultSet resultSet = null;
		Object result = null;
		NamedParameterStatement statement = null;
		try {
			statement = new NamedParameterStatement(connection, sql, parameters);
			if (timeOut > 0)
				statement.getStatement().setQueryTimeout(timeOut);

			for (NamedParameter param : parameters) {
				statement.setObject(param.getName(), param.getValue());
			}
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

	public SQLSessionResult queryWithResultSet(Connection connection, String sql, ResultSetHandler resultSetHandler, NamedParameter[] parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
		SQLSessionResult result = new SQLSessionResult();
		ResultSet resultSet = null;
		NamedParameterStatement stmt = null;
		try {
			stmt = new NamedParameterStatement(connection, sql, parameters);
			if (timeOut > 0)
				stmt.getStatement().setQueryTimeout(timeOut);

			for (NamedParameter param : parameters) {
				stmt.setObject(param.getName(), param.getValue());
			}
			if (showSql) {
				showSQLAndParameters(sql, parameters, formatSql, listeners, clientId);
			}

			resultSet = this.wrap(stmt.executeQuery());
			Object resultHandler = resultSetHandler.handle(resultSet);
			result.setResultSet(resultSet);
			result.setResultList(((List) resultHandler));
		} catch (SQLException e) {
			close(stmt);
			this.rethrow(e, sql, parameters, clientId);
		}

		return result;
	}

	public SQLSessionResult queryWithResultSet(Connection connection, String sql, ResultSetHandler resultSetHandler, Object[] parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception {
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
			Object resultHandler = resultSetHandler.handle(resultSet);
			result.setResultSet(resultSet);
			result.setResultList((List) resultHandler);
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
			Object[] inputParameters, String[] outputParametersName, boolean showSql, int timeOut, String clientId) throws Exception {
		CallableStatement statement = null;
		ResultSet resultSet = null;
		Object result = null;
		try {
			String[] split = name.split("\\(");

			statement = dialect.prepareCallableStatement(session.getConnection(), type, split[0], inputParameters, outputParametersName,
					getOutputSqlTypesByProcedure(session.getConnection(), session.getDialect(), split[0]), timeOut, showSql, clientId);

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

	public int[] getOutputSqlTypesByProcedure(Connection connection, DatabaseDialect dialect, String procedureName) throws Exception {
		int[] resultInt = cacheOutputTypes.get(procedureName);
		if (resultInt == null) {
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet resultSet = metadata.getProcedureColumns(dialect.getDefaultCatalog(), dialect.getDefaultSchema(), procedureName, null);
			List<Integer> result = new ArrayList<Integer>();
			int parameterType;
			if ((resultSet != null) && (resultSet.next())) {
				do {
					parameterType = resultSet.getShort("COLUMN_TYPE");
					if ((parameterType >= 3) && (parameterType <= 4))
						result.add(resultSet.getInt("DATA_TYPE"));
				} while (resultSet.next());
				close(resultSet);
			}
			resultInt = new int[result.size()];
			for (int i = 0; i < result.size(); i++)
				resultInt[i] = result.get(i).intValue();
			cacheOutputTypes.put(procedureName, resultInt);
		}
		return resultInt;
	}

	public ProcedureResult executeProcedure(SQLSession session, DatabaseDialect dialect, CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, boolean showSql, int timeOut, String clientId) throws Exception {
		CallableStatement statement = null;
		ProcedureResult result = new ProcedureResult();
		if (type == CallableType.FUNCTION) {
			if ((outputParametersName != null) && (outputParametersName.length > 0))
				log.error("Para executar a FUNCTION " + name + " não informe nenhum parâmetro de saída(output)." + " ##" + clientId);
			throw new SQLException("Para executar a FUNCTION " + name + " não informe nenhum parâmetro de saída(output).");
		}
		try {
			String[] split = name.split("\\(");
			log.debug("Preparando CallableStatement " + split[0] + " ##" + clientId);
			statement = dialect.prepareCallableStatement(session.getConnection(), type, split[0], inputParameters, outputParametersName,
					getOutputSqlTypesByProcedure(session.getConnection(), session.getDialect(), split[0]), timeOut, showSql, clientId);

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
			if (outputParametersName != null) {
				int outCount = inputParameters.length + 1;
				if (type == CallableType.FUNCTION)
					result.getOutputParameters().put("RESULT", statement.getObject(1));
				if (outputParametersName.length > 0) {
					int i = 0;
					if (type == CallableType.FUNCTION)
						i++;
					for (; i < outputParametersName.length; i++) {
						result.getOutputParameters().put(outputParametersName[i], statement.getObject(outCount));
						outCount++;
					}
				}
			}

		} catch (SQLException e) {
			this.rethrow(e, "", new Object[] {}, clientId);
		}
		
		if (result.getResultSet()==null) {
			statement.close();
		}
			
		
		return result;

	}

	public Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		return this.query(conn, sql, resultSetHandler, (Object[]) null, showSql, formatSql, 0, listeners, clientId);
	}

	public Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		return this.query(conn, sql, resultSetHandler, (Object[]) null, showSql, formatSql, timeOut, listeners, clientId);
	}

	public Object query(String sql, ResultSetHandler rsh, Object[] parameters, boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
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

	public Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, List<SQLSessionListener> listeners,
			String clientId) throws Exception {
		return this.query(sql, resultSetHandler, (Object[]) null, showSql, formatSql, listeners, clientId);
	}

	public Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		return this.query(sql, resultSetHandler, (Object[]) null, showSql, formatSql, timeOut, listeners, clientId);
	}

	public ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters, boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
		return executeQuery(connection, sql, parameters, showSql, formatSql, 0, listeners, clientId);
	}

	public ResultSet executeQuery(Connection connection, String sql, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception {
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

	public int update(Connection connection, String sql, NamedParameter[] parameters, boolean showSql, List<SQLSessionListener> listeners,
			String clientId) throws Exception {
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
		}

		return rows;
	}

	public int update(Connection connection, String sql, NamedParameter[] parameters, IdentifierPostInsert identifierPostInsert,
			String identitySelectString, boolean showSql, List<SQLSessionListener> listeners, String clientId) throws Exception {
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
			close(statement.getStatement());
			close(stmtGeneratedKeys);
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
		log.debug("Sql-> " + sqlFormatted + " ##" + clientId);

		if ((parameters != null) && (parameters.length > 0)) {
			StringBuffer sb = new StringBuffer("Parameters -> ");
			boolean append = false;
			for (Object p : parameters) {
				if (append)
					sb.append(", ");
				sb.append(p + "");
				append = true;
			}
			log.debug(sb.toString() + " ##" + clientId);
		}

		if (listeners != null)
			for (SQLSessionListener listener : listeners)
				listener.onExecuteSQL(sqlFormatted, parameters);
	}

	protected void showSQLAndParameters(String sql, NamedParameter[] parameters, boolean formatSql, List<SQLSessionListener> listeners,
			String clientId) {
		String sqlFormatted = (formatSql == true ? SQLFormatter.format(sql) : sql);
		log.debug("Sql-> " + sqlFormatted + " ##" + clientId);
		if ((parameters != null) && (parameters.length > 0)) {
			StringBuffer sb = new StringBuffer("Parâmetros -> ");
			boolean append = false;
			for (NamedParameter p : parameters) {
				if (append)
					sb.append(", ");
				sb.append(p.toString());
				append = true;
			}
			log.debug(sb.toString() + " ##" + clientId);
		}

		if (listeners != null) {
			for (SQLSessionListener listener : listeners)
				listener.onExecuteSQL(sqlFormatted, parameters);
		}
	}

	protected void showSQLAndParameters(String sql, Map<String, Object> parameters, boolean formatSql, List<SQLSessionListener> listeners,
			String clientId) {
		String sqlFormatted = (formatSql == true ? SQLFormatter.format(sql) : sql);
		log.debug("Sql-> " + sqlFormatted + " ##" + clientId);
		if ((parameters != null) && (parameters.size() > 0)) {
			StringBuffer sb = new StringBuffer("Parâmetros -> ");
			boolean append = false;
			for (String param : parameters.keySet()) {
				if (append)
					sb.append(", ");
				param += "=" + parameters.get(param);
				sb.append(param);
				append = true;
			}
			log.debug(sb.toString() + " ##" + clientId);

			if (listeners != null) {
				for (SQLSessionListener listener : listeners)
					listener.onExecuteSQL(sqlFormatted, parameters);
			}
		}
	}

}
