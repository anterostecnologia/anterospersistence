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

import br.com.anteros.persistence.osql.types.SubQueryExpression;
import br.com.anteros.persistence.osql.types.expression.BooleanExpression;

/**
 * Extensions to the SubQueryExpression interface
 *
 * @author tiwe
 *
 * @param <T> expression type
 */
public interface ExtendedSubQueryExpression<T> extends SubQueryExpression<T> {

    /**
     * Get an exists(this) expression for the subquery
     *
     * @return
     */
    BooleanExpression exists();

    /**
     * Get a not exists(this) expression for the subquery
     *
     * @return
     */
    BooleanExpression notExists();

}
