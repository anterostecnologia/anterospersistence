/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.session.context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import br.com.anteros.persistence.transaction.impl.TransactionException;

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
			if ( needsWrapping( current ) ) {
				current = wrap( current );
			}
			registerSQLTSessionListener(current);
			doBind(current, factory);
		}
		return current;
	}
	
	protected SQLSession wrap(SQLSession session) {
		TransactionProtectionWrapper wrapper = new TransactionProtectionWrapper( session );
		SQLSession wrapped = ( SQLSession ) Proxy.newProxyInstance(
				SQLSession.class.getClassLoader(),
				new Class[] {SQLSession.class},
		        wrapper
			);
		wrapper.setWrapped( wrapped );
		return wrapped;
	}
	
	private boolean needsWrapping(SQLSession session) {
		if (session ==null)
			return false;
		
		return ! Proxy.isProxyClass( session.getClass() )
		       || ( Proxy.getInvocationHandler( session ) != null
		       && ! ( Proxy.getInvocationHandler( session ) instanceof TransactionProtectionWrapper ) );
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
	
	private class TransactionProtectionWrapper implements InvocationHandler, Serializable {
		private static final long serialVersionUID = 1L;
		private final SQLSession realSession;
		private SQLSession wrappedSession;

		public TransactionProtectionWrapper(SQLSession realSession) {
			this.realSession = realSession;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				if ( "close".equals( method.getName()) ) {
					unbind( realSession.getSQLSessionFactory() );
				}
				else if ( "toString".equals( method.getName() )
					     || "equals".equals( method.getName() )
					     || "hashCode".equals( method.getName() )
					     || "addListener".equals( method.getName() )
					     || "setClientId".equals( method.getName() )
				         || "getStatistics".equals( method.getName() )
					     || "isOpen".equals( method.getName() )
						 || "getListeners".equals( method.getName() ) //useful for HSearch in particular
						) {
				}
				else if ( realSession.isClosed() ) {
				}
				else if ( !realSession.getTransaction().isActive() ) {
					if ( "beginTransaction".equals( method.getName() )
					     || "getTransaction".equals( method.getName() )
					     || "setFlushMode".equals( method.getName() )
					     || "getSQLSessionFactory".equals( method.getName() ) ) {
						log.debug( "allowing method [" + method.getName() + "] in non-transacted context" );
					}
					else {
						throw new TransactionException( method.getName() + " is not valid without active transaction" );
					}
				}
				log.debug( "allowing proxied method [" + method.getName() + "] to proceed to real session" );
				return method.invoke( realSession, args );
			}
			catch ( InvocationTargetException e ) {
				if ( e.getTargetException() instanceof RuntimeException ) {
					throw ( RuntimeException ) e.getTargetException();
				}
				else {
					throw e;
				}
			}
		}

		public void setWrapped(SQLSession wrapped) {
			this.wrappedSession = wrapped;
		}


		private void writeObject(ObjectOutputStream oos) throws IOException {
			oos.defaultWriteObject();
			if ( existingSession( factory ) == wrappedSession ) {
				unbind( factory );
			}
		}

		private void readObject(ObjectInputStream ois) throws Exception {
			ois.defaultReadObject();
			realSession.getTransaction().registerSynchronization( new CleaningSession(factory) );
			doBind( wrappedSession, factory );
		}
	}
}
