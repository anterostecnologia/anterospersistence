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

import java.io.Serializable;

/**
 * 
 * Implementação abstrata da interface BasicLogger, responsável por implementar
 * os métodos básicos de Logs disponíveis.
 * 
 * @author Douglas Junior (nassifrroma@gmail.com)
 * 
 */
public abstract class Logger implements Serializable, BasicLogger {

	private static final long serialVersionUID = 2380204066565833673L;

	private final String name;

	public Logger(String name) {
		this.name = name;
	}

	/**
	 * Retorna o nome dado ao Logger ao ser instanciado.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean isVerboseEnabled() {
		return isEnabled(LogLevel.VERBOSE);
	}

	@Override
	public void verbose(Object message, Throwable t) {
		log(LogLevel.VERBOSE, message, t);
	}

	@Override
	public void verbose(Object message) {
		verbose(message, null);
	}

	@Override
	public boolean isDebugEnabled() {
		return isEnabled(LogLevel.DEBUG);
	}

	@Override
	public void debug(Object message, Throwable t) {
		log(LogLevel.DEBUG, message, t);
	}

	@Override
	public void debug(Object message) {
		debug(message, null);
	}

	@Override
	public boolean isInfoEnabled() {
		return isEnabled(LogLevel.INFO);
	}

	@Override
	public void info(Object message, Throwable t) {
		log(LogLevel.INFO, message, t);
	}

	@Override
	public void info(Object message) {
		info(message, null);
	}

	@Override
	public boolean isWarnEnabled() {
		return isEnabled(LogLevel.WARN);
	}

	@Override
	public void warn(Object message, Throwable t) {
		log(LogLevel.WARN, message, t);
	}

	@Override
	public void warn(Object message) {
		warn(message, null);
	}

	@Override
	public boolean isErrorEnabled() {
		return isEnabled(LogLevel.ERROR);
	}

	@Override
	public void error(Object message, Throwable t) {
		log(LogLevel.ERROR, message, t);
	}

	@Override
	public void error(Object message) {
		error(message, null);
	}

	@Override
	public void log(LogLevel level, Object message, Throwable t) {
		doLog(level, message, t);
	}

	@Override
	public void log(LogLevel level, Object message) {
		log(level, message, null);
	}

	/**
	 * Recebe a mensagem, o Level e faz a execução do Log.
	 * @param level LogLevel
	 * @param message Object
	 * @param t Throwable
	 */
	protected abstract void doLog(LogLevel level, Object message, Throwable t);
}
