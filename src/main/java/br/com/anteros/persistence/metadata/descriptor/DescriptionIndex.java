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
package br.com.anteros.persistence.metadata.descriptor;

import br.com.anteros.persistence.metadata.EntityCache;

public class DescriptionIndex {

	private EntityCache entityCache;
	private String name;
	private String[] columnNames;
	private String catalog;
	private String schema;
	private boolean unique;

	public DescriptionIndex(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public DescriptionIndex name(String name) {
		this.name = name;
		return this;
	}

	public DescriptionIndex columnNames(String[] columnNames) {
		this.columnNames = columnNames;
		return this;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public String getName() {
		return name;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public boolean isUnique() {
		return unique;
	}

	public String getCatalog() {
		return catalog;
	}

	public DescriptionIndex catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public DescriptionIndex schema(String schema) {
		this.schema = schema;
		return this;
	}

	public DescriptionIndex unique(boolean unique) {
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
