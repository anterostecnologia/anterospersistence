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

/*******************************************************************************
 * Copyright (c) 2007 - 2009 ZIGEN
 * Eclipse Public License - v 1.0
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package br.com.anteros.persistence.sql.parser.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.Node;


public class OperatorNode extends Node {

	public static final String[] OPERATOR = { "!=", "<", "<=", "<>", "=", ">", ">=", "^=" };

	public static final String[] OPERATOR1 = { "+", "-" };

	public static final String[] OPERATOR2 = { "*", "/" };

	static Map<String,String> map = new HashMap<String,String>();

	static Map<String,String> map1 = new HashMap<String,String>();

	static Map<String,String> map2 = new HashMap<String,String>();
	static {
		for (int i = 0; i < OPERATOR.length; i++) {
			map.put(OPERATOR[i], OPERATOR[i]);
		}
		for (int i = 0; i < OPERATOR1.length; i++) {
			map1.put(OPERATOR1[i], OPERATOR1[i]);
		}
		for (int i = 0; i < OPERATOR2.length; i++) {
			map2.put(OPERATOR2[i], OPERATOR2[i]);
		}
	}

	public static int PRIORITY = 0;

	public static int PRIORITY_1 = 1; // + -

	public static int PRIORITY_2 = 2; // * /

	private int priority = PRIORITY;

	public OperatorNode(String s, int offset, int length, int scope) {
		super(s, offset, length, scope);

		if (map1.containsKey(getName())) {
			priority = PRIORITY_1;
		} else if (map2.containsKey(getName())) {
			priority = PRIORITY_2;
		} else if (map.containsKey(getName())) {
			priority = PRIORITY;
		}
	}

	public void addChild(INode n) {
		if (n != null) {
			if (children == null) {
				children = new ArrayList<INode>();
			}

			if (hasRightChild()) {

				System.err.println(n.getName() + ", " + n.getClass().getName());

			} else {
				children.add(n);
				n.setParent(this);
			}
		}
	}

	public int compare(OperatorNode ope) {
		return (getPriority() - ope.priority);
	}

	public boolean hasLeftChild() {
		return (children != null && children.size() >= 1);
	}

	public boolean hasRightChild() {
		return (children != null && children.size() == 2);
	}

	public int getPriority() {
		return priority;
	}


	public String toString() {
		return getNodeClassName() + " text=\"" + name + "\"";
	}

}
