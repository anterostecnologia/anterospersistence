package br.com.anteros.persistence.osql.condition;

@SuppressWarnings("serial")
public abstract class TemporalCondition<T extends Comparable> extends ComparableCondition<T> {

	public TemporalCondition(Condition<T> sourceCondition) {
		super(sourceCondition);
	}

	public BooleanCondition after(T right) {
		return gt(right);
	}

	public BooleanCondition after(Condition<T> right) {
		return gt(right);
	}

	public BooleanCondition before(T right) {
		return lt(right);
	}

	public BooleanCondition before(Condition<T> right) {
		return lt(right);
	}

}
