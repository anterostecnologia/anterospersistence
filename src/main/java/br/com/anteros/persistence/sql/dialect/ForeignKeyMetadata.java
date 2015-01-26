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
package br.com.anteros.persistence.sql.dialect;

import java.util.ArrayList;
import java.util.List;

public class ForeignKeyMetadata {

	public String fkName;
	public List<String> columns = new ArrayList<String>();

	public ForeignKeyMetadata(String name) {
		this.fkName = name;
	}

	public void addColumn(String name) {
		for (int i = 0; i < columns.size(); i++) {
			if (columns.get(i).equalsIgnoreCase(name))
				return;
		}
		columns.add(name);
	}

	public boolean containsAllColumns(String[] cls) {
		if (columns.size() != cls.length)
			return false;
		for (String c : cls) {
			if (!columns.contains(c.toLowerCase())) {
				if (!columns.contains(c.toUpperCase())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForeignKeyMetadata other = (ForeignKeyMetadata) obj;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		return true;
	}


}
