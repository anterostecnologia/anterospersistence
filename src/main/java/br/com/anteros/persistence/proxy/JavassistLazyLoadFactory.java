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
package br.com.anteros.persistence.proxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;
import br.com.anteros.persistence.proxy.collection.ProxiedSQLLazyLoadList;
import br.com.anteros.persistence.proxy.collection.ProxiedSQLLazyLoadSet;
import br.com.anteros.persistence.proxy.lob.BlobLazyLoadProxy;
import br.com.anteros.persistence.proxy.lob.ClobLazyLoadProxy;
import br.com.anteros.persistence.proxy.lob.NClobLazyLoadProxy;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.LockOptions;

public class JavassistLazyLoadFactory implements LazyLoadFactory {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(LazyLoadFactory.class);

	public Object createProxy(SQLSession session, Object targetObject, DescriptionField descriptionField, EntityCache targetEntityCache,
			Map<String, Object> columnKeyValues, Cache transactionCache, LockOptions lockOptions) throws Exception {

		LOG.debug("Criando proxy para objeto " + targetObject + " usando session " + session);
		Object newObject = null;
		if (descriptionField.isLob()) {
			if (descriptionField.getFieldClass().equals(java.sql.Blob.class)) {
				return BlobLazyLoadProxy.createProxy(session, targetObject, targetEntityCache, columnKeyValues, descriptionField);
			} else if (descriptionField.getFieldClass().equals(java.sql.Clob.class)) {
				return ClobLazyLoadProxy.createProxy(session, targetObject, targetEntityCache, columnKeyValues, descriptionField);
			} else if (descriptionField.getFieldClass().equals(java.sql.NClob.class)) {
				return NClobLazyLoadProxy.createProxy(session, targetObject, targetEntityCache, columnKeyValues, descriptionField);
			}
			throw new ProxyCreationException("Não é possível criar proxy para o tipo "
					+ (descriptionField.getFieldClass() == byte[].class ? "byte[]" : descriptionField.getFieldClass()) + " do campo "
					+ descriptionField.getField().getName() + " da classe " + targetEntityCache.getEntityClass().getSimpleName());
		} else {
			ProxyFactory factory = new ProxyFactory();
			factory.setInterfaces(new Class[] { AnterosProxyObject.class });
			if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class))
				factory.setSuperclass(ProxiedSQLLazyLoadSet.class);
			else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class))
				factory.setSuperclass(ProxiedSQLLazyLoadList.class);
			else
				factory.setSuperclass(descriptionField.getField().getType());
			factory.setFilter(FINALIZE_FILTER);
			Class<?> classNewObject = factory.createClass();
			newObject = classNewObject.newInstance();
			JavassistLazyLoadInterceptor lazyLoadInterceptor = new JavassistLazyLoadInterceptor(session, targetEntityCache, columnKeyValues,
					transactionCache, targetObject, descriptionField, lockOptions);
			((ProxyObject) newObject).setHandler(lazyLoadInterceptor);
			lazyLoadInterceptor.setConstructed(true);
		}
		LOG.debug("Proxy criado");
		return newObject;
	}

	private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
		public boolean isHandled(Method m) {
			return !(m.getParameterTypes().length == 0 && m.getName().equals("finalize"));
		}
	};

	public boolean proxyIsInitialized(Object object) throws Exception {
		if (object instanceof AnterosProxyObject) {
			return ((AnterosProxyObject) object).isInitialized();
		} else if (object instanceof AnterosPersistentCollection) {
			return ((AnterosPersistentCollection) object).isInitialized();
		}
		return false;
	}

	public boolean isProxyObject(Object object) throws Exception {
		if (object instanceof AnterosProxyObject)
			return true;

		if (object instanceof AnterosPersistentCollection)
			return ((AnterosPersistentCollection) object).isProxied();
		return false;
	}
}
