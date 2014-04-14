package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.StringCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

public class StringAttribute extends StringCondition implements Attribute<String> {

    private final AttributeImpl<String> sourceAttribute;

    public StringAttribute(Attribute parent, String property) {
        this(AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }

    public StringAttribute(AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<String>(String.class, metadata));
        this.sourceAttribute = (AttributeImpl<String>)sourceCondition;
    }

    public StringAttribute(String var) {
        this(AttributeDescriptorFactory.createVariableAccessor(var));
    }
    
    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(sourceAttribute, context);
    }

    @Override
    public AttributeDescriptor<?> getDescriptor() {
        return sourceAttribute.getDescriptor();
    }

    @Override
    public Attribute getRoot() {
        return sourceAttribute.getRoot();
    }
    
    @Override
    public AnnotatedElement getAnnotatedElement() {
        return sourceAttribute.getAnnotatedElement();
    }
}
