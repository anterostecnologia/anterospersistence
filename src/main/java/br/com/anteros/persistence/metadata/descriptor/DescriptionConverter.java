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
package br.com.anteros.persistence.metadata.descriptor;

import br.com.anteros.persistence.metadata.configuration.ConversionValueConfiguration;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;
import br.com.anteros.persistence.metadata.descriptor.type.DescriptionConverterType;

public class DescriptionConverter {

	private DescriptionConverterType converterType;

	private Class<AttributeConverter> attributeConverter;

	private String attributeName = "";

	private String name;

	private Class<?> dataType = void.class;

	private Class<?> objectType = void.class;

	private ConversionValueConfiguration[] conversionValues;

	private String defaultObjectValue = "";

	public DescriptionConverter(DescriptionConverterType converterType) {
		this.converterType = converterType;
	}

	public Class<AttributeConverter> getAttributeConverter() {
		return attributeConverter;
	}

	public DescriptionConverter attributeConverter(Class<AttributeConverter> attributeConverter) {
		this.attributeConverter = attributeConverter;
		return this;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public DescriptionConverter attributeName(String attributeName) {
		this.attributeName = attributeName;
		return this;
	}

	public DescriptionConverterType getConverterType() {
		return converterType;
	}

	public String getName() {
		return name;
	}

	public DescriptionConverter name(String name) {
		this.name = name;
		return this;
	}

	public Class<?> getDataType() {
		return dataType;
	}

	public DescriptionConverter dataType(Class<?> dataType) {
		this.dataType = dataType;
		return this;
	}

	public Class<?> getObjectType() {
		return objectType;
	}

	public DescriptionConverter objectType(Class<?> objectType) {
		this.objectType = objectType;
		return this;
	}

	public ConversionValueConfiguration[] getConversionValues() {
		return conversionValues;
	}

	public DescriptionConverter conversionValues(ConversionValueConfiguration[] conversionValues) {
		this.conversionValues = conversionValues;
		return this;
	}

	public String getDefaultObjectValue() {
		return defaultObjectValue;
	}

	public DescriptionConverter defaultObjectValue(String defaultObjectValue) {
		this.defaultObjectValue = defaultObjectValue;
		return this;
	}
	
	public boolean isCustomConverter(){
		return (converterType==DescriptionConverterType.CUSTOM_CONVERTER);
	}
	
	public boolean isObjectTypeConverter(){
		return (converterType==DescriptionConverterType.OBJECT_CONVERTER);
	}

	public boolean isTypeConverter(){
		return (converterType==DescriptionConverterType.TYPE_CONVERTER);
	}


}
