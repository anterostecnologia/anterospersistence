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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Delete {

	private String tableName;
	private String versionColumnName;
	private String where;

	private Map<String, String> primaryKeyColumns = new LinkedHashMap<String, String>();

	private String comment;

	public Delete setComment(String comment) {
		this.comment = comment;
		return this;
	}

	public Delete setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public String toStatementString() {
		StringBuilder buf = new StringBuilder(tableName.length() + 10);
		if (comment != null) {
			buf.append("/* ").append(comment).append(" */ ");
		}
		buf.append("delete from ").append(tableName);
		if (where != null || !primaryKeyColumns.isEmpty() || versionColumnName != null) {
			buf.append(" where ");
		}
		boolean conditionsAppended = false;
		Iterator<Entry<String, String>> iter = primaryKeyColumns.entrySet().iterator();
		Entry<String, String> entry;
		while (iter.hasNext()) {
			entry = iter.next();
			buf.append(entry.getKey()).append('=').append(entry.getValue());
			if (iter.hasNext())
				buf.append(" and ");
			conditionsAppended = true;
		}
		if (where != null) {
			if (conditionsAppended)
				buf.append(" and ");
			buf.append(where);
			conditionsAppended = true;
		}
		if (versionColumnName != null) {
			if (conditionsAppended)
				buf.append(" and ");
			buf.append(versionColumnName).append("=?");
		}
		return buf.toString();
	}

	public Delete setWhere(String where) {
		this.where = where;
		return this;
	}

	public Delete addWhereFragment(String fragment) {
		if (where == null)
			where = fragment;
		else
			where += (" and " + fragment);
		return this;
	}

	public Delete setPrimaryKeyColumnNames(String[] columnNames) {
		this.primaryKeyColumns.clear();
		addPrimaryKeyColumns(columnNames);
		return this;
	}

	public Delete addPrimaryKeyColumns(String[] columnNames) {
		for (int i = 0; i < columnNames.length; i++) {
			addPrimaryKeyColumn(columnNames[i], "?");
		}
		return this;
	}

	public Delete addPrimaryKeyColumns(String[] columnNames, boolean[] includeColumns, String[] valueExpressions) {
		for (int i = 0; i < columnNames.length; i++) {
			if (includeColumns[i])
				addPrimaryKeyColumn(columnNames[i], valueExpressions[i]);
		}
		return this;
	}

	public Delete addPrimaryKeyColumns(String[] columnNames, String[] valueExpressions) {
		for (int i = 0; i < columnNames.length; i++) {
			addPrimaryKeyColumn(columnNames[i], valueExpressions[i]);
		}
		return this;
	}

	public Delete addPrimaryKeyColumn(String columnName, String valueExpression) {
		this.primaryKeyColumns.put(columnName, valueExpression);
		return this;
	}

	public Delete setVersionColumnName(String versionColumnName) {
		this.versionColumnName = versionColumnName;
		return this;
	}

}
