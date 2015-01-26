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

import br.com.anteros.persistence.metadata.annotation.ConversionValue;
import br.com.anteros.persistence.metadata.annotation.ObjectTypeConverter;

public class ObjectTypeConverterConfiguration {

	private String name;

	private Class<?> dataType = void.class;

	private Class<?> objectType = void.class;

	private ConversionValueConfiguration[] conversionValues;

	private String defaultObjectValue = "";

	public ObjectTypeConverterConfiguration() {

	}

	public ObjectTypeConverterConfiguration(ObjectTypeConverter objectTypeConverter) {
		this.name = objectTypeConverter.name();
		this.dataType = objectTypeConverter.dataType();
		this.objectType = objectTypeConverter.objectType();
		this.conversionValues = new ConversionValueConfiguration[objectTypeConverter.conversionValues().length];
		int i = 0;
		for (ConversionValue conversionValue : objectTypeConverter.conversionValues()) {
			this.conversionValues[i] = new ConversionValueConfiguration(conversionValue);
			i++;
		}
		this.defaultObjectValue = objectTypeConverter.defaultObjectValue();
	}

	public ObjectTypeConverterConfiguration(String name) {
		this.name = name;
	}

	public ObjectTypeConverterConfiguration(String name, Class<?> dataType, Class<?> objectType, ConversionValueConfiguration[] conversionValues,
			String defaultObjectValue) {
		this.name = name;
		this.dataType = dataType;
		this.objectType = objectType;
		this.conversionValues = conversionValues;
		this.defaultObjectValue = defaultObjectValue;
	}

	public String getName() {
		return name;
	}

	public ObjectTypeConverterConfiguration name(String name) {
		this.name = name;
		return this;
	}

	public Class<?> getDataType() {
		return dataType;
	}

	public ObjectTypeConverterConfiguration dataType(Class<?> dataType) {
		this.dataType = dataType;
		return this;
	}

	public Class<?> getObjectType() {
		return objectType;
	}

	public ObjectTypeConverterConfiguration objectType(Class<?> objectType) {
		this.objectType = objectType;
		return this;
	}

	public ConversionValueConfiguration[] getConversionValues() {
		return conversionValues;
	}

	public ObjectTypeConverterConfiguration conversionValues(ConversionValueConfiguration[] conversionValues) {
		this.conversionValues = conversionValues;
		return this;
	}

	public String getDefaultObjectValue() {
		return defaultObjectValue;
	}

	public ObjectTypeConverterConfiguration defaultObjectValue(String defaultObjectValue) {
		this.defaultObjectValue = defaultObjectValue;
		return this;
	}
}
