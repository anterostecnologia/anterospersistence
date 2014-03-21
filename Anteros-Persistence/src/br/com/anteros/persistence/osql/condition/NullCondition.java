package br.com.anteros.persistence.osql.condition;

import java.util.Collections;

import br.com.anteros.persistence.osql.impl.TemplateConditionImpl;

public class NullCondition<T> extends TemplateConditionImpl<T> {

    private static final CodeTemplate NULL_TEMPLATE = CodeTemplateFactory.DEFAULT.create("null");

    private static final long serialVersionUID = -5311968198973316411L;

    public static final NullCondition<Object> DEFAULT = new NullCondition<Object>(Object.class);

    public NullCondition(Class<? extends T> type) {
        super(type, NULL_TEMPLATE, Collections.<Condition<?>>emptyList());
    }

}
