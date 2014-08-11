package br.com.anteros.persistence.session.dao;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;

public class SQLSessionFactoryDao<T> extends SQLDao<T> {

	private SQLSessionFactory factory;

	public SQLSessionFactoryDao(Class<T> clazz, SQLSessionFactory factory) {
		super(clazz);
		this.factory = factory;
	}

	@Override
	public SQLSession getSession() throws Exception {
		return factory.getCurrentSession();
	}

}
