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

package br.com.anteros.persistence.sql.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.sql.parser.node.BindNode;

public class ParserVisitorToBind implements IVisitor {
	
	private static Logger LOG = LoggerProvider.getInstance().getLogger(ParserVisitorToBind.class.getName());

	List<INode> list = null;

	boolean isShowAs = false;

	public ParserVisitorToBind() {
	}

	public INode findNode(int offset) {
		throw new UnsupportedOperationException("UnSupported Method");
	}

	public int getIndex() {
		throw new UnsupportedOperationException("UnSupported Method");
	}

	public Object visit(INode node, Object data) {
		if (node instanceof BindNode) {
			if (list == null) {
				list = new ArrayList<INode>();
			}
			list.add(node);
		}
		node.childrenAccept(this, data);
		return data;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			int i = 0;
			for (Iterator<INode> iterator = list.iterator(); iterator.hasNext();) {
				BindNode bindNode = (BindNode) iterator.next();
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(bindNode.toString());
				i++;
			}
		}
		return sb.toString();
	}

	public void print() {
		if (list != null) {
			for (Iterator<INode> iterator = list.iterator(); iterator.hasNext();) {
				BindNode bindNode = (BindNode) iterator.next();
				LOG.debug(bindNode.toString());
			}
		}
	}
}
