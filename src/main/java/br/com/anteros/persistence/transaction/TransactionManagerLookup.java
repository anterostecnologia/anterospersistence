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
package br.com.anteros.persistence.transaction;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import br.com.anteros.persistence.transaction.impl.TransactionException;

public interface TransactionManagerLookup {

	public TransactionManager getTransactionManager() throws TransactionException;

	public String getUserTransactionName();

	public Transaction getTransactionIdentifier(Transaction transaction);
}

