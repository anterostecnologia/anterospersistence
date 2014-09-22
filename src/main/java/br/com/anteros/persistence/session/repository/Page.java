package br.com.anteros.persistence.session.repository;

public interface Page<T> extends Slice<T> {

	int getTotalPages();

	long getTotalElements();
}
