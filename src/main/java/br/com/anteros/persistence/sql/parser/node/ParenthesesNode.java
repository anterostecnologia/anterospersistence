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

import br.com.anteros.persistence.sql.parser.ParserVisitor;


public class ParenthesesNode extends AliasNode {

	private int endOffset;

	private int scope = 0;

	private boolean forFunction;

	public ParenthesesNode(int offset, int length, int scope) {
		super("ParserParentheses", offset, length, scope);
	}

	public String getName(){
		return "()";
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getAliasName() != null) {
			sb.append(getAliasName());
		}
		return getNodeClassName() + " text=\"" + sb.toString() + "\"";
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public boolean isForFunction() {
		return forFunction;
	}

	public void setFunction(FunctionNode function) {
		this.function = function;
		this.forFunction = true;
	}

	FunctionNode function;
	public FunctionNode getFunction(){
		return function;
	}

	public boolean isOpen(){
		return (endOffset == 0);
	}

}
