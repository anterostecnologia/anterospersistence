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

import java.util.List;

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

public interface INode {

	public void setParent(INode n);

	public INode getParent();

	public void addChild(INode n);

	public INode getChild(int i);

	public int getChildrenSize();

	public INode getLastChild();

	public List<INode> getChildren();

	public int getOffset();

	public int getLength();

	public Object accept(IVisitor visitor, Object data);

	public Object childrenAccept(IVisitor visitor, Object data);

	public void removeChild(INode child);

	public String getNodeClassName();

	public String getName();

	public StatementNode getStatement() throws ParserException;

	public SelectStatementNode getSelectStatement() throws ParserException;

	public InsertStatementNode getInsertStatement() throws ParserException;

	public UpdateStatementNode getUpdateStatement() throws ParserException;

	public DeleteStatementNode getDeleteStatement() throws ParserException;

	public CreateStatementNode getCreateStatement() throws ParserException;

	public DropStatementNode getDropStatement() throws ParserException;

	public ParenthesesNode getParentheses() throws ParserException;

	public AliasNode getAlias() throws ParserException;

	public ExpressionNode getExpression() throws ParserException;

	public void addChildToStatement(INode addNode);

	public void addChildToStatementParent(INode addNode);

	public INode getChild(String nodeName);

	public void setId(int id);

	public int getId();

	public int getScope();
}
