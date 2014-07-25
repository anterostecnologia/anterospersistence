package br.com.anteros.persistence.transaction.impl;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import br.com.anteros.persistence.transaction.TransactionManagerLookup;


public abstract class JNDITransactionManagerLookup implements TransactionManagerLookup {

	protected abstract String getName();

	public TransactionManager getTransactionManager(Properties props) throws TransactionException {
		try {
			return (TransactionManager) new InitialContext(props).lookup( getName() );
		}
		catch (NamingException ne) {
			throw new TransactionException( "Could not locate TransactionManager", ne );
		}
	}

	public Transaction getTransactionIdentifier(Transaction transaction) {
		return transaction;
	}
}






