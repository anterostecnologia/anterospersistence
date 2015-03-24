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
import br.com.anteros.persistence.dsl.osql.templates.HSQLDBTemplates;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.sql.dialect.type.LimitClauseResult;

public class HSQLDialect extends DatabaseDialect {

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BOOLEAN", false, Types.BOOLEAN));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("REAL", false, Types.REAL));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("REAL", false, Types.REAL));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 38, Types.NUMERIC).setLimits(38, -19, 19));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 38, Types.NUMERIC).setLimits(38, -19, 19));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("LONGVARBINARY", false, Types.LONGVARBINARY));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("LONGVARCHAR", false, Types.LONGNVARCHAR));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("LONGVARBINARY", false, Types.LONGVARBINARY));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("LONGVARCHAR", false, Types.LONGVARCHAR));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE, Types.VARCHAR));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("LONGVARBINARY", false, Types.LONGVARBINARY));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("LONGVARCHAR", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false, Types.DATE));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false, Types.TIME));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false, Types.TIMESTAMP));
	}

	public HSQLDialect() {
		super();
	}

	public HSQLDialect(String defaultCatalog, String defaultSchema) {
		super(defaultCatalog, defaultSchema);

	}

	@Override
	public String getIdentitySelectString() {
		return null;
	}

	@Override
	public boolean supportsSequences() {
		return false;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		return null;
	}

	@Override
	public boolean supportInCondition() {
		return false;
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
	public String name() {
		return null;
	}

	@Override
	public boolean supportsPrimaryKeyConstraintOnTableCreate() {
		return false;
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
		return null;
	}

	@Override
	public boolean supportsUniqueKeyConstraints() {
		return false;
	}

	@Override
	public boolean supportsForeignKeyConstraints() {
		return false;
	}

	@Override
	public int getMaxUniqueKeyNameSize() {
		return 0;
	}

	@Override
	public boolean supportsDeleteOnCascade() {
		return false;
	}

	@Override
	public boolean supportsIdentity() {
		return false;
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
		return new HSQLDBTemplates();
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
