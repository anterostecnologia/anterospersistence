package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.OrderByType;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.StringOperation;

@SuppressWarnings({"unchecked", "serial"})
public abstract class AbstractComparableCondition<T extends Comparable> extends SimpleCondition<T> {

    private OrderBy<T> asc, desc;

    private StringCondition stringCast;

    public AbstractComparableCondition(Condition<T> mixin) {
        super(mixin);
    }

    public final OrderBy<T> asc() {
        if (asc == null) {
            asc = new OrderBy<T>(OrderByType.ASC, sourceCondition);
        }
        return asc;
    }

    public final Coalesce<T> coalesce(Condition<?>...exprs) {
        Coalesce<T> coalesce = new Coalesce<T>(getType(), sourceCondition);
        for (Condition expr : exprs) {
            coalesce.add(expr);
        }
        return coalesce;
    }

    public final Coalesce<T> coalesce(T... args) {
        Coalesce<T> coalesce = new Coalesce<T>(getType(), sourceCondition);
        for (T arg : args) {
            coalesce.add(arg);
        }
        return coalesce;
    }

    public <A extends Number & Comparable<? super A>> NumberCondition<A> castToNum(Class<A> type) {
        return NumberOperation.create(type, Operators.NUMCAST, sourceCondition, ConstantImpl.create(type));
    }

    public final OrderBy<T> desc() {
        if (desc == null) {
            desc = new OrderBy<T>(OrderByType.DESC, sourceCondition);
        }
        return desc;
    }

    public StringCondition stringValue() {
        if (stringCast == null) {
            stringCast = StringOperation.create(Operators.STRING_CAST, sourceCondition);
        }
        return stringCast;
    }

}
