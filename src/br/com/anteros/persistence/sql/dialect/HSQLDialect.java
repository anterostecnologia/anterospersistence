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

public class HSQLDialect extends DatabaseDialect {

	@Override
	protected void initializeTypes() {
		super.initializeTypes();

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("BOOLEAN", false));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("INTEGER", false));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("BIGINT", false));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("REAL", false));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("REAL", false));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("SMALLINT", false));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("BIGINT", false));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMERIC", 38).setLimits(38, -19, 19));
		registerJavaColumnType(Number.class, new ColumnDatabaseType("NUMERIC", 38).setLimits(38, -19, 19));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("LONGVARBINARY", false));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("LONGVARCHAR", false));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("LONGVARBINARY", false));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("LONGVARCHAR", false));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR", DEFAULT_VARCHAR_SIZE));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("LONGVARBINARY", false));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("LONGVARCHAR", false));
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
	public String getSelectForUpdateString() {
		return "";
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

}
