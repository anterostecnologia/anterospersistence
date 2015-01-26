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

public class IndexSchema extends ConstraintSchema {
	protected boolean unique;

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws Exception {
		return session.getDialect().writeCreateIndexDDLStatement(this, schemaWriter);
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception {
		return session.getDialect().writeDropIndexDDLStatement(this, schemaWriter);
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws SchemaGeneratorException {
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws SchemaGeneratorException {
	}

	public boolean isUnique() {
		return unique;
	}

	public IndexSchema setUnique(boolean unique) {
		this.unique = unique;
		return this;
	}
	
	public String getColumnsToString(){
		String result = "";
		boolean appendDelimiter = false;
		for (ColumnSchema columnSchema : columns){
			if (appendDelimiter)
				result += "_";
			result += columnSchema.getName();
			appendDelimiter = true;
		}
		
		return result;
	}
	
	public List<String> getColumnNames(){
		List<String> result = new ArrayList<String>();
		for (ColumnSchema columnSchema : getColumns()){
			result.add(columnSchema.name);
		}
		return result;
	}
}
