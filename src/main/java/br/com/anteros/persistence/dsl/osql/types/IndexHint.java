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
package br.com.anteros.persistence.dsl.osql.types;

public class IndexHint {

	private String alias;
	private String indexName;
	private EntityPath<?> aliasPath;

	public IndexHint(EntityPath<?> alias, String indexName) {
		this.aliasPath = alias;
		this.alias = alias.getMetadata().getElement()+"";
		this.indexName = indexName;
	}

	public IndexHint(String alias, String indexName) {
		this.alias = alias;
		this.indexName = indexName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public EntityPath<?> getAliasPath() {
		return aliasPath;
	}

	public boolean hasAliasPath() {
		return aliasPath != null;
	}

	@Override
	public String toString() {
		return indexName + " : " + alias;
	}

}
