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
package br.com.anteros.persistence.session.query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.persistence.beans.IntrospectionException;
import br.com.anteros.persistence.beans.Introspector;
import br.com.anteros.persistence.beans.PropertyDescriptor;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.session.type.DateTimeType;
import br.com.anteros.persistence.session.type.DateType;
import br.com.anteros.persistence.session.type.LobType;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;
import br.com.anteros.persistence.util.ArrayUtils;
import br.com.anteros.persistence.util.StringUtils;

public abstract class AbstractSQLRunner {

	protected static Logger log = LoggerFactory.getLogger(SQLQueryRunner.class);
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
			throws SQLException, IOException {
		if (parameter instanceof LobType) {
			LobType type = (LobType) parameter;
			if (type.getValue() instanceof Clob) {
				statement.setClob(parameterIndex, (Clob) type.getValue());
			} else if (type.getValue() instanceof Character[]) {
				String value = new String(ArrayUtils.toPrimitive((Character[]) type.getValue()));
				statement.setString(parameterIndex, value);
			} else if (type.getValue().getClass() == char[].class) {
				String value = new String((char[]) type.getValue());
				statement.setString(parameterIndex, value);
			} else if (type.getValue() instanceof String) {
				statement.setString(parameterIndex, (String) type.getValue());
			} else if (type.getValue() instanceof Blob) {
				statement.setBlob(parameterIndex + 1, (Blob) type.getValue());
			} else if (type.getValue() instanceof Byte[]) {
				statement.setObject(parameterIndex, type.getValue(), Types.BINARY);
			} else if (type.getValue().getClass() == byte[].class) {
				statement.setObject(parameterIndex, type.getValue(), Types.BINARY);
			} else if (type.getValue() instanceof Serializable) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(type.getValue());
				oos.flush();
				oos.close();
				statement.setObject(parameterIndex, baos.toByteArray(), Types.BINARY);
			}
		} else if (parameter instanceof DateType) {
			DateType dateType = (DateType) parameter;
			Date date = (Date) dateType.getValue();
			statement.setDate(parameterIndex, new java.sql.Date(date.getTime()));
		} else if (parameter instanceof DateTimeType) {
			DateTimeType dateType = (DateTimeType) parameter;
			Date date = (Date) dateType.getValue();
			statement.setTimestamp(parameterIndex, new Timestamp(date.getTime()));
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
		StringBuffer msg = new StringBuffer(causeMessage);

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
		StringBuffer msg = new StringBuffer(causeMessage);

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
		StringBuffer msg = new StringBuffer(causeMessage);

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

	protected void close(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void close(Statement statement) {
		try {
			if (statement != null)
				statement.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void close(NamedParameterStatement statement) {
		try {
			if (statement != null)
				statement.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void close(ResultSet resultSet) {
		try {
			if (resultSet != null)
				resultSet.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * ABSTRACT METHODS
	 */

	public abstract int[] batch(Connection conn, String sql, Object[][] params) throws Exception;

	public abstract int[] batch(String sql, Object[][] params) throws Exception;

	public abstract Object query(Connection connection, String sql, ResultSetHandler resultSetHandler,
			Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(Connection connection, String sql, ResultSetHandler resultSetHandler,
			NamedParameter[] parameters, boolean showSql,
			boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract SQLSessionResult queryWithResultSet(Connection connection, String sql,
			ResultSetHandler resultSetHandler,
			NamedParameter[] parameters, boolean showSql, boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId)
			throws Exception;

	public abstract SQLSessionResult queryWithResultSet(Connection connection, String sql,
			ResultSetHandler resultSetHandler, Object[] parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId)
			throws Exception;

	public abstract Object query(Connection connection, String sql, ResultSetHandler resultSetHandler,
			Map<String, Object> parameters,
			boolean showSql, boolean formatSql, int timeOut, List<SQLSessionListener> listeners, String clientId)
			throws Exception;

	public abstract Object queryProcedure(SQLSession session, DatabaseDialect dialect, CallableType type, String name,
			ResultSetHandler resultSetHandler, Object[] inputParameters, String[] outputParametersName,
			boolean showSql, int timeOut, String clientId)
			throws Exception;

	public abstract int[] getOutputSqlTypesByProcedure(Connection connection, DatabaseDialect dialect,
			String procedureName) throws Exception;

	public abstract ProcedureResult executeProcedure(SQLSession session, DatabaseDialect dialect, CallableType type,
			String name,
			Object[] inputParameters, String[] outputParametersName, boolean showSql, int timeOut, String clientId)
			throws Exception;

	public abstract Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql,
			boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(Connection conn, String sql, ResultSetHandler resultSetHandler, boolean showSql,
			boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler rsh, Object[] parameters, boolean showSql,
			boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler resultSetHandler, Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract Object query(String sql, ResultSetHandler resultSetHandler, boolean showSql, boolean formatSql,
			int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters,
			boolean showSql, boolean formatSql,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, boolean showSql, boolean formatSql,
			int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, NamedParameter[] parameters,
			boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, Object[] parameters, boolean showSql,
			boolean formatSql, int timeOut,
			List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract ResultSet executeQuery(Connection connection, String sql, Map<String, Object> parameters,
			boolean showSql, boolean formatSql,
			int timeOut, List<SQLSessionListener> listeners, String clientId) throws Exception;

	public abstract int update(Connection connection, String sql, Object[] parameters,
			IdentifierPostInsert identifierPostInsert,
			String identitySelectString, boolean showSql, List<SQLSessionListener> listeners, String clientId)
			throws Exception;

	public abstract int update(Connection connection, String sql, NamedParameter[] parameters, boolean showSql,
			List<SQLSessionListener> listeners,
			String clientId) throws Exception;

	public abstract int update(Connection connection, String sql, NamedParameter[] parameters,
			IdentifierPostInsert identifierPostInsert,
			String identitySelectString, boolean showSql, List<SQLSessionListener> listeners, String clientId)
			throws Exception;

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
