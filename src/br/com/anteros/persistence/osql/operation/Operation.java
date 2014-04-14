package br.com.anteros.persistence.osql.operation;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.condition.Condition;

public interface Operation<T> extends Condition<T> {

	Condition<?> getArgument(int index);

	List<Condition<?>> getArguments();

	Operator<? super T> getOperator();

}
