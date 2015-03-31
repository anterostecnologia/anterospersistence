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
package br.com.anteros.persistence.session.query;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;

public interface TypedSQLQuery<X> extends SQLQuery {

	public TypedSQLQuery<X> identifier(Identifier<?> identifier);

	public TypedSQLQuery<X> sql(String sql);

	public TypedSQLQuery<X> showSql(boolean showSql);

	public TypedSQLQuery<X> formatSql(boolean formatSql);

	public TypedSQLQuery<X> timeOut(int seconds);

	public TypedSQLQuery<X> resultSetHandler(ResultSetHandler handler);

	public TypedSQLQuery<X> namedQuery(String name);

	public TypedSQLQuery<X> clear();

	public TypedSQLQuery<X> setReadOnly(boolean readOnlyObjects);

	public TypedSQLQuery<X> setParameters(Object parameters) throws Exception;

	public TypedSQLQuery<X> setParameters(Object[] parameters) throws Exception;

	public TypedSQLQuery<X> setParameters(NamedParameter[] parameters) throws Exception;

	public TypedSQLQuery<X> setParameters(Map<String, Object> parameters) throws Exception;

	public TypedSQLQuery<X> setInteger(int parameterIndex, int value) throws Exception;

	public TypedSQLQuery<X> setString(int parameterIndex, String value) throws Exception;

	public TypedSQLQuery<X> setLong(int parameterIndex, long value) throws Exception;

	public TypedSQLQuery<X> setNull(int parameterIndex) throws Exception;

	public TypedSQLQuery<X> setDate(int parameterIndex, Date value) throws Exception;

	public TypedSQLQuery<X> setDateTime(int parameterIndex, Date value) throws Exception;

	public TypedSQLQuery<X> setObject(int parameterIndex, Object object) throws Exception;

	public TypedSQLQuery<X> setBlob(int parameterIndex, InputStream inputStream) throws Exception;

	public TypedSQLQuery<X> setBlob(int parameterIndex, byte[] bytes) throws Exception;

	public TypedSQLQuery<X> setClob(int parameterIndex, InputStream inputStream) throws Exception;

	public TypedSQLQuery<X> setClob(int parameterIndex, byte[] bytes) throws Exception;

	public TypedSQLQuery<X> setBoolean(int parameterIndex, boolean value) throws Exception;

	public TypedSQLQuery<X> setDouble(int parameterIndex, double value) throws Exception;

	public TypedSQLQuery<X> setFloat(int parameterIndex, float value) throws Exception;

	public TypedSQLQuery<X> setBigDecimal(int parameterIndex, BigDecimal value) throws Exception;

	public TypedSQLQuery<X> setInteger(String parameterName, int value) throws Exception;

	public TypedSQLQuery<X> setString(String parameterName, String value) throws Exception;

	public TypedSQLQuery<X> setLong(String parameterName, long value) throws Exception;

	public TypedSQLQuery<X> setNull(String parameterName) throws Exception;

	public TypedSQLQuery<X> setDate(String parameterName, Date value) throws Exception;

	public TypedSQLQuery<X> setDateTime(String parameterName, Date value) throws Exception;

	public TypedSQLQuery<X> setObject(String parameterName, Object object) throws Exception;

	public TypedSQLQuery<X> setBlob(String parameterName, InputStream inputStream) throws Exception;

	public TypedSQLQuery<X> setBlob(String parameterName, byte[] bytes) throws Exception;

	public TypedSQLQuery<X> setClob(String parameterName, InputStream inputStream) throws Exception;

	public TypedSQLQuery<X> setClob(String parameterName, byte[] bytes) throws Exception;

	public TypedSQLQuery<X> setBoolean(String parameterName, boolean value) throws Exception;

	public TypedSQLQuery<X> setDouble(String parameterName, double value) throws Exception;

	public TypedSQLQuery<X> setFloat(String parameterName, float value) throws Exception;

	public TypedSQLQuery<X> setBigDecimal(String parameterName, BigDecimal value) throws Exception;

	public List<X> getResultList() throws Exception;

	public SQLSessionResult<X> getResultListAndResultSet() throws Exception;

	public X getSingleResult() throws Exception;

	public ResultSet executeQuery() throws Exception;

	public TypedSQLQuery<X> setLockOptions(LockOptions lockOptions);

	public LockOptions getLockOptions();

	public SQLQuery setLockMode(String alias, LockMode lockMode);

	public TypedSQLQuery<X> allowDuplicateObjects(boolean allowDuplicateObjects);

	public TypedSQLQuery<X> setMaxResults(int maxResults);

	public TypedSQLQuery<X> setFirstResult(int firstResult);

	public TypedSQLQuery<X> procedureOrFunctionName(String procedureName);

	public TypedSQLQuery<X> namedStoredProcedureQuery(String name);

}
