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
package br.com.anteros.persistence.dsl.osql.types;



/**
 * Visitor defines a Visitor signature for {@link Expression} instances.
 *
 * @author tiwe
 */
public interface Visitor<R,C> {

    /**
     * @param expr
     */
    
    R visit(Constant<?> expr,  C context);

    /**
     * @param expr
     */
    
    R visit(FactoryExpression<?> expr,  C context);

    /**
     * @param expr
     */
    
    R visit(Operation<?> expr,  C context);

    /**
     * @param expr
     */
    
    R visit(ParamExpression<?> expr,  C context);

    /**
     * @param expr
     */
    
    R visit(Path<?> expr,  C context);

    /**
     * @param expr
     */
    
    R visit(SubQueryExpression<?> expr,  C context);

    /**
     * @param expr
     */
    
    R visit(TemplateExpression<?> expr,  C context);

}
