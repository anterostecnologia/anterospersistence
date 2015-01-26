/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.context;

import java.util.Hashtable;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.transaction.TransactionManagerLookup;

public class JTASQLSessionContext implements CurrentSQLSessionContext {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerProvider.getInstance().getLogger(JTASQLSessionContext.class.getName());

	protected final SQLSessionFactory factory;

	private transient Map<Transaction, SQLSession> currentSessionMap = new Hashtable<Transaction, SQLSession>();

	public JTASQLSessionContext(SQLSessionFactory factory) {
		this.factory = factory;
	}

	public SQLSession currentSession() throws Exception {
		TransactionManager transactionManager = null;
		if (factory instanceof AbstractSQLSessionFactory)
			transactionManager = ((AbstractSQLSessionFactory) factory).getTransactionManager();
		if (transactionManager == null) {
			throw new SQLSessionException("No TransactionManagerLookup specified");
		}

		Transaction txn;
		try {
			txn = transactionManager.getTransaction();
			if (txn == null) {
				throw new SQLSessionException("Unable to locate current JTA transaction");
			}
			if (!isInProgress(txn.getStatus())) {
				throw new SQLSessionException("Current transaction is not in progress");
			}
		} catch (SQLSessionException e) {
			throw e;
		} catch (Throwable t) {
			throw new SQLSessionException("Problem locating/validating JTA transaction", t);
		}
		
		TransactionManagerLookup lookup = null;
		if (factory instanceof AbstractSQLSessionFactory)
			lookup = ((AbstractSQLSessionFactory)factory).getTransactionManagerLookup();

		final Transaction txnIdentifier = lookup == null ? txn : lookup.getTransactionIdentifier(txn);

		SQLSession currentSession = currentSessionMap.get(txnIdentifier);

		if (currentSession == null) {
			currentSession = factory.openSession();

			try {
				txn.registerSynchronization(new CleaningSession(txnIdentifier, this));
			} catch (Throwable t) {
				try {
					currentSession.close();
				} catch (Throwable ignore) {
					log.debug("Unable to release generated current-session on failed synch registration", ignore);
				}
				throw new SQLSessionException("Unable to register cleanup Synchronization with TransactionManager");
			}

			currentSessionMap.put(txnIdentifier, currentSession);
		}

		return currentSession;
	}

	private boolean isInProgress(int status) {
		return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
	}

	protected static class CleaningSession implements Synchronization {
		private Object transactionIdentifier;
		private JTASQLSessionContext context;

		public CleaningSession(Object transactionIdentifier, JTASQLSessionContext context) {
			this.transactionIdentifier = transactionIdentifier;
			this.context = context;
		}

		public void beforeCompletion() {
		}

		public void afterCompletion(int i) {
			context.currentSessionMap.remove(transactionIdentifier);
		}
	}
}
