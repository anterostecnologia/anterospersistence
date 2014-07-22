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
package br.com.anteros.persistence.osql.group;

import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.Ops;
import br.com.anteros.persistence.osql.types.Path;
import br.com.anteros.persistence.osql.types.PathImpl;
import br.com.anteros.persistence.osql.types.Visitor;
import br.com.anteros.persistence.osql.types.expression.DslExpression;
import br.com.anteros.persistence.osql.types.expression.DslOperation;

/**
 * A base class for GroupExpressions
 * @author sasa
 *
 * @param <T>
 * @param <R>
 */
public abstract class AbstractGroupExpression<T, R> implements GroupExpression<T, R> {

    private static final long serialVersionUID = 1509709546966783160L;

    private final Class<? extends R> type;

    private final Expression<T> expr;

    @SuppressWarnings("unchecked")
    public AbstractGroupExpression(Class<? super R> type, Expression<T> expr) {
        this.type = (Class)type;
        this.expr = expr;
    }

    /**
     * Create an alias for the expression
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public DslExpression<R> as(Path<R> alias) {
        return DslOperation.create((Class<R>)getType(), Ops.ALIAS, this, alias);
    }

    /**
     * Create an alias for the expression
     *
     * @return
     */
    public DslExpression<R> as(String alias) {
        return as(new PathImpl<R>(getType(), alias));
    }

    
    public Expression<T> getExpression() {
        return expr;
    }

    
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return expr.accept(v, context);
    }

    
    public boolean equals(Object o) {
        if (o != null && getClass().equals(o.getClass())) {
            return ((GroupExpression<?,?>)o).getExpression().equals(expr);
        } else {
            return false;
        }
    }

    
    public Class<? extends R> getType() {
        return type;
    }

    
    public int hashCode() {
        return expr.hashCode();
    }

    
    public String toString() {
        return expr.toString();
    }

}