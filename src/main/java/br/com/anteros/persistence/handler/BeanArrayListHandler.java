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
package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BeanArrayListHandler extends AbstractListHandler<Object> {

	private final BeanProcessor convert;
	private Class<?> clazz;

	public BeanArrayListHandler(Class<?> clazz) {
		this(new BeanProcessor());
		this.clazz = clazz;
	}

	public BeanArrayListHandler(BeanProcessor convert) {
		super();
		this.convert = convert;
	}

	protected Object handleRow(ResultSet rs) throws SQLException {
		return this.convert.toBean(rs, clazz);
	}

}
