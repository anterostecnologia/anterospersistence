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
 * Abstract superclass for SubQuery implementations
 *
 * @author tiwe modified by: Edson Martins
 *
 */
public abstract class AbstractOSQLSubQuery<Q extends AbstractOSQLSubQuery<Q>> extends DetachableSQLQuery<Q>
{

	protected EntityCacheManager entityCacheManager;
	protected DatabaseDialect dialect;
	
    public AbstractOSQLSubQuery(DatabaseDialect dialect, EntityCacheManager entityCacheManager, SQLTemplates templates, QueryMetadata metadata)
    {
        super(templates, metadata);
        this.entityCacheManager = entityCacheManager;
        this.dialect = dialect;
    }

    protected SQLSerializer createSerializer()
    {
        return new SQLSerializer(dialect, entityCacheManager, templates);
    }

}
