package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;

import br.com.anteros.persistence.log.Logger;
import br.com.anteros.persistence.log.LoggerProvider;
import br.com.anteros.persistence.session.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.Transaction;

public class JDBCTransaction implements Transaction {

	private static Logger log = LoggerProvider.getInstance().getLogger(JDBCTransaction.class.getName());

	private Connection connection;
	private SQLPersistenceContext context;
	private boolean begun;
	private boolean commitFailed;
	private boolean toggleAutoCommit;

	private boolean committed;

	private boolean rolledBack;

	public JDBCTransaction(Connection connection, SQLPersistenceContext context) {
		this.connection = connection;
		this.context = context;
	}

	@Override
	public void begin() throws Exception {
		if (begun) {
			return;
		}
		if (commitFailed) {
			throw new TransactionException("não foi possível reiniciar a transação após o commit ter falhado");
		}

		log.debug("begin");

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

		begun = true;
		committed = false;
		rolledBack = false;
	}

	@Override
	public void commit() throws Exception {
		if (!begun) {
			throw new TransactionException("A transação não foi iniciada");
		}

		log.debug("commit");

		try {
			getPersistenceContext().onBeforeExecuteCommit(getConnection());
			commitAndResetAutoCommit();
			log.debug("committed JDBC Connection");
			committed = true;
			getPersistenceContext().onAfterExecuteCommit(getConnection());
		} catch (SQLException e) {
			log.error("JDBC commit failed", e);
			commitFailed = true;
			throw new TransactionException("JDBC commit failed", e);
		} finally {
			begun = false;
		}
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
	public void rollback() throws Exception {
		if (!begun && !commitFailed) {
			throw new TransactionException("Transaction not successfully started");
		}

		log.debug("rollback");

		if (!commitFailed) {
			try {
				getPersistenceContext().onBeforeExecuteRollback(getConnection());
				rollbackAndResetAutoCommit();
				log.debug("rolled back JDBC Connection");
				rolledBack = true;
				getPersistenceContext().onAfterExecuteRollback(getConnection());
			} catch (SQLException e) {
				log.error("JDBC rollback failed", e);
				throw new TransactionException("JDBC rollback failed", e);
			} finally {
				begun = false;
			}
		}
	}

	@Override
	public boolean isActive() throws Exception {
		return begun && !(rolledBack || committed | commitFailed);
	}

}
