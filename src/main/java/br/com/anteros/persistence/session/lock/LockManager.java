package br.com.anteros.persistence.session.lock;

import br.com.anteros.persistence.session.SQLSession;

public interface LockManager {

	public void lock(SQLSession session, Object entity, LockOptions lockOptions) throws Exception;
	
	public String applyLock(SQLSession session, String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception;

}
