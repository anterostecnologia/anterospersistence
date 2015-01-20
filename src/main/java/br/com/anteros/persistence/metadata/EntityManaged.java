package br.com.anteros.persistence.metadata;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.lock.LockMode;

public class EntityManaged {
	private EntityCache entityCache;
	private EntityStatus status;
	private Set<String> fieldsForUpdate = new LinkedHashSet<String>();
	private Set<FieldEntityValue> originalValues = new LinkedHashSet<FieldEntityValue>();
	private Set<FieldEntityValue> lastValues = new LinkedHashSet<FieldEntityValue>();
	private Object originalVersion;
	private Object oldVersion;
	private Object currentVersion;
	private boolean newEntity;
	private LockMode lockMode = LockMode.NONE;

	public EntityManaged(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public Set<String> getFieldsForUpdate() {
		return fieldsForUpdate;
	}

	public void setFieldsForUpdate(Set<String> fieldsForUpdate) {
		this.fieldsForUpdate = fieldsForUpdate;
	}

	public Set<FieldEntityValue> getOriginalValues() {
		return Collections.unmodifiableSet(originalValues);
	}

	public Set<FieldEntityValue> getLastValues() {
		return Collections.unmodifiableSet(lastValues);
	}

	public Object getOriginalVersion() {
		return originalVersion;
	}

	public void setOriginalVersion(Object originalVersion) {
		this.originalVersion = originalVersion;
	}

	public Object getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(Object oldVersion) {
		this.oldVersion = oldVersion;
	}

	public void addOriginalValue(FieldEntityValue value) {
		if (value != null) {
			if (originalValues.contains(value))
				originalValues.remove(value);
			originalValues.add(value);
		}
	}

	public void addLastValue(FieldEntityValue value) {
		if (value != null) {
			if (lastValues.contains(value))
				lastValues.remove(value);
			lastValues.add(value);
		}
	}

	public void clearLastValues() {
		lastValues.clear();
	}

	public void clearOriginalValues() {
		originalValues.clear();
	}

	public Object getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Object currentVersion) {
		this.currentVersion = currentVersion;
	}

	public EntityStatus getStatus() {
		return status;
	}

	public void setStatus(EntityStatus status) {
		this.status = status;
	}

	public void updateLastValues(SQLSession session, Object targetObject)
			throws Exception {
		this.clearLastValues();
		this.setStatus(EntityStatus.MANAGED);
		for (DescriptionField descriptionField : entityCache
				.getDescriptionFields())
			this.addLastValue(descriptionField.getFieldEntityValue(session,
					targetObject));
		this.setOldVersion(this.getCurrentVersion());
		this.setCurrentVersion(null);
	}

	public boolean isNewEntity() {
		return newEntity;
	}

	public void setNewEntity(boolean newEntity) {
		this.newEntity = newEntity;
	}

	public void resetValues() {
		this.clearLastValues();
		for (FieldEntityValue field : this.getOriginalValues())
			this.addLastValue(field);
		this.setOldVersion(this.getOriginalVersion());
		this.setCurrentVersion(null);
	}

	public void commitValues() {
		this.clearOriginalValues();
		for (FieldEntityValue field : this.getLastValues())
			this.addOriginalValue(field);
		this.setOldVersion(this.getCurrentVersion());
		this.setOriginalVersion(this.getCurrentVersion());
		this.setCurrentVersion(null);
		this.setNewEntity(false);
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	public void setLockMode(LockMode lockMode) {
		this.lockMode = lockMode;
	}

}
