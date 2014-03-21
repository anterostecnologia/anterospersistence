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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;

public class SQLiteDialect extends DatabaseDialect {
	private static Logger log = LoggerFactory.getLogger(SQLiteDialect.class);

	
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

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("INTEGER DEFAULT 0", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("NUMERIC", false));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("NUMERIC", false));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", false));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", false));
		registerJavaColumnType(String.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("BLOB", false));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TEXT", false));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TEXT", false));
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
	public String getSelectForUpdateString() {
		return "";
	}

	@Override
	public String name() {
		return "SQLite";
	}

	@Override
	public CallableStatement prepareCallableStatement(Connection connection, CallableType type, String name, Object[] inputParameters,
			String[] outputParametersName, int[] outputTypes, int queryTimeOut, boolean showSql, String clientId) throws Exception {
		return null;
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
	public boolean  supportsDropForeignKeyConstraints(){
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
	public String getNoWaitString() {
		return "";
	}
	
	@Override
	public String getSelectForUpdateNoWaitString() {
		return "";
	}
	
	@Override
	public String getSelectForUpdateOfString() {
		return "";
	}
}
