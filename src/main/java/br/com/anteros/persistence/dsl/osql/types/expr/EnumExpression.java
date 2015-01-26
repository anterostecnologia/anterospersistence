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
 * EnumExpression represents Enum typed expressions
 *
 * @author tiwe
 *
 * @param <T> expression type
 */
public abstract class EnumExpression<T extends Enum<T>> extends ComparableExpression<T> {

    private static final long serialVersionUID = 8819222316513862829L;

    public EnumExpression(Expression<T> mixin) {
        super(mixin);
    }

    @Override
    public EnumExpression<T> as(Path<T> alias) {
        return EnumOperation.create(getType(),Ops.ALIAS, mixin, alias);
    }

    @Override
    public EnumExpression<T> as(String alias) {
        return as(new PathImpl<T>(getType(), alias));
    }
    
    /**
     * @return
     */
    public NumberExpression<Integer> ordinal() {
        return NumberOperation.create(Integer.class, Ops.ORDINAL, mixin);
    }

}