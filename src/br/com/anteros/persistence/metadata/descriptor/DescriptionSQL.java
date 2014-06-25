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
package br.com.anteros.persistence.metadata.descriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.type.SQLStatementType;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;

public class DescriptionSQL {

	private SQLStatementType sqlType;
	private boolean callable;
	private String sql;
	private CallableType callableType;
	private String successParameter;
	private String successValue;
	/*
	 * Mapa PARAMETRO X COLUNA
	 */
	private Map<String, String> parametersId = new LinkedHashMap<String, String>();

	public DescriptionSQL() {

	}

	public DescriptionSQL(SQLStatementType sqlType, boolean callable, String sql, CallableType callableType,
			String successParameter, String successValue, Map<String, String> parametersId) {
		this.callable = callable;
		this.sql = sql;
		this.callableType = callableType;
		this.successParameter = successParameter;
		this.successValue = successValue;
		this.parametersId = parametersId;
		this.sqlType = sqlType;
	}

	public boolean isCallable() {
		return callable;
	}

	public void setCallable(boolean callable) {
		this.callable = callable;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public CallableType getCallableType() {
		return callableType;
	}

	public void setCallableType(CallableType callableType) {
		this.callableType = callableType;
	}

	public String getSuccessParameter() {
		return successParameter;
	}

	public void setSuccessParameter(String successParameter) {
		this.successParameter = successParameter;
	}

	public String getSuccessValue() {
		return successValue;
	}

	public void setSuccessValue(String successValue) {
		this.successValue = successValue;
	}

	public Map<String, String> getParametersId() {
		return parametersId;
	}

	public void setParametersId(Map<String, String> parametersId) {
		this.parametersId = parametersId;
	}

	public SQLStatementType getSqlType() {
		return sqlType;
	}

	public void setSqlType(SQLStatementType sqlType) {
		this.sqlType = sqlType;
	}

	public String[] getParametersName() {
		List<String> result = new ArrayList<String>();
		for (String parameter : NamedParameterStatement.parse(sql, null).getParsedParams().keySet()) {
			result.add(parameter);
		}
		return result.toArray(new String[] {});
	}

	public NamedParameter[] processParameters(List<NamedParameter> namedParameters) {
		List<NamedParameter> result = new ArrayList<NamedParameter>();
		String[] names = this.getParametersName();
		NamedParameter namedParam;
		for (String name : names) {
			namedParam = NamedParameter.getNamedParameterByName(namedParameters, name);
			if (namedParam != null) {
				result.add(namedParam);
			}
		}
		return result.toArray(new NamedParameter[] {});
	}

	public Object[] getInputParameters(List<NamedParameter> namedParameters) {
		List<Object> result = new ArrayList<Object>();
		if (callable) {
			String[] parsedParameters = getParametersName();
			NamedParameter namedParam;
			for (String parameter : parsedParameters) {
				if ((!parameter.equals(successParameter)) && (parametersId.get(parameter) == null)) {
					namedParam = NamedParameter.getNamedParameterByName(namedParameters, parameter);
					if (namedParam != null)
						result.add(namedParam.getValue());
				}
			}
		}
		return result.toArray(new Object[] {});
	}

	public String[] getOutputParameters(List<NamedParameter> namedParameters) {
		List<Object> result = new ArrayList<Object>();
		if (callable) {
			String[] parsedParameters = getParametersName();
			NamedParameter namedParam;
			for (String parameter : parsedParameters) {
				if ((parameter.equals(successParameter)) || (parametersId.get(parameter) != null)) {
					result.add(parameter);
				}
			}
		}
		return result.toArray(new String[] {});
	}
	
	public String getParameterIdByColumnName(String columnName){
		for (String s : parametersId.keySet()){
			if (columnName.equals(parametersId.get(s))){
				return s;
			}
		}
		return null;
	}

}
