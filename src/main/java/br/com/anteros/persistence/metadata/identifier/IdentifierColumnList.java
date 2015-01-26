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
package br.com.anteros.persistence.metadata.identifier;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class IdentifierColumnList extends ArrayList<IdentifierColumn> {
	
	public IdentifierColumnList addColumn(String columnName, Object value) {
		this.add(new IdentifierColumn(columnName, value));
		return this;
	}

	public IdentifierColumn[] values() {
		IdentifierColumn[] result = new IdentifierColumn[] {};
		result = (IdentifierColumn[]) this.toArray(result);
		return result;
	}
}
