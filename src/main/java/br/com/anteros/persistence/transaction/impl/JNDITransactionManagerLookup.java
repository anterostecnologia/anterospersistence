/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.transaction.impl;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import br.com.anteros.persistence.transaction.TransactionManagerLookup;


public class JNDITransactionManagerLookup implements TransactionManagerLookup {

	protected String getName(){
		return "java:comp/UserTransaction";
	}

	public TransactionManager getTransactionManager(Properties props) throws TransactionException {
		try {
			return (TransactionManager) new InitialContext(props).lookup( getName() );
		}
		catch (NamingException ne) {
			throw new TransactionException( "Could not locate TransactionManager", ne );
		}
	}

	public Transaction getTransactionIdentifier(Transaction transaction) {
		return transaction;
	}

	@Override
	public TransactionManager getTransactionManager() throws TransactionException {
		try {
			return (TransactionManager) new InitialContext().lookup( getName() );
		}
		catch (NamingException ne) {
			throw new TransactionException( "Could not locate TransactionManager", ne );
		}
	}

	@Override
	public String getUserTransactionName() {
		return "java:comp/UserTransaction";
	}
}






