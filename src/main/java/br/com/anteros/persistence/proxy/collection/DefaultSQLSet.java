package br.com.anteros.persistence.proxy.collection;

public class DefaultSQLSet<T> extends AbstractSQLSet<T> {

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
