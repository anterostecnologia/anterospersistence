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
package br.com.anteros.persistence.dsl.osql;

import br.com.anteros.persistence.session.SQLSession;

/**
 * OSQLQuery is a Anteros based implementation of the {@link SQLCommonQuery} interface
 *
 * @author tiwe modified by: Edson Martins
 */
public class OSQLQuery extends AbstractOSQLQuery<OSQLQuery> {

	/**
	 * Create a detached OSQLQuery instance The query can be attached via the clone method
	 * 
	 * @param configuration
	 */
	public OSQLQuery(Configuration configuration) {
		super(null, configuration);
	}
	
	/**
	 * Create a new OSQLQuery instance
	 * 
	 * @param session
	 */
	public OSQLQuery(SQLSession session) {
		super(session, new Configuration(session), new DefaultQueryMetadata());
	}

	/**
	 * Create a new OSQLQuery instance
	 * 
	 * @param session
	 * @param configuration
	 */
	public OSQLQuery(SQLSession session, Configuration configuration) {
		super(session, configuration, new DefaultQueryMetadata());
	}

	/**
	 * Create a new OSQLQuery instance
	 *
	 * @param conn
	 *            Connection to use
	 * @param templates
	 *            SQLTemplates to use
	 * @param metadata
	 */
	public OSQLQuery(SQLSession session, Configuration configuration, QueryMetadata metadata) {
		super(session, configuration, metadata);
	}

	/**
	 * Cria um clone da OSQLQuery atribuindo a sessão onde deverá ser executada a consulta.
	 */
	@Override
	public OSQLQuery clone(SQLSession session) {
		OSQLQuery q = new OSQLQuery(session, configuration, getMetadata().clone());
		q.clone(this);
		return q;
	}

}
