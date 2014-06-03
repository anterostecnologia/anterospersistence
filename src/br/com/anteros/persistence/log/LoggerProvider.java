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
package br.com.anteros.persistence.log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import br.com.anteros.persistence.log.impl.ConsoleLoggerProvider;
import br.com.anteros.persistence.session.configuration.AnterosConfiguration;
import br.com.anteros.persistence.session.configuration.AnterosProperties;

/**
 * 
 * @author Douglas Junior (nassifrroma@gmail.com)
 * 
 */
public abstract class LoggerProvider {

	private static LoggerProvider PROVIDER;

	public abstract Logger getLogger(String name);

	public static synchronized LoggerProvider getInstance() {
		if (PROVIDER == null) {
			PROVIDER = findProvider();
		}
		return PROVIDER;
	}

	public static LoggerProvider findProvider() {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AnterosConfiguration result = serializer.read(AnterosConfiguration.class,
					AnterosConfiguration.getDefaultXmlInputStream());
			String providerClassName = result.getSessionFactoryConfiguration().getProperties()
					.getProperty(AnterosProperties.LOGGER_PROVIDER);
			System.out.println("providerClassName: " + providerClassName);
			if (providerClassName == null)
				return new ConsoleLoggerProvider();
			return (LoggerProvider) Class.forName(providerClassName).newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ConsoleLoggerProvider();
		}
	}
	
	public static LogLevel findLevel(String propertyValue) {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AnterosConfiguration result = serializer.read(AnterosConfiguration.class, AnterosConfiguration.getDefaultXmlInputStream());
			String consoleLogLevel = result.getSessionFactoryConfiguration().getProperties()
					.getProperty(propertyValue);
			System.out.println("consoleLogLevel: " + consoleLogLevel);
			return LogLevel.valueOf(consoleLogLevel);
		} catch (Exception e) {
		}
		return null;
	}

}
