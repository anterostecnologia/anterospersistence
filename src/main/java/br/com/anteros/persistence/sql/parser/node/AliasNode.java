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

import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.Node;
import br.com.anteros.persistence.sql.parser.exception.ExistAliasNameException;

abstract public class AliasNode extends Node {

	protected boolean alias;

	protected INode aliasNode;

	protected boolean updatableAliasName = true;

	public AliasNode(String id, int offset, int length, int scope) {
		super(id, offset, length, scope);
	}

	public String getAliasName() {
		return aliasNode == null ? null : aliasNode.getName();
	}

	public void setAliasName(String aliasName, int offset, int length) {
		if (hasAlias()) {
			throw new ExistAliasNameException(aliasName, offset, length);
		}

		if (updatableAliasName) {
			aliasNode = new InnerAliasNode(aliasName, offset, length, scope);
			this.addChild(aliasNode);
		}
		alias = true;
	}

	public boolean hasAlias() {
		return alias;
	}

	public boolean isUpdatableAliasName() {
		return updatableAliasName;
	}

	public void setUpdatableAliasName(boolean updatable) {
		this.updatableAliasName = updatable;
	}

	public int getAliasLength() {
		return aliasNode == null ? 0 : aliasNode.getLength();
	}

	public int getAliasOffset() {
		return aliasNode == null ? 0 : aliasNode.getOffset();
	}

}
