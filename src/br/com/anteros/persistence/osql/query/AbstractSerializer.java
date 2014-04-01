package br.com.anteros.persistence.osql.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;







import br.com.anteros.persistence.osql.JoinInjectPart;
import br.com.anteros.persistence.osql.JoinInjectPosition;
import br.com.anteros.persistence.osql.Normalization;
import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.attribute.AttributeRelationType;
import br.com.anteros.persistence.osql.condition.CodeTemplate;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplates;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.operation.Operation;

import com.google.common.collect.ImmutableList;



public abstract class AbstractSerializer<S extends AbstractSerializer<S>> implements Visitor<Void,Void> {

    private final StringBuilder builder = new StringBuilder(256);
           
    private String constantPrefix = "a";

    private String parameterPrefix = "p";

    private String anonParameterPrefix = "_";

    private Map<Object,String> constantToLabel;

    private final S self = (S) this;

    private final CodeTemplates templates;
    
    private boolean normalize = true;
    
    private boolean strict = true;
    
    public AbstractSerializer(CodeTemplates templates) {
        this.templates = templates;
    }
    
    public final S prepend(final String str) {
        builder.insert(0, str);
        return self;
    }
    
    public final S insert(int position, String str) {
        builder.insert(position, str);
        return self;
    }

    public final S append(final String str) {
        builder.append(str);
        return self;
    }

    protected String getConstantPrefix() {
        return constantPrefix;
    }

    public Map<Object,String> getConstantToLabel() {
        if (constantToLabel == null) {
            constantToLabel = new HashMap<Object,String>(4);   
        }
        return constantToLabel;
    }
    
    public int getLength() {
        return builder.length();
    }

    protected final CodeTemplate getCodeTemplate(Operator<?> op) {
        return templates.getTemplate(op);
    }

    public final S handle(Condition<?> expr) {
        expr.accept(this, null);
        return self;
    }
    
    public final S handle(Object argument) {
        if (argument instanceof Condition) {
            ((Condition)argument).accept(this, null);
        } else {
            visitConstant(argument);
        }
        return self;
    }

    public final S handle(JoinInjectPart joinInjectPart) {
        return handle(joinInjectPart.getPart());
    }

    public final S handle(final String sep, final Condition<?>[] expressions) {
        for (int i = 0; i< expressions.length; i++) {
            if (i != 0) {
                append(sep);
            }
            handle(expressions[i]);
        }
        return self;
    }
    
    public final S handle(final String sep, final List<?> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            if (i != 0) {
                append(sep);
            }
            handle((Condition<?>)expressions.get(i));
        }
        return self;
    }

    protected void handleTemplate(final CodeTemplate template, final List<?> args) {
        for (final CodeTemplate.Element element : template.getElements()) {
            final Object rv = element.convert(args);
            if (rv instanceof Condition) {                    
                ((Condition)rv).accept(this, null);
            } else if (element.isString()) {
                builder.append(rv.toString());
            } else {
                visitConstant(rv);
            }
        }
    }

    public final boolean serialize(final QueryInjectPosition position, final Set<QueryInjectPart> parts) {
        boolean handled = false;
        for (final QueryInjectPart part : parts) {
            if (part.getPosition() == position) {
                handle(part.getPart());
                handled = true;
            }
        }
        return handled;
    }
    
    public final boolean serialize(final JoinInjectPosition position, final Set<JoinInjectPart> parts) {
        boolean handled = false;
        for (final JoinInjectPart part : parts) {
            if (part.getPosition() == position) {
                handle(part.getPart());
                handled = true;
            }
        }
        return handled;
    }

    public void setConstantPrefix(String prefix) {
        this.constantPrefix = prefix;
    }

    public void setParamPrefix(String prefix) {
        this.parameterPrefix = prefix;
    }

    public void setAnonParamPrefix(String prefix) {
        this.anonParameterPrefix = prefix;
    }
    
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;       
    }
    
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public String toString() {
        if (normalize) {
            return Normalization.normalize(builder.toString());    
        } else {
            return builder.toString();
        }        
    }

    @Override
    public final Void visit(ConstantCondition<?> expr, Void context) {
        visitConstant(expr.getConstant());
        return null;
    }
    
    public void visitConstant(Object constant) {
        if (!getConstantToLabel().containsKey(constant)) {
            final String constLabel = constantPrefix + (getConstantToLabel().size() + 1);
            getConstantToLabel().put(constant, constLabel);
            append(constLabel);
        } else {
            append(getConstantToLabel().get(constant));
        }
    }

    @Override
    public Void visit(ParameterCondition<?> parameter, Void context) {
        String paramLabel;
        if (parameter.isAnon()) {
            paramLabel = anonParameterPrefix + parameter.getName();
        } else {
            paramLabel = parameterPrefix + parameter.getName();
        }
        getConstantToLabel().put(parameter, paramLabel);
        append(paramLabel);
        return null;
    }

    @Override
    public Void visit(CodeTemplateCondition<?> expr, Void context) {
        handleTemplate(expr.getTemplate(), expr.getArguments());
        return null;
    }

    @Override
    public Void visit(FactoryCondition<?> expr, Void context) {
        handle(", ", expr.getArguments());
        return null;
    }

    @Override
    public Void visit(Operation<?> expr, Void context) {
        visitOperation(expr.getType(), expr.getOperator(), expr.getArguments());
        return null;
    }

    @Override
    public Void visit(Attribute<?> attribute, Void context) {
        final AttributeRelationType relationType = attribute.getDescriptor().getRelationType();
        final CodeTemplate template = templates.getTemplate(relationType);
        final Object element = attribute.getDescriptor().getElement();        
        List<Object> args;
        if (attribute.getDescriptor().getParent() != null) {
            args = ImmutableList.of(attribute.getDescriptor().getParent(), element);
        } else {
            args = ImmutableList.of(element);
        }
        handleTemplate(template, args);
        return null;
    }
    
    protected void visitOperation(Class<?> type, Operator<?> operator, final List<? extends Condition<?>> arguments) {
        final CodeTemplate template = templates.getTemplate(operator);
        if (template != null) {
            final int precedence = templates.getPrecedence(operator);        
            for (final CodeTemplate.Element element : template.getElements()) {
                final Object rv = element.convert(arguments);
                if (rv instanceof Condition) {
                    final Condition<?> expr = (Condition<?>)rv;                
                    if (precedence > -1 && expr instanceof Operation) {
                        if (precedence < templates.getPrecedence(((Operation<?>) expr).getOperator())) {
                            append("(").handle(expr).append(")");
                        } else {
                            handle(expr);
                        }
                    } else {
                        handle(expr);
                    }                  
                } else {
                    append(rv.toString());
                }            
            }    
        } else if (strict) {
            throw new IllegalArgumentException("Nenhum padrão para " + operator);
        } else {
            append(operator.toString());
            append("(");
            handle(", ", arguments);
            append(")");
        }        
    }


}
