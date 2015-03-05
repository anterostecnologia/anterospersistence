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
package br.com.anteros.persistence.dsl.osql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import br.com.anteros.core.configuration.SessionFactoryConfiguration;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.support.Expressions;
import br.com.anteros.persistence.dsl.osql.support.SerializerBase;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.ConstantImpl;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Operator;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.Order;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathType;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.Template;
import br.com.anteros.persistence.dsl.osql.types.Template.Element;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateFactory;
import br.com.anteros.persistence.dsl.osql.types.path.CollectionPathBase;
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorColumnPath;
import br.com.anteros.persistence.dsl.osql.types.path.EntityPathBase;
import br.com.anteros.persistence.dsl.osql.util.LiteralUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * SqlSerializer serializes Querydsl queries into SQL
 *
 * @author tiwe
 */
public class SQLSerializer extends SerializerBase<SQLSerializer> {

	protected enum Stage {
		SELECT, FROM, WHERE, GROUP_BY, HAVING, ORDER_BY, MODIFIERS
	}

	private static final Expression<?> Q = Expressions.template(Object.class, "?");

	private static final String COMMA = ", ";

	private final List<Path<?>> constantPaths = new ArrayList<Path<?>>();

	private final List<Object> constants = new ArrayList<Object>();

	protected Stage stage = Stage.SELECT;

	private boolean skipParent;

	private final SQLTemplates templates;

	private boolean inUnion = false;

	private boolean inJoin = false;

	private boolean inOperation = false;

	private boolean useLiterals = false;

	private EntityCacheManager entityCacheManager;

	private SQLAnalyser analyser;

	public SQLSerializer(EntityCacheManager entityCacheManager, SQLTemplates templates) {
		super(templates);
		this.templates = templates;
		this.entityCacheManager = entityCacheManager;
	}

	public SQLSerializer(EntityCacheManager entityCacheManager, SQLTemplates templates, SQLAnalyser analyser) {
		super(templates);
		this.templates = templates;
		this.entityCacheManager = entityCacheManager;
		this.analyser = analyser;
	}

	protected Expression<?>[] getAllFieldExpressions(EntityPathBase<?> path) throws Exception {
		List<Expression<?>> result = new ArrayList<Expression<?>>();

		Field[] fields = ReflectionUtils.getAllDeclaredFields(path.getClass());

		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers())) {
				if ((ReflectionUtils.isExtendsClass(Path.class, field.getType()))
						&& (!ReflectionUtils.isExtendsClass(EntityPath.class, field.getType()))
						&& ((!ReflectionUtils.isExtendsClass(CollectionPathBase.class, field.getType())))) {
					Expression<?> expr = (Expression<?>) field.get(path);
					result.add(expr);
				}
			}
		}

		return result.toArray(new Expression<?>[] {});
	}

	public List<Object> getConstants() {
		return constants;
	}

	public List<Path<?>> getConstantPaths() {
		return constantPaths;
	}

	protected SQLTemplates getTemplates() {
		return templates;
	}

	public void handle(String template, Object... args) {
		handleTemplate(TemplateFactory.DEFAULT.create(template), Arrays.asList(args));
	}

	private void handleJoinTarget(JoinExpression je) {
		if ((je.getTarget() instanceof EntityPath) && (templates.isSupportsAlias())) {
			if (((EntityPath<?>) je.getTarget()).getMetadata().getParent() == null) {
				if (templates.isPrintSchema()) {
					append(".");
				}
				EntityCache entityCache = getEntityCacheByPath((EntityPath<?>) je.getTarget());
				append(entityCache.getTableName());
				append(templates.getTableAlias());
			}
		}
		inJoin = true;
		handle(je.getTarget());
		inJoin = false;
	}

	public void serialize(QueryMetadata metadata, boolean forCountRow) {
		templates.serialize(metadata, forCountRow, this);
	}

	public void serializeForQuery(QueryMetadata metadata, boolean forCountRow) {
		skipParent = true;
		final List<? extends Expression<?>> select = metadata.getProjection();
		final List<JoinExpression> joins = metadata.getJoins();
		final Predicate where = metadata.getWhere();
		final List<? extends Expression<?>> groupBy = metadata.getGroupBy();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();
		final Set<QueryFlag> flags = metadata.getFlags();
		final boolean hasFlags = !flags.isEmpty();
		String suffix = null;

		List<Expression<?>> sqlSelect = analyser.getIndividualExpressions();

		// with
		if (hasFlags) {
			boolean handled = false;
			boolean recursive = false;
			for (QueryFlag flag : flags) {
				if (flag.getPosition() == Position.WITH) {
					if (flag.getFlag() == SQLTemplates.RECURSIVE) {
						recursive = true;
						continue;
					}
					if (handled) {
						append(", ");
					}
					handle(flag.getFlag());
					handled = true;
				}
			}
			if (handled) {
				if (recursive) {
					prepend(templates.getWithRecursive());
				} else {
					prepend(templates.getWith());
				}
				append("\n");
			}
		}

		// start
		if (hasFlags) {
			serialize(Position.START, flags);
		}

		// select
		Stage oldStage = stage;
		stage = Stage.SELECT;
		if (forCountRow) {
			append(templates.getSelect());
			if (hasFlags) {
				serialize(Position.AFTER_SELECT, flags);
			}

			if (!metadata.isDistinct()) {
				append(templates.getCountStar());
			} else {
				List<? extends Expression<?>> columns;
				if (sqlSelect.isEmpty()) {
					throw new SQLSerializerException("Informe uma clausula para o select.");
				} else {
					columns = sqlSelect;
				}
				if (columns.size() == 1) {
					append(templates.getDistinctCountStart());
					handle(columns.get(0));
					append(templates.getDistinctCountEnd());
				} else if (templates.isCountDistinctMultipleColumns()) {
					append(templates.getDistinctCountStart());
					append("(").handle(COMMA, columns).append(")");
					append(templates.getDistinctCountEnd());
				} else {
					// select count(*) from (select distinct ...)
					append(templates.getCountStar());
					append(templates.getFrom());
					append("(");
					append(templates.getSelectDistinct());
					handle(COMMA, columns);
					suffix = ") internal";
				}
			}

		} else if (!sqlSelect.isEmpty()) {
			if (!metadata.isDistinct()) {
				append(templates.getSelect());
			} else {
				append(templates.getSelectDistinct());
			}
			if (hasFlags) {
				serialize(Position.AFTER_SELECT, flags);
			}
			handle(COMMA, sqlSelect);
		}
		if (hasFlags) {
			serialize(Position.AFTER_PROJECTION, flags);
		}

		// from
		stage = Stage.FROM;
		serializeSources(joins);

		// where
		if (where != null) {
			stage = Stage.WHERE;
			if (hasFlags) {
				serialize(Position.BEFORE_FILTERS, flags);
			}
			append(templates.getWhere()).handle(where);
			if (hasFlags) {
				serialize(Position.AFTER_FILTERS, flags);
			}
		}

		// group by
		if (!groupBy.isEmpty()) {
			stage = Stage.GROUP_BY;
			if (hasFlags) {
				serialize(Position.BEFORE_GROUP_BY, flags);
			}
			append(templates.getGroupBy()).handle(COMMA, groupBy);
			if (hasFlags) {
				serialize(Position.AFTER_GROUP_BY, flags);
			}
		}

		// having
		if (having != null) {
			stage = Stage.HAVING;
			if (hasFlags) {
				serialize(Position.BEFORE_HAVING, flags);
			}
			append(templates.getHaving()).handle(having);
			if (hasFlags) {
				serialize(Position.AFTER_HAVING, flags);
			}
		}

		// order by
		if (hasFlags) {
			serialize(Position.BEFORE_ORDER, flags);
		}
		if (!orderBy.isEmpty() && !forCountRow) {
			stage = Stage.ORDER_BY;
			append(templates.getOrderBy());
			handleOrderBy(orderBy);
			if (hasFlags) {
				serialize(Position.AFTER_ORDER, flags);
			}
		}

		// modifiers
		if (!forCountRow && metadata.getModifiers().isRestricting() && !joins.isEmpty()) {
			stage = Stage.MODIFIERS;
			templates.serializeModifiers(metadata, this);
		}

		if (suffix != null) {
			append(suffix);
		}

		// reset stage
		stage = oldStage;
	}

	protected void handleOrderBy(List<OrderSpecifier<?>> orderBy) {
		boolean first = true;
		for (final OrderSpecifier<?> os : orderBy) {
			if (!first) {
				append(COMMA);
			}
			String order = os.getOrder() == Order.ASC ? templates.getAsc() : templates.getDesc();
			if (os.getNullHandling() == OrderSpecifier.NullHandling.NullsFirst) {
				if (templates.getNullsFirst() != null) {
					handle(os.getTarget());
					append(order);
					append(templates.getNullsFirst());
				} else {
					append("(case when ");
					handle(os.getTarget());
					append(" is null then 0 else 1 end), ");
					handle(os.getTarget());
					append(order);
				}
			} else if (os.getNullHandling() == OrderSpecifier.NullHandling.NullsLast) {
				if (templates.getNullsLast() != null) {
					handle(os.getTarget());
					append(order);
					append(templates.getNullsLast());
				} else {
					append("(case when ");
					handle(os.getTarget());
					append(" is null then 1 else 0 end), ");
					handle(os.getTarget());
					append(order);
				}

			} else {
				handle(os.getTarget());
				append(order);
			}
			first = false;
		}
	}

	private void serializeSources(List<JoinExpression> joins) {
		if (joins.isEmpty()) {
			String dummyTable = templates.getDummyTable();
			if (!Strings.isNullOrEmpty(dummyTable)) {
				append(templates.getFrom());
				append(dummyTable);
			}
		} else {
			append(templates.getFrom());
			for (int i = 0; i < joins.size(); i++) {
				final JoinExpression je = joins.get(i);
				if (je.getFlags().isEmpty()) {
					if (i > 0) {
						append(templates.getJoinSymbol(je.getType()));
					}
					handleJoinTarget(je);
					if (je.getCondition() != null) {
						append(templates.getOn()).handle(je.getCondition());
					}
				} else {
					serialize(JoinFlag.Position.START, je.getFlags());
					if (!serialize(JoinFlag.Position.OVERRIDE, je.getFlags()) && i > 0) {
						append(templates.getJoinSymbol(je.getType()));
					}
					serialize(JoinFlag.Position.BEFORE_TARGET, je.getFlags());
					handleJoinTarget(je);
					serialize(JoinFlag.Position.BEFORE_CONDITION, je.getFlags());
					if (je.getCondition() != null) {
						append(templates.getOn()).handle(je.getCondition());
					}
					serialize(JoinFlag.Position.END, je.getFlags());
				}
			}
		}
	}

	public void serializeUnion(Expression<?> union, QueryMetadata metadata, boolean unionAll) {
		final List<? extends Expression<?>> groupBy = metadata.getGroupBy();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();
		final Set<QueryFlag> flags = metadata.getFlags();
		final boolean hasFlags = !flags.isEmpty();

		// with
		if (hasFlags) {
			boolean handled = false;
			boolean recursive = false;
			for (QueryFlag flag : flags) {
				if (flag.getPosition() == Position.WITH) {
					if (flag.getFlag() == SQLTemplates.RECURSIVE) {
						recursive = true;
						continue;
					}
					if (handled) {
						append(", ");
					}
					handle(flag.getFlag());
					handled = true;
				}
			}
			if (handled) {
				if (recursive) {
					prepend(templates.getWithRecursive());
				} else {
					prepend(templates.getWith());
				}
				append("\n");
			}
		}

		// union
		Stage oldStage = stage;
		handle(union);

		// group by
		if (!groupBy.isEmpty()) {
			stage = Stage.GROUP_BY;
			if (hasFlags) {
				serialize(Position.BEFORE_GROUP_BY, flags);
			}
			append(templates.getGroupBy()).handle(COMMA, groupBy);
			if (hasFlags) {
				serialize(Position.AFTER_GROUP_BY, flags);
			}
		}

		// having
		if (having != null) {
			stage = Stage.HAVING;
			if (hasFlags) {
				serialize(Position.BEFORE_HAVING, flags);
			}
			append(templates.getHaving()).handle(having);
			if (hasFlags) {
				serialize(Position.AFTER_HAVING, flags);
			}
		}

		// order by
		if (hasFlags) {
			serialize(Position.BEFORE_ORDER, flags);
		}
		if (!orderBy.isEmpty()) {
			stage = Stage.ORDER_BY;
			append(templates.getOrderBy());
			boolean first = true;
			for (OrderSpecifier<?> os : orderBy) {
				if (!first) {
					append(COMMA);
				}
				handle(os.getTarget());
				append(os.getOrder() == Order.ASC ? templates.getAsc() : templates.getDesc());
				first = false;
			}
			if (hasFlags) {
				serialize(Position.AFTER_ORDER, flags);
			}
		}

		// end
		if (hasFlags) {
			serialize(Position.END, flags);
		}

		// reset stage
		stage = oldStage;
	}

	@Override
	public void visitConstant(Object constant) {
		Object newConstant = constant;
		if (newConstant instanceof Enum<?>) {
			newConstant = entityCacheManager.convertEnumToValue((Enum<?>) newConstant);
		}

		if (useLiterals) {
			if (newConstant instanceof Collection) {
				append("(");
				boolean first = true;
				for (Object o : ((Collection) newConstant)) {
					if (!first) {
						append(COMMA);
					}
					append(LiteralUtils.asLiteral(o));
					first = false;
				}
				append(")");
			} else {
				append(LiteralUtils.asLiteral(newConstant));
			}
		} else if (newConstant instanceof Collection) {
			append("(");
			boolean first = true;
			for (Object o : ((Collection) newConstant)) {
				if (!first) {
					append(COMMA);
				}
				append("?");
				constants.add(o);
				if (first && (constantPaths.size() < constants.size())) {
					constantPaths.add(null);
				}
				first = false;
			}
			append(")");

			int size = ((Collection) newConstant).size() - 1;
			Path<?> lastPath = constantPaths.get(constantPaths.size() - 1);
			for (int i = 0; i < size; i++) {
				constantPaths.add(lastPath);
			}
		} else {
			if (stage == Stage.SELECT && !Null.class.isInstance(newConstant) && getTemplates().isWrapSelectParameters()) {
				String typeName = templates.getTypeForCast(newConstant.getClass());
				Expression type = Expressions.constant(typeName);
				super.visitOperation(newConstant.getClass(), SQLOps.CAST, ImmutableList.<Expression<?>> of(Q, type));
			} else {
				append("?");
			}
			constants.add(newConstant);
			if (constantPaths.size() < constants.size()) {
				constantPaths.add(null);
			}
		}
	}

	@Override
	public Void visit(ParamExpression<?> param, Void context) {
		if (analyser.isNamedParameter()) {
			append(":").append(param.getName());
		} else {
			append("?");
		}
		constants.add(param);
		if (constantPaths.size() < constants.size()) {
			constantPaths.add(null);
		}
		return null;
	}

	@Override
	public Void visit(Path<?> path, Void context) {
		if (path instanceof DiscriminatorColumnPath) {
			EntityCache sourceEntityCache = entityCacheManager.getEntityCache(((DiscriminatorColumnPath) path).getDiscriminatorClass());
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorColumnPath) path).getDiscriminatorClass()
						+ " não foi encontrada na lista de entidades gerenciadas.");

			if (sourceEntityCache.getDiscriminatorColumn() == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorColumnPath) path).getDiscriminatorClass()
						+ " não possuí um discriminator column.");
			append(sourceEntityCache.getDiscriminatorColumn().getColumnName());
		} else if (path instanceof DiscriminatorColumnPath) {
			EntityCache sourceEntityCache = entityCacheManager.getEntityCache(((DiscriminatorColumnPath) path).getDiscriminatorClass());
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorColumnPath) path).getDiscriminatorClass()
						+ " não foi encontrada na lista de entidades gerenciadas.");

			if (StringUtils.isEmpty(sourceEntityCache.getDiscriminatorValue()))
				throw new SQLSerializerException("A classe " + ((DiscriminatorColumnPath) path).getDiscriminatorClass()
						+ " não possuí um discriminator value.");
			append(sourceEntityCache.getDiscriminatorValue());
		} else if ((path.getMetadata().getPathType() == PathType.VARIABLE) && (path instanceof EntityPath<?>)) {
			if (inOperation) {
				EntityCache sourceEntityCache = entityCacheManager.getEntityCache(analyser.getClassByEntityPath((EntityPath<?>) path));
				String alias = path.getMetadata().getName();
				for (DescriptionField descriptionField : sourceEntityCache.getPrimaryKeyFields()) {
					if (descriptionField.isSimple())
						append(templates.quoteIdentifier(alias)).append(".").append(
								templates.quoteIdentifier(descriptionField.getSimpleColumn().getColumnName()));
					else if (descriptionField.isAnyCollection()) {
						throw new SQLSerializerException("O campo " + path.getMetadata().getName() + " " + sourceEntityCache.getEntityClass()
								+ " não pode ser usado para criação da consulta pois é uma coleção. Use uma junção para isto. ");
					} else if (descriptionField.isRelationShip()) {
						boolean appendSep = false;
						for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
							if (appendSep) {
								append(" AND ");
							}
							append(alias).append(".").append(column.getColumnName());
							appendSep = true;
						}
					}
				}
			} else {
				if ((stage == Stage.SELECT) || (stage == Stage.GROUP_BY)) {
					appendAllColumnsForPath(this.getCurrentIndex());
				} else {
					append(templates.quoteIdentifier(path.getMetadata().getName()));
				}
			}
		} else if (path.getMetadata().getPathType() == PathType.PROPERTY) {
			EntityPath<?> entityPath = analyser.getAliasByEntityPath(path);
			String alias = entityPath.getMetadata().getName();

			EntityCache sourceEntityCache = entityCacheManager.getEntityCache(analyser.getClassByEntityPath(entityPath));
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + analyser.getClassByEntityPath(entityPath)
						+ " não foi encontrada na lista de entidades gerenciadas.");
			if (path instanceof EntityPath<?>) {
				if ((stage == Stage.SELECT) || (stage == Stage.GROUP_BY))
					appendAllColumnsForPath(this.getCurrentIndex());
				else
					appendAllDescriptionFields(path, alias, sourceEntityCache);
			} else {
				DescriptionField descriptionField = sourceEntityCache.getDescriptionField(path.getMetadata().getName());
				if (descriptionField == null)
					throw new SQLSerializerException("O campo " + path.getMetadata().getName() + " não foi encontrado na classe "
							+ analyser.getClassByEntityPath(entityPath) + ". ");
				if ((stage == Stage.SELECT) && (!inOperation))
					appendAllColumnsForPath(this.getCurrentIndex());
				else
					appendDescriptionField(path, entityPath, alias, descriptionField);
			}
		} else {
			append(path.getMetadata().getName());
		}

		return null;
	}

	protected void appendAllDescriptionFields(Path<?> path, String alias, EntityCache sourceEntityCache) {
		boolean appendSep = false;
		for (DescriptionField descriptionField : sourceEntityCache.getDescriptionFields()) {
			if (descriptionField.isAnyCollection())
				continue;
			if (appendSep)
				append(COMMA);
			if (appendDescriptionField(path, (EntityPath<?>) path, alias, descriptionField))
				appendSep = true;
		}
	}

	protected boolean appendDescriptionField(Path<?> path, EntityPath<?> entityPath, String alias, DescriptionField descriptionField) {
		if (descriptionField.isSimple()) {
			append(templates.quoteIdentifier(alias)).append(".")
					.append(templates.quoteIdentifier(descriptionField.getSimpleColumn().getColumnName()));
			return true;
		} else if (descriptionField.isRelationShip()) {
			boolean appendSep = false;
			for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
				if (appendSep) {
					if (stage == Stage.SELECT) {
						append(COMMA);
					} else if (stage == Stage.FROM) {
						append(" AND ");
					}
				}
				append(alias).append(".").append(column.getColumnName());
				appendSep = true;
			}
			return true;
		}
		return false;
	}

	protected void appendAllColumnsForPath(Integer index) {
		boolean appendSep = false;
		if (analyser.getColumns().containsKey(index)) {
			Set<SQLAnalyserColumn> columns = analyser.getColumns().get(index);
			for (SQLAnalyserColumn column : columns) {
				if (appendSep)
					append(COMMA);

				append(templates.quoteIdentifier(column.getAliasTableName())).append(".").append(templates.quoteIdentifier(column.getColumnName()));
				if ((stage == Stage.SELECT) && (!StringUtils.isEmpty(column.getAliasColumnName()) && !column.equals(column.getAliasColumnName()))
						&& (!column.isUserAliasDefined()))
					append(" AS ").append(column.getAliasColumnName());
				appendSep = true;
			}
		}
	}

	@Override
	public Void visit(SubQueryExpression<?> query, Void context) {
		if (inUnion && !templates.isUnionsWrapped()) {
			serialize(query.getMetadata(), false);
		} else {
			append("(");
			serialize(query.getMetadata(), false);
			append(")");
		}
		return null;
	}

	@Override
	public Void visit(TemplateExpression<?> expr, Void context) {
		if (inJoin && templates.isFunctionJoinsWrapped()) {
			append("table(");
			super.visit(expr, context);
			append(")");
		} else {
			super.visit(expr, context);
		}
		return null;
	}

	@Override
	protected void visitOperation(Class<?> type, Operator<?> operator, List<? extends Expression<?>> args) {
		try {
			System.out.println("=============maiko===============");
			System.out.println("tipo = " + type + "  Operator = " + operator + "  expression = " + args);
			System.out.println("=============maiko==============");
			inOperation = true;
			if (args.size() == 2 && !useLiterals && args.get(0) instanceof Path<?> && args.get(1) instanceof Constant<?> && operator != Ops.NUMCAST) {
				for (Element element : templates.getTemplate(operator).getElements()) {
					if (element instanceof Template.ByIndex && ((Template.ByIndex) element).getIndex() == 1) {
						constantPaths.add((Path<?>) args.get(0));
						break;
					}
				}
			}

			if (operator == SQLOps.UNION || operator == SQLOps.UNION_ALL) {
				boolean oldUnion = inUnion;
				inUnion = true;
				super.visitOperation(type, operator, args);
				inUnion = oldUnion;

			} else if (operator == Ops.LIKE && args.get(1) instanceof Constant) {
				final String escape = String.valueOf(templates.getEscapeChar());
				final String escaped = args.get(1).toString().replace(escape, escape + escape);
				super.visitOperation(String.class, Ops.LIKE, ImmutableList.of(args.get(0), ConstantImpl.create(escaped)));

			} else if (operator == Ops.STRING_CAST) {
				final String typeName = templates.getTypeForCast(String.class);
				super.visitOperation(String.class, SQLOps.CAST, ImmutableList.of(args.get(0), ConstantImpl.create(typeName)));

			} else if (operator == Ops.NUMCAST) {
				final Class<?> targetType = (Class<?>) ((Constant<?>) args.get(1)).getConstant();
				final String typeName = templates.getTypeForCast(targetType);
				super.visitOperation(targetType, SQLOps.CAST, ImmutableList.of(args.get(0), ConstantImpl.create(typeName)));

			} else if (operator == Ops.ALIAS) {
				if (stage == Stage.SELECT || stage == Stage.FROM) {
					super.visitOperation(type, operator, args);
				} else {
					// handle only target
					handle(args.get(1));
				}

			} else if (operator == SQLOps.WITH_COLUMNS) {
				boolean oldSkipParent = skipParent;
				skipParent = true;
				super.visitOperation(type, operator, args);
				skipParent = oldSkipParent;

			} else {
				skipParent = true;
				super.visitOperation(type, operator, args);
				skipParent = false;
			}
		} finally {
			inOperation = false;
		}
	}

	public void setUseLiterals(boolean useLiterals) {
		this.useLiterals = useLiterals;
	}

	protected void setSkipParent(boolean b) {
		skipParent = b;
	}

	protected EntityCache getEntityCacheByPath(EntityPath<?> path) {
		Type mySuperclass = path.getClass().getGenericSuperclass();
		Class<?> tType = (Class<?>) ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
		return entityCacheManager.getEntityCache(tType);
	}

}
