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


public class FunctionNode extends AliasNode {

	public FunctionNode(String functionName, int offset, int length, int scope) {
		super(functionName, offset, length, scope);
	}


	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		if (getAliasName() != null) {
			sb.append(" AS ");
			sb.append(getAliasName());
		}
		return getNodeClassName() + " text=\"" + sb.toString() + "\"";
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public String getName() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		return sb.toString();
	}

}