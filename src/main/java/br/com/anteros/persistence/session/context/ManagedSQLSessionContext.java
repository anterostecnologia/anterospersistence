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

import java.sql.SQLException;
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
			SQLSession session = sessionMap.get( factory );
			try {
				if (session.getConnection().isClosed() || !session.getConnection().isValid(2000)) {
					session = factory.openSession();
					bind(session);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return session;
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

