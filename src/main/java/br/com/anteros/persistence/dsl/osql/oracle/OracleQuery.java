/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package br.com.anteros.persistence.dsl.osql.oracle;

import br.com.anteros.persistence.dsl.osql.AbstractOSQLQuery;
import br.com.anteros.persistence.dsl.osql.Configuration;
import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.QueryMetadata;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.session.SQLSession;

/**
 * OracleQuery provides Oracle specific extensions to the base SQL query type
 *
 * @author tiwe
 */
public class OracleQuery extends AbstractOSQLQuery<OracleQuery> {

	private static final String CONNECT_BY = "\nconnect by ";

	private static final String CONNECT_BY_NOCYCLE_PRIOR = "\nconnect by nocycle prior ";

	private static final String CONNECT_BY_PRIOR = "\nconnect by prior ";

	private static final String ORDER_SIBLINGS_BY = "\norder siblings by ";

	private static final String START_WITH = "\nstart with ";

	public OracleQuery(SQLSession session, QueryMetadata metadata) {
		super(session, new Configuration(session), metadata);
	}

	public OracleQuery(SQLSession session) {
		super(session, new Configuration(session));
	}

	/**
	 * @param cond
	 * @return
	 */
	public OracleQuery connectByPrior(Predicate cond) {
		return addFlag(Position.BEFORE_ORDER, CONNECT_BY_PRIOR, cond);
	}

	/**
	 * @param cond
	 * @return
	 */
	public OracleQuery connectBy(Predicate cond) {
		return addFlag(Position.BEFORE_ORDER, CONNECT_BY, cond);
	}

	/**
	 * @param cond
	 * @return
	 */
	public OracleQuery connectByNocyclePrior(Predicate cond) {
		return addFlag(Position.BEFORE_ORDER, CONNECT_BY_NOCYCLE_PRIOR, cond);
	}

	/**
	 * @param cond
	 * @return
	 */
	public <A> OracleQuery startWith(Predicate cond) {
		return addFlag(Position.BEFORE_ORDER, START_WITH, cond);
	}

	/**
	 * @param path
	 * @return
	 */
	public OracleQuery orderSiblingsBy(Expression<?> path) {
		return addFlag(Position.BEFORE_ORDER, ORDER_SIBLINGS_BY, path);
	}

	@Override
	public OracleQuery clone(SQLSession session) {
		OracleQuery q = new OracleQuery(session, getMetadata().clone());
		q.clone(this);
		return q;
	}
}
