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
package br.com.anteros.persistence.parameter;

import java.util.HashMap;
import java.util.Map;

import br.com.anteros.persistence.metadata.annotation.EnumValue;
import br.com.anteros.persistence.metadata.annotation.EnumValues;
import br.com.anteros.persistence.parameter.type.EnumeratedFormatSQL;

public class EnumeratedParameter extends SubstitutedParameter {

	private EnumeratedFormatSQL format;

	public EnumeratedParameter(String name, EnumeratedFormatSQL format, Enum<?>... value) {
		super(name, value);
		this.format = format;
	}

	public EnumeratedParameter(String name, Enum<?>... value) {
		super(name, value);
		this.format = EnumeratedFormatSQL.STRING;
	}

	@Override
	public String toString() {
		String result = "";
		for (Object ev : (Enum[]) this.getValue()) {
			if (!"".equals(result))
				result += ",";
			String newValue;
			if (ev.getClass().isEnum()) {
				Map<String, String> enumValues = new HashMap<String, String>();
				if (ev.getClass().isAnnotationPresent(EnumValues.class)) {
					EnumValues enumAnnotation = ev.getClass().getAnnotation(EnumValues.class);
					if (enumAnnotation.value().length > 0) {
						for (EnumValue value : enumAnnotation.value()) {
							enumValues.put(value.enumValue(), value.value());
						}
					}
				} else {
					for (Object value : ev.getClass().getEnumConstants()) {
						enumValues.put(value.toString(), value.toString());
					}
				}
				newValue = enumValues.get(ev.toString());
			} else
				newValue = ev.toString();

			if (format.equals(EnumeratedFormatSQL.ORDINAL))
				result += newValue;
			else
				result += "'" + newValue + "'";
		}
		return result;
	}

}
