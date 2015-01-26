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

public class Update {

	private String tableName;
	private String versionColumnName;
	private String where;
	private String assignments;
	private String comment;

	private Map primaryKeyColumns = new LinkedHashMap();
	private Map columns = new LinkedHashMap();
	private Map whereColumns = new LinkedHashMap();
	
	private DatabaseDialect dialect;
	
	public Update(DatabaseDialect dialect) {
		this.dialect = dialect;
	}

	public String getTableName() {
		return tableName;
	}

	public Update appendAssignmentFragment(String fragment) {
		if ( assignments == null ) {
			assignments = fragment;
		}
		else {
			assignments += ", " + fragment;
		}
		return this;
	}

	public Update setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public Update setPrimaryKeyColumnNames(String[] columnNames) {
		this.primaryKeyColumns.clear();
		addPrimaryKeyColumns(columnNames);
		return this;
	}	
	
	public Update addPrimaryKeyColumns(String[] columnNames) {
		for ( int i=0; i<columnNames.length; i++ ) {
			addPrimaryKeyColumn( columnNames[i], "?" );
		}
		return this;
	}
	
	public Update addPrimaryKeyColumns(String[] columnNames, boolean[] includeColumns, String[] valueExpressions) {
		for ( int i=0; i<columnNames.length; i++ ) {
			if( includeColumns[i] ) addPrimaryKeyColumn( columnNames[i], valueExpressions[i] );
		}
		return this;
	}
	
	public Update addPrimaryKeyColumns(String[] columnNames, String[] valueExpressions) {
		for ( int i=0; i<columnNames.length; i++ ) {
			addPrimaryKeyColumn( columnNames[i], valueExpressions[i] );
		}
		return this;
	}	

	public Update addPrimaryKeyColumn(String columnName, String valueExpression) {
		this.primaryKeyColumns.put(columnName, valueExpression);
		return this;
	}
	
	public Update setVersionColumnName(String versionColumnName) {
		this.versionColumnName = versionColumnName;
		return this;
	}


	public Update setComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	public Update addColumns(String[] columnNames) {
		for ( int i=0; i<columnNames.length; i++ ) {
			addColumn( columnNames[i] );
		}
		return this;
	}

	public Update addColumns(String[] columnNames, boolean[] updateable, String[] valueExpressions) {
		for ( int i=0; i<columnNames.length; i++ ) {
			if ( updateable[i] ) addColumn( columnNames[i], valueExpressions[i] );
		}
		return this;
	}

	public Update addColumns(String[] columnNames, String valueExpression) {
		for ( int i=0; i<columnNames.length; i++ ) {
			addColumn( columnNames[i], valueExpression );
		}
		return this;
	}

	public Update addColumn(String columnName) {
		return addColumn(columnName, "?");
	}

	public Update addColumn(String columnName, String valueExpression) {
		columns.put(columnName, valueExpression);
		return this;
	}

	public Update addWhereColumns(String[] columnNames) {
		for ( int i=0; i<columnNames.length; i++ ) {
			addWhereColumn( columnNames[i] );
		}
		return this;
	}

	public Update addWhereColumns(String[] columnNames, String valueExpression) {
		for ( int i=0; i<columnNames.length; i++ ) {
			addWhereColumn( columnNames[i], valueExpression );
		}
		return this;
	}

	public Update addWhereColumn(String columnName) {
		return addWhereColumn(columnName, "=?");
	}

	public Update addWhereColumn(String columnName, String valueExpression) {
		whereColumns.put(columnName, valueExpression);
		return this;
	}

	public Update setWhere(String where) {
		this.where=where;
		return this;
	}

	public String toStatementString() {
		StringBuilder buf = new StringBuilder( (columns.size() * 15) + tableName.length() + 10 );
		if ( comment!=null ) {
			buf.append( "/* " ).append( comment ).append( " */ " );
		}
		buf.append( "update " ).append( tableName ).append( " set " );
		boolean assignmentsAppended = false;
		Iterator iter = columns.entrySet().iterator();
		while ( iter.hasNext() ) {
			Map.Entry e = (Map.Entry) iter.next();
			buf.append( e.getKey() ).append( '=' ).append( e.getValue() );
			if ( iter.hasNext() ) {
				buf.append( ", " );
			}
			assignmentsAppended = true;
		}
		if ( assignments != null ) {
			if ( assignmentsAppended ) {
				buf.append( ", " );
			}
			buf.append( assignments );
		}

		boolean conditionsAppended = false;
		if ( !primaryKeyColumns.isEmpty() || where != null || !whereColumns.isEmpty() || versionColumnName != null ) {
			buf.append( " where " );
		}
		iter = primaryKeyColumns.entrySet().iterator();
		while ( iter.hasNext() ) {
			Map.Entry e = (Map.Entry) iter.next();
			buf.append( e.getKey() ).append( '=' ).append( e.getValue() );
			if ( iter.hasNext() ) {
				buf.append( " and " );
			}
			conditionsAppended = true;
		}
		if ( where != null ) {
			if ( conditionsAppended ) {
				buf.append( " and " );
			}
			buf.append( where );
			conditionsAppended = true;
		}
		iter = whereColumns.entrySet().iterator();
		while ( iter.hasNext() ) {
			final Map.Entry e = (Map.Entry) iter.next();
			if ( conditionsAppended ) {
				buf.append( " and " );
			}
			buf.append( e.getKey() ).append( e.getValue() );
			conditionsAppended = true;
		}
		if ( versionColumnName != null ) {
			if ( conditionsAppended ) {
				buf.append( " and " );
			}
			buf.append( versionColumnName ).append( "=?" );
		}

		return buf.toString();
	}
}
