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

import br.com.anteros.persistence.handler.ResultClassDefinition;
import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;

/**
 * 
 * @author edson
 *
 */
@SuppressWarnings("rawtypes")
public interface SQLQuery {

	public static boolean READ_ONLY = true;

	public SQLQuery identifier(Identifier<?> identifier);

	public SQLSession getSession();

	public SQLQuery sql(String sql);

	public String getSql();

	public SQLQuery showSql(boolean showSql);

	public SQLQuery formatSql(boolean formatSql);

	public SQLQuery timeOut(int seconds);

	public SQLQuery resultSetHandler(ResultSetHandler handler);

	public SQLQuery resultSetTransformer(ResultSetTransformer resultTransformer);

	public SQLQuery namedQuery(String name);

	public SQLQuery setReadOnly(boolean readOnlyObjects);

	public SQLQuery clear();

	public long count() throws Exception;

	public SQLQuery setParameters(Object parameters) throws Exception;

	public SQLQuery setParameters(Object[] parameters) throws Exception;

	public SQLQuery setParameters(NamedParameter[] parameters) throws Exception;

	public SQLQuery setParameters(Map<String, Object> parameters) throws Exception;

	public SQLQuery setInteger(int parameterIndex, int value) throws Exception;

	public SQLQuery setString(int parameterIndex, String value) throws Exception;

	public SQLQuery setLong(int parameterIndex, long value) throws Exception;

	public SQLQuery setNull(int parameterIndex) throws Exception;

	public SQLQuery setDate(int parameterIndex, Date value) throws Exception;

	public SQLQuery setDateTime(int parameterIndex, Date value) throws Exception;

	public SQLQuery setObject(int parameterIndex, Object object) throws Exception;

	public SQLQuery setBlob(int parameterIndex, InputStream inputStream) throws Exception;

	public SQLQuery setBlob(int parameterIndex, byte[] bytes) throws Exception;

	public SQLQuery setClob(int parameterIndex, InputStream inputStream) throws Exception;

	public SQLQuery setClob(int parameterIndex, byte[] bytes) throws Exception;

	public SQLQuery setBoolean(int parameterIndex, boolean value) throws Exception;

	public SQLQuery setDouble(int parameterIndex, double value) throws Exception;

	public SQLQuery setFloat(int parameterIndex, float value) throws Exception;

	public SQLQuery setBigDecimal(int parameterIndex, BigDecimal value) throws Exception;

	public SQLQuery setInteger(String parameterName, int value) throws Exception;

	public SQLQuery setString(String parameterName, String value) throws Exception;

	public SQLQuery setLong(String parameterName, long value) throws Exception;

	public SQLQuery setNull(String parameterName) throws Exception;

	public SQLQuery setDate(String parameterName, Date value) throws Exception;

	public SQLQuery setDateTime(String parameterName, Date value) throws Exception;

	public SQLQuery setObject(String parameterName, Object object) throws Exception;

	public SQLQuery setBlob(String parameterName, InputStream inputStream) throws Exception;

	public SQLQuery setBlob(String parameterName, byte[] bytes) throws Exception;

	public SQLQuery setClob(String parameterName, InputStream inputStream) throws Exception;

	public SQLQuery setClob(String parameterName, byte[] bytes) throws Exception;

	public SQLQuery setBoolean(String parameterName, boolean value) throws Exception;

	public SQLQuery setDouble(String parameterName, double value) throws Exception;

	public SQLQuery setFloat(String parameterName, float value) throws Exception;

	public SQLQuery setBigDecimal(String parameterName, BigDecimal value) throws Exception;

	public List getResultList() throws Exception;

	public SQLSessionResult getResultListAndResultSet() throws Exception;

	public Object getSingleResult() throws Exception;

	public ScrollableResultSet getScrollableResultSet() throws Exception;

	public void refresh(Object entity) throws Exception;

	public ResultSet executeQuery() throws Exception;

	public Object loadData(EntityCache entityCacheTarget, Object owner, final DescriptionField descriptionFieldOwner, Map<String, Object> columnKeyTarget,
			Cache transactionCache) throws Exception;

	public SQLQuery setLockOptions(LockOptions lockOptions);

	public LockOptions getLockOptions();

	public SQLQuery setLockMode(String alias, LockMode lockMode);

	public SQLQuery allowDuplicateObjects(boolean allowDuplicateObjects);

	public SQLQuery nextAliasColumnName(int nextAlias);

	public SQLQuery setMaxResults(int max);

	public SQLQuery setFirstResult(int first);

	public SQLQuery procedureOrFunctionName(String procedureName);

	public Object getOutputParameterValue(int position);

	public Object getOutputParameterValue(String parameterName);

	public ProcedureResult execute() throws Exception;

	public SQLQuery namedStoredProcedureQuery(String name);

	public SQLQuery addEntityResult(Class<?> entity);

	public SQLQuery addColumnResult(String columnName, Class<?> type);

	public SQLQuery addColumnResult(int columnIndex, Class<?> type);

	public SQLQuery addResultClassDefinition(ResultClassDefinition... resultClassDefinition);

}
