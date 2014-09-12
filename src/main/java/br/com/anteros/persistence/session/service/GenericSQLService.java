package br.com.anteros.persistence.session.service;

import java.io.Serializable;
import java.util.List;

import br.com.anteros.core.utils.Assert;
import br.com.anteros.core.utils.TypeResolver;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.repository.Page;
import br.com.anteros.persistence.session.repository.Pageable;
import br.com.anteros.persistence.session.repository.SQLRepository;
import br.com.anteros.persistence.session.repository.impl.GenericSQLRepository;

public class GenericSQLService<T, ID extends Serializable> implements SQLService<T, ID> {

	protected SQLRepository<T, ID> repository;
	protected SQLSessionFactory sessionFactory;

	public GenericSQLService() {
	}

	public GenericSQLService(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		Class<?>[] typeArguments = TypeResolver.resolveRawArguments(GenericSQLService.class, getClass());
		if (typeArguments != null) {
			this.repository = new GenericSQLRepository<T, ID>(sessionFactory, typeArguments[0]);
		}
	}

	@Override
	public <S extends T> S save(S entity) {
		checkRepository();
		return repository.save(entity);
	}

	protected void checkRepository() {
		Assert.notNull(repository, "O repositório não foi criado. Verifique se a sessionFactory foi atribuida.");
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		checkRepository();
		return repository.save(entities);
	}

	@Override
	public <S extends T> S saveAndFlush(S entity) {
		checkRepository();
		return repository.saveAndFlush(entity);
	}

	@Override
	public void flush() {
		checkRepository();
		repository.flush();

	}

	@Override
	public T findOne(ID id) {
		checkRepository();
		return repository.findOne(id);
	}

	@Override
	public boolean exists(ID id) {
		checkRepository();
		return repository.exists(id);
	}

	@Override
	public List<T> findAll() {
		checkRepository();
		return repository.findAll();
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		checkRepository();
		return repository.findAll(pageable);
	}

	@Override
	public List<T> find(String sql) {
		checkRepository();
		return repository.find(sql);
	}

	@Override
	public Page<T> find(String sql, Pageable pageable) {
		checkRepository();
		return repository.find(sql, pageable);
	}

	@Override
	public List<T> find(String sql, Object parameters) {
		checkRepository();
		return repository.find(sql, parameters);
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable) {
		checkRepository();
		return repository.find(sql, parameters, pageable);
	}

	@Override
	public List<T> findByNamedQuery(String queryName) {
		checkRepository();
		return repository.findByNamedQuery(queryName);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Pageable pageable) {
		checkRepository();
		return repository.findByNamedQuery(queryName, pageable);
	}

	@Override
	public List<T> findByNamedQuery(String queryName, Object parameters) {
		checkRepository();
		return repository.findByNamedQuery(queryName, parameters);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable) {
		checkRepository();
		return repository.findByNamedQuery(queryName, parameters, pageable);
	}

	@Override
	public T findOne(Predicate predicate) {
		checkRepository();
		return repository.findOne(predicate);
	}

	@Override
	public List<T> findAll(Predicate predicate) {
		checkRepository();
		return repository.findAll(predicate);
	}

	@Override
	public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
		checkRepository();
		return repository.findAll(predicate, orders);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {
		checkRepository();
		return repository.findAll(predicate, pageable);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... orders) {
		checkRepository();
		return repository.findAll(predicate, pageable, orders);
	}

	@Override
	public void refresh(T entity) {
		checkRepository();
		repository.refresh(entity);
	}

	@Override
	public long count() {
		checkRepository();
		return repository.count();
	}

	@Override
	public long count(Predicate predicate) {
		checkRepository();
		return repository.count(predicate);
	}

	@Override
	public void remove(ID id) {
		checkRepository();
		repository.remove(id);
	}

	@Override
	public void remove(T entity) {
		checkRepository();
		repository.remove(entity);
	}

	@Override
	public void remove(Iterable<? extends T> entities) {
		checkRepository();
		repository.remove(entities);
	}

	@Override
	public void removeAll() {
		checkRepository();
		repository.removeAll();
	}

	public SQLSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		Class<?>[] typeArguments = TypeResolver.resolveRawArguments(GenericSQLService.class, getClass());
		if (typeArguments != null) {
			this.repository = new GenericSQLRepository<T, ID>(sessionFactory, typeArguments[0]);
		}
	}

	@Override
	public T findOne(String sql) {
		return this.repository.findOne(sql);
	}

	@Override
	public T findOne(String sql, Object parameters) {
		return this.repository.findOne(sql, parameters);
	}
}
