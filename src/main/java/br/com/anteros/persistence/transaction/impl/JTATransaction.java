package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.AbstractTransaction;
import br.com.anteros.persistence.transaction.TransactionSatus;

public class JTATransaction extends AbstractTransaction {

	private static Logger log = LoggerProvider.getInstance().getLogger(JTATransaction.class.getName());

	private UserTransaction userTransaction;
	private boolean newTransaction;

	public JTATransaction(Connection connection, SQLPersistenceContext context, UserTransaction userTransaction) {
		this(connection, context);
	}

	public JTATransaction(Connection connection, SQLPersistenceContext context) {
		super(connection, context);
	}

	@Override
	protected void doBegin() throws Exception {
		newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
		if (newTransaction) {
			userTransaction.begin();
			log.debug("Began a new JTA transaction");
		}
	}

	@Override
	protected void doCommit() throws Exception {
		userTransaction.commit();
		log.debug("Committed JTA UserTransaction");
	}

	@Override
	protected void doRollback() throws Exception {
		userTransaction.rollback();
		log.debug("Rolled back JTA UserTransaction");
	}

	@Override
	protected boolean doExtendedActiveCheck() {
		if (status != TransactionSatus.ACTIVE || status == TransactionSatus.FAILED_COMMIT
				|| status == TransactionSatus.COMMITTED) {
			return false;
		}

		final int status;
		try {
			status = userTransaction.getStatus();
		} catch (SystemException se) {
			log.error("Could not determine transaction status", se);
			throw new TransactionException("Could not determine transaction status: ", se);
		}
		if (status == Status.STATUS_UNKNOWN) {
			throw new TransactionException("Could not determine transaction status");
		}
		else {
			return status == Status.STATUS_ACTIVE;
		}
	}

}
