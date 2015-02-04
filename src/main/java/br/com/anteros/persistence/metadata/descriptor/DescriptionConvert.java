/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.metadata.descriptor;

import br.com.anteros.persistence.metadata.converter.AttributeConverter;

public class DescriptionConvert {

	private AttributeConverter<?, ?> converter;
	private String attributeName;
	private final Class<?> entityAttributeType;
	private final Class<?> databaseColumnType;

	public DescriptionConvert(AttributeConverter<?, ?> converter, String attributeName, Class<?> entityAttributeType, Class<?> databaseColumnType) {
		this.converter = converter;
		this.attributeName = attributeName;
		this.entityAttributeType = entityAttributeType;
		this.databaseColumnType = databaseColumnType;
	}	

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public Class<?> getEntityAttributeType() {
		return entityAttributeType;
	}

	public Class<?> getDatabaseColumnType() {
		return databaseColumnType;
	}

	public AttributeConverter<?, ?> getConverter() {
		return converter;
	}

	public void setConverter(AttributeConverter<?, ?> converter) {
		this.converter = converter;
	}

}
