package br.com.anteros.persistence.session.repository;

import java.io.Serializable;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.repository.impl.SQLRepositoryFactoryImpl;

public abstract class AbstractSQLRepositoryFactory {

	private static AbstractSQLRepositoryFactory factory;

	public static AbstractSQLRepositoryFactory getInstance() {
		if (factory == null) {
			factory = new SQLRepositoryFactoryImpl();
		}
		return factory;
	}

	public abstract <T, ID extends Serializable> SQLRepository<T, ID> getRepository(SQLSession session, Class<T> clazz);
}
