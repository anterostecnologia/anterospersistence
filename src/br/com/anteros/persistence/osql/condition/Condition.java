package br.com.anteros.persistence.osql.condition;

import java.io.Serializable;

import br.com.anteros.persistence.osql.Visitor;

public interface Condition<T> extends Serializable {

	<R, C> R accept(Visitor<R, C> v, C context);

	Class<? extends T> getType();

}
