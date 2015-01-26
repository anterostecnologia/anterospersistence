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
package br.com.anteros.persistence.dsl.osql;

import java.util.List;

import br.com.anteros.persistence.dsl.osql.lang.CloseableIterator;

/**
 * SimpleProjectable defines a simpler projection interface than {@link Projectable}.
 *
 * @author tiwe
 * @see Projectable
 */
public interface SimpleProjectable<T> {

    /**
     * @return true, if rows matching the given criteria exist, otherwise false
     */
    boolean exists();

    /**
     * @return true, if no rows matching the given criteria exist, otherwise false
     */
    boolean notExists();

    /**
     * Get the projection as a typed closeable Iterator
     *
     * @return
     */
    CloseableIterator<T> iterate();

    /**
     * Get the projection as a typed List
     *
     * @return
     */
    List<T> list();

    /**
     * Get the projection as a single result or null if no result is found
     *
     * <p>for multiple results only the first one is returned</p>
     *
     * @return
     */
    
    T singleResult();

    /**
     * Get the projection as a unique result or null if no result is found
     *
     * @throws NonUniqueResultException if there is more than one matching result
     * @return
     */
    
    T uniqueResult();

    /**
     * Get the projection in {@link SearchResults} form
     *
     * @return
     */
    SearchResults<T> listResults();

    /**
     * Get the count of matched elements
     *
     * @return
     */
    long count();

}
