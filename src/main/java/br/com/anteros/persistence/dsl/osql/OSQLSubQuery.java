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

import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;


/**
 * SQLSubQuery is a subquery implementation for SQL queries
 *
 * @author tiwe modified by: Edson Martins
 *
 */
public class OSQLSubQuery extends AbstractOSQLSubQuery<OSQLSubQuery> implements Cloneable {

    public OSQLSubQuery(DatabaseDialect dialect, EntityCacheManager entityCacheManager, SQLTemplates templates, QueryMetadata metadata) {
        super(dialect, entityCacheManager, templates, metadata);
    }

    @Override
    public OSQLSubQuery clone() {
        OSQLSubQuery subQuery = new OSQLSubQuery(dialect, entityCacheManager, templates, getMetadata());
        return subQuery;
    }

}
