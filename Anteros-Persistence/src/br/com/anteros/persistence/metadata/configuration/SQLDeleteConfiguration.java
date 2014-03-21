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

import br.com.anteros.persistence.metadata.annotation.SQLDelete;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;

public class SQLDeleteConfiguration {

	private String sql;
	private boolean callable = false;
	private CallableType callableType = CallableType.PROCEDURE;
	private String successParameter = "";
	private String successValue = "";

	public SQLDeleteConfiguration(String sql, boolean callable, CallableType callableType, String successParameter,
			String successValue) {
		this.sql = sql;
		this.callable = callable;
		this.callableType = callableType;
		this.successParameter = successParameter;
		this.successValue = successValue;
	}

	public SQLDeleteConfiguration(SQLDelete sqlDelete) {
		this.sql = sqlDelete.sql();
		this.callable = sqlDelete.callable();
		this.callableType = sqlDelete.callableType();
		this.successParameter = sqlDelete.successParameter();
		this.successValue = sqlDelete.successValue();
	}

	public String getSql() {
		return sql;
	}

	public SQLDeleteConfiguration sql(String sql) {
		this.sql = sql;
		return this;
	}

	public boolean isCallable() {
		return callable;
	}

	public SQLDeleteConfiguration callable(boolean callable) {
		this.callable = callable;
		return this;
	}

	public CallableType getCallableType() {
		return callableType;
	}

	public SQLDeleteConfiguration callableType(CallableType callableType) {
		this.callableType = callableType;
		return this;
	}

	public String getSuccessParameter() {
		return successParameter;
	}

	public SQLDeleteConfiguration successParameter(String successParameter) {
		this.successParameter = successParameter;
		return this;
	}

	public String getSuccessValue() {
		return successValue;
	}

	public SQLDeleteConfiguration successValue(String successValue) {
		this.successValue = successValue;
		return this;
	}
}
