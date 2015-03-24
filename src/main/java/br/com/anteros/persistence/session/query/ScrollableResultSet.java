package br.com.anteros.persistence.session.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public interface ScrollableResultSet {

	public boolean next() throws SQLQueryException;

	public boolean previous() throws SQLQueryException;

	public boolean scroll(int i) throws SQLQueryException;

	public boolean last() throws SQLQueryException;

	public boolean first() throws SQLQueryException;

	public void beforeFirst() throws SQLQueryException;

	public void afterLast() throws SQLQueryException;

	public boolean isFirst() throws SQLQueryException;

	public boolean isLast() throws SQLQueryException;

	public void close() throws SQLQueryException;

	public Object[] get() throws SQLQueryException;

	public Object get(int columnIndex) throws SQLQueryException;

	public Integer getInteger(int columnIndex) throws SQLQueryException;

	public Long getLong(int columnIndex) throws SQLQueryException;

	public Float getFloat(int columnIndex) throws SQLQueryException;

	public Boolean getBoolean(int columnIndex) throws SQLQueryException;

	public Double getDouble(int columnIndex) throws SQLQueryException;

	public Short getShort(int columnIndex) throws SQLQueryException;

	public Byte getByte(int columnIndex) throws SQLQueryException;

	public Character getCharacter(int columnIndex) throws SQLQueryException;

	public byte[] getBinary(int columnIndex) throws SQLQueryException;

	public String getText(int columnIndex) throws SQLQueryException;

	public Blob getBlob(int columnIndex) throws SQLQueryException;

	public Clob getClob(int columnIndex) throws SQLQueryException;

	public String getString(int columnIndex) throws SQLQueryException;

	public BigDecimal getBigDecimal(int columnIndex) throws SQLQueryException;

	public BigInteger getBigInteger(int columnIndex) throws SQLQueryException;

	public Date getDate(int columnIndex) throws SQLQueryException;

	public Locale getLocale(int columnIndex) throws SQLQueryException;

	public Calendar getCalendar(int columnIndex) throws SQLQueryException;

	public TimeZone getTimeZone(int columnIndex) throws SQLQueryException;

	public int getRowNumber() throws SQLQueryException;

	public boolean setRowNumber(int rowNumber) throws SQLQueryException;

}
