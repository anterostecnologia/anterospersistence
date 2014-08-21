/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.anteros.persistence.dsl.osql;

import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.session.SQLSession;

/**
 * SQLQuery is a JDBC based implementation of the {@link SQLCommonQuery}
 * interface
 *
 * @author tiwe
 */
public class OSQLQuery extends AbstractOSQLQuery<OSQLQuery> {

	/**
	 * Create a detached SQLQuery instance The query can be attached via the
	 * clone method
	 *
	 * @param connection
	 *            Connection to use
	 * @param templates
	 *            SQLTemplates to use
	 */
	public OSQLQuery(SQLTemplates templates) {
		super(null, templates, new DefaultQueryMetadata());
	}

	/**
	 * Create a new SQLQuery instance
	 *
	 * @param conn
	 *            Connection to use
	 * @param templates
	 *            SQLTemplates to use
	 */
	public OSQLQuery(SQLSession session, SQLTemplates templates) {
		super(session, templates, new DefaultQueryMetadata());
	}

	/**
	 * Create a new SQLQuery instance
	 *
	 * @param conn
	 *            Connection to use
	 * @param templates
	 *            SQLTemplates to use
	 * @param metadata
	 */
	public OSQLQuery(SQLSession session, SQLTemplates templates, QueryMetadata metadata) {
		super(session, templates, metadata);
	}

	@Override
	public OSQLQuery clone(SQLSession session) {
		OSQLQuery q = new OSQLQuery(session, templates, getMetadata().clone());
		q.clone(this);
		return q;
	}

}