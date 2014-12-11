package br.com.anteros.persistence.metadata.accessor;

import java.lang.reflect.Field;
import java.util.Map;

public interface PropertyAccessorFactory {

	public Map<String, PropertyAccessor> createAccessors(Class<?> clazz) throws Exception;

	public PropertyAccessor createAccessor(Class<?> clazz, Field field) throws Exception;
}
