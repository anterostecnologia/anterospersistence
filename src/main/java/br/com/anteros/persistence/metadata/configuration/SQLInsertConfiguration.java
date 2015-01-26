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

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.metadata.annotation.SQLInsert;
import br.com.anteros.persistence.metadata.annotation.SQLInsertId;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;

public class SQLInsertConfiguration {

	private String sql;
	private boolean callable = false;
	private CallableType callableType = CallableType.PROCEDURE;
	private String successParameter = "";
	private String successValue = "";
	private SQLInsertIdConfiguration[] parameterId = {};

	public SQLInsertConfiguration(String sql, boolean callable, CallableType callableType, String successParameter,
			String successValue, SQLInsertIdConfiguration[] parameterId) {
		this.sql = sql;
		this.callable = callable;
		this.callableType = callableType;
		this.successParameter = successParameter;
		this.successValue = successValue;
		this.parameterId = parameterId;
	}

	public SQLInsertConfiguration(SQLInsert sqlInsert) {
		this.sql = sqlInsert.sql();
		this.callable = sqlInsert.callable();
		this.callableType = sqlInsert.callableType();
		this.successParameter = sqlInsert.successParameter();
		this.successValue = sqlInsert.successValue();
		if (sqlInsert.parameterId() != null) {
			List<SQLInsertIdConfiguration> ids = new ArrayList<SQLInsertIdConfiguration>();
			for (SQLInsertId id : sqlInsert.parameterId())
				ids.add(new SQLInsertIdConfiguration(id));
			this.parameterId = ids.toArray(new SQLInsertIdConfiguration[] {});
		}

	}

	public String getSql() {
		return sql;
	}

	public SQLInsertConfiguration sql(String sql) {
		this.sql = sql;
		return this;
	}

	public boolean isCallable() {
		return callable;
	}

	public SQLInsertConfiguration callable(boolean callable) {
		this.callable = callable;
		return this;
	}

	public CallableType getCallableType() {
		return callableType;
	}

	public SQLInsertConfiguration callableType(CallableType callableType) {
		this.callableType = callableType;
		return this;
	}

	public String getSuccessParameter() {
		return successParameter;
	}

	public SQLInsertConfiguration successParameter(String successParameter) {
		this.successParameter = successParameter;
		return this;
	}

	public String getSuccessValue() {
		return successValue;
	}

	public SQLInsertConfiguration successValue(String successValue) {
		this.successValue = successValue;
		return this;
	}

	public SQLInsertIdConfiguration[] getParameterId() {
		return parameterId;
	}

	public SQLInsertConfiguration parameterId(SQLInsertIdConfiguration[] parameterId) {
		this.parameterId = parameterId;
		return this;
	}

}
