/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.repository.impl;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.anteros.core.utils.Assert;
import br.com.anteros.core.utils.TypeResolver;
import br.com.anteros.persistence.dsl.osql.EntityPathResolver;
import br.com.anteros.persistence.dsl.osql.OSQLQuery;
import br.com.anteros.persistence.dsl.osql.SimpleEntityPathResolver;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.path.PathBuilder;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionNamedQuery;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.session.query.TypedSQLQuery;
import br.com.anteros.persistence.session.repository.Page;
import br.com.anteros.persistence.session.repository.Pageable;
import br.com.anteros.persistence.session.repository.SQLRepository;
import br.com.anteros.persistence.session.repository.SQLRepositoryException;
import br.com.anteros.persistence.transaction.Transaction;

@SuppressWarnings("unchecked")
public class GenericSQLRepository<T, ID extends Serializable> implements SQLRepository<T, ID> {

	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;
	public static final String COUNT_QUERY_STRING = "select count(*) from %s x";
	public static final String DELETE_ALL_QUERY_STRING = "delete from %s x";

	protected SQLSession session;
	protected SQLSessionFactory sessionFactory;
	protected Class<?> persistentClass;
	protected EntityPath<T> path;
	protected PathBuilder<T> builder;

	public GenericSQLRepository(SQLSession session) {
		this.session = session;
		Class<?>[] typeArguments = TypeResolver.resolveRawArguments(GenericSQLRepository.class, getClass());
		if (typeArguments != null) {
			this.persistentClass = typeArguments[0];
		}
	}

	public GenericSQLRepository(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		Class<?>[] typeArguments = TypeResolver.resolveRawArguments(GenericSQLRepository.class, getClass());
		if (typeArguments != null) {
			this.persistentClass = typeArguments[0];
		}
	}

	public GenericSQLRepository(SQLSession session, Class<?> type) {
		this.session = session;
		this.persistentClass = type;
	}

	public GenericSQLRepository(SQLSessionFactory sessionFactory, Class<?> type) {
		this.sessionFactory = sessionFactory;
		this.persistentClass = type;
	}

	@Override
	public <S extends T> S save(S entity) {
		try {
			return (S) getSession().save(entity);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		List<S> result = new ArrayList<S>();

		if (entities == null) {
			return result;
		}

		for (S entity : entities) {
			result.add(save(entity));
		}

		return result;
	}

	@Override
	public <S extends T> S saveAndFlush(S entity) {
		S result = save(entity);
		flush();

		return result;
	}

	@Override
	public void flush() {
		try {
			getSession().flush();
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public T findOne(ID id, LockOptions lockOptions, boolean readOnly) {
		Assert.notNull(id, "O id não pode ser nulo.");

		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		try {
			return (T) getSession().find(persistentClass, id, lockOptions, readOnly);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public T findOne(String sql, LockOptions lockOptions, boolean readOnly) {
		List<T> result = find(sql, readOnly);
		if ((result != null) && (result.size() > 0))
			return result.get(0);
		return null;
	}

	@Override
	public T findOne(String sql, Object parameters, LockOptions lockOptions, boolean readOnly) {
		List<T> result = find(sql, parameters, readOnly);
		if ((result != null) && (result.size() > 0))
			return result.get(0);
		return null;
	}

	@Override
	public boolean exists(ID id) {
		Assert.notNull(id, "O id não pode ser nulo.");

		try {
			return (findOne(id, true) != null);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> findAll(LockOptions lockOptions, boolean readOnly) {
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		try {
			TypedSQLQuery<?> query = getSession().createQuery("select * from " + getEntityCache().getTableName(), persistentClass);
			query.setLockOptions(lockOptions);
			query.setReadOnly(readOnly);
			return (List<T>) query.getResultList();
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	protected EntityCache getEntityCache() {
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		return getSession().getEntityCacheManager().getEntityCache(persistentClass);
	}

	@Override
	public Page<T> findAll(Pageable pageable, LockOptions lockOptions, boolean readOnly) {

		if (null == pageable) {
			return new PageImpl<T>(findAll());
		}

		TypedSQLQuery<?> query;
		try {
			query = getSession().createQuery("select * from " + getEntityCache().getTableName(), persistentClass);

			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
			query.setReadOnly(readOnly);
			query.setLockOptions(lockOptions);

			Long total = count();
			List<T> content = (List<T>) (total > pageable.getOffset() ? query.getResultList() : Collections.<T> emptyList());

			return new PageImpl<T>(content, pageable, total);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> find(String sql, LockOptions lockOptions, boolean readOnly) {
		try {
			TypedSQLQuery<?> query = getSession().createQuery(sql, persistentClass);
			query.setReadOnly(readOnly);
			query.setLockOptions(lockOptions);
			return (List<T>) query.getResultList();
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public Page<T> find(String sql, Pageable pageable, LockOptions lockOptions, boolean readOnly) {
		if (null == pageable) {
			return new PageImpl<T>(find(sql));
		}

		TypedSQLQuery<?> query;
		try {
			query = getSession().createQuery(sql, persistentClass);
			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
			query.setReadOnly(readOnly);
			query.setLockOptions(lockOptions);

			Long total = doCount(getCountQueryString("(" + sql + ")"));
			List<T> content = (List<T>) (total > pageable.getOffset() ? query.getResultList() : Collections.<T> emptyList());

			return new PageImpl<T>(content, pageable, total);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> find(String sql, Object parameters, LockOptions lockOptions, boolean readOnly) {
		try {
			TypedSQLQuery<?> query = getSession().createQuery(sql, persistentClass, parameters);
			query.setReadOnly(readOnly);
			query.setLockOptions(lockOptions);
			return (List<T>) query.getResultList();
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable, LockOptions lockOptions, boolean readOnly) {
		if (null == pageable) {
			return new PageImpl<T>(find(sql, parameters));
		}

		TypedSQLQuery<?> query;
		try {
			query = getSession().createQuery(sql, persistentClass);
			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize()); 
			query.setParameters(parameters);
			query.setReadOnly(readOnly);
			query.setLockOptions(lockOptions);
			Long total = doCount(getCountQueryString("(" + sql + ")"), parameters);
			List<T> content = (List<T>) (total > pageable.getOffset() ? query.getResultList() : Collections.<T> emptyList());

			return new PageImpl<T>(content, pageable, total);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> findByNamedQuery(String queryName, boolean readOnly) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		return find(namedQuery.getQuery(), namedQuery.getLockOptions(), readOnly);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Pageable pageable, boolean readOnly) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		return find(namedQuery.getQuery(), pageable, namedQuery.getLockOptions(), readOnly);

	}

	@Override
	public List<T> findByNamedQuery(String queryName, Object parameters, boolean readOnly) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		return find(namedQuery.getQuery(), parameters, namedQuery.getLockOptions(), readOnly);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable, boolean readOnly) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		return find(namedQuery.getQuery(), parameters, pageable, namedQuery.getLockOptions(), readOnly);

	}

	@Override
	public T findOne(Predicate predicate) {
		return createQuery(predicate).uniqueResult(getEntityPath());
	}

	private EntityPath<T> getEntityPath() {
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		if (path == null)
			this.path = (EntityPath<T>) DEFAULT_ENTITY_PATH_RESOLVER.createPath(persistentClass);
		return path;
	}

	protected PathBuilder<T> getPathBuilder() {
		if (builder == null)
			this.builder = new PathBuilder<T>(getEntityPath().getType(), path.getMetadata());
		return builder;
	}

	@Override
	public List<T> findAll(Predicate predicate) {
		return createQuery(predicate).list(getEntityPath());
	}

	@Override
	public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
		OSQLQuery query = createQuery(predicate).orderBy(orders);
		return query.list(getEntityPath());
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {

		OSQLQuery countQuery = createQuery(predicate);
		Long total = countQuery.count();

		OSQLQuery query = createQuery(predicate);
		query.offset(pageable.getOffset());
		query.limit(pageable.getPageSize());

		List<T> content = total > pageable.getOffset() ? query.list(path) : Collections.<T> emptyList();

		return new PageImpl<T>(content, pageable, total);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... orders) {
		OSQLQuery countQuery = createQuery(predicate);
		Long total = countQuery.count();

		OSQLQuery query = createQuery(predicate);
		query.offset(pageable.getOffset());
		query.limit(pageable.getPageSize());
		query.orderBy(orders);

		List<T> content = total > pageable.getOffset() ? query.list(path) : Collections.<T> emptyList();

		return new PageImpl<T>(content, pageable, total);
	}

	@Override
	public SQLSession getSession() {
		if (session != null)
			return session;
		else if (sessionFactory != null) {
			try {
				return sessionFactory.getCurrentSession();
			} catch (Exception e) {
				throw new SQLRepositoryException(e);
			}
		}
		throw new SQLRepositoryException("Não foi configurado nenhuma SQLSession ou SQLSessionFactory para o repositório.");
	}

	@Override
	public void refresh(T entity, LockOptions lockOptions) {
		try {
			getSession().refresh(entity);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public long count() {
		return doCount(getCountQueryString(getEntityCache().getTableName()));
	}

	protected long doCount(String countSql) {
		try {
			ResultSet rs = getSession().createQuery(countSql).executeQuery();
			rs.next();
			Long value = rs.getLong(1);
			rs.close();
			return value;
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	protected long doCount(String countSql, Object parameters) {
		try {
			ResultSet rs = getSession().createQuery(countSql).setParameters(parameters).executeQuery();
			rs.next();
			Long value = rs.getLong(1);
			rs.close();
			return value;
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public long count(Predicate predicate) {
		return createQuery(predicate).count();
	}

	@Override
	public void remove(ID id) {
		try {
			Assert.notNull(id, "O id não pode ser nulo.");
			Assert.notNull(
					persistentClass,
					"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

			T entity = findOne(id);
			if (entity == null) {
				throw new SQLRepositoryException(String.format("Não foi encontrada nenhuma entidade %s com o id %s.", persistentClass, id));
			}
			remove(entity);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public void remove(T entity) {
		try {
			getSession().remove(entity);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public void remove(Iterable<? extends T> entities) {
		Assert.notNull(entities, "A lista de entidades não pode ser nula.");

		for (T entity : entities) {
			remove(entity);
		}
	}

	@Override
	public void removeAll() {
		for (T element : findAll()) {
			remove(element);
		}
	}

	public SQLSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setSession(SQLSession session) {
		this.session = session;
	}

	protected OSQLQuery createQuery(Predicate... predicate) {
		OSQLQuery query = new OSQLQuery(session).from(getEntityPath()).where(predicate);
		return query;
	}

	private String getCountQueryString(String tableName) {
		return String.format(COUNT_QUERY_STRING, tableName);
	}

	public Class<?> getPersistentClass() {
		return persistentClass;
	}

	public void setPersistentClass(Class<?> persistentClass) {
		this.persistentClass = persistentClass;
	}

	@Override
	public Transaction getTransaction() throws Exception {
		return getSession().getTransaction();
	}

	@Override
	public Identifier<T> createIdentifier() throws Exception {
		return (Identifier<T>) getSession().createIdentifier(getPersistentClass());
	}

	@Override
	public Identifier<T> getIdentifier(T owner) throws Exception {
		return getSession().getIdentifier(owner);
	}

	@Override
	public SQLSession openSession() throws Exception {
		if (sessionFactory == null)
			throw new SQLRepositoryException("Nenhuma fábrica de sessões foi atribuída ao repositório não é possível criar uma nova sessão SQL.");
		return sessionFactory.openSession();
	}

	@Override
	public SQLSessionFactory getSQLSessionFactory() throws Exception {
		return sessionFactory;
	}

	@Override
	public List<T> find(String sql, LockOptions lockOptions) {
		return find(sql, false);
	}

	@Override
	public Page<T> find(String sql, Pageable pageable, LockOptions lockOptions) {
		return find(sql, pageable, false);
	}

	@Override
	public List<T> find(String sql, Object parameters, LockOptions lockOptions) {
		return find(sql, parameters, false);
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable, LockOptions lockOptions) {
		return find(sql, parameters, pageable, false);
	}

	@Override
	public T findOne(ID id, LockOptions lockOptions) {
		return findOne(id, lockOptions, false);
	}

	@Override
	public T findOne(String sql, LockOptions lockOptions) {
		return findOne(sql, lockOptions, false);
	}

	@Override
	public T findOne(String sql, Object parameters, LockOptions lockOptions) {
		return findOne(sql, parameters, lockOptions, false);
	}

	@Override
	public List<T> findAll(LockOptions lockOptions) {
		return findAll(lockOptions, false);
	}

	@Override
	public Page<T> findAll(Pageable pageable, LockOptions lockOptions) {
		return findAll(pageable, lockOptions, false);
	}

	@Override
	public T findOne(ID id) {
		return findOne(id, LockOptions.NONE);
	}

	@Override
	public T findOne(String sql) {
		return findOne(sql, LockOptions.NONE);
	}

	@Override
	public T findOne(String sql, Object parameters) {
		return findOne(sql, parameters, LockOptions.NONE);
	}

	@Override
	public T findOne(ID id, boolean readOnly) {
		return findOne(id, LockOptions.NONE, readOnly);
	}

	@Override
	public T findOne(String sql, boolean readOnly) {
		return findOne(sql, LockOptions.NONE, readOnly);
	}

	@Override
	public T findOne(String sql, Object parameters, boolean readOnly) {
		return findOne(sql, parameters, LockOptions.NONE, readOnly);
	}

	@Override
	public List<T> findAll() {
		return findAll(LockOptions.NONE);
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		return findAll(pageable, LockOptions.NONE);
	}

	@Override
	public List<T> findAll(boolean readOnly) {
		return findAll(LockOptions.NONE, readOnly);
	}

	@Override
	public Page<T> findAll(Pageable pageable, boolean readOnly) {
		return findAll(pageable, LockOptions.NONE, readOnly);
	}

	@Override
	public List<T> find(String sql) {
		return find(sql, LockOptions.NONE);
	}

	@Override
	public Page<T> find(String sql, Pageable pageable) {
		return find(sql, pageable, LockOptions.NONE);
	}

	@Override
	public List<T> find(String sql, Object parameters) {
		return find(sql, parameters, LockOptions.NONE);
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable) {
		return find(sql, parameters, pageable, LockOptions.NONE);
	}

	@Override
	public List<T> find(String sql, boolean readOnly) {
		return find(sql, LockOptions.NONE, readOnly);
	}

	@Override
	public Page<T> find(String sql, Pageable pageable, boolean readOnly) {
		return find(sql, pageable, LockOptions.NONE, readOnly);
	}

	@Override
	public List<T> find(String sql, Object parameters, boolean readOnly) {
		return find(sql, parameters, LockOptions.NONE, readOnly);
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable, boolean readOnly) {
		return find(sql, parameters, pageable, LockOptions.NONE, readOnly);
	}

	@Override
	public void refresh(T entity) {
		refresh(entity, LockOptions.NONE);
	}

	@Override
	public List<T> findByNamedQuery(String queryName) {
		return findByNamedQuery(queryName, false);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Pageable pageable) {
		return findByNamedQuery(queryName, pageable, false);
	}

	@Override
	public List<T> findByNamedQuery(String queryName, Object parameters) {
		return findByNamedQuery(queryName, parameters, false);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable) {
		return findByNamedQuery(queryName, parameters, pageable, false);
	}

	@Override
	public OSQLQuery createObjectQuery() {
		return new OSQLQuery(session);
	}

}