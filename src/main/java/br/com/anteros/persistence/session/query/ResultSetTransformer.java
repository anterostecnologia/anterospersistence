package br.com.anteros.persistence.session.query;

public interface ResultSetTransformer<T> {

	T newInstance(Object... args);
}
