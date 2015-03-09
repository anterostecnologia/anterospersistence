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

package br.com.anteros.persistence.session.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.AnterosWeakHashMap;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.cache.SQLCache;
import br.com.anteros.persistence.session.context.SQLPersistenceContext;

public class SQLPersistenceContextImpl implements SQLPersistenceContext {

	private Map<Object, EntityManaged> entities = new AnterosWeakHashMap<Object, EntityManaged>();
	private EntityCacheManager entityCacheManager;
	private SQLSession session;
	private Cache cache;
	private static Logger LOG = LoggerProvider.getInstance().getLogger(SQLPersistenceContext.class);

	public SQLPersistenceContextImpl(SQLSession session, EntityCacheManager entityCacheManager) {
		this.entityCacheManager = entityCacheManager;
		this.session = session;
		this.cache = new SQLCache();
	}

	public EntityManaged addEntityManaged(Object value, boolean readOnly, boolean newEntity) throws Exception {
		LOG.debug("Add entity managed ");
		EntityManaged entityManaged = getEntityManaged(value);
		if (entityManaged == null) {
			LOG.debug("Create new entity managed");
			EntityCache entityCache = entityCacheManager.getEntityCache(value.getClass());
			entityManaged = new EntityManaged(entityCache);
			entityManaged.setStatus(readOnly ? EntityStatus.READ_ONLY : EntityStatus.MANAGED);
			entityManaged.setNewEntity(newEntity);

			if (!readOnly) {
				entityManaged.setFieldsForUpdate(entityCache.getAllFieldNames());
				for (DescriptionField descriptionField : entityCache.getDescriptionFields())
					entityManaged.addLastValue(descriptionField.getFieldEntityValue(session, value));
			}
			entities.put(value, entityManaged);
			LOG.debug("Entity managed created");
		}
		return entityManaged;
	}

	public EntityManaged getEntityManaged(Object key) {
		return entities.get(key);
	}

	public void removeEntityManaged(Object key) {
		entities.remove(key);
	}

	public boolean isExistsEntityManaged(Object key) {
		return entities.containsKey(key);
	}

	public void onBeforeExecuteCommit(Connection connection) throws Exception {
		session.onBeforeExecuteCommit(connection);
	}

	public void onBeforeExecuteRollback(Connection connection) throws Exception {
		session.onBeforeExecuteRollback(connection);
	}

	public void onAfterExecuteCommit(Connection connection) throws Exception {
		if (session.getConnection() == connection) {
			for (EntityManaged entityManaged : entities.values())
				entityManaged.commitValues();
		}
	}

	public void onAfterExecuteRollback(Connection connection) throws Exception {
		if (session.getConnection() == connection) {
			for (EntityManaged entityManaged : entities.values())
				entityManaged.resetValues();
			removeNewEntities();
		}
	}

	private void removeNewEntities() {
		List<EntityManaged> entitiesToRemove = new ArrayList<EntityManaged>();
		for (EntityManaged entityManaged : entities.values()) {
			if (entityManaged.isNewEntity())
				entitiesToRemove.add(entityManaged);
		}
		for (EntityManaged entityManaged : entitiesToRemove)
			entities.remove(entityManaged);
	}

	public Object getObjectFromCache(Object key) {
		return cache.get(key);
	}

	public void addObjectToCache(Object key, Object value) {
		cache.put(key, value);
	}

	public void addObjectToCache(Object key, Object value, int secondsToLive) {
		cache.put(key, value, secondsToLive);
	}

	public EntityManaged createEmptyEntityManaged(Object key) {
		EntityManaged em = new EntityManaged(entityCacheManager.getEntityCache(key.getClass()));
		entities.put(key, em);
		return em;
	}

	public void evict(Class sourceClass) {
		List<Object> keys = new ArrayList<Object>(entities.keySet());
		for (Object obj : keys) {
			if (obj != null) {
				if (obj.getClass().equals(sourceClass)) {
					entities.remove(obj);
				}
			}
		}
	}

	public void evictAll() {
		entities.clear();
	}

	public void clearCache() {
		cache.clear();
	}

	@Override
	public void detach(Object entity) {
		List<Object> keys = new ArrayList<Object>(entities.keySet());
		for (Object obj : keys) {
			if (obj != null) {
				if (obj.equals(entity)) {
					entities.remove(obj);
					break;
				}
			}
		}
	}

}
