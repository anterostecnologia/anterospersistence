package br.com.anteros.persistence.osql.condition;

import java.util.List;

public interface FactoryCondition<T> extends Condition<T> {
    
    List<Condition<?>> getArguments();

    T newInstance(Object... arguments);

}