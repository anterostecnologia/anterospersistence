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

import br.com.anteros.persistence.osql.types.Path;
import br.com.anteros.persistence.osql.types.PathImpl;
import br.com.anteros.persistence.osql.types.PathMetadata;
import br.com.anteros.persistence.osql.types.PathMetadataFactory;
import br.com.anteros.persistence.osql.types.Visitor;
import br.com.anteros.persistence.osql.types.expression.ComparableExpression;

/**
 * ComparablePath represents Comparable paths
 *
 * @author tiwe
 *
 * @param <T> expression type
 * @see java.util.ComparableType
 */
@SuppressWarnings({"unchecked"})
public class ComparablePath<T extends Comparable> extends ComparableExpression<T> implements Path<T> {

    private static final long serialVersionUID = -7434767743611671666L;

    private final PathImpl<T> pathMixin;

    public ComparablePath(Class<? extends T> type, Path<?> parent, String property) {
        this(type, PathMetadataFactory.forProperty(parent, property));
    }

    public ComparablePath(Class<? extends T> type, PathMetadata<?> metadata) {
        super(new PathImpl<T>(type, metadata));
        this.pathMixin = (PathImpl<T>)mixin;
    }

    public ComparablePath(Class<? extends T> type, String var) {
        this(type, PathMetadataFactory.forVariable(var));
    }
    
    
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(pathMixin, context);
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
}
