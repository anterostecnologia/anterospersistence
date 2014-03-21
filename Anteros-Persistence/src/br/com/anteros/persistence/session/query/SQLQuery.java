/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.session.query;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.type.LockModeType;

public interface SQLQuery<T> {

	public SQLQuery<T> resultClass(Class<?> resultClass);

	public SQLQuery<T> identifier(Identifier<?> identifier);

	public SQLSession getSession();

	public SQLQuery<T> sql(String sql);

	public SQLQuery<T> showSql(boolean showSql);

	public SQLQuery<T> formatSql(boolean formatSql);

	public SQLQuery<T> timeOut(int seconds);

	public SQLQuery<T> resultSetHandler(ResultSetHandler handler);

	public SQLQuery<T> callableType(CallableType type);

	public SQLQuery<T> procedureOrFunctionName(String procedureName);

	public SQLQuery<T> outputParametersName(String[] outputParametersName);

	public SQLQuery<T> namedQuery(String name);

	public SQLQuery<T> clear();

	public SQLQuery<T> setParameters(NamedParameter[] parameters) throws Exception;

	public SQLQuery<T> setParameters(Object[] parameters) throws Exception;

	public SQLQuery<T> setParameters(Map<String, Object> parameters) throws Exception;

	public SQLQuery<T> setInteger(int parameterIndex, int value) throws Exception;

	public SQLQuery<T> setString(int parameterIndex, String value) throws Exception;

	public SQLQuery<T> setLong(int parameterIndex, long value) throws Exception;

	public SQLQuery<T> setNull(int parameterIndex) throws Exception;

	public SQLQuery<T> setDate(int parameterIndex, Date value) throws Exception;

	public SQLQuery<T> setDateTime(int parameterIndex, Date value) throws Exception;

	public SQLQuery<T> setObject(int parameterIndex, Object object) throws Exception;

	public SQLQuery<T> setBlob(int parameterIndex, InputStream inputStream) throws Exception;

	public SQLQuery<T> setBlob(int parameterIndex, byte[] bytes) throws Exception;

	public SQLQuery<T> setClob(int parameterIndex, InputStream inputStream) throws Exception;

	public SQLQuery<T> setClob(int parameterIndex, byte[] bytes) throws Exception;

	public SQLQuery<T> setBoolean(int parameterIndex, boolean value) throws Exception;

	public SQLQuery<T> setDouble(int parameterIndex, double value) throws Exception;

	public SQLQuery<T> setFloat(int parameterIndex, float value) throws Exception;

	public SQLQuery<T> setBigDecimal(int parameterIndex, BigDecimal value) throws Exception;

	public SQLQuery<T> setInteger(String parameterName, int value) throws Exception;

	public SQLQuery<T> setString(String parameterName, String value) throws Exception;

	public SQLQuery<T> setLong(String parameterName, long value) throws Exception;

	public SQLQuery<T> setNull(String parameterName) throws Exception;

	public SQLQuery<T> setDate(String parameterName, Date value) throws Exception;

	public SQLQuery<T> setDateTime(String parameterName, Date value) throws Exception;

	public SQLQuery<T> setObject(String parameterName, Object object) throws Exception;

	public SQLQuery<T> setBlob(String parameterName, InputStream inputStream) throws Exception;

	public SQLQuery<T> setBlob(String parameterName, byte[] bytes) throws Exception;

	public SQLQuery<T> setClob(String parameterName, InputStream inputStream) throws Exception;

	public SQLQuery<T> setClob(String parameterName, byte[] bytes) throws Exception;

	public SQLQuery<T> setBoolean(String parameterName, boolean value) throws Exception;

	public SQLQuery<T> setDouble(String parameterName, double value) throws Exception;

	public SQLQuery<T> setFloat(String parameterName, float value) throws Exception;

	public SQLQuery<T> setBigDecimal(String parameterName, BigDecimal value) throws Exception;

	public List<T> selectList() throws Exception;

	public List<T> selectListNamedQuery() throws Exception;

	public List<T> selectListProcedure() throws Exception;

	public SQLSessionResult selectListAndResultSet() throws Exception;

	public T selectId() throws Exception;

	public T selectOne() throws Exception;

	public T selectOneNamedQuery() throws Exception;

	public T selectOneProcedure() throws Exception;

	public T select() throws Exception;

	public List<T> selectProcedure() throws Exception;

	public ResultSet executeQuery() throws Exception;

	public ProcedureResult executeProcedure() throws Exception;

	public Object loadData(EntityCache entityCacheTarget, Object owner, final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws Exception;
	
	public SQLQuery<T> setLockMode(LockModeType lockMode);
	
	public LockModeType getLockMode();

}
