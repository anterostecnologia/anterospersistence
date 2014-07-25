package br.com.anteros.persistence.transaction;

import javax.transaction.Synchronization;

/**
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 */
public interface Transaction {
	

	public void begin() throws Exception;


	public void commit() throws Exception;


	public void rollback() throws Exception;


	public boolean isActive() throws Exception;
	
	
	public void registerSynchronization(Synchronization synchronization) throws Exception;

}
