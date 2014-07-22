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
package br.com.anteros.persistence.osql.types.query;



import br.com.anteros.persistence.osql.QueryMetadata;
import br.com.anteros.persistence.osql.types.Ops;
import br.com.anteros.persistence.osql.types.SubQueryExpressionImpl;
import br.com.anteros.persistence.osql.types.Visitor;
import br.com.anteros.persistence.osql.types.expression.BooleanExpression;
import br.com.anteros.persistence.osql.types.expression.BooleanOperation;
import br.com.anteros.persistence.osql.types.expression.NumberExpression;

/**
 * Number typed single result subquery
 *
 * @author tiwe
 *
 * @param <T> expression type
 */
public final class NumberSubQuery<T extends Number & Comparable<?>> extends NumberExpression<T> implements ExtendedSubQueryExpression<T> {

    private static final long serialVersionUID = -64156984110154969L;

    private final SubQueryExpressionImpl<T> subQueryMixin;

    
    private volatile BooleanExpression exists;

    public NumberSubQuery(Class<T> type, QueryMetadata md) {
        super(new SubQueryExpressionImpl<T>(type, md));
        subQueryMixin = (SubQueryExpressionImpl<T>)mixin;
    }

    
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(subQueryMixin, context);
    }

    
    public BooleanExpression exists() {
        if (exists == null) {
            exists = BooleanOperation.create(Ops.EXISTS, mixin);
        }
        return exists;
    }

    
    public QueryMetadata getMetadata() {
        return subQueryMixin.getMetadata();
    }

    
    public BooleanExpression notExists() {
        return exists().not();
    }

}