/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public class ConnectionUtils {

	private static ThreadLocal<Map<Object, Reference<Connection>>> localConnection = new ThreadLocal<Map<Object, Reference<Connection>>>();

	public static Connection getConnection(DataSource dataSource) throws SQLException {
		if (localConnection.get() == null)
			localConnection.set(new HashMap<Object, Reference<Connection>>());
		Reference<Connection> refConnection = localConnection.get().get(dataSource);
		if (refConnection == null || refConnection.get() == null) {
			Connection connection = dataSource.getConnection();
			refConnection = new WeakReference<Connection>(connection);
			localConnection.get().put(dataSource, refConnection);
		}
		return refConnection.get();
	}

	public static void releaseConnection(DataSource dataSource) {
		if (localConnection.get() == null)
			localConnection.set(new HashMap<Object, Reference<Connection>>());
		Reference<Connection> refConnection = localConnection.get().get(dataSource);
		if (refConnection != null)
			localConnection.get().remove(dataSource);
	}

}
