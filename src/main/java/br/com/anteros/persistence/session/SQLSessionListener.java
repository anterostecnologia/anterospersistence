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
package br.com.anteros.persistence.session;

import java.util.Map;

import br.com.anteros.persistence.parameter.NamedParameter;

public interface SQLSessionListener {

	public void onExecuteSQL(String sql, NamedParameter[] parameters);

	public void onExecuteSQL(String sql, Map<String, Object> parameters);

	public void onExecuteSQL(String sql, Object[] parameters);

	public void onExecuteUpdateSQL(String sql, NamedParameter[] parameters);

	public void onExecuteUpdateSQL(String sql, Map<String, Object> parameters);

	public void onExecuteUpdateSQL(String sql, Object[] parameters);

	public void onClose(SQLSession session);
}
