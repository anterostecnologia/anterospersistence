package br.com.anteros.persistence.dsl.osql;

public class OSQLException extends RuntimeException {

	public OSQLException(String message) {
		super(message);
	}

	public OSQLException(String message, Throwable t) {
		super(message,t);
	}
	
	public OSQLException(Throwable t) {
		super(t);
	}
}
