package br.com.anteros.persistence.session.repository;

import java.io.Serializable;
import java.util.List;

import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.transaction.Transaction;

public interface SQLRepository<T, ID extends Serializable> {

	<S extends T> S save(S entity);

	<S extends T> Iterable<S> save(Iterable<S> entities);

	<S extends T> S saveAndFlush(S entity);

	void flush();

	T findOne(ID id);

	T findOne(String sql);

	T findOne(String sql, Object parameters);
	
	T findOne(ID id, boolean readOnly);

	T findOne(String sql, boolean readOnly);

	T findOne(String sql, Object parameters, boolean readOnly);

	boolean exists(ID id);

	List<T> findAll();

	Page<T> findAll(Pageable pageable);
	
	List<T> findAll(boolean readOnly);

	Page<T> findAll(Pageable pageable, boolean readOnly);

	List<T> find(String sql);

	Page<T> find(String sql, Pageable pageable);

	List<T> find(String sql, Object parameters);

	Page<T> find(String sql, Object parameters, Pageable pageable);

	List<T> find(String sql, boolean readOnly);

	Page<T> find(String sql, Pageable pageable, boolean readOnly);

	List<T> find(String sql, Object parameters, boolean readOnly);

	Page<T> find(String sql, Object parameters, Pageable pageable, boolean readOnly);

	List<T> findByNamedQuery(String queryName);

	Page<T> findByNamedQuery(String queryName, Pageable pageable);

	List<T> findByNamedQuery(String queryName, Object parameters);

	Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable);
	
	List<T> findByNamedQuery(String queryName, boolean readOnly);

	Page<T> findByNamedQuery(String queryName, Pageable pageable, boolean readOnly);

	List<T> findByNamedQuery(String queryName, Object parameters, boolean readOnly);

	Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable, boolean readOnly);


	T findOne(Predicate predicate);

	List<T> findAll(Predicate predicate);

	Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

	Page<T> findAll(Predicate predicate, Pageable pageable);

	Page<T> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... orders);

	SQLSession getSession();

	void setSession(SQLSession session);

	SQLSession openSession() throws Exception;

	SQLSessionFactory getSQLSessionFactory() throws Exception;

	void refresh(T entity);

	long count();

	long count(Predicate predicate);

	void remove(ID id);

	void remove(T entity);

	void remove(Iterable<? extends T> entities);

	void removeAll();

	Transaction getTransaction() throws Exception;

	Identifier<T> createIdentifier() throws Exception;

	Identifier<T> getIdentifier(T owner) throws Exception;

}