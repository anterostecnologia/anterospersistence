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

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;

public class SQLServerDialect extends DatabaseDialect {
	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BIT default 0", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("NUMERIC", 19));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("FLOAT(16)", false));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("FLOAT(32)", false));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMERIC", 28));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 28).setLimits(28, -19, 19));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 28).setLimits(28, -19, 19));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("CHAR", 1));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("IMAGE", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("IMAGE", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("IMAGE", false));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATETIME", false));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("DATETIME", false));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("DATETIME", false));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("DATETIME", false));
	}

	@Override
	public String getIdentitySelectString() {
		return "SELECT @@IDENTITY";
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) throws Exception {
		return "SELECT NEXT VALUE FOR " + sequenceName;
	}

	@Override
	public boolean supportInCondition() {
		return true;
	}

	@Override
	public String getSelectForUpdateString() {
		return " WITH (UPDLOCK)";
	}

	@Override
	public CallableStatement prepareCallableStatement(Connection connection, CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, int[] outputTypes, int queryTimeOut, boolean showSql, String clientId) throws Exception {
		return null;
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
		return "MSSQLServer";
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
	public int getMaxUniqueKeyNameSize() {
		return 22;
	}

	@Override
	public int getMaxColumnNameSize() {
		return 22;
	}

	@Override
	public int getMaxForeignKeyNameSize() {
		return 22;
	}

	@Override
	public int getMaxIndexKeyNameSize() {
		return 22;
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
    public char getCloseQuote() {
		return ']';
	}

	@Override
    public char getOpenQuote() {
		return '[';
	}

}
