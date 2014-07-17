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

import java.util.List;

import br.com.anteros.persistence.osql.Projectable;
import br.com.anteros.persistence.osql.QueryModifiers;
import br.com.anteros.persistence.osql.SearchResults;
import br.com.anteros.persistence.osql.SimpleProjectable;
import br.com.anteros.persistence.osql.SimpleQuery;
import br.com.anteros.persistence.osql.lang.CloseableIterator;
import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.OrderSpecifier;
import br.com.anteros.persistence.osql.types.ParamExpression;
import br.com.anteros.persistence.osql.types.Predicate;

/**
 * SimpleQueryAdapter is an adapter implementation for the {@link SimpleQuery} and 
 * {@link SimpleProjectable} interfaces
 *
 * @author tiwe
 *
 * @param <T> type of entity
 */
public class SimpleProjectableAdapter<T> implements SimpleQuery<SimpleProjectableAdapter<T>>, SimpleProjectable<T> {

    private final Projectable projectable;

    private final Expression<T> projection;

    private final SimpleQuery<?> query;


    public <Q extends SimpleQuery<?> & Projectable> SimpleProjectableAdapter(Q query, Expression<T> projection) {
        this(query, query, projection);
    }

    public SimpleProjectableAdapter(SimpleQuery<?> query, Projectable projectable, Expression<T> projection) {
        this.query = query;
        this.projectable = projectable;
        this.projection = projection;
    }

    
    public boolean exists() {
        return projectable.exists();
    }

    
    public boolean notExists() {
        return projectable.notExists();
    }

    
    public long count() {
        return projectable.count();
    }

    
    public SimpleProjectableAdapter<T> distinct() {
        query.distinct();
        return this;
    }

    
    public SimpleProjectableAdapter<T> limit(long limit) {
        query.limit(limit);
        return this;
    }

    
    public CloseableIterator<T> iterate() {
        return projectable.iterate(projection);
    }

    
    public List<T> list() {
        return projectable.list(projection);
    }

    
    public SearchResults<T> listResults() {
        return projectable.listResults(projection);
    }

    
    public SimpleProjectableAdapter<T> offset(long offset) {
        query.offset(offset);
        return this;
    }

    
    public SimpleProjectableAdapter<T> orderBy(OrderSpecifier<?>... o) {
        query.orderBy(o);
        return this;
    }

    
    public SimpleProjectableAdapter<T> restrict(QueryModifiers modifiers) {
        query.restrict(modifiers);
        return this;
    }

    
    public <P> SimpleProjectableAdapter<T> set(ParamExpression<P> param, P value) {
        query.set(param, value);
        return this;
    }

    
    public String toString() {
        return query.toString();
    }

    
    public T singleResult() {
        return projectable.singleResult(projection);
    }

    
    public T uniqueResult() {
        return projectable.uniqueResult(projection);
    }

    
    public SimpleProjectableAdapter<T> where(Predicate... e) {
        query.where(e);
        return this;
    }

}
