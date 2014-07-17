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

/**
 * Boolean typed single result subquery
 *
 * @author tiwe
 */
public final class BooleanSubQuery extends BooleanExpression implements ExtendedSubQueryExpression<Boolean> {

    private static final long serialVersionUID = -64156984110154969L;

    private final SubQueryExpressionImpl<Boolean> subQueryMixin;

    
    private volatile BooleanExpression exists;

    public BooleanSubQuery(QueryMetadata md) {
        super(new SubQueryExpressionImpl<Boolean>(Boolean.class, md));
        subQueryMixin = (SubQueryExpressionImpl<Boolean>)mixin;
    }

    
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
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
