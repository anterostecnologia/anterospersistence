package br.com.anteros.persistence.transaction;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import br.com.anteros.persistence.transaction.impl.TransactionException;

public interface TransactionManagerLookup {

	public TransactionManager getTransactionManager() throws TransactionException;

	public String getUserTransactionName();

	public Transaction getTransactionIdentifier(Transaction transaction);
}

