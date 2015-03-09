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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.com.anteros.persistence.schema.definition.type.StoredParameterType;
import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class StoredProcedureSchema extends ObjectSchema {

	protected final Set<StoredParameterSchema> parameters = new LinkedHashSet<StoredParameterSchema>();
	protected final List<String> statements = new ArrayList<String>();
	protected final Set<ColumnSchema> variables = new LinkedHashSet<ColumnSchema>();

	public Set<StoredParameterSchema> getParameters() {
		return parameters;
	}

	public List<String> getStatements() {
		return statements;
	}

	public Set<ColumnSchema> getVariables() {
		return variables;
	}

	public StoredProcedureSchema() {
	}

	public void addParameter(String parameterName, Class<?> type) {
		addParameter(new StoredParameterSchema(parameterName, type, StoredParameterType.IN));
	}

	public void addParameter(String parameterName, Class<?> type, int size) {
		addParameter(new StoredParameterSchema(parameterName, type, size, StoredParameterType.IN));
	}

	public void addParameter(String parameterName, String typeName) {
		addParameter(new StoredParameterSchema(parameterName, typeName, StoredParameterType.IN));
	}

	public void addParameter(StoredParameterSchema parameter) {
		getParameters().add(parameter);
	}

	public void addInOutputArgument(String parameterName, Class<?> type) {
		addInOutputParameter(new StoredParameterSchema(parameterName, type, StoredParameterType.IN));
	}

	public void addInOutputParameter(StoredParameterSchema parameter) {
		getParameters().add(parameter);
	}

	public void addOutputParameter(String parameterName, Class<?> type) {
		addOutputParameter(new StoredParameterSchema(parameterName, type, StoredParameterType.OUT));
	}

	public void addOutputParameter(String parameterName, Class<?> type, int size) {
		addOutputParameter(new StoredParameterSchema(parameterName, type, size, StoredParameterType.OUT));
	}

	public void addOutputParameter(String parameterName, String typeName) {
		addOutputParameter(new StoredParameterSchema(parameterName, typeName, StoredParameterType.OUT));
	}

	public void addOutputParameter(StoredParameterSchema argument) {
		getParameters().add(argument);
	}

	public void addStatement(String statement) {
		getStatements().add(statement);
	}

	public void addVariable(String variableName, String typeName) {
		this.addVariable(new StoredVariableSchema(variableName, typeName));
	}

	public void addVariable(StoredVariableSchema variable) {
		getVariables().add(variable);
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
