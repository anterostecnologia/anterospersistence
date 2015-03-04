/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2007 - 2009 ZIGEN Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package br.com.anteros.persistence.sql.parser;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.sql.parser.node.AliasNode;
import br.com.anteros.persistence.sql.parser.node.CaseCauseNode;
import br.com.anteros.persistence.sql.parser.node.ExpressionNode;
import br.com.anteros.persistence.sql.parser.node.FunctionNode;
import br.com.anteros.persistence.sql.parser.node.InnerAliasNode;
import br.com.anteros.persistence.sql.parser.node.OperatorNode;
import br.com.anteros.persistence.sql.parser.node.ParenthesesNode;
import br.com.anteros.persistence.sql.parser.node.RootNode;
import br.com.anteros.persistence.sql.parser.node.SelectStatementNode;
import br.com.anteros.persistence.sql.parser.node.TableNode;

public class ParserVisitorPrint implements IVisitor {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(ParserVisitorPrint.class.getName());

	StringBuilder sb = new StringBuilder();

	boolean isShowAs = false;

	public INode findNode(int offset) {
		throw new UnsupportedOperationException("UnSupported Method");
	}

	public int getIndex() {
		throw new UnsupportedOperationException("UnSupported Method");
	}

	private void setAliasName(AliasNode target) {
		if (target.hasAlias()) {
			if (isShowAs) {
				sb.append("AS ");
			}
			sb.append(target.getAliasName() + " ");
		}
	}

	public Object visit(INode node, Object data) {

		if (node instanceof RootNode || node instanceof SelectStatementNode || node instanceof ExpressionNode) {

			node.childrenAccept(this, data);

		} else if (node instanceof TableNode) {
			TableNode table = (TableNode) node;
			sb.append(table.getName() + " ");

			setAliasName(table);

			node.childrenAccept(this, data);

		} else if (node instanceof FunctionNode) {
			FunctionNode function = (FunctionNode) node;
			sb.append(function.getName() + "");
			node.childrenAccept(this, data);


		} else if (node instanceof ParenthesesNode) {
			ParenthesesNode p = (ParenthesesNode) node;
			sb.append("(");
			node.childrenAccept(this, data);
			sb.deleteCharAt(sb.toString().length() - 1);
			sb.append(") ");

			setAliasName(p);

		} else if (node instanceof OperatorNode) {

			OperatorNode ope = (OperatorNode) node;
			if (ope.hasRightChild()) {

				node.getChild(0).accept(this, null);
				sb.append(node.getName() + " ");
				node.getChild(1).accept(this, null);

			} else if (ope.hasLeftChild()) {

				node.getChild(0).accept(this, null);
				sb.append(node.getName() + " ");

			} else {
				sb.append(node.getName() + " ");
				node.childrenAccept(this, data);
			}

		} else if (node instanceof CaseCauseNode) {
			CaseCauseNode cc = (CaseCauseNode) node;
			node.childrenAccept(this, data);
			sb.deleteCharAt(sb.toString().length() - 1);

			if (cc.isComplete()) {
				sb.append(" end ");
			}

			setAliasName(cc);
		} else if (node instanceof InnerAliasNode) {
			if (isShowAs)
				sb.append("AS ");
			sb.append(node.getName() + " ");
		} else {
			sb.append(node.getName() + " ");
			node.childrenAccept(this, data);
		}

		return data;
	}

	public void print() {
		LOG.info(sb.toString());
	}
}
