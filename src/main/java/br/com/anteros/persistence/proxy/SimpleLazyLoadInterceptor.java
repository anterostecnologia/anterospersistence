package br.com.anteros.persistence.proxy;

import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.annotation.type.ScopeType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

public class SimpleLazyLoadInterceptor {

	private SQLSession session;
	private EntityCache entityCache;
	private Map<String, Object> columnKeyValuesTarget;
	private Cache transactionCache;
	private Object target;
	private Object owner;
	private DescriptionField descriptionFieldOwner;
	private boolean constructed = false;
	private boolean initialized = false;
	private Boolean processing = false;

	public SimpleLazyLoadInterceptor(SQLSession session, EntityCache entityCache, Map<String, Object> columKeyValues,
			Cache transactionCache, Object owner, DescriptionField descriptionField) {
		this.session = session;
		this.entityCache = entityCache;
		this.columnKeyValuesTarget = columKeyValues;
		this.transactionCache = transactionCache;
		this.owner = owner;
		this.descriptionFieldOwner = descriptionField;
	}

	public synchronized Object getTargetObject() throws Exception {
		if (!initialized) {
			try {
				initialized = true;
				processing = true;

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

				target = session.createQuery("").loadData(entityCache, owner, descriptionFieldOwner,
						columnKeyValuesTarget, transactionCache);

				if (ownerEntityCache != null) {
					if ((ownerEntityCache.getCacheScope().equals(ScopeType.TRANSACTION)) && (transactionCache != null))
						transactionCache.clear();
				}

				EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(owner);
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
				processing = false;
			}
		}
		return target;
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

}
