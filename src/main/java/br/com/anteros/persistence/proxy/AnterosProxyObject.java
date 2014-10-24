package br.com.anteros.persistence.proxy;


public interface AnterosProxyObject  {

	boolean isInitialized();
	
	void initialize();
	
	Object initializeAndReturnObject();
}
