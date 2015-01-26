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
package br.com.anteros.persistence.session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProcedureResult {

	private ResultSet resultSet;
	private Map<String, Object> outputParameters = new LinkedHashMap<String, Object>();

	public ProcedureResult() {
	}

	public ProcedureResult(ResultSet resultSet, Map<String, Object> outputParameters) {
		this.resultSet = resultSet;
		this.outputParameters = outputParameters;
	}

	public ResultSet getResultSet() throws SQLException {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public Map<String, Object> getOutputParameters() {
		return outputParameters;
	}

	public void setOutputParameters(Map<String, Object> outputParameters) {
		this.outputParameters = outputParameters;
	}

	public Object getOutPutParameter(String name) {
		return outputParameters.get(name);
	}

	public Object getFunctionResult() {
		if (outputParameters.size() != 0)
			return outputParameters.values().iterator().next();
		return null;
	}

	public void close() throws SQLException {
		if (resultSet != null) {
			if (!resultSet.isClosed())
				resultSet.close();
			if (!resultSet.getStatement().isClosed())
				resultSet.getStatement().close();
		}
	}

}
