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

public class JoinColumnConfiguration {
	private String referencedColumnName = "";
	private String name;
	private String columnDefinition ="";
	private boolean insertable = true;
	private boolean updatable = true;
	private boolean unique = false;
	
	public JoinColumnConfiguration() {
		
	}
	
	public JoinColumnConfiguration(String name, String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
		this.name = name;
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	public String getName() {
		return name;
	}

	public JoinColumnConfiguration referencedColumnName(String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
		return this;
	}

	public JoinColumnConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public JoinColumnConfiguration columnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
		return this;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public JoinColumnConfiguration insertable(boolean insertable) {
		this.insertable = insertable;
		return this;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public JoinColumnConfiguration updatable(boolean updatable) {
		this.updatable = updatable;
		return this;
	}

	public boolean isUnique() {
		return unique;
	}

	public JoinColumnConfiguration unique(boolean unique) {
		this.unique = unique;
		return this;
	}
	
}
