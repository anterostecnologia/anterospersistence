package br.com.anteros.persistence.proxy.collection;

public class DefaultSQLMap<K,V> extends AbstractSQLMap<K,V> {

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public boolean isProxied() {
		return false;
	}

	@Override
	public void initialize() {
	}

}
