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
package br.com.anteros.persistence.dsl.osql.dml;

import java.util.List;

import br.com.anteros.persistence.dsl.osql.FilteredClause;
import br.com.anteros.persistence.dsl.osql.types.Path;

/**
 * UpdateClause defines a generic extensible interface for Update clauses
 *
 * @author tiwe
 *
 * @param <C> concrete subtype
 */
public interface UpdateClause<C extends UpdateClause<C>> extends StoreClause<C>, FilteredClause<C> {

    /**
     * Set the paths to be updated
     *
     * @param paths
     * @param values
     * @return
     */
    C set(List<? extends Path<?>> paths, List<?> values);

}
