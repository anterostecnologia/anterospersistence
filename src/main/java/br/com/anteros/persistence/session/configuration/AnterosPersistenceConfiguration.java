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

import javax.sql.DataSource;

import br.com.anteros.core.configuration.exception.AnterosConfigurationException;
import br.com.anteros.persistence.metadata.accessor.PropertyAccessorFactory;
import br.com.anteros.persistence.metadata.configuration.PersistenceModelConfiguration;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.impl.SQLSessionFactoryImpl;
import br.com.anteros.persistence.translation.AnterosPersistenceTranslate;

public class AnterosPersistenceConfiguration extends AnterosPersistenceConfigurationBase {
	
	private static AnterosPersistenceTranslate TRANSLATOR = AnterosPersistenceTranslate.getInstance();

	public AnterosPersistenceConfiguration() {
		super();
	}

	public AnterosPersistenceConfiguration(DataSource dataSource) {
		super(dataSource);
	}

	public AnterosPersistenceConfiguration(PersistenceModelConfiguration modelConfiguration) {
		super(modelConfiguration);
	}

	public AnterosPersistenceConfiguration(DataSource dataSource, PersistenceModelConfiguration modelConfiguration) {
		super(dataSource, modelConfiguration);
	}

	@Override
	public PropertyAccessorFactory getPropertyAccessorFactory() {
		//return new PropertyAcessorFactoryImpl();
	return null;
	}
	
	
	public SQLSessionFactory buildSessionFactory() throws Exception {
		prepareClassesToLoad();
		buildDataSource();
		if (dataSource == null)
			throw new AnterosConfigurationException(TRANSLATOR.getMessage(this.getClass(),
					"datasourceNotConfigured"));
		SQLSessionFactoryImpl sessionFactory = new SQLSessionFactoryImpl(entityCacheManager, dataSource,
				this.getSessionFactoryConfiguration());
		loadEntities(sessionFactory.getDialect());		
		sessionFactory.generateDDL();
		return sessionFactory;
	}

}
