package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.EnumOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;

@SuppressWarnings("serial")
public abstract class EnumCondition<T extends Enum<T>> extends ComparableCondition<T> {

	public EnumCondition(Condition<T> sourceCondition) {
		super(sourceCondition);
	}

	@Override
	public EnumCondition<T> as(Attribute<T> alias) {
		return EnumOperation.create(getType(), Operators.ALIAS, sourceCondition, alias);
	}

	@Override
	public EnumCondition<T> as(String alias) {
		return as(new AttributeImpl<T>(getType(), alias));
	}

	public NumberCondition<Integer> ordinal() {
		return NumberOperation.create(Integer.class, Operators.ORDINAL, sourceCondition);
	}

}