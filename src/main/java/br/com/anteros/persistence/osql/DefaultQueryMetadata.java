/*
 * Copyright 2011, Mysema Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.anteros.persistence.osql;

import static br.com.anteros.persistence.osql.util.CollectionUtils.add;
import static br.com.anteros.persistence.osql.util.CollectionUtils.addSorted;
import static br.com.anteros.persistence.osql.util.CollectionUtils.copyOf;
import static br.com.anteros.persistence.osql.util.CollectionUtils.copyOfSorted;
import static br.com.anteros.persistence.osql.util.CollectionUtils.put;
import static br.com.anteros.persistence.osql.util.CollectionUtils.removeSorted;

import java.util.List;
import java.util.Map;
import java.util.Set;



import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.ExpressionUtils;
import br.com.anteros.persistence.osql.types.OrderSpecifier;
import br.com.anteros.persistence.osql.types.ParamExpression;
import br.com.anteros.persistence.osql.types.ParamsVisitor;
import br.com.anteros.persistence.osql.types.Path;
import br.com.anteros.persistence.osql.types.Predicate;
import br.com.anteros.persistence.osql.types.ValidatingVisitor;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * DefaultQueryMetadata is the default implementation of the {@link QueryMetadata} interface
 *
 * @author tiwe
 */
public class DefaultQueryMetadata implements QueryMetadata, Cloneable {

    private static final long serialVersionUID = 317736313966701232L;

    private boolean distinct;

    private Set<Expression<?>> exprInJoins = ImmutableSet.of();

    private List<Expression<?>> groupBy = ImmutableList.of();

    
    private Predicate having;

    private List<JoinExpression> joins = ImmutableList.of();

    private Expression<?> joinTarget;

    private JoinType joinType;

    
    private Predicate joinCondition;

    private Set<JoinFlag> joinFlags = ImmutableSet.of();

    
    private QueryModifiers modifiers = QueryModifiers.EMPTY;

    private List<OrderSpecifier<?>> orderBy = ImmutableList.of();

    private List<Expression<?>> projection = ImmutableList.of();

    // NOTE : this is not necessarily serializable
    private Map<ParamExpression<?>,Object> params = ImmutableMap.<ParamExpression<?>, Object>of();

    private boolean unique;

    
    private Predicate where;

    private Set<QueryFlag> flags = ImmutableSet.of();

    private boolean extractParams = true;

    private boolean validate = true;

    private ValidatingVisitor validatingVisitor = ValidatingVisitor.DEFAULT;

    private static Predicate and(Predicate lhs, Predicate rhs) {
        if (lhs == null) {
            return rhs;
        } else {
            return ExpressionUtils.and(lhs, rhs);
        }
    }

    /**
     * Create an empty DefaultQueryMetadata instance
     */
    public DefaultQueryMetadata() {}

    /**
     * Disable validation
     *
     * @return
     */
    public DefaultQueryMetadata noValidate() {
        validate = false;
        return this;
    }


    
    public void addFlag(QueryFlag flag) {
        flags = addSorted(flags, flag);
    }

    
    public void addJoinFlag(JoinFlag flag) {
        joinFlags = addSorted(joinFlags, flag);
    }

    
    public void addGroupBy(Expression<?> o) {
        addLastJoin();
        validate(o);
        groupBy = add(groupBy, o);
    }

    
    public void addHaving(Predicate e) {
        addLastJoin();
        if (e == null) {
            return;
        }
        e = (Predicate)ExpressionUtils.extract(e);
        if (e != null) {
            validate(e);
            having = and(having, e);
        }
    }

    private void addLastJoin() {
        if (joinTarget == null) {
            return;
        }
        joins = add(joins, new JoinExpression(joinType, joinTarget, joinCondition, joinFlags));

        joinType = null;
        joinTarget = null;
        joinCondition = null;
        joinFlags = ImmutableSet.of();
    }

    
    public void addJoin(JoinType joinType, Expression<?> expr) {
        addLastJoin();
        if (!exprInJoins.contains(expr)) {
            if (expr instanceof Path && ((Path<?>)expr).getMetadata().isRoot()) {
                exprInJoins = add(exprInJoins, expr);
            } else {
                validate(expr);
            }
            this.joinType = joinType;
            this.joinTarget = expr;
        } else if (validate) {
            throw new IllegalStateException(expr + " is already used");
        }
    }

    
    public void addJoinCondition(Predicate o) {
        validate(o);
        joinCondition = and(joinCondition, o);
    }

    
    public void addOrderBy(OrderSpecifier<?> o) {
        addLastJoin();
        // order specifiers can't be validated, since they can refer to projection elements
        // that are declared later
        orderBy = add(orderBy, o);
    }

    
    public void addProjection(Expression<?> o) {
        addLastJoin();
        validate(o);
        projection = add(projection, o);
    }

    
    public void addWhere(Predicate e) {
        if (e == null) {
            return;
        }
        addLastJoin();
        e = (Predicate)ExpressionUtils.extract(e);
        if (e != null) {
            validate(e);
            where = and(where, e);
        }
    }

    
    public void clearOrderBy() {
        orderBy = ImmutableList.of();
    }

    
    public void clearProjection() {
        projection = ImmutableList.of();
    }

    
    public void clearWhere() {
        where = new BooleanBuilder();
    }

    
    public QueryMetadata clone() {
        try {
            DefaultQueryMetadata clone = (DefaultQueryMetadata) super.clone();
            clone.exprInJoins = copyOf(exprInJoins);
            clone.groupBy = copyOf(groupBy);
            clone.having = having;
            clone.joins = copyOf(joins);
            clone.modifiers = modifiers;
            clone.orderBy = copyOf(orderBy);
            clone.projection = copyOf(projection);
            clone.params = copyOf(params);
            clone.where = where;
            clone.flags = copyOfSorted(flags);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new QueryException(e);
        }
    }

    
    public List<Expression<?>> getGroupBy() {
        return groupBy;
    }

    
    public Predicate getHaving() {
        return having;
    }

    
    public List<JoinExpression> getJoins() {
        addLastJoin();
        return joins;
    }

    
    
    public QueryModifiers getModifiers() {
        return modifiers;
    }

    
    public Map<ParamExpression<?>,Object> getParams() {
        return params;
    }

    
    public List<OrderSpecifier<?>> getOrderBy() {
        return orderBy;
    }

    
    public List<Expression<?>> getProjection() {
        return projection;
    }

    
    public Predicate getWhere() {
        return where;
    }

    
    public boolean isDistinct() {
        return distinct;
    }

    
    public boolean isUnique() {
        return unique;
    }

    
    public void reset() {
        clearProjection();
        params = ImmutableMap.of();
        modifiers = QueryModifiers.EMPTY;
    }

    
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    
    public void setLimit(Long limit) {
        if (modifiers == null || modifiers.getOffset() == null) {
            modifiers = QueryModifiers.limit(limit);
        } else {
            modifiers = new QueryModifiers(limit, modifiers.getOffset());
        }
    }

    
    public void setModifiers(QueryModifiers restriction) {
        this.modifiers = restriction;
    }

    
    public void setOffset(Long offset) {
        if (modifiers == null || modifiers.getLimit() == null) {
            modifiers = QueryModifiers.offset(offset);
        } else {
            modifiers = new QueryModifiers(modifiers.getLimit(), offset);
        }
    }

    
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    
    public <T> void setParam(ParamExpression<T> param, T value) {
        params = put(params, param, value);
    }

    
    public Set<QueryFlag> getFlags() {
        return flags;
    }

    
    public boolean hasFlag(QueryFlag flag) {
        return flags.contains(flag);
    }

    
    public void removeFlag(QueryFlag flag) {
        flags = removeSorted(flags, flag);
    }

    private void validate(Expression<?> expr) {
        if (extractParams) {
            expr.accept(ParamsVisitor.DEFAULT, this);
        }
        if (validate) {
            exprInJoins = expr.accept(validatingVisitor, exprInJoins);
        }
    }

    
    public void setValidate(boolean v) {
        this.validate = v;
    }

    public void setValidatingVisitor(ValidatingVisitor visitor) {
        this.validatingVisitor = visitor;
    }

    
    public boolean equals(Object o) {
        if (o instanceof QueryMetadata) {
            QueryMetadata q = (QueryMetadata)o;
            return q.getFlags().equals(flags)
                && q.getGroupBy().equals(groupBy)
                && Objects.equal(q.getHaving(), having)
                && q.isDistinct() == distinct
                && q.isUnique() == unique
                && q.getJoins().equals(joins)
                && Objects.equal(q.getModifiers(), modifiers)
                && q.getOrderBy().equals(orderBy)
                && q.getParams().equals(params)
                && q.getProjection().equals(projection)
                && Objects.equal(q.getWhere(), where);

        } else {
            return false;
        }
    }

    
    public int hashCode() {
        return Objects.hashCode(flags, groupBy, having, joins, modifiers,
                orderBy, params, projection, unique, where);
    }


}
