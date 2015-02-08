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
package br.com.anteros.persistence.metadata.identifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.command.Select;

public class Identifier<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	private Class<T> clazz;
	private Object owner;
	private SQLSession session;
	private transient EntityCache entityCache;
	private boolean onlyRefreshOwner = false;

	public static <T> Identifier<T> create(SQLSession session, Class<T> sourceClass) throws Exception {
		return new Identifier<T>(session, sourceClass);
	}

	public static <T> Identifier<T> create(SQLSession session, T owner) throws Exception {
		return new Identifier<T>(session, owner);
	}

	public static <T> Identifier<T> create(SQLSession session, T owner, boolean onlyRefreshOwner) throws Exception {
		return new Identifier<T>(session, owner, onlyRefreshOwner);
	}

	public Identifier(SQLSession session, Class<T> sourceClass) throws Exception {
		Class<T> anyClass = null;
		if (ReflectionUtils.isAbstractClass(sourceClass)) {
			anyClass = (Class<T>) session.getEntityCacheManager().getAnyConcreteClass(sourceClass);
			if (anyClass == null) {
				throw new IdentifierException("Não é possível criar um identificador para a classe abstrata " + sourceClass.getName()
						+ " pois não foi localizado nenhuma classe concreta que implemente a mesma.");
			}
		}
		entityCache = session.getEntityCacheManager().getEntityCache(sourceClass);
		if (entityCache == null) {
			throw new IdentifierException("Classe " + sourceClass.getName() + " não encontrada na lista de entidades.");
		}
		this.clazz = sourceClass;
		if (anyClass != null)
			this.owner = anyClass.newInstance();
		else
			this.owner = sourceClass.newInstance();
		this.session = session;
	}

	@SuppressWarnings("unchecked")
	public Identifier(SQLSession session, T owner) throws Exception {
		this.owner = owner;
		this.clazz = (Class<T>) owner.getClass();
		this.session = session;
		this.entityCache = session.getEntityCacheManager().getEntityCache(clazz);
		if (entityCache == null)
			throw new IdentifierException("Classe " + clazz.getName() + " não encontrada na lista de entidades.");
	}

	public Identifier(SQLSession session, T owner, boolean onlyRefreshOwner) throws Exception {
		this(session, owner);
		this.onlyRefreshOwner = onlyRefreshOwner;
	}

	public Identifier<T> setFieldValue(String fieldName, Object value) throws Exception {
		DescriptionField descriptionField = entityCache.getDescriptionField(fieldName);
		if (descriptionField == null)
			throw new IdentifierException("Campo " + fieldName + " não encontrado na classe " + clazz.getName()
					+ ". Não foi possível atribuir o valor.");

		if ((value == null) || (value.getClass() == descriptionField.getField().getType()) || (descriptionField.getTargetEntity() == null)) {
			if (value instanceof IdentifierColumn[])
				descriptionField.setObjectValue(owner, ((IdentifierColumn[]) value)[0].getValue());
			else
				descriptionField.setObjectValue(owner, value);
			return this;
		}

		Select select = new Select(session.getDialect());
		select.addTableName(descriptionField.getTargetEntity().getTableName());
		List<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean append = false;
		if (value instanceof Map) {
			for (Object column : ((Map<?, ?>) value).keySet()) {
				if (append)
					select.and();
				select.addCondition("" + column, "=", ":P" + column);
				params.add(new NamedParameter("P" + column, ((Map<?, ?>) value).get(column)));
				append = true;
			}
		} else if (value instanceof IdentifierColumn[]) {
			for (IdentifierColumn column : (IdentifierColumn[]) value) {
				if (append)
					select.and();
				select.addCondition("" + column.getColumnName(), "=", ":P" + column.getColumnName());
				params.add(new NamedParameter("P" + column.getColumnName(), column.getValue()));
				append = true;
			}
		} else if (value instanceof Object[]) {
			if (((Object[]) value).length != descriptionField.getDescriptionColumns().size()) {
				throw new IdentifierException("Número de parâmetros informados " + ((Object[]) value).length
						+ " diferente do número de colunas do campo " + descriptionField.getName() + " da classe "
						+ entityCache.getEntityClass().getName());
			}
			int index = 0;
			for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
				if (append)
					select.and();
				select.addCondition("" + descriptionColumn.getColumnName(), "=", ":P" + descriptionColumn.getColumnName());
				params.add(new NamedParameter("P" + descriptionColumn.getColumnName(), ((Object[]) value)[index]));
				append = true;
				index++;
			}
		} else {
			throw new IdentifierException("Tipo de parâmetro incorreto " + value.getClass() + ". Não foi possível atribuir o valor para o campo "
					+ descriptionField.getName() + " da classe " + entityCache.getEntityClass().getName());
		}
		descriptionField.setObjectValue(owner,
				session.createQuery(select.toStatementString(), descriptionField.getField().getType(), params.toArray(new NamedParameter[] {}))
						.getSingleResult());
		return this;
	}

	public Object getFieldValue(String fieldName) throws Exception {
		DescriptionField descriptionField = entityCache.getDescriptionField(fieldName);
		if (descriptionField == null)
			throw new IdentifierException("Campo " + fieldName + " não encontrado na classe " + clazz.getName()
					+ ". Não foi possível atribuir o valor.");
		return entityCache.getDescriptionField(fieldName).getObjectValue(owner);
	}

	public Object getColumnValue(String columnName) throws Exception {
		DescriptionColumn descriptionColumn = entityCache.getDescriptionColumnByName(columnName);
		if (descriptionColumn == null)
			throw new IdentifierException("Coluna " + columnName + " não encontrada na classe " + clazz.getName());
		return descriptionColumn.getColumnValue(owner);
	}

	public Map<String, Object> getColumns() throws Exception {
		return entityCache.getPrimaryKeysAndValues(owner);
	}
	
	public Map<String, Object> getDatabaseColumns() throws Exception {
		return entityCache.getPrimaryKeysAndDatabaseValues(owner);
	}

	public Collection<Object> getValues() throws Exception {
		Map<String, Object> primaryKeysAndValues = entityCache.getPrimaryKeysAndValues(owner);
		return primaryKeysAndValues.values();
	}
	
	public Collection<Object> getDatabaseValues() throws Exception {
		Map<String, Object> primaryKeysAndValues = entityCache.getPrimaryKeysAndDatabaseValues(owner);
		return primaryKeysAndValues.values();
	}
	
	public Map<String,Object> getColumnsValues() throws Exception {
		return entityCache.getPrimaryKeysAndValues(owner);
	}
	
	public Map<String,Object> getDatabaseColumnsValues() throws Exception {
		return entityCache.getPrimaryKeysAndDatabaseValues(owner);
	}

	public boolean hasIdentifier() throws Exception {
		Map<String, Object> result = getColumns();
		if (result.size() != entityCache.getPrimaryKeyColumns().size())
			return false;
		for (Object object : result.values()) {
			if (object == null)
				return false;
		}
		return true;
	}

	public String getUniqueId() throws Exception {
		StringBuilder sb = new StringBuilder("");
		Map<String, Object> primaryKey = new TreeMap<String, Object>(this.getColumns());
		for (String key : primaryKey.keySet()) {
			if (!"".equals(sb.toString()))
				sb.append("_");
			sb.append(primaryKey.get(key));
		}
		return sb.toString();
	}
	
	public String getDatabaseUniqueId() throws Exception {
		StringBuilder sb = new StringBuilder("");
		Map<String, Object> primaryKey = new TreeMap<String, Object>(this.getDatabaseColumns());
		for (String key : primaryKey.keySet()) {
			if (!"".equals(sb.toString()))
				sb.append("_");
			sb.append(primaryKey.get(key));
		}
		return sb.toString();
	}

	public Class<? extends Object> getClazz() {
		return clazz;
	}

	public Object getOwner() {
		return owner;
	}

	@SuppressWarnings("unchecked")
	public Identifier<T> setOwner(Object owner) throws Exception {
		this.owner = owner;
		this.clazz = (Class<T>) owner.getClass();
		entityCache = session.getEntityCacheManager().getEntityCache(clazz);
		if (entityCache == null)
			throw new IdentifierException("Classe " + clazz.getName() + " não encontrada na lista de entidades.");
		return this;
	}

	public SQLSession getSession() {
		return session;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		Map<String, Object> primaryKey;
		try {
			primaryKey = getColumns();
			boolean append = false;
			sb.append("[");
			for (String key : primaryKey.keySet()) {
				if (append)
					sb.append(", ");
				sb.append(key).append("=").append(primaryKey.get(key));
				append = true;
			}
			sb.append("]");
		} catch (Exception e) {
		}
		return sb.toString();
	}

	public void setIdIfPossible(Object id) throws Exception {
		if (id == null) {
			throw new IdentifierException("Não é possível atribuir um valor nulo para o Identificador da classe "
					+ entityCache.getEntityClass().getName());
		}
		if (id instanceof Map) {
			for (Object fieldName : ((Map) id).keySet()) {
				DescriptionField descriptionField = entityCache.getDescriptionField(fieldName.toString());
				if (descriptionField == null) {
					throw new IdentifierException("Campo " + fieldName + " não encontrado na classe " + entityCache.getEntityClass().getName()
							+ ". Não foi possível atribuir o id para o Identificador.");
				}
				descriptionField.setObjectValue(owner, ((Map) id).get(fieldName));
			}
		} else {
			if (entityCache.getPrimaryKeyFields().length == 1) {
				Class<?> fieldClass = entityCache.getPrimaryKeyFields()[0].getFieldClass();
				if ((id.getClass() != fieldClass) && (!ReflectionUtils.isStrictlyAssignableFrom(id.getClass(), fieldClass))) {
					throw new IdentifierException("Objeto ID passado como parâmetro é do tipo " + id.getClass().getName() + " diferente do tipo ID ("
							+ fieldClass + ") do campo " + entityCache.getPrimaryKeyFields()[0].getField().getName() + "  encontrado na classe "
							+ entityCache.getEntityClass().getName() + ". Não foi possível atribuir o id para o Identificador.");
				}
				entityCache.getPrimaryKeyFields()[0].setObjectValue(owner, id);
			} else {
				throw new IdentifierException(
						"Objeto ID passado como parâmetro é do tipo "
								+ id.getClass().getName()
								+ " diferente do tipo ID  encontrado na classe "
								+ entityCache.getEntityClass().getName()
								+ ". Não foi possível atribuir o id para o Identificador. Use mapas de <Campo,Valor> ou um objeto compatível com o ID para criar um identificar. ");
			}

		}

	}

	public boolean isOnlyRefreshOwner() {
		return onlyRefreshOwner;
	}

	public void setOnlyRefreshOwner(boolean onlyRefreshOwner) {
		this.onlyRefreshOwner = onlyRefreshOwner;
	}
}
