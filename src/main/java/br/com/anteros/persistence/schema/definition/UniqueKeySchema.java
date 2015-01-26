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


public class UniqueKeySchema  extends IndexSchema {

	public UniqueKeySchema() {
        this.name = "";
    }

    public UniqueKeySchema(String name, ColumnSchema column) {
        this();
        this.name = name;
        getColumns().add(column);
    }

    public UniqueKeySchema(String name, ColumnSchema[] columns) {
        this();
        this.name = name;
        for(ColumnSchema column : columns) {
            this.getColumns().add(column);
        }
    }    

	@Override
	public Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws Exception {
		return session.getDialect().writeAddUniqueKeyDDLStatement(this, schemaWriter);
	}

	@Override
	public Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception {
		return session.getDialect().writeDropUniqueKeyDDLStatement(this, schemaWriter);
	}

	@Override
	public void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws SchemaGeneratorException {
	}

	@Override
	public void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws SchemaGeneratorException {
	}

	public void appendConstraintDDL(Writer schemaWriter, SQLSession session) {
		
		
	}
	
	public List<String> getColumnNames(){
		List<String> result = new ArrayList<String>();
		for (ColumnSchema columnSchema : getColumns()){
			result.add(columnSchema.name);
		}
		return result;
	}

    
}
