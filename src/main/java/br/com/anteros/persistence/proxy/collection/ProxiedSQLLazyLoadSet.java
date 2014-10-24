package br.com.anteros.persistence.proxy.collection;

import java.util.LinkedHashSet;

import br.com.anteros.persistence.proxy.AnterosProxyObject;

public class ProxiedSQLLazyLoadSet<T> extends LinkedHashSet<T> implements AnterosProxyCollection {

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
