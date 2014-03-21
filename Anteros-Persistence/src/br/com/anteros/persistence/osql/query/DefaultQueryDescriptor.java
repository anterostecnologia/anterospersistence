package br.com.anteros.persistence.osql.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.osql.BooleanBuilder;
import br.com.anteros.persistence.osql.ConditionUtils;
import br.com.anteros.persistence.osql.JoinInjectPart;
import br.com.anteros.persistence.osql.JoinType;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.ParametersVisitor;
import br.com.anteros.persistence.osql.QueryModifiers;
import br.com.anteros.persistence.osql.ValidatingVisitor;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.JoinCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.Predicate;
import static br.com.anteros.persistence.util.CollectionUtils.*;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


@SuppressWarnings("serial")
public class DefaultQueryDescriptor implements QueryDescriptor, Cloneable {

    private boolean distinct;

    private Set<Condition<?>> conditionsInJoins = ImmutableSet.of();

    private List<Condition<?>> groupBy = ImmutableList.of();

    private Predicate having;

    private List<JoinCondition> joins = ImmutableList.of();

    private Condition<?> joinTarget;

    private JoinType joinType;

    private Predicate joinCondition;

    private Set<JoinInjectPart> joinParts = ImmutableSet.of();

    private List<OrderBy<?>> orderBy = ImmutableList.of();

    private List<Condition<?>> projection = ImmutableList.of();

    private Map<ParameterCondition<?>,Object> params = ImmutableMap.<ParameterCondition<?>, Object>of();

    private boolean unique;

    private Predicate where;

    private Set<QueryInjectPart> parts = ImmutableSet.of();

    private boolean extractParameters = true;

    private boolean validate = true;
    
    private QueryModifiers modifiers = QueryModifiers.EMPTY;
    
    private ValidatingVisitor validatingVisitor = ValidatingVisitor.DEFAULT;

    private static Predicate and(Predicate lhs, Predicate rhs) {
        if (lhs == null) {
            return rhs;
        } else {
            return ConditionUtils.and(lhs, rhs);
        }
    }

    public DefaultQueryDescriptor() {}

    public DefaultQueryDescriptor noValidate() {
        validate = false;
        return this;
    }


    @Override
    public void addPart(QueryInjectPart part) {
        parts = addSorted(parts, part);
    }

    @Override
    public void addJoinInjectPart(JoinInjectPart part) {
        joinParts = addSorted(joinParts, part);
    }

    @Override
    public void addGroupBy(Condition<?> o) {
        addLastJoin();
        validate(o);
        groupBy = add(groupBy, o);
    }

    @Override
    public void addHaving(Predicate e) {
        addLastJoin();
        if (e == null) {
            return;
        }
        e = (Predicate)ConditionUtils.extract(e);
        if (e != null) {
            validate(e);
            having = and(having, e);
        }
    }

    private void addLastJoin() {
        if (joinTarget == null) {
            return;
        }
        joins = add(joins, new JoinCondition(joinType, joinTarget, joinCondition, joinParts));

        joinType = null;
        joinTarget = null;
        joinCondition = null;
        joinParts = ImmutableSet.of();
    }

    @Override
    public void addJoin(JoinType joinType, Condition<?> condition) {
        addLastJoin();
        if (!conditionsInJoins.contains(condition)) {
            if (condition instanceof Attribute && ((Attribute<?>)condition).getDescriptor().isRoot()) {
                conditionsInJoins = add(conditionsInJoins, condition);
            } else {
                validate(condition);
            }
            this.joinType = joinType;
            this.joinTarget = condition;
        } else if (validate) {
            throw new IllegalStateException(condition + " is already used");
        }
    }

    @Override
    public void addJoinCondition(Predicate o) {
        validate(o);
        joinCondition = and(joinCondition, o);
    }

    @Override
    public void addOrderBy(OrderBy<?> o) {
        addLastJoin();
        orderBy = add(orderBy, o);
    }

    @Override
    public void addProjection(Condition<?> o) {
        addLastJoin();
        validate(o);
        projection = add(projection, o);
    }

    @Override
    public void addWhere(Predicate e) {
        if (e == null) {
            return;
        }
        addLastJoin();
        e = (Predicate)ConditionUtils.extract(e);
        if (e != null) {
            validate(e);
            where = and(where, e);
        }
    }

    @Override
    public void clearOrderBy() {
        orderBy = ImmutableList.of();
    }

    @Override
    public void clearProjection() {
        projection = ImmutableList.of();
    }

    @Override
    public void clearWhere() {
        where = new BooleanBuilder();
    }

    @Override
    public QueryDescriptor clone() {
        try {
            DefaultQueryDescriptor clone = (DefaultQueryDescriptor) super.clone();
            clone.conditionsInJoins = copyOf(conditionsInJoins);
            clone.groupBy = copyOf(groupBy);
            clone.having = having;
            clone.joins = copyOf(joins);
            clone.modifiers = modifiers;
            clone.orderBy = copyOf(orderBy);
            clone.projection = copyOf(projection);
            clone.params = copyOf(params);
            clone.where = where;
            clone.parts = copyOfSorted(parts);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new QueryException(e);
        }
    }

    @Override
    public List<Condition<?>> getGroupBy() {
        return groupBy;
    }

    @Override
    public Predicate getHaving() {
        return having;
    }

    @Override
    public List<JoinCondition> getJoins() {
        addLastJoin();
        return joins;
    }

    @Override
    public Map<ParameterCondition<?>,Object> getParameters() {
        return params;
    }

    @Override
    public List<OrderBy<?>> getOrderBy() {
        return orderBy;
    }

    @Override
    public List<Condition<?>> getProjection() {
        return projection;
    }

    @Override
    public Predicate getWhere() {
        return where;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public void reset() {
        clearProjection();
        params = ImmutableMap.of();
        modifiers = QueryModifiers.EMPTY;
    }

    @Override
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public void setLimit(Long limit) {
        if (modifiers == null || modifiers.getOffset() == null) {
            modifiers = QueryModifiers.limit(limit);
        } else {
            modifiers = new QueryModifiers(limit, modifiers.getOffset());
        }
    }

    @Override
    public void setModifiers(QueryModifiers restriction) {
        this.modifiers = restriction;
    }

    @Override
    public void setOffset(Long offset) {
        if (modifiers == null || modifiers.getLimit() == null) {
            modifiers = QueryModifiers.offset(offset);
        } else {
            modifiers = new QueryModifiers(modifiers.getLimit(), offset);
        }
    }

    @Override
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public <T> void setParameter(ParameterCondition<T> param, T value) {
        params = put(params, param, value);
    }

    @Override
    public Set<QueryInjectPart> getParts() {
        return parts;
    }

    @Override
    public boolean hasPart(QueryInjectPart part) {
        return parts.contains(part);
    }

    @Override
    public void removePart(QueryInjectPart part) {
        parts = removeSorted(parts, part);
    }

    private void validate(Condition<?> expr) {
        if (extractParameters) {
            expr.accept(ParametersVisitor.DEFAULT, this);
        }
        if (validate) {
            conditionsInJoins = expr.accept(validatingVisitor, conditionsInJoins);
        }
    }

    @Override
    public void setValidate(boolean v) {
        this.validate = v;
    }

    public void setValidatingVisitor(ValidatingVisitor visitor) {
        this.validatingVisitor = visitor;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof QueryDescriptor) {
        	QueryDescriptor q = (QueryDescriptor)o;
            return q.getParts().equals(parts)
                && q.getGroupBy().equals(groupBy)
                && Objects.equal(q.getHaving(), having)
                && q.isDistinct() == distinct
                && q.isUnique() == unique
                && q.getJoins().equals(joins)
                && Objects.equal(q.getModifiers(), modifiers)
                && q.getOrderBy().equals(orderBy)
                && q.getParameters().equals(params)
                && q.getProjection().equals(projection)
                && Objects.equal(q.getWhere(), where);

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parts, groupBy, having, joins, modifiers,
                orderBy, params, projection, unique, where);
    }

	@Override
	public QueryModifiers getModifiers() {
		return modifiers;
	}


}
