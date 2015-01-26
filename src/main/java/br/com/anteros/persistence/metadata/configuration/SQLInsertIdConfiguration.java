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

import br.com.anteros.persistence.metadata.annotation.SQLInsertId;

public class SQLInsertIdConfiguration {

	private String parameterId;

	private String columnName;

	public SQLInsertIdConfiguration(String parameterId, String columnName) {
		this.parameterId = parameterId;
		this.columnName = columnName;
	}

	public SQLInsertIdConfiguration(SQLInsertId id) {
		this.parameterId = id.parameterId();
		this.columnName = id.columnName();
	}

	public String getParameterId() {
		return parameterId;
	}

	public SQLInsertIdConfiguration parameterId(String parameterId) {
		this.parameterId = parameterId;
		return this;
	}

	public String getColumnName() {
		return columnName;
	}

	public SQLInsertIdConfiguration columnName(String columnName) {
		this.columnName = columnName;
		return this;
	}

}
