package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.attribute.Attribute;
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
