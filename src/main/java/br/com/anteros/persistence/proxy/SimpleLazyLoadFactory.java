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

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;
import br.com.anteros.persistence.proxy.collection.SQLLazyLoadList;
import br.com.anteros.persistence.proxy.collection.SQLLazyLoadMap;
import br.com.anteros.persistence.proxy.collection.SQLLazyLoadSet;
import br.com.anteros.persistence.proxy.lob.BlobLazyLoadProxy;
import br.com.anteros.persistence.proxy.lob.ClobLazyLoadProxy;
import br.com.anteros.persistence.proxy.lob.NClobLazyLoadProxy;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.LockOptions;

public class SimpleLazyLoadFactory implements LazyLoadFactory {

	@Override
	public Object createProxy(SQLSession session, Object targetObject, DescriptionField descriptionField,
			EntityCache targetEntityCache, Map<String, Object> columnKeyValues, Cache transactionCache, LockOptions lockOptions)
			throws Exception {
		Object newObject = null;
		if (descriptionField.isLob()) {
			if (descriptionField.getFieldClass().equals(java.sql.Blob.class)) {
				return BlobLazyLoadProxy.createProxy(session, targetObject, targetEntityCache,columnKeyValues, descriptionField);
			} else if (descriptionField.getFieldClass().equals(java.sql.Clob.class)) {
				return ClobLazyLoadProxy.createProxy(session, targetObject, targetEntityCache,columnKeyValues, descriptionField);
			} else if (descriptionField.getFieldClass().equals(java.sql.NClob.class)) {
				return NClobLazyLoadProxy.createProxy(session, targetObject, targetEntityCache,columnKeyValues, descriptionField);
			}
			throw new ProxyCreationException("Não é possível criar proxy para o tipo "
					+ descriptionField.getFieldClass() + " da classe "
					+ targetEntityCache.getEntityClass().getSimpleName());
		} else {
			SimpleLazyLoadInterceptor lazyLoadInterceptor = new SimpleLazyLoadInterceptor(session, targetEntityCache,
					columnKeyValues, transactionCache, targetObject, descriptionField, lockOptions);
			if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class))
				newObject = new SQLLazyLoadSet(lazyLoadInterceptor);
			else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class))
				newObject = new SQLLazyLoadList(lazyLoadInterceptor);
			else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Map.class))
				newObject = new SQLLazyLoadMap(session, targetEntityCache, descriptionField);
		}
		return newObject;
	}

	@Override
	public boolean proxyIsInitialized(Object object) throws Exception {
		if (object instanceof AnterosProxyObject) {
			return ((AnterosProxyObject)object).isInitialized();
		} else if (object instanceof AnterosPersistentCollection) {
			return ((AnterosPersistentCollection) object).isInitialized();
		}
		return false;
	}

	@Override
	public boolean isProxyObject(Object object) throws Exception {
		if (object instanceof AnterosProxyObject)
			return true;

		if (object instanceof AnterosPersistentCollection)
			return ((AnterosPersistentCollection) object).isProxied();
		return false;
	}

}
