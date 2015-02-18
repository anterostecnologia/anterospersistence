/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.transaction;

import java.sql.Connection;

import javax.transaction.Status;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.impl.JDBCTransaction;
import br.com.anteros.persistence.transaction.impl.SynchronizationRegistry;
import br.com.anteros.persistence.transaction.impl.TransactionException;

public abstract class AbstractTransaction implements Transaction {

	private static Logger log = LoggerProvider.getInstance().getLogger(JDBCTransaction.class.getName());
	private final SynchronizationRegistry synchronizationRegistry = new SynchronizationRegistry();

	private Connection connection;
	private SQLPersistenceContext context;

	protected TransactionSatus status = TransactionSatus.NOT_ACTIVE;

	public AbstractTransaction(Connection connection, SQLPersistenceContext context) {
		this.connection = connection;
		this.context = context;
	}

	@Override
	public void begin() throws Exception {
		if (status == TransactionSatus.ACTIVE) {
			throw new TransactionException("transações aninhadas não são suportadas");
		}
		if (status == TransactionSatus.FAILED_COMMIT) {
			throw new TransactionException("não foi possível reiniciar a transação após o commit ter falhado");
		}

		log.debug("begin");

		doBegin();

		status = TransactionSatus.ACTIVE;

	}

	protected abstract void doBegin() throws Exception;

	@Override
	public void commit() throws Exception {
		if (status != TransactionSatus.ACTIVE) {
			throw new TransactionException("A transação não foi iniciada");
		}

		log.debug("commit");

		notifySynchronizationsBeforeTransactionCompletion();

		getPersistenceContext().onBeforeExecuteCommit(getConnection());

		try {

			doCommit();
			status = TransactionSatus.COMMITTED;
			getPersistenceContext().onAfterExecuteCommit(getConnection());
			notifySynchronizationsAfterTransactionCompletion(Status.STATUS_COMMITTED);
		} catch (Exception e) {
			log.error("JDBC commit failed", e);
			status = TransactionSatus.FAILED_COMMIT;
			notifySynchronizationsAfterTransactionCompletion(Status.STATUS_UNKNOWN);
			throw new TransactionException("commit failed", e);
		}
	}

	protected abstract void doCommit() throws Exception;

	private void notifySynchronizationsBeforeTransactionCompletion() {
		synchronizationRegistry.notifySynchronizationsBeforeTransactionCompletion();
	}

	private void notifySynchronizationsAfterTransactionCompletion(int status) {
		synchronizationRegistry.notifySynchronizationsAfterTransactionCompletion(status);
	}

	protected SQLPersistenceContext getPersistenceContext() {
		return context;
	}

	protected Connection getConnection() {
		return connection;
	}

	@Override
	public void rollback() throws Exception {
		if (status != TransactionSatus.ACTIVE && status != TransactionSatus.FAILED_COMMIT) {
			throw new TransactionException("Transação não foi iniciada");
		}

		log.debug("rollback");

		if (status != TransactionSatus.FAILED_COMMIT) {
			getPersistenceContext().onBeforeExecuteCommit(getConnection());
			try {
				doRollback();
				status = TransactionSatus.ROLLED_BACK;
				getPersistenceContext().onAfterExecuteRollback(getConnection());
				notifySynchronizationsAfterTransactionCompletion(Status.STATUS_ROLLEDBACK);
			} catch (Exception e) {
				notifySynchronizationsAfterTransactionCompletion(Status.STATUS_UNKNOWN);
				throw new TransactionException("rollback failed", e);
			}
		}
	}

	protected abstract void doRollback() throws Exception;

	@Override
	public boolean isActive() throws Exception {
		return status == TransactionSatus.ACTIVE && doExtendedActiveCheck();
	}

	protected boolean doExtendedActiveCheck() {
		return true;
	}

	public void registerSynchronization(AnterosSynchronization synchronization) throws Exception {
		synchronizationRegistry.registerSynchronization(synchronization);
	}

}
