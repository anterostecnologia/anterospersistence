package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.EnumCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

public class EnumAttribute<T extends Enum<T>> extends EnumCondition<T> implements Attribute<T> {

    private final AttributeImpl<T> sourceAttribute;

    public EnumAttribute(Class<? extends T> type, Attribute parent, String property) {
        this(type, AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }

    public EnumAttribute(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<T>(type, metadata));
        this.sourceAttribute = (AttributeImpl<T>)sourceCondition;
    }

    public EnumAttribute(Class<? extends T> type, String var) {
        this(type, AttributeDescriptorFactory.createVariableAccessor(var));
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
