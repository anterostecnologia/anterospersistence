/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.session.impl;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import br.com.anteros.core.configuration.SessionFactoryConfiguration;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceProperties;
import br.com.anteros.persistence.session.context.CurrentSQLSessionContext;
import br.com.anteros.persistence.session.context.JTASQLSessionContext;
import br.com.anteros.persistence.session.context.ManagedSQLSessionContext;
import br.com.anteros.persistence.session.context.ThreadLocalSQLSessionContext;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.transaction.TransactionFactory;
import br.com.anteros.persistence.transaction.TransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.JDBCTransactionFactory;
import br.com.anteros.persistence.transaction.impl.JNDITransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.TransactionException;

/**
 * Implementação de SessionFactory. Factory fornecedora de SQLSessions.
 * 
 */
public class SQLSessionFactoryImpl extends AbstractSQLSessionFactory {

	private TransactionFactory transactionFactory;
	private static Logger log = LoggerProvider.getInstance().getLogger(SQLSessionFactoryImpl.class.getName());
	private TransactionManagerLookup transactionManagerLookup;
	private TransactionManager transactionManager;
	private CurrentSQLSessionContext currentSessionContext;

	public SQLSessionFactoryImpl(EntityCacheManager entityCacheManager, DataSource dataSource,
			SessionFactoryConfiguration configuration) throws Exception {
		super(entityCacheManager, dataSource, configuration);
		
		this.currentSessionContext = buildCurrentSessionContext();
	}

	@Override
	public SQLSession getCurrentSession() throws Exception {
		if ( currentSessionContext == null ) {
			throw new SQLSessionException( "No CurrentSessionContext configured!" );
		}
		return currentSessionContext.currentSession();
	}

	public void beforeGenerateDDL() throws Exception {
	}

	public void afterGenerateDDL() throws Exception {
	}

	public SQLSession openSession() throws Exception {
		return openSession(this.getDatasource().getConnection());
	}

	@Override
	protected TransactionFactory getTransactionFactory() {
		if (transactionFactory == null) {
			transactionFactory = new JDBCTransactionFactory();
		}
		return transactionFactory;
	}

	@Override
	public TransactionManagerLookup getTransactionManagerLookup() throws Exception {
		if (transactionManagerLookup == null) {
			String tmLookupClass = configuration.getProperty(AnterosPersistenceProperties.TRANSACTION_MANAGER_LOOKUP);
			if (tmLookupClass == null) {
				tmLookupClass = JNDITransactionManagerLookup.class.getName();
			}
			if (tmLookupClass == null) {
				log.info("No TransactionManagerLookup configured (in JTA environment, use of read-write or transactional second-level cache is not recommended)");
				return null;
			} else {
				log.info("instantiating TransactionManagerLookup: " + tmLookupClass);
				try {
					transactionManagerLookup = (TransactionManagerLookup) ReflectionUtils.classForName(tmLookupClass)
							.newInstance();
					log.info("instantiated TransactionManagerLookup");
				} catch (Exception e) {
					log.error("Could not instantiate TransactionManagerLookup", e);
					throw new TransactionException("Could not instantiate TransactionManagerLookup '" + tmLookupClass
							+ "'");
				}
			}
		}
		return transactionManagerLookup;
	}

	@Override
	public TransactionManager getTransactionManager() throws Exception {
		log.info("obtaining TransactionManager");
		if (transactionManager == null)
			transactionManager = getTransactionManagerLookup().getTransactionManager();
		return transactionManager;
	}
	
	private CurrentSQLSessionContext buildCurrentSessionContext() throws Exception {
		String impl = configuration.getProperty( AnterosPersistenceProperties.CURRENT_SESSION_CONTEXT );
		if ( impl == null && transactionManager != null ) {
			impl = "jta";
		}

		if ( impl == null ) {
			return null;
		}
		else if ( "jta".equals( impl ) ) {
			return new JTASQLSessionContext( this );
		}
		else if ( "thread".equals( impl ) ) {
			return new ThreadLocalSQLSessionContext( this );
		}
		else if ( "managed".equals( impl ) ) {
			return new ManagedSQLSessionContext( this );
		}
		else {
			return new ThreadLocalSQLSessionContext( this );
		}
	}

	@Override
	public SQLSession openSession(Connection connection) throws Exception {
		setConfigurationClientInfo(connection);
		return new SQLSessionImpl(this, connection, this.getEntityCacheManager(), new SQLQueryRunner(),
				this.getDialect(), this.isShowSql(), this.isFormatSql(), this.getQueryTimeout(),
				this.getTransactionFactory());
	}

}
