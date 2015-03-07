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


import java.util.UUID;

import br.com.anteros.persistence.dsl.osql.types.CollectionExpression;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.Operation;
import br.com.anteros.persistence.dsl.osql.types.OperationImpl;
import br.com.anteros.persistence.dsl.osql.types.Operator;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.PathMetadata;
import br.com.anteros.persistence.dsl.osql.types.PathMetadataFactory;
import br.com.anteros.persistence.dsl.osql.types.PathType;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.PredicateOperation;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpressionImpl;
import br.com.anteros.persistence.dsl.osql.types.Templates;
import br.com.anteros.persistence.dsl.osql.types.ToStringVisitor;
import br.com.anteros.persistence.dsl.osql.types.Visitor;
import br.com.anteros.persistence.dsl.osql.types.path.EntityPathBase;
import br.com.anteros.persistence.dsl.osql.types.path.ListPath;
import br.com.anteros.persistence.dsl.osql.types.path.SimplePath;
import br.com.anteros.persistence.dsl.osql.types.template.BooleanTemplate;

import com.google.common.collect.ImmutableList;

/**
 * CollectionAnyVisitor is an expression visitor which transforms any() path expressions which are
 * often transformed into subqueries
 *
 * @author tiwe
 *
 */
@SuppressWarnings("unchecked")
public class CollectionAnyVisitor implements Visitor<Expression<?>,Context> {

    public static final CollectionAnyVisitor DEFAULT = new CollectionAnyVisitor();

    public static final Templates TEMPLATES = new Templates() {
    {
        add(PathType.PROPERTY, "{0}_{1}");
        add(PathType.COLLECTION_ANY, "{0}");
    }};


    @SuppressWarnings("rawtypes")
    private static <T> Path<T> replaceParent(Path<T> path, Path<?> parent) {
        PathMetadata<?> metadata = new PathMetadata<Object>(parent, path.getMetadata().getElement(),
                path.getMetadata().getPathType());
        if (path instanceof CollectionExpression) {
            CollectionExpression<?,?> col = (CollectionExpression<?,?>)path;
            return new ListPath(col.getParameter(0), SimplePath.class, metadata);
        } else {
            return new PathImpl<T>(path.getType(), metadata);
        }
    }

    @Override
    public Expression<?> visit(Constant<?> expr, Context context) {
        return expr;
    }

    @Override
    public Expression<?> visit(TemplateExpression<?> expr, Context context) {
        Object[] args = new Object[expr.getArgs().size()];
        for (int i = 0; i < args.length; i++) {
            Context c = new Context();
            if (expr.getArg(i) instanceof Expression) {
                args[i] = ((Expression<?>)expr.getArg(i)).accept(this, c);
            } else {
                args[i] = expr.getArg(i);
            }
            context.add(c);
        }
        if (context.replace) {
            if (expr.getType().equals(Boolean.class)) {
                Predicate predicate = BooleanTemplate.create(expr.getTemplate(), args);
                return !context.paths.isEmpty() ? exists(context, predicate) : predicate;
            } else {
                return TemplateExpressionImpl.create(expr.getType(), expr.getTemplate(), args);
            }
        } else {
            return expr;
        }
    }

    @Override
    public Expression<?> visit(FactoryExpression<?> expr, Context context) {
        return expr;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Expression<?> visit(Operation<?> expr, Context context) {
        Expression<?>[] args = new Expression<?>[expr.getArgs().size()];
        for (int i = 0; i < args.length; i++) {
            Context c = new Context();
            args[i] = expr.getArg(i).accept(this, c);
            context.add(c);
        }
        if (context.replace) {
            if (expr.getType().equals(Boolean.class)) {
                Predicate predicate = new PredicateOperation((Operator<Boolean>)expr.getOperator(), ImmutableList.copyOf(args));
                return !context.paths.isEmpty() ? exists(context, predicate) : predicate;
            } else {
                return new OperationImpl(expr.getType(), expr.getOperator(), ImmutableList.copyOf(args));
            }
        } else {
            return expr;
        }
    }

    protected Predicate exists(Context c, Predicate condition) {
        return condition;
    }

    @Override
    public Expression<?> visit(Path<?> expr, Context context) {
        if (expr.getMetadata().getPathType() == PathType.COLLECTION_ANY) {
            Path<?> parent = (Path<?>) expr.getMetadata().getParent().accept(this, context);
            expr = new PathImpl<Object>(expr.getType(), PathMetadataFactory.forCollectionAny(parent));
            String variable = expr.accept(ToStringVisitor.DEFAULT, TEMPLATES).replace('.', '_');
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0,5);
            EntityPath<?> replacement = new EntityPathBase<Object>(expr.getType(), variable + suffix);
            context.add(expr, replacement);
            return replacement;

        } else if (expr.getMetadata().getParent() != null) {
            Context c = new Context();
            Path<?> parent = (Path<?>) expr.getMetadata().getParent().accept(this, c);
            if (c.replace) {
                context.add(c);
                return replaceParent(expr, parent);
            }
        }
        return expr;
    }

    @Override
    public Expression<?> visit(SubQueryExpression<?> expr, Context context) {
        return expr;
    }

    @Override
    public Expression<?> visit(ParamExpression<?> expr, Context context) {
        return expr;
    }

    protected CollectionAnyVisitor() {}

}
