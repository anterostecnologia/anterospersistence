package br.com.anteros.persistence.session.query;


public interface ScrollableResults {
	
	public boolean next() throws SQLQueryException;

	public boolean previous() throws SQLQueryException;

}
