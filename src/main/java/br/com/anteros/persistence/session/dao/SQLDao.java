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

public class SQLDao<T> {
	private SQLSession session;
	private Class<T> clazz;
	private boolean importTable = true;

	public SQLDao(SQLSession session, Class<T> clazz) {
		this.clazz = clazz;
		this.session = session;
	}

	public String getTableName() {
		return session.getEntityCacheManager().getEntityCache(clazz).getTableName();
	}

	public boolean isImportTable() {
		return importTable;
	}

	public void setImportTable(boolean importTable) {
		this.importTable = importTable;
	}

	public T selectOne(String sql) throws Exception {
		return (T) session.selectOne(sql, clazz);
	}

	public Object selectOne(String sql, Object[] parameter) throws Exception {
		return session.selectOne(sql, parameter, clazz);
	}

	public Object selectOne(String sql, Map<String, Object> namedParameter) throws Exception {
		return session.selectOne(sql, namedParameter, clazz);
	}

	public Object selectOne(String sql, NamedParameter[] namedParameter) throws Exception {
		return session.selectOne(sql, namedParameter, clazz);
	}

	public List<T> selectList(String sql) throws Exception {
		return (List<T>) session.selectList(sql, clazz);
	}

	public List<T> selectList(String sql, Object[] parameter) throws Exception {
		return (List<T>) session.selectList(sql, parameter, clazz);
	}

	public List<T> selectList(String sql, Map<String, Object> namedParameter) throws Exception {
		return (List<T>) session.selectList(sql, namedParameter, clazz);
	}

	public List<T> selectList(String sql, NamedParameter[] namedParameter) throws Exception {
		return (List<T>) session.selectList(sql, namedParameter, clazz);
	}

	public Object select(String sql, ResultSetHandler handler) throws Exception {
		return session.select(sql, handler);
	}

	public Object select(String sql, Object[] parameter, ResultSetHandler handler) throws Exception {
		return session.select(sql, parameter, handler);
	}

	public Object select(String sql, Map<String, Object> namedParameter, ResultSetHandler handler) throws Exception {
		return session.select(sql, namedParameter, handler);
	}

	public Object select(String sql, NamedParameter[] namedParameter, ResultSetHandler handler) throws Exception {
		return session.select(sql, namedParameter, handler);
	}

	public T selectId(Identifier<T> id) throws Exception {
		return session.selectId(id);
	}

	public Object save(Object object) throws Exception {
		return session.save(object);
	}

	public void save(Object[] object) throws Exception {
		session.save(object);
	}

	public void save(String[] columns, String[] values) throws Exception {
		session.save(clazz, columns, values);
	}

	public void remove(Object object) throws Exception {
		session.remove(object);
	}

	public void remove(Object[] object) throws Exception {
		session.remove(object);
	}

	public void removeAll() throws Exception {
		session.removeAll(clazz);
	}

	public void beginTransaction() throws Exception {
		session.beginTransaction();
	}

	public void commit() throws Exception {
		session.commit();
	}

	public void rollback() throws Exception {
		session.rollback();
	}

	public SQLSession getSession() {
		return session;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

}
