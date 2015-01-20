package br.com.anteros.persistence.session.lock;

public class OptimisticLockException extends LockException {

	public OptimisticLockException(String message) {
		super(message);
	}

	public OptimisticLockException(String message, Throwable t, String sql) {
		super(message, t, sql);
	}

}
