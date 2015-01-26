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
package br.com.anteros.persistence.session.configuration;

import java.io.File;

public class AnterosPersistenceProperties {

	public static final String JDBC_DRIVER = "driverClassName";

	public static final String JDBC_URL = "url";

	public static final String JDBC_USER = "username";

	public static final String JDBC_PASSWORD = "password";

	public static final String JDBC_SCHEMA = "defaultSchema";

	public static final String JDBC_CATALOG = "defaultCatalog";

	public static final String DIALECT = "dialect";

	public static final String SHOW_SQL = "showsql";

	public static final String FORMAT_SQL = "formatsql";

	public static final String QUERY_TIMEOUT = "queryTimeout";

	public static final String DATABASE_DDL_GENERATION = "database-ddl-generation";

	public static final String SCRIPT_DDL_GENERATION = "script-ddl-generation";

	public static final String CREATE_ONLY = "create-tables";

	public static final String DROP_AND_CREATE = "drop-and-create-tables";

	public static final String CREATE_OR_EXTEND = "create-or-extend-tables";

	public static final String CREATE_REFERENCIAL_INTEGRITY = "create-referencial-integrity";

	public static final String NONE = "none";

	public static final String DATASOURCE = "dataSource";

	public static final String JNDI_DATASOURCE = "jndi-name";

	public static final String DEFAULT_CREATE_TABLES_FILENAME = "createTables.sql";

	public static final String DEFAULT_DROP_TABLES_FILENAME = "dropTables.sql";

	public static final String DDL_OUTPUT_MODE = "ddl-output-mode";

	public static final String DDL_SQL_SCRIPT_OUTPUT = "sql-script";

	public static final String DDL_DATABASE_OUTPUT = "database";

	public static final String DDL_BOTH_OUTPUT = "both";
	
	public static final String DDL_DATABASE_IGNORE_EXCEPTION = "ddl-database-ignore-exception";

	public static final String CREATE_TABLES_FILENAME = "create-tables-file-name";

	public static final String DROP_TABLES_FILENAME = "drop-tables-file-name";

	public static final String APPLICATION_LOCATION = "application-location";

	public static final String DEFAULT_DDL_GENERATION_MODE = DDL_DATABASE_OUTPUT;

	public static final String DEFAULT_APPLICATION_LOCATION = "." + File.separator;

	public static final String LOCK_TIMEOUT = "lock-timeout";

	public static final String LOGGER_PROVIDER = "loggerProviderClassName";

	public static final String CONSOLE_LOG_LEVEL = "consoleLogLevel";

	public static final String CONNECTION_CLIENTINFO = "connectionClientInfo";
	
	public static final String CURRENT_SESSION_CONTEXT = "current-session-context";
	
	public static final String TRANSACTION_MANAGER_LOOKUP = "transaction-manager-lookup";
	
	public static final String TRANSACTION_FACTORY = "transaction-factory";

}
