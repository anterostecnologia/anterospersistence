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
package br.com.anteros.persistence.dsl.osql.types.expr.params;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;
import br.com.anteros.persistence.parameter.type.EnumeratedFormatSQL;

/**
 * Permite uso de parâmetros Enum sem a necessidade do uso de genérics.
 * 
 * @author Maiko Antonio Cunha
 *
 */
@SuppressWarnings("rawtypes")
public class EnumParam extends Param<Enum> {

	private static final long serialVersionUID = 1L;
	private EnumeratedFormatSQL format;

	public EnumParam(String name, EnumeratedFormatSQL format) {
		super(Enum.class, name);
		this.format = format;
	}

	public EnumParam(String name) {
		this(name, EnumeratedFormatSQL.STRING);
	}

	public EnumParam() {
		super(Enum.class);
	}

	public EnumeratedFormatSQL getFormat() {
		return format;
	}
}
