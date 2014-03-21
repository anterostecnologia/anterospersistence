package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;

@SuppressWarnings({"unchecked"})
public abstract class ComparableCondition<T extends Comparable> extends AbstractComparableCondition<T> {

    public ComparableCondition(Condition<T> mixin) {
        super(mixin);
    }
    
    @Override
    public ComparableCondition<T> as(Attribute<T> alias) {
        return ComparableOperation.create(getType(),(Operator)Operators.ALIAS, sourceCondition, alias);
    }
    
    @Override
    public ComparableCondition<T> as(String alias) {
        return as(new AttributeImpl<T>(getType(), alias));
    }

    public final BooleanCondition between(T from, T to) {
        if (from == null) {
            if (to != null) {
                return BooleanOperation.create(Operators.LOE, sourceCondition, ConstantImpl.create(to));
            } else {
                throw new IllegalArgumentException("Either from or to needs to be non-null");
            }
        } else if (to == null) {
            return BooleanOperation.create(Operators.GOE, sourceCondition, ConstantImpl.create(from));
        } else {
            return BooleanOperation.create(Operators.BETWEEN, sourceCondition, ConstantImpl.create(from), ConstantImpl.create(to));    
        }        
    }

    public final BooleanCondition between(Condition<T> from, Condition<T> to) {
        if (from == null) {
            if (to != null) {
                return BooleanOperation.create(Operators.LOE, sourceCondition, to);
            } else {
                throw new IllegalArgumentException("Either from or to needs to be non-null");
            }
        } else if (to == null) {
            return BooleanOperation.create(Operators.GOE, sourceCondition, from);
        } else {
            return BooleanOperation.create(Operators.BETWEEN, sourceCondition, from, to);    
        }
        
    }

    public final BooleanCondition notBetween(T from, T to) {
        return between(from, to).not();
    }

    public final BooleanCondition notBetween(Condition<T> from, Condition<T> to) {
        return between(from, to).not();
    }

    public BooleanCondition gt(T right) {
        return gt(ConstantImpl.create(right));
    }

    public BooleanCondition gt(Condition<T> right) {
        return BooleanOperation.create(Operators.GT, sourceCondition, right);
    }
    
    public BooleanCondition gtAll(CollectionCondition<?, ? super T> right) {
        return gt(ConditionUtils.<T>all(right));
    }

    
    public BooleanCondition gtAny(CollectionCondition<?, ? super T> right) {
        return gt(ConditionUtils.<T>any(right));
    }

    public BooleanCondition goe(T right) {
        return goe(ConstantImpl.create(right));
    }

    public BooleanCondition goe(Condition<T> right) {
        return BooleanOperation.create(Operators.GOE, sourceCondition, right);
    }
    
    public BooleanCondition goeAll(CollectionCondition<?, ? super T> right) {
        return goe(ConditionUtils.<T>all(right));
    }
    
    public BooleanCondition goeAny(CollectionCondition<?, ? super T> right) {
        return goe(ConditionUtils.<T>any(right));
    }

    public final BooleanCondition lt(T right) {
        return lt(ConstantImpl.create(right));
    }

    public final BooleanCondition lt(Condition<T> right) {
        return BooleanOperation.create(Operators.LT, sourceCondition, right);
    }
    
    public BooleanCondition ltAll(CollectionCondition<?, ? super T> right) {
        return lt(ConditionUtils.<T>all(right));
    }

    
    public BooleanCondition ltAny(CollectionCondition<?, ? super T> right) {
        return lt(ConditionUtils.<T>any(right));
    }

    public final BooleanCondition loe(T right) {
        return BooleanOperation.create(Operators.LOE, sourceCondition, ConstantImpl.create(right));
    }

    public final BooleanCondition loe(Condition<T> right) {
        return BooleanOperation.create(Operators.LOE, sourceCondition, right);
    }
    
    public BooleanCondition loeAll(CollectionCondition<?, ? super T> right) {
        return loe(ConditionUtils.<T>all(right));
    }
    
    public BooleanCondition loeAny(CollectionCondition<?, ? super T> right) {
        return loe(ConditionUtils.<T>any(right));
    }

}
