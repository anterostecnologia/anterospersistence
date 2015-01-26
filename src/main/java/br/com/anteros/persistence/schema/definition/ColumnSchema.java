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

import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class ColumnSchema extends ObjectSchema {

	/**
	 * Tipo de classe no Java
	 */
	protected Class<?> type;

	/**
	 * Tipo no banco de dados
	 */
	protected String typeSql;
	
	/**
	 * Tipo no banco de dados
	 */
	protected int dataTypeSql;

	/**
	 * Tamanho da coluna
	 */
	protected int size;

	/**
	 * Casas decimais para numéricos
	 */
	protected int subSize;

	/**
	 * Permite nulos
	 */
	protected Boolean nullable;

	/**
	 * Valor padrão
	 */
	protected Object defaultValue;

	/**
	 * Comentário da coluna
	 */
	protected String comment;

	/**
	 * Indicador se o campo é auto incremento
	 */
	protected boolean isAutoIncrement;

	/**
	 * Tipo de coluna definido pelo usuário
	 */
	protected String columnDefinition;
	
	/**
	 * Nome da sequence usada no campo para incremento
	 */
	protected String sequenceName;

	/**
	 * Tabela a qual pertence a coluna
	 */
	protected TableSchema table;
	
	/**
	 * Indica se a coluna é uma chave primária
	 */
	protected boolean primaryKey=false;
	
	/**
	 * Indica se a coluna é uma chave estrangeira 
	 */
	protected boolean foreignKey=false;

	public ColumnSchema() {
		this.name = "";
		this.size = 0;
		this.nullable = true;
		this.isAutoIncrement = false;
		this.columnDefinition = "";
	}

	public ColumnSchema(String name, Class<?> type) {
		this.name = name;
		this.type = type;
		this.size = 0;
		nullable = true;
		isAutoIncrement = false;
		this.columnDefinition = "";
	}

	public ColumnSchema(String name, Class<?> type, int size) {
		this();
		this.name = name;
		this.type = type;
		this.size = size;
	}

	public ColumnSchema(String name, Class<?> type, int size, int subSize) {
		this();
		this.name = name;
		this.type = type;
		this.size = size;
		this.subSize = subSize;
	}

	public ColumnSchema(String name, String typeSql) {
		this();
		this.name = name;
		this.typeSql = typeSql;
	}

	public Class<?> getType() {
		return type;
	}

	public ColumnSchema setType(Class<?> type) {
		this.type = type;
		return this;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
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

	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}

	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

	public int getSize() {
		return size;
	}

	public ColumnSchema setSize(int size) {
		this.size = size;
		return this;
	}

	public int getSubSize() {
		return subSize;
	}

	public void setSubSize(int subSize) {
		this.subSize = subSize;
	}

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws SchemaGeneratorException {
		return schemaWriter;
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws SchemaGeneratorException {
		return schemaWriter;
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws SchemaGeneratorException {
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws SchemaGeneratorException {
	}

	public String getTypeSql() {
		return typeSql;
	}

	public ColumnSchema setTypeSql(String typeSql) {
		this.typeSql = typeSql;
		return this;
	}

	@Override
	public ColumnSchema setName(String name) {
		return (ColumnSchema) super.setName(name);
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public ColumnSchema setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
		return this;
	}

	public TableSchema getTable() {
		return table;
	}

	public ColumnSchema setTable(TableSchema table) {
		this.table = table;
		return this;
	}

	public int getDataTypeSql() {
		return dataTypeSql;
	}

	public ColumnSchema setDataTypeSql(int dataTypeSql) {
		this.dataTypeSql = dataTypeSql;
		return this;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public ColumnSchema setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
		return this;
	}
	
	public boolean hasSequenceName(){
		return (sequenceName!=null) && !"".equals(sequenceName);
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public ColumnSchema setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
		return this;
	}

	public boolean isForeignKey() {
		return foreignKey;
	}

	public ColumnSchema setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
		return this;
	}

}
