package br.com.anteros.persistence.session.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import br.com.anteros.persistence.handler.ScrollableResultSetHandler;
import br.com.anteros.persistence.session.SQLSession;

public class ScrollableResultSetImpl implements ScrollableResultSet {

	private ResultSet resultSet;
	private SQLSession session;
	private ScrollableResultSetHandler scrollableHandler;
	private Object[] currentRow;

	public ScrollableResultSetImpl(SQLSession session, ResultSet resultSet, ScrollableResultSetHandler scrollableHandler) {
		this.session = session;
		this.resultSet = resultSet;
		this.scrollableHandler = scrollableHandler;
	}

	@Override
	public boolean next() throws SQLQueryException {
		try {
			boolean result = getResultSet().next();
			readCurrentRow(result);
			return result;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível passar para o próximo registro do ResultSet.", "");
		}
	}

	@Override
	public boolean previous() throws SQLQueryException {
		try {
			boolean result = getResultSet().previous();
			readCurrentRow(result);
			return result;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível voltar para o registro anterior do ResultSet.", "");
		}
	}

	@Override
	public boolean scroll(int i) throws SQLQueryException {
		try {
			boolean result = getResultSet().relative(i);
			readCurrentRow(result);
			return result;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível executar o método scroll.", "");
		}
	}

	private void readCurrentRow(boolean result) throws SQLQueryException {
		if (result) {
			try {
				currentRow = scrollableHandler.readCurrentRow(resultSet);
			} catch (Exception e) {
				throw new SQLQueryException("Não foi possível ler o registro corrente do ResultSet.", e);
			}
		}
	}

	@Override
	public boolean last() throws SQLQueryException {
		try {
			boolean result = getResultSet().last();
			readCurrentRow(result);
			return result;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível ir para o último registro do ResultSet.", "");
		}
	}

	@Override
	public boolean first() throws SQLQueryException {
		try {
			boolean result = getResultSet().first();
			readCurrentRow(result);
			return result;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível ir para o primeiro registro do ResultSet.", "");
		}
	}

	@Override
	public void beforeFirst() throws SQLQueryException {
		try {
			getResultSet().beforeFirst();
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível executar o método beforeFirst do ResultSet.", "");
		}
	}

	@Override
	public void afterLast() throws SQLQueryException {
		try {
			getResultSet().afterLast();
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível executar o método afterLast do ResultSet.", "");
		}
	}

	@Override
	public boolean isFirst() throws SQLQueryException {
		try {
			return getResultSet().isFirst();
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível executar o método isFirst do ResultSet.", "");
		}
	}

	@Override
	public boolean isLast() throws SQLQueryException {
		try {
			return getResultSet().isLast();
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível executar o método isLast do ResultSet.", "");
		}
	}

	@Override
	public void close() throws SQLQueryException {
		try {
			getResultSet().close();
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível fechar o ResultSet.", "");
		}
	}

	@Override
	public Object[] get() throws SQLQueryException {
		return currentRow;
	}

	@Override
	public Object get(int columnIndex) throws SQLQueryException {
		return currentRow[columnIndex];
	}

	@Override
	public Integer getInteger(int columnIndex) throws SQLQueryException {
		return (Integer) currentRow[columnIndex];
	}

	@Override
	public Long getLong(int columnIndex) throws SQLQueryException {
		return (Long) currentRow[columnIndex];
	}

	@Override
	public Float getFloat(int columnIndex) throws SQLQueryException {
		return (Float) currentRow[columnIndex];
	}

	@Override
	public Boolean getBoolean(int columnIndex) throws SQLQueryException {
		return (Boolean) currentRow[columnIndex];
	}

	@Override
	public Double getDouble(int columnIndex) throws SQLQueryException {
		return (Double) currentRow[columnIndex];
	}

	@Override
	public Short getShort(int columnIndex) throws SQLQueryException {
		return (Short) currentRow[columnIndex];
	}

	@Override
	public Byte getByte(int columnIndex) throws SQLQueryException {
		return (Byte) currentRow[columnIndex];
	}

	@Override
	public Character getCharacter(int columnIndex) throws SQLQueryException {
		return (Character) currentRow[columnIndex];
	}

	@Override
	public byte[] getBinary(int columnIndex) throws SQLQueryException {
		return (byte[]) currentRow[columnIndex];
	}

	@Override
	public String getText(int columnIndex) throws SQLQueryException {
		return (String) currentRow[columnIndex];
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLQueryException {
		return (Blob) currentRow[columnIndex];
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLQueryException {
		return (Clob) currentRow[columnIndex];
	}

	@Override
	public String getString(int columnIndex) throws SQLQueryException {
		return (String) currentRow[columnIndex];
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLQueryException {
		return (BigDecimal) currentRow[columnIndex];
	}

	@Override
	public BigInteger getBigInteger(int columnIndex) throws SQLQueryException {
		return (BigInteger) currentRow[columnIndex];
	}

	@Override
	public Date getDate(int columnIndex) throws SQLQueryException {
		return (Date) currentRow[columnIndex];
	}

	@Override
	public Locale getLocale(int columnIndex) throws SQLQueryException {
		return (Locale) currentRow[columnIndex];
	}

	@Override
	public Calendar getCalendar(int columnIndex) throws SQLQueryException {
		return (Calendar) currentRow[columnIndex];
	}

	@Override
	public TimeZone getTimeZone(int columnIndex) throws SQLQueryException {
		return (TimeZone) currentRow[columnIndex];
	}

	@Override
	public int getRowNumber() throws SQLQueryException {
		try {
			return getResultSet().getRow() - 1;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível executar o método getRow do ResultSet.", "");
		}
	}

	@Override
	public boolean setRowNumber(int rowNumber) throws SQLQueryException {
		if (rowNumber >= 0)
			rowNumber++;
		try {
			boolean result = getResultSet().absolute(rowNumber);
			readCurrentRow(result);
			return result;
		} catch (SQLException ex) {
			throw getSession().getDialect().convertSQLException(ex, "Não foi possível avancar usando o método absolute do ResultSet.", "");
		}
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public SQLSession getSession() {
		return session;
	}

}
