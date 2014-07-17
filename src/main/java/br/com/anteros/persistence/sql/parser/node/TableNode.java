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
package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.ParserVisitor;


public class TableNode extends AliasNode {

	private String schemaName;

	private String tableName;

	public TableNode(String tableName,int offset, int length, int scope) {
		super(tableName, offset, length, scope);
		parse(tableName);
	}

	private void parse(String tableName) {
		String[] strs = tableName.split("[.]");
		if (strs.length == 2) {
			this.schemaName = strs[0];
			this.tableName = strs[1];

		} else if (strs.length == 1) {
			this.tableName = strs[0];
		}
	}

	public String getName() {
		StringBuffer sb = new StringBuffer();
		if (schemaName != null) {
			sb.append(schemaName);
			sb.append(".");
		}
		if (tableName != null) {
			sb.append(tableName);
		}

		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());
		if(hasAlias()){
			sb.append(" AS ");
			sb.append(getAliasName());
		}
		return getNodeClassName() + " text=\"" + sb.toString() + "\"";
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getAliasName() {
		if (hasAlias()) {
			return super.getAliasName();
		} else {
			return tableName;
		}
	}
}
