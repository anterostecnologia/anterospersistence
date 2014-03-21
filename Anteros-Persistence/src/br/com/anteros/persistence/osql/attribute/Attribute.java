package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.condition.Condition;

public interface Attribute<T> extends Condition<T> {

    AttributeDescriptor<?> getDescriptor();

    Attribute<?> getRoot();

    AnnotatedElement getAnnotatedElement();

}
