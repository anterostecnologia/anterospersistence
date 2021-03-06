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
package br.com.anteros.persistence.session;

import java.io.InputStream;

import br.com.anteros.persistence.session.configuration.AnterosPersistenceConfiguration;



public abstract class SQLSessionFactoryHelper {

	private static SQLSessionFactory sessionFactory;

	public static SQLSessionFactory getSessionFactory() throws Exception {
		if (sessionFactory == null) {
			sessionFactory = new AnterosPersistenceConfiguration().configure().buildSessionFactory();
		}
		return sessionFactory;
	}

	public static SQLSessionFactory getSessionFactory(String configurationFile) throws Exception {
		if (sessionFactory == null) {
			sessionFactory = new AnterosPersistenceConfiguration().configure(configurationFile).buildSessionFactory();
		}
		return sessionFactory;
	}

	public static SQLSessionFactory getNewSessionFactory(String configurationFile) throws Exception {
		return new AnterosPersistenceConfiguration().configure(configurationFile).buildSessionFactory();
	}
	
	public static SQLSessionFactory getNewSessionFactory(InputStream inputStream) throws Exception {
		return new AnterosPersistenceConfiguration().configure(inputStream).buildSessionFactory();
	}
}
