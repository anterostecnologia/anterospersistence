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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import br.com.anteros.core.configuration.AnterosBasicConfiguration;
import br.com.anteros.core.configuration.AnterosCoreProperties;
import br.com.anteros.core.configuration.DataSourceConfiguration;
import br.com.anteros.core.configuration.PropertyConfiguration;
import br.com.anteros.core.configuration.SessionFactoryConfiguration;
import br.com.anteros.core.configuration.exception.AnterosConfigurationException;
import br.com.anteros.core.utils.ResourceUtils;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.comparator.DependencyComparator;
import br.com.anteros.persistence.metadata.configuration.ModelConfiguration;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.impl.SQLSessionFactoryImpl;

@Root(name = "anteros-configuration")
public abstract class AbstractPersistenceConfiguration extends AnterosBasicConfiguration implements
		PersistenceConfiguration {

	@Transient
	protected EntityCacheManager entityCacheManager;
	@Transient
	protected DataSource dataSource;
	@Transient
	protected ModelConfiguration modelConfiguration;

	public AbstractPersistenceConfiguration() {
		entityCacheManager = new EntityCacheManager();
	}

	public AbstractPersistenceConfiguration(DataSource dataSource) {
		this();
		this.dataSource = dataSource;
	}

	public AbstractPersistenceConfiguration(ModelConfiguration modelConfiguration) {
		this();
		this.modelConfiguration = modelConfiguration;
	}

	public AbstractPersistenceConfiguration(DataSource dataSource, ModelConfiguration modelConfiguration) {
		super();
		this.dataSource = dataSource;
		this.modelConfiguration = modelConfiguration;
	}

	public SessionFactoryConfiguration getSessionFactoryConfiguration() {
		if (sessionFactory == null)
			sessionFactory = new SessionFactoryConfiguration();
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactoryConfiguration value) {
		this.sessionFactory = value;
	}

	public AbstractPersistenceConfiguration addAnnotatedClass(Class<?> clazz) {
		getSessionFactoryConfiguration().getAnnotatedClasses().getClazz().add(clazz.getName());
		return this;
	}

	public AbstractPersistenceConfiguration addAnnotatedClass(String clazz) {
		getSessionFactoryConfiguration().getAnnotatedClasses().getClazz().add(clazz);
		return this;
	}

	public AbstractPersistenceConfiguration setLocationPlaceHolder(String location) {
		getSessionFactoryConfiguration().getPlaceholder().setLocation(location);
		return this;
	}

	public AbstractPersistenceConfiguration addDataSource(DataSourceConfiguration dataSource) {
		getSessionFactoryConfiguration().getDataSources().getDataSources().add(dataSource);
		return this;
	}

	public AbstractPersistenceConfiguration addDataSource(String id, Class<?> clazz, PropertyConfiguration[] properties) {
		return addDataSource(id, clazz.getName(), properties);
	}

	public AbstractPersistenceConfiguration addDataSource(String id, String clazz, PropertyConfiguration[] properties) {
		DataSourceConfiguration dataSource = new DataSourceConfiguration();
		dataSource.setId(id);
		dataSource.setClazz(clazz);
		for (PropertyConfiguration propertyConfiguration : properties) {
			dataSource.getProperties().add(propertyConfiguration);
		}
		getSessionFactoryConfiguration().getDataSources().getDataSources().add(dataSource);
		return this;
	}

	public AbstractPersistenceConfiguration addDataSource(String id, Class<?> clazz, Map<String, String> properties) {
		return addDataSource(id, clazz.getName(), properties);
	}

	public AbstractPersistenceConfiguration addDataSource(String id, String clazz, Map<String, String> properties) {
		List<PropertyConfiguration> props = new ArrayList<PropertyConfiguration>();
		for (String property : properties.keySet()) {
			props.add(new PropertyConfiguration().setName(property).setValue(properties.get(property)));
		}
		return addDataSource(id, clazz, props.toArray(new PropertyConfiguration[] {}));
	}

	public AbstractPersistenceConfiguration addDataSource(String id, Class<?> clazz, Properties properties) {
		return addDataSource(id, clazz.getName(), properties);
	}

	public AbstractPersistenceConfiguration addDataSource(String id, String clazz, Properties properties) {
		List<PropertyConfiguration> props = new ArrayList<PropertyConfiguration>();
		for (Object property : properties.keySet()) {
			props.add(new PropertyConfiguration().setName((String) property)
					.setValue((String) properties.get(property)));
		}
		return addDataSource(id, clazz, props.toArray(new PropertyConfiguration[] {}));
	}

	public AbstractPersistenceConfiguration addProperty(PropertyConfiguration property) {
		getSessionFactoryConfiguration().getProperties().getProperties().add(property);
		return this;
	}

	public AbstractPersistenceConfiguration addProperties(Properties properties) {
		for (Object property : properties.keySet()) {
			addProperty(new PropertyConfiguration().setName((String) property).setValue(
					(String) properties.get(property)));
		}
		return this;
	}

	public AbstractPersistenceConfiguration addProperties(PropertyConfiguration[] properties) {
		for (PropertyConfiguration property : properties) {
			addProperty(property);
		}
		return this;
	}

	public AbstractPersistenceConfiguration addProperty(String name, String value) {
		addProperty(new PropertyConfiguration().setName(name).setValue(value));
		return this;
	}

	public SQLSessionFactory buildSessionFactory() throws Exception {
		buildDataSource();
		if (dataSource == null)
			throw new AnterosConfigurationException(
					ResourceUtils.getMessage(this.getClass(), "datasourceNotConfigured"));
		loadEntities();
		SQLSessionFactoryImpl sessionFactory = new SQLSessionFactoryImpl(entityCacheManager, dataSource,
				this.getSessionFactoryConfiguration());
		sessionFactory.generateDDL();
		return sessionFactory;
	}

	public EntityCacheManager loadEntities() throws Exception {
		List<Class<? extends Serializable>> classes = getSessionFactoryConfiguration().getClasses();
		Collections.sort(classes, new DependencyComparator());

		if (modelConfiguration != null)
			this.entityCacheManager.load(modelConfiguration);
		else
			this.entityCacheManager.load(classes, true);
		return this.entityCacheManager;
	}

	@Override
	public AbstractPersistenceConfiguration configure() throws AnterosConfigurationException {
		return configure(AnterosCoreProperties.XML_CONFIGURATION);
	}

	@Override
	public AbstractPersistenceConfiguration configure(String xmlFile) throws AnterosConfigurationException {
		InputStream is;
		try {
			final List<URL> resources = ResourceUtils.getResources(xmlFile, getClass());
			if ((resources != null) && (resources.size() > 0)) {
				final URL url = resources.get(0);
				is = url.openStream();
				configure(is);
				return this;
			}
		} catch (final Exception e) {
			throw new AnterosConfigurationException("Impossível realizar a leitura " + xmlFile + " " + e);
		}

		throw new AnterosConfigurationException("Arquivo de configuração " + xmlFile + " não encontrado.");
	}

	@Override
	public AbstractPersistenceConfiguration configure(InputStream xmlConfiguration)
			throws AnterosConfigurationException {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AbstractPersistenceConfiguration result = serializer.read(this.getClass(), xmlConfiguration);
			this.setSessionFactory(result.getSessionFactoryConfiguration());
			this.dataSource = null;
			this.buildDataSource();
			return this;
		} catch (final Exception e) {
			throw new AnterosConfigurationException("Impossível realizar a leitura do arquivo de configuração." + e);
		}
	}

	protected abstract void buildDataSource() throws Exception;

	public DataSource getDataSource() {
		return dataSource;
	}

	public AbstractPersistenceConfiguration dataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	public AbstractPersistenceConfiguration modelConfiguration(ModelConfiguration modelConfiguration) {
		this.modelConfiguration = modelConfiguration;
		return this;
	}

	public EntityCacheManager getEntityCacheManager() {
		return entityCacheManager;
	}

	public String getProperty(String name) {
		return getSessionFactoryConfiguration().getProperties().getProperty(name);
	}

	public AbstractPersistenceConfiguration setPlaceHolder(InputStream placeHolder) throws IOException {
		if (placeHolder != null) {
			Properties props = new Properties();
			props.load(placeHolder);
			getSessionFactoryConfiguration().getPlaceholder().setProperties(props);
		}
		return this;
	}

	public AbstractPersistenceConfiguration setProperties(Properties props) {
		getSessionFactoryConfiguration().getProperties().setProperties(props);
		return this;
	}

	@Override
	public AbstractPersistenceConfiguration configure(InputStream xmlConfiguration, InputStream placeHolder)
			throws AnterosConfigurationException {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AbstractPersistenceConfiguration result = serializer.read(this.getClass(), xmlConfiguration);
			result.setPlaceHolder(placeHolder);
			this.setSessionFactory(result.getSessionFactoryConfiguration());
			this.dataSource = null;
			this.buildDataSource();

			return this;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new AnterosConfigurationException("Impossível realizar a leitura do arquivo de configuração." + e);
		}
	}

}