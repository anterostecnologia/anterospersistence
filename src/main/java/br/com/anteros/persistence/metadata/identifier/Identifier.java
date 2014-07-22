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
package br.com.anteros.persistence.metadata.identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.command.Select;
import br.com.anteros.persistence.util.ReflectionUtils;

public class Identifier<T> {

	private Class<T> clazz;
	private Object owner;
	private SQLSession session;
	private EntityCache entityCache;

	public static <T> Identifier<T> create(SQLSession session, Class<T> sourceClass) throws Exception {
		return new Identifier<T>(session, sourceClass);
	}

	public static <T> Identifier<T> create(SQLSession session, T owner) throws Exception {
		return new Identifier<T>(session, owner);
	}

	public Identifier(SQLSession session, Class<T> sourceClass) throws Exception {
		if (ReflectionUtils.isAbstractClass(sourceClass)) {
			throw new IdentifierException("Não é possível criar um identificador para uma classe abstrata "
					+ sourceClass.getName());
		}
		entityCache = session.getEntityCacheManager().getEntityCache(sourceClass);
		if (entityCache == null) {
			throw new IdentifierException("Classe " + sourceClass.getName() + " não encontrada na lista de entidades.");
		}
		this.clazz = sourceClass;
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

	public Identifier<T> setFieldValue(String fieldName, Object value) throws Exception {
		DescriptionField descriptionField = entityCache.getDescriptionField(fieldName);
		if (descriptionField == null)
			throw new IdentifierException("Campo " + fieldName + " não encontrado na classe " + clazz.getName()
					+ ". Não foi possível atribuir o valor.");

		if ((value == null) || (value.getClass() == descriptionField.getField().getType())
				|| (descriptionField.getTargetEntity() == null)) {
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
			for (Object column : ((Map<?,?>) value).keySet()) {
				if (append)
					select.and();
				select.addCondition("" + column, "=", ":P" + column);
				params.add(new NamedParameter("P" + column, ((Map<?,?>) value).get(column)));
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
				select.addCondition("" + descriptionColumn.getColumnName(), "=",
						":P" + descriptionColumn.getColumnName());
				params.add(new NamedParameter("P" + descriptionColumn.getColumnName(), ((Object[]) value)[index]));
				append = true;
				index++;
			}
		} else {
			throw new IdentifierException("Tipo de parâmetro incorreto " + value.getClass()
					+ ". Não foi possível atribuir o valor para o campo " + descriptionField.getName() + " da classe "
					+ entityCache.getEntityClass().getName());
		}
		descriptionField.setObjectValue(owner, session.selectOne(select.toStatementString(),
				params.toArray(new NamedParameter[] {}), descriptionField.getField().getType()));
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
		StringBuffer sb = new StringBuffer("");
		Map<String, Object> primaryKey = new TreeMap<String,Object>(this.getColumns());
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
		StringBuffer sb = new StringBuffer("");
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
}