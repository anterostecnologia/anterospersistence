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
package br.com.anteros.persistence.session.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLCache implements Cache {
	private Map<Object, Object[]> cache;
	private Queue queue;
	private int maxSize;
	private AtomicInteger size = new AtomicInteger();

	public SQLCache(int maxSize) {
		this.maxSize = maxSize;
		cache = new ConcurrentHashMap(maxSize);
		queue = new ConcurrentLinkedQueue();
	}

	public void put(Object key, Object value) {
		put(key, value, 0);
	}

	public void put(Object key, Object val, Integer seconds_to_store) {
		if (seconds_to_store == 0)
			seconds_to_store = null;

		if (key == null)
			throw new RuntimeException("Chave não pode ser nula!");
		seconds_to_store = seconds_to_store != null ? seconds_to_store
				: 99999999;
		cache.put(key, new Object[] {
				System.currentTimeMillis() + seconds_to_store, val });
		queue.add(key);
		size.incrementAndGet();

		while (size.get() > maxSize && maxSize > 0) {
			Object toRemove = queue.poll();
			if (toRemove == null)
				break;
			if (toRemove != null) {
				remove(key);
			}
		}
	}

	public Object get(Object key) {
		if (cache.containsKey(key)) {
			Long expires = (Long) cache.get(key)[0];
			if (expires - System.currentTimeMillis() > 0) {
				return cache.get(key)[1];
			} else {
				remove(key);
			}
		}
		return null;
	}

	public boolean remove(Object key) {
		return removeAndGet(key) != null;
	}

	public Object removeAndGet(Object key) {
		Object[] entry = cache.remove(key);
		if (entry != null) {
			return entry[1];
		}
		size.decrementAndGet();
		return null;
	}

	public int size() {
		return size.get();
	}

	public Map getAll(Collection collection) {
		Map ret = new HashMap();
		for (Object o : collection)
			ret.put(o, cache.get(o));
		return ret;
	}

	public void clear() {
		cache.clear();
		size.set(0);
	}

	public int mapSize() {
		return cache.size();
	}

	public int queueSize() {
		return queue.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("size=" + size + ", cache={");
		for (Object[] obj : cache.values()) {
			sb.append(obj[1]);
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}
}
