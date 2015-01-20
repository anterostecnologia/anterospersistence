package br.com.anteros.persistence.session.exception;

public class ConstraintViolationException extends SQLSessionException {

	private String constraintName;

	public ConstraintViolationException(String message) {
		super(message);
	}

	public ConstraintViolationException(String message, Throwable t, String sql) {
		super(message, t, sql);
	}
	
	public ConstraintViolationException(String message, Throwable t, String sql, String constraintName) {
		super(message, t, sql);
		this.constraintName = constraintName;
	}
}
