package br.com.anteros.persistence.session.context;

import java.io.Serializable;

import br.com.anteros.persistence.session.SQLSession;

public interface CurrentSQLSessionContext extends Serializable {
	
	public SQLSession currentSession() throws Exception;

}