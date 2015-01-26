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
package br.com.anteros.persistence.schema.definition;

import java.io.Writer;

import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class SequenceGeneratorSchema extends GeneratorSchema {

	protected int initialValue = 1;
	protected int incrementSize = 1;
	protected int cacheSize = 0;

	public int getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
	}

	public int getIncrementSize() {
		return incrementSize;
	}

	public void setIncrementSize(int incrementSize) {
		this.incrementSize = incrementSize;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws Exception {
		session.getDialect().writeCreateSequenceDDLStatement(this, schemaWriter);
		return schemaWriter;
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception {
		session.getDialect().writeDropSequenceDDLStatement(this, schemaWriter);
		return schemaWriter;
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws SchemaGeneratorException {
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws SchemaGeneratorException {
	}
}
