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

import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.LockOptions;

public interface LazyLoadFactory {

	public Object createProxy(SQLSession session, Object targetObject, DescriptionField descriptionField, EntityCache targetEntityCache,
			Map<String, Object> columnKeyValues, Cache transactionCache, LockOptions lockOptions) throws Exception;
	
	public boolean proxyIsInitialized(Object object) throws Exception;
	
	public boolean isProxyObject(Object object) throws Exception;
	
	
}
