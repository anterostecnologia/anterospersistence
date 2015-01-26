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

import br.com.anteros.persistence.session.SQLSession;

/**
 * Interface que define métodos responsáveis por modificar um SQL para realizar o bloqueio(lock) e também para realizar o bloqueio(lock) em
 * entidades de forma individual.
 * 
 * @author edson
 *
 */
public interface LockManager {

	/**
	 * Tenta realizar o bloqueio em uma entidade sendo gerenciada.
	 * 
	 * @param session
	 *            Sessão que está gerenciando a entidade.
	 * @param entity
	 *            Entidade alvo para o bloqueio.
	 * @param lockOptions
	 *            Opções de bloqueio.
	 * @throws Exception
	 *             Retorna um erro caso não consiga realizar o bloqueio.
	 * @see LockAcquisitionException
	 * @see LockTimeoutException
	 */
	public void lock(SQLSession session, Object entity, LockOptions lockOptions) throws Exception;

	/**
	 * Modifica o sql e aplica a estratégia de bloqueio conforme o dialeto do banco de dados.
	 * 
	 * @param session
	 *            Sessão que está gerenciando a entidade.
	 * @param sql
	 *            SQL a ser modificado e aplicado a estratégia de bloqueio.
	 * @param resultClass
	 *            Classe de resultado que representa a entidade a ser instanciada como resultado do SQL.
	 * @param lockOptions
	 *            Opções de bloqueio.
	 * @return SQL modificado
	 * @throws Exception
	 *             Retorna um erro caso não seja possível aplicar a estratégia de bloqueio para o SQL.
	 * @see LockAcquisitionException
	 * @see LockTimeoutException
	 */
	public String applyLock(SQLSession session, String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception;

}
