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
package br.com.anteros.persistence.metadata.configuration;

import br.com.anteros.persistence.metadata.annotation.TableGenerator;

public class TableGeneratorConfiguration {

	private String name;
	private int allocationSize = 1;
	private String catalog = "";
	private int initialValue = 1;
	private String pkColumnName;
	private String valueColumnName;
	private String value="";
	private String schema = "";

	public TableGeneratorConfiguration(String tableName, int allocationSize, String catalog, int initialValue, String pkColumnName,
			String valueColumnName, String value, String schema) {
		this.name = tableName;
		this.allocationSize = allocationSize;
		this.catalog = catalog;
		this.initialValue = initialValue;
		this.pkColumnName = pkColumnName;
		this.valueColumnName = valueColumnName;
		this.value = value;
		this.schema = schema;
	}

	public TableGeneratorConfiguration(TableGenerator tableGenerator) {
		this.name = tableGenerator.name();
		this.allocationSize = tableGenerator.allocationSize();
		this.catalog = tableGenerator.catalog();
		this.initialValue = tableGenerator.initialValue();
		this.pkColumnName = tableGenerator.pkColumnName();
		this.valueColumnName = tableGenerator.valueColumnName();
		this.value = tableGenerator.value();
		this.schema = tableGenerator.schema();
	}

	public TableGeneratorConfiguration tableName(String tableName) {
		this.name = tableName;
		return this;
	}

	public TableGeneratorConfiguration allocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
		return this;
	}

	public TableGeneratorConfiguration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public TableGeneratorConfiguration initialValue(int initialValue) {
		this.initialValue = initialValue;
		return this;
	}

	public TableGeneratorConfiguration pkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
		return this;
	}

	public TableGeneratorConfiguration valueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
		return this;
	}

	public TableGeneratorConfiguration value(String value) {
		this.value = value;
		return this;
	}

	public TableGeneratorConfiguration schema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getName() {
		return name;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public String getCatalog() {
		return catalog;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public String getValue() {
		return value;
	}

	public String getSchema() {
		return schema;
	}
}
