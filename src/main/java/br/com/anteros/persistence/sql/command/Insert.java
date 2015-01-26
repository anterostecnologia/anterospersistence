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

import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

public class Insert {
	private DatabaseDialect dialect;
	private String tableName;
	private String comment;
	private Map columns = new LinkedHashMap();

	public Insert(DatabaseDialect dialect) {
		this.dialect = dialect;
	}

	protected DatabaseDialect getDatabaseDialect() {
		return dialect;
	}

	public Insert setComment(String comment) {
		this.comment = comment;
		return this;
	}

	public Insert addColumn(String columnName) {
		return addColumn(columnName, "?");
	}

	public Insert addColumns(String[] columnNames) {
		for (int i = 0; i < columnNames.length; i++) {
			addColumn(columnNames[i]);
		}
		return this;
	}

	public Insert addColumns(String[] columnNames, boolean[] insertable) {
		for (int i = 0; i < columnNames.length; i++) {
			if (insertable[i]) {
				addColumn(columnNames[i]);
			}
		}
		return this;
	}

	public Insert addColumns(String[] columnNames, boolean[] insertable,
			String[] valueExpressions) {
		for (int i = 0; i < columnNames.length; i++) {
			if (insertable[i]) {
				addColumn(columnNames[i], valueExpressions[i]);
			}
		}
		return this;
	}

	public Insert addColumn(String columnName, String valueExpression) {
		columns.put(columnName, valueExpression);
		return this;
	}

	public Insert setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public String toStatementString() {
		StringBuilder buf = new StringBuilder(columns.size() * 15
				+ tableName.length() + 10);
		if (comment != null) {
			buf.append("/* ").append(comment).append(" */ ");
		}
		buf.append("insert into ").append(tableName);
		if (columns.size() > 0) {
			buf.append(" (");
			Iterator iter = columns.keySet().iterator();
			while (iter.hasNext()) {
				buf.append(iter.next());
				if (iter.hasNext()) {
					buf.append(", ");
				}
			}
			buf.append(") values (");
			iter = columns.values().iterator();
			while (iter.hasNext()) {
				buf.append(iter.next());
				if (iter.hasNext()) {
					buf.append(", ");
				}
			}
			buf.append(')');
		}
		return buf.toString();
	}
}
