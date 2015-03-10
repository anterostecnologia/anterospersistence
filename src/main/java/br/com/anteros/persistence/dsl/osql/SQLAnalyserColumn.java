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
package br.com.anteros.persistence.dsl.osql;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

public class SQLAnalyserColumn {

	private String aliasTableName;
	private String columnName;
	private String aliasColumnName;
	private boolean userAliasDefined = false;
	private DescriptionField descriptionField;

	public SQLAnalyserColumn(String aliasTableName, String columnName, String aliasColumnName, DescriptionField descriptionField) {
		super();
		this.aliasTableName = aliasTableName;
		this.columnName = columnName;
		this.aliasColumnName = aliasColumnName;
		this.descriptionField = descriptionField;
	}

	public String getAliasTableName() {
		return aliasTableName;
	}

	public void setAliasTableName(String aliasTableName) {
		this.aliasTableName = aliasTableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getAliasColumnName() {
		return aliasColumnName;
	}

	public void setAliasColumnName(String aliasColumnName) {
		this.aliasColumnName = aliasColumnName;
	}

	public boolean isUserAliasDefined() {
		return userAliasDefined;
	}

	public void setUserAliasDefined(boolean userAliasDefined) {
		this.userAliasDefined = userAliasDefined;
	}

	public DescriptionField getDescriptionField() {
		return descriptionField;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aliasColumnName == null) ? 0 : aliasColumnName.hashCode());
		result = prime * result + ((aliasTableName == null) ? 0 : aliasTableName.hashCode());
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
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
		SQLAnalyserColumn other = (SQLAnalyserColumn) obj;
		if (aliasColumnName == null) {
			if (other.aliasColumnName != null)
				return false;
		} else if (!aliasColumnName.equals(other.aliasColumnName))
			return false;
		if (aliasTableName == null) {
			if (other.aliasTableName != null)
				return false;
		} else if (!aliasTableName.equals(other.aliasTableName))
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return aliasTableName+"."+columnName+" AS "+aliasColumnName;
	}

}
