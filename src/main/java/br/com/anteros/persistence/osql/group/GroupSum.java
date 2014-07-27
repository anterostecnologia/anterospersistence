/*
 * Copyright 2012, Mysema Ltd
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

import java.math.BigDecimal;

import br.com.anteros.core.utils.MathUtils;
import br.com.anteros.persistence.osql.types.Expression;

/**
 * @author tiwe
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class GroupSum<T extends Number & Comparable<T>> extends AbstractGroupExpression<T, T> {

    private static final long serialVersionUID = 3518868612387641383L;

    public GroupSum(Expression<T> expr) {
        super((Class) expr.getType(), expr);
    }

    public GroupCollector<T, T> createGroupCollector() {
        return new GroupCollector<T, T>() {
            private BigDecimal sum = BigDecimal.ZERO;

            public void add(T t) {
                if (t != null) {
                    sum = sum.add(new BigDecimal(t.toString()));    
                }                
            }

            public T get() {
                return (T) MathUtils.cast(sum, (Class<T>) getType());
            }

        };
    }

}