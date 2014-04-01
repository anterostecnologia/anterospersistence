package br.com.anteros.persistence.osql;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.operation.Operation;


public interface Visitor<R,C> {

    R visit(ConstantCondition<?> expr, C context);

    R visit(FactoryCondition<?> expr, C context);

    R visit(Operation<?> expr, C context);

    R visit(ParameterCondition<?> expr, C context);

    R visit(Attribute<?> expr, C context);

    R visit(SubQueryCondition<?> expr, C context);

    R visit(CodeTemplateCondition<?> expr, C context);

}
