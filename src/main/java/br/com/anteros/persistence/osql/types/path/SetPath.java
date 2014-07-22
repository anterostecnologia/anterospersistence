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
package br.com.anteros.persistence.osql.types.path;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import br.com.anteros.persistence.osql.types.Path;
import br.com.anteros.persistence.osql.types.PathImpl;
import br.com.anteros.persistence.osql.types.PathMetadata;
import br.com.anteros.persistence.osql.types.PathMetadataFactory;
import br.com.anteros.persistence.osql.types.Visitor;
import br.com.anteros.persistence.osql.types.expression.SimpleExpression;

/**
 * SetPath represents set paths
 *
 * @author tiwe
 *
 * @param <E> component type
 * @param <Q> component query type
 */
public class SetPath<E, Q extends SimpleExpression<? super E>> extends CollectionPathBase<Set<E>, E, Q> {

    private static final long serialVersionUID = 4145848445507037373L;

    private final Class<E> elementType;

    private final PathImpl<Set<E>> pathMixin;

    private transient Q any;
    
    private final Class<Q> queryType;

    public SetPath(Class<? super E> type, Class<Q> queryType, String variable) {
        this(type, queryType, PathMetadataFactory.forVariable(variable));
    }
    
    public SetPath(Class<? super E> type, Class<Q> queryType, Path<?> parent, String property) {
        this(type, queryType, PathMetadataFactory.forProperty(parent, property));
    }
    
    public SetPath(Class<? super E> type, Class<Q> queryType, PathMetadata<?> metadata) {
        this(type, queryType, metadata, PathInits.DIRECT);
    }
    
    @SuppressWarnings("unchecked")
    public SetPath(Class<? super E> type, Class<Q> queryType, PathMetadata<?> metadata, PathInits inits) {
        super(new PathImpl<Set<E>>((Class)Set.class, metadata), inits);
        this.elementType = (Class<E>)type;
        this.queryType = queryType;
        this.pathMixin = (PathImpl<Set<E>>)mixin;
    }
    
    
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(pathMixin, context);
    }
    
    
    public Q any() {
        if (any == null) {
            any = newInstance(queryType, PathMetadataFactory.forCollectionAny(this));
        }
        return any;
    }

    public Class<E> getElementType() {
        return elementType;
    }

    
    public PathMetadata<?> getMetadata() {
        return pathMixin.getMetadata();
    }

    
    public Path<?> getRoot() {
        return pathMixin.getRoot();
    }

    
    public AnnotatedElement getAnnotatedElement() {
        return pathMixin.getAnnotatedElement();
    }
    
    
    public Class<?> getParameter(int index) {
        if (index == 0) {
            return elementType;
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }

}