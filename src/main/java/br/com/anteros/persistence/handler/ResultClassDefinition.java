package br.com.anteros.persistence.handler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ResultClassDefinition {

	private Class<?> resultClass;

	private Set<String> columns;

	public ResultClassDefinition(Class<?> resultClass, List<String> columns) {
		super();
		this.resultClass = resultClass;
		this.columns = ImmutableSet.copyOf(columns);
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public void setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
	}

	public Set<String> getColumns() {
		return columns;
	}
	
	@Override
	public String toString() {
		return resultClass.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resultClass == null) ? 0 : resultClass.hashCode());
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
		ResultClassDefinition other = (ResultClassDefinition) obj;
		if (resultClass == null) {
			if (other.resultClass != null)
				return false;
		} else if (!resultClass.equals(other.resultClass))
			return false;
		return true;
	}

}
