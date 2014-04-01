package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.query.QueryDescriptor;


public interface SubQueryCondition<T> extends Condition<T> {

    QueryDescriptor getDescriptor();

}
