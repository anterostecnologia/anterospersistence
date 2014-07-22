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
import br.com.anteros.persistence.osql.QueryMetadata;
import br.com.anteros.persistence.osql.Tuple;
import br.com.anteros.persistence.osql.types.ConstantImpl;
import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.NullExpression;
import br.com.anteros.persistence.osql.types.Predicate;
import br.com.anteros.persistence.osql.types.ProjectionRole;
import br.com.anteros.persistence.osql.types.expression.BooleanExpression;
import br.com.anteros.persistence.osql.types.expression.ComparableExpression;
import br.com.anteros.persistence.osql.types.expression.DateExpression;
import br.com.anteros.persistence.osql.types.expression.DateTimeExpression;
import br.com.anteros.persistence.osql.types.expression.NumberExpression;
import br.com.anteros.persistence.osql.types.expression.StringExpression;
import br.com.anteros.persistence.osql.types.expression.TimeExpression;
import br.com.anteros.persistence.osql.types.expression.Wildcard;
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
 * Mixin style implementation of the Detachable interface
 *
 * @author tiwe
 *
 */
public class DetachableMixin implements Detachable {

    private final QueryMixin<?> queryMixin;

    public DetachableMixin(QueryMixin<?> queryMixin) {
        this.queryMixin = queryMixin;
    }

    
    public NumberSubQuery<Long> count() {
        return new NumberSubQuery<Long>(Long.class, projection(Wildcard.count));
    }

    
    public BooleanExpression exists() {
        if (queryMixin.getMetadata().getJoins().isEmpty()) {
            throw new IllegalArgumentException("No sources given");
        }
        return unique(queryMixin.getMetadata().getJoins().get(0).getTarget()).exists();
    }

    
    public ListSubQuery<Tuple> list(Expression<?>... args) {
        return new ListSubQuery<Tuple>(Tuple.class, projection(args));
    }

    @SuppressWarnings("unchecked")
    
    public <RT> ListSubQuery<RT> list(Expression<RT> projection) {
        return new ListSubQuery<RT>((Class)projection.getType(), projection(projection));
    }

    public ListSubQuery<Tuple> list(Object arg) {
        return list((Expression)convert(arg));
    }

    
    public ListSubQuery<Tuple> list(Object... args) {
        return list(convert(args));
    }

    
    public SimpleSubQuery<Tuple> unique(Object... args) {
        return unique(convert(args));
    }

    private Expression<?> convert(Object arg) {
        if (arg instanceof Expression<?>) {
            return (Expression<?>)arg;
        } else if (arg instanceof ProjectionRole) {
            return ((ProjectionRole<?>)arg).getProjection();
        } else if (arg != null) {
            return ConstantImpl.create(arg);
        } else {
            return NullExpression.DEFAULT;
        }
    }

    private Expression<?>[] convert(Object... args) {
        final Expression<?>[] exprs = new Expression[args.length];
        for (int i = 0; i < exprs.length; i++) {
            exprs[i] = convert(args[i]);
        }
        return exprs;
    }

    
    public BooleanExpression notExists() {
        return exists().not();
    }

    private QueryMetadata projection(Expression<?>... projection) {
        QueryMetadata metadata = queryMixin.getMetadata().clone();
        for (Expression<?> expr : projection) {
            expr = queryMixin.convert(expr, false);
            metadata.addProjection(nullAsTemplate(expr));
        }
        return metadata;
    }

    private Expression<?> nullAsTemplate(Expression<?> expr) {
        return expr != null ? expr : NullExpression.DEFAULT;
    }

    @SuppressWarnings("unchecked")
    
    public <RT extends Comparable<?>> ComparableSubQuery<RT> unique(ComparableExpression<RT> projection) {
        return new ComparableSubQuery<RT>((Class)projection.getType(), uniqueProjection(projection));
    }

    @SuppressWarnings("unchecked")
    
    public <RT extends Comparable<?>> DateSubQuery<RT> unique(DateExpression<RT> projection) {
        return new DateSubQuery<RT>((Class)projection.getType(), uniqueProjection(projection));
    }

    @SuppressWarnings("unchecked")
    
    public <RT extends Comparable<?>> DateTimeSubQuery<RT> unique(DateTimeExpression<RT> projection) {
        return new DateTimeSubQuery<RT>((Class)projection.getType(), uniqueProjection(projection));
    }

    
    public SimpleSubQuery<Tuple> unique(Expression<?>... args) {
        return new SimpleSubQuery<Tuple>(Tuple.class, uniqueProjection(args));
    }


    @SuppressWarnings("unchecked")
    
    public <RT> SimpleSubQuery<RT> unique(Expression<RT> projection) {
        return new SimpleSubQuery<RT>((Class)projection.getType(), uniqueProjection(projection));
    }

    @SuppressWarnings("unchecked")
    
    public <RT extends Number & Comparable<?>> NumberSubQuery<RT> unique(NumberExpression<RT> projection) {
        return new NumberSubQuery<RT>((Class)projection.getType(), uniqueProjection(projection));
    }

    
    public BooleanSubQuery unique(Predicate projection) {
        return new BooleanSubQuery(uniqueProjection(projection));
    }

    
    public StringSubQuery unique(StringExpression projection) {
        return new StringSubQuery(uniqueProjection(projection));
    }

    @SuppressWarnings("unchecked")
    
    public <RT extends Comparable<?>> TimeSubQuery<RT> unique(TimeExpression<RT> projection) {
        return new TimeSubQuery<RT>((Class)projection.getType(), uniqueProjection(projection));
    }

    private QueryMetadata uniqueProjection(Expression<?>... projection) {
        QueryMetadata metadata = projection(projection);
        metadata.setUnique(true);
        return metadata;
    }

}