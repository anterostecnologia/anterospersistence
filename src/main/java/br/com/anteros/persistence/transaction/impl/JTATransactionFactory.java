package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import br.com.anteros.persistence.log.Logger;
import br.com.anteros.persistence.log.LoggerProvider;
import br.com.anteros.persistence.session.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.Transaction;
import br.com.anteros.persistence.transaction.TransactionFactory;

public class JTATransactionFactory implements TransactionFactory {

	private static Logger log = LoggerProvider.getInstance().getLogger(JTATransactionFactory.class.getName());

	public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

	private InitialContext initialContext;

	@Override
	public Transaction createTransaction(Connection connection, SQLPersistenceContext context)
			throws Exception {
		initialContext = new InitialContext();
		return new JTATransaction(connection, context, getUserTransaction());
	}

	protected UserTransaction getUserTransaction() {

		log.info("Attempting to locate UserTransaction via JNDI [" + DEFAULT_USER_TRANSACTION_NAME + "]");

		try {
			UserTransaction ut = (UserTransaction) getInitialContext().lookup(DEFAULT_USER_TRANSACTION_NAME);
			if (ut == null) {
				throw new TransactionException("Naming service lookup for UserTransaction returned null ["
						+ DEFAULT_USER_TRANSACTION_NAME
						+ "]");
			}

			log.info("Obtained UserTransaction");

			return ut;
		} catch (NamingException ne) {
			throw new TransactionException("Could not find UserTransaction in JNDI [" + DEFAULT_USER_TRANSACTION_NAME
					+ "]", ne);
		}
	}

	protected InitialContext getInitialContext() {
		return initialContext;
	}

}
