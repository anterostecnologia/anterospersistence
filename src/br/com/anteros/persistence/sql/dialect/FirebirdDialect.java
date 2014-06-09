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

import java.io.IOException;
import java.io.Writer;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.SequenceGeneratorSchema;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;

public class FirebirdDialect extends DatabaseDialect {

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("NUMERIC", 18).setLimits(18, -18, 18));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("FLOAT", false));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("DOUBLE PRECISION", false));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMERIC", 18).setLimits(18, -18, 18));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 18).setLimits(18, -18, 18));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 38).setLimits(18, -18, 18));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("VARCHAR", 1));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("VARCHAR", 32000));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("VARCHAR", 32000));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("VARCHAR", 32000));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE", false));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME", false));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP", false));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP", false));
	}

	@Override
	public String getIdentitySelectString() {
		return null;
	}

	@Override
	public String getSelectForUpdateString() {
		return " WITH LOCK";
	}

	@Override
	public CallableStatement prepareCallableStatement(Connection connection, CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, int[] outputTypes, int queryTimeOut, boolean showSql, String clientId) throws Exception {
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

}
