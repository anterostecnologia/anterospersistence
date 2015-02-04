package br.com.anteros.persistence.metadata.configuration;

import br.com.anteros.persistence.metadata.annotation.Convert;

public class ConvertConfiguration {
	private Class<?> converter;
	private String attributeName;
	
	public ConvertConfiguration(Convert convert) {
		this.attributeName(convert.attributeName()).converter(convert.converter());
	}
	
	public ConvertConfiguration(Class<?> converter, String attributeName){
		this.converter = converter;
		this.attributeName = attributeName;		
	}
	
	public ConvertConfiguration(Class<?> converter){
		this.converter = converter;
		this.attributeName = "";
	}

	public Class<?> getConverter() {
		return converter;
	}

	public ConvertConfiguration converter(Class<?> converter) {
		this.converter = converter;
		return this;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public ConvertConfiguration attributeName(String attributeName) {
		this.attributeName = attributeName;
		return this;
	}
	
	
	
}
