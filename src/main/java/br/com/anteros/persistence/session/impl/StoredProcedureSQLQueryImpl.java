/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.ArrayListHandler;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionNamedQuery;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.session.query.TypedSQLQuery;

public class StoredProcedureSQLQueryImpl<T> extends SQLQueryImpl<T> {

	protected CallableType callableType;
	protected String procedureName;
	protected ProcedureResult lastResult = null;

	public StoredProcedureSQLQueryImpl(SQLSession session, CallableType callableType) {
		super(session);
		this.callableType = callableType;
	}

	@SuppressWarnings("unchecked")
	public StoredProcedureSQLQueryImpl(SQLSession session, Class<?> resultClass, CallableType callableType) {
		super(session);
		this.setResultClass(resultClass);
		this.callableType = callableType;
	}

	@Override
	public Object getOutputParameterValue(int position) {
		if (lastResult == null)
			throw new SQLQueryException("É necessário executar o procedimento/função antes de obter o valor do parâmetro de saída. Procedimento/função "
					+ procedureName + " tipo: " + callableType);
		int i = 0;
		for (Object value : lastResult.getOutputParameters().values()) {
			if (i == position)
				return value;
			i++;
		}
		throw new SQLQueryException("Não encontrado parâmetro para a posição " + position + ". Procedimento/função " + procedureName + " tipo: " + callableType);
	}

	@Override
	public Object getOutputParameterValue(String parameterName) {
		if (lastResult == null)
			throw new SQLQueryException("É necessário executar o procedimento/função antes de obter o valor do parâmetro de saída. Procedimento/função "
					+ procedureName + " tipo: " + callableType);
		return lastResult.getOutPutParameter(parameterName);
	}

	@Override
	public ProcedureResult execute() throws Exception {
		if (((this.parameters.size() > 0) && (this.namedParameters.size() > 0)) || (((this.parameters.size() > 0) && (this.namedParameters.size() == 0))))
			throw new SQLQueryException("Use apenas parâmetros nomeados para execução de procedimento ou função.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo de objeto para execução é um PROCEDIMENTO ou uma FUNÇÃO.");

		session.flush();

		if (this.namedParameters.size() > 0) {
			Collection<NamedParameter> values = namedParameters.values();
			lastResult = session.getRunner().executeProcedure(session, session.getDialect(), callableType, procedureName,
					values.toArray(new NamedParameter[] {}), showSql, timeOut, session.clientId());
		} else
			lastResult = session.getRunner().executeProcedure(session, session.getDialect(), callableType, procedureName, new NamedParameter[] {}, showSql,
					timeOut, session.clientId());
		return lastResult;
	}

	@Override
	public TypedSQLQuery<T> namedStoredProcedureQuery(String name) {
		this.setNamedQuery(name);
		return this;
	}

	@Override
	public T getSingleResult() throws Exception {
		if (handler == null) {
			ProcedureResult procedureResult = execute();
			if (callableType == CallableType.FUNCTION) {
				procedureResult.close();
				return (T) procedureResult.getFunctionResult();
			} else {
				if (NamedParameter.countOutputParameters(namedParameters.values()) == 1) {
					Object result = procedureResult.getOutputParameters().values().iterator().next();
					procedureResult.close();
					return (T) result;
				} else
					return (T) procedureResult;
			}
		} else {
			List<T> result = getResultList();
			if (result.size() > 0)
				return result.get(0);
		}
		return null;
	}

	@Override
	public List<T> getResultList() throws Exception {
		if (getNamedQuery() != null) {
			throw new UnsupportedOperationException("Procedimento nomeado ainda não implementado.");
		}

		if (((this.parameters.size() > 0) && (this.namedParameters.size() > 0)) || (((this.parameters.size() > 0) && (this.namedParameters.size() == 0))))
			throw new SQLQueryException("Use apenas parâmetros nomeados para execução de procedimento ou função.");
		/*
		 * Se for uma stored procedure nomeada
		 */
		if (this.getNamedQuery() != null) {
			EntityCache cache = session.getEntityCacheManager().getEntityCache(getResultClass());
			DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(this.getNamedQuery());
			if (namedQuery == null)
				throw new SQLQueryException("Procedimento nomeado " + this.getNamedQuery() + " não encontrado.");
			this.sql(namedQuery.getQuery());
		}

		List<T> result = Collections.emptyList();
		T resultObject = getResultObjectByCustomHandler((handler != null ? handler : new ArrayListHandler()));
		if (resultObject != null) {
			if (resultObject instanceof Collection)
				result = new ArrayList<T>((Collection<T>) resultObject);
			else {
				result = new ArrayList<T>();
				result.add(resultObject);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T getResultObjectByCustomHandler(ResultSetHandler customHandler) throws Exception {
		if (getNamedQuery() != null) {
			throw new UnsupportedOperationException("Procedimento/função nomeado ainda não implementado.");
		}

		if ((this.parameters.size() > 0) && (this.namedParameters.size() > 0))
			throw new SQLQueryException("Use apenas parâmetros nomeados para execução de procedimento ou função.");

		if (customHandler == null)
			throw new SQLQueryException("Informe o ResultSetHandler para executar a consulta.");

		if (StringUtils.isEmpty(procedureName))
			throw new SQLQueryException("Informe o nome do procedimento ou função para executar.");

		if (callableType == null)
			throw new SQLQueryException("Informe se o tipo de objeto para execução é um PROCEDIMENTO ou uma FUNÇÃO.");

		session.flush();

		T result = null;

		if (this.namedParameters.size() > 0) {
			Collection<NamedParameter> values = namedParameters.values();
			result = (T) session.getRunner().queryProcedure(session, session.getDialect(), callableType, procedureName, customHandler,
					values.toArray(new NamedParameter[] {}), showSql, timeOut, session.clientId());
		} else
			result = (T) session.getRunner().queryProcedure(session, session.getDialect(), callableType, procedureName, customHandler, new NamedParameter[] {},
					showSql, timeOut, session.clientId());
		return result;
	}

	public TypedSQLQuery<T> callableType(CallableType type) {
		this.callableType = type;
		return this;
	}

	public TypedSQLQuery<T> procedureOrFunctionName(String procedureName) {
		this.procedureName = procedureName;
		return this;
	}

	@Override
	public TypedSQLQuery<T> setParameters(Object[] parameters) throws Exception {
		if ((parameters != null) && (parameters.length > 0)) {
			if (parameters[0] instanceof NamedParameter) {
				List<NamedParameter> params = new ArrayList<NamedParameter>();
				for (Object p : parameters) {
					params.add((NamedParameter) p);
				}
				this.setParameters(params.toArray(new NamedParameter[] {}));
				return this;
			}
		}
		throw new SQLQueryException("Formato para setParameters inválido. Use NamedParameter[] ou Map para execução de Procedimento/função ");
	}

	@Override
	public TypedSQLQuery<T> setParameters(Map parameters) throws Exception {
		int paramCount = 0;
		namedParameters.clear();
		for (Object namedParameter : parameters.keySet()) {
			paramCount++;
			namedParameters.put(paramCount, new NamedParameter(namedParameter + ""));
		}
		return super.setParameters(parameters);
	}

	@Override
	public TypedSQLQuery<T> setParameters(NamedParameter[] parameters) throws Exception {
		int paramCount = 0;
		namedParameters.clear();
		for (NamedParameter namedParameter : parameters) {
			paramCount++;
			namedParameters.put(paramCount, namedParameter);
		}
		return super.setParameters(parameters);
	}

}
