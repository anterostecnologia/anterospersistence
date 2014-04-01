package br.com.anteros.persistence.osql.operation;

import java.util.List;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.BooleanCondition;
import br.com.anteros.persistence.osql.condition.Condition;

import com.google.common.collect.ImmutableList;


@SuppressWarnings("serial")
public class BooleanOperation extends BooleanCondition implements Operation<Boolean> {

    public static BooleanCondition create(Operator<? super Boolean> operator, Condition<?> one) {
        return new BooleanOperation(operator, ImmutableList.<Condition<?>>of(one));
    }
    
    public static BooleanCondition create(Operator<? super Boolean> operator, Condition<?> one, Condition<?> two) {
        return new BooleanOperation(operator, ImmutableList.of(one, two));
    }
    
    public static BooleanCondition create(Operator<? super Boolean> operator, Condition<?>... arguments) {
        return new BooleanOperation(operator, arguments);
    }
    
    private final PredicateOperation sourceOperation;

    protected BooleanOperation(Operator<? super Boolean> operator, Condition<?>... arguments) {
        this(operator, ImmutableList.copyOf(arguments));
    }
    
    protected BooleanOperation(Operator<? super Boolean> operator, ImmutableList<Condition<?>> arguments) {
        super(new PredicateOperation((Operator)operator, arguments));
        this.sourceOperation = (PredicateOperation)sourceCondition;
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
    public Operator<? super Boolean> getOperator() {
        return sourceOperation.getOperator();
    }

    @Override
    public BooleanCondition not() {
        if (sourceOperation.getOperator() == Operators.NOT && sourceOperation.getArgument(0) instanceof BooleanCondition) {
            return (BooleanCondition) sourceOperation.getArgument(0);
        } else {
            return super.not();
        }
    }

}
