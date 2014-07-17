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
package br.com.anteros.persistence.log.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import br.com.anteros.persistence.log.LogLevel;
import br.com.anteros.persistence.log.Logger;
import br.com.anteros.persistence.log.LoggerProvider;
import br.com.anteros.persistence.session.configuration.AnterosConfiguration;
import br.com.anteros.persistence.session.configuration.AnterosProperties;

/**
 * 
 * Promove a instância de um objeto Logger do tipo ConsoleLogger.
 * 
 * @author Douglas Junior (nassifrroma@gmail.com)
 * 
 */
public class ConsoleLoggerProvider extends LoggerProvider {

	private final LogLevel level = findLevel(AnterosProperties.CONSOLE_LOG_LEVEL);

	@Override
	public Logger getLogger(String name) {
		return new ConsoleLogger(name, level);
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
