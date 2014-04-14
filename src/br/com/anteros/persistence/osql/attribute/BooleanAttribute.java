package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.BooleanCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;


@SuppressWarnings("serial")
public class BooleanAttribute extends BooleanCondition implements Attribute<Boolean> {

    private final AttributeImpl<Boolean> sourceAttribute;

    public BooleanAttribute(Attribute<?> parent, String property) {
        this(AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }

    public BooleanAttribute(AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<Boolean>(Boolean.class, metadata));
        this.sourceAttribute = (AttributeImpl<Boolean>)sourceCondition;
    }

    public BooleanAttribute(String var) {
        this(AttributeDescriptorFactory.createVariableAccessor(var));
    }
    
    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
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
