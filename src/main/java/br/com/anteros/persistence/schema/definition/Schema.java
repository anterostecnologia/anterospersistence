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
import java.util.List;

import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.session.SQLSession;

public class Schema extends ObjectSchema {

	protected final List<TableSchema> tables = new ArrayList<TableSchema>();
	protected final List<SequenceGeneratorSchema> sequences = new ArrayList<SequenceGeneratorSchema>();
	protected final List<StoredProcedureSchema> procedures = new ArrayList<StoredProcedureSchema>();
	protected final List<StoredFunctionSchema> functions = new ArrayList<StoredFunctionSchema>();
	protected final List<PackageSchema> packages = new ArrayList<PackageSchema>();
	protected final List<ViewSchema> views = new ArrayList<ViewSchema>();

	public List<TableSchema> getTables() {
		return tables;
	}

	public List<SequenceGeneratorSchema> getSequences() {
		return sequences;
	}

	public List<StoredProcedureSchema> getProcedures() {
		return procedures;
	}

	public List<StoredFunctionSchema> getFunctions() {
		return functions;
	}

	public List<PackageSchema> getPackages() {
		return packages;
	}

	public List<ViewSchema> getViews() {
		return views;
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
