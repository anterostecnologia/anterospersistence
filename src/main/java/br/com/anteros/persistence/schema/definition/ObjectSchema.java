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

import java.io.StringWriter;
import java.io.Writer;

import br.com.anteros.persistence.session.SQLSession;

public abstract class ObjectSchema {

	protected String name;

	public String getName() {
		return name;
	}

	public ObjectSchema setName(String name) {
		this.name = name;
		return this;
	}


	public abstract Writer generateDDLCreateObject(SQLSession session, Writer schemaWriter) throws Exception;

	public abstract Writer generateDDLDropObject(SQLSession session, Writer schemaWriter) throws Exception;

	public abstract void afterCreateObject(SQLSession session, Writer createSchemaWriter) throws Exception;

	public abstract void beforeDropObject(SQLSession session, Writer dropSchemaWriter) throws Exception;

	public void createObject(SQLSession session, Writer schemaWriter) throws Exception {
		if (schemaWriter == null) {
			this.createOnDatabase(session);
		} else {
			this.generateDDLCreateObject(session, schemaWriter);
		}
	}

	public void createOnDatabase(SQLSession session) throws Exception {
		String ddl = generateDDLCreateObject(session, new StringWriter()).toString();
		session.executeDDL(ddl);
	}

	public void dropObject(SQLSession session, Writer schemaWriter) throws Exception {
		if (schemaWriter == null) {
			this.dropFromDatabase(session);
		} else {
			this.generateDDLDropObject(session, schemaWriter);
		}
	}

	public void dropFromDatabase(SQLSession session) throws Exception {
		String ddl = generateDDLDropObject(session, new StringWriter()).toString();
		session.executeDDL(ddl);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectSchema other = (ObjectSchema) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	

	@Override
	public String toString() {
		return name;
	}
	
	
}
