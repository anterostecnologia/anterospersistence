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
package br.com.anteros.persistence.dsl.osql.types.expr;



import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.ExpressionUtils;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.Predicate;

/**
 * BooleanExpression represents {@link java.lang.Boolean} expressions
 *
 * @author tiwe
 * @see java.lang.Boolean
 *
 */
public abstract class BooleanExpression extends ComparableExpression<Boolean> implements Predicate{

    private static final long serialVersionUID = 3797956062512074164L;

    
    private volatile BooleanExpression eqTrue, eqFalse;

    /**
     * Return the intersection of the given Boolean expressions
     *
     * @param exprs
     * @return
     */
    
    public static BooleanExpression allOf(BooleanExpression... exprs) {
        BooleanExpression rv = null;
        for (BooleanExpression b : exprs) {
            rv = rv == null ? b : rv.and(b);
        }
        return rv;
    }

    /**
     * Return the union of the given Boolean expressions
     *
     * @param exprs
     * @return
     */
    
    public static BooleanExpression anyOf(BooleanExpression... exprs) {
        BooleanExpression rv = null;
        for (BooleanExpression b : exprs) {
            rv = rv == null ? b : rv.or(b);
        }
        return rv;
    }

    
    private volatile BooleanExpression not;

    public BooleanExpression(Expression<Boolean> mixin) {
        super(mixin);
    }

    @Override
    public BooleanExpression as(Path<Boolean> alias) {
        return BooleanOperation.create(Ops.ALIAS, mixin, alias);
    }

    @Override
    public BooleanExpression as(String alias) {
        return as(new PathImpl<Boolean>(Boolean.class, alias));
    }

    /**
     * Get an intersection of this and the given expression
     *
     * @param right right hand side of the union
     * @return this && right
     */
    public BooleanExpression and( Predicate right) {
        right = (Predicate) ExpressionUtils.extract(right);
        if (right != null) {
            return BooleanOperation.create(Ops.AND, mixin, right);
        } else {
            return this;
        }
    }

    /**
     * Get an intersection of this and the union of the given predicates
     *
     * @param predicates
     * @return
     */
    public BooleanExpression andAnyOf(Predicate... predicates) {
        return and(ExpressionUtils.anyOf(predicates));
    }

    /**
     * Get a negation of this boolean expression
     *
     * @return !this
     */
    @Override
    public BooleanExpression not() {
        if (not == null) {
            // uses this, because it makes unwrapping easier
            not = BooleanOperation.create(Ops.NOT, this);
        }
        return not;
    }

    /**
     * Get a union of this and the given expression
     *
     * @param right right hand side of the union
     * @return this || right
     */
    public BooleanExpression or( Predicate right) {
        right = (Predicate) ExpressionUtils.extract(right);
        if (right != null) {
            return BooleanOperation.create(Ops.OR, mixin, right);
        } else {
            return this;
        }
    }

    /**
     * Get a union of this and the intersection of the given predicates
     *
     * @param predicates
     * @return
     */
    public BooleanExpression orAllOf(Predicate... predicates) {
        return or(ExpressionUtils.allOf(predicates));
    }

    /**
     * Get a this == true expression
     *
     * @return
     */
    public BooleanExpression isTrue() {
        return eq(true);
    }

    /**
     * Get a this == false expression
     *
     * @return
     */
    public BooleanExpression isFalse() {
        return eq(false);
    }

    @Override
    public BooleanExpression eq(Boolean right) {
        if (right.booleanValue()) {
            if (eqTrue == null) {
                eqTrue = super.eq(true);
            }
            return eqTrue;
        } else {
            if (eqFalse == null) {
                eqFalse = super.eq(false);
            }
            return eqFalse;
        }
    }
}
