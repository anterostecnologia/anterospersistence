package br.com.anteros.persistence.transaction;

/**
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 */
public interface Transaction {

	/**
	 * Begin a new transaction.
	 */
	public void begin() throws Exception;

	/**
	 * Flush the associated <tt>Session</tt> and end the unit of work. </p> This
	 * method will commit the underlying transaction if and only if the
	 * underlying transaction was initiated by this object.
	 *
	 */
	public void commit() throws Exception;

	/**
	 * Force the underlying transaction to roll back.
	 */
	public void rollback() throws Exception;

	/**
	 * Is this transaction still active?
	 * <p/>
	 * Again, this only returns information in relation to the local
	 * transaction, not the actual underlying transaction.
	 *
	 * @return boolean True if this local transaction is still active.
	 */
	public boolean isActive() throws Exception;

}
