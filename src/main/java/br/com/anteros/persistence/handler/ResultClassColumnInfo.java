package br.com.anteros.persistence.handler;

import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

public class ResultClassColumnInfo {

	private String aliasTableName;
	private String columnName;
	private String aliasColumnName;
	private boolean userAliasDefined = false;
	private DescriptionField descriptionField;
	private int columnIndex;

	public ResultClassColumnInfo(String aliasTableName, String columnName, String aliasColumnName, DescriptionField descriptionField, int columnIndex) {
		super();
		this.aliasTableName = aliasTableName;
		this.columnName = columnName;
		this.aliasColumnName = aliasColumnName;
		this.descriptionField = descriptionField;
		this.columnIndex = columnIndex;
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
		ResultClassColumnInfo other = (ResultClassColumnInfo) obj;
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
	


	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	@Override
	public String toString() {
		return "ResultClassColumnInfo [aliasTableName=" + aliasTableName + ", columnName=" + columnName + ", aliasColumnName=" + aliasColumnName
				+ ", userAliasDefined=" + userAliasDefined + ", descriptionField=" + descriptionField + ", columnIndex=" + columnIndex + "]";
	}

}
