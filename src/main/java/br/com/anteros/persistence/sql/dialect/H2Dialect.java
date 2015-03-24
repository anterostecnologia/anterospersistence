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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.H2Templates;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.exception.ConstraintViolationException;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockAcquisitionException;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.lock.PessimisticLockException;
import br.com.anteros.persistence.sql.dialect.type.LimitClauseResult;

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

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BOOLEAN", false, Types.BOOLEAN));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("DOUBLE", false, Types.DOUBLE));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("DOUBLE", false, Types.DOUBLE));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMERIC", 38, Types.NUMERIC));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 38, Types.NUMERIC).setLimits(38, -19, 19));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 38, Types.NUMERIC).setLimits(38, -19, 19));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("LONGVARBINARY", false, Types.LONGVARBINARY));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("LONGVARCHAR", false, Types.LONGVARCHAR));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("LONGVARBINARY", false, Types.LONGVARBINARY));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("LONGVARCHAR", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("CLOB", false, Types.CLOB));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false, Types.DATE));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false, Types.TIME));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
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
		return new StringBuilder(20 + sequenceName.length()).append("CALL NEXT VALUE FOR ").append(sequenceName).toString();
	}

	@Override
	public boolean checkSequenceExists(Connection conn, String sequenceName) throws SQLException, Exception {
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = '"
				+ sequenceName.toUpperCase() + "'");

		if (resultSet.next()) {
			return true;
		}

		statement = conn.createStatement();
		resultSet = statement.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = '" + sequenceName + "'");

		if (resultSet.next()) {
			return true;
		}

		return false;
	}

	@Override
	public String[] getColumnNamesFromTable(Connection conn, String tableName) throws SQLException, Exception {
		List<String> result = new ArrayList<String>();
		Statement statement = conn.createStatement();
		ResultSet columns = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME =  '" + tableName
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

	@Override
	public SQLSessionException convertSQLException(SQLException ex, String msg, String sql) throws SQLSessionException {
		int errorCode = extractErrorCode(ex);

		if (40001 == errorCode) { // DEADLOCK DETECTED
			return new LockAcquisitionException(msg, ex, sql);
		}

		if (50200 == errorCode) { // LOCK NOT AVAILABLE
			return new PessimisticLockException(msg, ex, sql);
		}

		if (90006 == errorCode) {
			// NULL not allowed for column [90006-145]
			final String constraintName = extractConstraintName(ex);
			return new ConstraintViolationException(msg, ex, sql, constraintName);
		}

		return new SQLSessionException(msg, ex, sql);
	}

	public String extractConstraintName(SQLException ex) {
		String constraintName = null;
		if (ex.getSQLState().startsWith("23")) {
			final String message = ex.getMessage();
			int idx = message.indexOf("violation: ");
			if (idx > 0) {
				constraintName = message.substring(idx + "violation: ".length());
			}
		}
		return constraintName;
	}

	@Override
	public String applyLock(String sql, LockOptions lockOptions) {
		LockMode lockMode = lockOptions.getLockMode();
		switch (lockMode) {
		case PESSIMISTIC_READ:
			return sql + " FOR UPDATE " + (lockOptions.getTimeOut() == LockOptions.NO_WAIT ? " NOWAIT " : "");
		case PESSIMISTIC_WRITE:
		case PESSIMISTIC_FORCE_INCREMENT:
			return sql + " FOR UPDATE " + (lockOptions.getTimeOut() == LockOptions.NO_WAIT ? " NOWAIT" : "");
		default:
			return sql;
		}
	}

	@Override
	public String getSetLockTimeoutString(int secondsTimeOut) {
		return "SET LOCK_TIMEOUT " + secondsTimeOut * 1000;
	}

	@Override
	public LimitClauseResult getLimitClause(String sql, int offset, int limit, boolean namedParameter) {
		LimitClauseResult result;
		if (namedParameter) {
			result = new LimitClauseResult(new StringBuilder(sql.length() + 20).append(sql)
					.append(offset > 0 ? " LIMIT :PLIMIT OFFSET :POFFSET " : " LIMIT :PLIMIT").toString(), "PLIMIT", (offset > 0 ? "POFFSET" : ""),
					limit, offset);
		} else {
			result = new LimitClauseResult(new StringBuilder(sql.length() + 20).append(sql).append(offset > 0 ? " LIMIT ? OFFSET ?" : " LIMIT ?")
					.toString(), (offset > 0 ? LimitClauseResult.PREVIOUS_PARAMETER : LimitClauseResult.LAST_PARAMETER),
					(offset > 0 ? LimitClauseResult.LAST_PARAMETER : LimitClauseResult.NONE_PARAMETER), limit, offset);
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
		// TODO Auto-generated method stub
		return null;
	}

}
