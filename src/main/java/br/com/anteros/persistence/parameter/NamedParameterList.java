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

import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.parameter.type.EnumeratedFormatSQL;
import br.com.anteros.persistence.schema.definition.type.StoredParameterType;

public class NamedParameterList extends ArrayList<NamedParameter> {

	private static final long serialVersionUID = 1L;

	public NamedParameterList addParameter(String name, Object value) {
		this.add(new NamedParameter(name, value));
		return this;
	}

	public NamedParameterList addParameter(String name, Object value, TemporalType type) {
		this.add(new NamedParameter(name, value, type));
		return this;
	}

	public NamedParameterList addSubstitutedParameter(String name, Object value) {
		this.add(new SubstitutedParameter(name, value));
		return this;
	}

	public NamedParameterList addEnumeratedParameter(String name, EnumeratedFormatSQL format, Enum<?>... value) {
		this.add(new EnumeratedParameter(name, format, value));
		return this;
	}

	public NamedParameterList addEnumeratedParameter(String name, Enum<?>... value) {
		this.add(new EnumeratedParameter(name, value));
		return this;
	}

	public NamedParameterList addOutputParameter(String name) {
		this.add(new OutputNamedParameter(name, StoredParameterType.OUT));
		return this;
	}

	public NamedParameterList addInputOutputParameter(String name, Object value) {
		OutputNamedParameter parameter = new OutputNamedParameter(name, StoredParameterType.OUT);
		parameter.setValue(value);
		this.add(parameter);
		return this;
	}

	public NamedParameter[] values() {
		NamedParameter[] result = new NamedParameter[] {};
		result = (NamedParameter[]) this.toArray(result);
		return result;
	}

	public NamedParameter[] toArray() {
		NamedParameter[] result = new NamedParameter[] {};
		result = (NamedParameter[]) this.toArray(result);
		return result;
	}
}
