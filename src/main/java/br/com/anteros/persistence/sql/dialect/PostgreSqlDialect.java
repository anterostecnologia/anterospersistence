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

import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Map;

import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.PostgresTemplates;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockAcquisitionException;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.lock.PessimisticLockException;
import br.com.anteros.persistence.sql.dialect.type.LimitClauseResult;

public class PostgreSqlDialect extends DatabaseDialect {

	private final String SET_CLIENT_INFO_SQL = "SET application_name = '?'";
	private final String GET_CLIENT_INFO_SQL = "SHOW application_name";

	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BOOLEAN", false, Types.BOOLEAN));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("FLOAT", false, Types.FLOAT));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("FLOAT", false, Types.FLOAT));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("DECIMAL", 38, Types.DECIMAL));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("DECIMAL", 38, Types.DECIMAL));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE, Types.VARCHAR));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("CHAR", 1, Types.CHAR));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BYTEA", false, Types.NUMERIC));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BYTEA", false,Types.VARBINARY));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("TEXT", false,Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BYTEA",Types.VARBINARY));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("TEXT", false,Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false, Types.TIME));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
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

	@Override
	public SQLTemplates getTemplateSQL() {
		return new PostgresTemplates();
	}

	@Override
	public SQLSessionException convertSQLException(SQLException ex, String msg, String sql) throws SQLSessionException {
		final String sqlState = extractSqlState(ex);

		if ("40P01".equals(sqlState)) { // DEADLOCK DETECTED
			return new LockAcquisitionException(msg, ex, sql);
		}

		if ("55P03".equals(sqlState)) { // LOCK NOT AVAILABLE
			return new PessimisticLockException(msg, ex, sql);
		}

		return new SQLSessionException(msg, ex, sql);
	}

	@Override
	public String extractConstraintName(SQLException sqle) {
		try {
			int sqlState = Integer.valueOf(extractSqlState(sqle)).intValue();
			switch (sqlState) {
			case 23514:
				return extractUsingTemplate("violates check constraint \"", "\"", sqle.getMessage());
			case 23505:
				return extractUsingTemplate("violates unique constraint \"", "\"", sqle.getMessage());
			case 23503:
				return extractUsingTemplate("violates foreign key constraint \"", "\"", sqle.getMessage());
			case 23502:
				return extractUsingTemplate("null value in column \"", "\" violates not-null constraint", sqle.getMessage());
			default:
				return null;
			}
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	@Override
	public String applyLock(String sql, LockOptions lockOptions) {
		LockMode lockMode = lockOptions.getLockMode();
		switch (lockMode) {
		case PESSIMISTIC_READ:
			return sql + " FOR SHARE " + (lockOptions.getTimeOut() == LockOptions.NO_WAIT ? " NOWAIT " : "");
		case PESSIMISTIC_WRITE:
		case PESSIMISTIC_FORCE_INCREMENT:
			return sql + " FOR UPDATE " + (lockOptions.getTimeOut() == LockOptions.NO_WAIT ? " NOWAIT" : "");
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
					.append(offset > 0 ? " LIMIT :PLIMIT OFFSET :POFFSET " : " LIMIT :PLIMIT").toString(), "PLIMIT", (offset > 0 ? "POFFSET" : ""),limit, offset);
		} else {
			result = new LimitClauseResult(new StringBuilder(sql.length() + 20).append(sql).append(offset > 0 ? " LIMIT ? OFFSET ?" : " LIMIT ?")
					.toString(), (offset > 0 ? LimitClauseResult.PREVIOUS_PARAMETER : LimitClauseResult.LAST_PARAMETER),
					(offset > 0 ? LimitClauseResult.LAST_PARAMETER : LimitClauseResult.NONE_PARAMETER),limit, offset);
		}
		return result;
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
