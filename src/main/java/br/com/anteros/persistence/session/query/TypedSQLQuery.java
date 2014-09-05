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
import br.com.anteros.persistence.session.lock.type.LockModeType;

public interface TypedSQLQuery<X> extends SQLQuery {

	TypedSQLQuery<X> identifier(Identifier<?> identifier);

	TypedSQLQuery<X> sql(String sql);

	TypedSQLQuery<X> showSql(boolean showSql);

	TypedSQLQuery<X> formatSql(boolean formatSql);

	TypedSQLQuery<X> timeOut(int seconds);

	TypedSQLQuery<X> resultSetHandler(ResultSetHandler handler);

	TypedSQLQuery<X> namedQuery(String name);

	TypedSQLQuery<X> clear();

	TypedSQLQuery<X> setParameters(Object parameters) throws Exception;

	TypedSQLQuery<X> setParameters(Object[] parameters) throws Exception;

	TypedSQLQuery<X> setParameters(NamedParameter[] parameters) throws Exception;

	TypedSQLQuery<X> setParameters(Map<String, Object> parameters) throws Exception;

	TypedSQLQuery<X> setInteger(int parameterIndex, int value) throws Exception;

	TypedSQLQuery<X> setString(int parameterIndex, String value) throws Exception;

	TypedSQLQuery<X> setLong(int parameterIndex, long value) throws Exception;

	TypedSQLQuery<X> setNull(int parameterIndex) throws Exception;

	TypedSQLQuery<X> setDate(int parameterIndex, Date value) throws Exception;

	TypedSQLQuery<X> setDateTime(int parameterIndex, Date value) throws Exception;

	TypedSQLQuery<X> setObject(int parameterIndex, Object object) throws Exception;

	TypedSQLQuery<X> setBlob(int parameterIndex, InputStream inputStream) throws Exception;

	TypedSQLQuery<X> setBlob(int parameterIndex, byte[] bytes) throws Exception;

	TypedSQLQuery<X> setClob(int parameterIndex, InputStream inputStream) throws Exception;

	TypedSQLQuery<X> setClob(int parameterIndex, byte[] bytes) throws Exception;

	TypedSQLQuery<X> setBoolean(int parameterIndex, boolean value) throws Exception;

	TypedSQLQuery<X> setDouble(int parameterIndex, double value) throws Exception;

	TypedSQLQuery<X> setFloat(int parameterIndex, float value) throws Exception;

	TypedSQLQuery<X> setBigDecimal(int parameterIndex, BigDecimal value) throws Exception;

	TypedSQLQuery<X> setInteger(String parameterName, int value) throws Exception;

	TypedSQLQuery<X> setString(String parameterName, String value) throws Exception;

	TypedSQLQuery<X> setLong(String parameterName, long value) throws Exception;

	TypedSQLQuery<X> setNull(String parameterName) throws Exception;

	TypedSQLQuery<X> setDate(String parameterName, Date value) throws Exception;

	TypedSQLQuery<X> setDateTime(String parameterName, Date value) throws Exception;

	TypedSQLQuery<X> setObject(String parameterName, Object object) throws Exception;

	TypedSQLQuery<X> setBlob(String parameterName, InputStream inputStream) throws Exception;

	TypedSQLQuery<X> setBlob(String parameterName, byte[] bytes) throws Exception;

	TypedSQLQuery<X> setClob(String parameterName, InputStream inputStream) throws Exception;

	TypedSQLQuery<X> setClob(String parameterName, byte[] bytes) throws Exception;

	TypedSQLQuery<X> setBoolean(String parameterName, boolean value) throws Exception;

	TypedSQLQuery<X> setDouble(String parameterName, double value) throws Exception;

	TypedSQLQuery<X> setFloat(String parameterName, float value) throws Exception;

	TypedSQLQuery<X> setBigDecimal(String parameterName, BigDecimal value) throws Exception;

	List<X> getResultList() throws Exception;

	SQLSessionResult<X> getResultListAndResultSet() throws Exception;

	X getSingleResult() throws Exception;

	ResultSet executeQuery() throws Exception;

	TypedSQLQuery<X> setLockMode(LockModeType lockMode);

	LockModeType getLockMode();

	TypedSQLQuery<X> allowDuplicateObjects(boolean allowDuplicateObjects);

	TypedSQLQuery<X> setMaxResults(int maxResults);

	TypedSQLQuery<X> setFirstResult(int firstResult);

}
