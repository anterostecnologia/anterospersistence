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

import br.com.anteros.persistence.metadata.annotation.SQLUpdate;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;

public class SQLUpdateConfiguration {
	private String sql;
	private boolean callable = false;
	private CallableType callableType = CallableType.PROCEDURE;
	private String successParameter = "";
	private String successValue = "";

	public SQLUpdateConfiguration(String sql, boolean callable, CallableType callableType, String successParameter,
			String successValue) {
		this.sql = sql;
		this.callable = callable;
		this.callableType = callableType;
		this.successParameter = successParameter;
		this.successValue = successValue;
	}

	public SQLUpdateConfiguration(SQLUpdate sqlUpdate) {
		this.sql = sqlUpdate.sql();
		this.callable = sqlUpdate.callable();
		this.callableType = sqlUpdate.callableType();
		this.successParameter = sqlUpdate.successParameter();
		this.successValue = sqlUpdate.successValue();
	}

	public String getSql() {
		return sql;
	}

	public SQLUpdateConfiguration sql(String sql) {
		this.sql = sql;
		return this;
	}

	public boolean isCallable() {
		return callable;
	}

	public SQLUpdateConfiguration callable(boolean callable) {
		this.callable = callable;
		return this;
	}

	public CallableType getCallableType() {
		return callableType;
	}

	public SQLUpdateConfiguration callableType(CallableType callableType) {
		this.callableType = callableType;
		return this;
	}

	public String getSuccessParameter() {
		return successParameter;
	}

	public SQLUpdateConfiguration successParameter(String successParameter) {
		this.successParameter = successParameter;
		return this;
	}

	public String getSuccessValue() {
		return successValue;
	}

	public SQLUpdateConfiguration successValue(String successValue) {
		this.successValue = successValue;
		return this;
	}
}
