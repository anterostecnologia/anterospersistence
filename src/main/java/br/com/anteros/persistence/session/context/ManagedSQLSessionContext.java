package br.com.anteros.persistence.session.context;

import java.util.HashMap;
import java.util.Map;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.exception.SQLSessionException;

public class ManagedSQLSessionContext implements CurrentSQLSessionContext {

	private static final long serialVersionUID = 1L;

	private static final ThreadLocal<Map<SQLSessionFactory, SQLSession>> context = new ThreadLocal<Map<SQLSessionFactory, SQLSession>>();
	
	private final SQLSessionFactory factory;

	public ManagedSQLSessionContext(SQLSessionFactory factory) {
		this.factory = factory;
	}

	public SQLSession currentSession() {
		SQLSession current = existingSession( factory );
		if ( current == null ) {
			throw new SQLSessionException( "No session currently bound to execution context" );
		}
		return current;
	}

	public static boolean hasBind(SQLSessionFactory factory) {
		return existingSession( factory ) != null;
	}

	public static SQLSession bind(SQLSession session) {
		return sessionMap( true ).put( session.getSQLSessionFactory(), session );
	}

	public static SQLSession unbind(SQLSessionFactory factory) {
		SQLSession existing = null;
		Map<SQLSessionFactory, SQLSession> sessionMap = sessionMap();
		if ( sessionMap != null ) {
			existing = sessionMap.remove( factory );
			doCleanup();
		}
		return existing;
	}

	private static SQLSession existingSession(SQLSessionFactory factory) {
		Map<SQLSessionFactory, SQLSession> sessionMap = sessionMap();
		if ( sessionMap == null ) {
			return null;
		}
		else {
			return sessionMap.get( factory );
		}
	}

	protected static Map<SQLSessionFactory, SQLSession> sessionMap() {
		return sessionMap( false );
	}

	private static synchronized Map<SQLSessionFactory, SQLSession> sessionMap(boolean createMap) {
		Map<SQLSessionFactory, SQLSession> sessionMap = context.get();
		if ( sessionMap == null && createMap ) {
			sessionMap = new HashMap<SQLSessionFactory, SQLSession>();
			context.set( sessionMap );
		}
		return sessionMap;
	}

	private static synchronized void doCleanup() {
		Map<SQLSessionFactory, SQLSession> sessionMap = sessionMap( false );
		if ( sessionMap != null ) {
			if ( sessionMap.isEmpty() ) {
				context.set( null );
			}
		}
	}
}

