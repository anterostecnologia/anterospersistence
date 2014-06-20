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
import java.util.Map;

import br.com.anteros.persistence.osql.Projectable;
import br.com.anteros.persistence.osql.ResultTransformer;
import br.com.anteros.persistence.osql.SearchResults;
import br.com.anteros.persistence.osql.Tuple;
import br.com.anteros.persistence.osql.lang.CloseableIterator;
import br.com.anteros.persistence.osql.types.Expression;

/**
 * ProjectableAdapter is an adapter implementation for the Projectable interface
 *
 * @author tiwe
 */
public class ProjectableAdapter<P extends Projectable> implements Projectable {
  
    private final P projectable;

    public ProjectableAdapter(P projectable) {
        this.projectable = projectable;
    }

    protected P getProjectable() {
        return projectable;
    }

    @Override
    public long count() {
        return projectable.count();
    }

    @Override
    public boolean exists() {
        return projectable.exists();
    }

    @Override
    public boolean notExists() {
        return projectable.notExists();
    }

    @Override
    public CloseableIterator<Tuple> iterate(Expression<?>... args) {
        return projectable.iterate(args);
    }

    @Override
    public <RT> CloseableIterator<RT> iterate(Expression<RT> projection) {
        return projectable.iterate(projection);
    }

    @Override
    public List<Tuple> list(Expression<?>[] args) {
        return projectable.list(args);
    }

    @Override
    public <RT> List<RT> list(Expression<RT> projection) {
        return projectable.list(projection);
    }

    @Override
    public SearchResults<Tuple> listResults(Expression<?>... args) {
        return projectable.listResults(args);
    }
    
    @Override
    public <RT> SearchResults<RT> listResults(Expression<RT> expr) {
        return projectable.listResults(expr);
    }

    @Override
    public <K, V> Map<K, V> map(Expression<K> key, Expression<V> value) {
        return projectable.map(key, value);
    }

    @Override
    public String toString() {
        return projectable.toString();
    }

    @Override
    public Tuple singleResult(Expression<?>... args) {
        return projectable.singleResult(args);
    }

    @Override
    public <RT> RT singleResult(Expression<RT> expr) {
        return projectable.singleResult(expr);
    }

    @Override
    public <T> T transform(ResultTransformer<T> transformer) {
        return projectable.transform(transformer);
    }
    
    @Override
    public Tuple uniqueResult(Expression<?>... args) {
        return projectable.uniqueResult(args);
    }

    @Override
    public <RT> RT uniqueResult(Expression<RT> expr) {
        return projectable.uniqueResult(expr);
    }



}
