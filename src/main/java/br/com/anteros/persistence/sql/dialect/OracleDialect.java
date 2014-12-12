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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.IOUtils;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.OracleTemplates;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.ColumnSchema;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;

public class OracleDialect extends DatabaseDialect {

	private static Logger log = LoggerProvider.getInstance().getLogger(OracleDialect.class.getName());

	public final static Object RAW_CONNECTION = new Object();
	public final static int ORACLE_BLOB = 1;
	public final static int ORACLE_CLOB = 2;

	private final String SET_CLIENT_INFO_SQL = "{call DBMS_APPLICATION_INFO.SET_CLIENT_INFO(?)}";
	private final String GET_CLIENT_INFO_SQL = "{call DBMS_APPLICATION_INFO.READ_CLIENT_INFO (?)}";;

	public OracleDialect() {
		super();
	}

	public OracleDialect(String defaultCatalog, String defaultSchema) {
		super(defaultCatalog, defaultSchema);
		if ((defaultSchema == null) || ("".equals(defaultSchema)))
			throw new DatabaseDialectException("Informe o nome do Schema nas configurações para o dialeto do Oracle.");
	}

	protected void initializeTypes() {
		super.initializeTypes();
		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("NUMBER(1) default 0", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("NUMBER", 10));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("NUMBER", 19));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("NUMBER", 19, 4));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("NUMBER", 19, 4));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("NUMBER", 5));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("NUMBER", 3));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMBER", 38));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMBER", 38).setLimits(38, -38, 38));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMBER", 38).setLimits(38, -38, 38));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR2", DEFAULT_VARCHAR_SIZE));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("CHAR", 1));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("CLOB", false));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("CLOB", false));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("CLOB", false));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("DATE", false));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP"));
	}

	@Override
	public String getIdentitySelectString() {
		return "";
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
			log.debug("Sql-> " + sql.toString() + " ##" + clientId);
			if (inputParameters != null) {
				log.debug("      Input parameters->" + " ##" + clientId);
				for (Object parameter : inputParameters)
					log.debug("        " + parameter + " ##" + clientId);
			}
			if (outputParametersName != null) {
				log.debug("      Output parameters->" + " ##" + clientId);
				for (String opt : outputParametersName)
					log.debug("        " + opt + " ##" + clientId);
			}
		}

		CallableStatement call = (CallableStatement) connection.prepareCall(sql.toString());
		if (queryTimeOut > 0)
			call.setQueryTimeout(queryTimeOut);

		if (type == CallableType.FUNCTION) {
			if (outputTypes.length > 0)
				call.registerOutParameter(1, outputTypes[0]);

			for (i = 1; i < outputTypes.length; i++)
				call.registerOutParameter(numberInputParameters + i + 2, outputTypes[i]);
		} else {
			for (i = 0; i < outputTypes.length; i++)
				call.registerOutParameter(numberInputParameters + i + 1, outputTypes[i]);
		}

		if (inputParameters!=null)
		setParametersCallableStatement(call, type, inputParameters);
		return call;
	}

	/*
	 * public List<ProcedureMetadata> getNativeFunctions() throws Exception { //
	 * http://ss64.com/ora/syntax-functions.html List<ProcedureMetadata> result
	 * = new ArrayList<ProcedureMetadata>();
	 * 
	 * return result; }
	 */

	@Override
	public String name() {
		return "Oracle";
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		return "select " + sequenceName + ".nextval from dual";
	}

	@Override
	public boolean supportInCondition() {
		return true;
	}

	@Override
	public Blob createTemporaryBlob(Connection connection, byte[] bytes) throws Exception {
		return (Blob) createTemporaryLob(connection, new ByteArrayInputStream(bytes), ORACLE_BLOB,
				"getBinaryOutputStream");
	}

	@Override
	public Clob createTemporaryClob(Connection connection, byte[] bytes) throws Exception {
		return (Clob) createTemporaryLob(connection, new ByteArrayInputStream(bytes), ORACLE_CLOB,
				"getAsciiOutputStream");
	}

	protected Object createTemporaryLob(Connection connection, InputStream in, int lobType, String methodOutputStream)
			throws Exception {
		Class<?> lobClass = null;
		if (lobType == ORACLE_BLOB)
			lobClass = connection.getClass().getClassLoader().loadClass("oracle.sql.BLOB");
		else
			lobClass = connection.getClass().getClassLoader().loadClass("oracle.sql.CLOB");

		Integer durationSessionConstant = new Integer(lobClass.getField("DURATION_SESSION").getInt(null));
		Integer modeReadWriteConstant = new Integer(lobClass.getField("MODE_READWRITE").getInt(null));
		Object result = null;
		Class<?> c3p0ConnectionClass = null;
		Class<?> cp30OracleUtilsClass = null;
		try {
			c3p0ConnectionClass = connection.getClass().getClassLoader()
					.loadClass("com.mchange.v2.c3p0.C3P0ProxyConnection");
			cp30OracleUtilsClass = connection.getClass().getClassLoader()
					.loadClass("com.mchange.v2.c3p0.dbms.OracleUtils");
		} catch (Exception e) {
		}
		if ((cp30OracleUtilsClass != null) && (c3p0ConnectionClass != null)
				&& (ReflectionUtils.isImplementsInterface(connection.getClass(), c3p0ConnectionClass))) {
			Method m = null;
			if (lobType == ORACLE_BLOB)
				m = ReflectionUtils.findMethodObject(cp30OracleUtilsClass, "createTemporaryBLOB");
			else
				m = ReflectionUtils.findMethodObject(cp30OracleUtilsClass, "createTemporaryCLOB");
			result = m.invoke(null, new Object[] { connection, Boolean.valueOf(true), 10 });
		} else {
			Method createTemporary = lobClass.getMethod("createTemporary", new Class[] { Connection.class,
					Boolean.TYPE, Integer.TYPE });
			result = createTemporary.invoke(null, connection, true, durationSessionConstant);
		}
		Method open = lobClass.getMethod("open", new Class[] { Integer.TYPE });
		open.invoke(result, new Object[] { modeReadWriteConstant });

		Method getBinaryOutputStream = lobClass.getMethod(methodOutputStream, new Class[0]);
		OutputStream out = (OutputStream) getBinaryOutputStream.invoke(result, null);
		try {
			IOUtils.copy(in, out);
		} finally {
			try {
				out.flush();
			} catch (IOException ioe) {
			}
			out.close();
		}
		Method close = lobClass.getMethod("close", new Class[0]);
		close.invoke(result, null);
		return result;
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
		return false;
	}

	@Override
	public void setConnectionClientInfo(Connection connection, String clientInfo) throws SQLException {
		PreparedStatement prep = connection.prepareStatement(SET_CLIENT_INFO_SQL);
		try {
			prep.setString(1, clientInfo);
			prep.execute();
		} finally {
			prep.close();
		}
	}

	@Override
	public String getConnectionClientInfo(Connection connection) throws SQLException {
		CallableStatement stmt = connection.prepareCall(GET_CLIENT_INFO_SQL);
		try {
			stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
			stmt.execute();
			return stmt.getString(1);
		} finally {
			stmt.close();
		}
	}
	
	@Override
	public Map<String, IndexMetadata> getAllIndexesByTable(Connection conn, String tableName) throws Exception {
		Map<String, IndexMetadata> indexes = new HashMap<String, IndexMetadata>();
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT I.INDEX_NAME, IC.COLUMN_POSITION, IC.COLUMN_NAME, I.UNIQUENESS  FROM USER_INDEXES I  JOIN USER_IND_COLUMNS IC    ON I.INDEX_NAME = IC.INDEX_NAME WHERE I.TABLE_NAME = '"
						+ tableName.toUpperCase() + "' ORDER BY I.INDEX_NAME, IC.COLUMN_POSITION ");
		try {
			IndexMetadata index = null;
			while (resultSet.next()) {
				if (resultSet.getString("COLUMN_NAME") != null) {
					if (indexes.containsKey(resultSet.getString("INDEX_NAME")))
						index = indexes.get(resultSet.getString("INDEX_NAME"));
					else {
						index = new IndexMetadata(resultSet.getString("INDEX_NAME"));
						indexes.put(index.indexName, index);
					}
					index.unique = ("UNIQUE".equals(resultSet.getString("UNIQUENESS")));
					index.addColumn(resultSet.getString("COLUMN_NAME"));
				}
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}
		return indexes;
	}

	@Override
	public Map<String, IndexMetadata> getAllUniqueIndexesByTable(Connection conn, String tableName) throws Exception {
		Map<String, IndexMetadata> indexes = new HashMap<String, IndexMetadata>();
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT I.INDEX_NAME, IC.COLUMN_POSITION, IC.COLUMN_NAME, I.UNIQUENESS  FROM USER_INDEXES I  JOIN USER_IND_COLUMNS IC    ON I.INDEX_NAME = IC.INDEX_NAME WHERE I.TABLE_NAME = '"
						+ tableName.toUpperCase()
						+ "' AND I.UNIQUENESS = 'UNIQUE' ORDER BY I.INDEX_NAME, IC.COLUMN_POSITION ");
		try {
			IndexMetadata index = null;
			while (resultSet.next()) {
				if (resultSet.getString("COLUMN_NAME") != null) {
					if (indexes.containsKey(resultSet.getString("INDEX_NAME")))
						index = indexes.get(resultSet.getString("INDEX_NAME"));
					else {
						index = new IndexMetadata(resultSet.getString("INDEX_NAME"));
						indexes.put(index.indexName, index);
					}
					index.unique = ("UNIQUE".equals(resultSet.getString("UNIQUENESS")));
					index.addColumn(resultSet.getString("COLUMN_NAME"));
				}
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}
		return indexes;
	}

	@Override
	public boolean checkUniqueKeyExists(Connection conn, String tableName, String uniqueKeyName) throws Exception {
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT I.INDEX_NAME FROM USER_INDEXES I  WHERE I.INDEX_NAME = '"
				+ uniqueKeyName.toUpperCase() + "' AND I.UNIQUENESS = 'UNIQUE' ");
		try {
			if (resultSet.next()) {
				return true;
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}

		return false;
	}

	@Override
	public boolean checkIndexExistsByName(Connection conn, String tableName, String indexName) throws Exception {
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT I.INDEX_NAME FROM USER_INDEXES I  WHERE I.INDEX_NAME = '"
				+ indexName.toUpperCase() + "' ");
		try {
			if (resultSet.next()) {
				return true;
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}

		return false;
	}

	@Override
	public boolean checkForeignKeyExistsByName(Connection conn, String tableName, String foreignKeyName)
			throws Exception {
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT UC.CONSTRAINT_NAME  FROM USER_CONSTRAINTS UC WHERE UC.TABLE_NAME = '"
						+ tableName.toUpperCase() + "' AND   UC.CONSTRAINT_NAME = '" + foreignKeyName.toUpperCase()
						+ "' AND  UC.CONSTRAINT_TYPE = 'R'");
		try {
			if (resultSet.next()) {
				return true;
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}
		return false;
	}

	@Override
	public Map<String, ForeignKeyMetadata> getAllForeignKeysByTable(Connection conn, String tableName) throws Exception {

		Map<String, ForeignKeyMetadata> fks = new HashMap<String, ForeignKeyMetadata>();
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT A.CONSTRAINT_NAME, A.TABLE_NAME, A.COLUMN_NAME FROM USER_CONS_COLUMNS A  JOIN USER_CONSTRAINTS C ON A.OWNER = C.OWNER  AND A.CONSTRAINT_NAME = C.CONSTRAINT_NAME WHERE C.CONSTRAINT_TYPE = 'R' AND C.TABLE_NAME = '"
						+ tableName.toUpperCase() + "'");
		
		
		try {
			ForeignKeyMetadata fk = null;
			while (resultSet.next()) {
				if (fks.containsKey(resultSet.getString("CONSTRAINT_NAME")))
					fk = fks.get(resultSet.getString("CONSTRAINT_NAME"));
				else {
					fk = new ForeignKeyMetadata(resultSet.getString("CONSTRAINT_NAME"));
					fks.put(fk.fkName, fk);
				}

				fk.addColumn(resultSet.getString("COLUMN_NAME"));
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}

		return fks;
	}
	

	@Override
	public String getSchema(DatabaseMetaData metadata) throws Exception {
		return metadata.getUserName();
	}
	
	@Override
	public Writer writeColumnDDLStatement(ColumnSchema columnSchema, Writer schemaWriter) throws Exception {
		/*
		 * Nome da coluna
		 */
		schemaWriter.write(getQuoted(columnSchema.getName()));
		schemaWriter.write(" ");

		if (!StringUtils.isEmpty(columnSchema.getColumnDefinition())) {
			/*
			 * Tipo definido pelo usuário
			 */
			schemaWriter.write(columnSchema.getColumnDefinition());
		} else {

			if (columnSchema.isAutoIncrement() && supportsIdentity() && !useColumnDefinitionForIdentity()
					&& !columnSchema.hasSequenceName()) {
				writeColumnIdentityClauseDDLStatement(schemaWriter);
			} else {
				/*
				 * Tipo SQL
				 */
				schemaWriter.write(columnSchema.getTypeSql());
				/*
				 * Tamanho ou Precisão/Escala
				 */
				if (columnSchema.getSize() > 0) {
					schemaWriter.write("(" + columnSchema.getSize());
					if (columnSchema.getSubSize() > 0)
						schemaWriter.write("," + columnSchema.getSubSize());
					schemaWriter.write(")");
				}
				

				/*
				 * Se suporta sequence como default value
				 */
				if (supportsSequenceAsADefaultValue() && columnSchema.hasSequenceName()) {
					writeColumnSequenceDefaultValue(schemaWriter, columnSchema.getSequenceName());
				}

				/*
				 * Comentário da coluna
				 */
				// FALTA VER COMO FAZER

				/*
				 * Se a coluna possuí um valor default
				 */
				if ((columnSchema.getDefaultValue() != null) && (!"".equals(columnSchema.getDefaultValue())))
					schemaWriter.write(" DEFAULT " + columnSchema.getDefaultValue());

				/*
				 * Se a coluna for not null
				 */
				if (columnSchema.getNullable())
					writeColumnNullClauseDDLStatement(schemaWriter);
				else
					writeColumnNotNullClauseDDLStatement(schemaWriter);

				/*
				 * Se a coluna for de Identidade (auto incremento)
				 */
				if (columnSchema.isAutoIncrement() && supportsIdentity() && !columnSchema.hasSequenceName()) {
					writeColumnIdentityClauseDDLStatement(schemaWriter);
				}

			}
		}
		return schemaWriter;
	}

	@Override
	public SQLTemplates getTemplateSQL() {
		return new OracleTemplates();
	}



}
