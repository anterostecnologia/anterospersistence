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
package br.com.anteros.persistence.metadata.identifier;

import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionGenerator;
import br.com.anteros.persistence.session.SQLSession;

public class IdentifierGeneratorFactory {

	public static IdentifierGenerator createGenerator(SQLSession session, DescriptionColumn column) throws Exception {
		DescriptionGenerator generator = null;
		GeneratedType type;

		if (column.getGeneratedType().equals(GeneratedType.AUTO)) {
			if (session.getDialect().supportsSequences()) {
				generator = column.getGenerators().get(GeneratedType.SEQUENCE);
				type = GeneratedType.SEQUENCE;
			} else if (session.getDialect().supportsIdentity()) {
				generator = column.getGenerators().get(GeneratedType.IDENTITY);
				type = GeneratedType.IDENTITY;
			} else {
				generator = column.getGenerators().get(GeneratedType.TABLE);
				type = GeneratedType.TABLE;
			}
		} else {
			if (session.getDialect().supportsSequences()) {
				generator = column.getGenerators().get(column.getGeneratedType());
				type = column.getGeneratedType();
			} else {
				generator = column.getGenerators().get(GeneratedType.TABLE);
				type = GeneratedType.TABLE;
			}
		}

		if (type.equals(GeneratedType.IDENTITY)) {
			return new IdentifyGenerator(column.getField().getType());
		} else if (type.equals(GeneratedType.SEQUENCE)) {
			return new SequenceGenerator(session, generator.getCatalog(), generator.getSchema(), generator.getSequenceName(), null, column.getField()
					.getType(), generator.getInitialValue());
		} else if (type.equals(GeneratedType.TABLE)) {
			return new TableGenerator(session, generator.getTableName(), generator.getPkColumnName(), generator.getValueColumnName(),
					generator.getValue(), column.getField().getType(), null, generator.getCatalog(), generator.getSchema(),
					generator.getInitialValue());
		}
		return null;
	}
}
