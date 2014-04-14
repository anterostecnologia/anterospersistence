package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;

@SuppressWarnings("serial")
public abstract class BooleanCondition extends ComparableCondition<Boolean> implements Predicate {

	private BooleanCondition eqTrue, eqFalse;
	private BooleanCondition not;

	public static BooleanCondition allOf(BooleanCondition... exprs) {
		BooleanCondition rv = null;
		for (BooleanCondition b : exprs) {
			rv = rv == null ? b : rv.and(b);
		}
		return rv;
	}

	public static BooleanCondition anyOf(BooleanCondition... exprs) {
		BooleanCondition rv = null;
		for (BooleanCondition b : exprs) {
			rv = rv == null ? b : rv.or(b);
		}
		return rv;
	}

	public BooleanCondition(Condition<Boolean> mixin) {
		super(mixin);
	}

	@Override
	public BooleanCondition as(Attribute<Boolean> alias) {
		return BooleanOperation.create(Operators.ALIAS, sourceCondition, alias);
	}

	@Override
	public BooleanCondition as(String alias) {
		return as(new AttributeImpl<Boolean>(Boolean.class, alias));
	}

	public BooleanCondition and(Predicate right) {
		right = (Predicate) ConditionUtils.extract(right);
		if (right != null) {
			return BooleanOperation.create(Operators.AND, sourceCondition, right);
		} else {
			return this;
		}
	}

	public BooleanCondition andAnyOf(Predicate... predicates) {
		return and(ConditionUtils.anyOf(predicates));
	}

	@Override
	public BooleanCondition not() {
		if (not == null) {
			not = BooleanOperation.create(Operators.NOT, this);
		}
		return not;
	}

	public BooleanCondition or(Predicate right) {
		right = (Predicate) ConditionUtils.extract(right);
		if (right != null) {
			return BooleanOperation.create(Operators.OR, sourceCondition, right);
		} else {
			return this;
		}
	}

	public BooleanCondition orAllOf(Predicate... predicates) {
		return or(ConditionUtils.allOf(predicates));
	}

	public BooleanCondition isTrue() {
		return eq(true);
	}

	public BooleanCondition isFalse() {
		return eq(false);
	}

	@Override
	public BooleanCondition eq(Boolean right) {
		if (right.booleanValue()) {
			if (eqTrue == null) {
				eqTrue = super.eq(true);
			}
			return eqTrue;
		} else {
			if (eqFalse == null) {
				eqFalse = super.eq(false);
			}
			return eqFalse;
		}
	}
}
