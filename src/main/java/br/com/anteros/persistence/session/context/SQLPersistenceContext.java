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
package br.com.anteros.persistence.session.context;

import java.sql.Connection;

import br.com.anteros.persistence.metadata.EntityManaged;

public interface SQLPersistenceContext {

	public EntityManaged addEntityManaged(Object key, boolean readOnly, boolean newEntity) throws Exception;
	
	public EntityManaged getEntityManaged(Object key);
	
	public boolean isExistsEntityManaged(Object key);
	
	public void removeEntityManaged(Object key);
	
	public void onBeforeExecuteCommit(Connection connection) throws Exception;

	public void onBeforeExecuteRollback(Connection connection) throws Exception;

	public void onAfterExecuteCommit(Connection connection) throws Exception;

	public void onAfterExecuteRollback(Connection connection) throws Exception;
	
	public Object getObjectFromCache(Object key);
	
	public void addObjectToCache(Object key, Object value);
	
	public void addObjectToCache(Object key, Object value, int secondsToLive);
	
	public EntityManaged createEmptyEntityManaged(Object key);
	
	/**
	 * Remove todas as instâncias dos objetos da classe passada por parâmetro
	 * gerenciadas pela sessão
	 * 
	 * @param object
	 */
	public void evict(Class class0);

	/**
	 * Limpa o cache de entidades gerenciadas da sessão
	 */
	public void evictAll();
	
	public void detach(Object entity);

	public void clearCache();
	
}
