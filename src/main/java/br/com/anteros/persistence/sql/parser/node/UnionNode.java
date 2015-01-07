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



public class UnionNode extends KeywordNode {

	private boolean isAll;

	public UnionNode(int offset, int length, int scope) {
		super("union", offset, length, scope);
	}
	public UnionNode(int offset, int length, int scope, boolean isAll) {
		super("union", offset, length, scope);
		this.isAll =isAll;
	}
	
	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getNodeClassName());
		if (isAll) {
			sb.append(" All");
		}
		return getNodeClassName() + " text=\"" + sb.toString() + "\"";
	}

	public boolean isAll() {
		return isAll;
	}
}
