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
package br.com.anteros.persistence.osql.support;

import br.com.anteros.persistence.osql.Detachable;
import br.com.anteros.persistence.osql.Tuple;
import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.Predicate;
import br.com.anteros.persistence.osql.types.expression.BooleanExpression;
import br.com.anteros.persistence.osql.types.expression.ComparableExpression;
import br.com.anteros.persistence.osql.types.expression.DateExpression;
import br.com.anteros.persistence.osql.types.expression.DateTimeExpression;
import br.com.anteros.persistence.osql.types.expression.NumberExpression;
import br.com.anteros.persistence.osql.types.expression.StringExpression;
import br.com.anteros.persistence.osql.types.expression.TimeExpression;
import br.com.anteros.persistence.osql.types.query.BooleanSubQuery;
import br.com.anteros.persistence.osql.types.query.ComparableSubQuery;
import br.com.anteros.persistence.osql.types.query.DateSubQuery;
import br.com.anteros.persistence.osql.types.query.DateTimeSubQuery;
import br.com.anteros.persistence.osql.types.query.ListSubQuery;
import br.com.anteros.persistence.osql.types.query.NumberSubQuery;
import br.com.anteros.persistence.osql.types.query.SimpleSubQuery;
import br.com.anteros.persistence.osql.types.query.StringSubQuery;
import br.com.anteros.persistence.osql.types.query.TimeSubQuery;

/**
 * DetachableQuery is a base class for queries which implement the Query and Detachable interfaces
 * 
 * @author tiwe
 *
 * @param <Q> concrete subtype
 */
public class DetachableQuery <Q extends DetachableQuery<Q>> extends QueryBase<Q> implements Detachable {

    private final DetachableMixin detachableMixin;

    public DetachableQuery(QueryMixin<Q> queryMixin) {
        super(queryMixin);
        this.detachableMixin = new DetachableMixin(queryMixin);
    }

    @Override
    public NumberSubQuery<Long> count() {
        return detachableMixin.count();
    }

    @Override
    public BooleanExpression exists() {
        return detachableMixin.exists();
    }

    @Override
    public ListSubQuery<Tuple> list(Expression<?>... args) {
        return detachableMixin.list(args);
    }

    @Override
    public <RT> ListSubQuery<RT> list(Expression<RT> projection) {
        return detachableMixin.list(projection);
    }

    public ListSubQuery<Tuple> list(Object arg) {
        return detachableMixin.list(arg);
    }
    
    @Override
    public ListSubQuery<Tuple> list(Object... args) {
        return detachableMixin.list(args);
    }

    @Override
    public BooleanExpression notExists() {
        return detachableMixin.notExists();
    }

    @Override
    public <RT extends Comparable<?>> ComparableSubQuery<RT> unique(ComparableExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> DateSubQuery<RT> unique(DateExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> DateTimeSubQuery<RT> unique(DateTimeExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public SimpleSubQuery<Tuple> unique(Expression<?>... args) {
        return detachableMixin.unique(args);
    }

    @Override
    public <RT> SimpleSubQuery<RT> unique(Expression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Number & Comparable<?>> NumberSubQuery<RT> unique(NumberExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public BooleanSubQuery unique(Predicate projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public StringSubQuery unique(StringExpression projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> TimeSubQuery<RT> unique(TimeExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public SimpleSubQuery<Tuple> unique(Object... args) {
        return detachableMixin.unique(args);
    }

}
