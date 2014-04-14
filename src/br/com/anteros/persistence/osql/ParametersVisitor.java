package br.com.anteros.persistence.osql;

import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.JoinCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.operation.Operation;
import br.com.anteros.persistence.osql.query.QueryDescriptor;

public final class ParametersVisitor implements Visitor<Void, QueryDescriptor> {
    
    public static final ParametersVisitor DEFAULT = new ParametersVisitor();
    
    private ParametersVisitor() {}

    @Override
    public Void visit(ConstantCondition<?> expr, QueryDescriptor context) {
        return null;
    }

    @Override
    public Void visit(FactoryCondition<?> expr, QueryDescriptor context) {
        visit(expr.getArguments(), context);
        return null;
    }

    @Override
    public Void visit(Operation<?> expr, QueryDescriptor context) {
        visit(expr.getArguments(), context);
        return null;
    }

    @Override
    public Void visit(ParameterCondition<?> expr, QueryDescriptor context) {
        return null;
    }

    @Override
    public Void visit(Attribute<?> expr, QueryDescriptor context) {
        return null;
    }

    @Override
    public Void visit(SubQueryCondition<?> expr, QueryDescriptor context) {
    	QueryDescriptor md = expr.getDescriptor();
        for (Map.Entry<ParameterCondition<?>, Object> entry : md.getParameters().entrySet()) {
            context.setParameter((ParameterCondition)entry.getKey(), entry.getValue());
        }
        visit(md.getGroupBy(), context);        
        visit(md.getHaving(), context);
        for (JoinCondition join : md.getJoins()) {
            visit(join.getTarget(), context);
            visit(join.getCondition(), context);
        }
        visit(md.getProjection(), context);
        visit(md.getWhere(), context);
        
        return null;
    }

    @Override
    public Void visit(CodeTemplateCondition<?> expr, QueryDescriptor context) {
        for (Object arg : expr.getArguments()) {
            if (arg instanceof Condition<?>) {
                ((Condition<?>)arg).accept(this, context);
            }
        }
        return null;
    }
    
    private void visit(Condition<?> expr, QueryDescriptor context) {
        if (expr != null) {
            expr.accept(this, context);
        }
    }
    
    private void visit(List<Condition<?>> conditions, QueryDescriptor context) {
        for (Condition<?> arg : conditions) {
            arg.accept(this, context);
        }
    }

}
