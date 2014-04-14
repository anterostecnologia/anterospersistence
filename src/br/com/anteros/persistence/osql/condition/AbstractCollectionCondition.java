package br.com.anteros.persistence.osql.condition;

import java.util.Collection;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.EntityAttribute;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.OperationBase;


public abstract class AbstractCollectionCondition<T extends Collection<E>, E> extends AbstractCondition<T> implements CollectionCondition<T, E> {

    private volatile BooleanCondition empty;

    private volatile NumberCondition<Integer> size;

    public AbstractCollectionCondition(Condition<T> sourceCondition) {
        super(sourceCondition);
    }

    public AbstractCondition<E> as(EntityAttribute<E> alias) {
        return OperationBase.create(getElementType(), Operators.ALIAS, sourceCondition, alias);
    }

    public final BooleanCondition contains(E child) {
        return contains(ConstantImpl.create(child));
    }

    public final BooleanCondition contains(Condition<E> child) {
        return BooleanOperation.create(Operators.IN, child, sourceCondition);
    }

    public abstract Class<E> getElementType();

    public final BooleanCondition isEmpty() {
        if (empty == null) {
            empty = BooleanOperation.create(Operators.COLUMN_IS_EMPTY, sourceCondition);
        }
        return empty;
    }

    public final BooleanCondition isNotEmpty() {
        return isEmpty().not();
    }

    public final NumberCondition<Integer> size() {
        if (size == null) {
            size = NumberOperation.create(Integer.class, Operators.COLUMN_SIZE, sourceCondition);
        }
        return size;
    }

}
