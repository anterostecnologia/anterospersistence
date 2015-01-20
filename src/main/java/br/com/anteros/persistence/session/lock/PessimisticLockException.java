package br.com.anteros.persistence.session.lock;

public class PessimisticLockException extends LockException {

	public PessimisticLockException(String message) {
		super(message);
	}

	public PessimisticLockException(String message, Throwable t, String sql) {
		super(message, t, sql);
	}

}
