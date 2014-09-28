package br.com.anteros.persistence.proxy.collection;

public interface AnterosPersistentCollection {
	
	public boolean isInitialized();
	
	public boolean isProxied();
	
	public void initialize();

}
