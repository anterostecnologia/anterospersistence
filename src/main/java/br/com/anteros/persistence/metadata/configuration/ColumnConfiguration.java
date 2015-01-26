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

import br.com.anteros.persistence.metadata.annotation.Column;

public class ColumnConfiguration {
	private String name = "";
	private int length = 0;
	private int precision = 0;
	private int scale = 0;
	private boolean required = false;
	private boolean unique = false;
	private boolean updatable = true;
	private boolean insertable = true;
	private String inversedColumn = "";
	private String defaultValue = "";
	private String columnDefinition = "";
	private String datePattern = "";
	private String dateTimePattern = "";
	private String timePattern = "";

	public ColumnConfiguration() {
	}

	public ColumnConfiguration(Column column) {
		this.name(column.name()).defaultValue(column.defaultValue()).inversedColumn(column.inversedColumn()).length(column.length())
				.precision(column.precision()).scale(column.scale()).required(column.required()).columnDefinition(column.columnDefinition())
				.insertable(column.insertable()).updatable(column.updatable()).datePattern(column.datePattern())
				.dateTimePattern(column.dateTimePattern()).timePattern(column.timePattern());
	}

	public ColumnConfiguration(String name, int length, int precision, int scale, boolean required, String inversedColumn, boolean exportColumn,
			String defaultValue) {
		this.name = name;
		this.length = length;
		this.precision = precision;
		this.required = required;
		this.inversedColumn = inversedColumn;
		this.defaultValue = defaultValue;
		this.scale = scale;
	}

	public ColumnConfiguration(String name, int length, int precision, int scale, boolean required, String inversedColumn) {
		this.name = name;
		this.length = length;
		this.precision = precision;
		this.required = required;
		this.inversedColumn = inversedColumn;
		this.scale = scale;
	}

	public ColumnConfiguration(String name) {
		this.name = name;
	}

	public ColumnConfiguration(String name, int length, int precision, int scale, boolean required) {
		this.name = name;
		this.length = length;
		this.precision = precision;
		this.required = required;
		this.scale = scale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ColumnConfiguration other = (ColumnConfiguration) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public ColumnConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public int getLength() {
		return length;
	}

	public ColumnConfiguration length(int length) {
		this.length = length;
		return this;
	}

	public int getPrecision() {
		return precision;
	}

	public ColumnConfiguration precision(int precision) {
		this.precision = precision;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public ColumnConfiguration required(boolean required) {
		this.required = required;
		return this;
	}

	public String getInversedColumn() {
		return inversedColumn;
	}

	public ColumnConfiguration inversedColumn(String inversedColumn) {
		this.inversedColumn = inversedColumn;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public ColumnConfiguration defaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getScale() {
		return scale;
	}

	public ColumnConfiguration scale(int scale) {
		this.scale = scale;
		return this;
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public ColumnConfiguration columnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
		return this;
	}

	public boolean isUnique() {
		return unique;
	}

	public ColumnConfiguration unique(boolean unique) {
		this.unique = unique;
		return this;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public ColumnConfiguration updatable(boolean updatable) {
		this.updatable = updatable;
		return this;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public ColumnConfiguration insertable(boolean insertable) {
		this.insertable = insertable;
		return this;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public ColumnConfiguration datePattern(String datePattern) {
		this.datePattern = datePattern;
		return this;
	}

	public String getDateTimePattern() {
		return dateTimePattern;
	}

	public ColumnConfiguration dateTimePattern(String dateTimePattern) {
		this.dateTimePattern = dateTimePattern;
		return this;
	}

	public String getTimePattern() {
		return timePattern;
	}

	public ColumnConfiguration timePattern(String timePattern) {
		this.timePattern = timePattern;
		return this;
	}

}
