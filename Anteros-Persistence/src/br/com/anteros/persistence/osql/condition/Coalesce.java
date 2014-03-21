package br.com.anteros.persistence.osql.condition;

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.ComparableOperation;
import br.com.anteros.persistence.osql.operation.DateOperation;
import br.com.anteros.persistence.osql.operation.DateTimeOperation;
import br.com.anteros.persistence.osql.operation.EnumOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.OperationBase;
import br.com.anteros.persistence.osql.operation.StringOperation;
import br.com.anteros.persistence.osql.operation.TimeOperation;


@SuppressWarnings("unchecked")
public class Coalesce<T extends Comparable> extends AbstractMutableCondition<T> {


    private final List<Condition<? extends T>> exprs = new ArrayList<Condition<? extends T>>();

    private volatile ComparableCondition<T> value;

    public Coalesce(Class<? extends T> type, Condition<?>... exprs) {
        super(type);
        for (Condition expr : exprs) {
            add(expr);
        }
    }

    public Coalesce(Condition... exprs) {
        this((exprs.length > 0 ? exprs[0].getType() : Object.class), exprs);
    }

    @Override
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return getValue().accept(v, context);
    }

    public ComparableCondition<T> getValue() {
        if (value == null) {
            value = (ComparableCondition<T>)ComparableOperation.create(getType(), Operators.COALESCE, getExpressionList());
        }
        return value;
    }

    public AbstractCondition<T> as(Attribute<T> alias) {
        return OperationBase.create((Class<T>)getType(),Operators.ALIAS, this, alias);
    }


    public AbstractCondition<T> as(String alias) {
        return as(new AttributeImpl<T>(getType(), alias));
    }

    public final Coalesce<T> add(Condition<T> expr) {
        value = null;
        this.exprs.add(expr);
        return this;
    }

    public OrderBy<T> asc() {
        return getValue().asc();
    }

    public OrderBy<T> desc() {
        return getValue().desc();
    }

    public final Coalesce<T> add(T constant) {
        return add(ConstantImpl.create(constant));
    }

    public BooleanCondition asBoolean() {
        return BooleanOperation.create(Operators.COALESCE, getExpressionList());
    }

    public DateCondition<T> asDate() {
        return (DateCondition<T>) DateOperation.create(getType(), Operators.COALESCE, getExpressionList());
    }

    public DateTimeCondition<T> asDateTime() {
        return (DateTimeCondition<T>) DateTimeOperation.create(getType(), Operators.COALESCE, getExpressionList());
    }

    public EnumCondition<?> asEnum() {
        return EnumOperation.create((Class)getType(), Operators.COALESCE, getExpressionList());
    }

    public NumberCondition<?> asNumber() {
        return NumberOperation.create((Class)getType(), Operators.COALESCE, getExpressionList());
    }

    public StringCondition asString() {
        return StringOperation.create(Operators.COALESCE, getExpressionList());
    }

    public TimeCondition<T> asTime() {
        return (TimeCondition<T>) TimeOperation.create(getType(), Operators.COALESCE, getExpressionList());
    }

    private Condition<?> getExpressionList() {
        return ConditionUtils.list(getType(), exprs);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Coalesce<?>) {
            Coalesce<?> c = (Coalesce<?>)o;
            return c.exprs.equals(exprs);
        } else {
            return false;
        }
    }


}
