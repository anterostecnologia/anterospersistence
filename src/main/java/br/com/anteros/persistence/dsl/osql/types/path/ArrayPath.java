/*******************************************************************************
 * Copyright 2011, Mysema Ltd
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql.types.path;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.PathMetadata;
import br.com.anteros.persistence.dsl.osql.types.PathMetadataFactory;
import br.com.anteros.persistence.dsl.osql.types.Visitor;
import br.com.anteros.persistence.dsl.osql.types.expr.ArrayExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.NumberExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.NumberOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.SimpleExpression;

import com.google.common.primitives.Primitives;

/**
 * ArrayPath represents an array typed path
 *
 * @author tiwe
 * 
 * @param <A> array type
 * @param <E> component type
 */
public class ArrayPath<A, E> extends SimpleExpression<A> implements Path<A>, ArrayExpression<A, E> {

    private static final long serialVersionUID = 7795049264874048226L;

    private final Class<E> componentType;

    private final PathImpl<A> pathMixin;

    
    private volatile NumberExpression<Integer> size;

    public ArrayPath(Class<? super A> type, String variable) {
        this(type, PathMetadataFactory.forVariable(variable));
    }
    
    public ArrayPath(Class<? super A> type, Path<?> parent, String property) {
        this(type, PathMetadataFactory.forProperty(parent, property));
    }
    
    @SuppressWarnings("unchecked")
    public ArrayPath(Class<? super A> type, PathMetadata<?> metadata) {
        super(new PathImpl<A>((Class)type, metadata));
        this.pathMixin = (PathImpl<A>)mixin;
        this.componentType = Primitives.wrap((Class<E>)type.getComponentType());
    }

    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(pathMixin, context);
    }
    
    /**
     * Create a expression for indexed access
     *
     * @param index
     * @return
     */
    public SimplePath<E> get(Expression<Integer> index) {
        PathMetadata<Integer> md = PathMetadataFactory.forArrayAccess(pathMixin, index);
        return new SimplePath<E>(componentType, md);
    }

    /**
     * Create a expression for indexed access
     *
     * @param index
     * @return
     */
    public SimplePath<E> get(int index) {
        PathMetadata<Integer> md = PathMetadataFactory.forArrayAccess(pathMixin, index);
        return new SimplePath<E>(componentType, md);
    }

    public Class<E> getElementType() {
        return componentType;
    }

    @Override
    public PathMetadata<?> getMetadata() {
        return pathMixin.getMetadata();
    }

    @Override
    public Path<?> getRoot() {
        return pathMixin.getRoot();
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return pathMixin.getAnnotatedElement();
    }

    /**
     * Create an expression for the array size
     *
     * @return
     */
    public NumberExpression<Integer> size() {
        if (size == null) {
            size = NumberOperation.create(Integer.class, Ops.ARRAY_SIZE, pathMixin);
        }
        return size;
    }

}
