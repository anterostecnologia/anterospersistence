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
package br.com.anteros.persistence.dsl.osql.group;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.anteros.persistence.dsl.osql.Projectable;
import br.com.anteros.persistence.dsl.osql.Tuple;
import br.com.anteros.persistence.dsl.osql.lang.CloseableIterator;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpressionUtils;
import br.com.anteros.persistence.dsl.osql.types.QTuple;

/**
 * Provides aggregated results as a map
 *
 * @author tiwe
 *
 * @param <K>
 * @param <V>
 */
public class GroupByMap<K,V> extends AbstractGroupByTransformer<K, Map<K,V>> {

    GroupByMap(Expression<K> key, Expression<?>... expressions) {
        super(key, expressions);
    }

    @Override
    public Map<K, V> transform(Projectable projectable) {
        Map<K, Group> groups = new LinkedHashMap<K, Group>();

        // create groups
        FactoryExpression<Tuple> expr = FactoryExpressionUtils.wrap(new QTuple(expressions));
        boolean hasGroups = false;
        for (Expression<?> e : expr.getArgs()) {
            hasGroups |= e instanceof GroupExpression;
        }
        if (hasGroups) {
            expr = withoutGroupExpressions(expr);
        }
        CloseableIterator<Tuple> iter = projectable.iterate(expr);
        try {
            while (iter.hasNext()) {
                Object[] row = iter.next().toArray();
                K groupId = (K) row[0];
                GroupImpl group = (GroupImpl)groups.get(groupId);
                if (group == null) {
                    group = new GroupImpl(groupExpressions, maps);
                    groups.put(groupId, group);
                }
                group.add(row);
            }
        } finally {
            iter.close();
        }

        // transform groups
        return transform(groups);

    }

    protected Map<K, V> transform(Map<K, Group> groups) {
        return (Map<K,V>)groups;
    }

}
