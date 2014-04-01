package br.com.anteros.persistence.osql.operation;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.EnumCondition;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.collect.ImmutableList;

public class EnumOperation<T extends Enum<T>> extends EnumCondition<T> implements Operation<T> {

    public static <D extends Enum<D>> EnumCondition<D> create(Class<? extends D> type, Operator<? super D> operator, Condition<?> one) {
        return new EnumOperation<D>(type, operator, ImmutableList.<Condition<?>>of(one));
    }
    
    public static <D extends Enum<D>> EnumCondition<D> create(Class<? extends D> type, Operator<? super D> operator, Condition<?> one, Condition<?> two) {
        return new EnumOperation<D>(type, operator, ImmutableList.<Condition<?>>of(one, two));
    }
    
    public static <D extends Enum<D>> EnumCondition<D> create(Class<? extends D> type, Operator<? super D> operator, Condition<?>... arguments) {
        return new EnumOperation<D>(type, operator, arguments);
    }

    private final OperationImpl<T> sourceOperation;

    protected EnumOperation(Class<? extends T> type, Operator<? super T> op, Condition<?>... arguments) {
        this(type, op, ImmutableList.copyOf(arguments));
    }

    protected EnumOperation(Class<? extends T> type, Operator<? super T> op, ImmutableList<Condition<?>> args) {
        super(new OperationImpl<T>(type, op, args));
        this.sourceOperation = (OperationImpl<T>)sourceCondition;
    }
    
    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(sourceOperation, context);
    }

    @Override
    public Condition<?> getArgument(int index) {
        return sourceOperation.getArgument(index);
    }

    @Override
    public List<Condition<?>> getArguments() {
        return sourceOperation.getArguments();
    }

    @Override
    public Operator<? super T> getOperator() {
        return sourceOperation.getOperator();
    }

}
