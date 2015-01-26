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

import br.com.anteros.persistence.schema.definition.type.StoredParameterType;

public class OutputNamedParameter extends NamedParameter {

	private int dataTypeSql;
	private StoredParameterType type;

	public OutputNamedParameter(String name, StoredParameterType type) {
		super(name);
		this.type = type;
		if (type == StoredParameterType.IN)
			throw new RuntimeException("Tipo de parâmetro de saída inválido. " + type);
	}
	
	public OutputNamedParameter(String name, StoredParameterType type, int dataTypeSql) {
		this(name,type);
		this.dataTypeSql = dataTypeSql;
	}

	public int getDataTypeSql() {
		return dataTypeSql;
	}

	public void setDataTypeSql(int sqlType) {
		this.dataTypeSql = sqlType;
	}

	public StoredParameterType getType() {
		return type;
	}

	public void setType(StoredParameterType type) {
		this.type = type;
	}

}
