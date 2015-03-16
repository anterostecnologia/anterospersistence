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

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.dsl.osql.types.IndexHint;

/**
 * Abstract superclass for SubQuery implementations
 *
 * @author tiwe modified by: Edson Martins
 *
 */
public abstract class AbstractOSQLSubQuery<Q extends AbstractOSQLSubQuery<Q>> extends DetachableSQLQuery<Q> {

	protected Configuration configuration;
	
	protected List<IndexHint> indexHints = new ArrayList<IndexHint>();

	public AbstractOSQLSubQuery() {
		super();
	}

	public AbstractOSQLSubQuery(Configuration configuration) {
		super(configuration.getTemplates(), new DefaultQueryMetadata().noValidate());
		this.configuration = configuration;
	}

	public AbstractOSQLSubQuery(Configuration configuration, QueryMetadata metadata) {
		super(configuration.getTemplates(), metadata);
		this.configuration = configuration;
	}

	protected SQLSerializer createSerializer() {
		return new SQLSerializer(configuration, new SQLAnalyser(getMetadata(), configuration,null));
	}
	
	public AbstractOSQLSubQuery<Q> indexHint(IndexHint... indexes) {
		for (IndexHint index : indexes) {
			indexHints.add(index);
		}
		this.getMetadata().setIndexHints(indexHints);
		return this;
	}

	public AbstractOSQLSubQuery<Q> addIndexHint(String alias, String indexName) {
		return indexHint(new IndexHint(alias, indexName));
	}

}
