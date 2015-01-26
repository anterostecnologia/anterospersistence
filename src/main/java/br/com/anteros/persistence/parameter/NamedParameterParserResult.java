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
package br.com.anteros.persistence.parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.util.AnterosPersistenceTranslate;

public class NamedParameterParserResult {

	private String parsedSql;
	private Map<String, Object> parsedParams = new LinkedHashMap<String, Object>();

	public String getParsedSql() {
		return parsedSql;
	}

	public void setParsedSql(String parsedSql) {
		this.parsedSql = parsedSql;
	}

	public Map<String, Object> getParsedParams() { 
		return parsedParams;
	}

	public void setParsedParams(Map<String, Object> parsedParams) {
		this.parsedParams = parsedParams;
	}

	@Override
	public String toString() {
		return AnterosPersistenceTranslate.getMessage(NamedParameterParserResult.class, "toString", parsedSql, parsedParams);
	}

	public List<NamedParameter> getNamedParameters() {
		List<NamedParameter> result = new ArrayList<NamedParameter>();
		for (String param : parsedParams.keySet()) {
			result.add(new NamedParameter(param));
		}
		return result;
	}

}
