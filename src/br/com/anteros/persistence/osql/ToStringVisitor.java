package br.com.anteros.persistence.osql;

import java.util.Arrays;
import java.util.List;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplate;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplates;
import br.com.anteros.persistence.osql.operation.Operation;
import br.com.anteros.persistence.util.ResourceUtils;

public final class ToStringVisitor implements Visitor<String,CodeTemplates> {
    
    public static final ToStringVisitor DEFAULT = new ToStringVisitor();

    private ToStringVisitor() {}
    
    @Override
    public String visit(ConstantCondition<?> e, CodeTemplates codeTemplates) {
        return e.getConstant().toString();
    }

    @Override
    public String visit(FactoryCondition<?> e, CodeTemplates codeTemplates) {
        final StringBuilder builder = new StringBuilder();
        builder.append("new ").append(e.getType().getSimpleName()).append("(");
        boolean first = true;
        for (Condition<?> arg : e.getArguments()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(arg.accept(this, codeTemplates));
            first = false;
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visit(Operation<?> o, CodeTemplates codeTemplates) {
        final CodeTemplate codeTemplate = codeTemplates.getTemplate(o.getOperator());
        if (codeTemplate != null) {
            final StringBuilder builder = new StringBuilder();
            for (CodeTemplate.Element element : codeTemplate.getElements()) {
                final Object rv = element.convert(o.getArguments());
                if (rv instanceof Condition) {                    
                    builder.append(((Condition)rv).accept(this, codeTemplates));
                } else {                    
                    builder.append(rv.toString());
                }
            }
            return builder.toString();
        } else {
            return ResourceUtils.getMessage(ToStringVisitor.class, "operation_unknown", o.getArguments());
        }                  
    }

    @Override
    public String visit(ParameterCondition<?> param, CodeTemplates codeTemplates) {
        return "{" + param.getName() + "}";
    }

    @Override
    public String visit(Attribute<?> p, CodeTemplates codeTemplates) {
        final Attribute<?> parent = p.getDescriptor().getParent();
        final Object elem = p.getDescriptor().getElement();
        if (parent != null) {
            CodeTemplate pattern = codeTemplates.getTemplate(p.getDescriptor().getRelationType());
            if (pattern != null) {
                final List<?> args = Arrays.asList(parent, elem);
                final StringBuilder builder = new StringBuilder();
                for (CodeTemplate.Element element : pattern.getElements()) {
                    Object rv = element.convert(args);
                    if (rv instanceof Condition) {                    
                        builder.append(((Condition)rv).accept(this, codeTemplates));
                    } else {
                        builder.append(rv.toString());
                    }
                }                
                return builder.toString();
            } else {
                throw new IllegalArgumentException("Nenhum padrão para " + p.getDescriptor().getRelationType());
            }
        } else {
            return elem.toString();
        }
    }

    @Override
    public String visit(SubQueryCondition<?> expr, CodeTemplates codeTemplates) {
        return expr.getDescriptor().toString();
    }

    @Override
    public String visit(CodeTemplateCondition<?> expr, CodeTemplates codeTemplates) {
        final StringBuilder builder = new StringBuilder();
        for (CodeTemplate.Element element : expr.getTemplate().getElements()) {
            Object rv = element.convert(expr.getArguments());
            if (rv instanceof Condition) {                    
                builder.append(((Condition)rv).accept(this, codeTemplates));
            } else {
                builder.append(rv.toString());
            }
        }
        return builder.toString();
    }

}
