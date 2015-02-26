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

/*******************************************************************************
 * Copyright (c) 2007 - 2009 ZIGEN
 * Eclipse Public License - v 1.0
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package br.com.anteros.persistence.sql.parser;

import java.util.HashMap;
import java.util.Map;

public class SqlKeyword {
	static final Map<String, String> keywordMap = new HashMap<String, String>();

	static final Map<String, String> functionMap = new HashMap<String, String>();

	public static final String[] Keywords = { "add", "all", "alter", "and", "as", "asc", "between", "by", "cascade", "column", "comment", "commit",
			"constraint", "create", "date", "default", "delete", "desc", "distinct", "drop", "escape", "exists", "foreign", "from", "function",
			"group", "having", "in", "index", "inner", "insert", "into", "is", "join", "key", "left", "like", "limit", "match", "minus", "not", "null", "on",
			"option", "or", "order", "outer", "package", "primary", "procedure", "right", "rollback", "rows", "schema", "select", "set", "show",
			"table", "temporary", "time", "timestamp", "trigger", "truncate", "type", "union", "unique", "update", "values", "view", "where", "with" };

	public static final String[] Functions = { "count", "max", "min" };

	public static final String[] PLSQLTypes = { "function", "procedure", "trigger", "package" };

	static {
		for (int i = 0; i < Keywords.length; i++) {
			keywordMap.put(Keywords[i], Keywords[i]);
		}
		for (int i = 0; i < Functions.length; i++) {
			functionMap.put(Functions[i], Functions[i]);
		}
	}

	public static boolean isKeyword(String target) {
		target = target.toLowerCase();
		return (keywordMap.containsKey(target));
	}

	public static boolean isFunction(String target) {
		target = target.toLowerCase();
		return (functionMap.containsKey(target));
	}

}
