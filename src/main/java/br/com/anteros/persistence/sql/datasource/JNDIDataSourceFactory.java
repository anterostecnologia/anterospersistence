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

import javax.sql.DataSource;

import br.com.anteros.core.utils.ReflectionUtils;

public class JNDIDataSourceFactory {

	public static DataSource getDataSource(String jndiName) throws Exception {
		Class<?> initialContextClass = Class.forName("javax.naming.InitialContext");
		Object ctx = initialContextClass.newInstance();
		if (ctx == null)
			throw new RuntimeException("Context == null !!!");
		Object result = ReflectionUtils.invokeExactMethod(ctx, "lookup", "java:comp/env/jdbc/" + jndiName);
		return (DataSource) result;
	}
}
