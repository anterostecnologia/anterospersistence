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
package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
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
