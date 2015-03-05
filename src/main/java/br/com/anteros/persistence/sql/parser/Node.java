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
import br.com.anteros.persistence.sql.parser.exception.NotFoundParentNodeException;
import br.com.anteros.persistence.sql.parser.exception.ParserException;
import br.com.anteros.persistence.sql.parser.node.AliasNode;
import br.com.anteros.persistence.sql.parser.node.CreateStatementNode;
import br.com.anteros.persistence.sql.parser.node.DeleteStatementNode;
import br.com.anteros.persistence.sql.parser.node.DropStatementNode;
import br.com.anteros.persistence.sql.parser.node.ExpressionNode;
import br.com.anteros.persistence.sql.parser.node.InsertStatementNode;
import br.com.anteros.persistence.sql.parser.node.ParenthesesNode;
import br.com.anteros.persistence.sql.parser.node.SelectStatementNode;
import br.com.anteros.persistence.sql.parser.node.StatementNode;
import br.com.anteros.persistence.sql.parser.node.UpdateStatementNode;

public class Node implements INode {
	
	private static Logger LOG = LoggerProvider.getInstance().getLogger(Node.class.getName());

	protected INode parent;

	protected List<INode> children;

	protected String name;

	protected int offset;

	protected int length;

	protected int id;

	protected int scope;

	public Node(String name) {
		this(name, 0, 0, ISqlParser.SCOPE_DEFAULT);
	}

	public Node(String name, int offset, int length, int scope) {
		this.name = name;
		this.offset = offset;
		this.length = length;
		this.scope = scope;
	}

	public String getName() {
		return name;
	}

	public void setParent(INode n) {
		parent = n;
	}

	public INode getParent() {
		return parent;
	}

	public void addChild(INode n) {
		if (n != null) {
			if (children == null) {
				children = new ArrayList<INode>();
			}
			children.add(n);
			n.setParent(this);

		}
	}

	public void removeChild(INode child) {
		children.remove(child);
		if (child != null) {
			child.setParent(null);
		}
	}

	public INode getChild(int i) {
		return (INode) children.get(i);
	}

	public INode getLastChild() {
		if (children == null || children.size() == 0) {
			return null;
		} else {
			return (INode) children.get(children.size() - 1);
		}
	}

	public int getChildrenSize() {
		return (children == null) ? 0 : children.size();
	}

	public List<INode> getChildren() {
		return children;
	}

	public Object accept(IVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Object childrenAccept(IVisitor visitor, Object data) {
		try {
			if (children != null) {
				for (Iterator<INode> iter = children.iterator(); iter.hasNext();) {
					INode node = (INode) iter.next();
					node.accept(visitor, data);

				}
			}
		} catch (RuntimeException e) {
			;
		}
		return data;
	}

	public String getNodeClassName() {
		String name = getClass().getName();
		return name = name.substring(name.lastIndexOf(".") + 1);
	}

	public String toString() {
		return getNodeClassName() + " text=\"" + getName() + "\"";
	}

	public String toString(String prefix) {
		return prefix + toString();
	}

	public void dump(String prefix) {
		LOG.info(toString(prefix));
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.size(); ++i) {
				Node n = (Node) children.get(i);
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}

	public StatementNode getStatement() throws ParserException {
		return getStatement(this);
	}

	private StatementNode getStatement(INode node) throws ParserException {
		if (node instanceof StatementNode) {
			return (StatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getStatement(node.getParent());
			} else {
				return null;
			}
		}
	}

	public SelectStatementNode getSelectStatement() throws ParserException {
		return getSelectStatement(this);
	}

	private SelectStatementNode getSelectStatement(INode node) throws ParserException {
		if (node instanceof SelectStatementNode) {
			return (SelectStatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getSelectStatement(node.getParent());
			} else {
				throw new NotFoundParentNodeException("ASTSelectStatement");
			}
		}
	}

	public InsertStatementNode getInsertStatement() throws ParserException {
		return getInsertStatement(this);
	}

	private InsertStatementNode getInsertStatement(INode node) throws ParserException {
		if (node instanceof InsertStatementNode) {
			return (InsertStatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getInsertStatement(node.getParent());
			} else {
				throw new NotFoundParentNodeException("ASTInsertStatement");
			}
		}
	}

	public UpdateStatementNode getUpdateStatement() throws ParserException {
		return getUpdateStatement(this);
	}

	private UpdateStatementNode getUpdateStatement(INode node) throws ParserException {
		if (node instanceof UpdateStatementNode) {
			return (UpdateStatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getUpdateStatement(node.getParent());
			} else {
				throw new NotFoundParentNodeException("ASTUpdateStatement");
			}
		}
	}

	public DeleteStatementNode getDeleteStatement() throws ParserException {
		return getDeleteStatement(this);
	}

	private DeleteStatementNode getDeleteStatement(INode node) throws ParserException {
		if (node instanceof DeleteStatementNode) {
			return (DeleteStatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getDeleteStatement(node.getParent());
			} else {
				throw new NotFoundParentNodeException("ASTDeleteStatement");
			}
		}
	}

	public CreateStatementNode getCreateStatement() throws ParserException {
		return getCreateStatement(this);
	}

	private CreateStatementNode getCreateStatement(INode node) throws ParserException {
		if (node instanceof CreateStatementNode) {
			return (CreateStatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getCreateStatement(node.getParent());
			} else {
				throw new NotFoundParentNodeException("ASTCreateStatement");
			}
		}
	}

	public DropStatementNode getDropStatement() throws ParserException {
		return getDropStatement(this);
	}

	private DropStatementNode getDropStatement(INode node) throws ParserException {
		if (node instanceof DropStatementNode) {
			return (DropStatementNode) node;
		} else {
			if (node.getParent() != null) {
				return getDropStatement(node.getParent());
			} else {
				throw new NotFoundParentNodeException("ASTDropStatement");
			}
		}
	}

	public ParenthesesNode getParentheses() throws ParserException {
		return getParentheses(this);
	}

	private ParenthesesNode getParentheses(INode node) throws ParserException {
		if (node instanceof ParenthesesNode) {
			return (ParenthesesNode) node;
		} else {
			if (node.getParent() != null) {
				return getParentheses(node.getParent());
			} else {
				return null;
			}
		}
	}

	public ExpressionNode getExpression() throws ParserException {
		return getExpression(this);
	}

	private ExpressionNode getExpression(INode node) throws ParserException {
		if (node instanceof ExpressionNode) {
			return (ExpressionNode) node;
		} else if (node instanceof StatementNode) {
			return null;
		} else {
			if (node.getParent() != null) {
				return getExpression(node.getParent());
			} else {
				return null;
			}
		}
	}

	public AliasNode getAlias() throws ParserException {
		return getAlias(this);
	}

	private AliasNode getAlias(INode node) throws ParserException {
		if (node instanceof AliasNode) {
			return (AliasNode) node;
		} else {
			if (node.getParent() != null) {
				return getAlias(node.getParent());
			} else {
				return null;
			}
		}
	}

	public void addChildToStatement(INode addNode) {
		StatementNode st = getStatement();
		if (st != null)
			st.addChild(addNode);
	}

	public void addChildToStatementParent(INode addNode) {
		StatementNode st = getStatement();
		if (st != null && st.getParent() != null) {
			st.getParent().addChild(addNode);
		}
	}

	public INode getChild(String nodeName) {
		if (children != null) {
			for (Iterator<INode> iter = children.iterator(); iter.hasNext();) {
				INode n = (INode) iter.next();
				if (n.getNodeClassName().equals(nodeName)) {
					return n;
				}

			}
		}
		return null;
	}

	public final int getOffset() {
		return offset;
	}

	public final int getLength() {
		return length;
	}

	public final int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public final int getScope() {
		return scope;
	}
	
	public int getMaxOffset() {
		int result = 0;
		Node lastChild = (Node) this.getLastChild();

		if (lastChild != null) {
			int offSetLastChild = lastChild.getOffset();
			int lengthLastChild = lastChild.getLength();

			if (lastChild instanceof ParenthesesNode) {
				offSetLastChild = ((ParenthesesNode) lastChild).getEndOffset();
			}
			result = lastChild.getMaxOffset();
			result = Math.max(result, offSetLastChild + lengthLastChild);
		} else {
			int offSetNode = this.getOffset();
			int lengthNode = this.getLength();

			if (this instanceof ParenthesesNode) {
				offSetNode = ((ParenthesesNode) this).getEndOffset();
			}
			result = offSetNode + lengthNode;
		}
		return result;

	}
}
