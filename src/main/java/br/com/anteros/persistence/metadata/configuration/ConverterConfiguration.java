package br.com.anteros.persistence.metadata.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;
import br.com.anteros.persistence.metadata.exception.EntityCacheManagerException;

public class ConverterConfiguration {
	private Class<?> converter;
	private boolean autoApply = false;
	private Class<?> entityAttributeType;
	private Class<?> databaseColumnType;

	public ConverterConfiguration(Class<?> converterClass) throws InstantiationException, IllegalAccessException {
		if (!ReflectionUtils.isImplementsInterface(converterClass, AttributeConverter.class))
			throw new EntityCacheManagerException("A classe " + converterClass.getName() + " não implementa a interface AttributeConverter.");
		this.converter = converterClass;

		final ParameterizedType attributeConverterSignature = extractAttributeConverterParameterizedType(converter);

		if (attributeConverterSignature.getActualTypeArguments().length < 2) {
			throw new EntityCacheManagerException("A classe AttributeConverter [" + converter.getName()
					+ "] não contém informação de tipos parametrizaada.");
		}

		if (attributeConverterSignature.getActualTypeArguments().length > 2) {
			throw new EntityCacheManagerException("A classe AttributeConverter [" + converter.getName() + "] possuí mais de 2 tipos parametrizados.");
		}

		Type type = attributeConverterSignature.getActualTypeArguments()[0];
		if (type instanceof Class) {
			this.entityAttributeType = (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			this.entityAttributeType = (Class<?>) ((ParameterizedType) type).getRawType();
		}

		if (entityAttributeType == null) {
			throw new EntityCacheManagerException("Não foi possível determinar o o tipo de atributo para a Entidade na classe AttributeConverter ["
					+ converter.getName() + "]");
		}

		databaseColumnType = (Class<?>) attributeConverterSignature.getActualTypeArguments()[1];
		if (databaseColumnType == null) {
			throw new EntityCacheManagerException(
					"Não foi possível determinar o tipo de atributo para a coluna do banco de dados na classe AttributeConverter ["
							+ converter.getName() + "]");
		}
	}

	public static ParameterizedType extractAttributeConverterParameterizedType(Class<?> converter) {
		for (Type type : converter.getGenericInterfaces()) {
			if (ParameterizedType.class.isInstance(type)) {
				final ParameterizedType parameterizedType = (ParameterizedType) type;
				if (AttributeConverter.class.equals(parameterizedType.getRawType())) {
					return parameterizedType;
				}
			}
		}

		throw new EntityCacheManagerException("Não foi possível extrair a representação de um tipo parametrizado da classe AttributeConverter ["
				+ converter.getName() + "]");
	}

	public boolean isAutoApply() {
		return autoApply;
	}

	public void loadAnnotations() {
		Annotation[] annotations = converter.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof Converter) {
				this.autoApply = ((Converter) annotation).autoApply();
				break;
			}
		}
	}

	public Class<?> getEntityAttributeType() {
		return entityAttributeType;
	}

	public Class<?> getDatabaseColumnType() {
		return databaseColumnType;
	}

	public ConverterConfiguration autoApply(boolean autoApply) {
		this.autoApply = autoApply;
		return this;
	}

	public Class<?> getConverter() {
		return converter;
	}
}
