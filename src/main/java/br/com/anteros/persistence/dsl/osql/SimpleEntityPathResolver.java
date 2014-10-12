package br.com.anteros.persistence.dsl.osql;
	
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import br.com.anteros.core.utils.ClassUtils;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;

public enum SimpleEntityPathResolver implements EntityPathResolver {

	INSTANCE;

	private static final String NO_CLASS_FOUND_TEMPLATE = "Did not find a query class %s for domain class %s!";
	private static final String NO_FIELD_FOUND_TEMPLATE = "Did not find a static field of the same type in %s!";

	@SuppressWarnings("unchecked")
	public <T> EntityPath<T> createPath(Class<T> domainClass) {

		String pathClassName = getQueryClassName(domainClass);

		try {
			Class<?> pathClass = Class.forName(pathClassName, false, domainClass.getClassLoader());
			Field field = getStaticFieldOfType(pathClass);

			if (field == null) {
				throw new IllegalStateException(String.format(NO_FIELD_FOUND_TEMPLATE, pathClass));
			} else {
				return (EntityPath<T>) ReflectionUtils.getField(field, null);
			}

		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.format(NO_CLASS_FOUND_TEMPLATE, pathClassName, domainClass.getName()),
					e);
		}
	}

	private Field getStaticFieldOfType(Class<?> type) {

		for (Field field : type.getDeclaredFields()) {

			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean hasSameType = type.equals(field.getType());

			if (isStatic && hasSameType) {
				return field;
			}
		}

		Class<?> superclass = type.getSuperclass();
		return Object.class.equals(superclass) ? null : getStaticFieldOfType(superclass);
	}

	private String getQueryClassName(Class<?> domainClass) {

		String simpleClassName = ClassUtils.getShortName(domainClass);
		return String.format("%s.T%s%s", domainClass.getPackage().getName(), getClassBase(simpleClassName),
				domainClass.getSimpleName());
	}

	private String getClassBase(String shortName) {

		String[] parts = shortName.split("\\.");

		if (parts.length < 2) {
			return "";
		}

		return parts[0] + "_";
	}
}