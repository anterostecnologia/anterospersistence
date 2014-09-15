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
package br.com.anteros.persistence.session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import br.com.anteros.core.configuration.SessionFactoryConfiguration;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.schema.SchemaManager;
import br.com.anteros.persistence.schema.type.TableCreationType;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceProperties;
import br.com.anteros.persistence.session.context.CurrentSQLSessionContext;
import br.com.anteros.persistence.session.context.JTASQLSessionContext;
import br.com.anteros.persistence.session.context.ManagedSQLSessionContext;
import br.com.anteros.persistence.session.context.ThreadLocalSQLSessionContext;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.transaction.TransactionFactory;
import br.com.anteros.persistence.transaction.TransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.TransactionException;

public abstract class AbstractSQLSessionFactory implements SQLSessionFactory {

	private static Logger log = LoggerProvider.getInstance().getLogger(AbstractSQLSessionFactory.class.getName());

	protected DatabaseDialect dialect;
	protected EntityCacheManager entityCacheManager;
	protected DataSource dataSource;
	protected SessionFactoryConfiguration configuration;
	private static final ThreadLocal<Map<SQLSessionFactory, SQLSession>> sessionContext = new ThreadLocal<Map<SQLSessionFactory, SQLSession>>();
	protected CurrentSQLSessionContext currentSessionContext;
	private TransactionManager transactionManager;

	private boolean showSql = false;
	private boolean formatSql = false;
	private int queryTimeout = 0;

	public AbstractSQLSessionFactory(EntityCacheManager entityCacheManager, DataSource dataSource,
			SessionFactoryConfiguration configuration) throws Exception {
		this.entityCacheManager = entityCacheManager;
		this.dataSource = dataSource;
		this.configuration = configuration;

		if (configuration.getProperty(AnterosPersistenceProperties.DIALECT) == null) {
			throw new SQLSessionException("Dialeto não definido. Não foi possível instanciar SQLSessionFactory.");
		}

		String dialectProperty = configuration.getProperty(AnterosPersistenceProperties.DIALECT);
		Class<?> dialectClass = Class.forName(dialectProperty);

		if (!ReflectionUtils.isExtendsClass(DatabaseDialect.class, dialectClass))
			throw new SQLSessionException("A classe " + dialectClass.getName() + " não implementa a classe "
					+ DatabaseDialect.class.getName() + ".");

		this.dialect = (DatabaseDialect) dialectClass.newInstance();
		this.dialect.setDefaultCatalog(configuration.getProperty(AnterosPersistenceProperties.JDBC_CATALOG));
		this.dialect.setDefaultSchema(configuration.getProperty(AnterosPersistenceProperties.JDBC_SCHEMA));

		if (configuration.getProperty(AnterosPersistenceProperties.SHOW_SQL) != null)
			this.showSql = new Boolean(configuration.getProperty(AnterosPersistenceProperties.SHOW_SQL));

		if (configuration.getProperty(AnterosPersistenceProperties.FORMAT_SQL) != null)
			this.formatSql = new Boolean(configuration.getProperty(AnterosPersistenceProperties.FORMAT_SQL));

		if (configuration.getProperty(AnterosPersistenceProperties.QUERY_TIMEOUT) != null)
			this.queryTimeout = new Integer(configuration.getProperty(AnterosPersistenceProperties.QUERY_TIMEOUT))
					.intValue();

		this.currentSessionContext = buildCurrentSessionContext();
	}

	@Override
	public SQLSession getCurrentSession() throws Exception {
		if (currentSessionContext == null) {
			throw new SQLSessionException("No CurrentSessionContext configured!");
		}
		return currentSessionContext.currentSession();
	}

	protected CurrentSQLSessionContext buildCurrentSessionContext() throws Exception {
		String impl = configuration.getProperty(AnterosPersistenceProperties.CURRENT_SESSION_CONTEXT);
		if ((impl == null && transactionManager != null) || "jta".equals(impl)) {
			return new JTASQLSessionContext(this);
		} else if ("thread".equals(impl)) {
			return new ThreadLocalSQLSessionContext(this);
		} else if ("managed".equals(impl)) {
			return new ManagedSQLSessionContext(this);
		} else {
			return new ThreadLocalSQLSessionContext(this);
		}
	}

	protected abstract TransactionFactory getTransactionFactory();

	public void generateDDL() throws Exception {

		TableCreationType databaseDDLType = TableCreationType.NONE;
		TableCreationType scriptDDLType = TableCreationType.NONE;
		/*
		 * Verifica se é para gerar o schema no banco de dados
		 */
		String databaseDDLGeneration = configuration.getPropertyDef(
				AnterosPersistenceProperties.DATABASE_DDL_GENERATION, AnterosPersistenceProperties.NONE);
		databaseDDLGeneration = databaseDDLGeneration.toLowerCase();
		/*
		 * Verifica se é para gerar o schema em script sql
		 */
		String scriptDDLGeneration = configuration.getPropertyDef(AnterosPersistenceProperties.SCRIPT_DDL_GENERATION,
				AnterosPersistenceProperties.NONE);
		scriptDDLGeneration = scriptDDLGeneration.toLowerCase();

		/*
		 * Se não foi configurado para gerar nenhum schema retorna
		 */
		if ((databaseDDLGeneration.equals(AnterosPersistenceProperties.NONE))
				&& (scriptDDLGeneration.equals(AnterosPersistenceProperties.NONE))) {
			return;
		}

		/*
		 * Verifica se é para criar integridade referencial
		 */
		Boolean createReferentialIntegrity = Boolean.parseBoolean(configuration.getPropertyDef(
				AnterosPersistenceProperties.CREATE_REFERENCIAL_INTEGRITY, "true"));

		/*
		 * Verifica a forma de geração do schema no banco de dados
		 */
		if (databaseDDLGeneration.equals(AnterosPersistenceProperties.CREATE_ONLY)) {
			databaseDDLType = TableCreationType.CREATE;
		} else if (databaseDDLGeneration.equals(AnterosPersistenceProperties.DROP_AND_CREATE)) {
			databaseDDLType = TableCreationType.DROP;
		} else if (databaseDDLGeneration.equals(AnterosPersistenceProperties.CREATE_OR_EXTEND)) {
			databaseDDLType = TableCreationType.EXTEND;
		}

		/*
		 * Verifica a forma de geração do schema no script sql
		 */
		if (scriptDDLGeneration.equals(AnterosPersistenceProperties.CREATE_ONLY)) {
			scriptDDLType = TableCreationType.CREATE;
		} else if (scriptDDLGeneration.equals(AnterosPersistenceProperties.DROP_AND_CREATE)) {
			scriptDDLType = TableCreationType.DROP;
		} else if (scriptDDLGeneration.equals(AnterosPersistenceProperties.CREATE_OR_EXTEND)) {
			scriptDDLType = TableCreationType.EXTEND;
		}

		generateDDL(databaseDDLType, scriptDDLType, createReferentialIntegrity);
	}

	public void generateDDL(TableCreationType databaseDDLType, TableCreationType scriptDDLType,
			Boolean createReferentialIntegrity) throws Exception {

		/*
		 * Se foi definido a forma de gerar no banco de dados ou no script sql
		 */
		if ((databaseDDLType != TableCreationType.NONE) || ((scriptDDLType != TableCreationType.NONE))) {
			/*
			 * Verifica se foi definida a forma de saída: BANCO DE DADOS, SCRIPT
			 * SQL, AMBOS
			 */
			String ddlGenerationMode = configuration.getPropertyDef(AnterosPersistenceProperties.DDL_OUTPUT_MODE,
					AnterosPersistenceProperties.DEFAULT_DDL_GENERATION_MODE);
			if (ddlGenerationMode.equals(AnterosPersistenceProperties.NONE)) {
				return;
			}

			SQLSession sessionForDDL = this.openSession();

			try {
				SchemaManager schemaManager = new SchemaManager(sessionForDDL, entityCacheManager,
						createReferentialIntegrity);

				beforeGenerateDDL(sessionForDDL);

				/*
				 * Gera o schema no script sql
				 */
				if (ddlGenerationMode.equals(AnterosPersistenceProperties.DDL_SQL_SCRIPT_OUTPUT)
						|| ddlGenerationMode.equals(AnterosPersistenceProperties.DDL_BOTH_OUTPUT)) {
					String appLocation = configuration.getPropertyDef(
							AnterosPersistenceProperties.APPLICATION_LOCATION,
							AnterosPersistenceProperties.DEFAULT_APPLICATION_LOCATION);
					String createDDLJdbc = configuration.getPropertyDef(
							AnterosPersistenceProperties.CREATE_TABLES_FILENAME,
							AnterosPersistenceProperties.DEFAULT_CREATE_TABLES_FILENAME);
					String dropDDLJdbc = configuration.getPropertyDef(
							AnterosPersistenceProperties.DROP_TABLES_FILENAME,
							AnterosPersistenceProperties.DEFAULT_DROP_TABLES_FILENAME);
					schemaManager.writeDDLsToFiles(scriptDDLType, appLocation, createDDLJdbc, dropDDLJdbc);
				}

				/*
				 * Gera o schema no banco de dados
				 */
				if (ddlGenerationMode.equals(AnterosPersistenceProperties.DDL_DATABASE_OUTPUT)
						|| ddlGenerationMode.equals(AnterosPersistenceProperties.DDL_BOTH_OUTPUT)) {
					schemaManager.writeDDLToDatabase(databaseDDLType);
				}
				afterGenerateDDL(sessionForDDL);
			} finally {
				sessionForDDL.close();
			}	

		}

	}

	public abstract void beforeGenerateDDL(SQLSession session) throws Exception;

	public abstract void afterGenerateDDL(SQLSession session) throws Exception;

	public DatabaseDialect getDialect() {
		return dialect;
	}

	public void setDialect(DatabaseDialect dialect) {
		this.dialect = dialect;
	}

	public EntityCacheManager getEntityCacheManager() {
		return entityCacheManager;
	}

	public void setEntityCacheManager(EntityCacheManager enityCacheManager) {
		this.entityCacheManager = enityCacheManager;
	}

	public DataSource getDatasource() {
		return dataSource;
	}

	public void setDatasource(DataSource datasource) {
		this.dataSource = datasource;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public boolean isFormatSql() {
		return formatSql;
	}

	public void setFormatSql(boolean formatSql) {
		this.formatSql = formatSql;
	}

	public void onBeforeExecuteCommit(Connection connection) throws Exception {
		SQLSession session = getCurrentSession();
		if (session != null)
			session.getPersistenceContext().onBeforeExecuteCommit(connection);
	}

	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		SQLSession session = getCurrentSession();
		if (session != null)
			session.getPersistenceContext().onBeforeExecuteRollback(connection);
	}

	public void onAfterExecuteCommit(Connection connection) throws Exception {
		SQLSession session = getCurrentSession();
		if (session != null)
			session.getPersistenceContext().onAfterExecuteCommit(connection);
	}

	public void onAfterExecuteRollback(Connection connection) throws Exception {
		SQLSession session = getCurrentSession();
		if (session != null)
			session.getPersistenceContext().onAfterExecuteRollback(connection);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public SessionFactoryConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SessionFactoryConfiguration configuration) {
		this.configuration = configuration;
	}

	public int getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	protected void setConfigurationClientInfo(Connection connection) throws IOException, SQLException {
		String clientInfo = this.getConfiguration().getProperty(AnterosPersistenceProperties.CONNECTION_CLIENTINFO);
		if (clientInfo != null && clientInfo.length() > 0)
			this.getDialect().setConnectionClientInfo(connection, clientInfo);
	}

	/**
	 * Retorna a estratégia para fazer lookup (obter) transaction manager para
	 * JTA.
	 * 
	 * @return
	 * @throws TransactionException
	 */
	public abstract TransactionManagerLookup getTransactionManagerLookup() throws Exception;

	/**
	 * Gerenciador de transações JTA.
	 */
	public TransactionManager getTransactionManager() throws Exception {
		log.info("obtaining TransactionManager");
		if (transactionManager == null)
			transactionManager = getTransactionManagerLookup().getTransactionManager();
		return transactionManager;
	}
	
}
