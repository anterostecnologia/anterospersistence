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

import java.util.HashMap;
import java.util.Map;

import br.com.anteros.persistence.sql.format.SqlFormatRule;
import br.com.anteros.persistence.sql.format.tokenizer.SqlTokenizer;
import br.com.anteros.persistence.sql.format.tokenizer.Token;
import br.com.anteros.persistence.sql.format.tokenizer.TokenUtil;
import br.com.anteros.persistence.sql.parser.exception.LoopException;
import br.com.anteros.persistence.sql.parser.exception.ParserException;
import br.com.anteros.persistence.sql.parser.exception.UnexpectedTokenException;
import br.com.anteros.persistence.sql.parser.node.AliasNode;
import br.com.anteros.persistence.sql.parser.node.BindNode;
import br.com.anteros.persistence.sql.parser.node.CaseCauseNode;
import br.com.anteros.persistence.sql.parser.node.CaseNode;
import br.com.anteros.persistence.sql.parser.node.CastFunctionNode;
import br.com.anteros.persistence.sql.parser.node.ColumnNode;
import br.com.anteros.persistence.sql.parser.node.CommaNode;
import br.com.anteros.persistence.sql.parser.node.CommentNode;
import br.com.anteros.persistence.sql.parser.node.CreateStatementNode;
import br.com.anteros.persistence.sql.parser.node.DeleteStatementNode;
import br.com.anteros.persistence.sql.parser.node.DropStatementNode;
import br.com.anteros.persistence.sql.parser.node.ElseNode;
import br.com.anteros.persistence.sql.parser.node.ExpressionNode;
import br.com.anteros.persistence.sql.parser.node.FromNode;
import br.com.anteros.persistence.sql.parser.node.FunctionNode;
import br.com.anteros.persistence.sql.parser.node.GroupbyNode;
import br.com.anteros.persistence.sql.parser.node.HavingNode;
import br.com.anteros.persistence.sql.parser.node.InnerAliasNode;
import br.com.anteros.persistence.sql.parser.node.InsertStatementNode;
import br.com.anteros.persistence.sql.parser.node.IntoNode;
import br.com.anteros.persistence.sql.parser.node.JoinNode;
import br.com.anteros.persistence.sql.parser.node.KeywordNode;
import br.com.anteros.persistence.sql.parser.node.LimitNode;
import br.com.anteros.persistence.sql.parser.node.MinusNode;
import br.com.anteros.persistence.sql.parser.node.OffsetNode;
import br.com.anteros.persistence.sql.parser.node.OnNode;
import br.com.anteros.persistence.sql.parser.node.OperatorNode;
import br.com.anteros.persistence.sql.parser.node.OrderbyNode;
import br.com.anteros.persistence.sql.parser.node.OutfileNode;
import br.com.anteros.persistence.sql.parser.node.OverNode;
import br.com.anteros.persistence.sql.parser.node.ParenthesesNode;
import br.com.anteros.persistence.sql.parser.node.PartitionByNode;
import br.com.anteros.persistence.sql.parser.node.RootNode;
import br.com.anteros.persistence.sql.parser.node.SelectNode;
import br.com.anteros.persistence.sql.parser.node.SelectStatementNode;
import br.com.anteros.persistence.sql.parser.node.SetNode;
import br.com.anteros.persistence.sql.parser.node.StatementNode;
import br.com.anteros.persistence.sql.parser.node.SymbolNode;
import br.com.anteros.persistence.sql.parser.node.TableNode;
import br.com.anteros.persistence.sql.parser.node.TargetNode;
import br.com.anteros.persistence.sql.parser.node.ThenNode;
import br.com.anteros.persistence.sql.parser.node.TypeNode;
import br.com.anteros.persistence.sql.parser.node.UnionNode;
import br.com.anteros.persistence.sql.parser.node.UpdateStatementNode;
import br.com.anteros.persistence.sql.parser.node.ValueNode;
import br.com.anteros.persistence.sql.parser.node.ValuesNode;
import br.com.anteros.persistence.sql.parser.node.WhenNode;
import br.com.anteros.persistence.sql.parser.node.WhereNode;
import br.com.anteros.persistence.sql.parser.node.WithNode;

public class SqlParser implements ISqlParser {

	protected SqlTokenizer tokenizer;

	protected int offset = 0;

	protected int length = 0;

	protected int scope = SCOPE_DEFAULT;

	public SqlParser(String sql, SqlFormatRule rule) {
		this.tokenizer = new SqlTokenizer(sql, rule);
	}

	public boolean isCanceled() {
		return false;
	}

	public SqlParser() {
	}

	public void parse(INode node) throws ParserException {
		INode x;

		for (;;) {
			if (isCanceled())
				return;

			int next = nextToken();

			switch (next) {
			case TokenUtil.TYPE_END_SQL:
				return;
			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();

					if (begin.getParent() instanceof FunctionNode) {
						parse(begin.getParent().getParent());
					} else {
						parse(begin.getParent());
					}

					return;
				} else if ("(".equals(getToken())) {
					INode lastNode = node.getLastChild();
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					if (lastNode instanceof FunctionNode) {
						p.setFunction((FunctionNode) lastNode);
						lastNode.addChild(p);
					} else {
						node.addChild(p);
					}
					parse(p);

					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				} else if ("(*)".equals(getToken())) {
					INode lastNode = node.getLastChild();
					if (lastNode instanceof FunctionNode) {
						ParenthesesNode p = new ParenthesesNode(offset, length, scope);
						ColumnNode col = new ColumnNode("*", offset, length, scope);
						p.addChild(col);
						p.setFunction((FunctionNode) lastNode);
						lastNode.addChild(p);
						parse(lastNode);
					} else {
						throw new UnexpectedTokenException(getToken(), offset, length);
					}

				} else if ("||".equals(getToken())) {
					INode lastNode = node.getLastChild();
					if (lastNode instanceof ColumnNode) {
						ColumnNode col = (ColumnNode) lastNode;
						col.addConcat(getToken(), offset, length);

					} else {
						SymbolNode symbolNode = new SymbolNode(getToken(), offset, length, scope);
						node.addChild(symbolNode);
					}
				} else {
				}
				break;
			case TokenUtil.TYPE_OPERATOR:
				tokenizer.pushBack();
				parseExpression(node);
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;

					INode last = node.getLastChild();
					if (last instanceof UnionNode) {
						SelectStatementNode ss = new SelectStatementNode(offset, length, scope);
						SelectNode select = new SelectNode(offset, length, scope);
						ss.addChild(select);
						last.getParent().addChild(ss);
						parseSelectStatement(select);
						break;
					} else {
						SelectStatementNode ss = new SelectStatementNode(offset, length, scope);
						SelectNode select = new SelectNode(offset, length, scope);
						ss.addChild(select);
						node.addChild(ss);
						parseSelectStatement(select);
						break;
					}
				} else if ("insert".equalsIgnoreCase(getToken())) {
					scope = SCOPE_INSERT;
					InsertStatementNode st = new InsertStatementNode(offset, length, scope);
					node.addChild(st);
					parseInsertStatement(st);
					break;

				} else if ("update".equalsIgnoreCase(getToken())) {
					scope = SCOPE_UPDATE;
					UpdateStatementNode st = new UpdateStatementNode(offset, length, scope);
					node.addChild(st);
					parseUpdateStatement(st);
					break;

				} else if ("delete".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DELETE;
					DeleteStatementNode st = new DeleteStatementNode(offset, length, scope);
					node.addChild(st);
					parseDeleteStatement(st);
					break;

				} else if ("create".equalsIgnoreCase(getToken())) {
					scope = SCOPE_CREATE;
					CreateStatementNode st = new CreateStatementNode(offset, length, scope);
					node.addChild(st);
					parseCreateStatement(st);
					break;

				} else if ("create or replace".equalsIgnoreCase(getToken())) {
					scope = SCOPE_CREATE;
					CreateStatementNode st = new CreateStatementNode(offset, length, scope);
					st.changeCreateOrReplace();

					node.addChild(st);
					parseCreateStatement(st);
					break;

				} else if ("drop".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DROP;
					DropStatementNode st = new DropStatementNode(offset, length, scope);
					node.addChild(st);
					parseDropStatement(st);
					break;

				} else if ("where".equalsIgnoreCase(getToken())) {
					scope = SCOPE_WHERE;
					WhereNode where = new WhereNode(offset, length, scope);
					node.addChildToStatement(where);
					parseWhereClause(where);
					break;
				} else if ("union".equalsIgnoreCase(getToken())) {
					parseUnion(node, false);

				} else if ("union all".equalsIgnoreCase(getToken())) {
					parseUnion(node, true);

				} else if ("minus".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					node.addChild(new MinusNode(offset, length, scope));
				} else if ("limit".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					LimitNode limit = new LimitNode(offset, length, scope);
					node.addChildToStatement(limit);
					parseLimitClause(limit);
					return;

				} else if ("from".equalsIgnoreCase(getToken())) {
					scope = SCOPE_FROM;
					FromNode from = new FromNode(offset, length, scope);
					node.addChildToStatement(from);

					parseFromClause(from);
					return;

				} else if ("order by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;

					OrderbyNode by = new OrderbyNode(offset, length, scope);

					if (node instanceof ParenthesesNode) {
						ParenthesesNode p = (ParenthesesNode) node;
						p.addChild(by);
					} else {
						node.addChildToStatement(by);
					}
					parseOrderByClause(by);
					break;

				} else if ("partition by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					PartitionByNode by = new PartitionByNode(offset, length, scope);
					if (node instanceof ParenthesesNode) {
						ParenthesesNode p = (ParenthesesNode) node;
						p.addChild(by);
					} else {
						node.addChildToStatement(by);
					}
					parseOrderByClause(by);
					break;

				} else if ("group by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					GroupbyNode by = new GroupbyNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;
				} else if ("having".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					HavingNode by = new HavingNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;

				} else if ("case".equalsIgnoreCase(getToken())) {
					CaseCauseNode cc = new CaseCauseNode(offset, length, scope);
					node.addChild(cc);

					CaseNode c = new CaseNode(offset, length, scope);
					cc.addChild(c);

					parseCase(cc);
					break;

				} else if ("then".equalsIgnoreCase(getToken()) || "when".equalsIgnoreCase(getToken()) || "else".equalsIgnoreCase(getToken())
						|| "end".equalsIgnoreCase(getToken())) {
					if (node instanceof CaseCauseNode) {
						tokenizer.pushBack();
						parseCase((CaseCauseNode) node);
					} else if (node instanceof CaseNode || node instanceof WhenNode || node instanceof ThenNode || node instanceof ElseNode) {
						tokenizer.pushBack();
						parseCase((CaseCauseNode) node.getParent());
						return;
					} else {
						throw new UnexpectedTokenException(node.getClass().getName(), offset, length);
					}

				} else if ("into".equalsIgnoreCase(getToken())) {

					IntoNode into = new IntoNode(getToken(), offset, length, scope);
					if (node instanceof SelectNode || node instanceof FromNode) {
						node.getSelectStatement().addChild(into);
						parse(into);
					} else {
						node.addChild(new KeywordNode(getToken(), offset, length, scope));
					}

				} else if ("as".equalsIgnoreCase(getToken())) {
					INode last = node.getLastChild();

					if ((node.getParent() != null) && (node.getParent() instanceof CastFunctionNode)) {
						KeywordNode keywordNode = new KeywordNode(getToken(), offset, length, scope);
						node.addChild(keywordNode);
					} else {
						if (last instanceof ParenthesesNode) {
							ParenthesesNode p = (ParenthesesNode) last;
							if (p.isForFunction()) {
								if (nextToken() == TokenUtil.TYPE_NAME) {
									p.setAliasName(getToken(), offset, length);
									parse(node.getParent());
								} else {
									tokenizer.pushBack();
								}
								break;
							}
						}

						if (last instanceof AliasNode) {
							AliasNode aliasNode = (AliasNode) last;
							if (nextToken() == TokenUtil.TYPE_NAME) {
								aliasNode.setAliasName(getToken(), offset, length);
							} else {
								tokenizer.pushBack();
							}
						} else {
							throw new UnexpectedTokenException(node.getClass().getName(), offset, length);
						}
					}

				} else if (token.getSubType() == TokenUtil.SUBTYPE_KEYWORD_FUNCTION) {
					if ("cast".equalsIgnoreCase(getToken())) {
						CastFunctionNode col = new CastFunctionNode(getToken(), offset, length, scope);
						node.addChild(col);
					} else {
						FunctionNode function = new FunctionNode(getToken(), offset, length, scope);
						node.addChild(function);
					}
				} else if ("LEFT JOIN".equalsIgnoreCase(getToken()) || "LEFT OUTER JOIN".equalsIgnoreCase(getToken())
						|| "RIGHT JOIN".equalsIgnoreCase(getToken()) || "RIGHT OUTER JOIN".equalsIgnoreCase(getToken())
						|| "INNER JOIN".equalsIgnoreCase(getToken())) {
					scope = SCOPE_FROM;
					node.addChild(new JoinNode(getToken(), offset, length, scope));

				} else if ("JOIN".equalsIgnoreCase(getToken())) {
					scope = SCOPE_FROM;
					node.addChild(new JoinNode(getToken(), offset, length, scope));

				} else if ("on".equalsIgnoreCase(getToken())) {
					OnNode on = new OnNode(getToken(), offset, length, scope);
					node.addChild(on);

				} else if ("over".equalsIgnoreCase(getToken())) {
					INode last = node.getLastChild();
					OverNode over = new OverNode(getToken(), offset, length, scope);
					node.addChild(over);
					parse(over);

				} else {
					if (node instanceof OperatorNode) {
						tokenizer.pushBack();
						parse(node.getParent());
					} else {
						node.addChild(new KeywordNode(getToken(), offset, length, scope));
					}
				}
				break;

			case TokenUtil.TYPE_NAME:

				INode lastNode = node.getLastChild();

				if ("OUTFILE".equalsIgnoreCase(getToken())) {
					node.addChild(new OutfileNode(getToken(), offset, length, scope));

				} else {

					if (lastNode instanceof AliasNode) {
						if (node instanceof FunctionNode) {
							FunctionNode func = (FunctionNode) node;
							func.setAliasName(getToken(), offset, length);
							parse(func.getParent());
						} else {
							if ("OVER".equalsIgnoreCase(getToken()) && "ROW_NUMBER".equalsIgnoreCase(lastNode.getName())) {
								node.addChild(new FunctionNode(getToken(), offset, length, scope));
							} else {
								((AliasNode) lastNode).setAliasName(getToken(), offset, length);
							}
						}
					} else if (lastNode instanceof OutfileNode) {
						OutfileNode outfile = (OutfileNode) lastNode;
						outfile.setFilePath(getToken());

					} else if (node instanceof FromNode) {
						if ((lastNode instanceof OnNode) || ("AND".equalsIgnoreCase(lastNode.getName()))
								|| ("OR".equalsIgnoreCase(lastNode.getName()))) {
							node.addChild(new ColumnNode(getToken(), offset, length, scope));
						} else {
							node.addChild(new TableNode(getToken(), offset, length, scope));
						}

					} else if (node instanceof OperatorNode) {
						parseValue(lastNode);
					} else {
						node.addChild(new ColumnNode(getToken(), offset, length, scope));
					}
				}

				break;
			case TokenUtil.TYPE_VALUE:
				INode lastNode2 = node.getLastChild();
				if (lastNode2 instanceof AliasNode) {
					if (node instanceof FunctionNode) {
						FunctionNode func = (FunctionNode) node;
						func.setAliasName(getToken(), offset, length);
						parse(func.getParent());
						return;
					} else {
						if ((lastNode2 instanceof ColumnNode) && ((ColumnNode) lastNode2).isConcating()) {
							ColumnNode col = (ColumnNode) lastNode2;
							col.addConcat(getToken(), offset, length);
							col.setConcating(false);
						} else
							((AliasNode) lastNode2).setAliasName(getToken(), offset, length);
					}
				} else if (lastNode2 instanceof OperatorNode) {
					throw new UnexpectedTokenException(getToken(), offset, length);
				} else {
					parseValue(node);
				}

				break;

			default:
				break;
			}
		}
	}

	protected void parseSelectStatement(SelectNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			int next = nextToken();

			switch (next) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					INode lastNode = node.getLastChild();
					if (lastNode instanceof FunctionNode) {
						ParenthesesNode p = new ParenthesesNode(offset, length, scope);
						p.setFunction((FunctionNode) lastNode);
						lastNode.addChild(p);
						parse(p);
					} else {
						if (node instanceof SelectNode) {
							ParenthesesNode p = new ParenthesesNode(offset, length, scope);
							node.addChild(p);
							parse(p);
							break;
						}
						throw new UnexpectedTokenException(node.getClass().getName(), offset, length);
					}
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				} else if ("(*)".equals(getToken())) {
					INode lastNode = node.getLastChild();
					if (lastNode instanceof FunctionNode) {
						ParenthesesNode p = new ParenthesesNode(offset, length, scope);
						ColumnNode col = new ColumnNode("*", offset, length, scope);
						p.addChild(col);
						p.setFunction((FunctionNode) lastNode);
						lastNode.addChild(p);
						parse(lastNode);
					} else {
						throw new UnexpectedTokenException(getToken(), offset, length);
					}

				} else if ("||".equals(getToken())) {
					INode lastNode = node.getLastChild();
					if (lastNode instanceof ColumnNode) {
						ColumnNode col = (ColumnNode) lastNode;
						col.addConcat(getToken(), offset, length);

					} else {
						throw new UnexpectedTokenException(getToken(), offset, length);
					}

				} else {
					throw new UnexpectedTokenException(getToken(), offset, length);

				}
				break;

			case TokenUtil.TYPE_OPERATOR:
				if ("*".equals(getToken())) {
					node.addChild(new ColumnNode(getToken(), offset, length, scope));
				} else {
					tokenizer.pushBack();
					parseExpression(node);
				}
				break;
			case TokenUtil.TYPE_VALUE:
				INode lastNode1 = node.getLastChild();
				if (lastNode1 instanceof ColumnNode) {
					ColumnNode col = (ColumnNode) lastNode1;
					if (col.isConcating()) {
						col.addColumn(getToken(), offset, length);
					} else {
						col.setAliasName(getToken(), offset, length);
					}
				} else if (lastNode1 instanceof AliasNode) {
					((AliasNode) lastNode1).setAliasName(getToken(), offset, length);
				} else {
					parseValue(node);
				}
				break;

			case TokenUtil.TYPE_NAME:
				INode lastNode = node.getLastChild();
				if (lastNode instanceof ColumnNode) {
					ColumnNode col = (ColumnNode) lastNode;
					if (col.isConcating()) {
						col.addColumn(getToken(), offset, length);
					} else {
						col.setAliasName(getToken(), offset, length);
					}
				} else if (lastNode instanceof AliasNode) {
					((AliasNode) lastNode).setAliasName(getToken(), offset, length);
				} else {
					ColumnNode col = new ColumnNode(getToken(), offset, length, scope);
					node.addChild(col);
				}
				break;
			case TokenUtil.TYPE_COMMENT:
				String sComment = getToken();
				CommentNode comment = new CommentNode(sComment, offset, length, scope);
				node.addChild(comment);
				break;
			case TokenUtil.TYPE_KEYWORD:
				if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;
					tokenizer.pushBack();
					parse(node.getStatement().getParent());
					break;
				} else if ("from".equalsIgnoreCase(getToken())) {
					scope = SCOPE_FROM;
					FromNode from = new FromNode(offset, length, scope);
					StatementNode select = node.getStatement();

					if (select != null) {
						FromNode wkFrom = (FromNode) ParserUtil.findChildWide(select, "FromNode");
						if (wkFrom != null && select.getParent() != null) {
							StatementNode pSelect = select.getParent().getStatement();
							pSelect.addChild(from);
						} else {
							select.addChild(from);
						}
						parseFromClause(from);
					}

					break;

				} else if ("union".equalsIgnoreCase(getToken())) {
					parseUnion(node, false);
					break;
				} else if ("union all".equalsIgnoreCase(getToken())) {
					parseUnion(node, true);
					break;

				} else if ("minus".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					node.addChildToStatementParent(new MinusNode(offset, length, scope));

				} else if ("where".equalsIgnoreCase(getToken())) {
					scope = SCOPE_WHERE;
					WhereNode where = new WhereNode(offset, length, scope);
					node.addChildToStatement(where);
					parseWhereClause(where);
					break;

				} else if ("order by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					OrderbyNode by = new OrderbyNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;

				} else if ("group by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					GroupbyNode by = new GroupbyNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;
				} else if ("having".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					HavingNode by = new HavingNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;

				} else if ("case".equalsIgnoreCase(getToken())) {
					CaseCauseNode cc = new CaseCauseNode(offset, length, scope);
					node.addChild(cc);

					CaseNode c = new CaseNode(offset, length, scope);
					cc.addChild(c);

					parseCase(cc);
					break;
				} else if ("cast".equalsIgnoreCase(getToken())) {
					CastFunctionNode col = new CastFunctionNode(getToken(), offset, length, scope);
					node.addChild(col);
				} else {
					if (token.getSubType() == TokenUtil.SUBTYPE_KEYWORD_FUNCTION) {
						FunctionNode col = new FunctionNode(getToken(), offset, length, scope);
						node.addChild(col);
					} else {
						tokenizer.pushBack();
						parse(node);
					}
					break;
				}
			default:
				break;
			}
		}

	}

	protected void parseWithClause(WithNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_NAME:
				INode lastNode = node.getLastChild();
				if (lastNode instanceof AliasNode) {
					((AliasNode) lastNode).setAliasName(getToken(), offset, length);
				} else {
					TableNode table = new TableNode(getToken(), offset, length, scope);
					node.addChild(table);
				}
				break;

			case TokenUtil.TYPE_VALUE:
				INode lastNode2 = node.getLastChild();
				if (lastNode2 instanceof AliasNode) {
					((AliasNode) lastNode2).setAliasName(getToken(), offset, length);
				} else {
					parseValue(node);
				}

				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("as".equalsIgnoreCase(getToken())) {
					node.addChild(new KeywordNode("as", offset, length, scope));
					break;

				} else if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;

					INode last = node.getLastChild();
					if (last instanceof UnionNode) {
						SelectStatementNode ss = new SelectStatementNode(offset, length, scope);
						SelectNode select = new SelectNode(offset, length, scope);
						ss.addChild(select);
						last.getParent().addChild(ss);
						parseSelectStatement(select);
						break;
					} else {

						SelectStatementNode ss = new SelectStatementNode(offset, length, scope);
						SelectNode select = new SelectNode(offset, length, scope);
						ss.addChild(select);
						node.addChild(ss);
						parseSelectStatement(select);
						break;
					}
				} else {
					tokenizer.pushBack();
					parse(node);
					return;
				}
			default:
				break;
			}
		}

	}

	protected void parseFromClause(FromNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			int next = nextToken();

			switch (next) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_NAME:
				INode lastNode = node.getLastChild();
				if (lastNode instanceof AliasNode) {
					((AliasNode) lastNode).setAliasName(getToken(), offset, length);
				} else {
					TableNode table = new TableNode(getToken(), offset, length, scope);
					node.addChild(table);
				}
				break;

			case TokenUtil.TYPE_VALUE:
				INode lastNode2 = node.getLastChild();
				if (lastNode2 instanceof AliasNode) {
					((AliasNode) lastNode2).setAliasName(getToken(), offset, length);
				} else {
					parseValue(node);
				}

				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("where".equalsIgnoreCase(getToken())) {
					scope = SCOPE_WHERE;
					WhereNode where = new WhereNode(offset, length, scope);
					node.addChildToStatement(where);
					parseWhereClause(where);
					break;

				} else if ("union".equalsIgnoreCase(getToken())) {
					parseUnion(node, false);
					break;
				} else if ("union all".equalsIgnoreCase(getToken())) {
					parseUnion(node, true);
					break;
				} else if ("limit".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					LimitNode limit = new LimitNode(offset, length, scope);
					node.addChildToStatement(limit);
					parseLimitClause(limit);
					break;
				} else if ("minus".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					MinusNode minus = new MinusNode(offset, length, scope);

					StatementNode st = node.getStatement();
					if (st != null && st.getParent() != null) {
						st.getParent().addChild(minus);
						parse(st.getParent());
						return;
					} else {
						ParenthesesNode p = node.getParentheses();
						if (p != null) {
							p.addChild(minus);
							parse(p);
							return;
						}
					}
					throw new UnexpectedTokenException(getToken(), offset, length);

				} else {
					tokenizer.pushBack();
					parse(node);
					return;
				}
			default:
				break;
			}
		}

	}

	protected void parseWhereClause(WhereNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;
			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);
					break;

				} else if (",".equals(getToken())) {
					break;
				} else {
					parseOuterJoinForOracle(node);
				}
				break;
			case TokenUtil.TYPE_OPERATOR:
				tokenizer.pushBack();
				parseExpression(node);
				break;
			case TokenUtil.TYPE_NAME:
				node.addChild(new ColumnNode(getToken(), offset, length, scope));
				break;

			case TokenUtil.TYPE_VALUE:
				parseValue(node);
				break;
			case TokenUtil.TYPE_KEYWORD:
				if ("union".equalsIgnoreCase(getToken())) {
					parseUnion(node, false);
					break;
				} else if ("union all".equalsIgnoreCase(getToken())) {
					parseUnion(node, true);
					break;

				} else if ("minus".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					node.addChildToStatementParent(new MinusNode(offset, length, scope));

				} else if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;
					SelectStatementNode ss = new SelectStatementNode(offset, length, scope);
					SelectNode select = new SelectNode(offset, length, scope);
					ss.addChild(select);
					node.addChildToStatementParent(ss);
					parseSelectStatement(select);
					break;

				} else if ("order by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					OrderbyNode by = new OrderbyNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;
				} else if ("limit".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					LimitNode limit = new LimitNode(offset, length, scope);
					node.addChildToStatement(limit);
					parseLimitClause(limit);
					break;

				} else if ("group by".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					GroupbyNode by = new GroupbyNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;
				} else if ("having".equalsIgnoreCase(getToken())) {
					scope = SCOPE_BY;
					HavingNode by = new HavingNode(offset, length, scope);
					node.addChildToStatement(by);
					parseOrderByClause(by);
					break;

				} else {
					tokenizer.pushBack();
					parse(node);
					break;
				}
			}
		}

	}

	protected void parseOrderByClause(OrderbyNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_NAME:
				INode lastNode = node.getLastChild();
				if (lastNode instanceof AliasNode) {
					((AliasNode) lastNode).setAliasName(getToken(), offset, length);
				} else {
					ColumnNode col = new ColumnNode(getToken(), offset, length, scope);
					node.addChild(col);
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("asc".equalsIgnoreCase(getToken()) || ("desc".equalsIgnoreCase(getToken()))) {
					KeywordNode k = new KeywordNode(getToken(), offset, length, scope);
					node.addChild(k);

				} else {
					if (token.getSubType() == TokenUtil.SUBTYPE_KEYWORD_FUNCTION) {
						if ("cast".equalsIgnoreCase(getToken())) {
							CastFunctionNode col = new CastFunctionNode(getToken(), offset, length, scope);
							node.addChild(col);
							parse(col);
						} else {
							FunctionNode col = new FunctionNode(getToken(), offset, length, scope);
							node.addChild(col);
							parse(col);
						}
					} else {
						tokenizer.pushBack();
						parse(node);
					}
				}
				break;
			default:
				break;
			}
		}

	}

	protected void parseLimitClause(LimitNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;
			INode lastNode;

			int next = nextToken();
			switch (next) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_VALUE:
				node.addChild(new ValueNode(getToken(), offset, length, scope));
				break;

			case TokenUtil.TYPE_NAME:
				lastNode = node.getLastChild();
				if (lastNode instanceof AliasNode) {
					((AliasNode) lastNode).setAliasName(getToken(), offset, length);
				} else {
					node.addChild(new ValueNode(getToken(), offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("offset".equalsIgnoreCase(getToken())) {
					scope = SCOPE_DEFAULT;
					OffsetNode os = new OffsetNode(offset, length, scope);
					node.addChildToStatement(os);
					parseOffsetClause(os);
					break;
				} else {
					tokenizer.pushBack();
					return;
				}
			default:
				break;
			}
		}
	}

	protected void parseOffsetClause(OffsetNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			int next = nextToken();
			switch (next) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_VALUE:
				node.addChild(new ValueNode(getToken(), offset, length, scope));
				break;

			case TokenUtil.TYPE_NAME:
				INode lastNode = node.getLastChild();
				if (lastNode instanceof AliasNode) {
					((AliasNode) lastNode).setAliasName(getToken(), offset, length);
				} else {
					ValueNode val = new ValueNode(getToken(), offset, length, scope);
					node.addChild(val);
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				tokenizer.pushBack();
				return;
			default:
				break;
			}
		}
	}

	protected void skipToken(INode node, int offset, int length) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;
			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					return;
				} else if ("(".equals(getToken())) {
					skipToken(node, offset, length);
					break;
				}
				break;
			case TokenUtil.TYPE_KEYWORD:
				if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;
					tokenizer.pushBack();
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parse(p);

				} else {
					tokenizer.pushBack();
					boolean _exit = false;
					while (!_exit) {
						switch (nextToken()) {
						case TokenUtil.TYPE_END_SQL:
							_exit = true;
							break;
						case TokenUtil.TYPE_SYMBOL:
							if (")".equals(getToken())) {
								_exit = true;
								break;
							}
						default:
							break;
						}
					}
					tokenizer.pushBack();
				}
				break;

			default:
				break;
			}
		}

	}

	protected void skipClause(INode node, int offset, int length) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				tokenizer.pushBack();
				return;
			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					tokenizer.pushBack();
					return;
				} else if ("(".equals(getToken())) {
					skipClause(node, offset, length);
					break;
				}
				break;

			case TokenUtil.TYPE_KEYWORD:

				if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;
					tokenizer.pushBack();
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);

					node.addChild(p);
					parse(p);

				} else if ("by".equalsIgnoreCase(getToken())) {
					break;

				} else {
					tokenizer.pushBack();
					boolean _exit = false;
					while (!_exit) {
						switch (nextToken()) {
						case TokenUtil.TYPE_END_SQL:
							_exit = true;
							break;
						case TokenUtil.TYPE_SYMBOL:
							if (")".equals(getToken())) {
								_exit = true;
								break;
							}
						default:
							break;
						}
					}
					tokenizer.pushBack();
				}
				break;

			default:
				break;
			}
		}

	}

	protected void parseInsertStatement(INode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;
			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					parseInsertStatement(node.getParent());
					break;
				} else if ("(".equals(getToken())) {
					if (node instanceof InsertStatementNode) {
						scope = SCOPE_INTO_COLUMNS;
					} else {
						scope = SCOPE_DEFAULT;
					}
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);

					node.addChild(p);
					parseInsertStatement(p);
					break;
				} else if (",".equals(getToken())) {
					if (node instanceof ParenthesesNode) {
						node.addChild(new CommaNode(offset, length, scope));
					}
					break;
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("into".equalsIgnoreCase(getToken())) {
					scope = SCOPE_INTO;
					IntoNode into = new IntoNode(getToken(), offset, length, scope);
					node.getInsertStatement().addChild(into);
					parseInsertStatement(into);
					break;

				} else if ("values".equalsIgnoreCase(getToken())) {
					scope = SCOPE_WHERE;
					ValuesNode values = new ValuesNode(offset, length, scope);
					node.addChild(values);
					parseInsertStatement(values);
					break;

				} else if ("select".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SELECT;
					tokenizer.pushBack();
					parse(node.getInsertStatement());
					break;

				} else {
					break;
				}

			case TokenUtil.TYPE_NAME:
				if (node instanceof IntoNode) {
					node.addChild(new TableNode(getToken(), offset, length, scope));
					parseInsertStatement(node.getInsertStatement());
					break;
				} else if (node instanceof ParenthesesNode) {
					node.addChild(new ColumnNode(getToken(), offset, length, scope));
				} else {
					throw new UnexpectedTokenException(getToken(), offset, length);

				}
				break;
			case TokenUtil.TYPE_VALUE:
				parseValue(node);

				break;

			default:
				break;
			}
		}

	}

	protected void parseUpdateStatement(INode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;
			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					parseUpdateStatement(node.getParent());
					break;
				} else if ("(".equals(getToken())) {
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					node.addChild(p);
					parseUpdateStatement(p);
					break;
				} else if (",".equals(getToken())) {
					node.addChild(new CommaNode(offset, length, scope));
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("set".equalsIgnoreCase(getToken())) {
					scope = SCOPE_SET;
					SetNode set = new SetNode(offset, length, scope);
					node.getUpdateStatement().addChild(set);

					parseUpdateStatement(set);
					break;

				} else if ("where".equalsIgnoreCase(getToken())) {
					scope = SCOPE_WHERE;
					WhereNode where = new WhereNode(offset, length, scope);
					UpdateStatementNode st = node.getUpdateStatement();
					st.addChild(where);
					parseWhereClause(where);
					break;

				} else {
					break;
				}

			case TokenUtil.TYPE_OPERATOR:
				tokenizer.pushBack();
				parseExpression(node);
				break;

			case TokenUtil.TYPE_VALUE:
				parseValue(node);
				break;

			case TokenUtil.TYPE_NAME:
				if (node instanceof UpdateStatementNode) {
					node.addChild(new TableNode(getToken(), offset, length, scope));
					parseUpdateStatement(node.getUpdateStatement());
				} else if (node instanceof SetNode) {
					node.addChild(new ColumnNode(getToken(), offset, length, scope));
				} else if (node instanceof ParenthesesNode) {
					node.addChild(new ColumnNode(getToken(), offset, length, scope));
				} else {
					throw new UnexpectedTokenException(getToken(), offset, length);

				}
				break;

			default:
				break;
			}
		}

	}

	protected void parseDeleteStatement(DeleteStatementNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					skipToken(node, offset, length);
					break;
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("from".equalsIgnoreCase(getToken())) {
					scope = SCOPE_FROM;
					FromNode from = new FromNode(offset, length, scope);
					node.getDeleteStatement().addChild(from);
					parseFromClause(from);
					break;

				} else if ("where".equalsIgnoreCase(getToken())) {
					scope = SCOPE_WHERE;
					WhereNode where = new WhereNode(offset, length, scope);
					DeleteStatementNode ds = node.getDeleteStatement();
					ds.addChild(where);
					parseWhereClause(where);
					break;

				} else {
					break;
				}
			default:
				break;
			}
		}

	}

	public static Map createMap = new HashMap();

	protected void parseCreateStatement(CreateStatementNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_KEYWORD:
				if (!node.hasType()) {
					scope = SCOPE_TARGET;
					node.addChild(new TypeNode(getToken(), offset, length, scope));
				}
				break;
			case TokenUtil.TYPE_NAME:

				INode lastNode = node.getLastChild();
				if (lastNode instanceof TypeNode) {
					if ("body".equalsIgnoreCase(getToken())) {
						TypeNode type = (TypeNode) lastNode;
						type.setPackageBody(true);
					} else {
						TypeNode type = (TypeNode) lastNode;
						if (!type.hasTarget()) {
							TargetNode name = new TargetNode(getToken(), offset, length, scope);
							lastNode.addChild(name);
							break;
						}

					}
				}

			default:
				break;
			}
		}

	}

	protected void parseDropStatement(DropStatementNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_KEYWORD:
				scope = SCOPE_TARGET;
				if ("function".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("function", offset, length, scope);
					node.getDropStatement().addChild(type);
				} else if ("procedure".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("procedure", offset, length, scope);
					node.getDropStatement().addChild(type);
				} else if ("trigger".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("trigger", offset, length, scope);
					node.getDropStatement().addChild(type);
				} else if ("package".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("package", offset, length, scope);
					node.getDropStatement().addChild(type);
				} else if ("table".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("table", offset, length, scope);
					node.getDropStatement().addChild(type);
				} else if ("view".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("view", offset, length, scope);
					node.getDropStatement().addChild(type);
				} else if ("synonym".equalsIgnoreCase(getToken())) {
					TypeNode type = new TypeNode("synonym", offset, length, scope);
					node.getDropStatement().addChild(type);
				}
				break;
			case TokenUtil.TYPE_NAME:
				INode lastNode = node.getLastChild();
				if (lastNode instanceof TypeNode) {
					if ("body".equalsIgnoreCase(getToken())) {
						TypeNode type = (TypeNode) lastNode;
						type.setPackageBody(true);
					} else {
						TargetNode name = new TargetNode(getToken(), offset, length, scope);
						node.addChild(name);
					}
				}
				break;

			default:
				break;
			}
		}

	}

	protected void parseCase(CaseCauseNode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();
					parse(begin.getParent());
					return;
				} else if ("(".equals(getToken())) {
					INode lastNode = node.getLastChild();
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					if (lastNode instanceof FunctionNode) {
						p.setFunction((FunctionNode) lastNode);
						lastNode.addChild(p);
						parse(p);
					} else {
						node.getLastChild().addChild(p);
						parse(p);
					}
					return;
				} else if (",".equals(getToken())) {
					break;
				} else {
					;
				}
				break;
			case TokenUtil.TYPE_OPERATOR:
				tokenizer.pushBack();
				parseExpression(node);
				break;

			case TokenUtil.TYPE_NAME:
				INode n = node.getLastChild();
				n.addChild(new ColumnNode(getToken(), offset, length, scope));
				break;

			case TokenUtil.TYPE_VALUE:
				INode n2 = node.getLastChild();
				if (n2 instanceof AliasNode) {
					AliasNode aliasNode = (AliasNode) n2;
					aliasNode.setAliasName(getToken(), offset, length);

				} else {
					parseValue(n2);
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if ("when".equalsIgnoreCase(getToken())) {
					node.addChild(new WhenNode(offset, length, scope));

				} else if ("then".equalsIgnoreCase(getToken())) {
					node.addChild(new ThenNode(offset, length, scope));

				} else if ("else".equalsIgnoreCase(getToken())) {
					node.addChild(new ElseNode(offset, length, scope));

				} else if ("end".equalsIgnoreCase(getToken())) {
					node.setComplete(true);
					parse(node.getParent()); // ADD
					return;

				} else {
					if (token.getSubType() == TokenUtil.SUBTYPE_KEYWORD_FUNCTION) {
						tokenizer.pushBack();
						parse(node.getLastChild());
						return;
					}
				}
				break;
			}
		}
	}

	protected Token token;

	protected String getToken() {
		return token.getCustom();
	}

	protected String _preToken;

	protected int checkPoint = 0;

	protected static final int MAX_SAME_WORD = 1000;

	protected int nextToken() {
		if (tokenizer.hasNext()) {
			this._preToken = (token != null) ? token.getOriginal() : "";
			this.token = (Token) tokenizer.next();
			this.offset = token.getIndex();
			this.length = token.getOriginalLength();
			if (token.getOriginal().equals(_preToken)) {
				checkPoint++;
			} else {
				checkPoint = 0;
			}
			if (checkPoint > MAX_SAME_WORD) {
				throw new LoopException(MAX_SAME_WORD);
			}

			return token.getType();

		} else {
			return TokenUtil.TYPE_END_SQL;
		}
	}

	protected void parseExpression(INode node) throws ParserException {
		for (;;) {
			if (isCanceled())
				return;

			switch (nextToken()) {
			case TokenUtil.TYPE_END_SQL:
				return;

			case TokenUtil.TYPE_SYMBOL:
				if (")".equals(getToken())) {
					ParenthesesNode begin = node.getParentheses();
					begin.setEndOffset(offset);
					scope = begin.getScope();

					if (begin.isForFunction()) {
						parse(begin.getParent().getParent());
					} else {
						parse(begin.getParent());
					}

					return;
				} else if ("(".equals(getToken())) {
					INode lastNode = node.getLastChild();
					ParenthesesNode p = new ParenthesesNode(offset, length, scope);
					if (lastNode instanceof FunctionNode) {
						p.setFunction((FunctionNode) lastNode);
						lastNode.addChild(p);
						parse(p);
					} else {
						node.addChild(p);
						parse(p);
					}
					return;
				} else if (",".equals(getToken())) {
					if (node.getScope() == SCOPE_FROM) {
						ExpressionNode exp1 = node.getExpression();
						if (exp1 != null) {
							tokenizer.pushBack();
							parse(exp1.getParent());
						}
					} else {
						node.getParent().addChild(new CommaNode(offset, length, scope));
						parse(node.getParent());
					}
					return;
				} else {
					parseOuterJoinForOracle(node);
				}
				break;

			case TokenUtil.TYPE_KEYWORD:
				if (token.getSubType() == TokenUtil.SUBTYPE_KEYWORD_FUNCTION) {
					FunctionNode function = new FunctionNode(getToken(), offset, length, scope);
					node.addChild(function);
					parseExpression(node);
					return;

				} else {
					ExpressionNode exp1 = node.getExpression();
					if (exp1 != null) {
						tokenizer.pushBack();
						parse(exp1.getParent());
					} else {
						tokenizer.pushBack();
						parse(node);
					}
					return;
				}

			case TokenUtil.TYPE_NAME:
				if (node instanceof OperatorNode) {
					OperatorNode ope = (OperatorNode) node;
					if (ope.hasRightChild()) {
						ExpressionNode exp = ope.getExpression();
						exp.setAliasName(getToken(), offset, length);
						parse(exp.getParent());
						return;
					} else {
						ColumnNode col = new ColumnNode(getToken(), offset, length, scope);
						node.addChild(col);
					}
				} else {
					ColumnNode col = new ColumnNode(getToken(), offset, length, scope);
					node.addChild(col);
				}

				break;
			case TokenUtil.TYPE_VALUE:
				parseValue(node);
				break;

			case TokenUtil.TYPE_OPERATOR:
				OperatorNode ope = new OperatorNode(getToken(), offset, length, scope);

				INode lastNode = node.getLastChild();
				if (lastNode instanceof CaseNode || lastNode instanceof WhenNode || lastNode instanceof ThenNode || lastNode instanceof ElseNode) {
					tokenizer.pushBack();
					parseExpression(lastNode);
					return;

				} else if (lastNode instanceof ParenthesesNode && ((ParenthesesNode) lastNode).isForFunction()) {
					ParenthesesNode p = (ParenthesesNode) lastNode;
					ExpressionNode exp = p.getFunction().getExpression();
					if (exp == null) {
						exp = new ExpressionNode(offset, length, scope);
						tokenizer.pushBack();
						parseExpression(changeNode(p.getFunction(), exp));
						return;
					}

					parseExpression(changeNode(p.getFunction(), ope));

				} else if (lastNode instanceof ColumnNode || lastNode instanceof ValueNode || lastNode instanceof FunctionNode
						|| lastNode instanceof ParenthesesNode) {

					if (node.getParent() instanceof ExpressionNode) {

					}

					ExpressionNode exp = node.getExpression();
					if (exp == null) {
						exp = new ExpressionNode(offset, length, scope);
						tokenizer.pushBack();
						parseExpression(changeNode(lastNode, exp));
						return;
					}

					if (node instanceof OperatorNode) {
						OperatorNode pre = (OperatorNode) node;
						if (pre.compare(ope) < 0) {
							parseExpression(changeNode(lastNode, ope));
						} else {
							parseExpression(changeNode(pre, ope));
						}

					} else {
						parseExpression(changeNode(lastNode, ope));
						return;
					}
				}
			}

		}
	}

	protected void parseUnion(INode node, boolean isUnionAll) throws ParserException {
		scope = SCOPE_DEFAULT;
		UnionNode union;
		if (isUnionAll) {
			union = new UnionNode(offset, length, scope, true);
		} else {
			union = new UnionNode(offset, length, scope);
		}

		if (node instanceof RootNode) {
			node.addChild(union);
			parse(node);
			return;
		}

		if (node instanceof ParenthesesNode) {
			ParenthesesNode p = (ParenthesesNode) node;
			p.addChild(union);
			parse(p);
			return;
		}

		StatementNode st = node.getStatement();
		if (st != null && st.getParent() != null) {
			st.getParent().addChild(union);
			parse(st.getParent());
			return;
		} else {
			ParenthesesNode p = node.getParentheses();
			if (p != null) {
				p.addChild(union);
				parse(p);
				return;
			}
		}
		throw new UnexpectedTokenException(getToken(), offset, length);

	}

	protected void parseOuterJoinForOracle(INode node) {
		if ("(+)".equals(getToken())) {
			INode lastNode = node.getLastChild();
			if (lastNode instanceof ColumnNode) {
				((ColumnNode) lastNode).addOuterJoin(getToken(), offset, length);
			}
		}
	}

	private INode changeNode(INode from, INode to) {
		INode parent = from.getParent();
		parent.removeChild(from);
		parent.addChild(to);
		to.addChild(from);
		return to;
	}

	INode parseValue(INode parent) {
		INode node = null;
		if (token.getSubType() == TokenUtil.SUBTYPE_VALUE_BIND) {
			node = new BindNode(getToken(), offset, length, scope);
		} else {
			node = new ValueNode(getToken(), offset, length, scope);
		}
		parent.addChild(node);
		return node;
	}

	public int getScope() {
		return scope;
	}

	public String dump(INode node) {
		StringBuilder sb = new StringBuilder();
		dump(sb, node, "");
		return sb.toString();
	}

	protected void dump(StringBuilder sb, INode node, String pre) {

		if (node instanceof InnerAliasNode) {
		}

		if (node.getChildrenSize() != 0) {
			sb.append(pre + "<" + node.toString() + ">\r\n");
			for (int i = 0; i < node.getChildrenSize(); ++i) {
				INode n = (INode) node.getChild(i);
				if (n != null) {
					dump(sb, n, pre + " ");
				}
			}
			sb.append(pre + "</" + node.getNodeClassName() + ">\r\n");

		} else {
			sb.append(pre + "<" + node.toString() + " />\r\n");
		}
	}

	public String dumpXml(INode node) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		dumpXml(sb, node, "");
		return sb.toString();
	}

	protected void dumpXml(StringBuilder sb, INode node, String pre) {

		if (node instanceof InnerAliasNode) {
		}

		if (node.getChildrenSize() != 0) {
			sb.append(pre + "<" + encodeMarkup(node.toString()) + ">\r\n");
			for (int i = 0; i < node.getChildrenSize(); ++i) {
				INode n = (INode) node.getChild(i);
				if (n != null) {
					dumpXml(sb, n, pre + " ");
				}
			}
			sb.append(pre + "</" + node.getNodeClassName() + ">\r\n");

		} else {
			sb.append(pre + "<" + encodeMarkup(node.toString()) + " />\r\n");
		}
	}

	public void setTokenizer(SqlTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public String encodeMarkup(String strSrc) {
		int nLen;
		if (strSrc == null || (nLen = strSrc.length()) <= 0)
			return "";

		StringBuilder sbEnc = new StringBuilder(nLen * 2);

		for (int i = 0; i < nLen; i++) {
			char c;
			switch (c = strSrc.charAt(i)) {
			case '<':
				sbEnc.append("&lt;");
				break;
			case '>':
				sbEnc.append("&gt;");
				break;
			default:
				sbEnc.append(c);
				break;
			}
		}

		return sbEnc.toString();
	}
}
