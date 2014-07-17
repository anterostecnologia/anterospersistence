/*
 * Copyright 2013, Mysema Ltd
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
package br.com.anteros.persistence.osql.group;

import java.util.NoSuchElementException;

import br.com.anteros.persistence.osql.Projectable;
import br.com.anteros.persistence.osql.Tuple;
import br.com.anteros.persistence.osql.lang.CloseableIterator;
import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.FactoryExpression;
import br.com.anteros.persistence.osql.types.FactoryExpressionUtils;
import br.com.anteros.persistence.osql.types.QTuple;

import com.google.common.base.Objects;

/**
 * Provides aggregated results as an iterator
 *
 * @author tiwe
 *
 * @param <K>
 * @param <V>
 */
public class GroupByIterate<K, V> extends AbstractGroupByTransformer<K, CloseableIterator<V>> {

    GroupByIterate(Expression<K> key, Expression<?>... expressions) {
        super(key, expressions);
    }

    public CloseableIterator<V> transform(Projectable projectable) {
        // create groups
        FactoryExpression<Tuple> expr = FactoryExpressionUtils.wrap(new QTuple(expressions));
        boolean hasGroups = false;
        for (Expression<?> e : expr.getArgs()) {
            hasGroups |= e instanceof GroupExpression;
        }
        if (hasGroups) {
            expr = withoutGroupExpressions(expr);
        }
        final CloseableIterator<Tuple> iter = projectable.iterate(expr);

        return new CloseableIterator<V>() {

            private GroupImpl group;

            private K groupId;

            public boolean hasNext() {
                return group != null || iter.hasNext();
            }

            public V next() {
                if (!iter.hasNext()) {
                    if (group != null) {
                        Group current = group;
                        group = null;
                        return transform(current);
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                while (iter.hasNext()) {
                    Object[] row = iter.next().toArray();
                    if (group == null) {
                        group = new GroupImpl(groupExpressions, maps);
                        groupId = (K) row[0];
                        group.add(row);
                    } else if (Objects.equal(groupId, row[0])) {
                        group.add(row);
                    } else {
                        Group current = group;
                        group = new GroupImpl(groupExpressions, maps);
                        groupId = (K) row[0];
                        group.add(row);
                        return transform(current);
                    }
                }
                Group current = group;
                group = null;
                return transform(current);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void close() {
                iter.close();
            }

        };
    }

    protected V transform(Group group) {
        return (V)group;
    }
}
