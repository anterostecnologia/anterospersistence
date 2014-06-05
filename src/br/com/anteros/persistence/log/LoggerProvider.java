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
import br.com.anteros.persistence.util.ResourceUtils;

/**
 * 
 * Classe abstrata responsável por promover o objeto Logger a ser utilizado.
 * 
 * @author Douglas Junior (nassifrroma@gmail.com)
 * 
 */
public abstract class LoggerProvider {

	private static LoggerProvider PROVIDER;

	/**
	 * Instancia e retorna um objeto do tipo Logger.
	 * 
	 * @param name
	 *            Nome da classe que está sendo logada.
	 * @return Logger
	 */
	public abstract Logger getLogger(String name);

	/**
	 * Retorna uma instância Singleton do LoggerProvider configurado no arquivo
	 * anteros-config.xml
	 * 
	 * @return LoggerProvider
	 */
	public static synchronized LoggerProvider getInstance() {
		if (PROVIDER == null) {
			PROVIDER = findProvider();
		}
		return PROVIDER;
	}

	/**
	 * Busca o arquivo XML anteros-config.xml e tenta ler a propriedade
	 * loggerProviderClassName para saber qual a classe LoggerProvider que deve
	 * ser instanciada. Caso nenhuma classe seja encontrada será instanciado um
	 * ConsoleLoggerProvider.
	 * 
	 * @return LoggerProvider
	 */
	private static LoggerProvider findProvider() {
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
			System.err.println(ResourceUtils.getMessage(LoggerProvider.class, "not_configured", ex.getMessage()));
			return new ConsoleLoggerProvider();
		}
	}

	/**
	 * 
	 * Busca o arquivo XML anteros-config.xml e tenta ler a propriedade
	 * referente ao LogLevel para saber qual o Level de log que deve ser
	 * instanciada. Caso nenhuma Level seja encontrada será retornado null.
	 * 
	 * @param propertyValue
	 *            Nome da propriedade onde se encontra o LogLevel no arquivo
	 *            anteros-config.xml
	 * @return LogLevel
	 */
	public static LogLevel findLevel(String propertyValue) {
		try {
			Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
			final AnterosConfiguration result = serializer.read(AnterosConfiguration.class,
					AnterosConfiguration.getDefaultXmlInputStream());
			String consoleLogLevel = result.getSessionFactoryConfiguration().getProperties()
					.getProperty(propertyValue);
			System.out.println("consoleLogLevel: " + consoleLogLevel);
			return LogLevel.valueOf(consoleLogLevel);
		} catch (Exception e) {
		}
		return null;
	}

}
