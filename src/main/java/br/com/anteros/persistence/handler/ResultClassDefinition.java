package br.com.anteros.persistence.handler;

import java.util.Set;

import br.com.anteros.persistence.dsl.osql.SQLAnalyserColumn;

import com.google.common.collect.ImmutableSet;

public class ResultClassDefinition {

	private Class<?> resultClass;

	private Set<ResultClassColumnInfo> columns;

	public ResultClassDefinition(Class<?> resultClass, Set<ResultClassColumnInfo> columns) {
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

	public Set<ResultClassColumnInfo> getColumns() {
		return columns;
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
	
	public ResultClassColumnInfo getSimpleColumn(){
		if (columns.size()==0)
			return null;
		return columns.iterator().next();
	}

	@Override
	public String toString() {
		return "ResultClassDefinition [resultClass=" + resultClass + ", columns=" + columns + "]";
	}

}
