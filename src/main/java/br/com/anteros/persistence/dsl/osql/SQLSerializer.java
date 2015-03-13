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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.support.Expressions;
import br.com.anteros.persistence.dsl.osql.support.SerializerBase;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.ConstantImpl;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Operation;
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
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorColumnPath;
import br.com.anteros.persistence.dsl.osql.types.path.DiscriminatorValuePath;
import br.com.anteros.persistence.dsl.osql.util.LiteralUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

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

	private Operation<?> lastOperation = null;

	private boolean useLiterals = false;

	private EntityCacheManager entityCacheManager;

	private SQLAnalyser analyser;

	private DatabaseDialect dialect;

	public SQLSerializer(DatabaseDialect dialect, EntityCacheManager entityCacheManager, SQLTemplates templates) {
		super(templates);
		this.templates = templates;
		this.entityCacheManager = entityCacheManager;
		this.dialect = dialect;
	}

	public SQLSerializer(DatabaseDialect dialect, EntityCacheManager entityCacheManager, SQLTemplates templates, SQLAnalyser analyser) {
		super(templates);
		this.templates = templates;
		this.entityCacheManager = entityCacheManager;
		this.analyser = analyser;
		this.dialect = dialect;
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

				EntityCache entityCache = entityCacheManager.getEntityCache(((EntityPath<?>) je.getTarget()).getType());
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
		/*
		 * Se for um caminho para um discriminator colum gera o nome da coluna.
		 */
		if (path instanceof DiscriminatorColumnPath) {
			EntityCache sourceEntityCache = entityCacheManager.getEntityCache(((DiscriminatorColumnPath) path).getDiscriminatorClass());
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorColumnPath) path).getDiscriminatorClass()
						+ " não foi encontrada na lista de entidades gerenciadas.");

			if (sourceEntityCache.getDiscriminatorColumn() == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorColumnPath) path).getDiscriminatorClass()
						+ " não possuí um discriminator column.");
			append(sourceEntityCache.getDiscriminatorColumn().getColumnName());
		} else if (path instanceof DiscriminatorValuePath) {
			/*
			 * Se for um caminho para o valor do discriminator gera o valor
			 */
			EntityCache sourceEntityCache = entityCacheManager.getEntityCache(((DiscriminatorValuePath) path).getDiscriminatorClass());
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorValuePath) path).getDiscriminatorClass()
						+ " não foi encontrada na lista de entidades gerenciadas.");

			if (StringUtils.isEmpty(sourceEntityCache.getDiscriminatorValue()))
				throw new SQLSerializerException("A classe " + ((DiscriminatorValuePath) path).getDiscriminatorClass() + " não possuí um discriminator value.");
			append("'").append(sourceEntityCache.getDiscriminatorValue()).append("'");
		} else if (((path.getMetadata().getPathType() == PathType.VARIABLE) && ((path instanceof EntityPath<?>) || (path.getMetadata().isRoot())))
				|| (path.getMetadata().getPathType() == PathType.PROPERTY)) {
			if ((inOperation && (lastOperation.getOperator() == Ops.INSTANCE_OF))) {
				append(templates.quoteIdentifier(path.getMetadata().getName()));
			} else if (inOperation) {
				Set<SQLAnalyserColumn> columns = analyser.getParsedPathsOnOperations().get(path);
				if ((columns == null) || (columns.size() == 0)) {
					append(path.getMetadata().getName());
				} else {
					if (columns.size() > 1) {
						throw new SQLSerializerException("Não é permitido o uso de chave composta em algumas operações.");
					}
					SQLAnalyserColumn column = columns.iterator().next();
					append(column.getAliasTableName()).append(".").append(column.getColumnName());
				}
			} else {
				if ((stage == Stage.SELECT) || (stage == Stage.GROUP_BY) || (stage == Stage.HAVING) || (stage == Stage.ORDER_BY)) {
					Set<SQLAnalyserColumn> columns = analyser.getParsedPathsOnProjections().get(path);
					if ((columns == null) || (columns.size() == 0)) {
						append(path.getMetadata().getName());
					} else {
						boolean appendSep = false;
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
				} else
					append(templates.quoteIdentifier(path.getMetadata().getName()));
			}

		} else if (path.getMetadata().getPathType() == PathType.VARIABLE) {
		} else
			append(templates.quoteIdentifier(path.getMetadata().getName()));
		return null;
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
	public Void visit(Operation<?> expr, Void context) {

		lastOperation = expr;
		String booleanAsString = analyser.getBooleanDefinitions().get(expr);
		if (booleanAsString != null) {
			append(booleanAsString);
			return null;
		} else
			return super.visit(expr, context);
	}

	@Override
	protected void visitOperation(Class<?> type, Operator<?> operator, List<? extends Expression<?>> args) {
		try {
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
				final String typeName = dialect.convertJavaToDatabaseType(String.class).getName();
				super.visitOperation(String.class, SQLOps.CAST, ImmutableList.of(args.get(0), ConstantImpl.create(typeName)));

			} else if (operator == Ops.NUMCAST) {
				final Class<?> targetType = (Class<?>) ((Constant<?>) args.get(1)).getConstant();
				final String typeName = dialect.convertJavaToDatabaseType(targetType).getName();
				super.visitOperation(targetType, SQLOps.CAST, ImmutableList.of(args.get(0), ConstantImpl.create(typeName)));

			} else if (operator == Ops.ALIAS) {
				if (stage == Stage.SELECT || stage == Stage.FROM) {
					super.visitOperation(type, operator, args);
				} else {
					handle(args.get(0));
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

}
