package br.com.anteros.persistence.osql.condition;

import java.util.List;

public interface ListCondition<E, Q extends SimpleCondition<? super E>> extends CollectionCondition<List<E>, E> {

	Q get(Condition<Integer> index);

	Q get(int index);
}
