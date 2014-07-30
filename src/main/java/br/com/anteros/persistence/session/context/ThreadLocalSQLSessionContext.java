package br.com.anteros.persistence.session.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Synchronization;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.SQLSessionListener;
import br.com.anteros.persistence.transaction.AnterosSynchronization;

public class ThreadLocalSQLSessionContext implements CurrentSQLSessionContext {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerProvider.getInstance().getLogger(ThreadLocalSQLSessionContext.class.getName());

	private static final ThreadLocal<Map<SQLSessionFactory, SQLSession>> context = new ThreadLocal<Map<SQLSessionFactory, SQLSession>>();

	protected final SQLSessionFactory factory;

	public ThreadLocalSQLSessionContext(SQLSessionFactory factory) {
		this.factory = factory;
	}

	public final SQLSession currentSession() throws Exception {
		SQLSession current = existingSession(factory);
		if (current == null) {
			current = factory.openSession();
			current.getTransaction().registerSynchronization(new CleaningSession(factory));
			registerSQLTSessionListener(current);
			doBind(current, factory);
		}
		return current;
	}

	private void registerSQLTSessionListener(SQLSession session) {
		session.addListener(new SQLSessionListener() {
			@Override
			public void onExecuteUpdateSQL(String sql, Object[] parameters) {
			}

			@Override
			public void onExecuteUpdateSQL(String sql, Map<String, Object> parameters) {
			}

			@Override
			public void onExecuteUpdateSQL(String sql, NamedParameter[] parameters) {
			}

			@Override
			public void onExecuteSQL(String sql, Object[] parameters) {
			}

			@Override
			public void onExecuteSQL(String sql, Map<String, Object> parameters) {
			}

			@Override
			public void onExecuteSQL(String sql, NamedParameter[] parameters) {
			}

			@Override
			public void onClose(SQLSession session) {
				unbind(session.getSQLSessionFactory());
			}
		});
	}

	protected SQLSessionFactory getSQLSessionFactory() {
		return factory;
	}

	public static void bind(SQLSession session) {
		SQLSessionFactory factory = session.getSQLSessionFactory();
		cleanupAnyOrphanedSession(factory);
		doBind(session, factory);
	}

	private static void cleanupAnyOrphanedSession(SQLSessionFactory factory) {
		SQLSession orphan = doUnbind(factory, false);
		if (orphan != null) {
			log.warn("Already session bound on call to bind(); make sure you clean up your sessions!");
			try {
				if (orphan.getTransaction() != null && orphan.getTransaction().isActive()) {
					try {
						orphan.getTransaction().rollback();
					} catch (Throwable t) {
						log.debug("Unable to rollback transaction for orphaned session", t);
					}
				}
				orphan.close();
			} catch (Throwable t) {
				log.debug("Unable to close orphaned session", t);
			}
		}
	}

	public static SQLSession unbind(SQLSessionFactory factory) {
		return doUnbind(factory, true);
	}

	private static SQLSession existingSession(SQLSessionFactory factory) {
		Map<SQLSessionFactory, SQLSession> sessionMap = sessionMap();
		if (sessionMap == null) {
			return null;
		} else {
			return sessionMap.get(factory);
		}
	}

	protected static Map<SQLSessionFactory, SQLSession> sessionMap() {
		return context.get();
	}

	private static void doBind(SQLSession session, SQLSessionFactory factory) {
		Map<SQLSessionFactory, SQLSession> sessionMap = sessionMap();
		if (sessionMap == null) {
			sessionMap = new HashMap<SQLSessionFactory, SQLSession>();
			context.set(sessionMap);
		}
		sessionMap.put(factory, session);
	}

	private static SQLSession doUnbind(SQLSessionFactory factory, boolean releaseMapIfEmpty) {
		Map<SQLSessionFactory, SQLSession> sessionMap = sessionMap();
		SQLSession session = null;
		if (sessionMap != null) {
			session = (SQLSession) sessionMap.remove(factory);
			if (releaseMapIfEmpty && sessionMap.isEmpty()) {
				context.set(null);
			}
		}
		return session;
	}

	protected static class CleaningSession implements AnterosSynchronization, Synchronization, Serializable {
		private static final long serialVersionUID = 1L;
		protected final SQLSessionFactory factory;

		public CleaningSession(SQLSessionFactory factory) {
			this.factory = factory;
		}

		public void beforeCompletion() {
		}

		public void afterCompletion(int i) {
			unbind(factory);
		}
	}
}
