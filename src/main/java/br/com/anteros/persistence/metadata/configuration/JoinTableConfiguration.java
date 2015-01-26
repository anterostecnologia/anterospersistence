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

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.metadata.annotation.JoinColumn;
import br.com.anteros.persistence.metadata.annotation.JoinTable;
import br.com.anteros.persistence.metadata.annotation.UniqueConstraint;

public class JoinTableConfiguration {

	private String name;
	private String schema = "";
	private String catalog = "";
	private JoinColumnConfiguration[] joinColumns={};
	private JoinColumnConfiguration[] inversedJoinColumns={};
	private UniqueConstraintConfiguration[] uniqueConstraints={};

	public JoinTableConfiguration() {
	}

	public JoinTableConfiguration(String name, JoinColumnConfiguration[] joinColumns, JoinColumnConfiguration[] inversedJoinColumns) {
		this.name = name;
		this.joinColumns = joinColumns;
		this.inversedJoinColumns = inversedJoinColumns;
	}

	public JoinTableConfiguration(JoinTable joinTable) {
		this.name = joinTable.name();

		JoinColumn[] columns = joinTable.joinColumns();
		if (columns != null) {
			joinColumns = new JoinColumnConfiguration[columns.length];
			for (int i = 0; i < columns.length; i++)
				joinColumns[i] = new JoinColumnConfiguration(columns[i].name(), columns[i].referencedColumnName()).columnDefinition(columns[i]
						.columnDefinition());
		}

		JoinColumn[] inversed = joinTable.inversedJoinColumns();
		if (inversed != null) {
			inversedJoinColumns = new JoinColumnConfiguration[inversed.length];
			for (int i = 0; i < inversed.length; i++)
				inversedJoinColumns[i] = new JoinColumnConfiguration(inversed[i].name(), inversed[i].referencedColumnName());
		}
		
		UniqueConstraint[] constraints = joinTable.uniqueConstraints();
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

	public JoinTableConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public JoinColumnConfiguration[] getJoinColumns() {
		return joinColumns;
	}

	public JoinTableConfiguration joinColumns(JoinColumnConfiguration[] joinColumns) {
		this.joinColumns = joinColumns;
		return this;
	}

	public JoinColumnConfiguration[] getInversedJoinColumns() {
		return inversedJoinColumns;
	}

	public JoinTableConfiguration inversedJoinColumns(JoinColumnConfiguration[] inversedJoinColumns) {
		this.inversedJoinColumns = inversedJoinColumns;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public JoinTableConfiguration schema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getCatalog() {
		return catalog;
	}

	public JoinTableConfiguration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public UniqueConstraintConfiguration[] getUniqueConstraints() {
		return uniqueConstraints;
	}

	public JoinTableConfiguration uniqueConstraints(UniqueConstraintConfiguration[] uniqueConstraints) {
		this.uniqueConstraints = uniqueConstraints;
		return this;
	}

	public String[] getColumnNames() {
		List<String> result = new ArrayList<String>();
		for (JoinColumnConfiguration column : joinColumns) {
			result.add(column.getName());
		}
		return result.toArray(new String[] {});
	}

	public String[] getUniqueColumnNames() {
		List<String> result = new ArrayList<String>();
		for (JoinColumnConfiguration column : joinColumns) {
			if (column.isUnique()){
				result.add(column.getName());
			}
		}
		return result.toArray(new String[] {});
	}
}
