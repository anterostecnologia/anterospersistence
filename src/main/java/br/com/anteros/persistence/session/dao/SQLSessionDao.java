package br.com.anteros.persistence.session.dao;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;

public class SQLSessionDao<T> extends SQLDao<T> {

	private SQLSession session;

	public SQLSessionDao(Class<T> clazz, SQLSession session) {
		super(clazz);
		this.session = session;
	}

	@Override
	public SQLSession getSession() throws Exception {
		return session;
	}

}
