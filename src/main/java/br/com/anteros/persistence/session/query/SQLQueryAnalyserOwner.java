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
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

public class SQLQueryAnalyserOwner {

	private SQLQueryAnalyserAlias owner;
	private EntityCache entityCacheOwner;
	private DescriptionField fieldOwner;

	public SQLQueryAnalyserOwner(SQLQueryAnalyserAlias owner, EntityCache entityCacheOwner, DescriptionField fieldOwner) {
		this.owner = owner;
		this.fieldOwner = fieldOwner;
		this.entityCacheOwner = entityCacheOwner;
	}

	public SQLQueryAnalyserAlias getOwner() {
		return owner;
	}

	public void setOwner(SQLQueryAnalyserAlias owner) {
		this.owner = owner;
	}

	public DescriptionField getFieldOwner() {
		return fieldOwner;
	}

	public void setFieldOwner(DescriptionField fieldOwner) {
		this.fieldOwner = fieldOwner;
	}

	@Override
	public String toString() {
		return entityCacheOwner.getEntityClass().getName() + "->" + fieldOwner.getName();
	}

	public EntityCache getEntityCacheOwner() {
		return entityCacheOwner;
	}

	public void setEntityCacheOwner(EntityCache entityCacheOwner) {
		this.entityCacheOwner = entityCacheOwner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityCacheOwner == null) ? 0 : entityCacheOwner.hashCode());
		result = prime * result + ((fieldOwner == null) ? 0 : fieldOwner.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
		SQLQueryAnalyserOwner other = (SQLQueryAnalyserOwner) obj;
		if (entityCacheOwner == null) {
			if (other.entityCacheOwner != null)
				return false;
		} else if (!entityCacheOwner.equals(other.entityCacheOwner))
			return false;
		if (fieldOwner == null) {
			if (other.fieldOwner != null)
				return false;
		} else if (!fieldOwner.equals(other.fieldOwner))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	public String getPath() {
		String result = owner.getPath();
		if (!result.equals(""))
			result += ".";
		result += fieldOwner.getName();
		
		return result;
	}
	
	
	public String getAliasPath(){
		if (owner ==null)
			return "";
		return owner.getAliasPath();
	}

}
