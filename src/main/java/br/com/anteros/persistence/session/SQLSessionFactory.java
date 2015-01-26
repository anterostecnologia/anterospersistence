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
package br.com.anteros.persistence.session;

import java.sql.Connection;

import br.com.anteros.persistence.metadata.EntityCacheManager;


/**
 * SessionFactory - Responsável por fornecedor instâncias de SQLSession.
 * 
 */
public interface SQLSessionFactory {

	/**
	 * Retorna a SQLSession da thread corrente
	 */
	public SQLSession getCurrentSession() throws Exception;

	/**
	 * Retorna uma nova SQLSession a cada vez que é executado
	 */
	public SQLSession openSession() throws Exception;
	
	/**
	 * Retorna uma nova SQLSession a cada vez que é executado
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	public SQLSession openSession(Connection connection) throws Exception;
	
	/**
	 * Retorna o cache das descrições das entidades 
	 * @return
	 */
	public EntityCacheManager getEntityCacheManager();

}
