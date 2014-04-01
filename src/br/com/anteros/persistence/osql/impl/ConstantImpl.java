package br.com.anteros.persistence.osql.impl;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;


@SuppressWarnings("unchecked")
public final class ConstantImpl<T> extends AbstractCondition<T> implements ConstantCondition<T> {


    public static ConstantCondition<Boolean> create(boolean b) {
        return b ? new ConstantImpl<Boolean>(Boolean.TRUE) : new ConstantImpl<Boolean>(Boolean.FALSE);
    }

    public static ConstantCondition<Byte> create(byte i) {
            return new ConstantImpl<Byte>(Byte.class, Byte.valueOf(i));
    }

    public static ConstantCondition<Character> create(char i) {
            return new ConstantImpl<Character>(Character.class, Character.valueOf(i));
    }

    public static ConstantCondition<Integer> create(int i) {
            return new ConstantImpl<Integer>(Integer.class, Integer.valueOf(i));
    }

    public static ConstantCondition<Long> create(long i) {
            return new ConstantImpl<Long>(Long.class, Long.valueOf(i));
    }

    public static ConstantCondition<Short> create(short i) {
            return new ConstantImpl<Short>(Short.class, Short.valueOf(i));
    }

    public static <T> ConstantCondition<T> create(T obj) {
        return new ConstantImpl<T>(obj);
    }

    private final T constant;

    public ConstantImpl(T constant) {
        this((Class)constant.getClass(), constant);
    }

    public ConstantImpl(Class<T> type, T constant) {
        super(type);
        this.constant = constant;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ConstantCondition<?>) {
            return ((ConstantCondition<?>)o).getConstant().equals(constant);
        } else {
            return false;
        }
    }

    @Override
    public T getConstant() {
        return constant;
    }

}
