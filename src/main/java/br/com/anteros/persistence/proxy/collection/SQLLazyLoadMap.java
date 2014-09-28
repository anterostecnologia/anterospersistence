package br.com.anteros.persistence.proxy.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSession;

public class SQLLazyLoadMap<K, V> extends AbstractSQLMap<K, V> {

	private DescriptionField descriptionField;
	private EntityCache entityCache;
	private SQLSession session;
	private boolean initialized = false;

	public SQLLazyLoadMap(SQLSession session, EntityCache entityCache, DescriptionField descriptionField) {
		this.session = session;
		this.entityCache = entityCache;
		this.descriptionField = descriptionField;
	}

	@Override
	public void clear() {
		initialize();
		super.clear();
	}

	@Override
	public Object clone() {
		initialize();
		return super.clone();
	}

	@Override
	public boolean containsKey(Object key) {
		initialize();
		return super.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		initialize();
		return super.containsValue(value);
	}

	@Override
	public Set entrySet() {
		initialize();
		return super.entrySet();
	}

	@Override
	public boolean equals(Object object) {
		initialize();
		return super.equals(object);
	}

	@Override
	public V get(Object key) {
		initialize();
		return super.get(key);
	}

	@Override
	public boolean isEmpty() {
		initialize();
		return super.isEmpty();
	}

	@Override
	public V put(K key, V value) {
		initialize();
		return super.put(key, value);
	}

	@Override
	public void putAll(Map map) {
		initialize();
		super.putAll(map);
	}

	@Override
	public Set keySet() {
		initialize();
		return super.keySet();
	}

	@Override
	public V remove(Object key) {
		initialize();
		return super.remove(key);
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		initialize();
		return super.removeEldestEntry(eldest);
	}

	@Override
	public int size() {
		initialize();
		return super.size();
	}

	@Override
	public String toString() {
		initialize();
		return super.toString();
	}

	@Override
	public Collection values() {
		initialize();
		return super.values();
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public boolean isProxied() {
		return true;
	}

	@Override
	public void initialize() {
		if (!initialized) {
			initialized = true;
		}
	}

}
