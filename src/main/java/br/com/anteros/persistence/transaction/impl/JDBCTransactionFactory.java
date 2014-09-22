package br.com.anteros.persistence.transaction.impl;

import java.sql.Connection;

import br.com.anteros.persistence.session.context.SQLPersistenceContext;
import br.com.anteros.persistence.transaction.Transaction;
import br.com.anteros.persistence.transaction.TransactionFactory;

public class JDBCTransactionFactory implements TransactionFactory {

	@Override
	public Transaction createTransaction(Connection connection, SQLPersistenceContext context) throws TransactionException {
		return new JDBCTransaction(connection, context);
	}

}
