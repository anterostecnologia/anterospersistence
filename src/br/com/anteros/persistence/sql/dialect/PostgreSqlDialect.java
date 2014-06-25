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

import java.io.Writer;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;

public class PostgreSqlDialect extends DatabaseDialect {

	private final String SET_CLIENT_INFO_SQL = "SET application_name = ''{0}''";
	private final String GET_CLIENT_INFO_SQL = "SHOW application_name";

	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BOOLEAN", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("FLOAT", false));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("FLOAT", false));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("BIGINT", false));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("DECIMAL", 38));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("DECIMAL", 38));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("CHAR", 1));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BYTEA", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BYTEA", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BYTEA"));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false));
	}

	@Override
	public String getIdentitySelectString() {
		return "SELECT LASTVAL()";
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		return "SELECT NEXTVAL(\'" + sequenceName + "\')";
	}

	@Override
	public String getSelectForUpdateString() {
		return " FOR UPDATE";
	}

	@Override
	public CallableStatement prepareCallableStatement(Connection connection, CallableType type, String name,
			Object[] inputParameters,
			String[] outputParametersName, int[] outputTypes, int queryTimeOut, boolean showSql, String clientId)
			throws Exception {
		return null;
	}

	@Override
	public String name() {
		return "Postgresql";
	}

	@Override
	public boolean supportInCondition() {
		return true;
	}

	@Override
	public Blob createTemporaryBlob(Connection connection, byte[] bytes) throws Exception {
		return null;
	}

	@Override
	public Clob createTemporaryClob(Connection connection, byte[] bytes) throws Exception {
		return null;
	}

	@Override
	public boolean supportsPrimaryKeyConstraintOnTableCreate() {
		return true;
	}

	@Override
	public boolean requiresNamedPrimaryKeyConstraints() {
		return true;
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
		return 31;
	}

	@Override
	public int getMaxForeignKeyNameSize() {
		return 31;
	}

	@Override
	public int getMaxIndexKeyNameSize() {
		return 31;
	}

	@Override
	public int getMaxUniqueKeyNameSize() {
		return 31;
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
	public Writer writeColumnIdentityClauseDDLStatement(Writer schemaWriter) throws Exception {
		schemaWriter.write(" SERIAL");
		return schemaWriter;
	}
	
	public boolean useColumnDefinitionForIdentity() {
		return false;
	}
	
	public boolean supportsSequenceAsADefaultValue() {
		return true;
	}
	
	public Writer writeColumnSequenceDefaultValue(Writer schemaWriter, String sequenceName) throws Exception {
		schemaWriter.write(" DEFAULT nextval('" + sequenceName + "') ");
		return schemaWriter;
	}

	@Override
	public void setConnectionClientInfo(Connection connection, String clientInfo) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(GET_CLIENT_INFO_SQL);
		try {
			ResultSet rs = stmt.executeQuery();
			try {
				if (rs.next()) {
					String applicationName = rs.getString(1);
					if (applicationName.contains("#")) {
						applicationName = applicationName.split("#")[0].trim();
					}
					clientInfo = applicationName + " # " + clientInfo;
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}

		PreparedStatement prep = connection.prepareStatement(MessageFormat.format(SET_CLIENT_INFO_SQL, clientInfo));
		try {
			prep.execute();
		} finally {
			prep.close();
		}
	}
 
	@Override
	public String getConnectionClientInfo(Connection connection) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(GET_CLIENT_INFO_SQL);
		try {
			ResultSet rs = stmt.executeQuery();
			try {
				if (!rs.next())
					return "";

				String applicationName = rs.getString(1);

				if (applicationName != null) {
					String[] tokens = applicationName.split("#");
					if (tokens.length > 1)
						return tokens[1].trim();
				}
				return "";

			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}
}
