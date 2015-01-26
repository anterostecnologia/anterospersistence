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

import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;

public class IdentifierPath {
	private DescriptionColumn column;
	private String path = "";

	public IdentifierPath(DescriptionColumn column, String path) {
		this.column = column;
		this.path = path;
	}

	public DescriptionColumn getColumn() {
		return column;
	}

	public void setColumn(DescriptionColumn column) {
		this.column = column;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public IdentifierPath concat(String part) {
		if (!"".equals(path)) 
			path += ".";
		path += part;
		return this;
	}
}
