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
package br.com.anteros.persistence.session.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceMetadataCache implements Cache {

	private static PersistenceMetadataCache metadataCache;

	public static PersistenceMetadataCache getInstance() {
		synchronized (PersistenceMetadataCache.class) {
			if (metadataCache == null)
				metadataCache = new PersistenceMetadataCache();
			return metadataCache;
		}
	}

	private Map<Object, Object> cache;

	public PersistenceMetadataCache() {
		cache = new ConcurrentHashMap<Object, Object>();
	}

	public void put(Object key, Object value) {

		put(key, value, 0);
	}

	public void put(Object key, Object val, Integer seconds_to_store) {
		cache.put(key, val);
	}

	public Object get(Object key) {
		return cache.get(key);
	}

	public boolean remove(Object key) {
		return removeAndGet(key) != null;
	}

	public Object removeAndGet(Object key) {
		return cache.remove(key);
	}

	public int size() {
		return cache.size();
	}

	public void clear() {
		cache.clear();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("size=" + size() + ", cache={");
		for (Object obj : cache.values()) {
			sb.append(obj);
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}
}