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
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.SQLiteTemplates;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.sql.dialect.type.LimitClauseResult;

public class SQLiteDialect extends DatabaseDialect {

	public SQLiteDialect() {
		super();
		initializeTypes();
	}

	public SQLiteDialect(String defaultCatalog, String defaultSchema) {
		super(defaultCatalog, defaultSchema);
		initializeTypes();
	}

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("INTEGER DEFAULT 0", false, Types.INTEGER));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false,Types.INTEGER));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("NUMERIC", false, Types.NUMERIC));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("NUMERIC", false, Types.NUMERIC));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", false, Types.NUMERIC));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", false, Types.NUMERIC));
		registerJavaColumnType(String.class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("BLOB", false, Types.BLOB));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TEXT", false, Types.LONGVARCHAR));
	}

	@Override
	public String getIdentitySelectString() {
		return "";
	}

	@Override
	public boolean supportsSequences() {
		return false;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		throw new DatabaseDialectException(getClass().getName() + " não suporta sequence.");
	}

	@Override
	public String name() {
		return "SQLite";
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
	public boolean supportsIdentity() {
		return false;
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
	public boolean requiresTableInIndexDropDDL() {
		return false;
	}

	@Override
	public String getIdentifierQuoteCharacter() {
		return "";
	}

	@Override
	public String getForeignKeyDeletionString() {
		return "";
	}

	@Override
	public String getUniqueKeyDeletionString() {
		return "DROP INDEX IF EXISTS ";
	}

	@Override
	public boolean requiresUniqueConstraintCreationOnTableCreate() {
		return true;
	}

	@Override
	public boolean requiresForeignKeyConstraintCreationOnTableCreate() {
		return true;
	}

	@Override
	public boolean supportsDropForeignKeyConstraints() {
		return false;
	}

	@Override
	protected String getDropIndexString() {
		return "DROP INDEX IF EXISTS";
	}

	@Override
	public String getCreateTableString() {
		return "CREATE TABLE";
	}

	@Override
	public String getDropTableString() {
		return "DROP TABLE IF EXISTS";
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
		return new SQLiteTemplates();
	}

	@Override
	public SQLSessionException convertSQLException(SQLException ex, String msg, String sql) throws SQLSessionException {
		return new SQLSessionException(msg, ex, sql);
	}

	@Override
	public String applyLock(String sql, LockOptions lockOptions) {
		return sql;
	}

	@Override
	public String getSetLockTimeoutString(int secondsTimeOut) {
		return null;
	}

	@Override
	public boolean supportsLock() {
		return false;
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
