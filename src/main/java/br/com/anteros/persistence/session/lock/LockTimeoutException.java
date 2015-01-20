package br.com.anteros.persistence.session.lock;

public class LockTimeoutException extends LockException {

	public LockTimeoutException(String message) {
		super(message);
	}

	public LockTimeoutException(String message, Throwable t, String sql) {
		super(message,t, sql);
	}

}
