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

/**
 * ArrayExpression defines an interface for array typed expression
 *
 * @author tiwe
 *
 * @param <A> array type
 * @param <T> array element type
 */
public interface ArrayExpression<A, T> extends Expression<A> {

    /**
     * Get the size of the array
     *
     * @return
     */
    NumberExpression<Integer> size();

    /**
     * Get the element at the given index
     *
     * @param index
     * @return
     */
    SimpleExpression<T> get(Expression<Integer> index);

    /**
     * Get the element at the given index
     *
     * @param index
     * @return
     */
    SimpleExpression<T> get(int index);

}
