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
package br.com.anteros.persistence.osql.types.expression;

import java.util.List;

import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.Operation;
import br.com.anteros.persistence.osql.types.OperationImpl;
import br.com.anteros.persistence.osql.types.Operator;
import br.com.anteros.persistence.osql.types.Visitor;

import com.google.common.collect.ImmutableList;

/**
 * DateTimeOperation represents DateTime operations
 *
 * @author tiwe
 *
 * @param <T> expression type
 */
public class DateTimeOperation<T extends Comparable<?>> extends DateTimeExpression<T> implements Operation<T> {

    private static final long serialVersionUID = 6523293814317168556L;

    public static <D extends Comparable<?>> DateTimeExpression<D> create(Class<D> type, Operator<? super D> op, Expression<?> one) {
        return new DateTimeOperation<D>(type, op, ImmutableList.<Expression<?>>of(one));
    }
    
    public static <D extends Comparable<?>> DateTimeExpression<D> create(Class<D> type, Operator<? super D> op, Expression<?> one, Expression<?> two) {
        return new DateTimeOperation<D>(type, op, ImmutableList.of(one, two));
    }
    
    public static <D extends Comparable<?>> DateTimeExpression<D> create(Class<D> type, Operator<? super D> op, Expression<?>... args) {
        return new DateTimeOperation<D>(type, op, args);
    }

    private final OperationImpl<T> opMixin;

    protected DateTimeOperation(Class<T> type, Operator<? super T> op, Expression<?>... args) {
        this(type, op, ImmutableList.copyOf(args));
    }

    protected DateTimeOperation(Class<T> type, Operator<? super T> op, ImmutableList<Expression<?>> args) {
        super(new OperationImpl<T>(type, op, args));
        this.opMixin = (OperationImpl<T>)mixin;
    }

    
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(opMixin, context);
    }
    
    
    public Expression<?> getArg(int index) {
        return opMixin.getArg(index);
    }

    
    public List<Expression<?>> getArgs() {
        return opMixin.getArgs();
    }

    
    public Operator<? super T> getOperator() {
        return opMixin.getOperator();
    }

}