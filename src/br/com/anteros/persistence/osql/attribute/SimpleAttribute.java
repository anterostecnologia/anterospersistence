package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

@SuppressWarnings("serial")
public class SimpleAttribute<T> extends SimpleCondition<T> implements Attribute<T> {

    private final AttributeImpl<T> sourceAttribute;

    public SimpleAttribute(Class<? extends T> type, Attribute<?> parent, String property) {
        this(type, AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }

    public SimpleAttribute(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<T>(type, metadata));
        this.sourceAttribute = (AttributeImpl<T>)sourceCondition;
    }
    
    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(sourceAttribute, context);
    }

    public SimpleAttribute(Class<? extends T> type, String var) {
        this(type, AttributeDescriptorFactory.createVariableAccessor(var));
    }

    @Override
    public AttributeDescriptor<?> getDescriptor() {
        return sourceAttribute.getDescriptor();
    }

    @Override
    public Attribute<?> getRoot() {
        return sourceAttribute.getRoot();
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return sourceAttribute.getAnnotatedElement();
    }
}
