package br.com.anteros.persistence.osql.condition;

public interface ArrayCondition<A, T> extends Condition<A> {

	NumberCondition<Integer> size();

	SimpleCondition<T> get(Condition<Integer> index);

	SimpleCondition<T> get(int index);

}
