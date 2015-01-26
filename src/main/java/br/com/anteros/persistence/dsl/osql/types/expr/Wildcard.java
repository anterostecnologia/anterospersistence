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
import br.com.anteros.persistence.dsl.osql.types.Operator;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpressionImpl;

/**
 * Wildcard provides constant Expressions for general wildcard expressions
 * 
 * @author tiwe
 *
 */
public final class Wildcard {
    
    /**
     * Wilcard expression (*) for all columns
     */
    public static final Expression<Object[]> all = TemplateExpressionImpl.create(Object[].class, "*");

    /**
     * Wildcard count expression (count(*))
     */
    public static final NumberExpression<Long> count = NumberOperation.create(Long.class, Ops.AggOps.COUNT_ALL_AGG);
    
    /**
     * Wilcard count distinct expression ((count(distinct *))
     */
    public static final NumberExpression<Long> countDistinct = NumberOperation.create(Long.class, Ops.AggOps.COUNT_DISTINCT_ALL_AGG);

    /**
     * Wilcard count distinct expression ((count(distinct *))
     */
    public static final NumberExpression<Integer> countAsInt = NumberOperation.create(Integer.class, (Operator)Ops.AggOps.COUNT_ALL_AGG);

    private Wildcard() {}
    
}
