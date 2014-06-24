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
