/*******************************************************************************
 * Copyright 2011, Mysema Ltd
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import br.com.anteros.core.utils.Base64.InputStream;

public class LiteralUtils {

	protected static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	protected static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

	public static String asLiteral(Object value) {
		if (value instanceof Calendar) {
			return "'" + dateTimeFormatter.print(((Calendar) value).getTimeInMillis()) + "'";
		} else if (value instanceof DateTime) {
			return "'" + dateTimeFormatter.print((DateTime) value) + "'";
		} else if (value instanceof Date) {
			return "'" + dateFormatter.print(((Date) value).getTime()) + "'";
		} else if (value instanceof java.sql.Date) {
			return "'" + dateFormatter.print(((java.sql.Date) value).getTime()) + "'";
		} else if (value instanceof InputStream) {
			return value.toString();
		} else if (value instanceof Timestamp) {
			return "(timestamp '" + dateTimeFormatter.print(((Timestamp) value).getTime()) + "')";
		} else if (value instanceof Time) {
			return "(time '" + timeFormatter.print(((Time) value).getTime()) + "')";
		} else if (value instanceof String) {
			return "'" + escapeLiteral(value.toString()) + "'";
		} else if (value instanceof Enum<?>) {
			return "'" + value.toString() + "'";
		}
		return value.toString();
	}

	protected static String escapeLiteral(String str) {
		StringBuilder builder = new StringBuilder();
		for (char ch : str.toCharArray()) {
			if (ch == '\n') {
				builder.append("\\n");
				continue;
			} else if (ch == '\r') {
				builder.append("\\r");
				continue;
			} else if (ch == '\'') {
				builder.append("''");
				continue;
			}
			builder.append(ch);
		}
		return builder.toString();
	}
}
