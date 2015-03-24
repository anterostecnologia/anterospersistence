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
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.templates.MySQLTemplates;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockAcquisitionException;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.lock.LockTimeoutException;
import br.com.anteros.persistence.sql.dialect.type.LimitClauseResult;
import br.com.anteros.persistence.util.AnterosPersistenceTranslate;

public class MySQLDialect extends DatabaseDialect {

	private static Logger log = LoggerProvider.getInstance().getLogger(MySQLDialect.class.getName());

	public MySQLDialect() {
		super();
		initializeTypes();
	}

	public MySQLDialect(String defaultCatalog, String defaultSchema) {
		super(defaultCatalog, defaultSchema);
		initializeTypes();
	}

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("TINYINT(1) DEFAULT 0", false, Types.TINYINT));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false, Types.INTEGER));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("FLOAT", false, Types.FLOAT));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("DOUBLE", false, Types.DOUBLE));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false, Types.SMALLINT));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("TINYINT", false, Types.TINYINT));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("BIGINT", false, Types.BIGINT));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("DECIMAL", 38, Types.DECIMAL));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("DECIMAL", 38, Types.DECIMAL));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE, Types.VARCHAR));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("CHAR", 1, Types.CHAR));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("LONGBLOB", false, Types.VARBINARY));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("LONGTEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("LONGBLOB", false, Types.VARBINARY));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("LONGTEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("LONGBLOB", false, Types.VARBINARY));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("LONGTEXT", false, Types.LONGVARCHAR));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false, Types.DATE));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("DATETIME", false, Types.TIMESTAMP));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false, Types.TIME));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("DATETIME", false, Types.TIMESTAMP));
	}

	@Override
	public String getIdentitySelectString() {
		return "select last_insert_id()";
	}

	@Override
	public boolean supportsSequences() {
		return false;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		throw new DatabaseDialectException(AnterosPersistenceTranslate.getMessage(MySQLDialect.class, "sequenceException", getClass().getName()));
	}

	@Override
	public char getCloseQuote() {
		return 0;
	}

	@Override
	public char getOpenQuote() {
		return 0;
	}

	@Override
	public String name() {
		return "MySQL";
	}

	/*
	 * public List<ProcedureMetadata> getNativeFunctions() throws Exception {
	 * 
	 * // http://dev.mysql.com/doc/refman/5.1/en/string-functions.html if (nativeFunctions == null) { nativeFunctions =
	 * new ArrayList<ProcedureMetadata>(); nativeFunctions.add(new ProcedureMetadata("CASE", this,
	 * "CASE WHEN ${condition} THEN ${result} WHEN ${condition} THEN ${result} ELSE ${result} END" , "Case operator"));
	 * nativeFunctions.add(new ProcedureMetadata("IF", this, "IF(${expr1},${expr2},${expr3})", "If/else construct"));
	 * nativeFunctions.add(new ProcedureMetadata("IFNULL", this, "IFNULL(${expr1},${expr2})",
	 * "Null if/else construct")); nativeFunctions.add(new ProcedureMetadata("NULLIF", this,
	 * "NULLIF(${expr1},${expr2})", "Return NULL if expr1 = expr2"));
	 * 
	 * nativeFunctions.add(new ProcedureMetadata("ASCII", this, "ASCII(${str})",
	 * "Return numeric value of left-most character")); nativeFunctions.add(new ProcedureMetadata("BIN", this,
	 * "BIN(${N})", "Return a string representation of the argument")); nativeFunctions.add(new
	 * ProcedureMetadata("BIT_LENGTH", this, "BIT_LENGTH(${str})", "Return length of argument in bits"));
	 * nativeFunctions.add(new ProcedureMetadata("CHAR_LENGTH", this, "CHAR_LENGTH(${str})",
	 * "Return number of characters in argument")); nativeFunctions.add(new ProcedureMetadata("CHAR", this,
	 * "CHAR(${N},${...} ${USING charset_name})", "Return the character for each integer passed"));
	 * nativeFunctions.add(new ProcedureMetadata("CHARACTER_LENGTH", this, "CHARACTER_LENGTH(${str})",
	 * "A synonym for CHAR_LENGTH()")); nativeFunctions.add(new ProcedureMetadata("CONCAT_WS", this,
	 * "CONCAT_WS(${separator},${str1},${str2},${...})", "Return concatenate with separator")); nativeFunctions.add(new
	 * ProcedureMetadata("CONCAT", this, "CONCAT(${str1},${str2},${...})", "Return concatenated string"));
	 * nativeFunctions.add(new ProcedureMetadata("ELT", this, "ELT(${N},${str1},${str2},${str3},${...})",
	 * "Return string at index number")); nativeFunctions .add(new ProcedureMetadata("EXPORT_SET", this,
	 * "EXPORT_SET(${bits},${on},${off})",
	 * " 	Return a string such that for every bit set in the value bits, you get an on string and for every unset bit, you get an off string"
	 * )); nativeFunctions.add(new ProcedureMetadata("FIELD", this, "FIELD(${str},${str1},${str2},${str3},${...})",
	 * "Return the index (position) of the first argument in the subsequent arguments" )); nativeFunctions.add(new
	 * ProcedureMetadata("FIND_IN_SET", this, "FIND_IN_SET(${str},${strlist})",
	 * "Return the index position of the first argument within the second argument" )); nativeFunctions.add(new
	 * ProcedureMetadata("FORMAT", this, "FORMAT(${X},${D})",
	 * "Return a number formatted to specified number of decimal places"));
	 * 
	 * nativeFunctions .add(new ProcedureMetadata("HEX", this, "HEX(${str})",
	 * "Return a hexadecimal representation of a decimal or string value")); nativeFunctions.add(new
	 * ProcedureMetadata("INSERT", this, "INSERT(${str},${pos},${len},${newstr})",
	 * "Insert a substring at the specified position up to the specified number of characters" ));
	 * nativeFunctions.add(new ProcedureMetadata("INSTR", this, "INSTR(${str},${substr})",
	 * "Return the index of the first occurrence of substring")); nativeFunctions.add(new ProcedureMetadata("LCASE",
	 * this, "LCASE(${str})", "Synonym for LOWER()")); nativeFunctions.add(new ProcedureMetadata("LEFT", this,
	 * "LEFT(${str},${len})", "Return the leftmost number of characters as specified")); nativeFunctions.add(new
	 * ProcedureMetadata("LENGTH", this, "LENGTH(${str})", "Return the length of a string in bytes"));
	 * nativeFunctions.add(new ProcedureMetadata("LIKE", this, "LIKE", "Return the length of a string in bytes"));
	 * nativeFunctions.add(new ProcedureMetadata("LOAD_FILE", this, "LOAD_FILE(${file_name})", "Load the named file"));
	 * nativeFunctions.add(new ProcedureMetadata("LOCATE", this, "LOCATE(${substr},${str})",
	 * "Return the position of the first occurrence of substring")); nativeFunctions.add(new ProcedureMetadata("LOWER",
	 * this, "LOWER(${str})", "Return the argument in lowercase")); nativeFunctions.add(new ProcedureMetadata("LPAD",
	 * this, "LPAD(${str},${len},${padstr})", "Return the string argument, left-padded with the specified string"));
	 * nativeFunctions.add(new ProcedureMetadata("LTRIM", this, "LTRIM(${str})", "Remove leading spaces"));
	 * nativeFunctions.add(new ProcedureMetadata("MAKE_SET", this, "MAKE_SET(${bits},${str1},${str2},${...})",
	 * "Return a set of comma-separated strings that have the corresponding bit in bits set" )); nativeFunctions.add(new
	 * ProcedureMetadata("MATCH", this, "MATCH", "Perform full-text search"));
	 * 
	 * nativeFunctions.add(new ProcedureMetadata("MID", this, "MID(${str},${pos},${len})",
	 * "Return a substring starting from the specified position")); nativeFunctions.add(new
	 * ProcedureMetadata("NOT LIKE", this, "NOT LIKE", "Negation of simple pattern matching")); nativeFunctions.add(new
	 * ProcedureMetadata("NOT REGEXP", this, "NOT REGEXP", "Negation of REGEXP")); nativeFunctions.add(new
	 * ProcedureMetadata("OCTET_LENGTH", this, "OCTET_LENGTH(${str})", "A synonym for LENGTH()"));
	 * nativeFunctions.add(new ProcedureMetadata("ORD", this, "ORD(${str})",
	 * "Return character code for leftmost character of the argument")); nativeFunctions.add(new
	 * ProcedureMetadata("POSITION", this, "POSITION(${substr} IN ${str})", "A synonym for LOCATE()"));
	 * nativeFunctions.add(new ProcedureMetadata("QUOTE", this, "QUOTE(${str})",
	 * "Escape the argument for use in an SQL statement")); nativeFunctions.add(new ProcedureMetadata("REGEXP", this,
	 * "REGEXP", "Pattern matching using regular expressions")); nativeFunctions.add(new ProcedureMetadata("REPEAT",
	 * this, "REPEAT(${str},${count})", "Repeat a string the specified number of times")); nativeFunctions.add(new
	 * ProcedureMetadata("REPLACE", this, "REPLACE(${str},${from_str},${to_str})",
	 * "Replace occurrences of a specified string")); nativeFunctions.add(new ProcedureMetadata("REVERSE", this,
	 * "REVERSE(${str})", "Reverse the characters in a string")); nativeFunctions.add(new ProcedureMetadata("RIGHT",
	 * this, "RIGHT(${str},${len})", "Return the specified rightmost number of characters")); nativeFunctions.add(new
	 * ProcedureMetadata("RLIKE", this, "RLIKE", "Synonym for REGEXP")); nativeFunctions.add(new
	 * ProcedureMetadata("RPAD", this, "RPAD(${str},${len},${padstr})", "Append string the specified number of times"));
	 * nativeFunctions.add(new ProcedureMetadata("RTRIM", this, "RTRIM(${str})", "Remove trailing spaces"));
	 * nativeFunctions.add(new ProcedureMetadata("SOUNDEX", this, "SOUNDEX(${str})", "Return a soundex string"));
	 * nativeFunctions.add(new ProcedureMetadata("SOUNDS LIKE", this, "SOUNDS LIKE", "Compare sounds"));
	 * nativeFunctions.add(new ProcedureMetadata("SPACE", this, "SPACE(${N})",
	 * "Return a string of the specified number of spaces")); nativeFunctions.add(new ProcedureMetadata("STRCMP", this,
	 * "STRCMP", "Compare two strings"));
	 * 
	 * nativeFunctions.add(new ProcedureMetadata("SUBSTR", this, "SUBSTR(${str},${pos})",
	 * "Return the substring as specified")); nativeFunctions.add(new ProcedureMetadata("SUBSTRING_INDEX", this,
	 * "SUBSTRING_INDEX(${str},${delim},${count})",
	 * "Return a substring from a string before the specified number of occurrences of the delimiter" ));
	 * nativeFunctions.add(new ProcedureMetadata("SUBSTRING", this, "SUBSTRING(${str},${pos})",
	 * "Return the substring as specified")); nativeFunctions.add(new ProcedureMetadata("TRIM", this, "TRIM(${str})",
	 * "Remove leading and trailing spaces")); nativeFunctions.add(new ProcedureMetadata("UCASE", this, "UCASE(${str})",
	 * "Synonym for UPPER()")); nativeFunctions.add(new ProcedureMetadata("UNHEX", this, "UNHEX(${str})",
	 * "Convert each pair of hexadecimal digits to a character")); nativeFunctions.add(new ProcedureMetadata("UPPER",
	 * this, "UPPER(${str})", "Convert to uppercase")); } return nativeFunctions; }
	 */

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
	public boolean supportsIdentity() {
		return true;
	}

	@Override
	public Writer writeColumnIdentityClauseDDLStatement(Writer schemaWriter) throws Exception {
		schemaWriter.write(" AUTO_INCREMENT");
		return schemaWriter;
	}

	@Override
	public int getMaxColumnNameSize() {
		return 64;
	}

	@Override
	public int getMaxForeignKeyNameSize() {
		return 64;
	}

	@Override
	public int getMaxIndexKeyNameSize() {
		return 64;
	}

	@Override
	public int getMaxUniqueKeyNameSize() {
		return 64;
	}

	@Override
	public boolean requiresTableInIndexDropDDL() {
		return true;
	}

	@Override
	public String getIdentifierQuoteCharacter() {
		return "";
	}

	@Override
	public String getForeignKeyDeletionString() {
		return " DROP FOREIGN KEY ";
	}

	@Override
	public String getUniqueKeyDeletionString() {
		return " DROP INDEX ";
	}

	@Override
	public int getIndexTypeOfFunctionMetadata() {
		return 2;
	}

	@Override
	public int getIndexTypeOfProcedureMetadata() {
		return 0;
	}

	@Override
	public boolean requiresNamedPrimaryKeyConstraints() {
		return true;
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
		return new MySQLTemplates();
	}

	@Override
	public SQLSessionException convertSQLException(SQLException ex, String msg, String sql) throws SQLSessionException {
		final String sqlState = extractSqlState(ex);

		if ("41000".equals(sqlState)) {
			return new LockTimeoutException(msg, ex, sql);
		}

		if ("40001".equals(sqlState)) {
			return new LockAcquisitionException(msg, ex, sql);
		}

		return new SQLSessionException(msg, ex, sql);
	}

	@Override
	public String applyLock(String sql, LockOptions lockOptions) {
		LockMode lockMode = lockOptions.getLockMode();
		switch (lockMode) {
		case PESSIMISTIC_READ:
			return sql + " LOCK IN SHARE MODE ";
		case PESSIMISTIC_WRITE:
		case PESSIMISTIC_FORCE_INCREMENT:
			return sql + " FOR UPDATE ";
		default:
			return sql;
		}
	}

	@Override
	public String getSetLockTimeoutString(int secondsTimeOut) {
		return "set innodb_lock_wait_timeout  = " + secondsTimeOut;
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
