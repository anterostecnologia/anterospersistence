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
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.schema.SchemaManager;
import br.com.anteros.persistence.schema.type.TableCreationType;
import br.com.anteros.persistence.session.configuration.AnterosProperties;
import br.com.anteros.persistence.session.configuration.SessionFactoryConfiguration;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.util.ReflectionUtils;

public abstract class AbstractSQLSessionFactory implements SQLSessionFactory {

	protected DatabaseDialect dialect;
	protected EntityCacheManager entityCacheManager;
	protected DataSource dataSource;
	protected SessionFactoryConfiguration configuration;
	private static final ThreadLocal<Map<SQLSessionFactory, SQLSession>> sessionContext = new ThreadLocal<Map<SQLSessionFactory, SQLSession>>();

	private boolean showSql = false;
	private boolean formatSql = false;
	private int queryTimeout = 0;

	public AbstractSQLSessionFactory(EntityCacheManager entityCacheManager, DataSource dataSource,
			SessionFactoryConfiguration configuration)
			throws Exception {
		this.entityCacheManager = entityCacheManager;
		this.dataSource = dataSource;
		this.configuration = configuration;

		if (configuration.getProperty(AnterosProperties.DIALECT) == null) {
			throw new SQLSessionException("Dialeto não definido. Não foi possível instanciar SQLSessionFactory.");
		}

		String dialectProperty = configuration.getProperty(AnterosProperties.DIALECT);
		Class<?> dialectClass = Class.forName(dialectProperty);

		if (!ReflectionUtils.isExtendsClass(DatabaseDialect.class, dialectClass))
			throw new SQLSessionException("A classe " + dialectClass.getName() + " não implementa a classe "
					+ DatabaseDialect.class.getName() + ".");

		this.dialect = (DatabaseDialect) dialectClass.newInstance();
		this.dialect.setDefaultCatalog(configuration.getProperty(AnterosProperties.JDBC_CATALOG));
		this.dialect.setDefaultSchema(configuration.getProperty(AnterosProperties.JDBC_SCHEMA));

		if (configuration.getProperty(AnterosProperties.SHOW_SQL) != null)
			this.showSql = new Boolean(configuration.getProperty(AnterosProperties.SHOW_SQL));

		if (configuration.getProperty(AnterosProperties.FORMAT_SQL) != null)
			this.showSql = new Boolean(configuration.getProperty(AnterosProperties.FORMAT_SQL));

		if (configuration.getProperty(AnterosProperties.QUERY_TIMEOUT) != null)
			this.queryTimeout = new Integer(configuration.getProperty(AnterosProperties.QUERY_TIMEOUT)).intValue();
	}

	public void generateDDL() throws Exception {

		TableCreationType databaseDDLType = TableCreationType.NONE;
		TableCreationType scriptDDLType = TableCreationType.NONE;
		/*
		 * Verifica se é para gerar o schema no banco de dados
		 */
		String databaseDDLGeneration = configuration.getPropertyDef(AnterosProperties.DATABASE_DDL_GENERATION,
				AnterosProperties.NONE);
		databaseDDLGeneration = databaseDDLGeneration.toLowerCase();
		/*
		 * Verifica se é para gerar o schema em script sql
		 */
		String scriptDDLGeneration = configuration.getPropertyDef(AnterosProperties.SCRIPT_DDL_GENERATION,
				AnterosProperties.NONE);
		scriptDDLGeneration = scriptDDLGeneration.toLowerCase();

		/*
		 * Se não foi configurado para gerar nenhum schema retorna
		 */
		if ((databaseDDLGeneration.equals(AnterosProperties.NONE))
				&& (scriptDDLGeneration.equals(AnterosProperties.NONE))) {
			return;
		}

		/*
		 * Verifica se é para criar integridade referencial
		 */
		Boolean createReferentialIntegrity = Boolean.parseBoolean(configuration.getPropertyDef(
				AnterosProperties.CREATE_REFERENCIAL_INTEGRITY, "true"));

		/*
		 * Verifica a forma de geração do schema no banco de dados
		 */
		if (databaseDDLGeneration.equals(AnterosProperties.CREATE_ONLY)) {
			databaseDDLType = TableCreationType.CREATE;
		} else if (databaseDDLGeneration.equals(AnterosProperties.DROP_AND_CREATE)) {
			databaseDDLType = TableCreationType.DROP;
		} else if (databaseDDLGeneration.equals(AnterosProperties.CREATE_OR_EXTEND)) {
			databaseDDLType = TableCreationType.EXTEND;
		}

		/*
		 * Verifica a forma de geração do schema no script sql
		 */
		if (scriptDDLGeneration.equals(AnterosProperties.CREATE_ONLY)) {
			scriptDDLType = TableCreationType.CREATE;
		} else if (scriptDDLGeneration.equals(AnterosProperties.DROP_AND_CREATE)) {
			scriptDDLType = TableCreationType.DROP;
		} else if (scriptDDLGeneration.equals(AnterosProperties.CREATE_OR_EXTEND)) {
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
			String ddlGenerationMode = configuration.getPropertyDef(AnterosProperties.DDL_OUTPUT_MODE,
					AnterosProperties.DEFAULT_DDL_GENERATION_MODE);
			if (ddlGenerationMode.equals(AnterosProperties.NONE)) {
				return;
			}

			SchemaManager schemaManager = new SchemaManager(this.getSession(), entityCacheManager,
					createReferentialIntegrity);

			beforeGenerateDDL();

			/*
			 * Gera o schema no script sql
			 */
			if (ddlGenerationMode.equals(AnterosProperties.DDL_SQL_SCRIPT_OUTPUT)
					|| ddlGenerationMode.equals(AnterosProperties.DDL_BOTH_OUTPUT)) {
				String appLocation = configuration.getPropertyDef(AnterosProperties.APPLICATION_LOCATION,
						AnterosProperties.DEFAULT_APPLICATION_LOCATION);
				String createDDLJdbc = configuration.getPropertyDef(AnterosProperties.CREATE_TABLES_FILENAME,
						AnterosProperties.DEFAULT_CREATE_TABLES_FILENAME);
				String dropDDLJdbc = configuration.getPropertyDef(AnterosProperties.DROP_TABLES_FILENAME,
						AnterosProperties.DEFAULT_DROP_TABLES_FILENAME);
				schemaManager.writeDDLsToFiles(scriptDDLType, appLocation, createDDLJdbc, dropDDLJdbc);
			}

			/*
			 * Gera o schema no banco de dados
			 */
			if (ddlGenerationMode.equals(AnterosProperties.DDL_DATABASE_OUTPUT)
					|| ddlGenerationMode.equals(AnterosProperties.DDL_BOTH_OUTPUT)) {
				schemaManager.writeDDLToDatabase(databaseDDLType);
			}

			afterGenerateDDL();

		}

	}

	public abstract void beforeGenerateDDL() throws Exception;

	public abstract void afterGenerateDDL() throws Exception;

	public abstract SQLSession getSession() throws Exception;

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
		SQLSession session = existingSession(this);
		if (session != null)
			session.getPersistenceContext().onBeforeExecuteCommit(connection);
	}

	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		SQLSession session = existingSession(this);
		if (session != null)
			session.getPersistenceContext().onBeforeExecuteRollback(connection);
	}

	public void onAfterExecuteCommit(Connection connection) throws Exception {
		SQLSession session = existingSession(this);
		if (session != null)
			session.getPersistenceContext().onAfterExecuteCommit(connection);
	}

	public void onAfterExecuteRollback(Connection connection) throws Exception {
		SQLSession session = existingSession(this);
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

	protected static void doBind(SQLSession session, SQLSessionFactory factory) {
		Map sessionMap = sessionMap();
		if (sessionMap == null) {
			sessionMap = new HashMap();
			sessionContext.set(sessionMap);
		}
		sessionMap.put(factory, session);
	}

	protected static Map sessionMap() {
		return sessionContext.get();
	}

	protected static SQLSession existingSession(SQLSessionFactory factory) {
		Map sessionMap = sessionMap();
		if (sessionMap == null) {
			return null;
		}
		return (SQLSession) sessionMap.get(factory);
	}
	
	protected void setConfigurationClientInfo(Connection connection) throws IOException, SQLException {
		String clientInfo = this.getConfiguration().getProperty(AnterosProperties.CONNECTION_CLIENTINFO);
		if (clientInfo != null && clientInfo.length() > 0)
			this.getDialect().setConnectionClientInfo(connection, clientInfo);
	}

}
