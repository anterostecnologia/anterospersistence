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
package br.com.anteros.persistence.sql.dialect;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ResourceUtils;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.H2Templates;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;

public class H2Dialect extends DatabaseDialect {

	private static Logger log = LoggerProvider.getInstance().getLogger(H2Dialect.class.getName());

	public H2Dialect() {
		super();
		initializeTypes();
	}

	public H2Dialect(String defaultCatalog, String defaultSchema) {
		super(defaultCatalog, defaultSchema);
		initializeTypes();
	}

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BOOLEAN", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("DOUBLE", false));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("DOUBLE", false));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMERIC", 38));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 38).setLimits(38, -19, 19));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 38).setLimits(38, -19, 19));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("LONGVARBINARY", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("LONGVARCHAR", false));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("LONGVARBINARY", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("LONGVARCHAR", false));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("CLOB", false));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false));
	}

	@Override
	public String getIdentitySelectString() {
		return "CALL IDENTITY()";
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public String getSelectForUpdateString() {
		return " FOR UPDATE";
	}

	@Override
	public CallableStatement prepareCallableStatement(Connection connection, CallableType type, String name,
			Object[] inputParameters, String[] outputParametersName, int[] outputTypes, int queryTimeOut,
			boolean showSql, String clientId) throws Exception {
		int i;
		int numberInputParameters = (inputParameters == null ? 0 : inputParameters.length);
		StringBuffer sql = null;
		if (type == CallableType.PROCEDURE) {
			sql = new StringBuffer("{ call ");
		} else {
			sql = new StringBuffer("{? = call ");
		}

		sql.append(name).append("(");
		boolean append = false;
		if (inputParameters != null) {
			for (i = 0; i < inputParameters.length; i++) {
				if (append)
					sql.append(", ");
				sql.append("?");
				append = true;
			}
		}

		if (outputParametersName != null) {
			if (type == CallableType.FUNCTION) {
				for (i = 1; i < outputParametersName.length; i++) {
					if (append)
						sql.append(", ");
					sql.append("?");
					append = true;
				}
			} else {
				for (i = 0; i < outputParametersName.length; i++) {
					if (append)
						sql.append(", ");
					sql.append("?");
					append = true;
				}
			}
		}

		sql.append(") }");

		if (showSql) {
			log.debug(ResourceUtils.getMessage(H2Dialect.class, "showSql", sql.toString(), clientId));
			if (inputParameters != null) {
				log.debug(ResourceUtils.getMessage(H2Dialect.class, "showSql1", clientId));
				for (Object parameter : inputParameters)
					log.debug("        " + parameter + " ##" + clientId);
			}
			if (outputParametersName != null) {
				log.debug(ResourceUtils.getMessage(H2Dialect.class, "showSql2", clientId));
				for (String opt : outputParametersName)
					log.debug("        " + opt + " ##" + clientId);
			}
		}

		CallableStatement call = (CallableStatement) connection.prepareCall(sql.toString());

		if (type == CallableType.FUNCTION) {
			if (outputTypes.length > 0)
				call.registerOutParameter(1, outputTypes[0]);

			for (i = 1; i < outputTypes.length; i++)
				call.registerOutParameter(numberInputParameters + i + 2, outputTypes[i]);
		} else {
			for (i = 0; i < outputTypes.length; i++)
				call.registerOutParameter(numberInputParameters + i + 1, outputTypes[i]);
		}

		setParametersCallableStatement(call, type, inputParameters);
		return call;
	}

	@Override
	public String name() {
		return "H2 Database";
	}

	@Override
	public boolean supportInCondition() {
		return true;
	}

	@Override
	public Blob createTemporaryBlob(Connection connection, byte[] bytes) throws Exception {
		Blob blob = connection.createBlob();
		blob.setBytes(1, bytes);
		return blob;
	}

	@Override
	public Clob createTemporaryClob(Connection connection, byte[] bytes) throws Exception {
		Clob clob = connection.createClob();
		clob.setString(1, new String(bytes));
		return clob;
	}

	@Override
	public boolean supportsPrimaryKeyConstraintOnTableCreate() {
		return true;
	}

	@Override
	public boolean requiresNamedPrimaryKeyConstraints() {
		return false;
	}

	@Override
	public boolean requiresUniqueConstraintCreationOnTableCreate() {
		return false;
	}

	@Override
	public String getDefaultTableCreateSuffix() {
		return "";
	}

	@Override
	public boolean supportsUniqueKeyConstraints() {
		return true;
	}

	@Override
	public boolean supportsForeignKeyConstraints() {
		return true;
	}

	@Override
	public int getMaxColumnNameSize() {
		return 30;
	}

	@Override
	public int getMaxForeignKeyNameSize() {
		return 30;
	}

	@Override
	public int getMaxIndexKeyNameSize() {
		return 30;
	}

	@Override
	public int getMaxUniqueKeyNameSize() {
		return 30;
	}

	@Override
	public boolean supportsDeleteOnCascade() {
		return true;
	}

	@Override
	public boolean supportsIdentity() {
		return true;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		return new StringBuilder(20 + sequenceName.length()).append("CALL NEXT VALUE FOR ").append(sequenceName)
				.toString();
	}

	@Override
	public boolean checkSequenceExists(Connection conn, String sequenceName) throws SQLException, Exception {
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = '"
						+ sequenceName.toUpperCase() + "'");

		if (resultSet.next()) {
			return true;
		}

		statement = conn.createStatement();
		statement
				.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = '"
						+ sequenceName + "'");

		if (resultSet.next()) {
			return true;
		}

		return false;
	}

	@Override
	public String[] getColumnNamesFromTable(Connection conn, String tableName) throws SQLException, Exception {
		List<String> result = new ArrayList<String>();
		Statement statement = conn.createStatement();
		ResultSet columns = statement
				.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME =  '" + tableName
						+ "' ORDER BY ORDINAL_POSITION");

		while (columns.next()) {
			result.add(columns.getString("COLUMN_NAME"));
		}

		return result.toArray(new String[] {});
	}

	@Override
	public void setConnectionClientInfo(Connection connection, String clientInfo) throws SQLException {
	}

	@Override
	public String getConnectionClientInfo(Connection connection) throws SQLException {
		return "";
	}

	@Override
	public SQLTemplates getTemplateSQL() {
		return new H2Templates();
	}

}
