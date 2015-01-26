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
package br.com.anteros.persistence.metadata.configuration;

import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;

public class ConverterConfiguration {

	private Class<AttributeConverter> attributeConverter;

	private String attributeName = "";

	public ConverterConfiguration() {

	}
	
	public ConverterConfiguration(Class<AttributeConverter> attributeConverter, String attributeName) {
		this.attributeConverter = attributeConverter;
		this.attributeName = attributeName;
	}

	public ConverterConfiguration(Converter converter) {
		this.attributeConverter = converter.attributeConverter();
		this.attributeName = converter.attributeName();
	}

	public Class<AttributeConverter> getAttributeConverter() {
		return attributeConverter;
	}

	public ConverterConfiguration attributeConverter(Class<AttributeConverter> attributeConverter) {
		this.attributeConverter = attributeConverter;
		return this;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public ConverterConfiguration attributeName(String attributeName) {
		this.attributeName = attributeName;
		return this;
	}
}
