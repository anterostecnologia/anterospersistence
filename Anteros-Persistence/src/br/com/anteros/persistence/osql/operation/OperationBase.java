package br.com.anteros.persistence.osql.operation;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class OperationBase<T> extends AbstractCondition<T> implements Operation<T> {

	public static <D> OperationBase<D> create(Class<D> type, Operator<? super D> operator, Condition<?> one) {
		return new OperationBase<D>(type, operator, ImmutableList.<Condition<?>> of(one));
	}

	public static <D> OperationBase<D> create(Class<D> type, Operator<? super D> operator, Condition<?> one,
			Condition<?> two) {
		return new OperationBase<D>(type, operator, ImmutableList.of(one, two));
	}

	public static <D> OperationBase<D> create(Class<D> type, Operator<? super D> operator, Condition<?>... arguments) {
		return new OperationBase<D>(type, operator, arguments);
	}

	private final OperationImpl<T> sourceOperation;

	protected OperationBase(Class<T> type, Operator<? super T> operator, Condition<?>... args) {
		this(type, operator, ImmutableList.copyOf(args));
	}

	protected OperationBase(Class<T> type, Operator<? super T> operator, ImmutableList<Condition<?>> args) {
		super(new OperationImpl<T>(type, operator, args));
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
