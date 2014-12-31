package br.com.anteros.persistence.session.query;

public class SQLQueryNonUniqueResultException extends RuntimeException {

	public SQLQueryNonUniqueResultException() {
		super("A consulta não retornou um único resultado.");
	}

	public SQLQueryNonUniqueResultException(String message) {
		super(message);
	}

	public SQLQueryNonUniqueResultException(Throwable cause) {
		super(cause);
	}

	public SQLQueryNonUniqueResultException(String message, Throwable cause) {
		super(message, cause);
	}

}
