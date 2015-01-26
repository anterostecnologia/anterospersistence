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

import br.com.anteros.persistence.metadata.annotation.NamedQuery;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.session.lock.LockMode;

public class NamedQueryConfiguration {

	private String name;
	private String query;
	private CallableType callableType;
	private LockMode lockMode;
	private int lockTimeout;
	private Class<?> resultClass;

	public NamedQueryConfiguration() {

	}

	public NamedQueryConfiguration(NamedQuery namedQuery) {
		this.name = namedQuery.name();
		this.query = namedQuery.query();
		this.lockMode(namedQuery.lockMode());
		this.lockTimeout = namedQuery.lockTimeout();
	}

	public NamedQueryConfiguration(String name, String query, Class<?> resultClass) {
		this.name = name;
		this.query = query;
		this.callableType = CallableType.NONE;
		this.resultClass = resultClass;
	}
	
	public NamedQueryConfiguration(String name, String query, Class<?> resultClass, CallableType callableType) {
		this.name = name;
		this.query = query;
		this.callableType = callableType;
		this.resultClass = resultClass;
	}

	public String getName() {
		return name;
	}

	public NamedQueryConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public String getQuery() {
		return query;
	}

	public NamedQueryConfiguration query(String query) {
		this.query = query;
		return this;
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	public NamedQueryConfiguration lockMode(LockMode lockMode) {
		this.lockMode = lockMode;
		return this;
	}

	public CallableType getCallableType() {
		return callableType;
	}

	public NamedQueryConfiguration callableType(CallableType callableType) {
		this.callableType = callableType;
		return this;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public NamedQueryConfiguration resultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
		return this;
	}

	public int getLockTimeout() {
		return lockTimeout;
	}

	public NamedQueryConfiguration lockTimeout(int lockTimeout) {
		this.lockTimeout = lockTimeout;
		return this;
	}

}
