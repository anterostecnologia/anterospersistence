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
import java.util.List;

import br.com.anteros.persistence.sql.parser.node.ExpressionNode;
import br.com.anteros.persistence.sql.parser.node.FunctionNode;
import br.com.anteros.persistence.sql.parser.node.GroupbyNode;
import br.com.anteros.persistence.sql.parser.node.HavingNode;
import br.com.anteros.persistence.sql.parser.node.OperatorNode;
import br.com.anteros.persistence.sql.parser.node.ParenthesesNode;
import br.com.anteros.persistence.sql.parser.node.SelectNode;
import br.com.anteros.persistence.sql.parser.node.WhereNode;

public class ParserUtil {

	public static INode findParent(INode node, String type) {
		if (node == null || type.equals(node.getNodeClassName())) {
			return node;
		} else {
			return findParent((INode) node.getParent(), type);
		}
	}

	public static INode findChildDepth(INode current, String key) {
		List<INode> list = current.getChildren();
		if (list == null)
			return null;

		for (int i = 0; i < list.size(); i++) {
			if (current.getChild(i).getNodeClassName().equals(key))
				return current.getChild(i);

			INode node = findChildDepth(current.getChild(i), key);
			if (node != null)
				return node;
		}

		return null;
	}

	public static INode findChildWide(INode current, String key) {
		List<INode> list = current.getChildren();
		if (list == null)
			return null;

		for (int i = 0; i < list.size(); i++) {
			if (current.getChild(i).getNodeClassName().equals(key))
				return current.getChild(i);
		}

		for (int i = 0; i < list.size(); i++) {
			INode node = findChildWide(current.getChild(i), key);
			if (node != null)
				return node;
		}

		return null;
	}

	public static INode findFirstChild(INode node, String type) {
		INode[] nodes = findChildren(node, type);
		if (nodes != null && nodes.length > 0) {
			return nodes[0];
		} else {
			return null;
		}
	}
	
	public static INode[] findChildren(INode node, String type) {
		List<INode> list = new ArrayList<INode>();
		if (node != null) {
			for (int i = 0; i < node.getChildrenSize(); ++i) {
				INode n = (INode) node.getChild(i);
				if (n.getNodeClassName().equals(type)) {
					list.add(n);
				} else {
					INode[] nn = findChildren(n, type);
					if (nn != null) {
						for (int j = 0; j < nn.length; j++) {
							list.add(nn[j]);
						}
					}
				}
			}
		}
		return (INode[]) list.toArray(new INode[0]);
	}

	public static INode findBindParent(INode node) {
		INode result = null;
		if (node.getParent() instanceof ParenthesesNode)
			result = findBindParent(node.getParent());
		if (node.getParent() instanceof OperatorNode)
			result = findBindParent(node.getParent());
		else if (node.getParent() instanceof FunctionNode)
			result = node.getParent();
		else if (node.getParent() instanceof ExpressionNode)
			result = node.getParent();
		else if (node.getParent() instanceof GroupbyNode)
			result = node.getParent();
		else if (node.getParent() instanceof HavingNode)
			result = node.getParent();
		else if (node.getParent() instanceof SelectNode)
			result = node.getParent();
		else if (node.getParent() instanceof WhereNode)
			result = node.getParent();

		return result;
	}
	

}
