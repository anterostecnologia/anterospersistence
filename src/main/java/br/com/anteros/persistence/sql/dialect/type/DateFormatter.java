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
package br.com.anteros.persistence.sql.dialect.type;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.anteros.persistence.metadata.annotation.type.TemporalType;

public class DateFormatter {

	private final DateFormat format;

	public DateFormatter(TemporalType type) {
		if (type == TemporalType.DATE)
			format = new SimpleDateFormat("dd/MM/yyyy");
		else if (type == TemporalType.DATE_TIME)
			format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		else
			format = new SimpleDateFormat("HH:mm:ss");
	}

	public Date parse(String s) {
		try {
			return format.parse(s);
		} catch (Exception e) {
			return null;
		}
	}

	public String format(Date t) {
		try {
			return format.format(t);
		} catch (Exception e) {
			return null;
		}
	}

}
