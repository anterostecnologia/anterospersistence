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
import br.com.anteros.persistence.session.SQLSessionFactory;

public class SQLDao<T> {
	private SQLSessionFactory sessionFactory;
	private Class<T> clazz;
	private boolean importTable = true;

	public SQLDao(SQLSessionFactory sessionFactory, Class<T> clazz) {
		this.clazz = clazz;
		this.sessionFactory = sessionFactory;
	}

	public String getTableName() throws Exception {
		return sessionFactory.getCurrentSession().getEntityCacheManager().getEntityCache(clazz).getTableName();
	}

	public boolean isImportTable() {
		return importTable;
	}

	public void setImportTable(boolean importTable) {
		this.importTable = importTable;
	}

	public T selectOne(String sql) throws Exception {
		return (T) sessionFactory.getCurrentSession().selectOne(sql, clazz);
	}

	public Object selectOne(String sql, Object[] parameter) throws Exception {
		return sessionFactory.getCurrentSession().selectOne(sql, parameter, clazz);
	}

	public Object selectOne(String sql, Map<String, Object> namedParameter) throws Exception {
		return sessionFactory.getCurrentSession().selectOne(sql, namedParameter, clazz);
	}

	public Object selectOne(String sql, NamedParameter[] namedParameter) throws Exception {
		return sessionFactory.getCurrentSession().selectOne(sql, namedParameter, clazz);
	}

	public List<T> selectList(String sql) throws Exception {
		return (List<T>) sessionFactory.getCurrentSession().selectList(sql, clazz);
	}

	public List<T> selectList(String sql, Object[] parameter) throws Exception {
		return (List<T>) sessionFactory.getCurrentSession().selectList(sql, parameter, clazz);
	}

	public List<T> selectList(String sql, Map<String, Object> namedParameter) throws Exception {
		return (List<T>) sessionFactory.getCurrentSession().selectList(sql, namedParameter, clazz);
	}

	public List<T> selectList(String sql, NamedParameter[] namedParameter) throws Exception {
		return (List<T>) sessionFactory.getCurrentSession().selectList(sql, namedParameter, clazz);
	}

	public Object select(String sql, ResultSetHandler handler) throws Exception {
		return sessionFactory.getCurrentSession().select(sql, handler);
	}

	public Object select(String sql, Object[] parameter, ResultSetHandler handler) throws Exception {
		return sessionFactory.getCurrentSession().select(sql, parameter, handler);
	}

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler) throws Exception {
		return sessionFactory.getCurrentSession().select(sql, namedParameter, handler);
	}

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler) throws Exception {
		return sessionFactory.getCurrentSession().select(sql, namedParameter, handler);
	}

	public T selectId(Identifier<T> id) throws Exception {
		return sessionFactory.getCurrentSession().selectId(id);
	}

	public Object save(Object object) throws Exception {
		return sessionFactory.getCurrentSession().save(object);
	}

	public void save(Object[] object) throws Exception {
		sessionFactory.getCurrentSession().save(object);
	}

	public void save(String[] columns, String[] values) throws Exception {
		sessionFactory.getCurrentSession().save(clazz, columns, values);
	}

	public void remove(Object object) throws Exception {
		sessionFactory.getCurrentSession().remove(object);
	}

	public void remove(Object[] object) throws Exception {
		sessionFactory.getCurrentSession().remove(object);
	}

	public void removeAll() throws Exception {
		sessionFactory.getCurrentSession().removeAll(clazz);
	}

	public void beginTransaction() throws Exception {
		sessionFactory.getCurrentSession().getTransaction().begin();
	}

	public void commit() throws Exception {
		sessionFactory.getCurrentSession().getTransaction().commit();
	}

	public void rollback() throws Exception {
		sessionFactory.getCurrentSession().getTransaction().rollback();
	}

	public SQLSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

}
