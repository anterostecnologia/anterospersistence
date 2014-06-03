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

/**
 * 
 * @author Douglas Junior (nassifrroma@gmail.com)
 * 
 */
public interface BasicLogger {

	public boolean isVerboseEnabled();

	public void verbose(Object message, Throwable t);

	public void verbose(Object message);

	public boolean isDebugEnabled();

	public void debug(Object message, Throwable t);

	public void debug(Object message);

	public boolean isInfoEnabled();

	public void info(Object message, Throwable t);

	public void info(Object message);

	public boolean isWarnEnabled();

	public void warn(Object message, Throwable t);

	public void warn(Object message);

	public boolean isErrorEnabled();

	public void error(Object message, Throwable t);

	public void error(Object message);

	public void log(LogLevel level, Object message, Throwable t);

	public void log(LogLevel level, Object message);
	
	public boolean isEnabled(LogLevel level);

}
