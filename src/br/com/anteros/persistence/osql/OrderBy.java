package br.com.anteros.persistence.osql;

import java.io.Serializable;

import br.com.anteros.persistence.osql.condition.Condition;

@SuppressWarnings({ "serial", "rawtypes" })
public class OrderBy<T extends Comparable> implements Serializable {

	private final OrderByType order;

	private final Condition<T> target;

	private final OrderByNullClauseType nullClauseType;

	public OrderBy(OrderByType order, Condition<T> target, OrderByNullClauseType nullClauseType) {
		this.order = order;
		this.target = target;
		this.nullClauseType = nullClauseType;
	}

	public OrderBy(OrderByType order, Condition<T> target) {
		this(order, target, OrderByNullClauseType.DEFAULT);
	}

	public OrderByType getOrder() {
		return order;
	}

	public boolean isAscending() {
		return order == OrderByType.ASC;
	}

	public Condition<T> getTarget() {
		return target;
	}

	public OrderByNullClauseType getNullClauseType() {
		return nullClauseType;
	}

	public OrderBy<T> nullsFirst() {
		return new OrderBy<T>(order, target, OrderByNullClauseType.NULLS_FIRST);
	}

	public OrderBy<T> nullsLast() {
		return new OrderBy<T>(order, target, OrderByNullClauseType.NULLS_LAST);
	}

	@Override
	public String toString() {
		return target + " " + order;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof OrderBy) {
			OrderBy<?> os = (OrderBy) o;
			return os.order.equals(order) && os.target.equals(target) && os.nullClauseType.equals(nullClauseType);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return target.hashCode();
	}

}
