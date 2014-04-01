package br.com.anteros.persistence.osql.query;

import br.com.anteros.persistence.osql.ConditionUtils;
import br.com.anteros.persistence.osql.JoinInjectPart;
import br.com.anteros.persistence.osql.JoinType;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.QueryModifiers;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.CollectionCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.MapCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.Predicate;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;



public class QueryMixin<T> {

    private final QueryDescriptor descriptor;

    private T self;

    public QueryMixin() {
        this.descriptor = new DefaultQueryDescriptor();
    }

    public QueryMixin(QueryDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public QueryMixin(T self) {
        this(self, new DefaultQueryDescriptor());
    }

    public QueryMixin(T self, QueryDescriptor descriptor) {
        this.self = self;
        this.descriptor = descriptor;
    }

    public QueryMixin(T self, QueryDescriptor descriptor, boolean validateAnyPaths) {
        this.self = self;
        this.descriptor = descriptor;
    }

    public T addJoin(JoinType joinType, Condition<?> target) {
        descriptor.addJoin(joinType, target);
        return self;
    }

    public T addPart(QueryInjectPart queryPart) {
        descriptor.addPart(queryPart);
        return self;
    }

    public T addJoinPart(JoinInjectPart joinPart) {
        descriptor.addJoinInjectPart(joinPart);
        return self;
    }

    public T removePart(QueryInjectPart queryPart) {
        descriptor.removePart(queryPart);
        return self;
    }

    public <E> Condition<E> addProjection(Condition<E> e) {
        descriptor.addProjection(e);
        return e;
    }

    public T addProjection(Condition<?>... o) {
        for (Condition<?> e : o) {
            descriptor.addProjection(e);
        }
        return self;
    }

    private <P extends Attribute<?>> P assertRoot(P p) {
        if (!p.getRoot().equals(p)) {
            throw new IllegalArgumentException(p + " is not a root attribute");
        }
        return p;
    }


    protected <D> Condition<D> createAlias(Condition<?> expr, Attribute<?> alias) {
        assertRoot(alias);
        return ConditionUtils.as((Condition)expr, alias);
    }

    public final T distinct() {
        descriptor.setDistinct(true);
        return self;
    }

    public final T from(Condition<?> arg) {
        descriptor.addJoin(JoinType.DEFAULT, arg);
        return self;
    }

    public final T from(Condition<?>... arguments) {
        for (Condition<?> arg : arguments) {
            descriptor.addJoin(JoinType.DEFAULT, arg);
        }
        return self;
    }

    public final T fullJoin(Condition<?> target) {
        descriptor.addJoin(JoinType.FULLJOIN, target);
        return self;
    }

    public final <P> T fullJoin(Condition<P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.FULLJOIN, createAlias(target, alias));
        return self;
    }
    public final <P> T fullJoin(CollectionCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.FULLJOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T fullJoin(MapCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.FULLJOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T fullJoin(SubQueryCondition<P> target, Attribute<?> alias) {
        descriptor.addJoin(JoinType.FULLJOIN, createAlias(target, alias));
        return self;
    }

    public final QueryDescriptor getDescriptor() {
        return descriptor;
    }

    public final T getSelf() {
        return self;
    }

    public final T groupBy(Condition<?> e) {
        descriptor.addGroupBy(e);
        return self;
    }

    public final T groupBy(Condition<?>... o) {
        for (Condition<?> e : o) {
            descriptor.addGroupBy(e);
        }
        return self;
    }

    public final T having(Predicate e) {
        descriptor.addHaving(normalize(e, false));
        return self;
    }

    public final T having(Predicate... o) {
        for (Predicate e : o) {
            descriptor.addHaving(normalize(e, false));
        }
        return self;
    }

    public final <P> T innerJoin(Condition<P> target) {
        descriptor.addJoin(JoinType.INNERJOIN, target);
        return self;
    }

    public final <P> T innerJoin(Condition<P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.INNERJOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T innerJoin(CollectionCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.INNERJOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T innerJoin(MapCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.INNERJOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T innerJoin(SubQueryCondition<P> target, Attribute<?> alias) {
        descriptor.addJoin(JoinType.INNERJOIN, createAlias(target, alias));
        return self;
    }

    public final boolean isDistinct() {
        return descriptor.isDistinct();
    }

    public final boolean isUnique() {
        return descriptor.isUnique();
    }

    public final <P> T join(Condition<P> target) {
        descriptor.addJoin(JoinType.JOIN, target);
        return self;
    }

    public final <P> T join(Condition<P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.JOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T join(CollectionCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.JOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T join(MapCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.JOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T join(SubQueryCondition<P> target, Attribute<?> alias) {
        descriptor.addJoin(JoinType.JOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T leftJoin(Condition<P> target) {
        descriptor.addJoin(JoinType.LEFTJOIN, target);
        return self;
    }

    public final <P> T leftJoin(Condition<P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.LEFTJOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T leftJoin(CollectionCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.LEFTJOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T leftJoin(MapCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.LEFTJOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T leftJoin(SubQueryCondition<P> target, Attribute<?> alias) {
        descriptor.addJoin(JoinType.LEFTJOIN, createAlias(target, alias));
        return self;
    }

    public final T limit(long limit) {
        descriptor.setLimit(limit);
        return self;
    }

    public final T offset(long offset) {
        descriptor.setOffset(offset);
        return self;
    }

    public final T on(Predicate condition) {
        descriptor.addJoinCondition(normalize(condition, false));
        return self;
    }

    public final T on(Predicate... conditions) {
        for (Predicate condition : conditions) {
            descriptor.addJoinCondition(normalize(condition, false));
        }
        return self;
    }

    public final T orderBy(OrderBy<?> spec) {
        Condition<?> e = spec.getTarget();
        if (!spec.getTarget().equals(e)) {
            descriptor.addOrderBy(new OrderBy(spec.getOrder(), e));
        } else {
            descriptor.addOrderBy(spec);
        }
        return self;
    }

    public final T orderBy(OrderBy<?>... o) {
        for (OrderBy<?> spec : o) {
            orderBy(spec);
        }
        return self;
    }

    public final T restrict(QueryModifiers modifiers) {
        descriptor.setModifiers(modifiers);
        return self;
    }

    public final <P> T rightJoin(Condition<P> target) {
        descriptor.addJoin(JoinType.RIGHTJOIN, target);
        return self;
    }

    public final <P> T rightJoin(Condition<P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.RIGHTJOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T rightJoin(CollectionCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.RIGHTJOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T rightJoin(MapCondition<?,P> target, Attribute<P> alias) {
        descriptor.addJoin(JoinType.RIGHTJOIN, createAlias(target, alias));
        return getSelf();
    }

    public final <P> T rightJoin(SubQueryCondition<P> target, Attribute<?> alias) {
        descriptor.addJoin(JoinType.RIGHTJOIN, createAlias(target, alias));
        return self;
    }

    public final <P> T set(ParameterCondition<P> parameter, P value) {
        descriptor.setParameter(parameter, value);
        return self;
    }

    public final void setDistinct(boolean distinct) {
        descriptor.setDistinct(distinct);
    }

    public final void setSelf(T self) {
        this.self = self;
    }

    public final void setUnique(boolean unique) {
        descriptor.setUnique(unique);
    }

    public final T where(Predicate e) {
        descriptor.addWhere(normalize(e, true));
        return self;
    }

    public final T where(Predicate... o) {
        for (Predicate e : o) {
            descriptor.addWhere(normalize(e, true));
        }
        return self;
    }

    protected Predicate normalize(Predicate condition, boolean where) {
        return condition;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof QueryMixin) {
        	QueryMixin q = (QueryMixin)o;
            return q.descriptor.equals(descriptor);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

}
