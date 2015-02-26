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

import java.util.Map;
import java.util.TreeMap;

public class ParserVisitor2 implements IVisitor {
	private Map<Integer,INode> map = new TreeMap<Integer, INode>();

	private int index = -1;

	public Object visit(INode node, Object data) {
		setCurrentNode(node);
		node.childrenAccept(this, data);
		return data;
	}

	private void setCurrentNode(INode node) {
		index++;
		node.setId(index);
		map.put(new Integer(node.getOffset()), node);
	}

	public INode findNodeByOffset(int offset) {
		return (INode)map.get(new Integer(offset));
	}

	public INode findNodeByOffset(int offset, int length) {
		INode node = findNodeByOffset(offset);
		if(node != null){
			if(node.getLength() == length){
				return node;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	public INode findNode(int offset) {
		throw new UnsupportedOperationException("UnSupported Method");
	}

	public int getIndex() {
		throw new UnsupportedOperationException("UnSupported Method");
	}
}
