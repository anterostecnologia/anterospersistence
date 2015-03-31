package br.com.anteros.persistence.session.query.transformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import br.com.anteros.core.utils.ArrayUtils;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.dsl.osql.types.ExpressionException;
import br.com.anteros.persistence.session.query.ResultSetTransformer;

public class ConstructorTransformer<T> implements ResultSetTransformer<T> {

	public static <D> ConstructorTransformer<D> create(Class<D> type, Class<?>... paramTypes) {
		return new ConstructorTransformer<D>(type, paramTypes);
	}

	public static <D> ConstructorTransformer<D> create(Class<D> type) {
		return new ConstructorTransformer<D>(type);
	}

	private Class<?>[] parameterTypes;
	private transient Constructor<?> constructor;

	private Class<T> type;

	public ConstructorTransformer(Class<T> type, Class<?>... paramTypes) {
		this.type = type;
		this.parameterTypes = paramTypes;
	}

	public ConstructorTransformer(Class<T> type) {
		this.type = type;
	}

	public final Class<?>[] getParametersTypes() {
		return parameterTypes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T newInstance(Object... args) {
		try {
			if ((constructor == null) || (parameterTypes == null)) {
				if (parameterTypes == null) {
					if (args == null) {
						args = ArrayUtils.EMPTY_OBJECT_ARRAY;
					}
					Class<?> parameterTypes[] = new Class[args.length];
					for (int i = 0; i < args.length; i++) {
						parameterTypes[i] = args[i].getClass();
					}
					constructor = ReflectionUtils.getAccessibleConstructor(getType(), parameterTypes);
				} else {
					constructor = ReflectionUtils.getAccessibleConstructor(getType(), parameterTypes);
				}
			}
			return (T) constructor.newInstance(args);
		} catch (SecurityException e) {
			throw new ExpressionException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new ExpressionException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new ExpressionException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new ExpressionException(e.getMessage(), e);
		}
	}

	public Class<T> getType() {
		return type;
	}

}
