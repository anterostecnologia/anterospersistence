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
package br.com.anteros.persistence.dsl.osql.types;

import java.util.Set;

/**
 * EntityPath is the common interface for entity path expressions
 *
 * @author tiwe modified by: Edson Martins
 *
 * @param <T> entity type
 */
public interface EntityPath<T> extends Path<T> {

    /**
     * Returns additional metadata for the given property path or null if none is available
     *
     * @param property
     * @return
     */
    Object getMetadata(Path<?> property);
    
    /**
     * Retorna lista de Path's para projeção.
     * @return
     */
    Set<Path<?>> getCustomProjection();
    
    /**
     * Retorna lista de Path's para exclusão da projeção.
     * @return
     */
    Set<Path<?>> getExcludeProjection();
    
    /**
     * Atribui uma lista de Path's para projeção permitindo assim criar objetos parciais.
     * @param args
     */
    EntityPath<T> customProjection(Path<?>... args);
    
    /**
     * Atribui uma lista de Path's para exclusão da projeção permitindo assim omitir campos e assim criar objetos parciais.
     * @param args
     */
    EntityPath<T> excludeProjection(Path<?>... args);
    

}
