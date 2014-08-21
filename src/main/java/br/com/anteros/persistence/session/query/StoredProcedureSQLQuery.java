package br.com.anteros.persistence.session.query;

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.session.ProcedureResult;

public interface StoredProcedureSQLQuery extends SQLQuery {

	public StoredProcedureSQLQuery callableType(CallableType type);

	public StoredProcedureSQLQuery procedureOrFunctionName(String procedureName);

	public Object getOutputParameterValue(int position);

	public Object getOutputParameterValue(String parameterName);

	public ProcedureResult execute() throws Exception;
	
	public StoredProcedureSQLQuery outputParametersName(String[] outputParametersName);
	
	public StoredProcedureSQLQuery namedStoredProcedureQuery(String name);
	
}