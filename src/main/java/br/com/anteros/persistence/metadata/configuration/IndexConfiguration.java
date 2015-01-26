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

public class IndexConfiguration {

	private String name;
	private String schema;
	private String catalog;
	private String[] columnNames;
	private boolean unique;

	public IndexConfiguration() {

	}

	public IndexConfiguration(String name, String[] columnNames) {
		this.name = name;
		this.columnNames = columnNames;
	}

	public String getName() {
		return name;
	}

	public IndexConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public IndexConfiguration columns(String[] columnNames) {
		this.columnNames = columnNames;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public IndexConfiguration schema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getCatalog() {
		return catalog;
	}

	public IndexConfiguration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public IndexConfiguration setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isUnique() {
		return unique;
	}

	public IndexConfiguration unique(boolean unique) {
		this.unique = unique;
		return this;
	}

	@Override
	public String toString() {
		String result = name + " => ";
		boolean appendDelimiter = false;
		for (String c : columnNames) {
			if (appendDelimiter)
				result += ",";
			result += c;
		}
		return result;
	}

}
