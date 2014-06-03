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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import br.com.anteros.persistence.log.LoggerProvider;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.comparator.DependencyComparator;
import br.com.anteros.persistence.metadata.configuration.ModelConfiguration;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.exception.AnterosConfigurationException;
import br.com.anteros.persistence.session.impl.SQLSessionFactoryImpl;
import br.com.anteros.persistence.util.ResourceUtils;

@Root(name = "anteros-configuration")
public abstract class AbstractSQLConfiguration implements SQLConfiguration {

	@Element(name = "session-factory")
	protected SessionFactoryConfiguration sessionFactory;
	@Transient
	protected EntityCacheManager entityCacheManager;
	@Transient
	protected DataSource dataSource;
	@Transient
	protected ModelConfiguration modelConfiguration;

	public AbstractSQLConfiguration() {
		entityCacheManager = new EntityCacheManager();
	}

	public AbstractSQLConfiguration(DataSource dataSource) {
		this();
		this.dataSource = dataSource;		
	}

	public AbstractSQLConfiguration(ModelConfiguration modelConfiguration) {
		this();
		this.modelConfiguration = modelConfiguration;
	}

	public AbstractSQLConfiguration(DataSource dataSource, ModelConfiguration modelConfiguration) {
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

	public AbstractSQLConfiguration addAnnotatedClass(Class<?> clazz) {
		getSessionFactoryConfiguration().getAnnotatedClasses().getClazz().add(clazz.getName());
		return this;
	}

	public AbstractSQLConfiguration addAnnotatedClass(String clazz) {
		getSessionFactoryConfiguration().getAnnotatedClasses().getClazz().add(clazz);
		return this;
	}

	public AbstractSQLConfiguration setLocationPlaceHolder(String location) {
		getSessionFactoryConfiguration().getPlaceholder().setLocation(location);
		return this;
	}

	public AbstractSQLConfiguration addDataSource(DataSourceConfiguration dataSource) {
		getSessionFactoryConfiguration().getDataSources().getDataSources().add(dataSource);
		return this;
	}

	public AbstractSQLConfiguration addDataSource(String id, Class<?> clazz, PropertyConfiguration[] properties) {
		return addDataSource(id, clazz.getName(), properties);
	}

	public AbstractSQLConfiguration addDataSource(String id, String clazz, PropertyConfiguration[] properties) {
		DataSourceConfiguration dataSource = new DataSourceConfiguration();
		dataSource.setId(id);
		dataSource.setClazz(clazz);
		for (PropertyConfiguration propertyConfiguration : properties) {
			dataSource.getProperties().add(propertyConfiguration);
		}
		getSessionFactoryConfiguration().getDataSources().getDataSources().add(dataSource);
		return this;
	}

	public AbstractSQLConfiguration addDataSource(String id, Class<?> clazz, Map<String, String> properties) {
		return addDataSource(id, clazz.getName(), properties);
	}

	public AbstractSQLConfiguration addDataSource(String id, String clazz, Map<String, String> properties) {
		List<PropertyConfiguration> props = new ArrayList<PropertyConfiguration>();
		for (String property : properties.keySet()) {
			props.add(new PropertyConfiguration().setName(property).setValue(properties.get(property)));
		}
		return addDataSource(id, clazz, props.toArray(new PropertyConfiguration[] {}));
	}

	public AbstractSQLConfiguration addDataSource(String id, Class<?> clazz, Properties properties) {
		return addDataSource(id, clazz.getName(), properties);
	}

	public AbstractSQLConfiguration addDataSource(String id, String clazz, Properties properties) {
		List<PropertyConfiguration> props = new ArrayList<PropertyConfiguration>();
		for (Object property : properties.keySet()) {
			props.add(new PropertyConfiguration().setName((String) property).setValue((String) properties.get(property)));
		}
		return addDataSource(id, clazz, props.toArray(new PropertyConfiguration[] {}));
	}

	public AbstractSQLConfiguration addProperty(PropertyConfiguration property) {
		getSessionFactoryConfiguration().getProperties().getProperties().add(property);
		return this;
	}

	public AbstractSQLConfiguration addProperties(Properties properties) {
		for (Object property : properties.keySet()) {
			addProperty(new PropertyConfiguration().setName((String) property).setValue((String) properties.get(property)));
		}
		return this;
	}

	public AbstractSQLConfiguration addProperties(PropertyConfiguration[] properties) {
		for (PropertyConfiguration property : properties) {
			addProperty(property);
		}
		return this;
	}

	public AbstractSQLConfiguration addProperty(String name, String value) {
		addProperty(new PropertyConfiguration().setName(name).setValue(value));
		return this;
	}

	public SQLSessionFactory buildSessionFactory() throws Exception {
		buildDataSource();
		if (dataSource == null)
			throw new AnterosConfigurationException(ResourceUtils.getMessage(this.getClass(), "datasourceNotConfigured"));
		loadEntities();
		SQLSessionFactoryImpl sessionFactory = new SQLSessionFactoryImpl(entityCacheManager, dataSource, this.getSessionFactoryConfiguration());
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

	public SQLConfiguration configure() throws AnterosConfigurationException {
		return configure(AnterosProperties.XML_CONFIGURATION);
	}

	public SQLConfiguration configure(String xmlFile) throws AnterosConfigurationException {
		InputStream is;
		try {
			final List<URL> resources = ResourceUtils.getResources(xmlFile, getClass());
			if ((resources !=null) && (resources.size()>0)) {
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

	public SQLConfiguration configure(InputStream xmlConfiguration) throws AnterosConfigurationException {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AnterosConfiguration result = serializer.read(AnterosConfiguration.class, xmlConfiguration);
			this.setSessionFactory(result.getSessionFactoryConfiguration());
			this.dataSource = null;
			this.buildDataSource();
			return this;
		} catch (final Exception e) {
			throw new AnterosConfigurationException("Impossível realizar a leitura do arquivo de configuração." + e);
		}
	}

	public SQLConfiguration configure(InputStream xmlConfiguration, InputStream placeHolder) throws AnterosConfigurationException {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AnterosConfiguration result = serializer.read(AnterosConfiguration.class, xmlConfiguration);
			result.setPlaceHolder(placeHolder);
			this.setSessionFactory(result.getSessionFactoryConfiguration());
			this.dataSource = null;
			this.buildDataSource();
			
			System.out.println("Teste config: " + LoggerProvider.class.getName());
			System.out.println("Teste config2: " + LoggerProvider.getInstance());
			
			return this;
		} catch (final Exception e) {
			throw new AnterosConfigurationException("Impossível realizar a leitura do arquivo de configuração." + e);
		}
	}

	protected abstract void buildDataSource() throws Exception;

	public DataSource getDataSource() {
		return dataSource;
	}

	public AbstractSQLConfiguration dataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	public AbstractSQLConfiguration modelConfiguration(ModelConfiguration modelConfiguration) {
		this.modelConfiguration = modelConfiguration;
		return this;
	}

	public EntityCacheManager getEntityCacheManager() {
		return entityCacheManager;
	}

	public String getProperty(String name) {
		return getSessionFactoryConfiguration().getProperties().getProperty(name);
	}
	
	public AbstractSQLConfiguration setPlaceHolder(InputStream placeHolder) throws IOException {
		if (placeHolder!=null){
			Properties props = new Properties();
			props.load(placeHolder);
		    getSessionFactoryConfiguration().getPlaceholder().setProperties(props);	
		}		
		return this;
	}
	
	public AbstractSQLConfiguration setProperties(Properties props){
		getSessionFactoryConfiguration().getPlaceholder().setProperties(props);	
		return this;
	}

}
