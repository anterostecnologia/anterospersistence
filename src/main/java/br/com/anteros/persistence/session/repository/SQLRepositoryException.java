package br.com.anteros.persistence.session.repository;

public class SQLRepositoryException extends RuntimeException {

	public SQLRepositoryException(Throwable e) {
		super(e);
	}

	public SQLRepositoryException(String msg) {
		super(msg);
	}
	
}
