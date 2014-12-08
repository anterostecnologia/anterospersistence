package br.com.anteros.persistence.session.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistenceMetadataCache implements Cache {
	
	    private static PersistenceMetadataCache metadataCache;
	    
	    public static PersistenceMetadataCache getInstance(){
	    	if (metadataCache==null)
	    		metadataCache = new PersistenceMetadataCache(10000);
	    	return metadataCache;
	    }

		private Map<Object, Object[]> cache;
		private Queue queue;
		private int maxSize;
		private AtomicInteger size = new AtomicInteger();

		private PersistenceMetadataCache(int maxSize) {
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
