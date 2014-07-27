package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.Transaction;

public class JTATransaction implements Transaction {

	private static Logger log = LoggerProvider.getInstance().getLogger(JTATransaction.class.getName());

	private Connection connection;
	private SQLPersistenceContext context;
	private boolean begun;
	private boolean commitFailed;

	private UserTransaction userTransaction;
	private boolean newTransaction;
	private boolean commitSucceeded;
	
	public JTATransaction(Connection connection, SQLPersistenceContext context, UserTransaction userTransaction) {
		this(connection, context);
	}

	public JTATransaction(Connection connection, SQLPersistenceContext context) {
		this.connection = connection;
		this.context = context;
	}

	@Override
	public void begin() throws Exception {
		if (begun) {
			return;
		}
		if (commitFailed) {
			throw new TransactionException("cannot re-start transaction after failed commit");
		}

		log.debug("begin");

		try {
			newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
			if (newTransaction) {
				userTransaction.begin();
				log.debug("Began a new JTA transaction");
			}
		} catch (Exception e) {
			log.error("JTA transaction begin failed", e);
			throw new TransactionException("JTA transaction begin failed", e);
		}

		begun = true;
		commitSucceeded = false;
		commitFailed = false;
	}

	@Override
	public void commit() throws Exception {
		if (!begun) {
			throw new TransactionException("Transaction not successfully started");
		}

		log.debug("commit");

		if (newTransaction) {
			try {
				getPersistenceContext().onBeforeExecuteCommit(getConnection());
				userTransaction.commit();
				commitSucceeded = true;
				log.debug("Committed JTA UserTransaction");
				getPersistenceContext().onAfterExecuteCommit(getConnection());
			} catch (Exception e) {
				commitFailed = true;
				log.error("JTA commit failed", e);
				throw new TransactionException("JTA commit failed: ", e);
			} finally {
				begun = false;
			}
		}
	}

	private SQLPersistenceContext getPersistenceContext() {
		return context;
	}

	private Connection getConnection() {
		return connection;
	}

	@Override
	public void rollback() throws Exception {
		if (!begun && !commitFailed) {
			throw new TransactionException("Transaction not successfully started");
		}

		log.debug("rollback");

		try {
			if (newTransaction) {
				if (!commitFailed) {
					getPersistenceContext().onBeforeExecuteRollback(getConnection());
					userTransaction.rollback();
					log.debug("Rolled back JTA UserTransaction");
					getPersistenceContext().onAfterExecuteRollback(getConnection());
				}
			} else {
				userTransaction.setRollbackOnly();
				log.debug("set JTA UserTransaction to rollback only");
			}
		} catch (Exception e) {
			log.error("JTA rollback failed", e);
			throw new TransactionException("JTA rollback failed", e);
		} finally {
			begun = false;
		}
	}

	@Override
	public boolean isActive() throws Exception {
		if (!begun || commitFailed || commitSucceeded) {
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

	@Override
	public void registerSynchronization(Synchronization synchronization) throws Exception {
		
	}

}
