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

import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;

public class DescriptionGenerator {

	private GeneratedType generatedType;
	private String catalog;
	private int initialValue;
	private String pkColumnName;
	private String value;
	private String schema;
	private String tableName;
	private String valueColumnName;
	private String sequenceName;
	private int startsWith = 1;

	public DescriptionGenerator() {
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}

	public String getCatalog() {
		return catalog;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public String getSchema() {
		return schema;
	}

	public String getTableName() {
		return tableName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public boolean isSequenceGenerator() {
		return this.generatedType == GeneratedType.SEQUENCE;
	}

	public boolean isTableGenerator() {
		return this.generatedType == GeneratedType.TABLE;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public GeneratedType getGeneratedType() {
		return generatedType;
	}

	public void setGeneratedType(GeneratedType generatedType) {
		this.generatedType = generatedType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((generatedType == null) ? 0 : generatedType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DescriptionGenerator other = (DescriptionGenerator) obj;
		if (generatedType != other.generatedType)
			return false;
		return true;
	}

	public void setStartsWith(int startsWith) {
		this.startsWith = startsWith;
	}

	public int getStartsWith() {
		return startsWith;
	}

}
