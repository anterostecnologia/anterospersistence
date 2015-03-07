/*******************************************************************************
 * Copyright 2011, Mysema Ltd
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql.support;



import br.com.anteros.persistence.dsl.osql.Detachable;
import br.com.anteros.persistence.dsl.osql.Tuple;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.ComparableExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.DateExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.DateTimeExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.NumberExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.StringExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.TimeExpression;
import br.com.anteros.persistence.dsl.osql.types.query.BooleanSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.ComparableSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.DateSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.DateTimeSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.ListSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.NumberSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.SimpleSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.StringSubQuery;
import br.com.anteros.persistence.dsl.osql.types.query.TimeSubQuery;

/**
 * DetachableAdapter is an apadater implementation for the Detachable interface
 * 
 * @author tiwe
 *
 */
public class DetachableAdapter implements Detachable{

    
    private Detachable detachable;

    public DetachableAdapter() {}
    
    public DetachableAdapter(Detachable detachable) {
        this.detachable = detachable;
    }
    
    public NumberSubQuery<Long> count() {
        return detachable.count();
    }

    public BooleanExpression exists() {
        return detachable.exists();
    }

    public Detachable getDetachable() {
        return detachable;
    }

    public ListSubQuery<Tuple> list(Expression<?> first, Expression<?> second, 
            Expression<?>... rest) {
        return detachable.list(first, second, rest);
    }

    public ListSubQuery<Tuple> list(Expression<?>... args) {
        return detachable.list(args);
    }

    public <RT> ListSubQuery<RT> list(Expression<RT> projection) {
        return detachable.list(projection);
    }

    @Override
    public ListSubQuery<Tuple> list(Object... args) {
        return detachable.list(args);
    }

    public BooleanExpression notExists() {
        return detachable.notExists();
    }

    protected void setDetachable(Detachable detachable) {
        this.detachable = detachable;
    }

    public <RT extends Comparable<?>> ComparableSubQuery<RT> unique(ComparableExpression<RT> projection) {
        return detachable.unique(projection);
    }

    public <RT extends Comparable<?>> DateSubQuery<RT> unique(DateExpression<RT> projection) {
        return detachable.unique(projection);
    }

    public <RT extends Comparable<?>> DateTimeSubQuery<RT> unique(DateTimeExpression<RT> projection) {
        return detachable.unique(projection);
    }

    public SimpleSubQuery<Tuple> unique(Expression<?> first, Expression<?> second, Expression<?>... rest) {
        return detachable.unique(first, second, rest);
    }

    public SimpleSubQuery<Tuple> unique(Expression<?>... args) {
        return detachable.unique(args);
    }

    public <RT> SimpleSubQuery<RT> unique(Expression<RT> projection) {
        return detachable.unique(projection);
    }

    public <RT extends Number & Comparable<?>> NumberSubQuery<RT> unique(NumberExpression<RT> projection) {
        return detachable.unique(projection);
    }

    public BooleanSubQuery unique(Predicate projection) {
        return detachable.unique(projection);
    }
    
    public StringSubQuery unique(StringExpression projection) {
        return detachable.unique(projection);
    }

    public <RT extends Comparable<?>> TimeSubQuery<RT> unique(TimeExpression<RT> projection) {
        return detachable.unique(projection);
    }

    @Override
    public SimpleSubQuery<Tuple> unique(Object... args) {
        return detachable.unique(args);
    }

}
