package br.com.anteros.persistence.session.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionNamedQuery;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.session.query.StoredProcedureSQLQuery;

public class StoredProcedureSQLQueryImpl extends SQLQueryImpl implements StoredProcedureSQLQuery {

	protected CallableType callableType;
	protected String procedureName;
	protected String[] outputParametersName = new String[] {};
	protected ProcedureResult lastResult = null;

	public StoredProcedureSQLQueryImpl(SQLSession session) {
		super(session);
	}

	@SuppressWarnings("unchecked")
	public StoredProcedureSQLQueryImpl(SQLSession session, Class<?> resultClass) {
		super(session);
		this.setResultClass(resultClass);
	}

	@Override
	public Object getOutputParameterValue(int position) {
		if (lastResult == null)
			throw new SQLQueryException(
					"É necessário executar a stored procedure/function antes de obter o valor do parâmetro de saída. Stored Procedure "
							+ procedureName + " tipo: " + callableType);
		int i = 0;
		for (Object value : lastResult.getOutputParameters().values()) {
			if (i == position)
				return value;
			i++;
		}
		throw new SQLQueryException("Não encontrado parâmetro para a posição " + position + ". Stored Procedure "
				+ procedureName + " tipo: " + callableType);
	}

	@Override
	public Object getOutputParameterValue(String parameterName) {
		if (lastResult == null)
			throw new SQLQueryException(
					"É necessário executar a stored procedure/function antes de obter o valor do parâmetro de saída. Stored Procedure "
							+ procedureName + " tipo: " + callableType);
		return lastResult.getOutPutParameter(parameterName);
	}

	@Override
	public ProcedureResult execute() throws Exception {
		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo de objeto para execução é um PROCEDIMENTO ou uma FUNÇÃO.");

		session.flush();

		if (this.parameters.size() > 0)
			lastResult = session.getRunner().executeProcedure(session, session.getDialect(), callableType,
					procedureName, parameters.values().toArray(), outputParametersName, showSql, timeOut,
					session.clientId());
		else if (this.namedParameters.size() > 0)
			lastResult = session.getRunner().executeProcedure(session, session.getDialect(), callableType,
					procedureName, namedParameters.values().toArray(new NamedParameter[] {}), outputParametersName,
					showSql, timeOut, session.clientId());
		else
			lastResult = session.getRunner().executeProcedure(session, session.getDialect(), callableType,
					procedureName, new Object[] {}, outputParametersName, showSql, timeOut, session.clientId());
		return lastResult;
	}

	@Override
	public StoredProcedureSQLQuery namedStoredProcedureQuery(String name) {
		this.setNamedQuery(name);
		return this;
	}

	@Override
	public Object getSingleResult() throws Exception {
		List<Object> result = getResultList();
		if (result.size() > 0)
			return result.get(0);
		return null;
	}

	@Override
	public List<Object> getResultList() throws Exception {
		if (getNamedQuery() != null) {
			throw new UnsupportedOperationException("Stored procedure nomeada ainda não implementada.");
		}

		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");
		/*
		 * Se for uma stored procedure nomeada
		 */
		if (this.getNamedQuery() != null) {
			EntityCache cache = session.getEntityCacheManager().getEntityCache(getResultClass());
			DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(this.getNamedQuery());
			if (namedQuery == null)
				throw new SQLQueryException("Stored Procedure nomeada " + this.getNamedQuery() + " não encontrada.");
			this.sql(namedQuery.getQuery());
		}

		List<Object> result = Collections.emptyList();
		Object resultObject = getResultObjectByCustomHandler();
		if (resultObject != null) {
			if (resultObject instanceof Collection)
				result = new ArrayList<Object>((Collection<?>) resultObject);
			else {
				result = new ArrayList<Object>();
				result.add(resultObject);
			}
		}
		return result;
	}

	@Override
	protected Object getResultObjectByCustomHandler() throws Exception {
		if (getNamedQuery() != null) {
			throw new UnsupportedOperationException("Stored procedure nomeada ainda não implementada.");
		}

		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException(
					"Use apenas um formato de parâmetros. Parâmetros nomeados ou lista de parâmetros.");

		if (handler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo de objeto para execução é um PROCEDIMENTO ou uma FUNÇÃO.");

		session.flush();

		Object result = null;

		if (this.parameters.size() > 0)
			result = session.getRunner().queryProcedure(session, session.getDialect(), callableType, procedureName,
					handler, parameters.values().toArray(), outputParametersName, showSql, timeOut, session.clientId());
		else if (this.namedParameters.size() > 0)
			result = session.getRunner().queryProcedure(session, session.getDialect(), callableType, procedureName,
					handler, namedParameters.values().toArray(new NamedParameter[] {}), outputParametersName, showSql,
					timeOut, session.clientId());
		else
			result = session.getRunner().queryProcedure(session, session.getDialect(), callableType, procedureName,
					handler, new Object[] {}, outputParametersName, showSql, timeOut, session.clientId());
		return result;
	}

	public StoredProcedureSQLQuery callableType(CallableType type) {
		this.callableType = type;
		return this;
	}

	public StoredProcedureSQLQuery procedureOrFunctionName(String procedureName) {
		this.procedureName = procedureName;
		return this;
	}

	public StoredProcedureSQLQuery outputParametersName(String[] outputParametersName) {
		this.outputParametersName = outputParametersName;
		return this;
	}

}
