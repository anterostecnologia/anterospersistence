package br.com.anteros.persistence.osql.operation;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.NumberCondition;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.collect.ImmutableList;

public class NumberOperation<T extends Number & Comparable<?>> extends NumberCondition<T> implements
		Operation<T> {

	public static <D extends Number & Comparable<?>> NumberCondition<D> create(Class<? extends D> type,
			Operator<? super D> operator, Condition<?> one) {
		return new NumberOperation<D>(type, operator, ImmutableList.<Condition<?>> of(one));
	}

	public static <D extends Number & Comparable<?>> NumberCondition<D> create(Class<? extends D> type,
			Operator<? super D> operator, Condition<?> one, Condition<?> two) {
		return new NumberOperation<D>(type, operator, ImmutableList.of(one, two));
	}

	public static <D extends Number & Comparable<?>> NumberCondition<D> create(Class<? extends D> type,
			Operator<? super D> operator, Condition<?>... arguments) {
		return new NumberOperation<D>(type, operator, arguments);
	}

	private final OperationImpl<T> sourceOperation;

	protected NumberOperation(Class<? extends T> type, Operator<? super T> operator, Condition<?>... arguments) {
		this(type, operator, ImmutableList.copyOf(arguments));
	}

	protected NumberOperation(Class<? extends T> type, Operator<? super T> operator, ImmutableList<Condition<?>> arguments) {
		super(new OperationImpl<T>(type, operator, arguments));
		this.sourceOperation = (OperationImpl<T>) sourceCondition;
	}

	@Override
	public NumberCondition<T> negate() {
		if (sourceOperation.getOperator() == Operators.NEGATE) {
			return (NumberCondition<T>) sourceOperation.getArgument(0);
		} else {
			return super.negate();
		}
	}

	@Override
	public final <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(sourceOperation, context);
	}

	@Override
	public Condition<?> getArgument(int index) {
		return sourceOperation.getArgument(index);
	}

	@Override
	public List<Condition<?>> getArguments() {
		return sourceOperation.getArguments();
	}

	@Override
	public Operator<? super T> getOperator() {
		return sourceOperation.getOperator();
	}

}
