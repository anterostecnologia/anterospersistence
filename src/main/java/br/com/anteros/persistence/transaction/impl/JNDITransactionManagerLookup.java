package br.com.anteros.persistence.transaction.impl;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import br.com.anteros.persistence.transaction.TransactionManagerLookup;


public class JNDITransactionManagerLookup implements TransactionManagerLookup {

	protected String getName(){
		return "java:comp/UserTransaction";
	}

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

	@Override
	public TransactionManager getTransactionManager() throws TransactionException {
		try {
			return (TransactionManager) new InitialContext().lookup( getName() );
		}
		catch (NamingException ne) {
			throw new TransactionException( "Could not locate TransactionManager", ne );
		}
	}

	@Override
	public String getUserTransactionName() {
		return "java:comp/UserTransaction";
	}
}






