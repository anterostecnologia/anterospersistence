package br.com.anteros.persistence.handler;

import java.sql.ResultSet;

public interface ScrollableResultSetHandler extends ResultSetHandler {

	/**
	 * Método responsável por ler a linha corrente do ResultSet.
	 * 
	 */
	public abstract Object[] readCurrentRow(ResultSet resultSet) throws Exception;
}
