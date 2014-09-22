package br.com.anteros.persistence.transaction.impl;


public class TransactionException extends RuntimeException {

	public TransactionException(String msg) {
		super(msg);
	}

	public TransactionException(String msg, Exception e) {
		super(msg, e);
	}

}
