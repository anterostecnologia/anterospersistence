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
package br.com.anteros.persistence.sql.dialect;

import java.io.IOException;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.FirebirdTemplates;
import br.com.anteros.persistence.schema.definition.SequenceGeneratorSchema;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.sql.dialect.type.LimitClauseResult;

public class FirebirdDialect extends DatabaseDialect {

	private final String GET_CLIENT_INFO_SQL = "SELECT " + "    rdb$get_context('USER_SESSION', ?) session_context "
			+ "  , rdb$get_context('USER_TRANSACTION', ?) tx_context " + "FROM rdb$database";

	private final String SET_CLIENT_INFO_SQL = "SELECT rdb$set_context('USER_SESSION', ?, ?) session_context " + "FROM rdb$database";

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("NUMERIC", 18, Types.NUMERIC).setLimits(18, -18, 18));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("FLOAT", false, Types.FLOAT));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("DOUBLE PRECISION", false, Types.DOUBLE));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMERIC", 18, Types.NUMERIC).setLimits(18, -18, 18));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 18, Types.NUMERIC).setLimits(18, -18, 18));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 38, Types.NUMERIC).setLimits(18, -18, 18));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE, Types.VARCHAR));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("VARCHAR", 1, Types.VARCHAR));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("VARCHAR", 32000, Types.VARCHAR));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("VARCHAR", 32000, Types.VARCHAR));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("VARCHAR", 32000, Types.VARCHAR));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false, Types.DATE));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false, Types.TIME));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		
	}

	@Override
	public String getIdentitySelectString() {
		return null;
	}

	@Override
	public String name() {
		return "Firebird";
	}

	@Override
	public boolean supportInCondition() {
		return false;
	}

	@Override
	public boolean supportsPrimaryKeyConstraintOnTableCreate() {
		return false;
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
	public int getMaxUniqueKeyNameSize() {
		return 31;
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
	public boolean supportsDeleteOnCascade() {
		return false;
	}

	@Override
	public String getIdentifierQuoteCharacter() {
		return "";
	}

	@Override
	public boolean supportsIdentity() {
		return false;
	}

	@Override
	public boolean requiresTableInIndexDropDDL() {
		return false;
	}

	@Override
	public char getCloseQuote() {
		return '\'';
	}

	@Override
	public char getOpenQuote() {
		return '\'';
	}

	@Override
	public Writer writeColumnIdentityClauseDDLStatement(Writer schemaWriter) throws Exception {
		return schemaWriter;
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		StringBuilder builder = new StringBuilder(26 + sequenceName.length());
		builder.append("SELECT GEN_ID(");
		builder.append(sequenceName);
		builder.append(", ");
		builder.append(1);
		builder.append(") FROM RDB$DATABASE");
		return builder.toString();
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
	public Writer writeCreateSequenceDDLStatement(SequenceGeneratorSchema sequenceGeneratorSchema, Writer schemaWriter) throws IOException {
		schemaWriter.write("CREATE GENERATOR ");
		schemaWriter.write(sequenceGeneratorSchema.getName());
		return schemaWriter;
	}

	@Override
	public Writer writeDropSequenceDDLStatement(SequenceGeneratorSchema sequenceGeneratorSchema, Writer schemaWriter) throws IOException {
		schemaWriter.write("DROP GENERATOR ");
		schemaWriter.write(sequenceGeneratorSchema.getName());
		return schemaWriter;
	}

	@Override
	public void setConnectionClientInfo(Connection connection, String clientInfo) throws SQLException {

		PreparedStatement stmt = connection.prepareStatement(SET_CLIENT_INFO_SQL);
		try {
			setClientInfo(stmt, CLIENT_INFO, clientInfo);
		} finally {
			stmt.close();
		}
	}

	/**
	 * Executa o SQL que define variáveis da sessão do banco de dados com informações do cliente.
	 * 
	 * @param stmt
	 * @param name
	 * @param value
	 * @throws SQLException
	 */
	private void setClientInfo(PreparedStatement stmt, String name, String value) throws SQLException {
		stmt.clearParameters();
		stmt.setString(1, name);
		stmt.setString(2, value);
		ResultSet rs = stmt.executeQuery();
		try {
			if (!rs.next())
				throw new SQLException("Expected result from RDB$SET_CONTEXT call");

			// needed, since the value is set on fetch!!!
			rs.getInt(1);

		} finally {
			rs.close();
		}
	}

	@Override
	public String getConnectionClientInfo(Connection connection) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(GET_CLIENT_INFO_SQL);
		try {
			stmt.setString(1, CLIENT_INFO);
			stmt.setString(2, CLIENT_INFO);

			ResultSet rs = stmt.executeQuery();
			try {
				if (!rs.next())
					return "";

				String sessionContext = rs.getString(1);
				String transactionContext = rs.getString(2);

				if (transactionContext != null)
					return transactionContext;
				else if (sessionContext != null)
					return sessionContext;
				else
					return "";

			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}

	@Override
	public SQLTemplates getTemplateSQL() {
		return new FirebirdTemplates();
	}

	@Override
	public SQLSessionException convertSQLException(SQLException ex, String msg, String sql) throws SQLSessionException {
		return new SQLSessionException(msg, ex, sql);
	}

	@Override
	public String applyLock(String sql, LockOptions lockOptions) {
		LockMode lockMode = lockOptions.getLockMode();
		switch (lockMode) {
		case PESSIMISTIC_READ:
			return sql + " WITH LOCK ";
		case PESSIMISTIC_WRITE:
		case PESSIMISTIC_FORCE_INCREMENT:
			return sql + " WITH LOCK ";
		default:
			return sql;
		}
	}

	@Override
	public String getSetLockTimeoutString(int secondsTimeOut) {
		return null;
	}

	@Override
	public LimitClauseResult getLimitClause(String sql, int offset, int limit, boolean namedParameter) {
		LimitClauseResult result;
		if (namedParameter) {
			result = new LimitClauseResult(new StringBuilder(sql.length() + 20).append(sql)
					.append(offset > 0 ? " FIRST :PLIMIT SKIP :POFFSET " : " FIRST :PLIMIT").toString(), "PLIMIT", (offset > 0 ? "POFFSET" : ""), limit, offset);
		} else {
			result = new LimitClauseResult(new StringBuilder(sql.length() + 20).append(sql).append(offset > 0 ? " LIMIT ? OFFSET ?" : " LIMIT ?")
					.toString(), LimitClauseResult.FIRST_PARAMETER, (offset > 0 ? LimitClauseResult.SECOND_PARAMETER
					: LimitClauseResult.NONE_PARAMETER),limit, offset);
		}
		return result;
	}

	public boolean bindLimitParametersFirst() {
		return true;
	}

	public boolean bindLimitParametersInReverseOrder() {
		return true;
	}

	@Override
	public String getIndexHint(String indexName, String alias) {
		return "";
	}

	@Override
	public Position getIndexHintPosition() {
		return Position.AFTER_SELECT;
	}

	@Override
	public String getIndexHint(Map<String, String> indexes) {
		return "";
	}
}
