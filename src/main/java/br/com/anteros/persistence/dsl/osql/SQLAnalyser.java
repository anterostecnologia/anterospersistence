package br.com.anteros.persistence.dsl.osql;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.Operation;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathType;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpression;
import br.com.anteros.persistence.dsl.osql.types.Visitor;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanExpression;
import br.com.anteros.persistence.handler.ResultClassDefinition;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

public class SQLAnalyser implements Visitor<Void, Void> {

	private static int MAKE_ALIASES = 1;
	private static int MAKE_COLUMNS = 2;
	private static final String COMMA = ", ";
	private AbstractOSQLQuery<?> query;
	private Map<Path<?>, EntityPath<?>> aliases = new HashMap<Path<?>, EntityPath<?>>();
	private Map<Integer, Set<SQLAnalyserColumn>> columns = new HashMap<Integer, Set<SQLAnalyserColumn>>();
	private int nextAliasTableName = 0;
	private int nextAliasColumnName = 0;
	private int level = MAKE_ALIASES;
	private List<Expression<?>> individualExpressions = new ArrayList<Expression<?>>();
	private SQLAnalyserColumn lastColumnAdded = null;

	private boolean inOperation = false;
	private Boolean namedParameter = null;
	private int currentIndex;

	public SQLAnalyser(AbstractOSQLQuery<?> query) {
		this.query = query;
	}

	@Override
	public Void visit(Constant<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(FactoryExpression<?> expr, Void context) {
		for (Expression<?> arg : expr.getArgs()) {
			arg.accept(this, null);
		}
		return null;
	}

	@Override
	public Void visit(Operation<?> expr, Void context) {
		try {
			inOperation = true;
			for (Expression<?> arg : expr.getArgs()) {
				arg.accept(this, null);
			}
		} finally {
			inOperation = false;
		}
		return null;
	}

	@Override
	public Void visit(ParamExpression<?> expr, Void context) {
		if (namedParameter != null) {
			if (namedParameter != (!expr.isAnon())) {
				throw new SQLAnalyserException(
						"Foram encontrados parâmetros nomeados e não nomeados. Use apenas um formato de parâmetros na consulta.");
			}
		}
		this.namedParameter = !expr.isAnon();
		return null;
	}

	@Override
	public Void visit(Path<?> expr, Void context) {
		if (level == MAKE_ALIASES) {
			try {
				makePossibleJoins(expr);
			} catch (Exception e) {
				throw new SQLAnalyserException(e.getMessage(), e);
			}
		} else if (level == MAKE_COLUMNS) {
			processColumns(expr);
		}
		return null;
	}

	private void processColumns(Path<?> path) {
		if ((path.getMetadata().getPathType() == PathType.VARIABLE) && (path instanceof EntityPath<?>)) {
			EntityCache sourceEntityCache = this.query.getSession().getEntityCacheManager()
					.getEntityCache(getClassByEntityPath((EntityPath<?>) path));
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + getClassByEntityPath((EntityPath<?>) path)
						+ " não foi encontrada na lista de entidades gerenciadas.");

			String alias = path.getMetadata().getName();
			if (((EntityPath<?>) path).getCustomProjection().size() > 0) {
				for (Path<?> customPath : ((EntityPath<?>) path).getCustomProjection()) {
					if (customPath.equals(path))
						processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
					else
						processColumns(customPath);
				}
			} else
				processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
		} else if (path.getMetadata().getPathType() == PathType.PROPERTY) {
			EntityPath<?> entityPath = this.getAliasByEntityPath(path);
			String alias = entityPath.getMetadata().getName();

			EntityCache sourceEntityCache = this.query.getSession().getEntityCacheManager().getEntityCache(getClassByEntityPath(entityPath));
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + getClassByEntityPath(entityPath)
						+ " não foi encontrada na lista de entidades gerenciadas.");
			if (path instanceof EntityPath<?>) {
				if (((EntityPath<?>) path).getCustomProjection().size() > 0) {
					for (Path<?> customPath : ((EntityPath<?>) path).getCustomProjection()) {
						if (customPath.equals(path))
							processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
						else
							processColumns(customPath);
					}
				} else
					processAllDescriptionFields((EntityPath<?>) path, alias, sourceEntityCache);
			} else {
				DescriptionField descriptionField = sourceEntityCache.getDescriptionField(path.getMetadata().getName());
				if (descriptionField == null)
					throw new SQLSerializerException("O campo " + path.getMetadata().getName() + " não foi encontrado na classe "
							+ getClassByEntityPath(entityPath) + ". ");
				processDescriptionField(currentIndex, alias, descriptionField, true);
			}
		} else if (path.getMetadata().getPathType() == PathType.VARIABLE) {
			if (level == MAKE_COLUMNS) {
				if (lastColumnAdded != null) {
					lastColumnAdded.setAliasColumnName(path.getMetadata().getName());
					lastColumnAdded.setUserAliasDefined(true);
				}
			}
		}

	}

	protected boolean processDescriptionField(Integer index, String alias, DescriptionField descriptionField, boolean makeAlias) {
		Set<SQLAnalyserColumn> columnsForPath = getColumnsListForPath(index);
		if (descriptionField.isSimple()) {
			String aliasColumnName = "";
			if (makeAlias)
				aliasColumnName = makeNextAliasName(alias, descriptionField.getSimpleColumn().getColumnName());
			lastColumnAdded = new SQLAnalyserColumn(alias, descriptionField.getSimpleColumn().getColumnName(), aliasColumnName);
			columnsForPath.add(lastColumnAdded);
			return true;
		} else if (descriptionField.isRelationShip()) {
			for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
				String aliasColumnName = "";
				if (makeAlias)
					aliasColumnName = makeNextAliasName(alias, column.getColumnName());
				lastColumnAdded = new SQLAnalyserColumn(alias, column.getColumnName(), aliasColumnName);
				columnsForPath.add(lastColumnAdded);
			}
			return true;
		}
		return false;
	}

	private Set<SQLAnalyserColumn> getColumnsListForPath(Integer index) {
		Set<SQLAnalyserColumn> result = null;
		if (columns.containsKey(index))
			result = columns.get(index);
		if (result == null) {
			result = new LinkedHashSet<SQLAnalyserColumn>();
			columns.put(index, result);
		}

		return result;
	}

	private List<String> getColumnsForPathAsString(Integer index) {
		List<String> result = new ArrayList<String>();
		Set<SQLAnalyserColumn> columns = getColumnsListForPath(index);
		for (SQLAnalyserColumn column : columns) {
			result.add((StringUtils.isEmpty(column.getAliasColumnName()) ? column.getColumnName() : column.getAliasColumnName()));
		}
		return result;
	}

	protected void processAllDescriptionFields(EntityPath<?> entityPath, String alias, EntityCache sourceEntityCache) {

		for (DescriptionField descriptionField : sourceEntityCache.getDescriptionFields()) {
			if (descriptionField.isAnyCollection())
				continue;

			processDescriptionField(currentIndex, alias, descriptionField, true);
		}
	}

	@Override
	public Void visit(SubQueryExpression<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(TemplateExpression<?> expr, Void context) {
		return null;
	}

	public void process() throws Exception {
		QueryMetadata metadata = query.getMetadata();
		this.individualExpressions = extractIndividualColumnsExpression(metadata);
		processExpressions(metadata, MAKE_ALIASES);
		processExpressions(metadata, MAKE_COLUMNS);

		// for (Integer index : columns.keySet()) {
		// Set<SQLAnalyserColumn> list = columns.get(index);
		// for (SQLAnalyserColumn col : list)
		// System.out.println(index + "-> " + col);
		// }
	}

	public static List<Expression<?>> extractIndividualColumnsExpression(QueryMetadata metadata) {
		List<Expression<?>> select = metadata.getProjection();
		List<Expression<?>> sqlSelect;
		if (select.size() == 1) {
			final Expression<?> first = select.get(0);
			if (first instanceof FactoryExpression) {
				sqlSelect = ((FactoryExpression<?>) first).getArgs();
			} else {
				sqlSelect = (List) select;
			}
		} else {
			sqlSelect = new ArrayList<Expression<?>>(select.size());
			for (Expression<?> selectExpr : select) {
				if (selectExpr instanceof FactoryExpression) {
					sqlSelect.addAll(((FactoryExpression<?>) selectExpr).getArgs());
				} else {
					sqlSelect.add(selectExpr);
				}
			}
		}

		return sqlSelect;
	}

	protected void processExpressions(QueryMetadata metadata, int level) throws Exception {
		this.level = level;
		final List<Expression<?>> select = this.individualExpressions;
		final Predicate where = metadata.getWhere();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();
		final List<Expression<?>> groupBy = metadata.getGroupBy();

		if ((this.level == MAKE_ALIASES) || (this.level == MAKE_COLUMNS)) {
			currentIndex = 0;
			for (Expression<?> expr : select) {
				expr.accept(this, null);
				if (expr instanceof EntityPath<?>) {
					List<Path<?>> customProjection = ((EntityPath<?>) expr).getCustomProjection();
					for (Path<?> path : customProjection) {
						if (!(path.equals(expr)))
							makePossibleJoins(path);
					}
					for (Path<?> path : customProjection) {
						if (!(path.equals(expr)))
							processColumns(path);
					}

				}
				currentIndex++;
			}
		}
		if (this.level == MAKE_ALIASES) {
			if (where != null)
				where.accept(this, null);
			if (having != null)
				having.accept(this, null);
			if (groupBy != null) {
				currentIndex = 0;
				for (Expression<?> expr : select) {
					expr.accept(this, null);
					currentIndex++;
				}
			}
			if (orderBy != null) {
				for (OrderSpecifier<?> ord : orderBy) {
					ord.getTarget().accept(this, null);
				}
			}
		}
	}

	protected void makePossibleJoins(Path<?> expr) throws Exception {
		if (expr.getMetadata().isRoot())
			return;
		makePossibleJoins(expr.getMetadata().getParent());
		if (expr instanceof EntityPath) {
			if (!aliases.containsKey(expr)) {
				String alias = makeNextAliasTableName();
				EntityPath<?> newPath = (EntityPath<?>) ReflectionUtils.invokeConstructor(expr.getClass(), alias);
				BooleanExpression boExpression = (BooleanExpression) ReflectionUtils.invokeMethod(expr, "eq", newPath);
				query.leftJoin(newPath).on(boExpression);
				aliases.put(expr, newPath);
			}
		}
	}

	public Map<Path<?>, EntityPath<?>> getAliases() {
		return aliases;
	}

	public EntityPath<?> getAliasByEntityPath(Path<?> path) {
		EntityPath<?> result = null;
		if (path instanceof EntityPath<?>) {
			result = aliases.get(path);
			if (result == null)
				result = (EntityPath<?>) path.getMetadata().getParent();
		} else {
			result = aliases.get(path.getMetadata().getParent());
			if (result == null)
				result = (EntityPath<?>) path.getMetadata().getParent();
		}
		return result;
	}

	public Class<?> getClassByEntityPath(EntityPath<?> path) {
		Type mySuperclass = path.getClass().getGenericSuperclass();
		return (Class<?>) ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
	}

	private String makeNextAliasTableName() {
		nextAliasTableName++;
		String result = "TB_" + String.valueOf(nextAliasTableName);
		return result;
	}

	private String makeNextAliasName(String alias, String columnName) {
		nextAliasColumnName++;
		return adpatAliasColumnName(alias) + "_COL_" + String.valueOf(nextAliasColumnName);
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
		int maximumNameLength = query.getSession().getDialect().getMaxColumnNameSize() - 8;
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

	public Map<Integer, Set<SQLAnalyserColumn>> getColumns() {
		return columns;
	}

	public List<Expression<?>> getIndividualExpressions() {
		return individualExpressions;
	}

	public List<ResultClassDefinition> getResultClassDefinitions() {
		List<Expression<?>> select = getIndividualExpressions();
		List<ResultClassDefinition> result = new ArrayList<ResultClassDefinition>();
		int index = 0;
		for (Expression<?> expr : select) {
			if (expr instanceof FactoryExpression) {
				FactoryExpression<?> factory = ((FactoryExpression<?>) expr);
				Class<?> resultClass = factory.getType();
				List<String> columns = new ArrayList<String>();
				for (Expression<?> exprFact : factory.getArgs()) {
					columns.addAll(getColumnsForPathAsString(index));
					index++;
				}
				result.add(new ResultClassDefinition(resultClass, columns));
			} else {
				result.add(new ResultClassDefinition(expr.getType(), getColumnsForPathAsString(index)));
				index++;
			}
		}
		return result;
	}

	public boolean isNamedParameter() {
		return (namedParameter == null ? false : namedParameter);
	}
}
