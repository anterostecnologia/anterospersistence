package br.com.anteros.persistence.transaction;

public interface AnterosSynchronization {

	public void beforeCompletion();

	public void afterCompletion(int status);

}
