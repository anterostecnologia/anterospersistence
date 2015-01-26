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
package br.com.anteros.persistence.metadata.identifier;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;

public class IdentifyGenerator implements IdentifierGenerator, IdentifierPostInsert {

	private Serializable generatedValue;
	private Type type;

	public IdentifyGenerator(Type type) {
		this.type = type;
	}

	public Serializable generate() throws Exception {
		return generatedValue;
	}

	public void setGeneratedValue(ResultSet rs) throws Exception {
		if (type == Long.class)
			generatedValue = new Long(rs.getLong(1));
		else if (type == Integer.class)
			generatedValue = new Integer(rs.getInt(1));
		else if (type == Short.class)
			generatedValue = new Short(rs.getShort(1));
		else if (type == String.class)
			generatedValue = rs.getString(1);
		else if (type == BigInteger.class)
			generatedValue = rs.getBigDecimal(1).setScale(0, BigDecimal.ROUND_UNNECESSARY).toBigInteger();
		else if (type == BigDecimal.class)
			generatedValue = rs.getBigDecimal(1).setScale(0, BigDecimal.ROUND_UNNECESSARY);
		else
			throw new IdentifierException("Tipo de Identificador desconhecido : " + type);
	}

	public void setType(Type type) {
		this.type = type;

	}

	public Type getType() {
		return type;
	}

}
