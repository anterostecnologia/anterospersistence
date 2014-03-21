/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.schema.definition;

import java.util.ArrayList;
import java.util.List;

public abstract class ConstraintSchema extends ObjectSchema {

	protected final List<ColumnSchema> columns = new ArrayList<ColumnSchema>();
	protected TableSchema table;
	
	public ConstraintSchema() {
		this.name = "";
	}
	
    public void addColumn(ColumnSchema column) {
        columns.add(column);
    }

	
	public List<ColumnSchema> getColumns() {
		return columns;
	}

	public TableSchema getTable() {
		return table;
	}

	public void setTable(TableSchema table) {
		this.table = table;
	}

}
