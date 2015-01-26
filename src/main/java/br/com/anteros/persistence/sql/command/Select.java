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
package br.com.anteros.persistence.sql.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

public class Select {

	private String outerJoinsAfterFrom;
	private String outerJoinsAfterWhere;
	private String orderByClause;
	private String groupByClause;
	private String comment;
	private List<String> columns = new ArrayList<String>();
	private List<String> tables = new ArrayList<String>();
	private Map<String, String> tableAliases = new LinkedHashMap<String, String>();
	private Map<String, String> aliases = new LinkedHashMap<String, String>();
	private List<String> whereTokens = new ArrayList<String>();

	public final DatabaseDialect dialect;

	public Select(DatabaseDialect dialect) {
		this.dialect = dialect;
	}
	
	public Select addTableName(String tableName) {
		addTableName(tableName, null);
		return this;
	}

	public Select addTableName(String tableName, String tableAlias) {
		tables.add(tableName);
		if (tableAlias == null)
			tableAliases.put(tableName, "");
		else
			tableAliases.put(tableName, tableAlias);
		return this;
	}

	public Select addTableNames(String[] tableNames, String[] aliasTables) {
		for (int i = 0; i < tableNames.length; i++) {
			if (tableNames[i] != null) {
				addTableName(tableNames[i], aliasTables[i]);
			}
		}
		return this;
	}

	public Select addColumns(String[] columnNames, String[] columnAliases) {
		for (int i = 0; i < columnNames.length; i++) {
			if (columnNames[i] != null) {
				addColumn(columnNames[i], columnAliases[i]);
			}
		}
		return this;
	}

	public Select addColumns(String[] columns, String[] aliases,
			boolean[] ignore) {
		for (int i = 0; i < ignore.length; i++) {
			if (!ignore[i] && columns[i] != null) {
				addColumn(columns[i], aliases[i]);
			}
		}
		return this;
	}

	public Select addColumns(String[] columnNames) {
		for (int i = 0; i < columnNames.length; i++) {
			if (columnNames[i] != null)
				addColumn(columnNames[i]);
		}
		return this;
	}

	public Select addColumn(String columnName) {
		columns.add(columnName);
		return this;
	}

	public Select addColumn(String columnName, String alias) {
		columns.add(columnName);
		aliases.put(columnName, alias);
		return this;
	}

	public Select addWhereToken(String token) {
		whereTokens.add(token);
		return this;
	}

	public void and() {
		if (whereTokens.size() > 0) {
			whereTokens.add("and");
		}
	}
	
	public void or() {
		if (whereTokens.size() > 0) {
			whereTokens.add("and");
		}
	}

	public Select addCondition(String lhs, String op, String rhs) {
		whereTokens.add(lhs + ' ' + op + ' ' + rhs);
		return this;
	}

	public Select addCondition(String lhs, String condition) {
		whereTokens.add(lhs + ' ' + condition);
		return this;
	}

	public Select addCondition(String[] lhs, String op, String[] rhs) {
		for (int i = 0; i < lhs.length; i++) {
			addCondition(lhs[i], op, rhs[i]);
		}
		return this;
	}

	public Select addCondition(String[] lhs, String condition) {
		for (int i = 0; i < lhs.length; i++) {
			if (lhs[i] != null)
				addCondition(lhs[i], condition);
		}
		return this;
	}

	public String toStatementString() {
		StringBuilder buf = new StringBuilder();
		if (StringUtils.isNotEmpty(comment)) {
			buf.append("/* ").append(comment).append(" */ ");
		}

		buf.append("select ");
		if (columns.size() == 0) {
			buf.append("*");
		} else {
			Set uniqueColumns = new HashSet();
			Iterator<String> iter = columns.iterator();
			boolean appendComma = false;
			while (iter.hasNext()) {
				String col = (String) iter.next();
				String alias = (String) aliases.get(col);
				if (uniqueColumns.add(alias == null ? col : alias)) {
					if (appendComma)
						buf.append(", ");
					buf.append(col);
					if (alias != null && !alias.equals(col)) {
						buf.append(" as ").append(alias);
					}
					appendComma = true;
				}
			}
		}

		buf.append(" from ");
		if (tables.size() > 0) {
			Iterator<String> iter = tables.iterator();
			boolean appendComma = false;
			while (iter.hasNext()) {
				String table = iter.next();
				String tableAlias = (String) tableAliases.get(table);
				if (appendComma)
					buf.append(", ");
				buf.append(table);
				if (StringUtils.isNotEmpty(tableAlias)) {
					buf.append(" ").append(tableAlias);
				}
				appendComma = true;
			}
		}

		String whereClause = toWhereClause();

		if (StringUtils.isNotEmpty(outerJoinsAfterFrom)) {
			buf.append(outerJoinsAfterFrom);
		}

		if (StringUtils.isNotEmpty(whereClause)
				|| StringUtils.isNotEmpty(outerJoinsAfterWhere)) {
			buf.append(" where ");
			if (StringUtils.isNotEmpty(outerJoinsAfterWhere)) {
				buf.append(outerJoinsAfterWhere);
				if (StringUtils.isNotEmpty(whereClause)) {
					buf.append(" and ");
				}
			}
			if (StringUtils.isNotEmpty(whereClause)) {
				buf.append(whereClause);
			}
		}

		if (StringUtils.isNotEmpty(groupByClause)) {
			buf.append(" group by ").append(groupByClause);
		}

		if (StringUtils.isNotEmpty(orderByClause)) {
			buf.append(" order by ").append(orderByClause);
		}

		return buf.toString();
	}

	public String toWhereClause() {
		StringBuilder buf = new StringBuilder(whereTokens.size() * 5);
		Iterator iter = whereTokens.iterator();
		while (iter.hasNext()) {
			buf.append(iter.next());
			if (iter.hasNext())
				buf.append(' ');
		}
		return buf.toString();
	}

	public Select setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
		return this;
	}

	public Select setGroupByClause(String groupByClause) {
		this.groupByClause = groupByClause;
		return this;
	}

	public Select setOuterJoins(String outerJoinsAfterFrom,
			String outerJoinsAfterWhere) {
		this.outerJoinsAfterFrom = outerJoinsAfterFrom;

		String tmpOuterJoinsAfterWhere = outerJoinsAfterWhere.trim();
		if (tmpOuterJoinsAfterWhere.startsWith("and")) {
			tmpOuterJoinsAfterWhere = tmpOuterJoinsAfterWhere.substring(4);
		}
		this.outerJoinsAfterWhere = tmpOuterJoinsAfterWhere;

		return this;
	}

	public Select setComment(String comment) {
		this.comment = comment;
		return this;
	}


}
