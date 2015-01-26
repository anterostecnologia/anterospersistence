/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.schema.definition;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class TableSchema extends ObjectSchema {

	protected Schema schema;
	protected final List<ColumnSchema> columns = new ArrayList<ColumnSchema>();
	protected PrimaryKeySchema primaryKey;
	protected final List<ForeignKeySchema> foreignKeys = new ArrayList<ForeignKeySchema>();
	protected final List<UniqueKeySchema> uniqueKeys = new ArrayList<UniqueKeySchema>();
	protected final List<IndexSchema> indexes = new ArrayList<IndexSchema>();
	protected final List<TriggerSchema> triggers = new ArrayList<TriggerSchema>();
	protected String createTableSuffix = "";
	protected String alias;

	protected String comment;

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public List<ColumnSchema> getColumns() {
		return columns;
	}

	public List<IndexSchema> getIndexes() {
		return indexes;
	}

	public PrimaryKeySchema getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKeySchema primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<ForeignKeySchema> getForeignKeys() {
		return foreignKeys;
	}

	public List<TriggerSchema> getTriggers() {
		return triggers;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean hasComment() {
		return comment != null && !comment.equalsIgnoreCase(name);
	}

	public void addColumn(String columnName, Class<?> type) {
		this.addColumn(new ColumnSchema(columnName, type));
	}

	public void addColumn(String columnName, Class<?> type, int length) {
		this.addColumn(new ColumnSchema(columnName, type, length));
	}

	public void addColumn(String columnName, Class<?> type, int length, int precision) {
		this.addColumn(new ColumnSchema(columnName, type, length, precision));
	}

	public void addColumn(String columnName, String typeName) {
		addColumn(new ColumnSchema(columnName, typeName));
	}

	public void addColumn(ColumnSchema column) {
		column.setTable(this);
		getColumns().add(column);
	}

	public void addForeignKey(String name, ColumnSchema sourceColumn, ColumnSchema referencedColumn,
			TableSchema referencedTable) {
		ForeignKeySchema foreignKey = new ForeignKeySchema(this, name, sourceColumn, referencedColumn, referencedTable);
		addForeignKey(foreignKey);
	}

	public void addUniqueKey(String name, ColumnSchema column) {
		UniqueKeySchema uniqueKey = new UniqueKeySchema(name, column);
		addUniqueKey(uniqueKey);
	}

	public void addUniqueKey(String name, ColumnSchema[] columns) {
		UniqueKeySchema uniqueKey = new UniqueKeySchema(name, columns);
		addUniqueKey(uniqueKey);
	}

	public void addForeignKey(ForeignKeySchema foreignKey) {
		foreignKeys.add(foreignKey);
	}

	public void addUniqueKey(UniqueKeySchema uniqueKey) {
		getUniqueKeys().add(uniqueKey);
	}

	public void addIndex(IndexSchema index) {
		getIndexes().add(index);
	}

	public void addAutoIncrementColumn(String columnName, Class<?> type) {
		ColumnSchema column = new ColumnSchema(columnName, type);
		column.setAutoIncrement(true);
		addColumn(column);
	}

	public void addAutoIncrementColumn(String columnName, Class<?> type, int length) {
		ColumnSchema column = new ColumnSchema(columnName, type, length);
		column.setAutoIncrement(true);
		addColumn(column);
	}

	public void addPrimaryKey(String columnName, Class<?> type) {
		ColumnSchema column = new ColumnSchema(columnName, type);
		addPrimaryKey(column);
	}

	public void addPrimaryKey(String columnName, Class<?> type, int length) {
		ColumnSchema column = new ColumnSchema(columnName, type, length);
		addPrimaryKey(column);
	}

	public void addPrimaryKey(ColumnSchema column) {
		if (primaryKey == null) {
			primaryKey = new PrimaryKeySchema();
			primaryKey.setTable(this);
		}
		primaryKey.addColumn(column);
	}

	public List<UniqueKeySchema> getUniqueKeys() {
		return uniqueKeys;
	}

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws SchemaGeneratorException,
			Exception {
		session.getDialect().writeCreateTableDDLStatement(this, schemaWriter);
		return schemaWriter;
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception {
		session.getDialect().writeDropTableDDLStatement(this, schemaWriter);
		return schemaWriter;
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws Exception {
		boolean printLineSeparator = false;
		for (IndexSchema index : getIndexes()) {
			if (createSchemaWriter == null) {
				index.createOnDatabase(session);
			} else {
				index.createObject(session, createSchemaWriter);
				createSchemaWriter.write(session.getDialect().getBatchDelimiterString() + "\n");
				printLineSeparator = true;
			}
		}
		if (printLineSeparator)
			createSchemaWriter.write("\n");
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws Exception {
		for (IndexSchema index : getIndexes()) {
			if (dropSchemaWriter == null) {
				index.dropFromDatabase(session);
			} else {
				index.dropObject(session, dropSchemaWriter);
				dropSchemaWriter.write(session.getDialect().getBatchDelimiterString() + "\n");
			}
		}
	}

	public void addColumnOnDatabase(SQLSession session, ColumnSchema columnSchema) throws Exception {
		String ddl = session.getDialect().writerAddColumnDDLStatement(columnSchema, new StringWriter()).toString();
		session.executeDDL(ddl);
	}

	public void addColumn(SQLSession session, ColumnSchema columnSchema, Writer createSchemaWriter) throws Exception {
		String ddl = session.getDialect().writerAddColumnDDLStatement(columnSchema, new StringWriter()).toString();
		createSchemaWriter.write(ddl);
	}

	@Override
	public TableSchema setName(String name) {
		return (TableSchema) super.setName(name);
	}

	public String getCreateTableSuffix() {
		return createTableSuffix;
	}

	public void setCreateTableSufix(String createTableSuffix) {
		this.createTableSuffix = createTableSuffix;
	}

	public boolean existsColumn(ColumnSchema newColumn) {
		for (ColumnSchema columnSchema : columns) {
			if (columnSchema.getName().equalsIgnoreCase(newColumn.getName()))
				return true;
		}
		return false;
	}

	public boolean existsForeignKey(ForeignKeySchema foreignKeySchema) {
		for (ForeignKeySchema fk : foreignKeys) {
			if (fk.getColumnsToString().equals(foreignKeySchema.getColumnsToString()))
				return true;
		}
		return false;
	}

	public boolean existsIndex(IndexSchema indexSchema) {
		for (IndexSchema idx : indexes) {
			if (idx.getColumnsToString().equals(indexSchema.getColumnsToString()))
				return true;
		}
		return false;
	}

	public boolean existsIndex(String[] columns) {
		boolean found = false;
		for (IndexSchema idx : indexes) {
			found = false;
			for (String column : columns) {
				if (idx.getColumnNames().contains(column)) {
					found = true;
				} else
					break;
			}
			if (found)
				break;
		}
		return found;
	}

	public boolean existsUniqueKey(UniqueKeySchema uniqueKeySchema) {
		for (UniqueKeySchema idx : uniqueKeys) {
			if (idx.getColumnsToString().equals(uniqueKeySchema.getColumnsToString()))
				return true;
		}
		return false;
	}

	public boolean depends(TableSchema tableSchema) {
		for (ForeignKeySchema foreignKeySchema : getForeignKeys()) {
			if (foreignKeySchema.referencedTable.getName().equalsIgnoreCase(tableSchema.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean existsForeignKeyByName(String foreignKeyName) {
		return false;
	}
	
	public List<ConstraintSchema> getConstraints(){
		List<ConstraintSchema> result = new ArrayList<ConstraintSchema>();
		if (primaryKey!=null)
		result.add(primaryKey);
		result.addAll(foreignKeys);
		
		return result;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public ColumnSchema getColumn(String columnName) {
		if (columns!=null){
			for (ColumnSchema column : columns){
				if (column.getName().equalsIgnoreCase(columnName)){
					return column;
				}
			}
		}
		return null;
	}

}
