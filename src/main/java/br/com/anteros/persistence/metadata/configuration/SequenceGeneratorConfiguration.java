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

import br.com.anteros.persistence.metadata.annotation.SequenceGenerator;

public class SequenceGeneratorConfiguration {

	private String sequenceName;

	private String catalog = "";

	private int initialValue = 1;

	private int startsWith = 1;

	private String schema = "";

	public SequenceGeneratorConfiguration(String sequenceName, String catalog, int initialValue, int startsWith, String schema) {
		this.sequenceName = sequenceName;
		this.catalog = catalog;
		this.initialValue = initialValue;
		this.startsWith = startsWith;
		this.schema = schema;
	}

	public SequenceGeneratorConfiguration(SequenceGenerator sequenceGenerator) {
		this.sequenceName = sequenceGenerator.sequenceName();
		this.catalog = sequenceGenerator.catalog();
		this.initialValue = sequenceGenerator.initialValue();
		this.startsWith = sequenceGenerator.startsWith();
		this.schema = sequenceGenerator.schema();
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public SequenceGeneratorConfiguration sequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
		return this;
	}

	public String getCatalog() {
		return catalog;
	}

	public SequenceGeneratorConfiguration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public SequenceGeneratorConfiguration initialValue(int initialValue) {
		this.initialValue = initialValue;
		return this;
	}

	public int getStartsWith() {
		return startsWith;
	}

	public SequenceGeneratorConfiguration startsWith(int startsWith) {
		this.startsWith = startsWith;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public SequenceGeneratorConfiguration schema(String schema) {
		this.schema = schema;
		return this;
	}
}
