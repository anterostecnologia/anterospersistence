/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.lock;

import java.util.Set;

import br.com.anteros.core.utils.CompactHashSet;

/**
 * Opções para o travamento(lock) de entidades.
 * 
 * @author edson
 *
 */
public class LockOptions {

	/**
	 * Define um timeout para não aguardar o bloqueio.
	 */
	public static final int NO_WAIT = 0;

	/**
	 * Define um timeout para aguardar indefinidamente até que o bloqueio seja realizado.
	 */
	public static final int WAIT_FOREVER = -1;

	/**
	 * Instância estática de LockOptions com um LockMode do tipo READ. Podendo ser usado para casos onde não há
	 * necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions READ = new LockOptions(LockMode.READ);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo WRITE. Podendo ser usado para casos onde não há
	 * necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions WRITE = new LockOptions(LockMode.WRITE);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo OPTIMISTIC. Podendo ser usado para casos onde não há
	 * necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions OPTIMISTIC = new LockOptions(LockMode.OPTIMISTIC);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo OPTIMISTIC_FORCE_INCREMENT. Podendo ser usado para
	 * casos onde não há necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions OPTIMISTIC_FORCE_INCREMENT = new LockOptions(LockMode.OPTIMISTIC_FORCE_INCREMENT);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo PESSIMISTIC_READ. Podendo ser usado para casos onde não
	 * há necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions PESSIMISTIC_READ = new LockOptions(LockMode.PESSIMISTIC_READ);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo PESSIMISTIC_WRITE. Podendo ser usado para casos onde
	 * não há necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions PESSIMISTIC_WRITE = new LockOptions(LockMode.PESSIMISTIC_WRITE);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo PESSIMISTIC_FORCE_INCREMENT. Podendo ser usado para
	 * casos onde não há necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions PESSIMISTIC_FORCE_INCREMENT = new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT);
	/**
	 * Instância estática de LockOptions com um LockMode do tipo NONE. Podendo ser usado para casos onde não há
	 * necessidade de alterar as opções de bloqueio.
	 */
	public static final LockOptions NONE = new LockOptions(LockMode.NONE);

	private LockMode lockMode;

	private int timeout = WAIT_FOREVER;

	private Set<String> aliasesToLock = new CompactHashSet<String>();

	private LockScope lockScope = LockScope.NORMAL;

	/**
	 * Cria uma instância de LockOptions
	 * 
	 * @param lockMode
	 *            Tipo de bloqueio
	 * @return Instância de LockOptions
	 */
	public static LockOptions create(LockMode lockMode) {
		return new LockOptions(lockMode);
	}

	/**
	 * Cria uma instância de LockOptions
	 * 
	 * @param lockMode
	 *            Tipo de bloqueio
	 * @param timeout
	 *            Tempo de espera do bloqueio
	 * @param aliasesToLock
	 *            Aliases das tabelas que serão bloqueadas no SQL
	 * @return Instância de LockOptions
	 */
	public static LockOptions create(LockMode lockMode, int timeout, String... aliasesToLock) {
		LockOptions lockOptions = new LockOptions(lockMode, timeout);
		for (String alias : aliasesToLock) {
			lockOptions.addAliasToLock(alias);
		}
		return lockOptions;
	}

	/**
	 * Cria uma instância de LockOptions
	 * 
	 * @param lockMode
	 *            Tipo de bloqueio
	 * @param aliasesToLock
	 *            Aliases das tabelas que serão bloqueadas no SQL
	 * @return Instância de LockOptions
	 */
	public static LockOptions create(LockMode lockMode, String... aliasesToLock) {
		LockOptions lockOptions = new LockOptions(lockMode);
		for (String alias : aliasesToLock) {
			lockOptions.addAliasToLock(alias);
		}
		return lockOptions;
	}

	/**
	 * Cria um instância de LockOptions.
	 * 
	 * @param lockMode
	 *            Tipo de bloqueio
	 * @param timeout
	 *            Tempo de espera do bloqueio
	 * @return Instância de LockOptions
	 */
	public static LockOptions create(LockMode lockMode, int timeout) {
		return new LockOptions(lockMode, timeout);
	}

	public LockOptions() {
	}

	public LockOptions(LockMode lockMode) {
		this.lockMode = lockMode;
	}

	public LockOptions(LockMode lockMode, int timeout) {
		this.lockMode = lockMode;
		this.timeout = timeout;
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	public LockOptions setLockMode(LockMode lockMode) {
		this.lockMode = lockMode;
		return this;
	}

	public LockOptions addAliasToLock(String alias) {
		aliasesToLock.add(alias);
		return this;
	}

	public int getTimeOut() {
		return timeout;
	}

	public LockOptions setTimeOut(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Cria uma cópia do LockOptions.
	 * 
	 * @return Nova instância.
	 */
	public LockOptions makeCopy() {
		final LockOptions copy = new LockOptions();
		copy(this, copy);
		return copy;
	}

	/**
	 * Copia opções de um instância LockOptions para outra.
	 * 
	 * @param source
	 *            Origem
	 * @param destination
	 *            Destino
	 * @return Destino
	 */
	public static LockOptions copy(LockOptions source, LockOptions destination) {
		destination.setLockMode(source.getLockMode());
		destination.setTimeOut(source.getTimeOut());
		destination.setLockScope(source.getLockScope());
		if (source.aliasesToLock != null) {
			destination.aliasesToLock = new CompactHashSet<String>(source.aliasesToLock);
		}
		return destination;
	}

	public LockScope getLockScope() {
		return lockScope;
	}

	public LockOptions setLockScope(LockScope lockScope) {
		this.lockScope = lockScope;
		return this;
	}

	/**
	 * Retorna se um dos LockMode passados como parâmetro é igual ao LockMode.
	 * 
	 * @param locks
	 *            Tipos de bloqueio
	 * @return Verdadeiro e o LockMode é igual a um dos tipos da lista.
	 */
	public boolean contains(LockMode... locks) {
		for (LockMode lock : locks) {
			if (lock == lockMode)
				return true;
		}
		return false;
	}

	public Set<String> getAliasesToLock() {
		return aliasesToLock;
	}

	public void setAliasesToLock(Set<String> aliasesToLock) {
		this.aliasesToLock = aliasesToLock;
	}

	public void clearAliasesToLock() {
		if (aliasesToLock != null)
			aliasesToLock.clear();
	}

	@Override
	public String toString() {
		return "LockOptions [lockMode=" + lockMode + ", timeout=" + timeout + ", lockScope=" + lockScope + "]";
	}	

}
