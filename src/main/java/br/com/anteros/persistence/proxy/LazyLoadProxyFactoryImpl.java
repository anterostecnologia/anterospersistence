/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.proxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.proxy.collection.SQLArrayList;
import br.com.anteros.persistence.proxy.collection.SQLHashSet;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

public class LazyLoadProxyFactoryImpl implements LazyLoadProxyFactory {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(LazyLoadProxyFactory.class); 
	
	public Object createProxy(SQLSession session, Object targetObject, DescriptionField descriptionField, EntityCache targetEntityCache,
			Map<String, Object> columnKeyValues, Cache transactionCache) throws Exception {
		
		LOG.debug("Criando proxy para objeto "+targetObject+" usando session "+session);
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(new Class[]{AnterosProxyObject.class});
		if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class))
			factory.setSuperclass(SQLHashSet.class);
		else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class))
			factory.setSuperclass(SQLArrayList.class);
		else
			factory.setSuperclass(descriptionField.getField().getType());
		factory.setFilter(FINALIZE_FILTER);
		Class<?> classNewObject = factory.createClass();
		Object newObject = classNewObject.newInstance();
		LazyLoadInterceptor lazyLoadInterceptor = new LazyLoadInterceptor(session, targetEntityCache, columnKeyValues, transactionCache,
				targetObject, descriptionField);
		((ProxyObject) newObject).setHandler(lazyLoadInterceptor);
		lazyLoadInterceptor.setConstructed(true);
		LOG.debug("Proxy criado");
		return newObject;
	}

	private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
		public boolean isHandled(Method m) {
			return !(m.getParameterTypes().length == 0 && m.getName().equals("finalize"));
		}
	};

	
	public boolean proxyIsInitialized(Object object) throws Exception {
		if (object instanceof ProxyObject) {
			MethodHandler mh = ((ProxyObject) object).getHandler();
			return ((LazyLoadInterceptor) mh).isInitialized();
		}
		return false;
	}

	
	public boolean isProxyObject(Object object) throws Exception {
		return (object instanceof AnterosProxyObject);
	}
}
