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
package br.com.anteros.persistence.util;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

public final class SQLParserUtil {

	private static final Set<String> KEYWORDS = new HashSet<String>();
	private static final Set<String> BEFORE_TABLE_KEYWORDS = new HashSet<String>();
	public static final String WHITESPACE = " \n\r\f\t";
	static {
		KEYWORDS.add("and");
		KEYWORDS.add("or");
		KEYWORDS.add("not");
		KEYWORDS.add("like");
		KEYWORDS.add("is");
		KEYWORDS.add("in");
		KEYWORDS.add("between");
		KEYWORDS.add("null");
		KEYWORDS.add("select");
		KEYWORDS.add("distinct");
		KEYWORDS.add("from");
		KEYWORDS.add("join");
		KEYWORDS.add("inner");
		KEYWORDS.add("outer");
		KEYWORDS.add("left");
		KEYWORDS.add("right");
		KEYWORDS.add("on");
		KEYWORDS.add("where");
		KEYWORDS.add("having");
		KEYWORDS.add("group");
		KEYWORDS.add("order");
		KEYWORDS.add("by");
		KEYWORDS.add("desc");
		KEYWORDS.add("asc");
		KEYWORDS.add("limit");
		KEYWORDS.add("any");
		KEYWORDS.add("some");
		KEYWORDS.add("exists");
		KEYWORDS.add("all");
		KEYWORDS.add("union");
		KEYWORDS.add("minus");

		BEFORE_TABLE_KEYWORDS.add("from");
		BEFORE_TABLE_KEYWORDS.add("join");
	}

	public static Set<String> getTableNames(String sql, DatabaseDialect dialect) throws Exception {
		Set<String> result = new LinkedHashSet<String>();
		String newSql = sql.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");
		String symbols = new StringBuilder().append("=><!+-*/()',|&`").append(WHITESPACE).append(dialect.getOpenQuote())
				.append(dialect.getCloseQuote()).toString();
		StringTokenizer tokens = new StringTokenizer(newSql, symbols, true);

		boolean quoted = false;
		boolean quotedIdentifier = false;
		boolean beforeTable = false;
		boolean inFromClause = false;
		boolean afterFromTable = false;

		boolean hasMore = tokens.hasMoreTokens();
		String nextToken = hasMore ? tokens.nextToken() : null;
		while (hasMore) {
			String token = nextToken;

			String lcToken = token.toLowerCase();
			hasMore = tokens.hasMoreTokens();
			nextToken = hasMore ? tokens.nextToken() : null;

			boolean isQuoteCharacter = false;

			if (!quotedIdentifier && "'".equals(token)) {
				quoted = !quoted;
				isQuoteCharacter = true;
			}

			if (!quoted) {
				boolean isOpenQuote;
				if ("`".equals(token)) {
					isOpenQuote = !quotedIdentifier;
					token = lcToken = isOpenQuote ? Character.toString(dialect.getOpenQuote()) : Character.toString(dialect.getCloseQuote());
					quotedIdentifier = isOpenQuote;
					isQuoteCharacter = true;
				} else if (!quotedIdentifier && (dialect.getOpenQuote() == token.charAt(0))) {
					isOpenQuote = true;
					quotedIdentifier = true;
					isQuoteCharacter = true;
				} else if (quotedIdentifier && (dialect.getCloseQuote() == token.charAt(0))) {
					quotedIdentifier = false;
					isQuoteCharacter = true;
					isOpenQuote = false;
				} else {
					isOpenQuote = false;
				}

			}

			boolean quotedOrWhitespace = quoted || quotedIdentifier || isQuoteCharacter
					|| Character.isWhitespace(token.charAt(0));

			if ((!quotedOrWhitespace) && (beforeTable)) {
				beforeTable = false;
				afterFromTable = true;
				result.add(lcToken.toUpperCase());
			} else if (afterFromTable) {
				if (!"as".equals(lcToken)) {
					afterFromTable = false;
				}
			} else {
				if (BEFORE_TABLE_KEYWORDS.contains(lcToken)) {
					beforeTable = true;
					inFromClause = true;
				} else if (inFromClause && ",".equals(lcToken)) {
					beforeTable = true;
				}
			}

			if (inFromClause && KEYWORDS.contains(lcToken) && !BEFORE_TABLE_KEYWORDS.contains(lcToken)) {
				inFromClause = false;
			}
		}

		return result;
	}

}
