package br.com.anteros.persistence.session.lock;

import br.com.anteros.persistence.session.exception.SQLSessionException;

public class LockException extends SQLSessionException {

	public LockException(String message) {
		super(message);
	}

	public LockException(String message, Throwable t, String sql) {
		super(message,t, sql);
	}
}