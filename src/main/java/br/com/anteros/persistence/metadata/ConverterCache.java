package br.com.anteros.persistence.metadata;

import br.com.anteros.persistence.metadata.converter.AttributeConverter;

public class ConverterCache {

	private AttributeConverter<?, ?> converter;
	private boolean autoApply = false;
	private final Class<?> entityAttributeType;
	private final Class<?> databaseColumnType;

	public ConverterCache(AttributeConverter<?, ?> converter, Class<?> entityAttributeType, Class<?> databaseColumnType) {
		this.converter = converter;
		this.entityAttributeType = entityAttributeType;
		this.databaseColumnType = databaseColumnType;
	}

	public ConverterCache(AttributeConverter<?, ?> converter, Class<?> entityAttributeType, Class<?> databaseColumnType, boolean autoApply) {
		this.converter = converter;
		this.autoApply = autoApply;
		this.entityAttributeType = entityAttributeType;
		this.databaseColumnType = databaseColumnType;
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

	public boolean isAutoApply() {
		return autoApply;
	}

	public void setAutoApply(boolean autoApply) {
		this.autoApply = autoApply;
	}

}
