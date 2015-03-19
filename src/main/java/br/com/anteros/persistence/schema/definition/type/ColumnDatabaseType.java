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
package br.com.anteros.persistence.schema.definition.type;

public class ColumnDatabaseType {

	protected String name;
	protected int defaultSize;
	protected int defaultSubSize;
	protected boolean isSizeAllowed;
	protected boolean isSizeRequired;
	protected int maxLength;
	protected int maxPrecision;
	protected int maxScale;
	protected int minScale;
	protected boolean allowsNull;
	protected int sqlType;

	public ColumnDatabaseType() {
		defaultSize = 10;
		isSizeRequired = false;
		isSizeAllowed = true;
		maxLength = 10;
		allowsNull = true;
	}

	public ColumnDatabaseType(String databaseTypeName, int sqlType) {
		this();
		name = databaseTypeName;
		this.sqlType = sqlType;
	}

	public ColumnDatabaseType(String databaseTypeName, int defaultSize, int sqlType) {
		this();
		this.name = databaseTypeName;
		this.defaultSize = defaultSize;
		this.isSizeRequired = true;
		this.maxLength = defaultSize;
		this.sqlType = sqlType;
	}

	public ColumnDatabaseType(String databaseTypeName, int defaultSize, int defaultSubSize, int sqlType) {
		this();
		this.name = databaseTypeName;
		this.defaultSize = defaultSize;
		this.defaultSubSize = defaultSubSize;
		this.isSizeRequired = true;
		this.maxLength = defaultSize;
		this.sqlType = sqlType;
	}

	public ColumnDatabaseType(String databaseTypeName, boolean allowsSize, int sqlType) {
		this();
		this.name = databaseTypeName;
		this.isSizeAllowed = allowsSize;
		this.sqlType = sqlType;
	}

	public ColumnDatabaseType(String databaseTypeName, boolean allowsSize, boolean allowsNull, int sqlType) {
		this(databaseTypeName, allowsSize, sqlType);
		this.allowsNull = allowsNull;
		this.sqlType = sqlType;
	}

	public ColumnDatabaseType setLimits(int maxLength, int minScale, int maxScale) {
		this.maxLength = maxLength;
		this.minScale = minScale;
		this.maxScale = maxScale;
		return this;
	}

	

	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDefaultSize() {
		return defaultSize;
	}

	public void setDefaultSize(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	public int getDefaultSubSize() {
		return defaultSubSize;
	}

	public void setDefaultSubSize(int defaultSubSize) {
		this.defaultSubSize = defaultSubSize;
	}

	public boolean isSizeAllowed() {
		return isSizeAllowed;
	}

	public void setSizeAllowed(boolean isSizeAllowed) {
		this.isSizeAllowed = isSizeAllowed;
	}

	public boolean isSizeRequired() {
		return isSizeRequired;
	}

	public void setSizeRequired(boolean isSizeRequired) {
		this.isSizeRequired = isSizeRequired;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxPrecision() {
		return maxPrecision;
	}

	public void setMaxPrecision(int maxPrecision) {
		this.maxPrecision = maxPrecision;
	}

	public int getMaxScale() {
		return maxScale;
	}

	public void setMaxScale(int maxScale) {
		this.maxScale = maxScale;
	}

	public int getMinScale() {
		return minScale;
	}

	public void setMinScale(int minScale) {
		this.minScale = minScale;
	}

	public boolean isAllowsNull() {
		return allowsNull;
	}

	public void setAllowsNull(boolean allowsNull) {
		this.allowsNull = allowsNull;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}
}
