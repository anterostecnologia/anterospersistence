package br.com.anteros.persistence.osql;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.operation.Operation;


final class ExtractorVisitor implements Visitor<Condition<?>,Void> {
    
    public static final ExtractorVisitor DEFAULT = new ExtractorVisitor();
    
    private ExtractorVisitor() {}
    
    @Override
    public Condition<?> visit(ConstantCondition<?> expr, Void context) {
        return expr;
    }

    @Override
    public Condition<?> visit(FactoryCondition<?> expr, Void context) {
        return expr;
    }

    @Override
    public Condition<?> visit(Operation<?> expr, Void context) {
        return expr;
    }

    @Override
    public Condition<?> visit(ParameterCondition<?> expr, Void context) {
        return expr;
    }

    @Override
    public Condition<?> visit(Attribute<?> expr, Void context) {
        return expr;
    }

    @Override
    public Condition<?> visit(SubQueryCondition<?> expr, Void context) {
        return expr;
    }

    @Override
    public Condition<?> visit(CodeTemplateCondition<?> expr, Void context) {
        return expr;
    }
    
}
