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

import br.com.anteros.persistence.schema.definition.type.StoredParameterType;
import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class StoredParameterSchema extends ColumnSchema {

	protected StoredParameterType parameterType;

	public StoredParameterSchema() {
		super();
	}

	public StoredParameterSchema(String name, Class<?> type, StoredParameterType parameterType) {
		super(name, type);
		this.parameterType = parameterType;
	}

	public StoredParameterSchema(String name, Class<?> type, int lenght, StoredParameterType parameterType) {
		super(name, type, lenght);
		this.parameterType = parameterType;
	}

	public StoredParameterSchema(String name, Class<?> type, int lenght, int precision, StoredParameterType parameterType) {
		super(name, type, lenght, precision);
		this.parameterType = parameterType;
	}

	public StoredParameterSchema(String name, String typeName, StoredParameterType parameterType) {
		super(name, typeName);
		this.parameterType = parameterType;
	}

	public StoredParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(StoredParameterType parameterType) {
		this.parameterType = parameterType;
	}
	

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws SchemaGeneratorException {
		return schemaWriter;
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws SchemaGeneratorException {
		return schemaWriter;
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws SchemaGeneratorException {
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws SchemaGeneratorException {
	}

}
