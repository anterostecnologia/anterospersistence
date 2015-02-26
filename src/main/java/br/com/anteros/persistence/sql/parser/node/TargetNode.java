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
package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.Node;
import br.com.anteros.persistence.sql.parser.ParserVisitor;

public class TargetNode extends Node {

	private String schemaName;

	private String createName;

	public TargetNode(String plsqlName,int offset, int length, int scope) {
		super(plsqlName, offset, length, scope);
		parse(plsqlName);
	}

	private void parse(String tableName) {
		String[] strs = tableName.split("[.]");
		if (strs.length == 2) {
			this.schemaName = strs[0];
			this.createName = strs[1];

		} else if (strs.length == 1) {
			this.createName = strs[0];
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (schemaName != null) {
			sb.append(schemaName);
			sb.append(".");
		}
		if (createName != null) {
			sb.append(createName);
		}
		return getNodeClassName() + " text=\"" + sb.toString() + "\"";
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCreateName() {
		return createName;
	}

}
