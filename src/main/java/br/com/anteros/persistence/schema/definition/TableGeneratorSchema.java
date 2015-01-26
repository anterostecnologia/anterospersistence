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

import br.com.anteros.persistence.session.SQLSession;

public class TableGeneratorSchema extends GeneratorSchema {

	private String pkColumnName;
	private String pkName;
	private String valueColumnName;
	private String catalogName;
	private String schemaName;

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws Exception {
		TableSchema tableSchema = new TableSchema();
		tableSchema.setName(this.getName());
		tableSchema.addColumn(new ColumnSchema().setName(pkColumnName).setSize(100).setType(String.class)
				.setTypeSql(session.getDialect().convertJavaToDatabaseType(String.class).getName()));
		tableSchema.addColumn(new ColumnSchema().setName(valueColumnName).setSize(12).setType(Integer.class)
				.setTypeSql(session.getDialect().convertJavaToDatabaseType(String.class).getName()));
		tableSchema.addPrimaryKey(new ColumnSchema().setName(pkColumnName));
		session.getDialect().writeCreateTableDDLStatement(tableSchema, schemaWriter);
		return schemaWriter;
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception {
		TableSchema tableSchema = new TableSchema();
		tableSchema.setName(this.getName());
		session.getDialect().writeDropTableDDLStatement(tableSchema, schemaWriter);
		return schemaWriter;
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws Exception {

	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws Exception {

	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getPkName() {
		return pkName;
	}

	public void setPkName(String pkName) {
		this.pkName = pkName;
	}

}
