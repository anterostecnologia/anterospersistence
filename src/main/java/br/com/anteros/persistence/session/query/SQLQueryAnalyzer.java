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
package br.com.anteros.persistence.session.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import br.com.anteros.core.utils.CompactHashSet;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.EntityHandler;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.sql.format.SqlFormatRule;
import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.Node;
import br.com.anteros.persistence.sql.parser.ParserUtil;
import br.com.anteros.persistence.sql.parser.ParserVisitorToSql;
import br.com.anteros.persistence.sql.parser.SqlParser;
import br.com.anteros.persistence.sql.parser.node.BindNode;
import br.com.anteros.persistence.sql.parser.node.ColumnNode;
import br.com.anteros.persistence.sql.parser.node.CommaNode;
import br.com.anteros.persistence.sql.parser.node.CommentNode;
import br.com.anteros.persistence.sql.parser.node.FromNode;
import br.com.anteros.persistence.sql.parser.node.GroupbyNode;
import br.com.anteros.persistence.sql.parser.node.KeywordNode;
import br.com.anteros.persistence.sql.parser.node.OperatorNode;
import br.com.anteros.persistence.sql.parser.node.ParenthesesNode;
import br.com.anteros.persistence.sql.parser.node.RootNode;
import br.com.anteros.persistence.sql.parser.node.SelectNode;
import br.com.anteros.persistence.sql.parser.node.SelectStatementNode;
import br.com.anteros.persistence.sql.parser.node.TableNode;
import br.com.anteros.persistence.sql.parser.node.UnionNode;
import br.com.anteros.persistence.sql.parser.node.ValueNode;

/**
 * Classe responsável pela análise do SQL e montagem das expressões necessárias para criação dos objetos da classe de
 * resultado. Esta é umas das principais classes no conceito do framework pois permite que o usuário escreva o SQL sem
 * precisar informar nenhum tipo de mapeamento entre o SQL e o Objeto. A classe analisa e descobre qual parte do SQL
 * corresponde a qual parte do Objeto. A análise será usado pela classe {@link EntityHandler}.
 * 
 * @author edson
 *
 */
public class SQLQueryAnalyzer implements Comparator<String[]> {

	private static final boolean GENERATE_ALIAS_TO_COLUM = true;
	public static final boolean IGNORE_NOT_USED_ALIAS_TABLE = true;
	private String sql;
	private EntityCacheManager entityCacheManager;
	private DatabaseDialect databaseDialect;
	private Class<?> resultClass;
	private Set<SQLQueryAnalyserAlias> aliases;
	private List<ExpressionFieldMapper> expressionsFieldMapper = new ArrayList<ExpressionFieldMapper>();
	private Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases = new LinkedHashMap<SQLQueryAnalyserAlias, Map<String, String[]>>();
	private int numberOfColumn = 0;
	private Set<String> usedAliases;
	private boolean allowApplyLockStrategy;
	private boolean ignoreNotUsedAliasTable;

	public SQLQueryAnalyzer(EntityCacheManager entityCacheManager, DatabaseDialect databaseDialect, boolean ignoreNotUsedAliasTable) {
		this.entityCacheManager = entityCacheManager;
		this.databaseDialect = databaseDialect;
		this.ignoreNotUsedAliasTable = ignoreNotUsedAliasTable;
	}

	/**
	 * Analisa um SQL validando se sua estrutura permite montar objetos para classe de resultado. Monta uma lista de
	 * expressões que serão utilizadas na montagem dos objetos pelo EntityHandler.
	 * 
	 * @param sql
	 *            SQL a ser analisado
	 * @param resultClass
	 *            Classe de resultado
	 * @return Resultado da análise
	 * @throws SQLQueryAnalyzerException
	 */
	public SQLQueryAnalyzerResult analyze(String sql, Class<?> resultClass) throws SQLQueryAnalyzerException {

		this.sql = sql;
		this.resultClass = resultClass;
		/*
		 * Faz o parse do SQL e cria as expressões necessárias para montagem dos objetos da classe de resultado
		 */
		parseAndMakeExpressions();
		/*
		 * Cria retorno da análise do SQL.
		 */
		SQLQueryAnalyzerResult result = new SQLQueryAnalyzerResult(getParsedSQL(), aliases, expressionsFieldMapper, columnAliases, allowApplyLockStrategy);

		return result;
	}

	/**
	 * Faz o parse do SQL e cria as expressões necessárias para montagem dos objetos da classe de resultado.
	 * 
	 * @throws SQLQueryAnalyzerException
	 */
	protected void parseAndMakeExpressions() throws SQLQueryAnalyzerException {
		/*
		 * Faz o parse do SQL para transformar em uma estrutura de objetos o que torna possível a análise do mapeamento.
		 */
		SqlParser parser = new SqlParser(sql, new SqlFormatRule());
		INode node = new RootNode();
		parser.parse(node);

		//System.out.println(parser.dump(node));

		allowApplyLockStrategy = false;
		INode[] children = ParserUtil.findChildren(getFirstSelectStatement(node), ColumnNode.class.getSimpleName());
		for (INode columnNode : children) {
			if (columnNode.toString().toLowerCase().contains("distinct")) {
				allowApplyLockStrategy = true;
				break;
			}
		}

		if (!allowApplyLockStrategy)
			allowApplyLockStrategy = (ParserUtil.findChildren(node, UnionNode.class.getSimpleName()).length == 0);

		/*
		 * Busca lista de aliases usados nas colunas do Select
		 */
		usedAliases = getUsedColumnAliases(node);

		/*
		 * Busca lista de aliases usados para as tabelas do primeiro Select. Caso seja um SQL com Union valem os aliases
		 * das colunas do primeiro Select.
		 */
		aliases = getTableAliasesFromFirstSelectNode(node);

		/*
		 * Valida se a classe de resultado faz parte do SQL.
		 */
		validateResultClassOnSQL();

		/*
		 * Inicializa contador para criação do alias das colunas
		 */
		numberOfColumn = 0;

		/*
		 * Realiza o parse das colunas do SQL. Verifica a existem de * e substitui pelos nomes das colunas da tabela.
		 * Verifica a ausência e adiciona as colunas que fazem parte da chave da tabela e também as colunas que
		 * representam o discriminator column das entidades no caso de classes abstratas.
		 */
		node = parseColumns(node);

		/*
		 * Busca o primeiro SelectStatement do SQL. Será com base nele que será montado a árvore de expressões para
		 * criação dos objetos da classe de resultado.
		 * 
		 * Monta a estrutura de expressões necessárias para criação do objeto da classe de resultado
		 */
		buildExpressionsAndColumnAliases(getFirstSelectStatement(node));

		// System.out.println(sql);
		// System.out.println("--------------------EXPRESSIONS-------------------------------");
		// for (ExpressionFieldMapper expField : expressionsFieldMapper)
		// System.out.println(expField);
		System.out.println("--------------------COLUMN ALIASES----------------------------");
		for (SQLQueryAnalyserAlias a : columnAliases.keySet()) {
			System.out.println("ALIAS-> " + a.getAlias() + " path " + a.getAliasPath());
			System.out.println("    ----------------------------------");
			for (String k : columnAliases.get(a).keySet()) {
				System.out.println("    " + k + " = " + Arrays.toString(columnAliases.get(a).get(k)));
			}
		}

	}

	/**
	 * Associa o pai a cada alias filho usado no SQL montando assim um caminho para montar o objeto da classe de
	 * resultado. Esta é parte mais importante do processo de análise pois detecta os relacionamentos entre os aliases e
	 * eles devem estar de acordo com o mapeamento das entidades.
	 * 
	 * @param selectStatement
	 *            Select
	 * @param aliasOwner
	 *            Alias pai
	 * @throws SQLQueryAnalyzerException
	 */
	protected void findAndSetOwnerToChildAlias(SelectStatementNode selectStatement, SQLQueryAnalyserAlias aliasOwner) throws SQLQueryAnalyzerException {
		List<String> columnNames = null;
		/*
		 * Analisa os aliases para encontrar os filhos
		 */
		for (SQLQueryAnalyserAlias aliasChild : aliases) {
			if ((aliasChild == getAliasResultClass()) || (aliasChild.getOwner() != null) || (aliasChild == aliasOwner))
				continue;

			/*
			 * Para que um alias seja filho ele precisa ter uma junção pelo nomes das colunas com seu pai(usando JOIN ou
			 * =), ou seja, precisa haver um relacionamento entre eles. Sem isto não é possível montar o objeto.
			 */
			columnNames = getColumnNameEqualsAliases(selectStatement, aliasChild, aliasOwner);
			if (columnNames.size() > 0) {
				if (aliasOwner.getEntity() != null) {
					EntityCache caches[] = { aliasOwner.getEntity() };
					/*
					 * Se for uma classe abstrata pega as classes concretas
					 */
					if (aliasOwner.getEntity().isAbstractClass())
						caches = entityCacheManager.getEntitiesBySuperClass(aliasOwner.getEntity());
					boolean found = false;
					for (EntityCache cache : caches) {
						/*
						 * Busca o campo da entidade que usa as colunas do relacionamento
						 */
						DescriptionField descriptionField = cache.getDescriptionFieldUsesColumns(aliasChild.getEntity().getEntityClass(), columnNames);
						/*
						 * Se encontrar o campo seta do alias pai no filho pois eles possuem um relacionamento
						 */
						if (descriptionField != null) {
							aliasChild.setOwner(new SQLQueryAnalyserOwner(aliasOwner, aliasOwner.getEntity(), descriptionField));
							found = true;
							/*
							 * Usando recursividade podemos encontrar os filhos do filho
							 */
							findAndSetOwnerToChildAlias(selectStatement, aliasChild);
							break;
						}
					}

					/*
					 * Caso o filho não se relacione com o pai não é possível usar o alias para montar o objeto. Alias
					 * sem relacionamento com a classe de resultado somente poderão ser usados para montagem de filtro
					 * (clásula where).
					 */
					if ((!found) && (aliasChild.isUsedOnSelect()) && (!ignoreNotUsedAliasTable)) {
						throw new SQLQueryAnalyzerException("Foi encontrado alias " + aliasChild.getAlias() + "->"
								+ aliasChild.getEntity().getEntityClass().getName()
								+ " no sql sem junção com nenhum outro alias ou as colunas usadas não estão mapeadas na "
								+ "classe ou as mesmas não possuem relacionamento. "
								+ "Não será possível montar a árvore do objeto sem que eles se relacionem diretamente. "
								+ "Somente pode ficar sem junção o alias da classe de resultado " + resultClass.getName()
								+ " e os aliases não usados como resultado na colunas do SELECT.");
					}
				}
			}
		}
	}

	/**
	 * Retorna o alias da classe de resultado
	 * 
	 * @return Alias
	 */
	private SQLQueryAnalyserAlias getAliasResultClass() {
		for (SQLQueryAnalyserAlias alias : aliases) {
			if ((alias.getEntity() == null) || (alias.getEntity().getEntityClass() == resultClass)
					|| (ReflectionUtils.isExtendsClass(alias.getEntity().getEntityClass(), resultClass)))
				return alias;
		}
		return null;
	}

	/**
	 * Gera um nome de coluna válido para o Alias
	 * 
	 * @param alias
	 *            Alias da tabela
	 * @param columnName
	 *            Nome da coluna
	 * @return Novo nome
	 */
	private String makeNextAliasName(String alias, String columnName) {
		String result = null;
		while (true) {
			numberOfColumn++;
			result = adpatAliasColumnName(alias) + "_COL_" + String.valueOf(numberOfColumn);
			if (usedAliases.contains(alias + "_COL_" + String.valueOf(numberOfColumn)))
				continue;
			return result;
		}
	}

	/**
	 * Lê as colunas do Select e monta uma lista de aliases usados.
	 * 
	 * @param node
	 * @return Set de aliases usados
	 */
	protected Set<String> getUsedColumnAliases(INode node) {
		Set<String> result = new CompactHashSet<String>();
		INode[] columns = ParserUtil.findChildren(node, ColumnNode.class.getSimpleName());
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				String alias = ((ColumnNode) column).getAliasName();
				if ((alias != null) && (!alias.equals("")))
					result.add(alias);
			}
		}
		return result;
	}

	/**
	 * Realiza o parse das colunas do SQL. Verifica a existência de coluna com "*" e substitui pelos nomes das colunas
	 * da tabela. Verifica a ausência e adiciona as colunas que fazem parte da chave da tabela e também as colunas que
	 * representam o discriminator column das entidades no caso de classes abstratas.
	 * 
	 * @param mainNode
	 * @return
	 * @throws SQLQueryAnalyzerException
	 */
	protected INode parseColumns(INode mainNode) throws SQLQueryAnalyzerException {
		SqlParser parser;
		/*
		 * Busca todos os nós do tipo Select
		 */
		SelectStatementNode[] selectStatements = getAllSelectStatement(mainNode);			
		
		/*
		 * Analisa os Select's encontrados pelo parser.
		 */
		int size = selectStatements.length;

		for (int i = 0; i < size; i++) {
			SelectStatementNode selectStatement = selectStatements[i];
			/*
			 * Cria um Mapa de strings que irão guardar as colunas que precisam ter seus nomes(aliases) alterados no sql
			 */
			Map<String, String> replaceStrings = new LinkedHashMap<String, String>();

			/*
			 * Valida se existem colunas sem o alias da tabela ou condition do where sem o alias
			 */
			validateColumnsAndWhereCondition(selectStatement);

			
			SelectNode select = (SelectNode) ParserUtil.findFirstChild(selectStatement, "SelectNode");
			/*
			 * Obtém as posições do Select/Group by dentro do SQL.
			 */
			int offsetProcessNode = select.getOffset();
			int offSetProcessNodeFinal = select.getMaxOffset();
			/*
			 * Processa Select
			 */
			processSelectOrGroupByColumns(mainNode, replaceStrings, selectStatement, select, GENERATE_ALIAS_TO_COLUM);

			/*
			 * Processa Group By se houver
			 */
			GroupbyNode groupBy = (GroupbyNode) ParserUtil.findFirstChild(selectStatement, "GroupbyNode");
			if (groupBy != null)
				processSelectOrGroupByColumns(mainNode, replaceStrings, selectStatement, groupBy, !GENERATE_ALIAS_TO_COLUM);

			/*
			 * Subtitui as strings no sql
			 */

			for (String r : replaceStrings.keySet()) {
				StringBuilder sb = new StringBuilder(sql);
				
				sql = sb.replace(offsetProcessNode, offSetProcessNodeFinal, replaceStrings.get(r)).toString();
				
				/*
				 * Recarrega a árvore do Sql realizando um novo parser.
				 */
				parser = new SqlParser(sql, new SqlFormatRule());
				mainNode = new RootNode();
				parser.parse(mainNode);
				selectStatements = getAllSelectStatement(mainNode);
			}
		}

		/*
		 * Retorna a nova estrutura(árvore do sql) já com as novas colunas.
		 */
		return mainNode;
	}

	protected void processSelectOrGroupByColumns(INode mainNode, Map<String, String> replaceStrings, SelectStatementNode selectStatement, Node processNode,
			boolean generateAliasToColum) throws SQLQueryAnalyzerException {

		/*
		 * Cria uma lista para guardar as novas colunas a serem adicionadas no SQL
		 */
		List<INode> newColumns = new ArrayList<INode>();
		Set<String> distinctNewColumns = new CompactHashSet<String>();

		/*
		 * Lista temporária de aliases do Select/Group by
		 */
		Set<SQLQueryAnalyserAlias> aliasesTemporary;
		/*
		 * Obtém as posições do Select/Group by dentro do SQL.
		 */
		int offsetProcessNode = processNode.getOffset();
		int offSetProcessNodeFinal = processNode.getMaxOffset();

		/*
		 * Guarda o Select/Group By antigo
		 */
		String oldSelectOrGroupBy = sql.substring(offsetProcessNode, offSetProcessNodeFinal);

		/*
		 * Obtém a lista de aliases das tabelas usadas no Select/Group by
		 */
		aliasesTemporary = getTableAliasesFromSelectNode(selectStatement);

		/*
		 * Gera os nomes das colunas do select/Group by
		 */
		makeColumnNameAliases(aliasesTemporary, newColumns, distinctNewColumns, processNode, generateAliasToColum);

		/*
		 * Busca o alias da classe de resultado
		 */
		SQLQueryAnalyserAlias aliasResultClass = getAliasResultClass();

		/*
		 * Associa o pai a cada alias filho usado no Select formando assim um caminho para montar o objeto da classe de
		 * resultado.
		 */
		findAndSetOwnerToChildAlias(getFirstSelectStatement(mainNode), aliasResultClass);

		/*
		 * Adiciona as colunas da chave primária do alias(tabela) e discriminator column caso não existam no
		 * Select/GroupBy.
		 */
		addPkAndDiscriminatorColumnsIfNotExists(aliasesTemporary, mainNode, newColumns, processNode, generateAliasToColum);

		/*
		 * Remove as colunas atuais do Select/GroupBy
		 */
		List<INode> listToRemove = new ArrayList<INode>();
		for (INode child : processNode.getChildren()) {
			if (child instanceof CommentNode)
				continue;
			if (child instanceof KeywordNode) {
				if (((KeywordNode) child).getName().equalsIgnoreCase("DISTINCT")) {
					continue;
				}
			}

			listToRemove.add(child);
		}
		for (INode item : listToRemove) {
			processNode.removeChild(item);
		}

		/*
		 * Adiciona as novas colunas no Select/GroupBy
		 */
		INode oldColumn = null;
		for (INode newColumn : newColumns) {
			if (oldColumn != null)
				processNode.addChild(new CommaNode(0, 0, 0));
			processNode.addChild(newColumn);
			oldColumn = newColumn;
		}

		/*
		 * Converte o Select/GroupBy em uma string SQL
		 */
		ParserVisitorToSql v = new ParserVisitorToSql();
		v.visit(processNode, sql);
		String newSelectGroupBy = v.toString();
		/*
		 * Armazena a string do Select/GroupBy antigo a ser substituída pelo novo Select/GroupBy
		 */
		replaceStrings.put(oldSelectOrGroupBy, newSelectGroupBy);
	}

	void makeColumnNameAliases(Set<SQLQueryAnalyserAlias> aliasesTemporary, List<INode> newColumns, Set<String> distinctNewColumns, INode node,
			boolean generateAliasToColum) throws SQLQueryAnalyzerException {
		/*
		 * Substituiu * pelos nomes das colunas
		 */
		for (INode selectNodeChild : node.getChildren()) {
			/*
			 * Se o nó filho for uma coluna
			 */
			if (selectNodeChild instanceof ColumnNode) {
				/*
				 * Se o nome da coluna for * (asterisco)
				 */
				if ("*".equals(((ColumnNode) selectNodeChild).getColumnName())) {
					replaceAsteriskByAliasColumnName(aliasesTemporary, newColumns, distinctNewColumns, selectNodeChild, generateAliasToColum);
				} else {
					replaceColumnNameByAlias(newColumns, distinctNewColumns, selectNodeChild, generateAliasToColum);
				}
			} else {

				if ((selectNodeChild instanceof CommaNode) || (selectNodeChild instanceof CommentNode) || (selectNodeChild instanceof CommentNode))
					continue;

				if (selectNodeChild instanceof KeywordNode) {
					if (((KeywordNode) selectNodeChild).getName().equalsIgnoreCase("DISTINCT")) {
						continue;
					}
				}

				/*
				 * Adiciona as demais projeções do select
				 */
				newColumns.add((Node) selectNodeChild);
			}
		}
	}

	protected void replaceColumnNameByAlias(List<INode> newColumns, Set<String> distinctNewColumns, INode selectNodeChild, boolean generateAliasToColum) {
		/*
		 * Caso não seja uma coluna com "*"
		 */
		String tableName = ((ColumnNode) selectNodeChild).getTableName();
		String columnName = ((ColumnNode) selectNodeChild).getColumnName();
		/*
		 * Verifica se a coluna possuí um alias
		 */
		if (!((ColumnNode) selectNodeChild).hasAlias()) {
			if (!distinctNewColumns.contains(tableName + "." + columnName)) {
				/*
				 * Se não tiver gera um alias para a coluna, pois todas as colunas vão ter um alias para facilitar o
				 * mapeamento para os objetos.
				 */
				String aliasColumnName = makeNextAliasName(tableName, columnName);
				/*
				 * Cria a nova coluna e adiciona na lista para ser adicionada no Select posteriormente
				 */
				ColumnNode newColumn = new ColumnNode(columnName, 0, 0, 0);
				if (generateAliasToColum)
					newColumn.setAliasName(aliasColumnName, 0, 0);
				newColumn.setTableName(tableName);
				newColumns.add(newColumn);
				distinctNewColumns.add(tableName + "." + columnName);
			}
		} else {
			/*
			 * Caso já possua um alias apenas adiciona na lista de colunas do Select.
			 */
			newColumns.add(((ColumnNode) selectNodeChild));
		}
	}

	protected void replaceAsteriskByAliasColumnName(Set<SQLQueryAnalyserAlias> aliasesTemporary, List<INode> newColumns, Set<String> distinctNewColumns,
			INode selectNodeChild, boolean generateAliasToColum) throws SQLQueryAnalyzerException {
		SQLQueryAnalyserAlias[] cacheAliases = null;
		/*
		 * Se o "*" não possuí um alias de tabela é válido para todas as tabelas do Select e será substituído pelo nomes
		 * das colunas de todos os aliases do Select.
		 */
		if (((ColumnNode) selectNodeChild).getTableName() == null) {
			cacheAliases = aliasesTemporary.toArray(new SQLQueryAnalyserAlias[] {});
		} else {
			/*
			 * Caso o "*" possua uma alias de tabela será trocado somente pelas colunas do alias
			 */
			cacheAliases = new SQLQueryAnalyserAlias[] { getAliasByName(aliasesTemporary, ((ColumnNode) selectNodeChild).getTableName()) };
		}

		/*
		 * Substitui o "*" pelo nomes das colunas dos aliases
		 */
		for (SQLQueryAnalyserAlias alias : cacheAliases) {
			if (alias != null) {
				/*
				 * Obtém a entidade correspondente ao alias
				 */
				EntityCache caches[] = { alias.getEntity() };
				/*
				 * Se não encontrou a entidade é porque a tabela do alias não está mapeada e não faz parte do modelo de
				 * dados.
				 */
				if (alias.getEntity() == null)
					throw new SQLQueryAnalyzerException("Não foi encontrada nenhuma entidade para o alias " + alias.getAlias()
							+ " fazendo parser do SQL para criação de objetos " + resultClass.getName());

				/*
				 * Se o alias encontrado for de um entidade cuja classe é abstrata, pega todas as classes concretas + a
				 * classe abstrata para adicionar as colunas no Select.
				 */
				if (alias.getEntity().isAbstractClass())
					caches = entityCacheManager.getEntitiesBySuperClassIncluding(alias.getEntity());

				/*
				 * Adiciona as coluna das entidades no Select
				 */
				for (EntityCache cache : caches) {
					for (DescriptionField descriptionField : cache.getDescriptionFields()) {
						/*
						 * Adiciona as colunas do campo caso o mesmo não seja uma coleção, uma junção ou um Lob
						 * configurado com Lazy.
						 */
						if (!descriptionField.isAnyCollectionOrMap() && !descriptionField.isJoinTable()
								&& !(descriptionField.isLob() && descriptionField.getFetchType() == FetchType.LAZY)) {
							for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
								String originalColumnName = alias.getAlias() + "." + descriptionColumn.getColumnName();
								if (!distinctNewColumns.contains(originalColumnName)) {
									/*
									 * Gera um alias para a coluna
									 */
									String aliasColumnName = makeNextAliasName(alias.getAlias(), descriptionColumn.getColumnName());
									/*
									 * Cria a nova coluna e adiciona na lista para ser adicionada no Select
									 * posteriormente
									 */
									ColumnNode newColumn = new ColumnNode(descriptionColumn.getColumnName(), 0, 0, 0);
									if (generateAliasToColum)
										newColumn.setAliasName(aliasColumnName, 0, 0);
									newColumn.setTableName(alias.getAlias());
									newColumns.add(newColumn);
									distinctNewColumns.add(originalColumnName);
								}
							}
						}
					}
					/*
					 * Se a entidade possuir um discriminator column adiciona na lista também
					 */
					if (cache.hasDiscriminatorColumn()) {
						String originalColumnName = alias.getAlias() + "." + cache.getDiscriminatorColumn().getColumnName();
						if (!distinctNewColumns.contains(originalColumnName)) {
							/*
							 * Gera um alias para a coluna
							 */
							String aliasColumnName = makeNextAliasName(alias.getAlias(), cache.getDiscriminatorColumn().getColumnName());
							/*
							 * Cria a nova coluna e adiciona na lista para ser adicionada no Select posteriormente
							 */
							ColumnNode newColumn = new ColumnNode(cache.getDiscriminatorColumn().getColumnName(), 0, 0, 0);
							if (generateAliasToColum)
								newColumn.setAliasName(aliasColumnName, 0, 0);
							newColumn.setTableName(alias.getAlias());
							newColumns.add(newColumn);
							distinctNewColumns.add(originalColumnName);
						}
					}
				}
			}
		}
	}

	/**
	 * Adiciona as colunas da chave primária do alias(tabela) e discriminator column caso não existem no Select.
	 * 
	 * @param aliases
	 *            Aliases do Select.
	 * @param node
	 *            Node que representa o SQL
	 * @param newColumns
	 *            Lista onde deverão ser adicionadas as novas colunas
	 * @param generateAliasToColum
	 * @param selectStatement
	 *            Nó do SelectStatement
	 * @throws SQLQueryAnalyzerException
	 */
	private void addPkAndDiscriminatorColumnsIfNotExists(Set<SQLQueryAnalyserAlias> aliases, INode node, List<INode> newColumns, INode select,
			boolean generateAliasToColum) throws SQLQueryAnalyzerException {
		/*
		 * Adiciona colunas da chave da tabelas(alias) e colunas DISCRIMINATOR caso não existam
		 */
		if (!isExistsSelectAsterisk(node)) {
			for (SQLQueryAnalyserAlias alias : aliases) {
				/*
				 * Se o alias possuí um entidade e foi usado no Select
				 */
				if ((alias.getEntity() != null) && (alias.isUsedOnSelect() || childrenUsedOnSelect(aliases, alias))) {
					List<DescriptionColumn> columns = new ArrayList<DescriptionColumn>(alias.getEntity().getPrimaryKeyColumns());
					if (alias.getEntity().hasDiscriminatorColumn())
						columns.add(alias.getEntity().getDiscriminatorColumn());
					/*
					 * Verifica se as colunas da chave primária e a discriminator column existem na lista de colunas do
					 * Select, caso não existam adiciona. Isto é necessário pois não é possível criar os objetos sem o
					 * ID e nem criar as classes concretas sem saber o valor do discriminator column.
					 */
					for (DescriptionColumn descriptionColumn : columns) {
						if (!existsColumnByAlias(select, alias.getAlias(), descriptionColumn.getColumnName())) {
							/*
							 * Gera um alias para a coluna
							 */
							String aliasColumnName = makeNextAliasName(alias.getAlias(), descriptionColumn.getColumnName());
							/*
							 * Cria a nova coluna e adiciona na lista para ser adicionada no Select posteriormente
							 */
							ColumnNode newColumn = new ColumnNode(descriptionColumn.getColumnName(), 0, 0, 0);
							if (generateAliasToColum)
								newColumn.setAliasName(aliasColumnName, 0, 0);
							newColumn.setTableName(alias.getAlias());
							newColumns.add(newColumn);
						}
					}
				}
			}
		}
	}

	private boolean childrenUsedOnSelect(Set<SQLQueryAnalyserAlias> aliases, SQLQueryAnalyserAlias alias) {
		for (SQLQueryAnalyserAlias child : aliases) {
			if (child.getOwner() != null && child.getOwner().getOwner().equals(alias)) {
				if (child.isUsedOnSelect())
					return true;
				return childrenUsedOnSelect(aliases, child);
			}
		}
		return false;
	}

	/**
	 * Valida se classe de resultado faz parte dos aliases do SQL
	 * 
	 * @throws SQLQueryAnalyzerException
	 */
	private void validateResultClassOnSQL() throws SQLQueryAnalyzerException {
		/*
		 * Verifica se a resultClass faz parte do sql
		 */
		for (SQLQueryAnalyserAlias alias : aliases) {
			/*
			 * Se a classe não possuí uma entidade ignora o alias
			 */
			if (alias.getEntity() == null)
				continue;

			/*
			 * Se o alias possuí uma entidade cuja classe é igual a classe de resultado então é valida.
			 */
			if (alias.getEntity().getEntityClass().equals(resultClass)) {
				return;
			}
			/*
			 * Se a entidade do alias possuí um dicriminator colum analisa se classe de resultado extende a classe da
			 * entidade
			 */
			if (alias.getEntity().hasDiscriminatorColumn()) {
				Class<?> superClass = alias.getEntity().getEntityClass();
				Class<?> childClass = resultClass;
				if (ReflectionUtils.isExtendsClass(superClass, childClass)) {
					return;
				}
			}
		}
		/*
		 * Se não encontrou a classe de resultado nos aliases do SQL gera uma exception
		 */
		throw new SQLQueryAnalyzerException("A classe de resultado para criação do(s) objeto(s) " + resultClass.getName()
				+ " não foi encontrada na instrução SQL. ");
	}

	/**
	 * Valida se existem colunas sem o alias da tabela de origem.
	 * 
	 * @param selectStatement
	 *            Select
	 * @throws SQLQueryAnalyzerException
	 */
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

	/**
	 * Retorna o primeiro nó do tipo SelectStatement da árvore do SQL
	 * 
	 * @param node
	 *            Árvore do SQL
	 * @return Nó que representa o SelectStatement
	 */
	private SelectStatementNode getFirstSelectStatement(INode node) {
		return (SelectStatementNode) ParserUtil.findFirstChild(node, "SelectStatementNode");
	}

	/**
	 * Retorna todos os nós do tipo SelectStatement da árvore do SQL
	 * 
	 * @param node
	 *            Árvore do SQL
	 * @return Array de nós que representam os SelectStatement's
	 */
	private SelectStatementNode[] getAllSelectStatement(INode node) {
		List<SelectStatementNode> result = new ArrayList<SelectStatementNode>();
		for (Object child : node.getChildren()) {
			if (child instanceof ParenthesesNode) {
				if (((ParenthesesNode) child).getChildrenSize() > 0) {
					if (((ParenthesesNode) child).getChildren().get(0) instanceof SelectStatementNode) {
						result.add((SelectStatementNode) ((ParenthesesNode) child).getChildren().get(0));
					}
				}
			} else if (child instanceof SelectStatementNode)
				result.add((SelectStatementNode) child);
		}
		return result.toArray(new SelectStatementNode[] {});
	}

	/**
	 * Retorna se existem colunas com "*" na árvore ou nó do SQL
	 * 
	 * @param node
	 *            Árvore ou Nó do SQL
	 * @return Verdadeiro caso exista o "*"
	 */
	private boolean isExistsSelectAsterisk(INode node) {
		for (Object child : node.getChildren()) {
			/*
			 * Verifica apenas nos nós do tipo Select
			 */
			if (child instanceof SelectStatementNode) {
				SelectStatementNode selectStatement = ((SelectStatementNode) child);
				INode[] columns = ParserUtil.findChildren(selectStatement, ColumnNode.class.getSimpleName());
				for (INode column : columns) {
					if (column.getParent() instanceof SelectNode) {
						String tableName = ((ColumnNode) column).getTableName();
						String columnName = ((ColumnNode) column).getColumnName();
						/*
						 * Caso o nome da coluna seja "*"
						 */
						if (columnName.equalsIgnoreCase("*") && tableName == null)
							return true;
					}
				}
				break;
			}
		}
		return false;
	}

	/**
	 * Retorna os alias das tabelas usadas no primeiro Select do SQL
	 * 
	 * @param node
	 *            Árvore ou nó do SQL
	 * @return Lista de aliases usados
	 * @throws SQLQueryAnalyzerException
	 */
	private Set<SQLQueryAnalyserAlias> getTableAliasesFromFirstSelectNode(INode node) throws SQLQueryAnalyzerException {
		Set<SQLQueryAnalyserAlias> result = new LinkedHashSet<SQLQueryAnalyserAlias>();

		SelectStatementNode selectStatement = getFirstSelectStatement(node);
		if (selectStatement != null) {
			/*
			 * Retorna os aliases do Select
			 */
			return getTableAliasesFromSelectNode(selectStatement);
		}
		return result;
	}

	/**
	 * Verifica se o alias foi usado no Select
	 * 
	 * @param selectStatement
	 *            Select
	 * @param alias
	 *            Alias
	 * @return Verdadeiro se o alias foi usado
	 */
	private boolean isUsedOnSelect(SelectStatementNode selectStatement, String alias) {
		for (Object selectChild : selectStatement.getChildren()) {
			/*
			 * Analisa apenas nós do tipo Select
			 */
			if (selectChild instanceof SelectNode) {
				SelectNode select = (SelectNode) selectChild;
				for (Object column : select.getChildren()) {
					/*
					 * Se for uma coluna
					 */
					if (column instanceof ColumnNode) {
						String tableName = ((ColumnNode) column).getTableName();
						String columnName = ((ColumnNode) column).getColumnName();
						/*
						 * Se foi usado "*" no alias da tabela ou o alias é igual ao desejado então o alias foi usado no
						 * select.
						 */
						if (((columnName.equalsIgnoreCase("*") && tableName == null)) || (alias.equalsIgnoreCase(tableName)))
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Retorna uma lista de aliases das tabelas usadas no Select e que tenham uma entidade mapeada
	 * 
	 * @param selectStatement
	 *            Select
	 * @return Lista de aliases das tabelas
	 */
	public Set<SQLQueryAnalyserAlias> getTableAliasesFromSelectNode(SelectStatementNode selectStatement) {
		Set<SQLQueryAnalyserAlias> result = new LinkedHashSet<SQLQueryAnalyserAlias>();
		for (Object selectChild : selectStatement.getChildren()) {
			/*
			 * Analisa os nós do tipo Select
			 */
			if (selectChild instanceof FromNode) {
				FromNode from = (FromNode) selectChild;
				for (Object fromChild : from.getChildren()) {
					/*
					 * Se o nó filho do Select for uma tabela
					 */
					if (fromChild instanceof TableNode) {
						/*
						 * Verifica se a tabela possui uma entidade mapeada
						 */
						EntityCache entityCache = entityCacheManager.getEntityCacheByTableName(((TableNode) fromChild).getName());
						if (entityCache != null) {
							/*
							 * Cria um SQLQueryAnalyserAlias e retorna
							 */
							SQLQueryAnalyserAlias alias = new SQLQueryAnalyserAlias();
							alias.setAlias(((TableNode) fromChild).getAliasName() == null ? ((TableNode) fromChild).getTableName() : ((TableNode) fromChild)
									.getAliasName());
							alias.setEntity(entityCache);
							alias.setUsedOnSelect(isUsedOnSelect(selectStatement, alias.getAlias()));
							result.add(alias);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Retorna se existe uma coluna no Select usando o nome e o alias da tabela
	 * 
	 * @param selectStatement
	 *            Select
	 * @param alias
	 *            Alias da tabela
	 * @param columnName
	 *            Nome da coluna
	 * @return Verdadeiro se existe a coluna
	 * @throws SQLQueryAnalyzerException
	 */
	private boolean existsColumnByAlias(INode select, String alias, String columnName) throws SQLQueryAnalyzerException {
		/*
		 * Localiza os nós do tipo ColumnNode no Select
		 */
		INode[] columns = ParserUtil.findChildren(select, ColumnNode.class.getSimpleName());
		for (INode column : columns) {
			/*
			 * Somente as colunas que foram usadas no Select
			 */
			if (column.getParent() instanceof SelectNode) {
				String tn = ((ColumnNode) column).getTableName();
				String cn = ((ColumnNode) column).getColumnName();
				/*
				 * E que estejam com "*" ou o nome da coluna igual a desejada
				 */
				if ((columnName.equalsIgnoreCase(cn) || cn.equalsIgnoreCase("*")) && alias.equalsIgnoreCase(tn))
					return true;
			}
		}
		return false;
	}

	/**
	 * Monta a lista de expressões e aliases das colunas que serão usados na montagem dos objetos pelo EntityHandler.
	 * 
	 * @param selectStatement
	 *            Select
	 * @throws SQLQueryAnalyzerException
	 */
	private void buildExpressionsAndColumnAliases(SelectStatementNode selectStatement) throws SQLQueryAnalyzerException {
		/*
		 * Limpa a lista de aliases das colunas
		 */
		columnAliases.clear();
		/*
		 * Busca todos os nós do tipo ColumnNode
		 */
		SelectNode select = (SelectNode) ParserUtil.findFirstChild(selectStatement, "SelectNode");
		INode[] columns = ParserUtil.findChildren(select, ColumnNode.class.getSimpleName());
		/*
		 * Cria lista temporária de expressões no formato String para facilitar a criação das expressões no formato
		 * ExpressionFieldMapper
		 */
		Map<String[], String[]> expressions = new TreeMap<String[], String[]>(this);
		for (INode column : columns) {
			if (column.getParent() instanceof SelectNode) {
				/*
				 * Pega nome da tabela, da coluna e alias da coluna
				 */
				String tableName = ((ColumnNode) column).getTableName();
				String columnName = ((ColumnNode) column).getColumnName();
				String aliasName = ((ColumnNode) column).getColumnName();
				if (((ColumnNode) column).hasAlias()) {
					aliasName = ((ColumnNode) column).getAliasName();
				}
				/*
				 * Busca o objeto SQLQueryAnalyserAlias correspondente ao tableName da coluna
				 */
				SQLQueryAnalyserAlias alias = getAlias(tableName);
				if (alias != null) {
					/*
					 * Pega a Entidade mapeada para o alias
					 */
					EntityCache caches[] = { alias.getEntity() };
					/*
					 * Se for abstrata pega as entidades concretas + abstrata para obter as colunas
					 */
					if (alias.getEntity().isAbstractClass())
						caches = entityCacheManager.getEntitiesBySuperClassIncluding(alias.getEntity());

					for (EntityCache cache : caches) {
						/*
						 * Busca o descriptionColumn correspondente ao nome da coluna
						 */
						DescriptionColumn descriptionColumn = cache.getDescriptionColumnByName(columnName);
						if (descriptionColumn == null)
							continue;
						/*
						 * Adiciona na lista aliases das colunas
						 */
						if (!columnAliases.containsKey(alias)) {
							columnAliases.put(alias, new HashMap<String, String[]>());
						}
						columnAliases.get(alias).put(columnName, (alias.getAlias() + "." + aliasName).split("\\."));
						/*
						 * Se a coluna tem um descriptionField e o campo é uma coleção, função ou relacionamento não
						 * gera uma expressão
						 */
						if (descriptionColumn.hasDescriptionField()) {
							if (descriptionColumn.getDescriptionField().isAnyCollectionOrMap()
									|| (descriptionColumn.getDescriptionField().isJoinTable() || (descriptionColumn.getDescriptionField().isRelationShip())))
								continue;
						}
						/*
						 * Se encontrou o descriptionColumn e não é do tipo discriminatorColumn cria uma expressão.
						 */
						if ((descriptionColumn != null) && (!descriptionColumn.isDiscriminatorColumn())) {
							String path = alias.getPath();
							if (!path.equals(""))
								path += ".";
							/*
							 * Adiciona expressão formato de String[] para evitar fazer split posteriormente.
							 */
							expressions.put((path + descriptionColumn.getDescriptionField().getName()).split("\\."), (((alias.getAliasPath()).equals("") ? ""
									: alias.getAliasPath() + ".") + aliasName).split("\\."));
						} else {
							if (!((cache.getDiscriminatorColumn() != null) && (cache.getDiscriminatorColumn().getColumnName().equalsIgnoreCase(columnName))))
								throw new SQLQueryAnalyzerException("A coluna " + columnName + " não foi encontrada na configuração da classe "
										+ cache.getEntityClass().getName());
						}
					}

				}
			}
		}

		/*
		 * Com base na lista de expressões gerados no código acima no formato String[] gera as expressões no formato
		 * ExpressionFieldMapper que serão usadas pelo EntityHandler para criar os objetos da classe de resultado.
		 */
		for (String[] expression : expressions.keySet()) {
			/*
			 * Chama o método que cria a expressão.
			 */
			makeExpressionFieldMapper(null, resultClass, expression, 0, expressions.get(expression));
		}
	}

	/**
	 * Retorna um objeto ExpressionFieldMapper baseado no nome do campo.
	 * 
	 * @param fieldName
	 *            Nome do campo
	 * @return Objeto ExpressionFieldMapper correspondente ao nome.
	 */
	private ExpressionFieldMapper getExpressionFieldByName(String fieldName) {
		for (ExpressionFieldMapper expressionField : expressionsFieldMapper) {
			if (expressionField.getDescriptionField().getField().getName().equalsIgnoreCase(fieldName)) {
				return expressionField;
			}
		}
		return null;
	}

	/**
	 * Cria uma expressão do tipo ExpressionFieldMapper baseado na expressão formato String[] parseada do sql.
	 * 
	 * @param owner
	 *            Expressão pai
	 * @param targetClass
	 *            Classe alvo
	 * @param expression
	 *            Expressão no formato String[] correspondente ao caminho na árvore do objeto.
	 * @param position
	 *            Posição inicial dentro da expressão.
	 * @param aliasPathWithColumn
	 *            Expressão no formato String[] correspodente aos aliases no sql.
	 */
	private void makeExpressionFieldMapper(ExpressionFieldMapper owner, Class<?> targetClass, String[] expression, int position, String[] aliasPathWithColumn) {

		EntityCache resultEntityCache = entityCacheManager.getEntityCache(targetClass);

		EntityCache caches[] = { resultEntityCache };
		/*
		 * Se for uma classe abstrata pega as classes concretas
		 */
		if (resultEntityCache.isAbstractClass())
			caches = entityCacheManager.getEntitiesBySuperClass(resultEntityCache);

		/*
		 * Pega a primeira parte da expressão onde será iniciado o processamento
		 */
		String targetField = expression[position];
		String aliasExpression = aliasPathWithColumn[position];

		int oldPosition = position;
		for (EntityCache entityCache : caches) {
			position = oldPosition;
			/*
			 * Pega o DescriptionColumn do field alvo
			 */
			DescriptionField descriptionField = entityCache.getDescriptionField(targetField);
			if (descriptionField == null)
				continue;

			/*
			 * Se a posição passada for a última da lista de expressões isto corresponde a um campo simples a ser
			 * atribuido ao objeto alvo. Criamos aqui um SimpleExpressionFieldMapper que irá fazer isto.
			 */
			if (position == expression.length - 1) {
				if (owner != null)
					owner.addChild(new SimpleExpressionFieldMapper(entityCache, descriptionField, aliasExpression));
				else
					expressionsFieldMapper.add(new SimpleExpressionFieldMapper(entityCache, descriptionField, aliasExpression));
			} else {
				ExpressionFieldMapper expressionField = null;
				if (owner != null) {
					expressionField = owner.getExpressionFieldByName(descriptionField.getField().getName());
				} else {
					expressionField = getExpressionFieldByName(descriptionField.getField().getName());
				}
				if (expressionField == null) {
					/*
					 * Se o campo for uma coleção criamos aqui um CollectionExpressionFieldMapper que irá criar a mesma
					 * e atribuir ao objeto alvo.
					 */
					if (descriptionField.isCollectionEntity()) {
						EntityCache fieldEntityCache = entityCacheManager.getEntityCache(descriptionField.getTargetClass());
						String discriminatorColumnName = "";
						/*
						 * Se a classe alvo da coleção for abstrata guardamos o nome da coluna correspondente ao
						 * discriminatorColumn no ResultSet para ser usado posteriormente para saber qual instância
						 * concreta do objeto deverá ser criada.
						 */
						if (ReflectionUtils.isAbstractClass(descriptionField.getTargetClass())) {
							discriminatorColumnName = getAliasColumnName(fieldEntityCache, fieldEntityCache.getDiscriminatorColumn().getColumnName());
						}
						/*
						 * Armazenamos também o nome das colunas no ResultSet que formam a chave do objeto para
						 * facilitar a busca da chave posteriormente e verificar se devemos criar o objeto
						 */
						List<String> aliasPrimaryKeyColumns = new ArrayList<String>();
						for (DescriptionColumn column : fieldEntityCache.getPrimaryKeyColumns()) {
							if (aliasExpression != null) {
								aliasPrimaryKeyColumns.add(getAliasColumnName(aliasExpression, column.getColumnName()));
							} else {
								aliasPrimaryKeyColumns.add(getAliasColumnName(fieldEntityCache, column.getColumnName()));
							}
						}
						expressionField = new CollectionExpressionFieldMapper(fieldEntityCache, descriptionField, aliasExpression, discriminatorColumnName,
								aliasPrimaryKeyColumns.toArray(new String[] {}));
					} else {
						EntityCache fieldEntityCache = entityCacheManager.getEntityCache(descriptionField.getField().getType());
						String discriminatorColumnName = "";
						/*
						 * Se a classe da entidade for abstrata guardamos o nome da coluna correspondente ao
						 * discriminatorColumn no ResultSet para ser usado posteriormente para saber qual instância
						 * concreta do objeto deverá ser criada.
						 */
						if (ReflectionUtils.isAbstractClass(descriptionField.getTargetClass())) {
							discriminatorColumnName = getAliasColumnName(fieldEntityCache, fieldEntityCache.getDiscriminatorColumn().getColumnName());
						}
						/*
						 * Armazenamos também o nome das colunas no ResultSet que formam a chave do objeto para
						 * facilitar a busca da chave posteriormente e verificar se devemos criar o objeto
						 */
						List<String> aliasPrimaryKeyColumns = new ArrayList<String>();
						for (DescriptionColumn column : fieldEntityCache.getPrimaryKeyColumns()) {
							if (aliasExpression != null) {
								aliasPrimaryKeyColumns.add(getAliasColumnName(aliasExpression, column.getColumnName()));
							} else {
								aliasPrimaryKeyColumns.add(getAliasColumnName(fieldEntityCache, column.getColumnName()));
							}
						}

						expressionField = new EntityExpressionFieldMapper(fieldEntityCache, descriptionField, aliasExpression, discriminatorColumnName,
								aliasPrimaryKeyColumns.toArray(new String[] {}));
					}
					/*
					 * Se foi informado o ExpressionFieldMapper então a nova expressão será filha da expressão pai
					 * formando assim uma árvore de expressões.
					 */
					if (owner != null) {
						owner.addChild(expressionField);
					} else {
						/*
						 * Sem um pai a expressão é adicionada na lista primária de expressões(primeiro nível).
						 */
						expressionsFieldMapper.add(expressionField);
					}
				}
				/*
				 * Incrementa a posição para processar próxima expressão
				 */
				position++;
				/*
				 * Chama o método novamente fazendo assim recursivamente o processamento das expressões. Na nova chamada
				 * o a expressão criada será o pai da próxima expressão.
				 */
				makeExpressionFieldMapper(expressionField, expressionField.getTargetEntityCache().getEntityClass(), expression, position, aliasPathWithColumn);
			}
		}
	}

	/**
	 * Retorna o objeto SQLQueryAnalyserAlias correspondente ao alias informado.
	 * 
	 * @param alias
	 *            Nome do alias da tabela.
	 * @return SQLQueryAnalyserAlias correspodente a tabela.
	 */
	protected SQLQueryAnalyserAlias getAlias(String alias) {
		for (SQLQueryAnalyserAlias a : aliases) {
			if (a.getAlias().equalsIgnoreCase(alias))
				return a;
		}
		return null;
	}

	/**
	 * Retorna o nome do alias da coluna usada no SQL.
	 * 
	 * @param sourceAlias
	 *            Alias da tabela
	 * @param columnName
	 *            Nome da coluna
	 * @return Alias da coluna
	 */
	private String getAliasColumnName(String sourceAlias, String columnName) {
		String result = null;
		for (SQLQueryAnalyserAlias queryAnalyserAlias : columnAliases.keySet()) {
			if (queryAnalyserAlias.getAlias().equals(sourceAlias)) {
				String[] alias = null;
				for (String column : columnAliases.get(queryAnalyserAlias).keySet()) {
					if (column.equalsIgnoreCase(columnName)) {
						alias = columnAliases.get(queryAnalyserAlias).get(column);
						break;
					}
				}
				result = (alias == null || alias.length <= 1 ? columnName : alias[1]);
				return result;
			}
		}
		return columnName;
	}

	/**
	 * Retorna o alias da coluna usada no SQL.
	 * 
	 * @param sourceEntityCache
	 *            Entidade correspondente ao alias da tabela
	 * @param columnName
	 *            Nome da coluna
	 * @return Alias da coluna
	 */
	private String getAliasColumnName(EntityCache sourceEntityCache, String columnName) {
		String result = null;
		for (SQLQueryAnalyserAlias queryAnalyserAlias : columnAliases.keySet()) {
			if (queryAnalyserAlias.getEntity().equals(sourceEntityCache)
					|| (ReflectionUtils.isExtendsClass(queryAnalyserAlias.getEntity().getEntityClass(), sourceEntityCache.getEntityClass()))) {
				String[] alias = null;
				for (String column : columnAliases.get(queryAnalyserAlias).keySet()) {
					if (column.equalsIgnoreCase(columnName)) {
						alias = columnAliases.get(queryAnalyserAlias).get(column);
						break;

					}
				}
				result = (alias == null || alias.length == 0 ? columnName : alias[alias.length - 1]);
				return result;
			}
		}
		return columnName;
	}

	/**
	 * Retorna uma lista com os nomes das colunas que são iguais a dois aliases. Usado para detectar o relacionamento
	 * das entidades no SQL.
	 * 
	 * @param selectStatement
	 *            Select
	 * @param sourceAlias
	 *            Alias de origem
	 * @param targetAlias
	 *            Alias de destino
	 * @return Lista com nomes das colunas
	 * @throws SQLQueryAnalyzerException
	 */
	private List<String> getColumnNameEqualsAliases(SelectStatementNode selectStatement, SQLQueryAnalyserAlias sourceAlias, SQLQueryAnalyserAlias targetAlias)
			throws SQLQueryAnalyzerException {
		List<String> result = new ArrayList<String>();
		/*
		 * Busca nós no select que são do tipo OperatorNode (operadores). Isto irá retornar operadores usados no Where
		 * ou Join.
		 */
		INode[] expressions = ParserUtil.findChildren(selectStatement, OperatorNode.class.getSimpleName());
		for (INode expression : expressions) {
			OperatorNode operator = (OperatorNode) expression;
			/*
			 * Se o operador for um "=" (igual)
			 */
			if ("=".equals(operator.getName())) {
				/*
				 * Se a esquerda e a direita do operador for uma coluna e não um valor é possível que seja um
				 * relacionamento(junção) entre entidades.
				 */
				if ((operator.getChild(0) instanceof ColumnNode) && (operator.getChild(1) instanceof ColumnNode)
						&& !(operator.getChild(0) instanceof ValueNode) && !(operator.getChild(1) instanceof ValueNode)) {
					/*
					 * Se a esquerda ou a direita do operador não for um parâmetro
					 */
					if (!((operator.getChild(0) instanceof BindNode) || (operator.getChild(1) instanceof BindNode))) {
						ColumnNode columnLeft = (ColumnNode) operator.getChild(0);
						ColumnNode columnRight = (ColumnNode) operator.getChild(1);

						/*
						 * Pega nome da tabela e coluna da esquerda
						 */
						String tn = columnLeft.getTableName();
						String cn = columnLeft.getColumnName();
						if ((tn == null) || ("".equals(tn))) {
							throw new SQLQueryAnalyzerException(
									"Foi encontrado a coluna "
											+ cn
											+ " sem o alias da tabela de origem na condição WHERE. É necessário que seja informado o alias da tabela para que seja possível realizar o mapeamento correto do objeto da classe de resultado "
											+ resultClass.getName());
						}

						/*
						 * Pega nome da tabela e coluna da direita
						 */
						tn = columnRight.getTableName();
						cn = columnRight.getColumnName();
						if ((tn == null) || ("".equals(tn))) {
							throw new SQLQueryAnalyzerException(
									"Foi encontrado a coluna "
											+ cn
											+ " sem o alias da tabela de origem na condição WHERE. É necessário que seja informado o alias da tabela para que seja possível realizar o mapeamento correto do objeto da classe de resultado "
											+ resultClass.getName());
						}

						/*
						 * Se a coluna da esquerda e direita tiverem o alias da tabela.
						 */
						if ((columnRight.getTableName() != null) && (columnLeft.getTableName() != null)) {
							/*
							 * Se encontrar uma igualdade nos nomes da tabela e coluna adiciona na lista. Verificação da
							 * esquerda pra direita e da direita pra esquerda (bidirecional).
							 */
							if ((columnLeft.getTableName().equalsIgnoreCase(sourceAlias.getAlias()) && (columnRight.getTableName().equalsIgnoreCase(targetAlias
									.getAlias()))))
								result.add(columnRight.getColumnName());
							else if ((columnRight.getTableName().equalsIgnoreCase(sourceAlias.getAlias()) && (columnLeft.getTableName()
									.equalsIgnoreCase(targetAlias.getAlias()))))
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
		StringBuilder sb = new StringBuilder();
		for (SQLQueryAnalyserAlias alias : aliases) {
			sb.append("\nalias=").append(alias.getAlias()).append(" [");
			sb.append(" ").append(alias.getEntity().toString());
			sb.append("] ");
			sb.append(alias.getAliasPath());
		}
		return sb.toString();
	}

	public String getParsedSQL() {
		return sql;
	}

	/**
	 * Retorna o objeto SQLQueryAnalyserAlias correspondente ao nome do alias.
	 * 
	 * @param aliases
	 *            Lista de aliases
	 * @param name
	 *            Nome do alias
	 * @return SQLQueryAnalyserAlias correspondente ao alias
	 */
	private SQLQueryAnalyserAlias getAliasByName(Set<SQLQueryAnalyserAlias> aliases, String name) {
		for (SQLQueryAnalyserAlias alias : aliases) {
			if (alias.getAlias().equalsIgnoreCase(name))
				return alias;
		}
		return null;
	}

	public Map<SQLQueryAnalyserAlias, Map<String, String[]>> getColumnAliases() {
		return columnAliases;
	}

	/**
	 * Ajusta o nome do alias gerado para a coluna para não ultrapassar o máximo de caracteres permitido pelo dialeto do
	 * banco de dados.
	 * 
	 * @param aliasColumnNamePrefix
	 *            Prefixo do nome da coluna
	 * @return
	 */
	private String adpatAliasColumnName(String aliasColumnNamePrefix) {
		int maximumNameLength = databaseDialect.getMaxColumnNameSize() - 8;
		String result = adjustName(aliasColumnNamePrefix);

		if (result.length() > maximumNameLength) {
			result = StringUtils.removeAllButAlphaNumericToFit(aliasColumnNamePrefix, maximumNameLength);
			if (result.length() > maximumNameLength) {
				String onlyAlphaNumeric = StringUtils.removeAllButAlphaNumericToFit(aliasColumnNamePrefix, 0);
				result = StringUtils.shortenStringsByRemovingVowelsToFit(onlyAlphaNumeric, "", maximumNameLength);
				if (result.length() > maximumNameLength) {
					String shortenedName = StringUtils.removeVowels(onlyAlphaNumeric);
					if (shortenedName.length() >= maximumNameLength) {
						result = StringUtils.truncate(shortenedName, maximumNameLength);
					} else {
						result = StringUtils.truncate(shortenedName, maximumNameLength - shortenedName.length());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Ajusta o nome da coluna gerado
	 * 
	 * @param name
	 *            Nome da coluna
	 * @return Nome ajustado.
	 */
	private String adjustName(String name) {
		String adjustedName = name;
		if (adjustedName.indexOf(' ') != -1 || adjustedName.indexOf('\"') != -1 || adjustedName.indexOf('`') != -1) {
			StringBuilder buff = new StringBuilder();
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c != ' ' && c != '\"' && c != '`') {
					buff.append(c);
				}
			}
			adjustedName = buff.toString();
		}
		return adjustedName;
	}

	@Override
	public int compare(String[] o1, String[] o2) {
		return (Arrays.toString(o1).compareTo(Arrays.toString(o2)));
	}
}


