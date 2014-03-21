package br.com.anteros.persistence.osql.condition;

public interface ConstantCondition<T> extends Condition<T> {

	T getConstant();

}
