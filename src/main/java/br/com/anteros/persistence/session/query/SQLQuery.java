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
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionResult;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.type.LockModeType;

@SuppressWarnings("rawtypes")
public interface SQLQuery {

	SQLQuery identifier(Identifier<?> identifier);

	SQLSession getSession();

	SQLQuery sql(String sql);

	SQLQuery showSql(boolean showSql);

	SQLQuery formatSql(boolean formatSql);

	SQLQuery timeOut(int seconds);

	SQLQuery resultSetHandler(ResultSetHandler handler);

	SQLQuery namedQuery(String name);
	
	SQLQuery setReadOnly(boolean readOnlyObjects);

	SQLQuery clear();

	SQLQuery setParameters(Object parameters) throws Exception;

	SQLQuery setParameters(Object[] parameters) throws Exception;

	SQLQuery setParameters(NamedParameter[] parameters) throws Exception;

	SQLQuery setParameters(Map<String, Object> parameters) throws Exception;

	SQLQuery setInteger(int parameterIndex, int value) throws Exception;

	SQLQuery setString(int parameterIndex, String value) throws Exception;

	SQLQuery setLong(int parameterIndex, long value) throws Exception;

	SQLQuery setNull(int parameterIndex) throws Exception;

	SQLQuery setDate(int parameterIndex, Date value) throws Exception;

	SQLQuery setDateTime(int parameterIndex, Date value) throws Exception;

	SQLQuery setObject(int parameterIndex, Object object) throws Exception;

	SQLQuery setBlob(int parameterIndex, InputStream inputStream) throws Exception;

	SQLQuery setBlob(int parameterIndex, byte[] bytes) throws Exception;

	SQLQuery setClob(int parameterIndex, InputStream inputStream) throws Exception;

	SQLQuery setClob(int parameterIndex, byte[] bytes) throws Exception;

	SQLQuery setBoolean(int parameterIndex, boolean value) throws Exception;

	SQLQuery setDouble(int parameterIndex, double value) throws Exception;

	SQLQuery setFloat(int parameterIndex, float value) throws Exception;

	SQLQuery setBigDecimal(int parameterIndex, BigDecimal value) throws Exception;

	SQLQuery setInteger(String parameterName, int value) throws Exception;

	SQLQuery setString(String parameterName, String value) throws Exception;

	SQLQuery setLong(String parameterName, long value) throws Exception;

	SQLQuery setNull(String parameterName) throws Exception;

	SQLQuery setDate(String parameterName, Date value) throws Exception;

	SQLQuery setDateTime(String parameterName, Date value) throws Exception;

	SQLQuery setObject(String parameterName, Object object) throws Exception;

	SQLQuery setBlob(String parameterName, InputStream inputStream) throws Exception;

	SQLQuery setBlob(String parameterName, byte[] bytes) throws Exception;

	SQLQuery setClob(String parameterName, InputStream inputStream) throws Exception;

	SQLQuery setClob(String parameterName, byte[] bytes) throws Exception;

	SQLQuery setBoolean(String parameterName, boolean value) throws Exception;

	SQLQuery setDouble(String parameterName, double value) throws Exception;

	SQLQuery setFloat(String parameterName, float value) throws Exception;

	SQLQuery setBigDecimal(String parameterName, BigDecimal value) throws Exception;

	List getResultList() throws Exception;

	SQLSessionResult getResultListAndResultSet() throws Exception;

	Object getSingleResult() throws Exception;

	void refresh(Object entity) throws Exception;

	ResultSet executeQuery() throws Exception;

	Object loadData(EntityCache entityCacheTarget, Object owner, final DescriptionField descriptionFieldOwner,
			Map<String, Object> columnKeyTarget, Cache transactionCache) throws Exception;

	SQLQuery setLockMode(LockModeType lockMode);

	LockModeType getLockMode();

	SQLQuery allowDuplicateObjects(boolean allowDuplicateObjects);

	SQLQuery setMaxResults(int max);

	SQLQuery setFirstResult(int first);

}
