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
package br.com.anteros.persistence.session.query;

import br.com.anteros.persistence.metadata.EntityCache;

public class SQLQueryAnalyserAlias {

	private String alias;
	private EntityCache entity;
	private SQLQueryAnalyserOwner owner;
	private boolean usedOnSelect;

	public boolean isUsedOnSelect() {
		return usedOnSelect;
	}

	public void setUsedOnSelect(boolean usedOnSelect) {
		this.usedOnSelect = usedOnSelect;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return new StringBuilder(alias).append(" = ").append((owner == null ? "" : owner.toString())).toString();
	}

	public String getPath() {
		if (owner == null)
			return "";
		String path = owner.getPath();
		return path;
	}

	public String getAliasPath() {
		if (owner == null)
			return "";
		String result = owner.getAliasPath();
		if (!result.equals(""))
			result += ".";
		return result + alias;
	}

	public EntityCache getEntity() {
		return entity;
	}

	public void setEntity(EntityCache entity) {
		this.entity = entity;
	}

	public SQLQueryAnalyserOwner getOwner() {
		return owner;
	}

	public void setOwner(SQLQueryAnalyserOwner owner) {
		this.owner = owner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
		SQLQueryAnalyserAlias other = (SQLQueryAnalyserAlias) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		return true;
	}

}
