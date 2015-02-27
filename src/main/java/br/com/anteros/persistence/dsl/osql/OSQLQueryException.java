package br.com.anteros.persistence.dsl.osql;

public class OSQLQueryException extends RuntimeException {

	public OSQLQueryException(String message) {
		super(message);
	}

	public OSQLQueryException(Throwable cause) {
		super(cause);
	}

	public OSQLQueryException(String message, Throwable cause) {
		super(message, cause);
	}

	public OSQLQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
