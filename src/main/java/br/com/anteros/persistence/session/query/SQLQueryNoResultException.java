package br.com.anteros.persistence.session.query;

public class SQLQueryNoResultException extends RuntimeException {

	public SQLQueryNoResultException() {
		super("Nenhum resultado foi encontrado na execução da consulta.");
	}

	public SQLQueryNoResultException(String msg) {
		super(msg);
	}

	public SQLQueryNoResultException(Throwable cause) {
		super(cause);
	}

	public SQLQueryNoResultException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
