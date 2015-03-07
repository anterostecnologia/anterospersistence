/*******************************************************************************
 * Copyright 2011, Mysema Ltd
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql;

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
 * Detachable defines methods for the construction of SubQuery instances
 *
 * @author tiwe
 *
 */
public interface Detachable {

    /**
     * Return the count of matched rows as a sub query
     *
     * @return
     */
    NumberSubQuery<Long> count();

    /**
     * Create an exists(this) expression
     *
     * @return
     */
    BooleanExpression exists();

    /**
     * Create a projection expression for the given projection
     *
     * @param args
     * @return
     */
    ListSubQuery<Tuple> list(Expression<?>... args);
    
    /**
     * Create a projection expression for the given projection
     * Non expression arguments are converted into constant expressions
     * 
     * @param args
     * @return
     */
    ListSubQuery<Tuple> list(Object... args);

    /**
     * Create a projection expression for the given projection
     *
     * @param <RT>
     *            generic type of the List
     * @param projection
     * @return a List over the projection
     */
    <RT> ListSubQuery<RT> list(Expression<RT> projection);
    

    /**
     * Create an not exists(this) expression
     *
     * @return
     */
    BooleanExpression notExists();

    /**
     * Create a projection expression for the given projection
     *
     * @param args
     * @return
     */
    SimpleSubQuery<Tuple> unique(Expression<?>... args);
    
    /**
     * Create a projection expression for the given projection
     * Non expression arguments are converted into constant expressions
     * 
     * @param args
     * @return
     */
    SimpleSubQuery<Tuple> unique(Object... args);

    /**
     * Create a subquery expression for the given projection
     *
     * @param <RT>
     *            return type
     * @param projection
     * @return the result or null for an empty result
     */
    <RT> SimpleSubQuery<RT> unique(Expression<RT> projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param projection
     * @return
     */
    BooleanSubQuery unique(Predicate projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param projection
     * @return
     */
    StringSubQuery unique(StringExpression projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param <RT>
     * @param projection
     * @return
     */
    <RT extends Comparable<?>> ComparableSubQuery<RT> unique(ComparableExpression<RT> projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param <RT>
     * @param projection
     * @return
     */
    <RT extends Comparable<?>> DateSubQuery<RT> unique(DateExpression<RT> projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param <RT>
     * @param projection
     * @return
     */
    <RT extends Comparable<?>> DateTimeSubQuery<RT> unique(DateTimeExpression<RT> projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param <RT>
     * @param projection
     * @return
     */
    <RT extends Comparable<?>> TimeSubQuery<RT> unique(TimeExpression<RT> projection);

    /**
     * Create a subquery expression for the given projection
     *
     * @param <RT>
     * @param projection
     * @return
     */
    <RT extends Number & Comparable<?>> NumberSubQuery<RT> unique(NumberExpression<RT> projection);

}
