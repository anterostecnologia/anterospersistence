/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.metadata.configuration;

public class UniqueConstraintConfiguration {

	private String name;
	private String[] columnNames;

	public UniqueConstraintConfiguration() {

	}

	public UniqueConstraintConfiguration(String name, String[] columnNames) {
		this.name = name;
		this.columnNames = columnNames;
	}

	public String getName() {
		return name;
	}

	public UniqueConstraintConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public UniqueConstraintConfiguration columns(String[] columnNames) {
		this.columnNames = columnNames;
		return this;
	}

}
