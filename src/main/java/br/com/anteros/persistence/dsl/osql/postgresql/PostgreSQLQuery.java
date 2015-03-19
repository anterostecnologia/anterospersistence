/**
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
package br.com.anteros.persistence.dsl.osql.postgresql;

import br.com.anteros.persistence.dsl.osql.AbstractOSQLQuery;
import br.com.anteros.persistence.dsl.osql.Configuration;
import br.com.anteros.persistence.dsl.osql.QueryMetadata;
import br.com.anteros.persistence.dsl.osql.SQLOps;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.query.SQLQuery;

/**
 * PostgreSQLQuery provides PostgreSQL related extensions to SQLQuery
 *
 * @author tiwe
 * @see SQLQuery
 *
 */
public class PostgreSQLQuery extends AbstractOSQLQuery<PostgreSQLQuery> {

	public PostgreSQLQuery(SQLSession session, QueryMetadata metadata) {
		super(session, new Configuration(session), metadata);
	}

	public PostgreSQLQuery(SQLSession session) {
		super(session, new Configuration(session));
	}

    /**
     * @return
     */
    public PostgreSQLQuery forShare() {
        return addFlag(SQLOps.FOR_SHARE_FLAG);
    }

    /**
     * @return
     */
    public PostgreSQLQuery noWait() {
        return addFlag(SQLOps.NO_WAIT_FLAG);
    }

   
 
    @Override
    public PostgreSQLQuery clone(SQLSession session) {
        PostgreSQLQuery q = new PostgreSQLQuery(session, getMetadata().clone());
        q.clone(this);
        return q;
    }

}
