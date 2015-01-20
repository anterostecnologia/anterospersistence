package br.com.anteros.persistence.session.exception;

public class QueryTimeoutException extends SQLSessionException {

	public QueryTimeoutException(String message) {
		super(message);
	}

	public QueryTimeoutException(String message, Throwable t, String sql) {
		super(message, t, sql);
	}

}
