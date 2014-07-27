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
package br.com.anteros.persistence.osql.support;

import java.util.List;
import java.util.Map;

import br.com.anteros.core.utils.MathUtils;
import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.ExpressionBase;
import br.com.anteros.persistence.osql.types.FactoryExpression;
import br.com.anteros.persistence.osql.types.Visitor;

import com.google.common.collect.Maps;

/**
 * NumberConversions ensures that the results of a projection involving numeric expressions
 * confirm to the types of the numeric expressions
 *
 * @author tiwe
 *
 * @param <T>
 */
public class NumberConversions<T> extends ExpressionBase<T> implements FactoryExpression<T> {

    private static final long serialVersionUID = -7834053123363933721L;

    private final FactoryExpression<T> expr;

    private final Map<Class<?>, Enum<?>[]> values = Maps.newHashMap();

    public NumberConversions(FactoryExpression<T> expr) {
        super(expr.getType());
        this.expr = expr;
    }

    
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    
    public List<Expression<?>> getArgs() {
        return expr.getArgs();
    }

    private <E extends Enum<E>> Enum<E>[] getValues(Class<E> enumClass) {
        Enum<E>[] values = (Enum<E>[]) this.values.get(enumClass);
        if (values == null) {
            try {
                values = (Enum<E>[]) enumClass.getMethod("values").invoke(null);
                this.values.put(enumClass, values);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return values;
    }

    
    public T newInstance(Object... args) {
        for (int i = 0; i < args.length; i++) {
            Class<?> type = expr.getArgs().get(i).getType();
            if (Enum.class.isAssignableFrom(type) && !type.isInstance(args[i])) {
                if (args[i] instanceof String) {
                    args[i] = Enum.valueOf((Class)type, (String)args[i]);
                } else if (args[i] instanceof Number) {
                    args[i] = getValues((Class)type)[((Number)args[i]).intValue()];
                }
            } else if (args[i] instanceof Number && !type.isInstance(args[i])) {
                if (type.equals(Boolean.class)) {
                    args[i] = ((Number)args[i]).intValue() > 0;
                } else if (Number.class.isAssignableFrom(type)){
                    args[i] = MathUtils.cast((Number)args[i], (Class)type);
                }
            }
        }
        return expr.newInstance(args);
    }

}
