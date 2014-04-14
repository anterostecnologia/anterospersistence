package br.com.anteros.persistence.osql.condition;

import java.util.Collection;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.SimpleOperation;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public abstract class SimpleCondition<T> extends AbstractCondition<T> {

	private BooleanCondition is_Not_Null;
	private NumberCondition<Long> count_Distinct;
	private NumberCondition<Long> count;
	private BooleanCondition is_Null;

	public SimpleCondition(Condition<T> sourceCondition) {
		super(sourceCondition);
	}

	public SimpleCondition<T> as(Attribute<T> alias) {
		return SimpleOperation.create((Class<T>) getType(), Operators.ALIAS, sourceCondition, alias);
	}

	public SimpleCondition<T> as(String alias) {
		return as(new AttributeImpl<T>(getType(), alias));
	}

	public BooleanCondition isNotNull() {
		if (is_Not_Null == null) {
			is_Not_Null = BooleanOperation.create(Operators.IS_NOT_NULL, sourceCondition);
		}
		return is_Not_Null;
	}

	public BooleanCondition isNull() {
		if (is_Null == null) {
			is_Null = BooleanOperation.create(Operators.IS_NULL, sourceCondition);
		}
		return is_Null;
	}

	public NumberCondition<Long> count() {
		if (count == null) {
			count = NumberOperation.create(Long.class, Operators.COUNT_AGG, sourceCondition);
		}
		return count;
	}

	public NumberCondition<Long> countDistinct() {
		if (count_Distinct == null) {
			count_Distinct = NumberOperation.create(Long.class, Operators.COUNT_DISTINCT_AGG, sourceCondition);
		}
		return count_Distinct;
	}

	public BooleanCondition eq(T right) {
		if (right == null) {
			throw new IllegalArgumentException("equals nulo não são permitidos. Use isNull() se for o caso");
		} else {
			return eq(ConstantImpl.create(right));
		}
	}

	public BooleanCondition eq(Condition<? super T> right) {
		return BooleanOperation.create(Operators.EQ, sourceCondition, right);
	}

	public BooleanCondition eqAll(CollectionCondition<?, ? super T> right) {
		return eq(ConditionUtils.all(right));
	}

	public BooleanCondition eqAny(CollectionCondition<?, ? super T> right) {
		return eq(ConditionUtils.any(right));
	}

	public BooleanCondition in(Collection<? extends T> right) {
		if (right.size() == 1) {
			return eq(right.iterator().next());
		} else {
			return BooleanOperation.create(Operators.IN, sourceCondition, ConstantImpl.create(right));
		}
	}

	public BooleanCondition in(T... right) {
		if (right.length == 1) {
			return eq(right[0]);
		} else {
			return BooleanOperation.create(Operators.IN, sourceCondition,
					ConstantImpl.create(ImmutableList.copyOf(right)));
		}
	}

	public BooleanCondition in(CollectionCondition<?, ? extends T> right) {
		return BooleanOperation.create(Operators.IN, sourceCondition, right);
	}

	public BooleanCondition ne(T right) {
		return ne(ConstantImpl.create(right));
	}

	public BooleanCondition ne(Condition<? super T> right) {
		return BooleanOperation.create(Operators.NE, sourceCondition, right);
	}

	public BooleanCondition neAll(CollectionCondition<?, ? super T> right) {
		return ne(ConditionUtils.all(right));
	}

	public BooleanCondition neAny(CollectionCondition<?, ? super T> right) {
		return ne(ConditionUtils.any(right));
	}

	public BooleanCondition notIn(Collection<? extends T> right) {
		if (right.size() == 1) {
			return ne(right.iterator().next());
		} else {
			return BooleanOperation.create(Operators.NOT_IN, sourceCondition, ConstantImpl.create(right));
		}
	}

	public BooleanCondition notIn(T... right) {
		if (right.length == 1) {
			return ne(right[0]);
		} else {
			return BooleanOperation.create(Operators.NOT_IN, sourceCondition,
					ConstantImpl.create(ImmutableList.copyOf(right)));
		}
	}

	public final BooleanCondition notIn(CollectionCondition<?, ? extends T> right) {
		return BooleanOperation.create(Operators.NOT_IN, sourceCondition, right);
	}

	public SimpleCondition<T> nullif(Condition<T> other) {
		return SimpleOperation.create((Class<T>) this.getType(), Operators.NULLIF, this, other);
	}

	public SimpleCondition<T> nullif(T other) {
		return nullif(ConstantImpl.create(other));
	}

	public CaseForEqBuilder<T> when(T other) {
		return new CaseForEqBuilder<T>(sourceCondition, ConstantImpl.create(other));
	}

	public CaseForEqBuilder<T> when(Condition<? extends T> other) {
		return new CaseForEqBuilder<T>(sourceCondition, other);
	}

}
