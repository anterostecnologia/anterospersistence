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
import java.util.HashSet;
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
import br.com.anteros.persistence.sql.parser.node.ValueNode;

public class SQLQueryAnalyzer {

	private String sql;
	private SQLSession session;
	private Class<?> resultClass;
	private Set<SQLQueryAnalyserAlias> aliases;
	private Set<SQLQueryAnalyserAlias> aliasesTemporary = new LinkedHashSet<SQLQueryAnalyserAlias>();
	private Map<String, String> expressions;
	private Map<String, Map<String, Object>> cacheResultAnalyze = new HashMap<String, Map<String, Object>>();
	private Map<SQLQueryAnalyserAlias, Map<String, String>> columnAliases = new LinkedHashMap<SQLQueryAnalyserAlias, Map<String, String>>();
	private int numberOfColumn = 0;
	private Set<String> usedAliases = new HashSet<String>();

	public SQLQueryAnalyzer(SQLSession session) {
		this.session = session;
	}

	public void analyze(String sql, Class<?> resultClass) throws SQLQueryAnalyzerException {
		this.sql = sql;
		this.resultClass = resultClass;
		// Map<String, Object> result = cacheResultAnalyze.get(sql);
		// if (result == null) {
		loadAliases();
		// result = new HashMap<String, Object>();
		// result.put("columnAliases", columnAliases);
		// result.put("aliases", aliases);
		// result.put("expressions", expressions);
		// result.put("sql", this.sql);
		// cacheResultAnalyze.put(sql, result);
		// } else {
		// columnAliases = (Map<SQLQueryAnalyserAlias, Map<String, String>>)
		// result.get("columnAliases");
		// aliases = (LinkedHashSet<SQLQueryAnalyserAlias>)
		// result.get("aliases");
		// expressions = (Map<String, String>) result.get("expressions");
		// this.sql = (String) result.get("sql");
		// }
	}

	protected void loadAliases() throws SQLQueryAnalyzerException {
		SqlParser parser = new SqlParser(sql, new SqlFormatRule());
		INode node = new Node("root");
		parser.parse(node);

		buildUsedAliases(node);

		aliases = getFirstAliasesFromNode(node);

		validateResultClassOnSQL();

		numberOfColumn = 0;

		node = parseColumns(node);

		SelectStatementNode firstSelectStatement = getFirstSelectStatement(node);

		if (firstSelectStatement != null) {

			SQLQueryAnalyserAlias aliasResultClass = getAliasResultClass();

			findAndSetOwnerToChildAlias(firstSelectStatement, aliasResultClass);

			validateAliases();

			buildExpressionsAndColumnAliases(firstSelectStatement);

			/*System.out.println(sql);

			System.out.println("--------------------EXPRESSIONS-------------------------------");
			Iterator<String> iterator = expressions.keySet().iterator();
			while (iterator.hasNext()) {
				String k = iterator.next();
				String v = expressions.get(k);
				System.out.println(k + " = " + v);
			}
			System.out.println("--------------------COLUMN ALIASES----------------------------");
			for (SQLQueryAnalyserAlias a : columnAliases.keySet()) {
				System.out.println("ALIAS-> " + a.getAlias() + " path " + a.getAliasPath());
				System.out.println("    ----------------------------------");
				for (String k : columnAliases.get(a).keySet()) {
					System.out.println("    " + k + " = " + columnAliases.get(a).get(k));
				}
			}*/

		}
	}

	protected void findAndSetOwnerToChildAlias(Node node, SQLQueryAnalyserAlias aliasOwner)
			throws SQLQueryAnalyzerException {
		List<String> columnNames = null;
		for (SQLQueryAnalyserAlias aliasChild : aliases) {
			if ((aliasChild == getAliasResultClass()) || (aliasChild.getOwner() != null) || (aliasChild == aliasOwner))
				continue;

			columnNames = getColumnNameEqualsAliases(node, aliasChild, aliasOwner);
			if (columnNames.size() > 0) {
				if (aliasOwner.getEntity() != null) {

					EntityCache caches[] = { aliasOwner.getEntity() };
					if (aliasOwner.getEntity().isAbstractClass())
						caches = session.getEntityCacheManager().getEntitiesBySuperClass(aliasOwner.getEntity());
					for (EntityCache cache : caches) {
						DescriptionField descriptionField = cache.getDescriptionFieldUsesColumns(aliasChild.getEntity()
								.getEntityClass(), columnNames);
						if (descriptionField != null) {
							aliasChild.setOwner(new SQLQueryAnalyserOwner(aliasOwner, aliasOwner.getEntity(),
									descriptionField));
							findAndSetOwnerToChildAlias(node, aliasChild);
							break;
						}
					}
				}
			}

		}

	}

	protected SQLQueryAnalyserAlias getAliasResultClass() {
		for (SQLQueryAnalyserAlias alias : aliases) {
			if ((alias.getEntity() == null) || (alias.getEntity().getEntityClass() == resultClass)
					|| (ReflectionUtils.isExtendsClass(alias.getEntity().getEntityClass(), resultClass)))
				return alias;
		}
		return null;
	}

	protected String makeNextAliasName(String alias, String columnName) {
		String result = null;
		while (true) {
			numberOfColumn++;
			result = alias + "." + columnName + " AS " + alias + "_COL_" + String.valueOf(numberOfColumn);
			if (usedAliases.contains(alias + "_COL_" + String.valueOf(numberOfColumn)))
				continue;
			return result;
		}
	}

	protected void buildUsedAliases(INode node) {
		usedAliases.clear();
		INode[] columns = ParserUtil.findChildren(node, ColumnNode.class.getSimpleName());
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				String alias = ((ColumnNode) column).getAliasName();
				if ((alias != null) && (!alias.equals("")))
					usedAliases.add(alias);
			}
		}
	}

	protected void validateAliases() throws SQLQueryAnalyzerException {
		/*
		 * Valida se existem aliases sem owner (sem junção) diferentes de
		 * resultClass e que estejam no select
		 */
		for (SQLQueryAnalyserAlias a : aliases) {
			if (a.getEntity() != null) {
				Class<?> superClass = a.getEntity().getEntityClass();
				Class<?> childClass = resultClass;
				if ((a.getOwner() == null) && ((!a.getEntity().getEntityClass().equals(resultClass)))
						&& (!ReflectionUtils.isExtendsClass(superClass, childClass))) {
					throw new SQLQueryAnalyzerException(
							"Foi encontrado alias "
									+ a.getAlias()
									+ "->"
									+ a.getEntity().getEntityClass().getName()
									+ " no sql sem junção com nenhum outro alias ou as colunas usadas não está mapeadas na classe. Somente pode ficar sem junção o alias da classe de resultado "
									+ resultClass.getName());
				}
			}
		}
	}

	protected INode parseColumns(INode node) throws SQLQueryAnalyzerException {
		SqlParser parser;
		boolean appendDelimiter = false;
		List<InjectSQLPart> partsToInject = new ArrayList<SQLQueryAnalyzer.InjectSQLPart>();

		SelectStatementNode[] selectStatements = getAllSelectStatement(node);

		for (SelectStatementNode selectStatement : selectStatements) {

			/*
			 * Valida se existem colunas sem alias da tabela ou condition do
			 * where sem o alias
			 */
			validateColumnsAndWhereCondition(selectStatement);

			aliasesTemporary = getAliasesFromNode(selectStatement);
			/*
			 * Substituiu * pelos nomes das colunas
			 */
			for (INode selectNodeChild : ((SelectNode) selectStatement.getChild(0)).getChildren()) {
				if (selectNodeChild instanceof ColumnNode) {
					StringBuffer sbColumns = new StringBuffer();
					Map<String, String> cols = new LinkedHashMap<String, String>();
					if ("*".equals(((ColumnNode) selectNodeChild).getColumnName())) {
						SQLQueryAnalyserAlias[] cacheAliases = null;
						appendDelimiter = false;
						if (((ColumnNode) selectNodeChild).getTableName() == null) {
							cacheAliases = aliasesTemporary.toArray(new SQLQueryAnalyserAlias[] {});
						} else {
							cacheAliases = new SQLQueryAnalyserAlias[] { getAliasByName(((ColumnNode) selectNodeChild)
									.getTableName()) };
						}

						for (SQLQueryAnalyserAlias alias : cacheAliases) {
							if (alias != null) {

								EntityCache caches[] = { alias.getEntity() };
								if (alias.getEntity() == null)
									throw new SQLQueryAnalyzerException(
											"Não foi encontrada nenhuma entidade para o alias " + alias.getAlias()
													+ " fazendo parser do SQL para criação de objetos "
													+ resultClass.getName());

								if (alias.getEntity().isAbstractClass())
									caches = session.getEntityCacheManager().getEntitiesBySuperClassIncluding(
											alias.getEntity());

								for (EntityCache cache : caches) {
									for (DescriptionField descriptionField : cache.getDescriptionFields()) {
										if (!descriptionField.isCollection() && !descriptionField.isJoinTable()) {
											for (DescriptionColumn descriptionColumn : descriptionField
													.getDescriptionColumns()) {
												String aliasColumnName = makeNextAliasName(alias.getAlias(),
														descriptionColumn.getColumnName());
												if (!cols.containsKey(descriptionColumn.getColumnName())) {
													cols.put(descriptionColumn.getColumnName(), aliasColumnName);
												}

											}
										}
									}
									if (cache.hasDiscriminatorColumn()) {
										String aliasColumnName = makeNextAliasName(alias.getAlias(), cache
												.getDiscriminatorColumn().getColumnName());
										if (!cols.containsKey(cache.getDiscriminatorColumn().getColumnName())) {
											cols.put(cache.getDiscriminatorColumn().getColumnName(), aliasColumnName);
										}
									}
								}

								for (String col : cols.values()) {
									if (appendDelimiter)
										sbColumns.append(", ");
									sbColumns.append(col);
									appendDelimiter = true;
								}
								cols.clear();
							}

						}

					} else {
						String tableName = ((ColumnNode) selectNodeChild).getTableName();
						String columnName = ((ColumnNode) selectNodeChild).getColumnName();
						if (!((ColumnNode) selectNodeChild).hasAlias()) {
							String aliasColumnName = makeNextAliasName(tableName, columnName);
							if (!cols.containsKey(columnName)) {
								cols.put(columnName, aliasColumnName);
								sbColumns.append(aliasColumnName);
							}
						}

					}
					if (sbColumns.length() > 0) {
						partsToInject.add(new InjectSQLPart(((ColumnNode) selectNodeChild).getOriginalColumnName(),
								sbColumns.toString()));
					}
				}

			}

			addColumnsIfNotExists(node, partsToInject, selectStatement);
		}

		boolean reload = false;
		for (InjectSQLPart part : partsToInject) {
			sql = part.process(sql);
			reload = true;
		}

		if (reload) {
			parser = new SqlParser(sql, new SqlFormatRule());
			node = new Node("root");
			parser.parse(node);
		}
		return node;
	}

	protected void addColumnsIfNotExists(INode node, List<InjectSQLPart> partsToInject,
			SelectStatementNode selectStatement) throws SQLQueryAnalyzerException {
		boolean appendDelimiter;
		/*
		 * Adiciona colunas DISCRIMINATOR caso não existam
		 */
		if (!isExistsSelectAsterisk(node)) {
			StringBuffer sbDiscriminatorColumn = new StringBuffer();
			appendDelimiter = false;
			for (SQLQueryAnalyserAlias alias : aliasesTemporary) {
				if (alias.getEntity() != null) {
					List<DescriptionColumn> columns = alias.getEntity().getPrimaryKeyColumns();
					if (alias.getEntity().hasDiscriminatorColumn())
						columns.add(alias.getEntity().getDiscriminatorColumn());
					for (DescriptionColumn descriptionColumn : columns) {
						if (!existsColumnByAlias(selectStatement, alias.getAlias(), descriptionColumn.getColumnName())) {
							if (appendDelimiter)
								sbDiscriminatorColumn.append(", ");
							String aliasColumnName = makeNextAliasName(alias.getAlias(),
									descriptionColumn.getColumnName());
							sbDiscriminatorColumn.append(aliasColumnName);
							appendDelimiter = true;
						}
					}
				}
			}

			if (sbDiscriminatorColumn.length() > 0) {
				ColumnNode columnNode = getFirstColumnNode(selectStatement);
				partsToInject
						.add(new InjectSQLPart("", sbDiscriminatorColumn.toString() + ", ", columnNode.getOffset()));
			}
		}
	}

	protected void validateResultClassOnSQL() throws SQLQueryAnalyzerException {
		/*
		 * Verifica se a resultClass faz parte do sql
		 */
		boolean found = false;
		for (SQLQueryAnalyserAlias a : aliases) {
			if (a.getEntity() == null)
				continue;

			if (a.getEntity().getEntityClass().equals(resultClass)) {
				found = true;
				break;
			}
			if (a.getEntity().hasDiscriminatorColumn()) {
				Class<?> superClass = a.getEntity().getEntityClass();
				Class<?> childClass = resultClass;
				if (ReflectionUtils.isExtendsClass(superClass, childClass)) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			throw new SQLQueryAnalyzerException("A classe de resultado para criação do(s) objeto(s) "
					+ resultClass.getName() + " não foi encontrada na instrução SQL. ");
		}
	}

	private ColumnNode getFirstColumnNode(SelectStatementNode selectStatement) {
		INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
		if (columns.length > 0)
			return (ColumnNode) columns[0];
		return null;
	}

	private void validateColumnsAndWhereCondition(SelectStatementNode selectStatement) throws SQLQueryAnalyzerException {

		INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				String tn = ((ColumnNode) column).getTableName();
				String cn = ((ColumnNode) column).getColumnName();
				if (((tn == null) || (tn.equals(""))) && (!"*".equals(cn)))
					throw new SQLQueryAnalyzerException(
							"Foi encontrado a coluna "
									+ cn
									+ " sem um o alias da tabela de origem. É necessário que seja informado o alias da tabela para que seja possível realizar o mapeamento correto do objeto da classe de resultado "
									+ resultClass.getName());
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

	private SelectStatementNode[] getAllSelectStatement(INode node) {
		List<SelectStatementNode> result = new ArrayList<SelectStatementNode>();
		for (Object child : node.getChildren()) {
			if (child instanceof SelectStatementNode)
				result.add((SelectStatementNode) child);
		}
		return result.toArray(new SelectStatementNode[] {});
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

	public Set<SQLQueryAnalyserAlias> getFirstAliasesFromNode(INode node) throws SQLQueryAnalyzerException {
		Set<SQLQueryAnalyserAlias> result = new LinkedHashSet<SQLQueryAnalyserAlias>();
		for (Object child : node.getChildren()) {
			if (child instanceof SelectStatementNode) {
				SelectStatementNode selectStatement = ((SelectStatementNode) child);
				for (Object selectChild : selectStatement.getChildren()) {
					if (selectChild instanceof FromNode) {
						FromNode from = (FromNode) selectChild;
						for (Object fromChild : from.getChildren()) {
							if (fromChild instanceof TableNode) {
								EntityCache entityCache = session.getEntityCacheManager().getEntityCacheByTableName(
										((TableNode) fromChild).getName());
								if (entityCache == null) {
									throw new SQLQueryAnalyzerException("A tabela " + ((TableNode) fromChild).getName()
											+ " não foi encontrada na lista de entidades gerenciadas.");
								}
								SQLQueryAnalyserAlias alias = new SQLQueryAnalyserAlias();
								result.add(alias);
								alias.setAlias(((TableNode) fromChild).getAliasName() == null ? ((TableNode) fromChild)
										.getTableName() : ((TableNode) fromChild).getAliasName());
								alias.setEntity(entityCache);
							}
						}
						return result;
					}
				}
			}
		}
		return result;
	}

	public Set<SQLQueryAnalyserAlias> getAliasesFromNode(SelectStatementNode node) {
		Set<SQLQueryAnalyserAlias> result = new LinkedHashSet<SQLQueryAnalyserAlias>();
		for (Object selectChild : node.getChildren()) {
			if (selectChild instanceof FromNode) {
				FromNode from = (FromNode) selectChild;
				for (Object fromChild : from.getChildren()) {
					if (fromChild instanceof TableNode) {
						EntityCache entityCache = session.getEntityCacheManager().getEntityCacheByTableName(
								((TableNode) fromChild).getName());
						SQLQueryAnalyserAlias alias = new SQLQueryAnalyserAlias();
						result.add(alias);
						alias.setAlias(((TableNode) fromChild).getAliasName() == null ? ((TableNode) fromChild)
								.getTableName() : ((TableNode) fromChild).getAliasName());
						alias.setEntity(entityCache);
					}
				}
				break;
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
				if ((columnName.equalsIgnoreCase(cn) || cn.equalsIgnoreCase("*")) && alias.equalsIgnoreCase(tn))
					return true;
			}
		}
		return false;
	}

	protected void buildExpressionsAndColumnAliases(SelectStatementNode selectStatement)
			throws SQLQueryAnalyzerException {
		columnAliases.clear();
		INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
		expressions = new LinkedHashMap<String, String>();
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				String tableName = ((ColumnNode) column).getTableName();
				String columnName = ((ColumnNode) column).getColumnName();
				String aliasName = ((ColumnNode) column).getColumnName();
				if (((ColumnNode) column).hasAlias()) {
					aliasName = ((ColumnNode) column).getAliasName();
				}

				SQLQueryAnalyserAlias alias = getAlias(tableName);
				if (alias != null) {

					EntityCache caches[] = { alias.getEntity() };
					if (alias.getEntity().isAbstractClass())
						caches = session.getEntityCacheManager().getEntitiesBySuperClassIncluding(alias.getEntity());

					for (EntityCache cache : caches) {
						DescriptionColumn descriptionColumn = cache.getDescriptionColumnByName(columnName);
						if (descriptionColumn == null)
							continue;

						if (!columnAliases.containsKey(alias)) {
							columnAliases.put(alias, new HashMap<String, String>());
						}

						columnAliases.get(alias).put(columnName, alias.getAlias() + "." + aliasName);

						if (descriptionColumn.hasDescriptionField()) {
							if (descriptionColumn.getDescriptionField().isCollection()
									|| (descriptionColumn.getDescriptionField().isJoinTable() || (descriptionColumn
											.getDescriptionField().isRelationShip())))
								continue;
						}
						if ((descriptionColumn != null) && (!descriptionColumn.isDiscriminatorColumn())) {
							String path = alias.getPath();
							if (!path.equals(""))
								path += ".";

							expressions.put(path + descriptionColumn.getDescriptionField().getName(),
									((alias.getAliasPath()).equals("") ? "" : alias.getAliasPath() + ".") + aliasName);
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

	protected SQLQueryAnalyserAlias getAlias(String alias) {
		for (SQLQueryAnalyserAlias a : aliases) {
			if (a.getAlias().equalsIgnoreCase(alias))
				return a;
		}
		return null;
	}

	protected void findOwnerByAlias(INode node, SQLQueryAnalyserAlias aliasSideA) throws SQLQueryAnalyzerException {
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
			SQLQueryAnalyserAlias targetAlias) throws SQLQueryAnalyzerException {
		List<String> result = new ArrayList<String>();
		INode[] expressions = ParserUtil.findChildren(node, ExpressionNode.class.getSimpleName());
		for (INode expression : expressions) {
			OperatorNode operator = (OperatorNode) expression.getChild(0);
			if ("=".equals(operator.getName())) {
				if ((operator.getChild(0) instanceof ColumnNode) && (operator.getChild(1) instanceof ColumnNode)
						&& !(operator.getChild(0) instanceof ValueNode) && !(operator.getChild(1) instanceof ValueNode)) {
					if (!((operator.getChild(0) instanceof BindNode) || (operator.getChild(1) instanceof BindNode))) {
						ColumnNode columnLeft = (ColumnNode) operator.getChild(0);
						ColumnNode columnRight = (ColumnNode) operator.getChild(1);

						String tn = columnLeft.getTableName();
						String cn = columnLeft.getColumnName();
						if ((tn == null) || ("".equals(tn))) {
							throw new SQLQueryAnalyzerException(
									"Foi encontrado a coluna "
											+ cn
											+ " sem o alias da tabela de origem na condição WHERE. É necessário que seja informado o alias da tabela para que seja possível realizar o mapeamento correto do objeto da classe de resultado "
											+ resultClass.getName());
						}

						tn = columnRight.getTableName();
						cn = columnRight.getColumnName();
						if ((tn == null) || ("".equals(tn))) {
							throw new SQLQueryAnalyzerException(
									"Foi encontrado a coluna "
											+ cn
											+ " sem o alias da tabela de origem na condição WHERE. É necessário que seja informado o alias da tabela para que seja possível realizar o mapeamento correto do objeto da classe de resultado "
											+ resultClass.getName());
						}

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
			sb.append("] ");
			sb.append(alias.getAliasPath());
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

	protected SQLQueryAnalyserAlias getAliasByName(String name) {
		for (SQLQueryAnalyserAlias al : aliasesTemporary) {
			if (al.getAlias().equals(name))
				return al;
		}
		return null;
	}

	class InjectSQLPart {
		private String searchTo;
		private String replaceWith;
		private int position;

		public InjectSQLPart(String searchTo, String replaceWith) {
			this.searchTo = searchTo;
			this.replaceWith = replaceWith;
			this.position = -1;
		}

		public InjectSQLPart(String searchTo, String replaceWith, int position) {
			this.searchTo = searchTo;
			this.replaceWith = replaceWith;
			this.position = position;
		}

		public String getSearchTo() {
			return searchTo;
		}

		public void setSearchTo(String searchTo) {
			this.searchTo = searchTo;
		}

		public String getReplaceWith() {
			return replaceWith;
		}

		public void setReplaceWith(String replaceWith) {
			this.replaceWith = replaceWith;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public int getPosition() {
			return position;
		}

		public String process(String sql) {
			if (position != -1) {
				StringBuilder sb = new StringBuilder(sql);
				sb.insert(getPosition(), replaceWith);
				return sb.toString();
			} else {
				return StringUtils.replaceOnce(sql, this.getSearchTo(), this.getReplaceWith());
			}
		}

	}

	public Map<SQLQueryAnalyserAlias, Map<String, String>> getColumnAliases() {
		return columnAliases;
	}
}
