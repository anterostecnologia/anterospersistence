package br.com.anteros.persistence.osql;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;

@SuppressWarnings("serial")
public class ConstructorCondition<T> extends AbstractCondition<T> implements FactoryCondition<T> {

    private static Class<?> normalize(Class<?> clazz) {
        return Primitives.wrap(clazz);
    }

    private static Class<?>[] getRealParameters(Class<?> type, Class<?>[] givenTypes) {
        for (Constructor<?> c : type.getConstructors()) {
            Class<?>[] paramTypes = c.getParameterTypes();
            if (c.isVarArgs()) {
                return paramTypes;
            } else if (paramTypes.length == givenTypes.length) {
                boolean found = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!normalize(paramTypes[i]).isAssignableFrom(normalize(givenTypes[i]))) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return paramTypes;
                }
            }
        }
        StringBuilder formattedTypes = new StringBuilder();
        for (Class<?> typ : givenTypes) {
            if (formattedTypes.length() > 0) {
                formattedTypes.append(", ");
            }
            formattedTypes.append(typ.getName());
        }
        throw new ConditionException("Got no matching constructor. Class: " +
                type.getName() +", parameters: " + formattedTypes.toString());
    }

    public static <D> ConstructorCondition<D> create(Class<D> type, Condition<?>... arguments) {
        Class<?>[] paramTypes = new Class[arguments.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arguments[i].getType();
        }
        return new ConstructorCondition<D>(type, paramTypes, arguments);
    }

    private final ImmutableList<Condition<?>> arguments;

    private final Class<?>[] parameterTypes;

    private transient Constructor<?> constructor;

    public ConstructorCondition(Class<T> type, Class<?>[] paramTypes, Condition<?>... arguments) {
        this(type, paramTypes, ImmutableList.copyOf(arguments));
    }

    public ConstructorCondition(Class<T> type, Class<?>[] paramTypes, ImmutableList<Condition<?>> args) {
        super(type);
        this.parameterTypes = getRealParameters(type, paramTypes).clone();
        this.arguments = args;
    }

    public Condition<T> as(Attribute<T> alias) {
        return OperationImpl.create(getType(),Operators.ALIAS, this, alias);
    }

    public Condition<T> as(String alias) {
        return as(new AttributeImpl<T>(getType(), alias));
    }

    @Override
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ConstructorCondition<?>) {
            ConstructorCondition<?> c = (ConstructorCondition<?>)obj;
            return Arrays.equals(parameterTypes, c.parameterTypes)
                && arguments.equals(c.arguments)
                && getType().equals(c.getType());
        } else {
            return false;
        }
    }

    @Override
    public final List<Condition<?>> getArguments() {
        return arguments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T newInstance(Object... args) {
        try {
            if (constructor == null) {
                constructor = getType().getConstructor(parameterTypes);
            }
            if (constructor.isVarArgs()) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] cargs = new Object[paramTypes.length];
                System.arraycopy(args, 0, cargs, 0, cargs.length - 1);
                int size = args.length - cargs.length + 1;
                Object array = Array.newInstance(
                        paramTypes[paramTypes.length - 1].getComponentType(), size);
                cargs[cargs.length - 1] = array;
                System.arraycopy(args, cargs.length - 1, array, 0, size);
                return (T) constructor.newInstance(cargs);
            } else {
                return (T) constructor.newInstance(args);
            }

        } catch (SecurityException e) {
           throw new ConditionException(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
           throw new ConditionException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new ConditionException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConditionException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new ConditionException(e.getMessage(), e);
        }
    }

}
