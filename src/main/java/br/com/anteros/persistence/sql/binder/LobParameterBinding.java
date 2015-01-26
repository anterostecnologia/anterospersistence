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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.Types;

import br.com.anteros.core.utils.ArrayUtils;

public class LobParameterBinding implements ParameterBinding {

	private Object value;
	private int type = Types.BLOB;

	public LobParameterBinding(Object value) {
		this.value = value;
	}
	
	public LobParameterBinding(Object value, int type) {
		this.value = value;
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	
	public void bindValue(PreparedStatement statement, int parameterIndex) throws Exception {
		if (this.getValue() == null) {
			statement.setObject(parameterIndex, null);
		} else if (this.getValue() instanceof Clob) {
			statement.setClob(parameterIndex, (Clob) this.getValue());
		} else if (this.getValue() instanceof Character[]) {
			String value = new String(ArrayUtils.toPrimitive((Character[]) this.getValue()));
			statement.setString(parameterIndex, value);
		} else if (this.getValue().getClass() == char[].class) {
			String value = new String((char[]) this.getValue());
			statement.setString(parameterIndex, value);
		} else if (this.getValue() instanceof String) {
			statement.setString(parameterIndex, (String) this.getValue());
		} else if (this.getValue() instanceof Blob) {
			statement.setBlob(parameterIndex + 1, (Blob) this.getValue());
		} else if (this.getValue() instanceof Byte[]) {
			statement.setObject(parameterIndex, this.getValue(), Types.BINARY);
		} else if (this.getValue().getClass() == byte[].class) {
			statement.setObject(parameterIndex, this.getValue(), Types.BINARY);
		} else if (this.getValue() instanceof Serializable) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this.getValue());
			oos.flush();
			oos.close();
			statement.setObject(parameterIndex, baos.toByteArray(), Types.BINARY);
		}
	}

	public int getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return ""+value;
	}
}
