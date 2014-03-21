package br.com.anteros.persistence.osql.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.osql.JoinType;
import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.OrderByNullClauseType;
import br.com.anteros.persistence.osql.OrderByType;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.attribute.AttributeRelationType;
import br.com.anteros.persistence.osql.attribute.EntityAttribute;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.JoinCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.Predicate;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.util.MathUtils;

import com.google.common.collect.ImmutableSet;

public class OSQLSerializer extends AbstractSerializer<OSQLSerializer> {

    private static final Set<Operator<?>> NUMERIC = ImmutableSet.<Operator<?>>of(
            Operators.ADD, Operators.SUB, Operators.MULT, Operators.DIV,
            Operators.LT, Operators.LOE, Operators.GT, Operators.GOE, Operators.BETWEEN);

    private static final String COMMA = ", ";

    private static final String DELETE = "delete from ";

    private static final String FROM = "from ";

    private static final String GROUP_BY = "\ngroup by ";

    private static final String HAVING = "\nhaving ";

    private static final String ORDER_BY = "\norder by ";

    private static final String SELECT = "select ";

    private static final String SELECT_COUNT = "select count(";

    private static final String SELECT_COUNT_DISTINCT = "select count(distinct ";

    private static final String SELECT_DISTINCT = "select distinct ";

    private static final String SET = "\nset ";

    private static final String UPDATE = "update ";

    private static final String WHERE = "\nwhere ";

    private static final String WITH = " with ";

    private static final String ON = " on ";

    private static final Map<JoinType, String> joinTypes = new HashMap<JoinType, String>();

    private final OSQLCodeTemplates templates;

    private final SQLSession session;

    private boolean inProjection = false;

    static{
        joinTypes.put(JoinType.DEFAULT, COMMA);
        joinTypes.put(JoinType.FULLJOIN, "\n  full join ");
        joinTypes.put(JoinType.INNERJOIN, "\n  inner join ");
        joinTypes.put(JoinType.JOIN, "\n  inner join ");
        joinTypes.put(JoinType.LEFTJOIN, "\n  left join ");
        joinTypes.put(JoinType.RIGHTJOIN, "\n  right join ");
    }

    private boolean wrapElements = false;

    public OSQLSerializer(OSQLCodeTemplates templates) {
        this(templates, null);
    }

    public OSQLSerializer(OSQLCodeTemplates templates, SQLSession em) {
        super(templates);
        this.templates = templates;
        this.session = em;
    }

    private void handleJoinTarget(JoinCondition je) {
        if (je.getTarget() instanceof EntityAttribute<?>) {
            final EntityAttribute<?> pe = (EntityAttribute<?>) je.getTarget();
            if (pe.getDescriptor().isRoot()) {
                final Entity entityAnnotation = pe.getAnnotatedElement().getAnnotation(Entity.class);
                if (entityAnnotation != null && entityAnnotation.name().length() > 0) {
                    append(entityAnnotation.name());
                } else if (pe.getType().getPackage() != null) {
                    final String pn = pe.getType().getPackage().getName();
                    final String typeName = pe.getType().getName().substring(pn.length() + 1);
                    append(typeName);
                } else {
                    append(pe.getType().getName());
                }
                append(" ");
            }
        }
        handle(je.getTarget());
    }

    public void serialize(QueryDescriptor descriptor, boolean forCountRow, String projection) {
        final List<? extends Condition<?>> select = descriptor.getProjection();
        final List<JoinCondition> joins = descriptor.getJoins();
        final Predicate where = descriptor.getWhere();
        final List<? extends Condition<?>> groupBy = descriptor.getGroupBy();
        final Predicate having = descriptor.getHaving();
        final List<OrderBy<?>> orderBy = descriptor.getOrderBy();

        // select
        boolean inProjectionOrig = inProjection;
        inProjection = true;
        if (projection != null) {
            append(SELECT).append(projection).append("\n");

        } else if (forCountRow) {
            if (!descriptor.isDistinct()) {
                append(SELECT_COUNT);
            } else {
                append(SELECT_COUNT_DISTINCT);
            }
            if(!select.isEmpty()) {
                if (select.get(0) instanceof FactoryCondition) {
                    handle(joins.get(0).getTarget());
                } else {
                    // TODO : make sure this works
                    handle(COMMA, select);
                }
            } else {
                handle(joins.get(0).getTarget());
            }
            append(")\n");

        } else {
            if (!descriptor.isDistinct()) {
                append(SELECT);
            } else {
                append(SELECT_DISTINCT);
            }
            if (!select.isEmpty()) {
                handle(COMMA, select);
            } else {
                handle(descriptor.getJoins().get(0).getTarget());
            }
            append("\n");

        }
        inProjection = inProjectionOrig;

        // from
        append(FROM);
        serializeSources(forCountRow, joins);

        // where
        if (where != null) {
            append(WHERE).handle(where);
        }

        // group by
        if (!groupBy.isEmpty()) {
            append(GROUP_BY).handle(COMMA, groupBy);
        }

        // having
        if (having != null) {
            append(HAVING).handle(having);
        }

        // order by
        if (!orderBy.isEmpty() && !forCountRow) {
            append(ORDER_BY);
            boolean first = true;
            for (final OrderBy<?> os : orderBy) {
                if (!first) {
                    append(COMMA);
                }
                handle(os.getTarget());
                append(os.getOrder() == OrderByType.ASC ? " asc" : " desc");
                if (os.getNullClauseType() == OrderByNullClauseType.NULLS_FIRST) {
                    append(" nulls first");
                } else if (os.getNullClauseType() == OrderByNullClauseType.NULLS_LAST) {
                    append(" nulls last");
                }
                first = false;
            }
        }
    }

    public void serializeForDelete(QueryDescriptor md) {
        append(DELETE);
        handleJoinTarget(md.getJoins().get(0));
        if (md.getWhere() != null) {
            append(WHERE).handle(md.getWhere());
        }
    }

    public void serializeForUpdate(QueryDescriptor md) {
        append(UPDATE);
        handleJoinTarget(md.getJoins().get(0));
        append(SET);
        handle(COMMA, md.getProjection());
        if (md.getWhere() != null) {
            append(WHERE).handle(md.getWhere());
        }
    }

    private void serializeSources(boolean forCountRow, List<JoinCondition> joins) {
        for (int i = 0; i < joins.size(); i++) {
            final JoinCondition je = joins.get(i);
            if (i > 0) {
                append(joinTypes.get(je.getType()));
            }
            if (je.hasFlag(OSQLQueryMixin.FETCH) && !forCountRow) {
                handle(OSQLQueryMixin.FETCH);
            }
            handleJoinTarget(je);
            // XXX Hibernate specific flag
            if (je.hasFlag(OSQLQueryMixin.FETCH_ALL_PROPERTIES) && !forCountRow) {
                handle(OSQLQueryMixin.FETCH_ALL_PROPERTIES);
            }

            if (je.getCondition() != null) {
                append(templates.isWithForOn() ? WITH : ON);
                handle(je.getCondition());
            }
        }
    }

    @Override
    public void visitConstant(Object constant) {
        boolean wrap = templates.wrapConstant(constant);
        if (wrap) {
             append("(");
        }
        append("?");
        if (!getConstantToLabel().containsKey(constant)) {
            final String constLabel = String.valueOf(getConstantToLabel().size()+1);
            getConstantToLabel().put(constant, constLabel);
            append(constLabel);
        } else {
            append(getConstantToLabel().get(constant));
        }
        if (wrap) {
            append(")");
        }
    }

    @Override
    public Void visit(ParameterCondition<?> param, Void context) {
        append("?");
        if (!getConstantToLabel().containsKey(param)) {
            final String paramLabel = String.valueOf(getConstantToLabel().size()+1);
            getConstantToLabel().put(param, paramLabel);
            append(paramLabel);
        } else {
            append(getConstantToLabel().get(param));
        }
        return null;
    }

    @Override
    public Void visit(SubQueryCondition<?> query, Void context) {
        append("(");
        serialize(query.getDescriptor(), false, null);
        append(")");
        return null;
    }

    @Override
    public Void visit(Attribute<?> expr, Void context) {
        boolean wrap = wrapElements
        && (Collection.class.isAssignableFrom(expr.getType()) || Map.class.isAssignableFrom(expr.getType()))
        && expr.getDescriptor().getRelationType().equals(AttributeRelationType.PROPERTY);
        if (wrap) {
            append("elements(");
        }
        super.visit(expr, context);
        if (wrap) {
            append(")");
        }
        return null;
    }

    @Override
    public Void visit(FactoryCondition<?> expr, Void context) {
        if (!inProjection) {
            append("(");
            super.visit(expr, context);
            append(")");
        } else {
            super.visit(expr, context);
        }
        return null;
    }

    @Override
    protected void visitOperation(Class<?> type, Operator<?> operator, List<? extends Condition<?>> conditions) {
        boolean old = wrapElements;
        /* VER AQUI
         wrapElements = templates.wrapElements(operator);

         if (operator == Ops.EQ && conditions.get(1) instanceof Operation &&
                ((Operation)conditions.get(1)).getOperator() == Operators.ANY) {
            conditions = ImmutableList.<Condition<?>>of(conditions.get(0), ((Operation)conditions.get(1)).getArgument(0));
            visitOperation(type, Operators.IN, conditions);

        } else if (operator == Operators.NE && conditions.get(1) instanceof Operation &&
                ((Operation)conditions.get(1)).getOperator() == Operators.ANY) {
            conditions = ImmutableList.<Condition<?>>of(conditions.get(0), ((Operation)conditions.get(1)).getArgument(0));
            visitOperation(type, Operators.NOT_IN, conditions);

        } else if (operator == Operators.IN || operator == Operators.NOT_IN) {
            if (conditions.get(1) instanceof Attribute) {
                visitAnyInAttribute(type, operator, conditions);
            } else if (conditions.get(0) instanceof Attribute && conditions.get(1) instanceof ConstantCondition) {
                visitAttributeInCollection(type, operator, conditions);
            } else {
                super.visitOperation(type, operator, conditions);
            }

        } else if (operator == Operators.INSTANCE_OF) {
            visitInstanceOf(type, operator, conditions);

        } else if (operator == Operators.NUMCAST) {
            visitNumCast(conditions);

        } else if (operator == Operators.EXISTS && conditions.get(0) instanceof SubQueryCondition) {
            final SubQueryCondition subQuery = (SubQueryCondition) conditions.get(0);
            append("exists (");
            // VER AQUI serialize(subQuery.getDescriptor(), false, templates.getExistsProjection());
            append(")");

        } else if (operator == Operators.MATCHES || operator == Operators.MATCHES_IC) {
            super.visitOperation(type, Operators.LIKE,
                    ImmutableList.of(args.get(0), ConditionUtils.regexToLike((Condition<String>) args.get(1))));

        } else if (operator == Operators.LIKE && args.get(1) instanceof ConstantCondition) {
            final String escape = String.valueOf(templates.getEscapeChar());
            final String escaped = args.get(1).toString().replace(escape, escape + escape);
            super.visitOperation(String.class, Operators.LIKE,
                    ImmutableList.of(args.get(0), ConstantImpl.create(escaped)));

        } else if (NUMERIC.contains(operator)) {
            super.visitOperation(type, operator, normalizeNumericArgs(args));

        } else {
            super.visitOperation(type, operator, args);
        }

        wrapElements = old;*/
    }

    private void visitNumCast(List<? extends Condition<?>> arguments) {
        final Class<?> targetType = (Class<?>) ((ConstantCondition<?>) arguments.get(1)).getConstant();
        final String typeName = templates.getTypeForCast(targetType);
       // VER AQUI visitOperation(targetType, JPQLOps.CAST, ImmutableList.of(arguments.get(0), ConstantImpl.create(typeName)));
    }

    private void visitInstanceOf(Class<?> type, Operator<?> operator,
            List<? extends Condition<?>> arguments) {
        if (templates.isTypeAsString()) {
            final List<Condition<?>> newArgs = new ArrayList<Condition<?>>(arguments);
            final Class<?> cl = ((Class<?>) ((ConstantCondition<?>) newArgs.get(1)).getConstant());
            // use discriminator value instead of fqnm
            if (cl.getAnnotation(DiscriminatorValue.class) != null) {
                newArgs.set(1, ConstantImpl.create(cl.getAnnotation(DiscriminatorValue.class).value()));
            } else {
                newArgs.set(1, ConstantImpl.create(cl.getSimpleName()));
            }
            super.visitOperation(type, operator, newArgs);
        } else {
            super.visitOperation(type, operator, arguments);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void visitPathInCollection(Class<?> type, Operator<?> operator,
            List<? extends Condition<?>> args) {
        /*
    	// NOTE turns entityPath in collection into entityPath.id in (collection of ids)
        if (session != null && !templates.isPathInEntitiesSupported() && args.get(0).getType().isAnnotationPresent(Entity.class)) {
        	Attribute<?> lhs = (Attribute<?>) args.get(0);
            ConstantCondition<?> rhs = (ConstantCondition<?>) args.get(1);
            final Metamodel metamodel = session.getMetamodel();
            final PersistenceUnitUtil util = session.getEntityManagerFactory().getPersistenceUnitUtil();
            final EntityType<?> entityType = metamodel.entity(args.get(0).getType());
            if (entityType.hasSingleIdAttribute()) {
                SingularAttribute<?,?> id = getIdProperty(entityType);
                // turn lhs into id path
                lhs = new PathImpl(id.getJavaType(), lhs, id.getName());
                // turn rhs into id collection
                Set ids = new HashSet();
                for (Object entity : (Collection<?>)rhs.getConstant()) {
                    ids.add(util.getIdentifier(entity));
                }
                rhs = ConstantImpl.create(ids);
                args = ImmutableList.of(lhs, rhs);
            }
        }
        super.visitOperation(type, operator, args);*/
    }

   /* VER AQUI private SingularAttribute<?,?> getIdProperty(EntityType entity) {
        final Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
        for (final SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                return singularAttribute;
            }
        }
        return null;
    }*/

    private void visitAnyInAttribute(Class<?> type, Operator<?> operator, List<? extends Condition<?>> args) {
        /* VER AQUI if (!templates.isEnumInPathSupported() && args.get(0) instanceof ConstantCondition && Enum.class.isAssignableFrom(args.get(0).getType())) {
            final Enumerated enumerated = ((Path)args.get(1)).getAnnotatedElement().getAnnotation(Enumerated.class);
            final Enum constant = (Enum)((Constant)args.get(0)).getConstant();
            if (enumerated == null || enumerated.value() == EnumType.ORDINAL) {
                args = ImmutableList.of(ConstantImpl.create(constant.ordinal()), args.get(1));
            } else {
                args = ImmutableList.of(ConstantImpl.create(constant.name()), args.get(1));
            }
        }
        super.visitOperation(type,
                operator == Ops.IN ? JPQLOps.MEMBER_OF : JPQLOps.NOT_MEMBER_OF,
                args);*/
    }

    private List<? extends Condition<?>> normalizeNumericArgs(List<? extends Condition<?>> args) {
        boolean hasConstants = false;
        Class<? extends Number> numType = null;
        for (Condition<?> arg : args) {
            if (Number.class.isAssignableFrom(arg.getType())) {
                if (arg instanceof ConstantCondition) {
                    hasConstants = true;
                } else {
                    numType = (Class<? extends Number>) arg.getType();
                }
            }
        }
        if (hasConstants && numType != null) {
            final List<Condition<?>> newArgs = new ArrayList<Condition<?>>(args.size());
            for (final Condition<?> arg : args) {
                if (arg instanceof ConstantCondition && Number.class.isAssignableFrom(arg.getType())
                        && !arg.getType().equals(numType)) {
                    final Number number = (Number) ((ConstantCondition)arg).getConstant();
                    newArgs.add(ConstantImpl.create(MathUtils.cast(number, (Class)numType)));
                } else {
                    newArgs.add(arg);
                }
            }
            return newArgs;
        } else {
            return args;
        }
    }

}
