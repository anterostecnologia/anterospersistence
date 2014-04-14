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

package br.com.anteros.persistence.sql.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;



public class JDBCDataSource implements DataSource {
	private PrintWriter logWriter;
	private int loginTimeout = 0;
	private String driverClassName;
	private String username;
	private String password;
	private String url;
	
	public JDBCDataSource() {
		
	}
	
	public JDBCDataSource(String driverClassName, String username, String password, String url) throws Exception {
		this.driverClassName = driverClassName;
		this.username = username;
		this.password = password;
		this.url = url;
		//Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
        Class.forName(driverClassName); 
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		if (logWriter == null) {
			logWriter = new PrintWriter(System.out);
		}
		return logWriter;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}

	@Override
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		this.logWriter = logWriter;
	}

	@Override
	public void setLoginTimeout(int loginTimeout) throws SQLException {
		this.loginTimeout = loginTimeout;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		throw new SQLException("JDBCDataSource is not a wrapper.");
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(username, password);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection result = DriverManager.getConnection(url, username, password);
		return result;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
