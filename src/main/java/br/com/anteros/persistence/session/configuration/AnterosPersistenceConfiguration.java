/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.configuration;

import java.io.InputStream;

import javax.sql.DataSource;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.com.anteros.core.resource.messages.AnterosBundle;
import br.com.anteros.core.resource.messages.AnterosResourceBundle;
import br.com.anteros.core.utils.IOUtils;
import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.persistence.metadata.accessor.PropertyAccessorFactory;
import br.com.anteros.persistence.metadata.configuration.PersistenceModelConfiguration;
import br.com.anteros.persistence.resource.messages.AnterosPersistenceMessages;
import br.com.anteros.persistence.session.ExternalFileManager;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.exception.AnterosConfigurationException;
import br.com.anteros.persistence.session.impl.SQLSessionFactoryImpl;
import br.com.anteros.xml.helper.XMLReader;

public class AnterosPersistenceConfiguration extends AnterosPersistenceConfigurationBase {

	private static AnterosBundle MESSAGES = AnterosResourceBundle.getBundle(AnterosPersistenceProperties.ANTEROS_PERSISTENCE,AnterosPersistenceMessages.class);

	public AnterosPersistenceConfiguration() {
		super();
	}

	public AnterosPersistenceConfiguration(DataSource dataSource, ExternalFileManager externalFileManager) {
		super(dataSource, externalFileManager);
	}

	public AnterosPersistenceConfiguration(PersistenceModelConfiguration modelConfiguration, ExternalFileManager externalFileManager) {
		super(modelConfiguration, externalFileManager);
	}

	public AnterosPersistenceConfiguration(DataSource dataSource, PersistenceModelConfiguration modelConfiguration, ExternalFileManager externalFileManager) {
		super(dataSource, modelConfiguration, externalFileManager);
	}

	@Override
	public PropertyAccessorFactory getPropertyAccessorFactory() {
		// return new PropertyAcessorFactoryImpl();
		return null;
	}

	public SQLSessionFactory buildSessionFactory() throws Exception {
		prepareClassesToLoad();
		buildDataSource();
		if (dataSource == null)
			throw new AnterosConfigurationException(MESSAGES.getMessage(this.getClass().getSimpleName()+".datasourceNotConfigured"));
		SQLSessionFactoryImpl sessionFactory = new SQLSessionFactoryImpl(entityCacheManager, dataSource, this.getSessionFactoryConfiguration(), externalFileManager);
		loadEntities(sessionFactory.getDialect());
		sessionFactory.generateDDL();
		return sessionFactory;
	}

	@Override
	protected AbstractPersistenceConfiguration parseXmlConfiguration(InputStream xmlConfiguration) throws Exception {
		String xml = IOUtils.toString(xmlConfiguration);

		/*
		 * Cria a configuração da fábrica de sessões baseado no xml de configuração.
		 */
		SessionFactoryConfiguration sessionFactoryConfiguration = new SessionFactoryConfiguration();
		sessionFactoryConfiguration.setPlaceholder(new PlaceholderConfiguration(XMLReader.readAttributeFromXML(xml, SESSION_FACTORY_PATH + "/" + PLACEHOLDER,
				LOCATION)));
		sessionFactoryConfiguration.setPackageToScanEntity(new PackageScanEntity(XMLReader.readAttributeFromXML(xml, SESSION_FACTORY_PATH + "/"
				+ PACKAGE_SCAN_ENTITY, PACKAGE_NAME)));
		sessionFactoryConfiguration.setIncludeSecurityModel((Boolean) ObjectUtils.convert(
				XMLReader.readElementFromXML(xml, SESSION_FACTORY_PATH + "/" + INCLUDE_SECURITY_MODEL), Boolean.class));

		DataSourcesConfiguration dataSourcesConfiguration = new DataSourcesConfiguration();
		sessionFactoryConfiguration.setDataSources(dataSourcesConfiguration);
		NodeList dataSources = XMLReader.readNodesFromXML(xml, SESSION_FACTORY_PATH + "/" + DATA_SOURCES);

		/*
		 * Lê as fontes de dados
		 */
		for (int i = 0, length = dataSources.getLength(); i < length; i++) {
			Node dataSource = dataSources.item(i);
			DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration(XMLReader.readAttributeFromNode(dataSource, DATA_SOURCE, ID),
					XMLReader.readAttributeFromNode(dataSource, DATA_SOURCE, CLASS_NAME));
			NodeList properties = XMLReader.readNodesFromNode(dataSource, DATA_SOURCE_PROPERTY);
			for (int z = 0, zLength = properties.getLength(); z < zLength; z++) {
				Node property = properties.item(z);
				PropertyConfiguration propertyConfiguration = new PropertyConfiguration(XMLReader.readAttributeFromNode(property, PROPERTY, NAME),
						XMLReader.readAttributeFromNode(property, PROPERTY, VALUE));
				dataSourceConfiguration.getProperties().add(propertyConfiguration);
			}
			dataSourcesConfiguration.getDataSources().add(dataSourceConfiguration);
		}

		/*
		 * Lê as propriedades
		 */
		NodeList sessionProperties = XMLReader.readNodesFromXML(xml, SESSION_FACTORY_PATH + "/" + PROPERTIES);
		for (int z = 0, zLength = sessionProperties.getLength(); z < zLength; z++) {
			Node property = sessionProperties.item(z);
			sessionFactoryConfiguration.addProperty(XMLReader.readAttributeFromNode(property, PROPERTY, NAME),
					XMLReader.readAttributeFromNode(property, PROPERTY, VALUE));
		}

		/*
		 * Lê a lista de classes anotadas
		 */
		NodeList annotatedClasses = XMLReader.readNodesFromXML(xml, SESSION_FACTORY_PATH + "/" + ANNOTATED_CLASSES);
		for (int z = 0, zLength = annotatedClasses.getLength(); z < zLength; z++) {
			Node property = annotatedClasses.item(z);
			sessionFactoryConfiguration.addAnnotatedClass(XMLReader.readElementFromNode(property, CLASS_NAME));
		}
		
		setSessionFactory(sessionFactoryConfiguration);
		return this;
	}

	public AnterosPersistenceConfiguration setPackageToScanEntity(PackageScanEntity packageToScanEntity){
		getSessionFactoryConfiguration().setPackageToScanEntity(packageToScanEntity);
		return this;
	}
}
