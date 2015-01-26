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
import java.sql.SQLException;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.AbstractTransaction;

public class JDBCTransaction extends AbstractTransaction {

	private static Logger log = LoggerProvider.getInstance().getLogger(JDBCTransaction.class.getName());

	private boolean toggleAutoCommit;

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
