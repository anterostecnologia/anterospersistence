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
import br.com.anteros.persistence.dsl.osql.types.ExpressionUtils;
import br.com.anteros.persistence.dsl.osql.types.OperationImpl;
import br.com.anteros.persistence.dsl.osql.types.Operator;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;

/**
 * UnionUtils provides static utility methods for Union handling
 *
 * @author tiwe
 *
 */
public final class UnionUtils {

    public static Expression<?> union(SubQueryExpression<?>[] union, boolean unionAll) {
        final Operator<Object> operator = unionAll ? SQLOps.UNION_ALL : SQLOps.UNION;
        Expression<?> rv = union[0];
        for (int i = 1; i < union.length; i++) {
            rv = OperationImpl.create(rv.getType(), operator, rv, union[i]);
        }
        return rv;
    }

    public static Expression<?> union(SubQueryExpression<?>[] union, Path<?> alias,
            boolean unionAll) {
        final Expression<?> rv = union(union, unionAll);
        return ExpressionUtils.as((Expression)rv, alias);
    }

    private UnionUtils() {}

}
