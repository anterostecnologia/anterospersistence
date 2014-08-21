package br.com.anteros.persistence.dsl.osql.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
			return "'" +dateTimeFormatter.print(((Calendar) value).getTimeInMillis())+"'";
		} else if (value instanceof DateTime) {
			return "'" +dateTimeFormatter.print((DateTime) value)+"'";
		} else if (value instanceof Date) {
			return "'" +dateFormatter.print(((Date) value).getTime())+"'";
		} else if (value instanceof java.sql.Date) {
			return "'" +dateFormatter.print(((java.sql.Date) value).getTime())+"'";
		} else if (value instanceof InputStream) {
			return value.toString();
		} else if (value instanceof Timestamp) {
			return "(timestamp '" + dateTimeFormatter.print(((Timestamp) value).getTime())+"')";
		} else if (value instanceof Time) {
			return "(time '"+timeFormatter.print(((Time) value).getTime())+"')";
		} else if (value instanceof String) {
			return "'" + escapeLiteral(value.toString()) + "'";
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
