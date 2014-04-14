package br.com.anteros.persistence.osql.impl;

import br.com.anteros.persistence.osql.Operator;

@SuppressWarnings("serial")
public final class OperatorImpl<T> implements Operator<T> {

    private final String id;

    public OperatorImpl(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}