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

import java.sql.Time;
import java.util.Date;

import br.com.anteros.persistence.dsl.osql.QueryMetadata;
import br.com.anteros.persistence.dsl.osql.Tuple;
import br.com.anteros.persistence.dsl.osql.types.ConstantImpl;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.NullExpression;
import br.com.anteros.persistence.dsl.osql.types.Operator;
import br.com.anteros.persistence.dsl.osql.types.Ops;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.PathMetadataFactory;
import br.com.anteros.persistence.dsl.osql.types.Template;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.CaseBuilder;
import br.com.anteros.persistence.dsl.osql.types.expr.ComparableExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.ComparableOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.DateExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.DateOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.DateTimeExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.DateTimeOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.DslExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.DslOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.NumberExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.NumberOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.SimpleExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.SimpleOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.StringExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.StringOperation;
import br.com.anteros.persistence.dsl.osql.types.expr.TimeExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.TimeOperation;
import br.com.anteros.persistence.dsl.osql.types.path.BooleanPath;
import br.com.anteros.persistence.dsl.osql.types.path.ComparablePath;
import br.com.anteros.persistence.dsl.osql.types.path.DatePath;
import br.com.anteros.persistence.dsl.osql.types.path.DateTimePath;
import br.com.anteros.persistence.dsl.osql.types.path.DslPath;
import br.com.anteros.persistence.dsl.osql.types.path.NumberPath;
import br.com.anteros.persistence.dsl.osql.types.path.SimplePath;
import br.com.anteros.persistence.dsl.osql.types.path.StringPath;
import br.com.anteros.persistence.dsl.osql.types.path.TimePath;
import br.com.anteros.persistence.dsl.osql.types.query.ExtendedSubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.query.SimpleSubQuery;
import br.com.anteros.persistence.dsl.osql.types.template.BooleanTemplate;
import br.com.anteros.persistence.dsl.osql.types.template.ComparableTemplate;
import br.com.anteros.persistence.dsl.osql.types.template.DslTemplate;
import br.com.anteros.persistence.dsl.osql.types.template.NumberTemplate;
import br.com.anteros.persistence.dsl.osql.types.template.SimpleTemplate;
import br.com.anteros.persistence.dsl.osql.types.template.StringTemplate;

/**
 * Expression factory class
 *
 * @author tiwe
 *
 */
public final class Expressions {

    @SuppressWarnings("unchecked")
    public static <D> SimpleExpression<D> as(Expression<D> source, Path<D> alias) {
        if (source == null) {
            return as((Expression)NullExpression.DEFAULT, alias);
        } else {
            return SimpleOperation.create((Class<D>)alias.getType(), Ops.ALIAS, source, alias);
        }
    }

    /**
     * Get an expression representing the current date as a DateExpression instance
     *
     * @return
     */
    public static DateExpression<Date> currentDate() {
        return DateExpression.currentDate();
    }

    /**
     * Get an expression representing the current time instant as a DateTimeExpression instance
     *
     * @return
     */
    public static DateTimeExpression<Date> currentTimestamp() {
        return DateTimeExpression.currentTimestamp();
    }

    /**
     * Get an expression representing the current time as a TimeExpression instance
     *
     * @return
     */
    public static TimeExpression<Time> currentTime() {
        return TimeExpression.currentTime();
    }

    /**
     * Create the alias expression source as alias
     *
     * @param source
     * @param alias
     * @return
     */
    public static <D> SimpleExpression<D> as(Expression<D> source, String alias) {
        return as(source, new PathImpl<D>(source.getType(), alias));
    }

    /**
     * Get the intersection of the given Boolean expressions
     *
     * @param exprs
     * @return
     */
    
    public static BooleanExpression allOf(BooleanExpression... exprs) {
        return BooleanExpression.allOf(exprs);
    }

    /**
     * Get the union of the given Boolean expressions
     *
     * @param exprs
     * @return
     */
    
    public static BooleanExpression anyOf(BooleanExpression... exprs) {
        return BooleanExpression.anyOf(exprs);
    }

    /**
     * Create a Constant expression for the given value
     *
     * @param value
     * @return
     */
    public static <T> Expression<T> constant(T value) {
        return ConstantImpl.create(value);
    }

    /**
     * Get the alias expression source as alias
     *
     * @param source
     * @param alias
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <D> SimpleExpression<D> constantAs(D source, Path<D> alias) {
        if (source == null) {
            return as((Expression)NullExpression.DEFAULT, alias);
        } else {
            return as(ConstantImpl.create(source), alias);
        }
    }

    /**
     * Create a new Template expression
     *
     * @param cl
     * @param template
     * @param args
     * @return
     */
    public static <T> SimpleExpression<T> template(Class<T> cl, String template, Object... args) {
        return SimpleTemplate.create(cl, template, args);
    }

    /**
     * Create a new Template expression
     *
     * @param cl
     * @param template
     * @param args
     * @return
     */
    public static <T> SimpleExpression<T> template(Class<T> cl, Template template, Object... args) {
        return SimpleTemplate.create(cl, template, args);
    }

    /**
     * Create a new Template expression
     *
     * @param cl
     * @param template
     * @param args
     * @return
     */
    public static <T> DslExpression<T> dslTemplate(Class<T> cl, String template, Object... args) {
        return DslTemplate.create(cl, template, args);
    }

    /**
     * Create a new Template expression
     *
     * @param cl
     * @param template
     * @param args
     * @return
     */
    public static <T extends Comparable<?>> ComparableExpression<T> comparableTemplate(Class<T> cl,
            String template, Object... args) {
        return ComparableTemplate.create(cl, template, args);
    }

    /**
     * Create a new Template expression
     *
     * @param cl
     * @param template
     * @param args
     * @return
     */
    public static <T extends Number & Comparable<?>> NumberExpression<T> numberTemplate(Class<T> cl,
            String template, Object... args) {
        return NumberTemplate.create(cl, template, args);
    }

    /**
     * Create a new Template expression
     *
     * @param template
     * @param args
     * @return
     */
    public static StringExpression stringTemplate(String template, Object... args) {
        return StringTemplate.create(template, args);
    }

    /**
     * Create a new Template expression
     *
     * @param template
     * @param args
     * @return
     */
    public static BooleanExpression booleanTemplate(String template, Object... args) {
        return BooleanTemplate.create(template, args);
    }

    /**
     * Create a new SubQuery expression
     *
     * @param type
     * @param metadata
     * @return
     */
    public static <T> ExtendedSubQueryExpression<T> subQuery(Class<T> type, QueryMetadata metadata) {
        return new SimpleSubQuery<T>(type, metadata);
    }

    /**
     * Create a new Predicate operation
     *
     * @param operation
     * @param args
     * @return
     */
    public static BooleanExpression predicate(Operator<Boolean> operation, Expression<?>... args) {
        return BooleanOperation.create(operation, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T> SimpleExpression<T> operation(Class<T> type, Operator<? super T> operator,
            Expression<?>... args) {
        return SimpleOperation.create(type, operator, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T> DslExpression<T> dslOperation(Class<T> type, Operator<? super T> operator,
            Expression<?>... args) {
        return DslOperation.create(type, operator, args);
    }

    /**
     * Create a new Boolean operation
     *
     * @param operation
     * @param args
     * @return
     */
    public static BooleanExpression booleanOperation(Operator<Boolean> operation, Expression<?>... args) {
        return predicate(operation, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T extends Comparable<?>> ComparableExpression<T> comparableOperation(Class<T> type,
            Operator<? super T> operator, Expression<?>... args) {
        return ComparableOperation.create(type, operator, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T extends Comparable<?>> DateExpression<T> dateOperation(Class<T> type,
            Operator<? super T> operator, Expression<?>... args) {
        return DateOperation.create(type, operator, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T extends Comparable<?>> DateTimeExpression<T> dateTimeOperation(Class<T> type,
            Operator<? super T> operator, Expression<?>... args) {
        return DateTimeOperation.create(type, operator, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T extends Comparable<?>> TimeExpression<T> timeOperation(Class<T> type,
            Operator<? super T> operator, Expression<?>... args) {
        return TimeOperation.create(type, operator, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param type
     * @param operator
     * @param args
     * @return
     */
    public static <T extends Number & Comparable<?>> NumberExpression<T> numberOperation(Class<T> type,
            Operator<? super T> operator, Expression<?>... args) {
        return NumberOperation.create(type, operator, args);
    }

    /**
     * Create a new Operation expression
     *
     * @param operator
     * @param args
     * @return
     */
    public static StringExpression stringOperation(Operator<? super String> operator, Expression<?>... args) {
        return StringOperation.create(operator, args);
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T> SimplePath<T> path(Class<T> type, String variable) {
        return new SimplePath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T> SimplePath<T> path(Class<T> type, Path<?> parent, String property) {
        return new SimplePath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T> DslPath<T> dslPath(Class<T> type, String variable) {
        return new DslPath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T> DslPath<T> dslPath(Class<T> type, Path<?> parent, String property) {
        return new DslPath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T extends Comparable<?>> ComparablePath<T> comparablePath(Class<T> type,
            String variable) {
        return new ComparablePath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T extends Comparable<?>> ComparablePath<T> comparablePath(Class<T> type,
            Path<?> parent, String property) {
        return new ComparablePath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T extends Comparable<?>> DatePath<T> datePath(Class<T> type, String variable) {
        return new DatePath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T extends Comparable<?>> DatePath<T> datePath(Class<T> type, Path<?> parent,
            String property) {
        return new DatePath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T extends Comparable<?>> DateTimePath<T> dateTimePath(Class<T> type, String variable) {
        return new DateTimePath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T extends Comparable<?>> DateTimePath<T> dateTimePath(Class<T> type, Path<?> parent,
            String property) {
        return new DateTimePath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T extends Comparable<?>> TimePath<T> timePath(Class<T> type, String variable) {
        return new TimePath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T extends Comparable<?>> TimePath<T> timePath(Class<T> type, Path<?> parent,
            String property) {
        return new TimePath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param variable
     * @return
     */
    public static <T extends Number & Comparable<?>> NumberPath<T> numberPath(Class<T> type,
            String variable) {
        return new NumberPath<T>(type, PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param type
     * @param parent
     * @param property
     * @return
     */
    public static <T extends Number & Comparable<?>> NumberPath<T> numberPath(Class<T> type,
            Path<?> parent, String property) {
        return new NumberPath<T>(type, PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param variable
     * @return
     */
    public static StringPath stringPath(String variable) {
        return new StringPath(PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param parent
     * @param property
     * @return
     */
    public static StringPath stringPath(Path<?> parent, String property) {
        return new StringPath(PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Create a new Path expression
     *
     * @param variable
     * @return
     */
    public static BooleanPath booleanPath(String variable) {
        return new BooleanPath(PathMetadataFactory.forVariable(variable));
    }

    /**
     * Create a new Path expression
     *
     * @param parent
     * @param property
     * @return
     */
    public static BooleanPath booleanPath(Path<?> parent, String property) {
        return new BooleanPath(PathMetadataFactory.forProperty(parent, property));
    }

    /**
     * Get a builder for a case expression
     *
     * @return
     */
    public static CaseBuilder cases() {
        return new CaseBuilder();
    }

    /**
     * Combine the given expressions into a list expression
     *
     * @param exprs
     * @return
     */
    public static SimpleExpression<Tuple> list(SimpleExpression<?>... exprs) {
        return list(Tuple.class, exprs);
    }

    /**
     * Combine the given expressions into a list expression
     *
     * @param clazz
     * @param exprs
     * @return
     */
    public static <T> SimpleExpression<T> list(Class<T> clazz, SimpleExpression<?>... exprs) {
        SimpleExpression<T> rv = (SimpleExpression<T>)exprs[0];
        for (int i = 1; i < exprs.length; i++) {
            rv = SimpleOperation.create(clazz, Ops.LIST, rv, exprs[i]);
        }
        return rv;
    }

    private Expressions() {}

}
