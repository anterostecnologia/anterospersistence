package br.com.anteros.persistence.dsl.osql;

import br.com.anteros.core.utils.StringUtils;

public class SQLAnalyserColumn {

	private String aliasTableName;
	private String columnName;
	private String aliasColumnName;
	private boolean userAliasDefined = false;

	public SQLAnalyserColumn(String aliasTableName, String columnName, String aliasColumnName) {
		super();
		this.aliasTableName = aliasTableName;
		this.columnName = columnName;
		this.aliasColumnName = aliasColumnName;
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

	@Override
	public String toString() {
		return aliasTableName + "." + columnName
				+ ((columnName.equals(aliasColumnName) || StringUtils.isEmpty(aliasColumnName) ? "" : " AS " + aliasColumnName));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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

	public boolean isUserAliasDefined() {
		return userAliasDefined;
	}

	public void setUserAliasDefined(boolean userAliasDefined) {
		this.userAliasDefined = userAliasDefined;
	}
}
