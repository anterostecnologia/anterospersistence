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

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.session.lock.LockMode;

public class DescriptionNamedQuery {

	private String name;
	private String query;
	private CallableType callableType;
	private LockMode lockModeType;
	private Class<?> resultClass;

	public DescriptionNamedQuery() {
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public LockMode getLockModeType() {
		return lockModeType;
	}

	public void setLockModeType(LockMode lockModeType) {
		this.lockModeType = lockModeType;
	}

	public CallableType getCallableType() {
		return callableType;
	}

	public void setCallableType(CallableType callableType) {
		this.callableType = callableType;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public void setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
	}

}
