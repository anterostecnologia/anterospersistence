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
package br.com.anteros.persistence.sql.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.descriptor.DescriptionSQL;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.util.AnterosPersistenceTranslate;

public abstract class CommandSQL {

	protected String sql;
	protected List<NamedParameter> namedParameters = new ArrayList<NamedParameter>();
	protected SQLSession session;
	protected Object targetObject;
	protected boolean showSql;
	protected EntityCache entityCache;
	protected Serializable generatedId;
	protected SQLQueryRunner queryRunner = new SQLQueryRunner();
	protected String targetTableName;
	protected DescriptionSQL descriptionSQL;

	public CommandSQL(SQLSession session, String sql, List<NamedParameter> namedParameters, Object targetObject,
			EntityCache entityCache, String targetTableName, boolean showSql, DescriptionSQL descriptionSQL) {
		this.sql = sql;
		this.namedParameters = namedParameters;
		this.session = session;
		this.targetObject = targetObject;
		this.showSql = showSql;
		this.entityCache = entityCache;
		this.targetTableName = targetTableName;
		this.descriptionSQL = descriptionSQL;
	}

	public abstract void execute() throws Exception;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public SQLSession getSession() {
		return session;
	}

	public void setSession(SQLSession session) {
		this.session = session;
	}

	public Object getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public Serializable getGeneratedId() {
		return generatedId;
	}

	public void setGeneratedId(Serializable generatedId) {
		this.generatedId = generatedId;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	public void setEntityManaged() throws Exception {
		if (targetObject != null) {
			/*
			 * Adiciona o objeto na lista de entidades gerenciadas
			 */
			EntityManaged entityManaged = session.getPersistenceContext().addEntityManaged(targetObject, false,
					isNewEntity());
			/*
			 * Atualiza a lista de "últimos Valores" (lastValues)
			 */
			entityManaged.updateLastValues(session, targetObject);
		}
	}

	public String getObjectId() throws Exception {
		StringBuilder sb = new StringBuilder();
		if (entityCache != null) {
			Map<String, Object> primaryKey = session.getIdentifier(targetObject).getDatabaseColumns();
			for (String key : primaryKey.keySet())
				sb.append(key).append("=").append(primaryKey.get(key)).append(" ");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return AnterosPersistenceTranslate.getMessage(CommandSQL.class, "toString", sql, namedParameters, targetTableName);
	}

	public List<NamedParameter> getNamedParameters() {
		return namedParameters;
	}

	public void setNamedParameters(List<NamedParameter> params) {
		this.namedParameters = params;
	}

	public NamedParameter getNamedParameter(String name) {
		for (NamedParameter parameter : namedParameters) {
			if (parameter.getName().equals(name))
				return parameter;
		}
		return null;
	}

	public DescriptionSQL getDescriptionSQL() {
		return descriptionSQL;
	}

	public void setDescriptionSQL(DescriptionSQL descriptionSQL) {
		this.descriptionSQL = descriptionSQL;
	}

	public abstract boolean isNewEntity();

}
