package br.com.anteros.persistence.session.service;

import java.io.Serializable;
import java.util.List;

import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.session.repository.Page;
import br.com.anteros.persistence.session.repository.Pageable;

public interface SQLService<T, ID extends Serializable> {

	<S extends T> S save(S entity);

	<S extends T> Iterable<S> save(Iterable<S> entities);

	<S extends T> S saveAndFlush(S entity);

	void flush();

	T findOne(ID id);

	boolean exists(ID id);

	List<T> findAll();

	Page<T> findAll(Pageable pageable);

	List<T> find(String sql);

	Page<T> find(String sql, Pageable pageable);

	List<T> find(String sql, Object parameters);

	Page<T> find(String sql, Object parameters, Pageable pageable);

	List<T> findByNamedQuery(String queryName);

	Page<T> findByNamedQuery(String queryName, Pageable pageable);

	List<T> findByNamedQuery(String queryName, Object parameters);

	Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable);

	T findOne(Predicate predicate);

	T findOne(String sql);

	T findOne(String sql, Object parameters);

	List<T> findAll(Predicate predicate);

	Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

	Page<T> findAll(Predicate predicate, Pageable pageable);

	Page<T> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... orders);

	void refresh(T entity);

	long count();

	long count(Predicate predicate);

	void remove(ID id);

	void remove(T entity);

	void remove(Iterable<? extends T> entities);

	void removeAll();
}
