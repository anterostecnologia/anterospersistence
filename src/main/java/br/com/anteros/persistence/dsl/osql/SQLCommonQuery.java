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
package br.com.anteros.persistence.dsl.osql;


import br.com.anteros.persistence.dsl.osql.QueryFlag.Position;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;

/**
 * SQLCommonQuery is a common interface for SQLQuery and SQLSubQuery
 *
 * @author tiwe
 *
 * @param <Q> concrete type
 */
public interface SQLCommonQuery<Q extends SQLCommonQuery<Q>> extends Query<Q> {

    /**
     * Add the given Expression as a query flag
     *
     * @param position
     * @param flag
     * @return
     */
    Q addFlag(Position position, Expression<?> flag);

    /**
     * Add the given String literal as query flag
     *
     * @param position
     * @param flag
     * @return
     */
    Q addFlag(Position position, String flag);

    /**
     * Add the given prefix and expression as a general query flag
     *
     * @param position position of the flag
     * @param prefix prefix for the flag
     * @param expr expression of the flag
     * @return
     */
    Q addFlag(Position position, String prefix, Expression<?> expr);

    /**
     * Add the given String literal as a join flag to the last added join with the
     * position BEFORE_TARGET
     *
     * @param flag
     * @return
     */
    Q addJoinFlag(String flag);

    /**
     * Add the given String literal as a join flag to the last added join
     *
     * @param flag
     * @param position
     * @return
     */
    Q addJoinFlag(String flag, JoinFlag.Position position);

    /**
     * Defines the sources of the query
     *
     * @param o
     * @return
     */
    Q from(Expression<?>... o);

    /**
     * Adds a sub query source
     *
     * @param subQuery
     * @param alias
     * @return
     */
    Q from(SubQueryExpression<?> subQuery, Path<?> alias);

    /**
     * Adds a full join to the given target
     *
     * @param o
     * @return
     */
    Q fullJoin(EntityPath<?> o);

    /**
     * Adds a full join to the given target
     *
     * @param o
     * @return
     */
    Q fullJoin(SubQueryExpression<?> o, Path<?> alias);

    /**
     * Adds an inner join to the given target
     *
     * @param o
     * @return
     */
    Q innerJoin(EntityPath<?> o);

    /**
     * Adds an inner join to the given target
     *
     * @param o
     * @return
     */
    Q innerJoin(SubQueryExpression<?> o, Path<?> alias);

    /**
     * Adds a join to the given target
     *
     * @param o
     * @return
     */
    Q join(EntityPath<?> o);

    /**
     * Adds a join to the given target
     *
     * @param o
     * @return
     */
    Q join(SubQueryExpression<?> o, Path<?> alias);

    /**
     * Adds a left join to the given target
     *
     * @param o
     * @return
     */
    Q leftJoin(EntityPath<?> o);

    /**
     * Adds a left join to the given target
     *
     * @param o
     * @return
     */
    Q leftJoin(SubQueryExpression<?> o, Path<?> alias);

    /**
     * Defines a filter to the last added join
     *
     * @param conditions
     * @return
     */
    Q on(Predicate... conditions);

    /**
     * Adds a right join to the given target
     *
     * @param o
     * @return
     */
    Q rightJoin(EntityPath<?> o);

    /**
     * Adds a right join to the given target
     *
     * @param o
     * @return
     */
    Q rightJoin(SubQueryExpression<?> o, Path<?> alias);

    /**
     * Adds a common table expression
     *
     * @return
     */
    Q with(Path<?> alias, SubQueryExpression<?> o);

    /**
     * Adds a common table expression
     *
     * @param alias
     * @param query
     * @return
     */
    Q with(Path<?> alias, Expression<?> query);

    /**
     * Adds a common table expression
     *
     * @param alias
     * @param columns
     * @return
     */
    WithBuilder<Q> with(Path<?> alias, Path<?>... columns);

    /**
     * Adds a common table expression
     *
     * @return
     */
    Q withRecursive(Path<?> alias, SubQueryExpression<?> o);

    /**
     * Adds a common table expression
     *
     * @param alias
     * @param query
     * @return
     */
    Q withRecursive(Path<?> alias, Expression<?> query);

    /**
     * Adds a common table expression
     *
     * @param alias
     * @param columns
     * @return
     */
    WithBuilder<Q> withRecursive(Path<?> alias, Path<?>... columns);
}
