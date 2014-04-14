package br.com.anteros.persistence.osql.condition;


public interface Predicate extends Condition<Boolean> {
    
    Predicate not();

}
