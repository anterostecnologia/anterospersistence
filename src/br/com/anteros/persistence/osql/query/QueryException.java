package br.com.anteros.persistence.osql.query;

public class QueryException extends RuntimeException {

	public QueryException(String arg){
		super(arg);
	}

	public QueryException(Exception e) {
		super(e);
	}
}
