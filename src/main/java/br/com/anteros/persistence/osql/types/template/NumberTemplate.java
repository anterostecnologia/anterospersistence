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
package br.com.anteros.persistence.osql.types.template;

import java.util.List;

import br.com.anteros.persistence.osql.types.Template;
import br.com.anteros.persistence.osql.types.TemplateExpression;
import br.com.anteros.persistence.osql.types.TemplateExpressionImpl;
import br.com.anteros.persistence.osql.types.TemplateFactory;
import br.com.anteros.persistence.osql.types.Visitor;
import br.com.anteros.persistence.osql.types.expression.NumberExpression;

import com.google.common.collect.ImmutableList;

/**
 * NumberTemplate defines custom numeric expressions
 *
 * @author tiwe
 *
 * @param <T> expression type
 */
public class NumberTemplate<T extends Number & Comparable<?>> extends NumberExpression<T> implements TemplateExpression<T> {

    private static final long serialVersionUID = 351057421752203377L;

    public static <T extends Number & Comparable<?>> NumberExpression<T> create(Class<T> type, String template) {
        return new NumberTemplate<T>(type, TemplateFactory.DEFAULT.create(template), ImmutableList.of());
    }
    
    public static <T extends Number & Comparable<?>> NumberExpression<T> create(Class<T> type, String template, Object one) {
        return new NumberTemplate<T>(type, TemplateFactory.DEFAULT.create(template), ImmutableList.of(one));
    }
    
    public static <T extends Number & Comparable<?>> NumberExpression<T> create(Class<T> type, String template, Object one, Object two) {
        return new NumberTemplate<T>(type, TemplateFactory.DEFAULT.create(template), ImmutableList.of(one, two));
    }
    
    public static <T extends Number & Comparable<?>> NumberExpression<T> create(Class<T> type, String template, Object... args) {
        return new NumberTemplate<T>(type, TemplateFactory.DEFAULT.create(template), ImmutableList.copyOf(args));
    }

    public static <T extends Number & Comparable<?>> NumberExpression<T> create(Class<T> type, Template template, Object... args) {
        return new NumberTemplate<T>(type, template, ImmutableList.copyOf(args));
    }

    public static final NumberExpression<Integer> ONE = create(Integer.class, "1");
    
    public static final NumberExpression<Integer> TWO = create(Integer.class, "2");
    
    public static final NumberExpression<Integer> THREE = create(Integer.class, "3");

    public static final NumberExpression<Integer> ZERO = create(Integer.class, "0");

    private final TemplateExpressionImpl<T> templateMixin;

    public NumberTemplate(Class<T> type, Template template, ImmutableList<?> args) {
        super(new TemplateExpressionImpl<T>(type, template, args));
        templateMixin = (TemplateExpressionImpl<T>)mixin;
    }

    
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(templateMixin, context);
    }
    
    
    public Object getArg(int index) {
        return templateMixin.getArg(index);
    }

    
    public List<?> getArgs() {
        return templateMixin.getArgs();
    }

    
    public Template getTemplate() {
        return templateMixin.getTemplate();
    }

}