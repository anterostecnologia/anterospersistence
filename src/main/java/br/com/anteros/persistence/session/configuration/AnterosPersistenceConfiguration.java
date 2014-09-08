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
package br.com.anteros.persistence.session.configuration;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.sql.DataSource;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import br.com.anteros.core.configuration.DataSourceConfiguration;
import br.com.anteros.core.configuration.PropertyConfiguration;
import br.com.anteros.core.configuration.exception.AnterosConfigurationException;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.ResourceUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.configuration.ModelConfiguration;
import br.com.anteros.persistence.sql.datasource.JDBCDataSource;
import br.com.anteros.persistence.sql.datasource.JNDIDataSourceFactory;

public class AnterosPersistenceConfiguration extends AbstractPersistenceConfiguration {

	public AnterosPersistenceConfiguration() {
		super();
	}

	public AnterosPersistenceConfiguration(DataSource dataSource) {
		super(dataSource);
	}

	public AnterosPersistenceConfiguration(ModelConfiguration modelConfiguration) {
		super(modelConfiguration);
	}

	public AnterosPersistenceConfiguration(DataSource dataSource, ModelConfiguration modelConfiguration) {
		super(dataSource, modelConfiguration);
	}

	@Override
	protected void buildDataSource() throws Exception {
		if (dataSource == null) {
			String dataSourceId = getProperty(AnterosPersistenceProperties.DATASOURCE);
			if (dataSourceId != null) {
				DataSourceConfiguration dataSourceConfiguration = getSessionFactoryConfiguration().getDataSourceById(
						dataSourceId);
				if (dataSourceConfiguration == null)
					throw new AnterosConfigurationException("Não foi possível encontrar o DataSource " + dataSourceId
							+ " configurado.");

				Class<?> datasourceClass = Thread.currentThread().getContextClassLoader()
						.loadClass(dataSourceConfiguration.getClazz());

				if (datasourceClass == JNDIDataSourceFactory.class) {
					String jndiName = getSessionFactoryConfiguration().ConvertPlaceHolder(
							dataSourceConfiguration.getProperty(AnterosPersistenceProperties.JNDI_DATASOURCE));
					if ((jndiName == null) || (jndiName.equals("")))
						throw new AnterosConfigurationException("não foi possível criar o DataSource " + dataSourceId
								+ ", não foi configurado o nome do JNDI.");
					dataSource = JNDIDataSourceFactory.getDataSource(jndiName);
				} else {
					dataSource = (DataSource) datasourceClass.newInstance();
					String value;
					for (PropertyConfiguration prop : dataSourceConfiguration.getProperties()) {
						value = getSessionFactoryConfiguration().ConvertPlaceHolder(prop.getValue());
						if (value != null)
							ReflectionUtils.invokeMethodWithParameterString(dataSource,
									"set" + StringUtils.capitalize(prop.getName()), value);
					}
				}
			} else {
				String driverClassName = getSessionFactoryConfiguration().getProperty("driverClassName");
				String url = getSessionFactoryConfiguration().getProperty("url");
				String username = getSessionFactoryConfiguration().getProperty("username");
				String password = getSessionFactoryConfiguration().getProperty("password");
				if ((driverClassName != null) && (username != null)) {
					dataSource = new JDBCDataSource(driverClassName, username, password, url);
				} else
					throw new AnterosConfigurationException("Nenhum DataSource foi configurado.");
			}
		}
	}
	
	public static InputStream getDefaultXmlInputStream() throws Exception {	
		List<URL> resources = ResourceUtils.getResources("/anteros-config.xml", AnterosPersistenceConfiguration.class);
		if ((resources == null) || (resources.isEmpty())) {
			resources = ResourceUtils.getResources("/assets/anteros-config.xml", AnterosPersistenceConfiguration.class);
			if ((resources != null) && (!resources.isEmpty())) {
				final URL url = resources.get(0);
				return url.openStream();
			}
		}
		return null;
	}
	


}
