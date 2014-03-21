package br.com.anteros.persistence.osql.operation;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.Predicate;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public final class PredicateOperation extends OperationImpl<Boolean> implements Predicate {

	private Predicate not;

	public static PredicateOperation create(Operator<Boolean> operator, Condition<?> one) {
		return new PredicateOperation(operator, ImmutableList.<Condition<?>> of(one));
	}

	public static PredicateOperation create(Operator<Boolean> operator, Condition<?> one, Condition<?> two) {
		return new PredicateOperation(operator, ImmutableList.of(one, two));
	}

	public PredicateOperation(Operator<Boolean> operator, ImmutableList<Condition<?>> args) {
		super(Boolean.class, operator, args);
	}

	@Override
	public Predicate not() {
		if (not == null) {
			not = PredicateOperation.create(Operators.NOT, this);
		}
		return not;
	}

}
