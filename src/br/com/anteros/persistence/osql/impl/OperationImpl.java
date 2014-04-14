package br.com.anteros.persistence.osql.impl;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.operation.Operation;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class OperationImpl<T> extends AbstractCondition<T> implements Operation<T> {

	private final ImmutableList<Condition<?>> arguments;

	private final Operator<? super T> operator;

	public static <RT> Operation<RT> create(Class<? extends RT> type, Operator<? super RT> operator,
			Condition<?> one) {
		return new OperationImpl<RT>(type, operator, ImmutableList.<Condition<?>> of(one));
	}

	public static <RT> Operation<RT> create(Class<? extends RT> type, Operator<? super RT> operator,
			Condition<?> one, Condition<?> two) {
		return new OperationImpl<RT>(type, operator, ImmutableList.of(one, two));
	}

	protected OperationImpl(Class<? extends T> type, Operator<? super T> operator, Condition<?>... arguments) {
		this(type, operator, ImmutableList.copyOf(arguments));
	}

	public OperationImpl(Class<? extends T> type, Operator<? super T> operator, ImmutableList<Condition<?>> arguments) {
		super(type);
		this.operator = operator;
		this.arguments = arguments;
	}

	@Override
	public final Condition<?> getArgument(int i) {
		return arguments.get(i);
	}

	@Override
	public final List<Condition<?>> getArguments() {
		return arguments;
	}

	@Override
	public final Operator<? super T> getOperator() {
		return operator;
	}

	@Override
	public final boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof Operation<?>) {
			Operation<?> op = (Operation<?>) o;
			return op.getOperator() == operator && op.getArguments().equals(arguments)
					&& op.getType().equals(getType());
		} else {
			return false;
		}
	}

	@Override
	public final <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(this, context);
	}

}
