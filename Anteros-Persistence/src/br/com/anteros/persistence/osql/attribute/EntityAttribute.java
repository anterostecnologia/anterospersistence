package br.com.anteros.persistence.osql.attribute;

public interface EntityAttribute<T> extends Attribute<T> {

    Object getDescriptor(Attribute<?> property);

}
