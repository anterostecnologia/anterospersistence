/*******************************************************************************
 * Copyright 2011, Mysema Ltd
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql.support;

import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.dsl.osql.DefaultQueryMetadata;
import br.com.anteros.persistence.dsl.osql.JoinExpression;
import br.com.anteros.persistence.dsl.osql.JoinFlag;
import br.com.anteros.persistence.dsl.osql.QueryFlag;
import br.com.anteros.persistence.dsl.osql.QueryMetadata;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpressionUtils;
import br.com.anteros.persistence.dsl.osql.types.Operation;
import br.com.anteros.persistence.dsl.osql.types.OperationImpl;
import br.com.anteros.persistence.dsl.osql.types.Operator;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.PathMetadata;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.PredicateOperation;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpressionImpl;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpressionImpl;
import br.com.anteros.persistence.dsl.osql.types.Visitor;
import br.com.anteros.persistence.dsl.osql.types.template.BooleanTemplate;

import com.google.common.collect.ImmutableList;

/**
 * ReplaceVisitor is a deep visitor that can be customized to replace segments of
 * expression trees
 */
public class ReplaceVisitor implements Visitor<Expression<?>, Void> {

    @Override
    public Expression<?> visit(Constant<?> expr,  Void context) {
        return expr;
    }

    @Override
    public Expression<?> visit(FactoryExpression<?> expr,  Void context) {
        List<Expression<?>> args = visit(expr.getArgs());
        if (args.equals(expr.getArgs())) {
            return expr;
        } else {
            return FactoryExpressionUtils.wrap(expr, args);
        }
    }

    @Override
    public Expression<?> visit(Operation<?> expr,  Void context) {
        ImmutableList<Expression<?>> args = visit(expr.getArgs());
        if (args.equals(expr.getArgs())) {
            return expr;
        } else if (expr instanceof Predicate) {
            return new PredicateOperation((Operator)expr.getOperator(), args);
        } else {
            return new OperationImpl(expr.getType(), expr.getOperator(), args);
        }
    }

    @Override
    public Expression<?> visit(ParamExpression<?> expr,  Void context) {
        return expr;
    }

    @Override
    public Expression<?> visit(Path<?> expr,  Void context) {
        if (expr.getMetadata().isRoot()) {
            return expr;
        } else {
            PathMetadata metadata = expr.getMetadata();
            Path<?> parent = (Path)metadata.getParent().accept(this, null);
            if (parent.equals(metadata.getParent())) {
                return expr;
            } else {
                metadata = new PathMetadata(parent, metadata.getElement(),
                        metadata.getPathType());
                return new PathImpl(expr.getType(), metadata);
            }
        }
    }

    @Override
    public Expression<?> visit(SubQueryExpression<?> expr,  Void context) {
        QueryMetadata md = new DefaultQueryMetadata();
        md.setDistinct(expr.getMetadata().isDistinct());
        md.setModifiers(expr.getMetadata().getModifiers());
        md.setUnique(expr.getMetadata().isUnique());
        for (QueryFlag flag : expr.getMetadata().getFlags()) {
            md.addFlag(new QueryFlag(flag.getPosition(), flag.getFlag().accept(this, null)));
        }
        for (Expression<?> e : expr.getMetadata().getGroupBy()) {
            md.addGroupBy(e.accept(this, null));
        }
        Predicate having = expr.getMetadata().getHaving();
        if (having != null) {
            md.addHaving((Predicate)having.accept(this, null));
        }
        for (JoinExpression je : expr.getMetadata().getJoins()) {
            md.addJoin(je.getType(), je.getTarget().accept(this, null));
            if (je.getCondition() != null) {
                md.addJoinCondition((Predicate)je.getCondition().accept(this, null));
            }
            for (JoinFlag jf : je.getFlags()) {
                md.addJoinFlag(new JoinFlag(jf.getFlag().accept(this, null), jf.getPosition()));
            }
        }
        for (OrderSpecifier<?> os : expr.getMetadata().getOrderBy()) {
            OrderSpecifier<?> os2 = new OrderSpecifier(os.getOrder(), os.getTarget().accept(this,
                    null));
            switch (os.getNullHandling()) {
                case NullsFirst: os2 = os2.nullsFirst(); break;
                case NullsLast: os2 = os2.nullsLast(); break;
            }
            md.addOrderBy(os2);
        }
        for (Map.Entry<ParamExpression<?>, Object> entry : expr.getMetadata().getParams()
                .entrySet()) {
            md.setParam((ParamExpression)entry.getKey().accept(this, null), entry.getValue());
        }
        for (Expression<?> e : expr.getMetadata().getProjection()) {
            md.addProjection(e.accept(this, null));
        }
        Predicate where = expr.getMetadata().getWhere();
        if (where != null) {
           md.addWhere((Predicate)where.accept(this, null));
        }
        if (expr.getMetadata().equals(md)) {
            return expr;
        } else {
            return new SubQueryExpressionImpl(expr.getType(), md);
        }
    }

    @Override
    public Expression<?> visit(TemplateExpression<?> expr,  Void context) {
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Object arg : expr.getArgs()) {
            if (arg instanceof Expression) {
                builder.add(((Expression)arg).accept(this, null));
            } else {
                builder.add(arg);
            }
        }
        ImmutableList args = builder.build();
        if (args.equals(expr.getArgs())) {
            return expr;
        } else {
            if (expr instanceof Predicate) {
                return BooleanTemplate.create(expr.getTemplate(), args);
            } else {
                return new TemplateExpressionImpl(expr.getType(), expr.getTemplate(), args);
            }
        }
    }

    private ImmutableList<Expression<?>> visit(List<Expression<?>> args) {
        ImmutableList.Builder<Expression<?>> builder = ImmutableList.builder();
        for (Expression<?> arg : args) {
            builder.add(arg.accept(this, null));
        }
        return builder.build();
    }
}
