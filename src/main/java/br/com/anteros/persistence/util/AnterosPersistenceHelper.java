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
package br.com.anteros.persistence.util;

import java.lang.reflect.Field;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.proxy.JavassistLazyLoadInterceptor;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;

public class AnterosPersistenceHelper {
	private static Boolean androidPresent = null;

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
			if (!(handler instanceof JavassistLazyLoadInterceptor))
				return true;

			return ((JavassistLazyLoadInterceptor) (handler)).isInitialized();

		} else if (value instanceof AnterosPersistentCollection) {
			AnterosPersistentCollection coll = (AnterosPersistentCollection) value;
			return coll.isInitialized();
		}
		return true;
	}

	public static boolean androidIsPresent() {
		if (androidPresent == null) {
			try {
				Class.forName("br.com.anteros.android.persistence.session.AndroidSQLSession");
				androidPresent = true;
			} catch (ClassNotFoundException e) {
				androidPresent = false;
			}
		}
		return androidPresent;
	}
}
