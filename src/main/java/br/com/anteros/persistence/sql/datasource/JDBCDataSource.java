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

package br.com.anteros.persistence.sql.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
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

	public JDBCDataSource(String driverClassName, String username,
			String password, String url) throws Exception {
		this.driverClassName = driverClassName;
		this.username = username;
		this.password = password;
		this.url = url;
		Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
	}

	public PrintWriter getLogWriter() throws SQLException {
		if (logWriter == null) {
			logWriter = new PrintWriter(System.out);
		}
		return logWriter;
	}

	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}

	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		this.logWriter = logWriter;
	}

	public void setLoginTimeout(int loginTimeout) throws SQLException {
		this.loginTimeout = loginTimeout;
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		throw new SQLException("JDBCDataSource is not a wrapper.");
	}

	public Connection getConnection() throws SQLException {
		try {
			Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		return getConnection(username, password);
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		
		 Class driver_class=null;
		try {
			driver_class = Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
			Driver driver = (Driver) driver_class.newInstance();
	         DriverManager.registerDriver(driver);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		} catch (InstantiationException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		}         
		
		Connection result = DriverManager
				.getConnection(url, username, password);
		return result;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName)
			throws ClassNotFoundException {
		Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
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

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

}
