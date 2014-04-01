package br.com.anteros.persistence.osql.condition;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.impl.OperationImpl;
import br.com.anteros.persistence.osql.operation.Operation;

import com.google.common.collect.ImmutableList;

public class ComparableOperation<T extends Comparable<?>> extends ComparableCondition<T> implements
		Operation<T> {

	public static <D extends Comparable<?>> ComparableCondition<D> create(Class<D> type, Operator<? super D> operator,
			Condition<?>... arguments) {
		return new ComparableOperation<D>(type, operator, arguments);
	}

	private final OperationImpl<T> sourceOperation;

	protected ComparableOperation(Class<T> type, Operator<? super T> operator, Condition<?>... arguments) {
		this(type, operator, ImmutableList.copyOf(arguments));
	}

	protected ComparableOperation(Class<T> type, Operator<? super T> operator, ImmutableList<Condition<?>> arguments) {
		super(new OperationImpl<T>(type, operator, arguments));
		this.sourceOperation = (OperationImpl<T>) sourceCondition;
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
