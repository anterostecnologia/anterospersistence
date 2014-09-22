package br.com.anteros.persistence.session.repository.impl;

import java.io.Serializable;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.repository.AbstractSQLRepositoryFactory;
import br.com.anteros.persistence.session.repository.SQLRepository;

public class SQLRepositoryFactoryImpl extends AbstractSQLRepositoryFactory {
	

	@Override
	public <T, ID extends Serializable> SQLRepository<T, ID> getRepository(SQLSession session, Class<T> clazz) {
		return new GenericSQLRepository<T, ID>(session,clazz);
	}

}
