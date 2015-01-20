package br.com.anteros.persistence.session.lock;

public class LockAcquisitionException extends LockException {

	public LockAcquisitionException(String message) {
		super(message);
	}

	public LockAcquisitionException(String message, Throwable t, String sql) {
		super(message,t, sql);
	}
}
