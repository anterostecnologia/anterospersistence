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
package br.com.anteros.persistence.session.lock;

/**
 * Exceção será lançado quando ocorrerem conflitos nas tentativas de bloqueio(lock).
 * @author edson
 *
 */
public class PessimisticLockException extends LockException {

	public PessimisticLockException(String message) {
		super(message);
	}

	public PessimisticLockException(String message, Throwable t, String sql) {
		super(message, t, sql);
	}

}
