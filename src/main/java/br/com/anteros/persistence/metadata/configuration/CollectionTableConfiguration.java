/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.metadata.configuration;

import br.com.anteros.persistence.metadata.annotation.CollectionTable;
import br.com.anteros.persistence.metadata.annotation.JoinColumn;
import br.com.anteros.persistence.metadata.annotation.UniqueConstraint;

public class CollectionTableConfiguration {

	private String name;
	private String schema = "";
	private String catalog = "";

	private JoinColumnConfiguration[] joinColumns = 
			{};
	private UniqueConstraintConfiguration[] uniqueConstraints={};

	public CollectionTableConfiguration() {
	}

	public CollectionTableConfiguration(String name, JoinColumnConfiguration[] joinColumns) {
		this.name = name;
		this.joinColumns = joinColumns;
	}

	public CollectionTableConfiguration(CollectionTable collectionTable) {
		JoinColumn[] joinColumns = collectionTable.joinColumns();
		JoinColumnConfiguration[] joinColumnsConf = null;
		if (joinColumns != null) {
			joinColumnsConf = new JoinColumnConfiguration[joinColumns.length];
			for (int i = 0; i < joinColumns.length; i++)
				joinColumnsConf[i] = new JoinColumnConfiguration(joinColumns[i].name(), joinColumns[i].referencedColumnName())
						.columnDefinition(joinColumns[i].columnDefinition()).insertable(joinColumns[i].insertable())
						.updatable(joinColumns[i].updatable()).unique(joinColumns[i].unique());
		}
		this.name = collectionTable.name();
		this.joinColumns = joinColumnsConf;
		this.catalog = collectionTable.catalog();
		this.schema = collectionTable.schema();

		UniqueConstraint[] constraints = collectionTable.uniqueConstraints();
		UniqueConstraintConfiguration[] uniqueConstraintsDef = null;
		if (constraints != null) {
			uniqueConstraintsDef = new UniqueConstraintConfiguration[constraints.length];
			for (int i = 0; i < constraints.length; i++)
				uniqueConstraintsDef[i] = new UniqueConstraintConfiguration(constraints[i].name(), constraints[i].columnNames());
		}
		uniqueConstraints(uniqueConstraintsDef);
	}

	public String getName() {
		return name;
	}

	public CollectionTableConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public JoinColumnConfiguration[] getJoinColumns() {
		return joinColumns;
	}

	public CollectionTableConfiguration joinColumns(JoinColumnConfiguration[] joinColumns) {
		this.joinColumns = joinColumns;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public CollectionTableConfiguration schema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getCatalog() {
		return catalog;
	}

	public CollectionTableConfiguration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public UniqueConstraintConfiguration[] getUniqueConstraints() {
		return uniqueConstraints;
	}

	public CollectionTableConfiguration uniqueConstraints(UniqueConstraintConfiguration[] uniqueConstraints) {
		this.uniqueConstraints = uniqueConstraints;
		return this;
	}
	
	public boolean hasJoinColumns(){
		return ((joinColumns!=null) && (joinColumns.length>0));
	}


}
