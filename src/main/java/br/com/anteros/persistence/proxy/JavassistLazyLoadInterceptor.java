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
package br.com.anteros.persistence.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.annotation.type.ScopeType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.query.SQLQuery;

public class JavassistLazyLoadInterceptor implements MethodHandler {

	private SQLSession session;
	private EntityCache entityCache;
	private Map<String, Object> columnKeyValuesTarget;
	private Cache transactionCache;
	private Object target;
	private Object owner;
	private DescriptionField descriptionFieldOwner;
	private Boolean constructed = Boolean.FALSE;
	private Boolean initialized = Boolean.FALSE;
	private Boolean processing = Boolean.FALSE;
	private LockOptions lockOptions;

	public JavassistLazyLoadInterceptor(SQLSession session, EntityCache entityCache,
			Map<String, Object> columKeyValues, Cache transactionCache, Object owner, DescriptionField descriptionField, LockOptions lockOptions) {
		this.session = session;
		this.entityCache = entityCache;
		this.columnKeyValuesTarget = columKeyValues;
		this.transactionCache = transactionCache;
		this.owner = owner;
		this.descriptionFieldOwner = descriptionField;
		this.lockOptions = lockOptions;
	}

	public Object invoke(final Object proxy, final Method thisMethod, final Method proceed, final Object[] args)
			throws Throwable {
		if (this.constructed) {

			if ("isInitialized".equals(thisMethod.getName())) {
				return isInitialized();
			}

			if ("initialize".equals(thisMethod.getName())) {
				getTargetObject();
				return null;
			}

			if ("initializeAndReturnObject".equals(thisMethod.getName())) {
				return initializeAndReturnObject();
			}

			Object target = getTargetObject();
			final Object returnValue;
			try {
				/*
				 * [ALTERADO] se entityCache != null, já que qdo for
				 * COLLECTION_TABLE o entityCache sera
				 */
				if (entityCache != null && ReflectionUtils.isPublic(entityCache.getEntityClass(), thisMethod)) {
					Class<?> dc = thisMethod.getDeclaringClass();
					if (!dc.isInstance(target))
						throw new ClassCastException(target.getClass().getName());
					returnValue = thisMethod.invoke(target, args);
				} else {
					if (!thisMethod.isAccessible())
						thisMethod.setAccessible(true);
					returnValue = thisMethod.invoke(target, args);
				}
				return returnValue == target ? proxy : returnValue;
			} catch (InvocationTargetException ite) {
				throw ite.getTargetException();
			}
		}
		return null;
	}

	private synchronized Object getTargetObject() throws Exception {
		if (!initialized) {
			try {
				initialized = Boolean.TRUE;
				processing = Boolean.TRUE;

				/*
				 * Se a lista possui um pai adiciona no cache para evitar
				 * duplicidade de objetos
				 */
				EntityCache ownerEntityCache = null;
				if (owner != null) {
					ownerEntityCache = session.getEntityCacheManager().getEntityCache(owner.getClass());
					if (ownerEntityCache != null) {
						String uniqueId = ownerEntityCache.getCacheUniqueId(owner);
						if ((ownerEntityCache.getCacheScope().equals(ScopeType.TRANSACTION))
								&& (transactionCache != null)) {
							transactionCache.put(ownerEntityCache.getEntityClass().getName() + "_" + uniqueId, owner,
									ownerEntityCache.getMaxTimeCache());
						}
					}
				}

				SQLQuery query = session.createQuery("");
				query.setLockOptions(lockOptions);
				query.allowDuplicateObjects(true);
				target = query.loadData(entityCache, owner, descriptionFieldOwner,
						columnKeyValuesTarget, transactionCache);

				EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(owner);
				/*
				 * Caso o objeto possa ser gerenciado(objeto completo ou parcial
				 * que tenha sido buscado id no sql) adiciona o objeto no cache
				 */
				if (entityManaged != null) {
					if (entityManaged.getStatus() != EntityStatus.READ_ONLY) {
						/*
						 * Guarda o valor da chave do objeto result na lista de
						 * oldValues
						 */
						FieldEntityValue value = descriptionFieldOwner.getFieldEntityValue(session, owner, target);
						entityManaged.addOriginalValue(value);
						entityManaged.addLastValue(value);
						/*
						 * Adiciona o campo na lista de campos que poderão ser
						 * alterados. Se o campo não for buscado no select não
						 * poderá ser alterado.
						 */
						entityManaged.getFieldsForUpdate().add(descriptionFieldOwner.getField().getName());
					}
				}

			} finally {
				processing = Boolean.FALSE;
			}
		}
		return target;
	}

	public Object initializeAndReturnObject() throws Exception {
		return getTargetObject();
	}

	public SQLSession getSession() {
		return session;
	}

	public void setSession(SQLSession session) {
		this.session = session;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public Map<String, Object> getColumnKeyValuesTarget() {
		return columnKeyValuesTarget;
	}

	public void setColumnKeyValuesTarget(Map<String, Object> columnKeyValuesTarget) {
		this.columnKeyValuesTarget = columnKeyValuesTarget;
	}

	public Cache getTransactionCache() {
		return transactionCache;
	}

	public void setTransactionCache(Cache transactionCache) {
		this.transactionCache = transactionCache;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public boolean isConstructed() {
		return constructed;
	}

	public void setConstructed(boolean constructed) {
		this.constructed = constructed;
	}

	public DescriptionField getDescriptionFieldOwner() {
		return descriptionFieldOwner;
	}

	public void setDescriptionField(DescriptionField descriptionFieldOwner) {
		this.descriptionFieldOwner = descriptionFieldOwner;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public LockOptions getLockOptions() {
		return lockOptions;
	}

}
