package br.com.anteros.persistence.util;

import java.lang.reflect.Field;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.proxy.ProxyLazyLoadInterceptor;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;

public class AnterosPersistenceHelper {

	private AnterosPersistenceHelper() {

	}

	public static boolean isLoaded(Object proxy, String property) {
		if (proxy == null)
			return true;
		Field field = ReflectionUtils.getFieldByName(proxy.getClass(), property);
		Object value;
		try {
			value = ReflectionUtils.getFieldValue(proxy, field);
		} catch (Exception e) {
			return true;
		}
		if (value == null)
			return true;

		if (AnterosProxyObject.class.isAssignableFrom(value.getClass())) {
			MethodHandler handler = ((ProxyObject) value).getHandler();
			if (!(handler instanceof ProxyLazyLoadInterceptor))
				return true;

			return ((ProxyLazyLoadInterceptor) (handler)).isInitialized();

		} else if (value instanceof AnterosPersistentCollection) {
			AnterosPersistentCollection coll = (AnterosPersistentCollection) value;
			return coll.isInitialized();
		}
		return true;
	}
}
