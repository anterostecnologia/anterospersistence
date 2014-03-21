package br.com.anteros.persistence.osql.operation;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.StringCondition;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class StringOperation extends StringCondition implements Operation<String> {

	public static StringCondition create(Operator<? super String> operator, Condition<?> one) {
		return new StringOperation(operator, ImmutableList.<Condition<?>> of(one));
	}

	public static StringCondition create(Operator<? super String> operator, Condition<?> one, Condition<?> two) {
		return new StringOperation(operator, ImmutableList.of(one, two));
	}

	public static StringCondition create(Operator<? super String> operator, Condition<?>... args) {
		return new StringOperation(operator, args);
	}

	private final OperationImpl<String> sourceOperation;

	protected StringOperation(Operator<? super String> op, Condition<?>... args) {
		this(op, ImmutableList.copyOf(args));
	}

	protected StringOperation(Operator<? super String> op, ImmutableList<Condition<?>> args) {
		super(new OperationImpl<String>(String.class, op, args));
		this.sourceOperation = (OperationImpl<String>) sourceCondition;
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
	public Operator<? super String> getOperator() {
		return sourceOperation.getOperator();
	}

}
