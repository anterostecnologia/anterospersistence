package br.com.anteros.persistence.transaction.impl;


public class NullSynchronizationException extends TransactionException {
	public NullSynchronizationException() {
		this( "Synchronization to register cannot be null" );
	}

	public NullSynchronizationException(String s) {
		super( s );
	}
}
