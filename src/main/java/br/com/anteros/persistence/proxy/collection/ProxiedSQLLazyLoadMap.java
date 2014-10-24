package br.com.anteros.persistence.proxy.collection;

import java.util.LinkedHashMap;

public class ProxiedSQLLazyLoadMap<K,V> extends LinkedHashMap<K,V> implements AnterosProxyMap {

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public void initialize() {
	}

	@Override
	public Object initializeAndReturnObject() {
		return null;
	}

	@Override
	public boolean isProxied() {
		return false;
	}


}
