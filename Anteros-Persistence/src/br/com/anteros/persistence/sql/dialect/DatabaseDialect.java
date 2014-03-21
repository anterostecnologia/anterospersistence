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
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.schema.definition.ColumnSchema;
import br.com.anteros.persistence.schema.definition.ForeignKeySchema;
import br.com.anteros.persistence.schema.definition.IndexSchema;
import br.com.anteros.persistence.schema.definition.PrimaryKeySchema;
import br.com.anteros.persistence.schema.definition.SequenceGeneratorSchema;
import br.com.anteros.persistence.schema.definition.StoredFunctionSchema;
import br.com.anteros.persistence.schema.definition.StoredParameterSchema;
import br.com.anteros.persistence.schema.definition.StoredProcedureSchema;
import br.com.anteros.persistence.schema.definition.TableSchema;
import br.com.anteros.persistence.schema.definition.UniqueKeySchema;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.schema.definition.type.StoredParameterType;
import br.com.anteros.persistence.session.type.LobType;
import br.com.anteros.persistence.util.StringUtils;

public abstract class DatabaseDialect {

	private HashMap<Integer, String> jdbcTypes;
	// protected List<ProcedureMetadata> nativeFunctions;
	private Map<Class<?>, ColumnDatabaseType> javaTypes = new HashMap<Class<?>, ColumnDatabaseType>();
	private Map<Class<?>, String> databaseTypes = new HashMap<Class<?>, String>();
	private String defaultCatalog;
	private String defaultSchema;

	public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_PATTERN = "yyyy-MM-dd";
	public static final String TIME_PATTERN = "HH:mm:ss";

	public static final int DEFAULT_VARCHAR_SIZE = 255;
	public static final int MAX_CLOB = 2147483647;
	public static final int MAX_BLOB = MAX_CLOB;
	public static String PROCEDURE_CAT = "PROCEDURE_CAT";
	public static String PROCEDURE_SCHEM = "PROCEDURE_SCHEM";
	public static String PROCEDURE_NAME = "PROCEDURE_NAME";
	public static String PROCEDURE_TYPE = "PROCEDURE_TYPE";
	public static final String COLUMN_NAME = "COLUMN_NAME";
	public static final String COLUMN_TYPE = "COLUMN_TYPE";
	public static final String DATA_TYPE = "DATA_TYPE";
	public static final String TYPE_NAME = "TYPE_NAME";
	public static final String PRECISION = "PRECISION";
	public static final String LENGTH = "LENGTH";
	public static final String SCALE = "SCALE";
	public static final String RADIX = "RADIX";
	public static final String REMARKS = "REMARKS";
	public static final String NULLABLE = "NULLABLE";
	public static int FUNCTION = 2;
	public static int PROCEDURE = 1;

	protected String startDelimiter = null;
	protected String endDelimiter = null;
	public boolean supportsComments = false;

	public DatabaseDialect() {
		this.startDelimiter = "";
		this.endDelimiter = "";
		initializeTypes();
	}

	public DatabaseDialect(String defaultCatalog, String defaultSchema) {
		initializeTypes();

		if (!"".equals(defaultCatalog))
			this.defaultCatalog = defaultCatalog;

		if (!"".equals(defaultSchema))
			this.defaultSchema = defaultSchema;
		jdbcTypes = new HashMap<Integer, String>();

		Field[] fields = java.sql.Types.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				String name = fields[i].getName();
				Integer value = (Integer) fields[i].get(null);
				jdbcTypes.put(value, name);
			} catch (IllegalAccessException e) {
			}
		}
	}

	protected void initializeTypes() {
		registerDatabaseColumnType("NUMBER", java.math.BigInteger.class);
		registerDatabaseColumnType("DECIMAL", java.math.BigDecimal.class);
		registerDatabaseColumnType("INTEGER", Integer.class);
		registerDatabaseColumnType("INT", Integer.class);
		registerDatabaseColumnType("NUMERIC", java.math.BigInteger.class);
		registerDatabaseColumnType("FLOAT(16)", Float.class);
		registerDatabaseColumnType("FLOAT(32)", Double.class);
		registerDatabaseColumnType("NUMBER(1) default 0", Boolean.class);
		registerDatabaseColumnType("SHORT", Short.class);
		registerDatabaseColumnType("BYTE", Byte.class);
		registerDatabaseColumnType("DOUBLE", Double.class);
		registerDatabaseColumnType("FLOAT", Float.class);
		registerDatabaseColumnType("SMALLINT", Short.class);
		registerDatabaseColumnType("BIT", Boolean.class);
		registerDatabaseColumnType("SMALLINT DEFAULT 0", Boolean.class);
		registerDatabaseColumnType("VARCHAR", String.class);
		registerDatabaseColumnType("CHAR", Character.class);
		registerDatabaseColumnType("LONGVARBINARY", Byte[].class);
		registerDatabaseColumnType("TEXT", Character[].class);
		registerDatabaseColumnType("LONGTEXT", Character[].class);
		registerDatabaseColumnType("MEMO", Character[].class);
		registerDatabaseColumnType("VARCHAR2", String.class);
		registerDatabaseColumnType("LONG RAW", Byte[].class);
		registerDatabaseColumnType("LONG", Character[].class);
		registerDatabaseColumnType("DATE", java.sql.Date.class);
		registerDatabaseColumnType("TIMESTAMP", java.sql.Timestamp.class);
		registerDatabaseColumnType("TIME", java.sql.Time.class);
		registerDatabaseColumnType("DATETIME", java.sql.Timestamp.class);
		registerDatabaseColumnType("BIGINT", java.math.BigInteger.class);
		registerDatabaseColumnType("DOUBLE PRECIS", Double.class);
		registerDatabaseColumnType("IMAGE", Byte[].class);
		registerDatabaseColumnType("LONGVARCHAR", Character[].class);
		registerDatabaseColumnType("REAL", Float.class);
		registerDatabaseColumnType("TINYINT", Short.class);
		registerDatabaseColumnType("BLOB", Byte[].class);
		registerDatabaseColumnType("CLOB", Character[].class);

		registerJavaColumnType(Boolean.class, new ColumnDatabaseType("NUMBER", 1));
		registerJavaColumnType(Integer.class, new ColumnDatabaseType("NUMBER", 10));
		registerJavaColumnType(Long.class, new ColumnDatabaseType("NUMBER", 19));
		registerJavaColumnType(Float.class, new ColumnDatabaseType("NUMBER", 12, 5).setLimits(19, 0, 19));
		registerJavaColumnType(Double.class, new ColumnDatabaseType("NUMBER", 10, 5).setLimits(19, 0, 19));
		registerJavaColumnType(Short.class, new ColumnDatabaseType("NUMBER", 5));
		registerJavaColumnType(Byte.class, new ColumnDatabaseType("NUMBER", 3));
		registerJavaColumnType(java.math.BigInteger.class, new ColumnDatabaseType("NUMBER", 19));
		registerJavaColumnType(java.math.BigDecimal.class, new ColumnDatabaseType("NUMBER", 19, 0).setLimits(19, 0, 19));
		registerJavaColumnType(String.class, new ColumnDatabaseType("VARCHAR"));
		registerJavaColumnType(Character.class, new ColumnDatabaseType("CHAR"));
		registerJavaColumnType(Byte[].class, new ColumnDatabaseType("BLOB"));
		registerJavaColumnType(Character[].class, new ColumnDatabaseType("CLOB"));
		registerJavaColumnType(byte[].class, new ColumnDatabaseType("BLOB"));
		registerJavaColumnType(char[].class, new ColumnDatabaseType("CLOB"));
		registerJavaColumnType(java.sql.Blob.class, new ColumnDatabaseType("BLOB"));
		registerJavaColumnType(java.sql.Clob.class, new ColumnDatabaseType("CLOB"));
		registerJavaColumnType(java.sql.Date.class, new ColumnDatabaseType("DATE"));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("DATE"));
		registerJavaColumnType(java.sql.Timestamp.class, new ColumnDatabaseType("TIMESTAMP"));
		registerJavaColumnType(java.sql.Time.class, new ColumnDatabaseType("TIME"));
		registerJavaColumnType(java.util.Calendar.class, new ColumnDatabaseType("TIMESTAMP"));
		registerJavaColumnType(java.util.Date.class, new ColumnDatabaseType("TIMESTAMP"));
		registerJavaColumnType(java.lang.Number.class, new ColumnDatabaseType("NUMBER", 10));
	}

	public void registerJavaColumnType(Class<?> type, ColumnDatabaseType columnDatabaseType) {
		javaTypes.put(type, columnDatabaseType);
	}

	public void registerDatabaseColumnType(String value, Class<?> type) {
		databaseTypes.put(type, value.toLowerCase());
	}

	public Class<?> convertDatabaseToJavaType(String databaseType) {
		databaseType = databaseType.toLowerCase();
		for (Class<?> type : databaseTypes.keySet()) {
			String dbType = databaseTypes.get(type);
			if (databaseType.equals(dbType))
				return type;
		}
		return null;
	}

	public ColumnDatabaseType convertJavaToDatabaseType(Class<?> javaType) {
		return javaTypes.get(javaType);
	}

	protected void setParametersCallableStatement(CallableStatement call, CallableType type, Object[] inputParameters)
			throws Exception {
		int i = 0;
		int index = 0;
		for (Object parameter : inputParameters) {
			index = i;
			if (type == CallableType.FUNCTION)
				index = i + 1;
			if (parameter instanceof LobType) {
				if (((LobType) parameter).getType() == Types.BLOB) {
					Blob blob = createTemporaryBlob(call.getConnection(), (byte[]) (((LobType) parameter).getValue()));
					if (blob != null)
						call.setBlob(index + 1, blob);
					else
						call.setObject(index + 1, parameter);
				} else if (((LobType) parameter).getType() == Types.CLOB) {
					Clob clob = createTemporaryClob(call.getConnection(), (byte[]) (((LobType) parameter).getValue()));
					if (clob != null)
						call.setClob(index + 1, clob);
					else
						call.setObject(index + 1, parameter);
				} else
					call.setObject(index + 1, parameter);

			} else
				call.setObject(index + 1, parameter);
			i++;
		}
	}

	public String getJdbcTypeName(int jdbcType) {
		return this.jdbcTypes.get(jdbcType);
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public void setDefaultCatalog(String defaultCatalog) {
		if ("".equals(defaultCatalog))
			this.defaultCatalog = null;
		else
			this.defaultCatalog = defaultCatalog;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		if ("".equals(defaultSchema))
			this.defaultSchema = null;
		else
			this.defaultSchema = defaultSchema;
	}

	/**
	 * Gera DDL para criação da tabela no banco de dados.
	 * 
	 * @param tableSchema
	 *            objeto tabela
	 * @param schemaWriter
	 *            saida para os comandos DDL
	 * @return objeto de saída para os comandos DDL
	 * @throws IOException
	 *             se não conseguir gravar os comandos gera uma exceção
	 */
	public Writer writeCreateTableDDLStatement(TableSchema tableSchema, Writer schemaWriter) throws Exception {
		/*
		 * Adiciona o nome da tabela
		 */
		schemaWriter.write(getCreateTableString() + " " + getQuoted(tableSchema.getName()) + " (\n");
		/*
		 * Gera as colunas
		 */
		for (Iterator<ColumnSchema> it = tableSchema.getColumns().iterator(); it.hasNext();) {
			ColumnSchema column = it.next();
			/*
			 * Gera a coluna
			 */
			schemaWriter.write("       ");
			writeColumnDDLStatement(column, schemaWriter);
			if (it.hasNext()) {
				schemaWriter.write(",\n");
			}
		}
		/*
		 * Adiciona a chave primária caso o banco de dados suporte chave
		 * primária na criação da tabela
		 */

		if (tableSchema.getPrimaryKey() != null) {
			if ((!tableSchema.getPrimaryKey().getColumns().isEmpty()) && supportsPrimaryKeyConstraintOnTableCreate()) {
				schemaWriter.write(", ");
				schemaWriter.write("\n       ");
				writePrimaryKeyDDLStatement(tableSchema, schemaWriter);
			}
		}
		/*
		 * Adiciona as constraints únicas caso o banco não suporte criar usando
		 * ALTER TABLE ADD CONSTRAINT
		 */
		if (requiresUniqueConstraintCreationOnTableCreate()) {
			if (supportsUniqueKeyConstraints()) {
				for (UniqueKeySchema constraint : tableSchema.getUniqueKeys()) {
					schemaWriter.write(", ");
					schemaWriter.write("\n       ");
					writeUniqueKeyDDLStatement(constraint, schemaWriter);
				}
			}
		}

		/*
		 * Adiciona as constraints ForeignKey caso o banco não suporte criar
		 * usando ALTER TABLE ADD CONSTRAINT
		 */
		if (requiresForeignKeyConstraintCreationOnTableCreate()) {
			if (supportsForeignKeyConstraints()) {
				for (ForeignKeySchema constraint : tableSchema.getForeignKeys()) {
					schemaWriter.write(", ");
					schemaWriter.write("\n       ");
					writeForeignKeyConstraintDDLStatement(constraint, schemaWriter);
				}
			}
		}
		schemaWriter.write(")");
		/*
		 * Adiciona sufixo de criação.
		 */
		if (!StringUtils.isEmpty(tableSchema.getCreateTableSuffix()))
			schemaWriter.write(" " + tableSchema.getCreateTableSuffix());
		else if (!StringUtils.isEmpty(getDefaultTableCreateSuffix()))
			schemaWriter.write(" " + tableSchema.getCreateTableSuffix());
		return schemaWriter;
	}

	protected String getQuoted(String name) {
		return getIdentifierQuoteCharacter() + name + getIdentifierQuoteCharacter();
	}

	public Writer writeDropTableDDLStatement(TableSchema tableSchema, Writer schemaWriter) throws IOException {
		schemaWriter.write(getDropTableString() + " " + tableSchema.getName());
		return schemaWriter;
	}

	public String getDropTableString() {
		return "DROP TABLE";
	}

	/**
	 * Gera DDL da coluna da tabela
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @param columnSchema
	 *            Coluna
	 * @param schemaWriter
	 *            Objeto de saida para o DDL
	 * @return
	 */
	public Writer writeColumnDDLStatement(ColumnSchema columnSchema, Writer schemaWriter) throws Exception {
		/*
		 * Nome da coluna
		 */
		schemaWriter.write(getQuoted(columnSchema.getName()));
		schemaWriter.write(" ");

		if (!StringUtils.isEmpty(columnSchema.getColumnDefinition())) {
			/*
			 * Tipo definido pelo usuário
			 */
			schemaWriter.write(columnSchema.getColumnDefinition());
		} else {

			if (columnSchema.isAutoIncrement() && supportsIdentity() && !useColumnDefinitionForIdentity()
					&& !columnSchema.hasSequenceName()) {
				writeColumnIdentityClauseDDLStatement(schemaWriter);
			} else {
				/*
				 * Tipo SQL
				 */
				schemaWriter.write(columnSchema.getTypeSql());
				/*
				 * Tamanho ou Precisão/Escala
				 */
				if (columnSchema.getSize() > 0) {
					schemaWriter.write("(" + columnSchema.getSize());
					if (columnSchema.getSubSize() > 0)
						schemaWriter.write("," + columnSchema.getSubSize());
					schemaWriter.write(")");
				}

				/*
				 * Se a coluna for not null
				 */
				if (columnSchema.getNullable())
					writeColumnNullClauseDDLStatement(schemaWriter);
				else
					writeColumnNotNullClauseDDLStatement(schemaWriter);

				/*
				 * Se a coluna for de Identidade (auto incremento)
				 */
				if (columnSchema.isAutoIncrement() && supportsIdentity() && !columnSchema.hasSequenceName()) {
					writeColumnIdentityClauseDDLStatement(schemaWriter);
				}

				/*
				 * Se suporta sequence como default value
				 */
				if (supportsSequenceAsADefaultValue() && columnSchema.hasSequenceName()) {
					writeColumnSequenceDefaultValue(schemaWriter, columnSchema.getSequenceName());
				}

				/*
				 * Comentário da coluna
				 */
				// FALTA VER COMO FAZER

				/*
				 * Se a coluna possuí um valor default
				 */
				if ((columnSchema.getDefaultValue() != null) && (!"".equals(columnSchema.getDefaultValue())))
					schemaWriter.write(" DEFAULT " + columnSchema.getDefaultValue());
			}
		}
		return schemaWriter;
	}

	public Writer writeColumnSequenceDefaultValue(Writer schemaWriter, String sequenceName) throws Exception {
		return schemaWriter;
	}

	public boolean supportsSequenceAsADefaultValue() {
		return false;
	}

	/**
	 * Gera DDL da chave primária da tabela
	 * 
	 * @param tableSchema
	 *            Objeto tabela
	 * @param schemaWriter
	 *            Objeto de saida para o DDL
	 * @return
	 */
	public Writer writePrimaryKeyDDLStatement(TableSchema tableSchema, Writer schemaWriter) throws Exception {
		if (requiresNamedPrimaryKeyConstraints()) {
			schemaWriter.write("CONSTRAINT PK_" + getQuoted(tableSchema.getName()) + " ");
		}
		schemaWriter.write("PRIMARY KEY (");
		List<ColumnSchema> keyColumns = tableSchema.getPrimaryKey().getColumns();
		for (Iterator<ColumnSchema> iterator = keyColumns.iterator(); iterator.hasNext();) {
			schemaWriter.write(getQuoted(iterator.next().getName()));
			if (iterator.hasNext()) {
				schemaWriter.write(", ");
			}
		}
		schemaWriter.write(")");
		return schemaWriter;
	}

	/**
	 * Gera DDL da constraint única
	 * 
	 * @param uniqueKeySchema
	 *            Constraint única
	 * @param schemaWriter
	 *            Objeto de saida para o DDL
	 * @return
	 */
	public Writer writeUniqueKeyDDLStatement(UniqueKeySchema uniqueKeySchema, Writer schemaWriter) throws Exception {
		schemaWriter.write("CONSTRAINT " + getQuoted(uniqueKeySchema.getName()) + " UNIQUE (");
		boolean appendDelimiter = false;
		for (ColumnSchema column : uniqueKeySchema.getColumns()) {
			if (appendDelimiter)
				schemaWriter.write(", ");
			schemaWriter.write(getQuoted(column.getName()));
			appendDelimiter = true;
		}
		schemaWriter.write(")");
		return schemaWriter;
	}

	public Writer writeCreateSequenceDDLStatement(SequenceGeneratorSchema sequenceGeneratorSchema, Writer schemaWriter)
			throws IOException {
		schemaWriter.write(getCreateSequenceString() + " ");
		schemaWriter.write(sequenceGeneratorSchema.getName());
		if (sequenceGeneratorSchema.getIncrementSize() != 1) {
			schemaWriter.write(" INCREMENT BY " + sequenceGeneratorSchema.getIncrementSize());
		}
		schemaWriter.write(" START WITH " + sequenceGeneratorSchema.getInitialValue());
		return schemaWriter;
	}

	public String getCreateSequenceString() {
		return "CREATE SEQUENCE";
	}

	public Writer writeAlterSequenceDDLStatement(SequenceGeneratorSchema sequenceGeneratorSchema, Writer schemaWriter) {
		return schemaWriter;
	}

	public Writer writeDropSequenceDDLStatement(SequenceGeneratorSchema sequenceGeneratorSchema, Writer schemaWriter)
			throws IOException {
		schemaWriter.write(getDropSequenceString() + " " + sequenceGeneratorSchema.getName());
		return schemaWriter;
	}

	public String getDropSequenceString() {
		return "DROP SEQUENCE";
	}

	public String getCreateTableString() {
		return "CREATE TABLE";
	}

	public Writer writeCreateIndexDDLStatement(IndexSchema indexSchema, Writer schemaWriter) throws IOException {
		if (indexSchema.isUnique()) {
			schemaWriter.write("CREATE UNIQUE INDEX ");
		} else {
			schemaWriter.write("CREATE INDEX ");
		}
		schemaWriter.append(indexSchema.getName()).append(" ON ").append(indexSchema.getTable().getName()).append(" (");
		boolean appendDelimiter = false;
		for (ColumnSchema columnSchema : indexSchema.getColumns()) {
			if (appendDelimiter)
				schemaWriter.write(",");
			schemaWriter.write(columnSchema.getName());
		}
		schemaWriter.write(")");
		return schemaWriter;
	}

	public Writer writeDropIndexDDLStatement(IndexSchema indexSchema, Writer schemaWriter) throws IOException {
		schemaWriter.append(getDropIndexString() + " ");
		schemaWriter.append(indexSchema.getName());
		if (requiresTableInIndexDropDDL()) {
			schemaWriter.append(" ON ").append(indexSchema.getTable().getName());
		}
		return schemaWriter;
	}

	protected String getDropIndexString() {
		return "DROP INDEX";
	}

	public Writer writerAddColumnDDLStatement(ColumnSchema columnSchema, Writer schemaWriter) throws Exception {
		schemaWriter.write("ALTER TABLE " + columnSchema.getTable().getName());
		schemaWriter.write(" ADD ");
		writeColumnDDLStatement(columnSchema, schemaWriter);
		return schemaWriter;
	}

	public Writer writeDropColumnDDlStatement(ColumnSchema columnSchemas, Writer schemaWriter) {
		return schemaWriter;
	}

	public Writer writeAddPrimaryKeyDDLStatement(PrimaryKeySchema primaryKeySchema, Writer schemaWriter)
			throws Exception {
		schemaWriter.write("ALTER TABLE " + getQuoted(primaryKeySchema.getTable().getName()));
		schemaWriter.write(" ADD CONSTRAINT " + getQuoted(primaryKeySchema.getName()));

		writePrimaryKeyConstraintDDLStatement(primaryKeySchema, schemaWriter);
		return schemaWriter;
	}

	public void writePrimaryKeyConstraintDDLStatement(PrimaryKeySchema primaryKeySchema, Writer schemaWriter)
			throws IOException {
		schemaWriter.write(" PRIMARY KEY (");
		boolean appendDelimiter = false;
		for (ColumnSchema column : primaryKeySchema.getColumns()) {
			if (appendDelimiter)
				schemaWriter.write(", ");
			schemaWriter.write(getQuoted(column.getName()));
			appendDelimiter = true;
		}
		schemaWriter.write(")");
	}

	public Writer writeDropPrimaryKeyDDLStatement(PrimaryKeySchema primaryKeySchema, Writer schemaWriter) {
		return schemaWriter;
	}

	/**
	 * Gera DDL da chave estrangeira
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @param foreignKeySchema
	 *            Chave estrangeira
	 * @param schemaWriter
	 *            Objeto de saida para o DDL
	 * @return
	 */
	public Writer writeAddForeignKeyDDLStatement(ForeignKeySchema foreignKeySchema, Writer schemaWriter)
			throws Exception {
		schemaWriter.write("ALTER TABLE " + getQuoted(foreignKeySchema.getTable().getName()));
		schemaWriter.write(" ADD CONSTRAINT " + getQuoted(foreignKeySchema.getName()));

		writeForeignKeyConstraintDDLStatement(foreignKeySchema, schemaWriter);
		return schemaWriter;
	}

	public void writeForeignKeyConstraintDDLStatement(ForeignKeySchema foreignKeySchema, Writer schemaWriter)
			throws IOException {
		schemaWriter.write(" FOREIGN KEY (");
		boolean appendDelimiter = false;
		for (ColumnSchema column : foreignKeySchema.getColumns()) {
			if (appendDelimiter)
				schemaWriter.write(", ");
			schemaWriter.write(getQuoted(column.getName()));
			appendDelimiter = true;
		}
		schemaWriter.write(") REFERENCES ");
		schemaWriter.write(getQuoted(foreignKeySchema.getReferencedTable().getName()));
		schemaWriter.write(" (");
		appendDelimiter = false;
		for (ColumnSchema column : foreignKeySchema.getColumnsReferences()) {
			if (appendDelimiter)
				schemaWriter.write(", ");
			schemaWriter.write(getQuoted(column.getName()));
			appendDelimiter = true;
		}
		schemaWriter.write(")");

		if (foreignKeySchema.isCascadeOnDelete() && supportsDeleteOnCascade()) {
			schemaWriter.write(" ON DELETE CASCADE");
		}
	}

	public Writer writeDropForeignKeyDDLStatement(ForeignKeySchema foreignKeySchema, Writer schemaWriter)
			throws IOException {
		schemaWriter.write("ALTER TABLE " + foreignKeySchema.getTable().getName());
		schemaWriter.write(getForeignKeyDeletionString() + foreignKeySchema.getName());
		return schemaWriter;
	}

	/**
	 * Gera DDL para alterar tabela e adiciona constraint única
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @param uniqueKeySchema
	 *            Constraint única
	 * @param schemaWriter
	 *            Objeto de saida para o DDL
	 * @return
	 */
	public Writer writeAddUniqueKeyDDLStatement(UniqueKeySchema uniqueKeySchema, Writer schemaWriter) throws Exception {
		schemaWriter.write("ALTER TABLE " + getQuoted(uniqueKeySchema.getTable().getName()));
		schemaWriter.write(" ADD ");
		return writeUniqueKeyDDLStatement(uniqueKeySchema, schemaWriter);
	}

	public Writer writeDropUniqueKeyDDLStatement(UniqueKeySchema uniqueKeySchema, Writer schemaWriter)
			throws IOException {
		schemaWriter.write("ALTER TABLE " + uniqueKeySchema.getTable().getName());
		schemaWriter.write(getUniqueKeyDeletionString() + uniqueKeySchema.getName());
		return schemaWriter;
	}

	public Writer writeColumnIdentityClauseDDLStatement(Writer schemaWriter) throws Exception {
		return schemaWriter;
	}

	public Writer writeColumnNullClauseDDLStatement(Writer schemaWriter) throws Exception {
		return schemaWriter;
	}

	public Writer writeColumnNotNullClauseDDLStatement(Writer schemaWriter) throws Exception {
		schemaWriter.write(" NOT NULL");
		return schemaWriter;
	}

	public boolean checkTableExists(Connection conn, String tableName) throws SQLException, Exception {
		ResultSet resultSet = conn.getMetaData().getTables(null, null, tableName.toUpperCase(),
				new String[] { "TABLE" });
		if (resultSet.next())
			return true;

		resultSet = conn.getMetaData().getTables(null, null, tableName.toLowerCase(), new String[] { "TABLE" });
		if (resultSet.next())
			return true;

		return false;
	}

	public boolean checkSequenceExists(Connection conn, String sequenceName) throws SQLException, Exception {
		ResultSet resultSet = conn.getMetaData().getTables(null, null, sequenceName.toLowerCase(),
				new String[] { "SEQUENCE" });
		if (resultSet.next())
			return true;

		resultSet = conn.getMetaData().getTables(null, null, sequenceName.toUpperCase(), new String[] { "SEQUENCE" });
		if (resultSet.next())
			return true;
		return false;
	}

	public boolean checkTableAndColumnExists(Connection conn, String tableName, String columnName) throws SQLException,
			Exception {
		ResultSet resultSet = conn.getMetaData().getColumns(null, null, tableName.toLowerCase(),
				columnName.toLowerCase());
		if (resultSet.next())
			return true;

		resultSet = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase());
		if (resultSet.next())
			return true;
		return false;
	}

	public boolean checkForeignKeyExists(Connection conn, String tableName, String foreignKeyName) throws Exception {
		DatabaseMetaData metaData = conn.getMetaData();

		ResultSet resultSet = metaData.getImportedKeys("", "", tableName.toLowerCase());
		if ((resultSet != null) && (resultSet.next())) {
			do {
				if (foreignKeyName.equalsIgnoreCase(resultSet.getString("FK_NAME")))
					return true;
			} while (resultSet.next());
			resultSet.close();
			return false;
		}

		resultSet = metaData.getImportedKeys("", "", tableName.toUpperCase());
		if ((resultSet != null) && (resultSet.next())) {
			do {
				if (foreignKeyName.equalsIgnoreCase(resultSet.getString("FK_NAME")))
					return true;
			} while (resultSet.next());
			resultSet.close();
		}
		return false;
	}

	public boolean checkIndexExists(Connection conn, String tableName, String indexName) throws Exception {
		DatabaseMetaData metaData = conn.getMetaData();

		ResultSet resultSet = metaData.getIndexInfo("", "", tableName.toLowerCase(), false, false);
		if ((resultSet != null) && (resultSet.next())) {
			do {
				if (indexName.equalsIgnoreCase(resultSet.getString("INDEX_NAME")))
					return true;
			} while (resultSet.next());
			resultSet.close();
			return false;
		}
		
		resultSet = metaData.getIndexInfo("", "", tableName.toUpperCase(), false, false);
		if ((resultSet != null) && (resultSet.next())) {
			do {
				if (indexName.equalsIgnoreCase(resultSet.getString("INDEX_NAME")))
					return true;
			} while (resultSet.next());
			resultSet.close();
		}
		return false;
	}

	public boolean checkUniqueKeyExists(Connection conn, String tableName, String uniqueKeyName) throws Exception {
		DatabaseMetaData metaData = conn.getMetaData();

		ResultSet resultSet = metaData.getIndexInfo("", "", tableName.toLowerCase(), false, false);
		if ((resultSet != null) && (resultSet.next())) {
			do {
				if (uniqueKeyName.equalsIgnoreCase(resultSet.getString("INDEX_NAME")))
					return true;
			} while (resultSet.next());
			resultSet.close();
			return false;
		}
		
		resultSet = metaData.getIndexInfo("", "", tableName.toUpperCase(), false, false);
		if ((resultSet != null) && (resultSet.next())) {
			do {
				if (uniqueKeyName.equalsIgnoreCase(resultSet.getString("INDEX_NAME")))
					return true;
			} while (resultSet.next());
			resultSet.close();
		}
		return false;
	}

	public String[] getColumnNamesFromTable(Connection conn, String tableName) throws SQLException, Exception {
		String tableToSearch = tableName.toLowerCase();
		if (!checkTableExists(conn, tableToSearch)) {
			tableToSearch = tableName.toUpperCase();
		}
		List<String> result = new ArrayList<String>();
		ResultSet columns = conn.getMetaData().getColumns(null, null, tableToSearch, "%");
		while (columns.next()) {
			result.add(columns.getString("COLUMN_NAME"));
		}
		return result.toArray(new String[] {});
	}

	public boolean supportsPrimaryKeyConstraintOnTableCreate() {
		return true;
	}

	public boolean requiresNamedPrimaryKeyConstraints() {
		return false;
	}

	public boolean requiresUniqueConstraintCreationOnTableCreate() {
		return false;
	}

	public boolean requiresForeignKeyConstraintCreationOnTableCreate() {
		return false;
	}

	public String getDefaultTableCreateSuffix() {
		return "";
	}

	public boolean supportsUniqueKeyConstraints() {
		return true;
	}

	public boolean supportsForeignKeyConstraints() {
		return true;
	}

	public boolean supportsDropForeignKeyConstraints() {
		return true;
	}

	public int getMaxUniqueKeyNameSize() {
		return getMaxColumnNameSize();
	}

	public int getMaxColumnNameSize() {
		return 50;
	}

	public int getMaxForeignKeyNameSize() {
		return getMaxColumnNameSize();
	}
	
	public int getMaxPrimaryKeyNameSize() {
		return getMaxColumnNameSize();
	}

	public boolean supportsDeleteOnCascade() {
		return supportsForeignKeyConstraints();
	}

	public boolean supportsIdentity() {
		return false;
	}

	public boolean useColumnDefinitionForIdentity() {
		return true;
	}

	public boolean requiresTableInIndexDropDDL() {
		return false;
	}

	public String getStartDelimiter() {
		return startDelimiter;
	}

	public void setStartDelimiter(String startDelimiter) {
		this.startDelimiter = startDelimiter;
	}

	public String getEndDelimiter() {
		return endDelimiter;
	}

	public void setEndDelimiter(String endDelimiter) {
		this.endDelimiter = endDelimiter;
	}

	public int getMaxIndexKeyNameSize() {
		return getMaxColumnNameSize();
	}

	public String getIdentifierQuoteCharacter() {
		return "";
	}

	public String getBatchDelimiterString() {
		return "; ";
	}

	public String getForeignKeyDeletionString() {
		return " DROP CONSTRAINT ";
	}

	public String getUniqueKeyDeletionString() {
		return " DROP CONSTRAINT ";
	}

	public String getStartCommentString() {
		return "/*";
	}

	public String getEndCommentString() {
		return "*/";
	}

	public char getOpenQuote() {
		return '"';
	}

	public char getCloseQuote() {
		return '"';
	}

	public final String quote(String name) {
		if (name == null) {
			return null;
		}

		if (name.charAt(0) == '`') {
			return getOpenQuote() + name.substring(1, name.length() - 1) + getCloseQuote();
		} else {
			return name;
		}
	}

	public Blob createTemporaryBlob(Connection connection, byte[] bytes) throws Exception {
		return null;
	}

	public Clob createTemporaryClob(Connection connection, byte[] bytes) throws Exception {
		return null;
	}

	public boolean supportInCondition() {
		return false;
	}

	public CallableStatement prepareCallableStatement(Connection connection, CallableType type, String name,
			Object[] inputParameters, String[] outputParametersName, int[] outputTypes, int queryTimeOut,
			boolean showSql, String clientId) throws Exception {
		return null;
	}

	/**
	 * Nome do dialeto
	 * 
	 * @return
	 */
	public String name() {
		return "";
	}

	/**
	 * SEQUENCES
	 */

	public boolean supportsSequences() {
		return false;
	}

	public String getSequenceNextValString(String sequenceName) throws Exception {
		return "";
	}

	public String getIdentitySelectString() {
		return "";
	}

	/**
	 * LOCK MANAGEMENT
	 */

	/**
	 * Retorna se a exception ocorrida no banco de dados é resultado de uma
	 * tentativa de lock com WAIT.
	 * 
	 * @param e
	 *            Exception
	 * @return true se o erro ocorrido foi porque expirou o tempo numa tentativa
	 *         de bloqueio no banco de dados.
	 */
	public boolean isLockTimeoutException(RuntimeException e) {
		return false;
	}

	/**
	 * Retorna se o dialeto do banco de dados aceita SELECT DISTINCT com FOR
	 * UPDATE.
	 * 
	 * @return true se aceita.
	 */
	public boolean isForUpdateCompatibleWithDistinct() {
		return true;
	}

	/**
	 * Retorna string FOR UPDATE para bloqueio de registro caso seja suportado
	 * pelo dialoeto do banco de dados.
	 */
	public String getSelectForUpdateString() {
		return " FOR UPDATE";
	}

	/**
	 * Retorna string FOR UPDATE para bloqueio de colunas caso seja suportado
	 * pelo dialeto do banco de dados.
	 */
	public String getSelectForUpdateOfString() {
		return " FOR UPDATE OF ";
	}

	/**
	 * Retorna string NOWAIT (não aguardar) caso o dialeto do banco de dados
	 * suporte.
	 */
	public String getNoWaitString() {
		return " NOWAIT";
	}

	/**
	 * Retorna string FOR UPDATE com NOWAIT(não aguardar) caso o dialeto do
	 * banco de dados suporte.
	 */
	public String getSelectForUpdateNoWaitString() {
		return getSelectForUpdateString() + getNoWaitString();
	}

	/**
	 * Retorna se o dialeto suporta LOCK de tabelas individuais.
	 * 
	 * @return
	 */
	public boolean supportsIndividualTableLocking() {
		return true;
	}

	/**
	 * Retorna se o dialeto suporta LOCK com várias tabelas.
	 * 
	 * @return true se suporta
	 */
	public boolean supportsLockingQueriesWithMultipleTables() {
		return true;
	}

	/**
	 * Retorna os nomes das stored procedures
	 * 
	 * @param connection
	 *            Conexão
	 * @return Lista de stored procedures
	 * @throws SQLException
	 */
	public Set<String> getStoredProcedureNames(Connection connection) throws Exception {
		return getStoredProcedureNames(connection, "%");
	}

	/**
	 * Retorna os nomes das stored procedures
	 * 
	 * @param connection
	 *            Conexão
	 * @param procedureNamePattern
	 *            Filtro
	 * @return Lista de stored procedures
	 * @throws SQLException
	 */
	public Set<String> getStoredProcedureNames(Connection connection, String procedureNamePattern) throws Exception {
		Set<String> result = new HashSet<String>();
		DatabaseMetaData metaData = connection.getMetaData();
		metaData.getProcedures(getDefaultCatalog(), getDefaultSchema(), procedureNamePattern);
		ResultSet procedureMetaData = metaData.getProcedures(getDefaultCatalog(), getDefaultSchema(),
				procedureNamePattern);
		if ((procedureMetaData != null) && (procedureMetaData.next())) {
			do {
				if (procedureMetaData.getInt(PROCEDURE_TYPE) == getIndexTypeOfProcedureMetadata())
					result.add(procedureMetaData.getString(PROCEDURE_NAME));
			} while (procedureMetaData.next());
			procedureMetaData.close();
		}
		return result;
	}

	/**
	 * Retorna os nomes das functions
	 * 
	 * @param connection
	 *            Conexão
	 * @return Lista de functions
	 * @throws SQLException
	 */
	public Set<String> getStoredFunctionNames(Connection connection) throws Exception {
		return getStoredFunctionNames(connection, "%");
	}

	/**
	 * Retorna os nomes das functions
	 * 
	 * @param connection
	 *            Conexão
	 * @param functionNamePattern
	 *            Filtro
	 * @return Lista de functions
	 * @throws SQLException
	 */
	public Set<String> getStoredFunctionNames(Connection connection, String functionNamePattern) throws Exception {
		Set<String> result = new HashSet<String>();
		DatabaseMetaData metaData = connection.getMetaData();
		ResultSet functionMetaData = metaData.getProcedures(getDefaultCatalog(), getDefaultSchema(),
				functionNamePattern);
		if ((functionMetaData != null) && (functionMetaData.next())) {
			do {
				if (functionMetaData.getInt(PROCEDURE_TYPE) == getIndexTypeOfFunctionMetadata())
					result.add(functionMetaData.getString(PROCEDURE_NAME));
			} while (functionMetaData.next());
			functionMetaData.close();
		}
		return result;
	}

	/**
	 * Retorna uma lista de objetos StoredProcedureSchema
	 * 
	 * @param connection
	 *            Conexão
	 * @return Lista de Stored Procedures
	 * @throws SQLException
	 */
	public Set<StoredProcedureSchema> getStoredProcedures(Connection connection) throws Exception {
		return getStoredProcedures(connection, "%", false);
	}

	/**
	 * Retorna uma lista de objetos StoredProcedureSchema
	 * 
	 * @param connection
	 *            Conexão
	 * @param getParameters
	 *            Com parâmetros
	 * @return Lista de Stored Procedures
	 * @throws SQLException
	 */
	public Set<StoredProcedureSchema> getStoredProcedures(Connection connection, boolean getParameters)
			throws Exception {
		return getStoredProcedures(connection, "%", getParameters);
	}

	/**
	 * Retorna uma lista de objetos StoredProcedureSchema
	 * 
	 * @param connection
	 *            Conexão
	 * @param procedureNamePattern
	 *            Filtro
	 * @return Lista de Stored procedures
	 * @throws SQLException
	 */
	public Set<StoredProcedureSchema> getStoredProcedures(Connection connection, String procedureNamePattern,
			boolean getParameters) throws Exception {
		Set<StoredProcedureSchema> result = new HashSet<StoredProcedureSchema>();
		DatabaseMetaData metaData = connection.getMetaData();
		ResultSet procedureMetaData = metaData.getProcedures(getDefaultCatalog(), getDefaultSchema(),
				procedureNamePattern);
		if (procedureMetaData != null) {
			while (procedureMetaData.next()) {
				if (procedureMetaData.getInt(PROCEDURE_TYPE) == getIndexTypeOfProcedureMetadata()) {
					StoredProcedureSchema storedProcedureSchema = (StoredProcedureSchema) readStoredProcedure(metaData,
							procedureMetaData, getIndexTypeOfProcedureMetadata(), getParameters);
					result.add(storedProcedureSchema);
				}
			}
			procedureMetaData.close();
		}
		return result;
	}

	public int getIndexTypeOfProcedureMetadata() {
		return PROCEDURE;
	}

	public int getIndexTypeOfFunctionMetadata() {
		return FUNCTION;
	}

	/**
	 * Lê os dados da Stored Procedure/Function
	 * 
	 * @param metaData
	 *            Metadata
	 * @param procedureMetaData
	 *            ResultSet contendo dados da stored procedure/function
	 * @param type
	 *            tipo
	 * @return Objeto StoredProcedureSchema
	 * @throws SQLException
	 */
	protected StoredProcedureSchema readStoredProcedure(DatabaseMetaData metaData, ResultSet procedureMetaData,
			int type, boolean getParameters) throws Exception {
		StoredProcedureSchema storedProcedureSchema;
		if (type == getIndexTypeOfProcedureMetadata())
			storedProcedureSchema = new StoredProcedureSchema();
		else
			storedProcedureSchema = new StoredFunctionSchema();

		storedProcedureSchema.setName(procedureMetaData.getString(PROCEDURE_NAME));

		if (getParameters) {
			ResultSet resultSet = metaData.getProcedureColumns(getDefaultCatalog(), getDefaultSchema(),
					storedProcedureSchema.getName(), null);
			if ((resultSet != null) && (resultSet.next())) {
				do {
					String parameterName = resultSet.getString(COLUMN_NAME);
					short parameterType = resultSet.getShort(COLUMN_TYPE);
					String parameterTypeName = resultSet.getString(TYPE_NAME);
					int parameterPrecision = resultSet.getInt(PRECISION);
					short parameterScale = resultSet.getShort(SCALE);
					String parameterNullable = resultSet.getString(NULLABLE);
					int dataType = resultSet.getInt(DATA_TYPE);

					StoredParameterSchema parameterSchema = new StoredParameterSchema();
					parameterSchema.setName(parameterName);
					parameterSchema.setSize(parameterPrecision);
					parameterSchema.setSubSize(parameterScale);
					parameterSchema.setTypeSql(parameterTypeName);
					parameterSchema.setNullable(parameterNullable.equals(0) ? true : false);
					parameterSchema.setDataTypeSql(dataType);
					switch (parameterType) {
					case DatabaseMetaData.procedureColumnIn:
						parameterSchema.setParameterType(StoredParameterType.IN);
						break;
					case DatabaseMetaData.procedureColumnOut:
						parameterSchema.setParameterType(StoredParameterType.OUT);
						break;
					case DatabaseMetaData.procedureColumnInOut:
						parameterSchema.setParameterType(StoredParameterType.IN);
						break;
					case DatabaseMetaData.procedureColumnReturn:
						parameterSchema.setParameterType(StoredParameterType.RETURN_VALUE);
						break;
					case DatabaseMetaData.procedureColumnResult:
						parameterSchema.setParameterType(StoredParameterType.RETURN_RESULTSET);
					default:
						parameterSchema.setParameterType(StoredParameterType.IN);
					}
					storedProcedureSchema.addParameter(parameterSchema);

				} while (resultSet.next());
				resultSet.close();
			}
		}
		return storedProcedureSchema;
	}

	/**
	 * Retorna uma lista de objetos StoredFunctionSchema
	 * 
	 * @param connection
	 *            Conexão
	 * @return Lista de functions
	 * @throws SQLException
	 */
	public Set<StoredFunctionSchema> getStoredFunctions(Connection connection) throws Exception {
		return getStoredFunctions(connection, "%", false);
	}

	/**
	 * Retorna uma lista de objetos StoredFunctionSchema
	 * 
	 * @param connection
	 *            Conexão
	 * @return Lista de functions
	 * @throws SQLException
	 */
	public Set<StoredFunctionSchema> getStoredFunctions(Connection connection, boolean getParameters) throws Exception {
		return getStoredFunctions(connection, "%", getParameters);
	}

	/**
	 * Retorna uma lista de objetos StoredFunctionSchema
	 * 
	 * @param connection
	 *            Conexão
	 * @param functionNamePattern
	 *            Filtro
	 * @return Lista de functions
	 * @throws SQLException
	 */
	public Set<StoredFunctionSchema> getStoredFunctions(Connection connection, String functionNamePattern,
			boolean getParameters) throws Exception {
		Set<StoredFunctionSchema> result = new HashSet<StoredFunctionSchema>();
		DatabaseMetaData metaData = connection.getMetaData();
		ResultSet functionMetaData = metaData.getProcedures(getDefaultCatalog(), getDefaultSchema(),
				functionNamePattern);
		if (functionMetaData != null) {
			while (functionMetaData.next()) {
				if (functionMetaData.getInt(PROCEDURE_TYPE) == getIndexTypeOfFunctionMetadata()) {
					StoredFunctionSchema storedFunctionSchema = (StoredFunctionSchema) readStoredProcedure(metaData,
							functionMetaData, getIndexTypeOfFunctionMetadata(), getParameters);
					result.add(storedFunctionSchema);
				}
			}
			functionMetaData.close();
		}
		return result;
	}

	// public abstract List<ProcedureMetadata> getNativeFunctions() throws
	// Exception;
}
