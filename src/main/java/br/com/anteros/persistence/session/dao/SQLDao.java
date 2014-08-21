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
package br.com.anteros.persistence.session.dao;

import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.handler.ResultSetHandler;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;

public abstract class SQLDao<T> {

	private Class<T> clazz;
	private boolean importTable = true;

	public SQLDao(Class<T> clazz) {
		this.clazz = clazz;
	}

	public abstract SQLSession getSession() throws Exception;

	public String getTableName() throws Exception {
		return getSession().getEntityCacheManager().getEntityCache(clazz).getTableName();
	}

	public boolean isImportTable() {
		return importTable;
	}

	public void setImportTable(boolean importTable) {
		this.importTable = importTable;
	}

	public T selectOne(String sql) throws Exception {
		return (T) getSession().createQuery(sql, clazz).getSingleResult();
	}

	public Object selectOne(String sql, Object[] parameter) throws Exception {
		return getSession().createQuery(sql, clazz, parameter);
	}

	public Object selectOne(String sql, Map<String, Object> namedParameter) throws Exception {
		return getSession().createQuery(sql, clazz, namedParameter).getSingleResult();
	}

	public Object selectOne(String sql, NamedParameter[] namedParameter) throws Exception {
		return getSession().createQuery(sql, clazz, namedParameter).getSingleResult();
	}

	public List<T> selectList(String sql) throws Exception {
		return (List<T>) getSession().createQuery(sql, clazz).getResultList();
	}

	public List<T> selectList(String sql, Object[] parameter) throws Exception {
		return (List<T>) getSession().createQuery(sql, clazz, parameter).getResultList();
	}

	public List<T> selectList(String sql, Map<String, Object> namedParameter) throws Exception {
		return (List<T>) getSession().createQuery(sql, clazz, namedParameter).getResultList();
	}

	public List<T> selectList(String sql, NamedParameter[] namedParameter) throws Exception {
		return (List<T>) getSession().createQuery(sql, clazz, namedParameter).getResultList();
	}

	public Object select(String sql, ResultSetHandler handler) throws Exception {
		return getSession().createQuery(sql).resultSetHandler(handler).getSingleResult();
	}

	public Object select(String sql, Object[] parameter, ResultSetHandler handler) throws Exception {
		return getSession().createQuery(sql, parameter).resultSetHandler(handler).getSingleResult();
	}

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler) throws Exception {
		return getSession().createQuery(sql, namedParameter).resultSetHandler(handler).getSingleResult();
	}

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler) throws Exception {
		return getSession().createQuery(sql, namedParameter).resultSetHandler(handler).getSingleResult();
	}

	public T selectId(Identifier<T> id) throws Exception {
		return getSession().find(id);
	}

	public Object save(Object object) throws Exception {
		return getSession().save(object);
	}

	public void save(Object[] object) throws Exception {
		getSession().save(object);
	}

	public void save(String[] columns, String[] values) throws Exception {
		getSession().save(clazz, columns, values);
	}

	public void remove(Object object) throws Exception {
		getSession().remove(object);
	}

	public void remove(Object[] object) throws Exception {
		getSession().remove(object);
	}

	public void removeAll() throws Exception {
		getSession().removeAll(clazz);
	}

	public void beginTransaction() throws Exception {
		getSession().getTransaction().begin();
	}

	public void commit() throws Exception {
		getSession().getTransaction().commit();
	}

	public void rollback() throws Exception {
		getSession().getTransaction().rollback();
	}

}
