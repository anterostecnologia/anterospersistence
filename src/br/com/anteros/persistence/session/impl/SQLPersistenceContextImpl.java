/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package br.com.anteros.persistence.session.impl;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLPersistenceContext;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.cache.SQLCache;

public class SQLPersistenceContextImpl implements SQLPersistenceContext {

	private Map<EntityManaged, Reference> entities = new HashMap<EntityManaged, Reference>();
	private EntityCacheManager entityCacheManager;
	private SQLSession session;
	private Cache cache;

	public SQLPersistenceContextImpl(SQLSession session,
			EntityCacheManager entityCacheManager, int cacheSize) {
		this.entityCacheManager = entityCacheManager;
		this.session = session;
		this.cache = new SQLCache(cacheSize);
	}

	@Override
	public EntityManaged addEntityManaged(Object value, boolean readOnly,
			boolean newEntity) throws Exception {
		EntityManaged key = getEntityManaged(value);
		if (key == null) {
			EntityCache entityCache = entityCacheManager.getEntityCache(value
					.getClass());
			key = new EntityManaged(entityCache);
			key.setStatus(readOnly ? EntityStatus.READ_ONLY
					: EntityStatus.MANAGED);
			key.setFieldsForUpdate(entityCache.getAllFieldNames());
			key.setNewEntity(newEntity);
			for (DescriptionField descriptionField : entityCache
					.getDescriptionFields())
				key.addLastValue(descriptionField.getFieldEntityValue(
						session, value));
			entities.put(key, new WeakReference(value));
		}
		return key;
	}

	@Override
	public EntityManaged getEntityManaged(Object key) {
		List<EntityManaged> keysToRemove = new ArrayList<EntityManaged>();
		EntityManaged em = null;
		for (EntityManaged entityManaged : entities.keySet()) {
			Reference ref = entities.get(entityManaged);
			if (ref.get() == null) {
				keysToRemove.add(entityManaged);
				continue;
			}
			/*
			 * Utiliza a função System.identityHashCode() para obter o hashCode
			 * único do objeto e não chamar o hashCode() reescrito pelo usuário
			 */
			if (System.identityHashCode(key) == System.identityHashCode(ref.get())) {
				em = entityManaged;
			}
		}
		for (EntityManaged entityManaged : keysToRemove) {
			entities.remove(entityManaged);
		}
		return em;
	}

	@Override
	public void removeEntityManaged(Object key) {
		EntityManaged entity = getEntityManaged(key);
		if (entity != null)
			entities.remove(entity);
	}

	@Override
	public boolean isExistsEntityManaged(Object key) {
		EntityManaged entity = getEntityManaged(key);
		return (entity != null);
	}

	@Override
	public void onBeforeExecuteCommit(Connection connection) throws Exception {
		session.onBeforeExecuteCommit(connection);
	}

	@Override
	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		session.onBeforeExecuteRollback(connection);
	}

	@Override
	public void onAfterExecuteCommit(Connection connection) throws Exception {
		if (session.getConnection() == connection) {
			for (EntityManaged entityManaged : entities.keySet())
				entityManaged.commitValues();
		}
	}

	@Override
	public void onAfterExecuteRollback(Connection connection) throws Exception {
		if (session.getConnection() == connection) {
			for (EntityManaged entityManaged : entities.keySet())
				entityManaged.resetValues();
			removeNewEntities();
		}
	}

	private void removeNewEntities() {
		List<EntityManaged> entitiesToRemove = new ArrayList<EntityManaged>();
		for (EntityManaged entityManaged : entities.keySet()) {
			if (entityManaged.isNewEntity())
				entitiesToRemove.add(entityManaged);
		}
		for (EntityManaged entityManaged : entitiesToRemove)
			entities.remove(entityManaged);
	}

	@Override
	public Object getObjectFromCache(Object key) {
		return cache.get(key);
	}

	@Override
	public void addObjectToCache(Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public void addObjectToCache(Object key, Object value, int secondsToLive) {
		cache.put(key, value, secondsToLive);
	}

	@Override
	public EntityManaged createEmptyEntityManaged(Object key) {
		EntityManaged em = new EntityManaged(entityCacheManager.getEntityCache(key.getClass()));
		entities.put(em, new WeakReference(key));
		return em;
	}

	@Override
	public void evict(Class class0) {
		List<EntityManaged> keys = new ArrayList<EntityManaged>(entities.keySet());
		for (EntityManaged entityManaged : keys) {
			Object obj = entities.get(entityManaged);
			if (obj.getClass().equals(class0)) {
				entities.remove(entityManaged);
			}
		}
	}

	@Override
	public void evictAll() {
		entities.clear();
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

}
