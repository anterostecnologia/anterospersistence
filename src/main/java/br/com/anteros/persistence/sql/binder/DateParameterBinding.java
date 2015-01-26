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
package br.com.anteros.persistence.sql.binder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class DateParameterBinding implements ParameterBinding {

	private Object value;

	public DateParameterBinding(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	
	public void bindValue(PreparedStatement statement, int parameterIndex) throws SQLException {
		Date date = (Date) this.getValue();
		statement.setDate(parameterIndex, new java.sql.Date(date.getTime()));
	}
	
	@Override
	public String toString() {
		return ""+value;
	}
}
