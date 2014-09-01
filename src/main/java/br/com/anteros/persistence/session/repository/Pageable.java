package br.com.anteros.persistence.session.repository;

public interface Pageable {

	int getPageNumber();

	int getPageSize();

	int getOffset();

	Pageable next();

	Pageable previousOrFirst();

	Pageable first();

	boolean hasPrevious();
}
