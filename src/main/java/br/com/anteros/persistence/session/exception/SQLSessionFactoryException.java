package br.com.anteros.persistence.session.exception;

public class SQLSessionFactoryException extends RuntimeException {

	public SQLSessionFactoryException(String message) {
		super(message);
	}

	public SQLSessionFactoryException(String message, Throwable t) {
		super(message,t);
	}
}
