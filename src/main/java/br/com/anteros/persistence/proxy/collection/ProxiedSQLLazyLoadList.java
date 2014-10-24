package br.com.anteros.persistence.proxy.collection;

import java.util.ArrayList;

public class ProxiedSQLLazyLoadList<T> extends ArrayList<T> implements AnterosProxyCollection {

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
		return true;
	}


}
