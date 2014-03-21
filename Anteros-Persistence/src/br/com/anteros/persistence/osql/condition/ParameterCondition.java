package br.com.anteros.persistence.osql.condition;


public interface ParameterCondition<T> extends Condition<T> {

    String getName();

    boolean isAnon();

    String getNotSetMessage();

}
