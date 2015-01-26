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
package br.com.anteros.persistence.session.query;
 

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.metadata.beans.IntrospectionException;
import br.com.anteros.core.metadata.beans.Introspector;
import br.com.anteros.core.metadata.beans.PropertyDescriptor;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.sql.binder.ParameterBinding;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;

public abstract class AbstractSQLRunner {

	protected static Logger log = LoggerProvider.getInstance().getLogger(SQLQueryRunner.class.getName());
	protected volatile boolean pmdKnownBroken = false;
	protected DataSource dataSource;
	protected Map<String, int[]> cacheOutputTypes = new HashMap<String, int[]>();

	public AbstractSQLRunner() {
		super();
		dataSource = null;
	}

	public AbstractSQLRunner(boolean pmdKnownBroken) {
		super();
		this.pmdKnownBroken = pmdKnownBroken;
		dataSource = null;
	}

	public AbstractSQLRunner(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public AbstractSQLRunner(DataSource dataSource, boolean pmdKnownBroken) {
		super();
		this.pmdKnownBroken = pmdKnownBroken;
		this.dataSource = dataSource;
	}

	public void fillStatement(PreparedStatement statement, Object[] parameters) throws Exception {
		if (parameters == null) {
			return;
		}
		ParameterMetaData parameterMetadata = statement.getParameterMetaData();
		if (parameterMetadata.getParameterCount() < parameters.length) {
			log.error("Muitos parâmetros: esperado " + parameterMetadata.getParameterCount() + ", encontrado "
					+ parameters.length);
			throw new SQLException("Muitos parâmetros: esperado " + parameterMetadata.getParameterCount()
					+ ", encontrado " + parameters.length);
		}
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] != null) {
				setParameterValueStatement(statement, parameters[i], i + 1);
			} else {
				int sqlType = Types.VARCHAR;
				if (!pmdKnownBroken) {
					try {
						sqlType = parameterMetadata.getParameterType(i + 1);
					} catch (SQLException e) {
						pmdKnownBroken = true;
					}
				}
				statement.setNull(i + 1, sqlType);
			}
		}
	}

	public void setParameterValueStatement(PreparedStatement statement, Object parameter, int parameterIndex)
			throws Exception {
		if (parameter instanceof ParameterBinding) {
			((ParameterBinding) parameter).bindValue(statement, parameterIndex);
		} else
			statement.setObject(parameterIndex, parameter);
	}

	public void fillStatementWithBean(PreparedStatement statement, Object bean, PropertyDescriptor[] properties)
			throws Exception {
		Object[] params = new Object[properties.length];
		for (int i = 0; i < properties.length; i++) {
			PropertyDescriptor property = properties[i];
			Object value = null;
			Method method = property.getReadMethod();
			if (method == null) {
				String erro = "Não há nenhum método para leitura da propriedade do objeto " + bean.getClass() + " "
						+ property.getName();
				log.error(erro);
				throw new RuntimeException(erro);
			}

			try {
				value = method.invoke(bean, new Object[0]);
			} catch (InvocationTargetException e) {
				log.error("Não foi possível invocar o método: " + method, e);
				throw new RuntimeException("Não foi possível invocar o método: " + method, e);
			} catch (IllegalArgumentException e) {
				log.error("Não foi possível invocar o método sem argumentos: " + method, e);
				throw new RuntimeException("Não foi possível invocar o método sem argumentos: " + method, e);
			} catch (IllegalAccessException e) {
				log.error("Não foi possível invocar o método: " + method, e);
				throw new RuntimeException("Não foi possível invocar o método: " + method, e);
			}
			params[i] = value;
		}
		fillStatement(statement, params);
	}

	public void fillStatementWithBean(PreparedStatement statement, Object bean, String[] propertyNames)
			throws Exception {
		PropertyDescriptor[] descriptors;
		try {
			descriptors = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
		} catch (IntrospectionException e) {
			log.error("Não foi possível obter informações sobre o objeto " + bean.getClass().toString(), e);
			throw new RuntimeException("Não foi possível obter informações sobre o objeto "
					+ bean.getClass().toString(), e);
		}
		PropertyDescriptor[] sorted = new PropertyDescriptor[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (propertyName == null) {
				log.error("Nome da propriedade não pode ser nulo: " + i);
				throw new NullPointerException("Nome da propriedade não pode ser nulo: " + i);
			}
			boolean found = false;
			for (int j = 0; j < descriptors.length; j++) {
				PropertyDescriptor descriptor = descriptors[j];
				if (propertyName.equals(descriptor.getName())) {
					sorted[i] = descriptor;
					found = true;
					break;
				}
			}
			if (!found) {
				log.error("Não foi encontrada a propriedade no objeto: " + bean.getClass() + " " + propertyName);
				throw new RuntimeException("Não foi encontrada a propriedade no objeto: " + bean.getClass() + " "
						+ propertyName);
			}
		}
		fillStatementWithBean(statement, bean, sorted);
	}

	protected PreparedStatement prepareStatement(Connection connection, String sql) throws Exception {
		return connection.prepareStatement(sql);
	}

	protected Connection prepareConnection() throws Exception {
		if (this.getDataSource() == null) {
			log.error("SQLQueryRunner requer um DataSource ou uma conexão para ser executado.");
			throw new SQLException("SQLQueryRunner requer um DataSource ou uma conexão para ser executado.");
		}
		return this.getDataSource().getConnection();
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	protected void rethrow(SQLException cause, String sql, Object[] parameters, String clientId) throws Exception {

		String causeMessage = cause.getMessage();
		if (causeMessage == null) {
			causeMessage = "";
		}
		StringBuilder msg = new StringBuilder(causeMessage);

		if ("".equals(sql)) {
			msg.append(" Query: ").append(sql);
		}
		if ((parameters != null) && (parameters.length > 0)) {
			msg.append(" Parâmetros: ");
			msg.append(Arrays.asList(parameters));
		}

		SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
		e.setNextException(cause);
		if (StringUtils.isEmpty(clientId))
			log.error(msg.toString(), e);
		else
			log.error(msg.toString() + " ##" + clientId, e);
		throw e;
	}

	protected void rethrow(SQLException cause, String sql, NamedParameter[] parameters, String clientId)
			throws Exception {

		String causeMessage = cause.getMessage();
		if (causeMessage == null) {
			causeMessage = "";
		}
		StringBuilder msg = new StringBuilder(causeMessage);

		msg.append(" Query: ");
		msg.append(sql);
		msg.append(" Parâmetros: ");

		if (parameters == null) {
			msg.append("[]");
		} else {
			for (NamedParameter namedParameter : parameters)
				msg.append(namedParameter.getValue());
		}

		SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
		e.setNextException(cause);

		if (StringUtils.isEmpty(clientId))
			log.error(msg.toString(), e);
		else
			log.error(msg.toString() + " ##" + clientId, e);
		throw e;
	}

	protected void rethrow(SQLException cause, String sql, Map<String, Object> parameters, String clientId)
			throws Exception {

		String causeMessage = cause.getMessage();
		if (causeMessage == null) {
			causeMessage = "";
		}
		StringBuilder msg = new StringBuilder(causeMessage);

		msg.append(" Query: ");
		msg.append(sql);
		msg.append(" Parâmetros: ");

		if (parameters == null) {
			msg.append("[]");
		} else {
			msg.append(parameters);
		}

		SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
		e.setNextException(cause);

		if (StringUtils.isEmpty(clientId))
			log.error(msg.toString(), e);
		else
			log.error(msg.toString() + " ##" + clientId);
		throw e;
	}

	protected ResultSet wrap(ResultSet resultSet) {
		return resultSet;
	}

	protected void close(Connection connection) throws Exception {
		if (connection != null)
			connection.close();
	}

	protected void close(Statement statement) throws Exception {
		if (statement != null)
			statement.close();
	}

	protected void close(ResultSet resultSet) throws Exception {
		if (resultSet != null)
			resultSet.close();
	}

	protected void close(NamedParameterStatement stmt) throws Exception {
		if (stmt != null)
			stmt.close();
	}

	/**
	 * ABSTRACT METHODS
	 */

	public abstract int[] batch(Connection conn, String sql, Object[][] params) throws Exception;

	public abstract int[] batch(String sql, Object[][] params) throws Exception;

	public abstract Object query(Connection connection, String sql, ResultSetHandler resultSetHandler,
			Object[] parameters, boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners,
			String clientId) throws Exception;

	public abstract Object query(Connection connection, String sql, ResultSetHandler resultSetHandler,
			NamedParameter[] parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract SQLSessionResult queryWithResultSet(Connection connection, String sql,
			ResultSetHandler resultSetHandler, NamedParameter[] parameters, boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract SQLSessionResult queryWithResultSet(Connection connection, String sql,
			ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(Connection connection, String sql, ResultSetHandler resultSetHandler,
			Map<String, Object> parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object queryProcedure(SQLSession session, DatabaseDialect dialect, CallableType type, String name,
			ResultSetHandler resultSetHandler, NamedParameter[] parameters, 
			boolean showSql, int timeOut, String clientId) throws Exception;

		public abstract ProcedureResult executeProcedure(SQLSession session, DatabaseDialect dialect, CallableType type,
			String name, NamedParameter[] parameters, boolean showSql, int timeOut,
			String clientId) throws Exception;

	public abstract Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql,
			boolean formatSql, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler rsh, Object[] parameters, boolean showSql,
			boolean formatSql, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters,
			boolean showSql, boolean formatSql, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId)
			throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, Map<String, Object> parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId)
			throws Exception;

	public abstract int update(Connection connection, String sql, Object[] parameters,
			IdentifierPostInsert identifierPostInsert, String identitySelectString, boolean showSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract int update(Connection connection, String sql, NamedParameter[] parameters, boolean showSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract int update(Connection connection, String sql, NamedParameter[] parameters,
			IdentifierPostInsert identifierPostInsert, String identitySelectString, boolean showSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract int update(Connection connection, String sql, List<SQLSessionListener> listeners) throws Exception;

	public abstract int update(Connection connection, String sql, Object parameter, List<SQLSessionListener> listeners)
			throws Exception;

	public abstract int update(Connection connection, String sql, Object[] parameters,
			List<SQLSessionListener> listeners) throws Exception;

	public abstract int update(Connection connection, String sql, NamedParameter[] parameters,
			List<SQLSessionListener> listeners) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, boolean showSql, boolean formatSql,
			String clientId) throws Exception;

	public abstract void executeDDL(Connection connection, String ddl, boolean showSql, boolean formatSql,
			String clientId) throws Exception;

}
