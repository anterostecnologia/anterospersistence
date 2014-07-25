package br.com.anteros.persistence.transaction;

import java.sql.Connection;

import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.impl.TransactionException;

/**
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 */
public interface TransactionFactory {

	/**
	 * Begin a transaction and return the associated <tt>Transaction</tt>
	 * instance.
	 * 
	 * @param connection
	 * @param context
	 * @return
	 */
	public Transaction createTransaction(Connection connection, SQLPersistenceContext context)
			throws Exception;

}
