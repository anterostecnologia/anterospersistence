package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.operation.OperationBase;

public abstract class AbstractCondition<T> implements Condition<T> {

	protected Condition<T> sourceCondition;

	private Class<? extends T> type;

	public AbstractCondition(Condition<T> sourceCondition) {
		this.sourceCondition = sourceCondition;
	}

	public AbstractCondition(Class<? extends T> type) {
		this.type = type;
	}

	public final Class<? extends T> getType() {
		return type;
	}

	public Condition<T> as(Attribute<T> alias) {
		return OperationBase.create((Class<T>) getType(), Operators.ALIAS, sourceCondition, alias);
	}

}
