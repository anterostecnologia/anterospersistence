/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql.types.expr;

import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;

/**
 * DslExpression is the base class for DSL expressions, but {@link SimpleExpression} is the base class
 * for scalar Expressions
 *
 * @author tiwe
 *
 */
public abstract class DslExpression<T> implements Expression<T> {

    private static final long serialVersionUID = -3383063447710753290L;

    protected final Expression<T> mixin;

    protected final int hashCode;

    public DslExpression(Expression<T> mixin) {
        this.mixin = mixin;
        this.hashCode = mixin.hashCode();
    }

    @Override
    public final Class<? extends T> getType() {
        return mixin.getType();
    }

    /**
     * Create an alias for the expression
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public DslExpression<T> as(Path<T> alias) {
        return DslOperation.create((Class<T>)getType(),Ops.ALIAS, mixin, alias);
    }

    /**
     * Create an alias for the expression
     *
     * @return
     */
    public DslExpression<T> as(String alias) {
        return as(new PathImpl<T>(getType(), alias));
    }

    @Override
    public boolean equals(Object o) { // can be overwritten
        return mixin.equals(o);
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public final String toString() {
        return mixin.toString();
    }

}
