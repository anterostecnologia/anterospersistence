package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.AbstractTransaction;

public class JDBCTransaction extends AbstractTransaction {

	private static Logger log = LoggerProvider.getInstance().getLogger(JDBCTransaction.class.getName());
	private final SynchronizationRegistry synchronizationRegistry = new SynchronizationRegistry();

	private Connection connection;
	private SQLPersistenceContext context;
	private boolean begun;
	private boolean commitFailed;
	private boolean toggleAutoCommit;

	private boolean committed;

	private boolean rolledBack;

	public JDBCTransaction(Connection connection, SQLPersistenceContext context) {
		super(connection, context);
	}

	@Override
	protected void doBegin() {
		try {
			toggleAutoCommit = getConnection().getAutoCommit();
			log.debug("status atual do autocommit: " + toggleAutoCommit);
			if (getConnection().getAutoCommit()) {
				log.debug("desabilitando autocommit");
				getConnection().setAutoCommit(false);
			}
		} catch (SQLException e) {
			log.error("JDBC begin falhou", e);
			throw new TransactionException("JDBC begin failed: ", e);
		}
	}

	@Override
	protected void doCommit() {
		try {
			commitAndResetAutoCommit();
			log.debug("committed JDBC Connection");
		} catch (Exception e) {
			throw new TransactionException("unable to commit against JDBC connection", e);
		}
	}

	private void notifySynchronizationsBeforeTransactionCompletion() {
		synchronizationRegistry.notifySynchronizationsBeforeTransactionCompletion();
	}

	private void notifySynchronizationsAfterTransactionCompletion(int status) {
		begun = false;
		synchronizationRegistry.notifySynchronizationsAfterTransactionCompletion(status);
	}

	private void commitAndResetAutoCommit() throws SQLException {
		try {
			getConnection().commit();
		} finally {
			toggleAutoCommit();
		}
	}

	private void rollbackAndResetAutoCommit() throws SQLException {
		try {
			getConnection().rollback();
			notifySynchronizationsAfterTransactionCompletion(Status.STATUS_ROLLEDBACK);
		} finally {
			toggleAutoCommit();
		}
	}

	private void toggleAutoCommit() {
		try {
			if (toggleAutoCommit) {
				log.debug("re-enabling autocommit");
				getConnection().setAutoCommit(true);
			}
		} catch (Exception sqle) {
			log.error("Could not toggle autocommit", sqle);
		}
	}

	private SQLPersistenceContext getPersistenceContext() {
		return context;
	}

	private Connection getConnection() {
		return connection;
	}

	@Override
	public boolean isActive() throws Exception {
		return begun && !(rolledBack || committed | commitFailed);
	}

	@Override
	public void registerSynchronization(Synchronization synchronization) throws Exception {
		synchronizationRegistry.registerSynchronization(synchronization);
	}

	@Override
	protected void doRollback() {
		try {
			rollbackAndResetAutoCommit();
			log.debug("rolled back JDBC Connection");
		} catch (SQLException e) {
			throw new TransactionException("JDBC rollback failed", e);
		}
	}

}
