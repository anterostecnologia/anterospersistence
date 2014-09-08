package br.com.anteros.persistence.session.repository;

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
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.query.SQLQueryException;
import br.com.anteros.persistence.session.query.TypedSQLQuery;

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
	public T findOne(ID id) {
		Assert.notNull(id, "O id não pode ser nulo.");

		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		try {
			return (T) getSession().find(persistentClass, id);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public boolean exists(ID id) {
		Assert.notNull(id, "O id não pode ser nulo.");

		try {
			return (findOne(id) != null);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> findAll() {
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		try {
			return (List<T>) getSession().createQuery("select * from " + getEntityCache().getTableName(),
					persistentClass).getResultList();
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
	public Page<T> findAll(Pageable pageable) {

		if (null == pageable) {
			return new PageImpl<T>(findAll());
		}

		TypedSQLQuery<?> query;
		try {
			query = getSession().createQuery("select * from " + getEntityCache().getTableName(), persistentClass);

			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());

			Long total = count();
			List<T> content = (List<T>) (total > pageable.getOffset() ? query.getResultList() : Collections
					.<T> emptyList());

			return new PageImpl<T>(content, pageable, total);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> find(String sql) {
		try {
			return (List<T>) getSession().createQuery(sql, persistentClass).getResultList();
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public Page<T> find(String sql, Pageable pageable) {
		if (null == pageable) {
			return new PageImpl<T>(find(sql));
		}

		TypedSQLQuery<?> query;
		try {
			query = getSession().createQuery(sql, persistentClass);
			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());

			Long total = doCount(getCountQueryString("(" + sql + ")"));
			List<T> content = (List<T>) (total > pageable.getOffset() ? query.getResultList() : Collections
					.<T> emptyList());

			return new PageImpl<T>(content, pageable, total);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> find(String sql, Object parameters) {
		try {
			return (List<T>) getSession().createQuery(sql, persistentClass, parameters).getResultList();
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable) {
		if (null == pageable) {
			return new PageImpl<T>(find(sql, parameters));
		}

		TypedSQLQuery<?> query;
		try {
			query = getSession().createQuery(sql, persistentClass);
			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
			query.setParameters(parameters);

			Long total = doCount(getCountQueryString("(" + sql + ")"), parameters);
			List<T> content = (List<T>) (total > pageable.getOffset() ? query.getResultList() : Collections
					.<T> emptyList());

			return new PageImpl<T>(content, pageable, total);
		} catch (Exception e) {
			throw new SQLRepositoryException(e);
		}
	}

	@Override
	public List<T> findByNamedQuery(String queryName) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		String sql = namedQuery.getQuery();
		return find(sql);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Pageable pageable) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		String sql = namedQuery.getQuery();
		return find(sql, pageable);

	}

	@Override
	public List<T> findByNamedQuery(String queryName, Object parameters) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		String sql = namedQuery.getQuery();
		return find(sql, parameters);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable) {
		Assert.notNull(queryName, "O nome da query não pode ser nulo.");
		Assert.notNull(
				persistentClass,
				"A classe de persistência não foi informada. Verifique se usou a classe GenericSQLRepository diretamente, se usou será necessário passar a classe de persistência como parâmetro. Se preferir pode extender a classe GenericSQLRepository e definir os parâmetros do genérics da classe.");

		EntityCache cache = session.getEntityCacheManager().getEntityCache(persistentClass);
		DescriptionNamedQuery namedQuery = cache.getDescriptionNamedQuery(queryName);
		if (namedQuery == null)
			throw new SQLQueryException("Query nomeada " + queryName + " não encontrada.");
		String sql = namedQuery.getQuery();
		return find(sql, parameters, pageable);

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
		if (sessionFactory != null)
			try {
				return sessionFactory.getCurrentSession();
			} catch (Exception e) {
				throw new SQLRepositoryException(e);
			}
		else
			return session;
	}

	@Override
	public void refresh(T entity) {
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
				throw new SQLRepositoryException(String.format("Não foi encontrada nenhuma entidade %s com o id %s.",
						persistentClass, id));
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

}