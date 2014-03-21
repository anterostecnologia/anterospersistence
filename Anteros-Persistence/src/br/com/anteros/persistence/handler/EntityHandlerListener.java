package br.com.anteros.persistence.handler;


public interface EntityHandlerListener {
	public boolean setValue(String fieldName, Object fieldValue);

	public Object getValue(String fieldName);
}
