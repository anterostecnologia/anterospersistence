/*
 * Copyright 2012, Mysema Ltd
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.OrderSpecifier;
import br.com.anteros.persistence.osql.types.ParamExpression;
import br.com.anteros.persistence.osql.types.Predicate;

/**
 * EmptyMetadata provides a immutable empty QueryMetadata instace
 *
 * @author tiwe
 *
 */
public final class EmptyMetadata implements QueryMetadata {

    private static final long serialVersionUID = 134750105981272499L;

    public static final QueryMetadata DEFAULT = new EmptyMetadata();

    
    public void addGroupBy(Expression<?> o) {
        throw new UnsupportedOperationException();
    }

    
    public void addHaving(Predicate o) {
        throw new UnsupportedOperationException();
    }

    
    public void addJoin(JoinType joinType, Expression<?> expr) {
        throw new UnsupportedOperationException();
    }

    
    public void addJoinCondition(Predicate o) {
        throw new UnsupportedOperationException();
    }

    
    public void addOrderBy(OrderSpecifier<?> o) {
        throw new UnsupportedOperationException();
    }

    
    public void addProjection(Expression<?> o) {
        throw new UnsupportedOperationException();
    }

    
    public void addWhere(Predicate o) {
        throw new UnsupportedOperationException();
    }

    
    public void clearOrderBy() {
        throw new UnsupportedOperationException();
    }

    
    public void clearProjection() {
        throw new UnsupportedOperationException();
    }

    
    public void clearWhere() {
        throw new UnsupportedOperationException();
    }

    
    public QueryMetadata clone() {
        return this;
    }

    
    public List<Expression<?>> getGroupBy() {
        return Collections.emptyList();
    }

    
    public Predicate getHaving() {
        return null;
    }

    
    public List<JoinExpression> getJoins() {
        return Collections.emptyList();
    }

    
    public QueryModifiers getModifiers() {
        return null;
    }

    
    public List<OrderSpecifier<?>> getOrderBy() {
        return Collections.emptyList();
    }

    
    public List<Expression<?>> getProjection() {
        return Collections.emptyList();
    }

    
    public Map<ParamExpression<?>, Object> getParams() {
        return Collections.emptyMap();
    }

    
    public Predicate getWhere() {
        return null;
    }

    
    public boolean isDistinct() {
        return false;
    }

    
    public boolean isUnique() {
        return false;
    }

    
    public void reset() {
        throw new UnsupportedOperationException();
    }

    
    public void setDistinct(boolean distinct) {
        throw new UnsupportedOperationException();
    }

    
    public void setLimit(Long limit) {
        throw new UnsupportedOperationException();
    }

    
    public void setModifiers(QueryModifiers restriction) {
        throw new UnsupportedOperationException();
    }

    
    public void setOffset(Long offset) {
        throw new UnsupportedOperationException();
    }

    
    public void setUnique(boolean unique) {
        throw new UnsupportedOperationException();
    }

    
    public <T> void setParam(ParamExpression<T> param, T value) {
        throw new UnsupportedOperationException();
    }

    
    public void addFlag(QueryFlag flag) {
        throw new UnsupportedOperationException();
    }

    
    public boolean hasFlag(QueryFlag flag) {
        return false;
    }

    
    public Set<QueryFlag> getFlags() {
        return Collections.emptySet();
    }

    
    public void setValidate(boolean v) {
        throw new UnsupportedOperationException();
    }

    
    public void addJoinFlag(JoinFlag flag) {
        throw new UnsupportedOperationException();
    }

    
    public void removeFlag(QueryFlag flag) {

    }

}
