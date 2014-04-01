package br.com.anteros.persistence.osql;

import java.lang.reflect.Array;
import java.util.List;

import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class ArrayConstructorCondition<T> extends AbstractCondition<T[]> implements FactoryCondition<T[]> {

    private final Class<T> elementType;

    private final ImmutableList<Condition<?>> arguments;

    @SuppressWarnings("unchecked")
    public ArrayConstructorCondition(Condition<?>... arguments) {
        this((Class)Object[].class, (Condition[])arguments);
    }
    
    @SuppressWarnings("unchecked")
    public ArrayConstructorCondition(Class<T[]> type, Condition<T>... arguments) {
        super(type);
        this.elementType = (Class<T>)type.getComponentType();
        this.arguments = ImmutableList.<Condition<?>>copyOf(arguments);
    }

    public final Class<T> getElementType() {
        return elementType;
    }

    @Override
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] newInstance(Object... a) {
        if (a.getClass().getComponentType().equals(elementType)) {
            return (T[])a;
        } else {
            T[] rv = (T[]) Array.newInstance(elementType, a.length);
            System.arraycopy(a, 0, rv, 0, a.length);
            return rv;
        }
    }

    @Override
    public List<Condition<?>> getArguments() {
        return arguments;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FactoryCondition<?>) {
            FactoryCondition<?> c = (FactoryCondition<?>)obj;
            return arguments.equals(c.getArguments()) && getType().equals(c.getType());
        } else {
            return false;
        }
    }

}
