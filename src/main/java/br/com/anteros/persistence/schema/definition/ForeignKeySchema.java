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

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class ForeignKeySchema extends ConstraintSchema {

	protected final Set<ColumnSchema> columnsReferences = new LinkedHashSet<ColumnSchema>();
	protected TableSchema referencedTable;
	protected boolean cascadeOnDelete;

	public ForeignKeySchema(TableSchema table, String name, ColumnSchema sourceColumn, ColumnSchema referencedColumn,
			TableSchema referencedTable) {
		super();
		this.name = name;
		this.setTable(table);
		this.addColumns(sourceColumn, referencedColumn);
		this.setReferencedTable(referencedTable);
	}

	public ForeignKeySchema(TableSchema table, String name) {
		super();
		this.name = name;
		this.setTable(table);
	}

	public ForeignKeySchema(TableSchema table) {
		super();
		this.setTable(table);
	}

	public void addColumns(ColumnSchema sourceColumn, ColumnSchema referencedColumn) {
		if (sourceColumn != null) {
			sourceColumn.setForeignKey(true);
			getColumns().add(sourceColumn);
			columnsReferences.add(referencedColumn);
		}
	}

	public void addSourceColumn(ColumnSchema sourceColumn) {
		if (sourceColumn != null) {
			sourceColumn.setForeignKey(true);
			getColumns().add(sourceColumn);
		}
	}

	public void addReferencedColumn(ColumnSchema referencedColumn) {
		columnsReferences.add(referencedColumn);
	}

	public Set<ColumnSchema> getColumnsReferences() {
		return Collections.unmodifiableSet(columnsReferences);
	}

	public TableSchema getReferencedTable() {
		return referencedTable;
	}

	public void setReferencedTable(TableSchema referencedTable) {
		this.referencedTable = referencedTable;
	}

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws Exception {
		return session.getDialect().writeAddForeignKeyDDLStatement(this, schemaWriter);
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception {
		return session.getDialect().writeDropForeignKeyDDLStatement(this, schemaWriter);
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws SchemaGeneratorException {
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws SchemaGeneratorException {
	}

	public boolean isCascadeOnDelete() {
		return cascadeOnDelete;
	}

	public void setCascadeOnDelete(boolean cascadeOnDelete) {
		this.cascadeOnDelete = cascadeOnDelete;
	}

	public String getColumnsToString() {
		String result = "";
		boolean appendDelimiter = false;
		for (ColumnSchema columnSchema : columns) {
			if (appendDelimiter)
				result += "_";
			result += columnSchema.getName();
			appendDelimiter = true;
		}

		return result;
	}

	public String[] getColumnNames() {
		List<String> result = new ArrayList<String>();
		for (ColumnSchema columnSchema : columns) {
			result.add(columnSchema.name);
		}
		return result.toArray(new String[] {});
	}

}
