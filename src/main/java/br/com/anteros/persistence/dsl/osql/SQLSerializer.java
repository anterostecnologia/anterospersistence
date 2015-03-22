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
import br.com.anteros.persistence.metadata.EntityCache;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * SqlSerializer serializes Querydsl queries into SQL
 *
 * @author tiwe modified by : Edson Martins
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

	private boolean inUnion = false;

	private boolean inJoin = false;

	private boolean inOperation = false;

	private boolean inSubQuery = false;

	private Operation<?> lastOperation = null;

	private boolean useLiterals = false;

	private final SQLAnalyser analyser;

	private final Configuration configuration;

	private boolean inSerializeUnion;

	public SQLSerializer(Configuration configuration, SQLAnalyser analyser) {
		super(configuration.getTemplates());
		this.configuration = configuration;
		this.analyser = analyser;
	}

	public List<Object> getConstants() {
		return constants;
	}

	public List<Path<?>> getConstantPaths() {
		return constantPaths;
	}

	public void handle(String template, Object... args) {
		handleTemplate(TemplateFactory.DEFAULT.create(template), Arrays.asList(args));
	}

	private void handleJoinTarget(JoinExpression je) {
		if ((je.getTarget() instanceof EntityPath) && (configuration.getTemplates().isSupportsAlias())) {
			if (((EntityPath<?>) je.getTarget()).getMetadata().getParent() == null) {
				if (configuration.getTemplates().isPrintSchema()) {
					append(".");
				}

				EntityCache entityCache = configuration.getEntityCacheManager().getEntityCache(((EntityPath<?>) je.getTarget()).getType());
				append(entityCache.getTableName());
				append(configuration.getTemplates().getTableAlias());
			}
		}
		inJoin = true;
		handle(je.getTarget());
		inJoin = false;
	}

	public void serialize(QueryMetadata metadata, boolean forCountRow) {
		configuration.getTemplates().serialize(metadata, forCountRow, this);
	}

	public void serializeForQuery(QueryMetadata metadata, boolean forCountRow) {
		skipParent = true;
		final List<JoinExpression> joins = metadata.getJoins();
		final Predicate where = metadata.getWhere();
		final List<? extends Expression<?>> groupBy = metadata.getGroupBy();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();
		final Set<QueryFlag> flags = metadata.getFlags();
		final boolean hasFlags = !flags.isEmpty();
		String suffix = null;

		List<Expression<?>> sqlSelect = SQLAnalyser.extractIndividualColumnsExpression(metadata);

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
					prepend(configuration.getTemplates().getWithRecursive());
				} else {
					prepend(configuration.getTemplates().getWith());
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
			append(configuration.getTemplates().getSelect());
			if (hasFlags) {
				serialize(Position.AFTER_SELECT, flags);
			}

			if (!metadata.isDistinct()) {
				append(configuration.getTemplates().getCountStar());
			} else {
				List<? extends Expression<?>> columns;
				if (sqlSelect.isEmpty()) {
					throw new SQLSerializerException("Informe uma clausula para o select.");
				} else {
					columns = sqlSelect;
				}
				if (columns.size() == 1) {
					append(configuration.getTemplates().getDistinctCountStart());
					handle(columns.get(0));
					append(configuration.getTemplates().getDistinctCountEnd());
				} else if (configuration.getTemplates().isCountDistinctMultipleColumns()) {
					append(configuration.getTemplates().getDistinctCountStart());
					append("(").handle(COMMA, columns).append(")");
					append(configuration.getTemplates().getDistinctCountEnd());
				} else {
					// select count(*) from (select distinct ...)
					append(configuration.getTemplates().getCountStar());
					append(configuration.getTemplates().getFrom());
					append("(");
					append(configuration.getTemplates().getSelectDistinct());
					handle(COMMA, columns);
					suffix = ") internal";
				}
			}

		} else if (!sqlSelect.isEmpty()) {
			if (!metadata.isDistinct()) {
				append(configuration.getTemplates().getSelect());
			} else {
				append(configuration.getTemplates().getSelectDistinct());
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
			append(configuration.getTemplates().getWhere()).handle(where);
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
			append(configuration.getTemplates().getGroupBy()).handle(COMMA, groupBy);
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
			append(configuration.getTemplates().getHaving()).handle(having);
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
			append(configuration.getTemplates().getOrderBy());
			handleOrderBy(orderBy);
			if (hasFlags) {
				serialize(Position.AFTER_ORDER, flags);
			}
		}

		// modifiers
		if (!forCountRow && metadata.getModifiers().isRestricting() && !joins.isEmpty()) {
			stage = Stage.MODIFIERS;
			configuration.getTemplates().serializeModifiers(metadata, this);
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
			String order = os.getOrder() == Order.ASC ? configuration.getTemplates().getAsc() : configuration.getTemplates().getDesc();
			if (os.getNullHandling() == OrderSpecifier.NullHandling.NullsFirst) {
				if (configuration.getTemplates().getNullsFirst() != null) {
					handle(os.getTarget());
					append(order);
					append(configuration.getTemplates().getNullsFirst());
				} else {
					append("(case when ");
					handle(os.getTarget());
					append(" is null then 0 else 1 end), ");
					handle(os.getTarget());
					append(order);
				}
			} else if (os.getNullHandling() == OrderSpecifier.NullHandling.NullsLast) {
				if (configuration.getTemplates().getNullsLast() != null) {
					handle(os.getTarget());
					append(order);
					append(configuration.getTemplates().getNullsLast());
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
			String dummyTable = configuration.getTemplates().getDummyTable();
			if (!Strings.isNullOrEmpty(dummyTable)) {
				append(configuration.getTemplates().getFrom());
				append(dummyTable);
			}
		} else {
			append(configuration.getTemplates().getFrom());
			for (int i = 0; i < joins.size(); i++) {
				final JoinExpression je = joins.get(i);
				if (je.getFlags().isEmpty()) {
					if (i > 0) {
						append(configuration.getTemplates().getJoinSymbol(je.getType()));
					}
					handleJoinTarget(je);
					if (je.getCondition() != null) {
						append(configuration.getTemplates().getOn()).handle(je.getCondition());
					}
				} else {
					serialize(JoinFlag.Position.START, je.getFlags());
					if (!serialize(JoinFlag.Position.OVERRIDE, je.getFlags()) && i > 0) {
						append(configuration.getTemplates().getJoinSymbol(je.getType()));
					}
					serialize(JoinFlag.Position.BEFORE_TARGET, je.getFlags());
					handleJoinTarget(je);
					serialize(JoinFlag.Position.BEFORE_CONDITION, je.getFlags());
					if (je.getCondition() != null) {
						append(configuration.getTemplates().getOn()).handle(je.getCondition());
					}
					serialize(JoinFlag.Position.END, je.getFlags());
				}
			}
		}
	}

	public void serializeUnion(Expression<?> union, QueryMetadata metadata, boolean unionAll) {
		inSerializeUnion = true;
		try {
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
						prepend(configuration.getTemplates().getWithRecursive());
					} else {
						prepend(configuration.getTemplates().getWith());
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
				append(configuration.getTemplates().getGroupBy()).handle(COMMA, groupBy);
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
				append(configuration.getTemplates().getHaving()).handle(having);
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
				append(configuration.getTemplates().getOrderBy());
				boolean first = true;
				for (OrderSpecifier<?> os : orderBy) {
					if (!first) {
						append(COMMA);
					}
					handle(os.getTarget());
					append(os.getOrder() == Order.ASC ? configuration.getTemplates().getAsc() : configuration.getTemplates().getDesc());
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
		} finally {
			inSerializeUnion = false;
		}
	}

	@Override
	public void visitConstant(Object constant) {
		Object newConstant = constant;
		if (newConstant instanceof Enum<?>) {
			newConstant = configuration.convertEnumToValue((Enum<?>) newConstant);
		}

		if (useLiterals) {
			if (newConstant instanceof Collection) {
				append("(");
				boolean first = true;
				for (Object o : ((Collection) newConstant)) {
					if (!first) {
						append(COMMA);
					}
					append(configuration.asLiteral(o));
					first = false;
				}
				append(")");
			} else {
				append(configuration.asLiteral(newConstant));
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
			if (stage == Stage.SELECT && !Null.class.isInstance(newConstant) && configuration.getTemplates().isWrapSelectParameters()) {
				String typeName = configuration.getTemplates().getTypeForCast(newConstant.getClass());
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
			EntityCache sourceEntityCache = configuration.getEntityCacheManager().getEntityCache(((DiscriminatorColumnPath) path).getDiscriminatorClass());
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
			EntityCache sourceEntityCache = configuration.getEntityCacheManager().getEntityCache(((DiscriminatorValuePath) path).getDiscriminatorClass());
			if (sourceEntityCache == null)
				throw new SQLSerializerException("A classe " + ((DiscriminatorValuePath) path).getDiscriminatorClass()
						+ " não foi encontrada na lista de entidades gerenciadas.");

			if (StringUtils.isEmpty(sourceEntityCache.getDiscriminatorValue()))
				throw new SQLSerializerException("A classe " + ((DiscriminatorValuePath) path).getDiscriminatorClass() + " não possuí um discriminator value.");
			append("'").append(sourceEntityCache.getDiscriminatorValue()).append("'");
		} else if (((path.getMetadata().getPathType() == PathType.VARIABLE) && ((path instanceof EntityPath<?>) || (path.getMetadata().isRoot())))
				|| (path.getMetadata().getPathType() == PathType.PROPERTY)) {
			if (inOperation && (lastOperation != null)
					&& ((lastOperation.getOperator() == Ops.INSTANCE_OF) || ((stage == Stage.FROM) && (lastOperation.getOperator() == Ops.ALIAS)))) {
				append(configuration.getTemplates().quoteIdentifier(path.getMetadata().getName()));
			} else if (inOperation) {
				Set<SQLAnalyserColumn> columns = analyser.getParsedPathsOnOperations().get(path);
				if ((columns == null) || (columns.size() == 0)) {
					append(path.getMetadata().getName());
				} else {
					if (columns.size() > 1) {
						throw new SQLSerializerException("Não é permitido o uso de chave composta em algumas operações. Caminho " + path);
					}
					SQLAnalyserColumn column = columns.iterator().next();
					if ((inSerializeUnion) && (stage == Stage.ORDER_BY))
						append(configuration.getTemplates().quoteIdentifier(
								(column.isUserAliasDefined() ? column.getAliasColumnName() : column.getColumnName())));
					else
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
							if (inSerializeUnion) {
								append(configuration.getTemplates().quoteIdentifier(
										(column.isUserAliasDefined() ? column.getAliasColumnName() : column.getColumnName())));
							} else {
								append(configuration.getTemplates().quoteIdentifier(column.getAliasTableName())).append(".").append(
										configuration.getTemplates().quoteIdentifier(column.getColumnName()));
								if ((stage == Stage.SELECT)
										&& (!StringUtils.isEmpty(column.getAliasColumnName()) && !column.getColumnName().equals(column.getAliasColumnName()))
										&& (!column.isUserAliasDefined()) && (!inSubQuery))
									append(" AS ").append(column.getAliasColumnName());
							}
							appendSep = true;
						}
					}
				} else
					append(configuration.getTemplates().quoteIdentifier(path.getMetadata().getName()));
			}

		} else if (path.getMetadata().getPathType() == PathType.VARIABLE) {
		} else
			append(configuration.getTemplates().quoteIdentifier(path.getMetadata().getName()));
		return null;
	}

	@Override
	public Void visit(SubQueryExpression<?> query, Void context) {
		boolean oldInOperation = inOperation;
		boolean oldInSerializeUnion = inSerializeUnion;
		Operation<?> oldLastOperation = lastOperation;
		boolean oldInUnion = inUnion;
		this.inSubQuery = true;
		this.inOperation = false;
		this.inSerializeUnion = false;

		try {
			if (inUnion && !configuration.getTemplates().isUnionsWrapped()) {
				this.inUnion = false;
				serialize(query.getMetadata(), false);
			} else {
				append("(");
				serialize(query.getMetadata(), false);
				append(")");
			}
		} finally {
			this.inOperation = oldInOperation;
			this.inSubQuery = false;
			this.lastOperation = oldLastOperation;
			this.inUnion = oldInUnion;
			this.inSerializeUnion = oldInSerializeUnion;
		}
		return null;
	}

	@Override
	public Void visit(TemplateExpression<?> expr, Void context) {
		if (inJoin && configuration.getTemplates().isFunctionJoinsWrapped()) {
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
				for (Element element : configuration.getTemplates().getTemplate(operator).getElements()) {
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
				final String escape = String.valueOf(configuration.getTemplates().getEscapeChar());
				final String escaped = args.get(1).toString().replace(escape, escape + escape);
				super.visitOperation(String.class, Ops.LIKE, ImmutableList.of(args.get(0), ConstantImpl.create(escaped)));

			} else if (operator == Ops.STRING_CAST) {
				final String typeName = configuration.convertJavaToDatabaseType(String.class);
				super.visitOperation(String.class, SQLOps.CAST, ImmutableList.of(args.get(0), ConstantImpl.create(typeName)));

			} else if (operator == Ops.NUMCAST) {
				final Class<?> targetType = (Class<?>) ((Constant<?>) args.get(1)).getConstant();
				final String typeName = configuration.convertJavaToDatabaseType(targetType);
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

	public Configuration getConfiguration() {
		return configuration;
	}

}
