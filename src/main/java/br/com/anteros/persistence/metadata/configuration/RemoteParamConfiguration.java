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

public class RemoteParamConfiguration {
	private String paramName;
	private String paramValue = "";
	private int paramOrder;
	
	public RemoteParamConfiguration() {
	}
	
	public RemoteParamConfiguration(String paramName, String paramValue, int paramOrder) {
		this.paramName = paramName;
		this.paramValue = paramValue;
		this.paramOrder = paramOrder;
	}
	
	public RemoteParamConfiguration(String paramName, int paramOrder) {
		this.paramName = paramName;
		this.paramOrder = paramOrder;
	}
	
	public String getParamName() {
		return paramName;
	}
	public RemoteParamConfiguration paramName(String paramName) {
		this.paramName = paramName;
		return this;
	}
	public String getParamValue() {
		return paramValue;
	}
	public RemoteParamConfiguration paramValue(String paramValue) {
		this.paramValue = paramValue;
		return this;
	}
	public int getParamOrder() {
		return paramOrder;
	}
	public RemoteParamConfiguration paramOrder(int paramOrder) {
		this.paramOrder = paramOrder;
		return this;
	}

}
