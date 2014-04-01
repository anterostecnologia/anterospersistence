package br.com.anteros.persistence.osql.impl;

import java.util.List;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.CodeTemplate;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplateFactory;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class TemplateConditionImpl<T> extends AbstractCondition<T> implements CodeTemplateCondition<T> {

	private final ImmutableList<?> arguments;

	private final CodeTemplate codeTemplate;

	public static <C> Condition<C> create(Class<C> cl, String template) {
		return new TemplateConditionImpl<C>(cl, CodeTemplateFactory.DEFAULT.create(template), ImmutableList.of());
	}

	public static <C> Condition<C> create(Class<C> cl, String template, Object one) {
		return new TemplateConditionImpl<C>(cl, CodeTemplateFactory.DEFAULT.create(template), ImmutableList.of(one));
	}

	public static <C> Condition<C> create(Class<C> cl, String template, Object one, Object two) {
		return new TemplateConditionImpl<C>(cl, CodeTemplateFactory.DEFAULT.create(template), ImmutableList.of(one, two));
	}

	public static <C> Condition<C> create(Class<C> cl, String template, Object... arguments) {
		return new TemplateConditionImpl<C>(cl, CodeTemplateFactory.DEFAULT.create(template), arguments);
	}

	public static <C> Condition<C> create(Class<C> cl, CodeTemplate codeTemplate, Object... arguments) {
		return new TemplateConditionImpl<C>(cl, codeTemplate, arguments);
	}

	protected TemplateConditionImpl(Class<? extends T> type, CodeTemplate codeTemplate, Object... arguments) {
		this(type, codeTemplate, ImmutableList.copyOf(arguments));
	}

	public TemplateConditionImpl(Class<? extends T> type, CodeTemplate codeTemplate, ImmutableList<?> arguments) {
		super(type);
		this.arguments = arguments;
		this.codeTemplate = codeTemplate;
	}

	@Override
	public final Object getArgument(int index) {
		return getArguments().get(index);
	}

	@Override
	public final List<?> getArguments() {
		return arguments;
	}

	@Override
	public final CodeTemplate getTemplate() {
		return codeTemplate;
	}

	@Override
	public final boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof CodeTemplateCondition) {
			CodeTemplateCondition<?> c = (CodeTemplateCondition<?>) o;
			return c.getTemplate().equals(codeTemplate) && c.getType().equals(getType());
		} else {
			return false;
		}
	}

	@Override
	public final <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(this, context);
	}

}
