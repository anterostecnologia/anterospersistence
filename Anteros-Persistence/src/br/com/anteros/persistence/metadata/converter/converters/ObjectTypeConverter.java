/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.metadata.converter.converters;

import br.com.anteros.persistence.metadata.converter.AttributeConverter;
import br.com.anteros.persistence.session.SQLSession;

public class ObjectTypeConverter implements AttributeConverter<Object, Object> {

	@Override
	public Object convertToDatabaseColumn(Object attribute, SQLSession session) {
		return null;
	}

	@Override
	public Object convertToEntityAttribute(Object dbData, SQLSession session) {
		return null;
	}

	
}
