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

import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;

public class FetchConfiguration {

	private String statement ="";
	private FetchType type = FetchType.EAGER;
	private FetchMode mode;
	private String mappedBy = "";
	private Class<?> targetEntity = void.class;
	
	public FetchConfiguration() {
	}

	public FetchConfiguration(String statement, FetchType type, FetchMode mode, String mappedBy, Class<?> targetEntity) {
		this.statement = statement;
		this.type = type;
		this.mode = mode;
		this.mappedBy = mappedBy;
		this.targetEntity = targetEntity;
	}

	public FetchConfiguration(Fetch fetch) {
		this.statement = fetch.statement();
		this.type = fetch.type();
		this.mode = fetch.mode();
		this.mappedBy = fetch.mappedBy();
		this.targetEntity = fetch.targetEntity();
	}
	
	public FetchConfiguration(FetchType fetchType) {
		this.type = fetchType;
	}

	public String getStatement() {
		return statement;
	}

	public FetchConfiguration statement(String statement) {
		this.statement = statement;
		return this;
	}

	public FetchType getType() {
		return type;
	}

	public FetchConfiguration type(FetchType type) {
		this.type = type;
		return this;
	}

	public FetchMode getMode() {
		return mode;
	}

	public FetchConfiguration mode(FetchMode mode) {
		this.mode = mode;
		return this;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public FetchConfiguration mappedBy(String mappedBy) {
		this.mappedBy = mappedBy;
		return this;
	}

	public Class<?> getTargetEntity() {
		return targetEntity;
	}

	public FetchConfiguration targetEntity(Class<?> targetEntity) {
		this.targetEntity = targetEntity;
		return this;
	}

}
