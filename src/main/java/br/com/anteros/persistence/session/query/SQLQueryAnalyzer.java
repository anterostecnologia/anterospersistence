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
package br.com.anteros.persistence.session.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.format.SqlFormatRule;
import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.Node;
import br.com.anteros.persistence.sql.parser.ParserUtil;
import br.com.anteros.persistence.sql.parser.SqlParser;
import br.com.anteros.persistence.sql.parser.node.BindNode;
import br.com.anteros.persistence.sql.parser.node.ColumnNode;
import br.com.anteros.persistence.sql.parser.node.ExpressionNode;
import br.com.anteros.persistence.sql.parser.node.FromNode;
import br.com.anteros.persistence.sql.parser.node.OperatorNode;
import br.com.anteros.persistence.sql.parser.node.SelectNode;
import br.com.anteros.persistence.sql.parser.node.SelectStatementNode;
import br.com.anteros.persistence.sql.parser.node.TableNode;

public class SQLQueryAnalyzer {

	private String sql;
	private SQLSession session;
	private Class<?> resultClass;
	private List<SQLQueryAnalyserAlias> aliases;
	private Map<String, String> expressions;
	private Map<String, Map<String, Object>> cacheResultAnalyze = new HashMap<String, Map<String, Object>>();

	public SQLQueryAnalyzer(SQLSession session) {
		this.session = session;
	}

	public void analyze(String sql, Class<?> resultClass) throws SQLQueryAnalyzerException {
		this.sql = sql;
		this.resultClass = resultClass;
		Map<String, Object> result = cacheResultAnalyze.get(sql);
		if (result == null) {
			loadAliases();
			result = new HashMap<String, Object>();
			result.put("aliases", aliases);
			result.put("expressions", expressions);
			result.put("sql", this.sql);
			cacheResultAnalyze.put(sql, result);
		} else {
			aliases = (List<SQLQueryAnalyserAlias>) result.get("aliases");
			expressions = (Map<String, String>) result.get("expressions");
			this.sql = (String) result.get("sql");
		}
	}

	protected void loadAliases() throws SQLQueryAnalyzerException {
		SqlParser parser = new SqlParser(sql, new SqlFormatRule());
		INode node = new Node("root");
		parser.parse(node);
		
		System.out.println(parser.dump(node));

		SelectStatementNode selectStatement = null;

		if (!isExistsSelectAsterisk(node)) {
			StringBuffer sb = new StringBuffer();
			selectStatement = getFirstSelectStatement(node);
			Set<SQLQueryAnalyserAlias> aliasesFromNode = getAliasesFromNode(node);
			boolean appendDelimiter = false;
			for (SQLQueryAnalyserAlias alias : aliasesFromNode) {
				if (alias.getEntity() != null) {
					List<DescriptionColumn> columns = alias.getEntity().getPrimaryKeyColumns();
					if (alias.getEntity().hasDiscriminatorColumn())
						columns.add(alias.getEntity().getDiscriminatorColumn());
					for (DescriptionColumn descriptionColumn : columns) {
						if (!existsColumnByAlias(selectStatement, alias.getAlias(), descriptionColumn.getColumnName())) {
							if (appendDelimiter)
								sb.append(", ");
							sb.append(alias.getAlias()).append(".").append(descriptionColumn.getColumnName());
							appendDelimiter = true;
						}
					}
				}
			}

			if (sb.length() > 0) {
				sql = StringUtils.replaceOnce(sql, "SELECT", "SELECT " + sb.toString() + ", ");
				sql = StringUtils.replaceOnce(sql, "select", "select " + sb.toString() + ", ");
				sql = StringUtils.replaceOnce(sql, "Select", "Select " + sb.toString() + ", ");
			}

			parser = new SqlParser(sql, new SqlFormatRule());
			node = new Node("root");
			parser.parse(node);
		}

		aliases = new ArrayList<SQLQueryAnalyserAlias>();

		for (Object child : node.getChildren()) {
			if (child instanceof SelectStatementNode) {
				selectStatement = ((SelectStatementNode) child);
				for (Object selectChild : selectStatement.getChildren()) {
					if (selectChild instanceof FromNode) {
						FromNode from = (FromNode) selectChild;
						for (Object fromChild : from.getChildren()) {
							if (fromChild instanceof TableNode) {
								EntityCache entityCache = session.getEntityCacheManager().getEntityCacheByTableName(
										((TableNode) fromChild).getName());
								SQLQueryAnalyserAlias alias = new SQLQueryAnalyserAlias();
								aliases.add(alias);
								alias.setAlias(((TableNode) fromChild).getAliasName() == null ? ((TableNode) fromChild)
										.getTableName() : ((TableNode) fromChild).getAliasName());
								alias.setEntity(entityCache);
							}
						}
						break;
					}
				}
			}
		}

		if (selectStatement != null) {
			for (SQLQueryAnalyserAlias alias : aliases){
				System.out.println(" ALIAS -> "+alias);
			}
			for (SQLQueryAnalyserAlias alias : aliases)
				findOwnerByAlias(selectStatement, alias);
			buildExpressions(selectStatement);

			Iterator<String> iterator = expressions.keySet().iterator();
			while (iterator.hasNext()) {
				String k = iterator.next();
				String v = expressions.get(k);
				System.out.println(k + " = " + v);
			}
		}

	}

	private SelectStatementNode getFirstSelectStatement(INode node) {
		for (Object child : node.getChildren()) {
			if (child instanceof SelectStatementNode)
				return ((SelectStatementNode) child);
		}
		return null;
	}

	private boolean isExistsSelectAsterisk(INode node) {
		for (Object child : node.getChildren()) {
			if (child instanceof SelectStatementNode) {
				SelectStatementNode selectStatement = ((SelectStatementNode) child);
				INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
				for (INode column : columns) {
					if (column.getParent() instanceof SelectNode) {
						String tableName = ((ColumnNode) column).getTableName();
						String columnName = ((ColumnNode) column).getColumnName();
						if (columnName.equalsIgnoreCase("*") && tableName == null)
							return true;
					}
				}
				break;
			}
		}
		return false;
	}

	public Set<SQLQueryAnalyserAlias> getAliasesFromNode(INode node) {
		Set<SQLQueryAnalyserAlias> result = new LinkedHashSet<SQLQueryAnalyserAlias>();
		for (Object child : node.getChildren()) {
			if (child instanceof SelectStatementNode) {
				SelectStatementNode selectStatement = ((SelectStatementNode) child);
				for (Object selectStatementChild : selectStatement.getChildren()) {
					if (selectStatementChild instanceof SelectNode) {
						SelectNode select = (SelectNode) selectStatementChild;
						for (Object selectChild : select.getChildren()) {
							if (selectChild instanceof ColumnNode) {
								EntityCache entityCache = session.getEntityCacheManager().getEntityCacheByTableName(
										getTableName(selectStatement, ((ColumnNode) selectChild).getTableName()));
								SQLQueryAnalyserAlias alias = new SQLQueryAnalyserAlias();
								alias.setAlias(((ColumnNode) selectChild).getTableName());
								alias.setEntity(entityCache);
								result.add(alias);
							}
						}
						break;
					}
				}
			}
		}
		return result;
	}

	private String getTableName(SelectStatementNode selectStatement, String alias) {
		for (Object selectChild : selectStatement.getChildren()) {
			if (selectChild instanceof FromNode) {
				FromNode from = (FromNode) selectChild;
				for (Object fromChild : from.getChildren()) {
					if (fromChild instanceof TableNode) {

						if (alias != null && alias.equalsIgnoreCase(((TableNode) fromChild).getAliasName()))
							return ((TableNode) fromChild).getTableName();
					}
				}
				break;
			}
		}
		return "";
	}

	protected boolean existsColumnByAlias(SelectStatementNode selectStatement, String alias, String columnName)
			throws SQLQueryAnalyzerException {
		INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				String tn = ((ColumnNode) column).getTableName();
				String cn = ((ColumnNode) column).getColumnName();
				if ((cn.equalsIgnoreCase(columnName) || cn.equalsIgnoreCase("*")) && tn.equalsIgnoreCase(alias))
					return true;
			}
		}
		return false;
	}

	protected void buildExpressions(SelectStatementNode selectStatement) throws SQLQueryAnalyzerException {
		INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
		expressions = new LinkedHashMap<String, String>();
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				String tableName = ((ColumnNode) column).getTableName();
				String columnName = ((ColumnNode) column).getColumnName();

				if ((tableName == null) && ("*".equals(columnName))) {
					for (SQLQueryAnalyserAlias alias : aliases) {
						EntityCache caches[] = { alias.getEntity() };
						if (alias.getEntity() == null)
							throw new SQLQueryAnalyzerException("Não foi encontrada nenhuma entidade para o alias "
									+ alias.getAlias());

						if (alias.getEntity().isAbstractClass())
							caches = session.getEntityCacheManager().getEntitiesBySuperClass(alias.getEntity());

						for (EntityCache cache : caches) {
							for (DescriptionField descriptionField : cache.getDescriptionFields()) {
								if (!descriptionField.isCollection() && !descriptionField.isJoinTable()
										&& !descriptionField.isRelationShip()) {
									String path = alias.getPath();
									if (path != "")
										path += ".";
									expressions.put(path + descriptionField.getName(), descriptionField
											.getSimpleColumn().getColumnName());
								}
							}
						}
					}
				} else {
					SQLQueryAnalyserAlias alias = getAlias(tableName);
					if (alias != null) {
						if (columnName.equals("*")) {
							EntityCache caches[] = { alias.getEntity() };
							if (alias.getEntity().isAbstractClass())
								caches = session.getEntityCacheManager().getEntitiesBySuperClass(alias.getEntity());

							for (EntityCache cache : caches) {
								for (DescriptionField descriptionField : cache.getDescriptionFields()) {
									if (((!descriptionField.isCollection()) && (!descriptionField.isJoinTable()) && (!descriptionField
											.isRelationShip()))) {
										String path = alias.getPath();
										if (path != "")
											path += ".";
										expressions.put(path + descriptionField.getName(), descriptionField
												.getSimpleColumn().getColumnName());
									}
								}
							}
						} else {
							EntityCache caches[] = { alias.getEntity() };
							if (alias.getEntity().isAbstractClass())
								caches = session.getEntityCacheManager().getEntitiesBySuperClass(alias.getEntity());

							for (EntityCache cache : caches) {
								DescriptionColumn descriptionColumn = cache.getDescriptionColumnByName(columnName);
								if (descriptionColumn == null)
									throw new SQLQueryAnalyzerException("A Coluna " + columnName
											+ " não foi encontrada na classe " + cache.getEntityClass().getName());
								if (descriptionColumn.hasDescriptionField()) {
									if (descriptionColumn.getDescriptionField().isCollection()
											|| (descriptionColumn.getDescriptionField().isJoinTable() || (descriptionColumn
													.getDescriptionField().isRelationShip())))
										continue;
								}
								if ((descriptionColumn != null) && (!descriptionColumn.isDiscriminatorColumn())) {
									String path = alias.getPath();
									if (path != "")
										path += ".";
									expressions.put(path + descriptionColumn.getDescriptionField().getName(),
											descriptionColumn.getColumnName());
								} else {
									if (!((cache.getDiscriminatorColumn() != null) && (cache.getDiscriminatorColumn()
											.getColumnName().equalsIgnoreCase(columnName))))
										throw new SQLQueryAnalyzerException("A coluna " + columnName
												+ " não foi encontrada na configuração da classe "
												+ cache.getEntityClass().getName());
								}

							}
						}

					}
				}
			}
		}
	}

	protected SQLQueryAnalyserAlias getAlias(String alias) {
		for (SQLQueryAnalyserAlias a : aliases) {
			if (a.getAlias().equals(alias))
				return a;
		}
		return null;
	}

	protected void findOwnerByAlias(INode node, SQLQueryAnalyserAlias aliasSideA) {
		EntityCache entityCache = aliasSideA.getEntity();
		/*
		 * Se for a mesma classe ou herança de resultClass não precisa path
		 * retorna ""
		 */
		if ((entityCache == null) || (entityCache.getEntityClass() == resultClass)
				|| (ReflectionUtils.isExtendsClass(resultClass, entityCache.getEntityClass())))
			return;

		List<String> columnNames = null;
		for (SQLQueryAnalyserAlias aliasSideB : aliases) {
			if (aliasSideB != aliasSideA) {
				columnNames = getColumnNameEqualsAliases(node, aliasSideA, aliasSideB);
				if (columnNames.size() > 0) {
					if (aliasSideB.getEntity() != null) {

						EntityCache caches[] = { aliasSideB.getEntity() };
						if (aliasSideB.getEntity().isAbstractClass())
							caches = session.getEntityCacheManager().getEntitiesBySuperClass(aliasSideB.getEntity());
						for (EntityCache cache : caches) {
							DescriptionField descriptionField = cache.getDescriptionFieldUsesColumns(aliasSideA
									.getEntity().getEntityClass(), columnNames);
							if (descriptionField != null) {
								aliasSideA.setOwner(new SQLQueryAnalyserOwner(aliasSideB, aliasSideB.getEntity(),
										descriptionField));
								break;
							}
						}
					}
				}
			}
		}
	}

	private List<String> getColumnNameEqualsAliases(INode node, SQLQueryAnalyserAlias sourceAlias,
			SQLQueryAnalyserAlias targetAlias) {
		List<String> result = new ArrayList<String>();
		INode[] expressions = ParserUtil.findChildren(node, ExpressionNode.class.getSimpleName());
		for (INode expression : expressions) {
			OperatorNode operator = (OperatorNode) expression.getChild(0);
			if ("=".equals(operator.getName())) {
				if ((operator.getChild(0) instanceof ColumnNode) && (operator.getChild(1) instanceof ColumnNode)) {
					if (!((operator.getChild(0) instanceof BindNode) || (operator.getChild(1) instanceof BindNode))) {
						ColumnNode columnLeft = (ColumnNode) operator.getChild(0);
						ColumnNode columnRight = (ColumnNode) operator.getChild(1);
						if ((columnRight.getTableName() != null) && (columnLeft.getTableName() != null)) {
							if ((columnLeft.getTableName().equalsIgnoreCase(sourceAlias.getAlias()) && (columnRight
									.getTableName().equalsIgnoreCase(targetAlias.getAlias()))))
								result.add(columnRight.getColumnName());
							else if ((columnRight.getTableName().equalsIgnoreCase(sourceAlias.getAlias()) && (columnLeft
									.getTableName().equalsIgnoreCase(targetAlias.getAlias()))))
								result.add(columnLeft.getColumnName());
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (SQLQueryAnalyserAlias alias : aliases) {
			sb.append("\nalias=").append(alias.getAlias()).append(" [");
			sb.append(" ").append(alias.getEntity().toString());
			sb.append("]");
		}
		return sb.toString();
	}

	public Map<String, String> getExpressions() {
		return expressions;
	}

	public void setExpressions(Map<String, String> expressions) {
		this.expressions = expressions;
	}

	public String getParsedSQL() {
		return sql;
	}
}
