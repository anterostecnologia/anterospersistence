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
 * Tipos de bloqueio disponíveis para realizar o travamento de entidades.
 * 
 * @author edson
 *
 */
public enum LockMode {

	/**
	 * Nenhum bloqueio é necessário. Modo padrão.
	 */
	NONE(0), 
	
	/**
	 * Sinônimo para OPTIMISTIC.
	 */
	READ(5), 
	
	/**
	 * Sinônimo para OPTIMISTIC_FORCE_INCREMENT.
	 */
	WRITE(10),

	/**
	 * A versão da entidade será verificada no COMMIT da transação para checar se a mesma foi alterada. Se a entidade
	 * foi removida ou alterada para uma nova versão será lançada uma exceção {@link OptimisticLockException}.
	 */
	OPTIMISTIC(6),

	/**
	 * A versão da entidade será incrementada e verificada no COMMIT da transação para checar se a mesma foi alterada. Se a entidade
	 * foi removida ou alterada para uma nova versão será lançada uma exceção {@link OptimisticLockException}.
	 */
	OPTIMISTIC_FORCE_INCREMENT(7),

	/**
	 * A entidade será bloqueada no banco de dados impedindo que outra transação tente realizar um bloqueio do tipo PESSIMISTIC_WRITE. 
	 */
	PESSIMISTIC_READ(12),

	/**
	 * A entidade será bloqueada no banco de dados impedindo que outra transação tente realizar um bloqueio do tipo PESSIMISTIC_WRITE ou PESSIMISTIC_READ.
	 */
	PESSIMISTIC_WRITE(13),

	/**
	 * A entidade será bloqueada no banco de dados impedindo que outra transação tente realizar um bloqueio do tipo PESSIMISTIC_WRITE ou PESSIMISTIC_READ. 
	 * A entidade terá também sua versão incrementada no COMMIT da transação.	  
	 */
	PESSIMISTIC_FORCE_INCREMENT(17);
	
	private final int level;

	private LockMode(int level) {
		this.level = level;
	}

	/**
	 * Retorna se um nível do LockMode é superior a outro.
	 * @param lockMode 
	 * @return Verdadeiro se for superior.
	 */
	public boolean greaterThan(LockMode lockMode) {
		return level > lockMode.level;
	}

	/**
	 * Retorna se um nível do LockMode é inferior a outro.
	 * @param lockMode 
	 * @return Verdadeiro se for inferior.
	 */
	public boolean lessThan(LockMode lockMode) {
		return level < lockMode.level;
	}

}
