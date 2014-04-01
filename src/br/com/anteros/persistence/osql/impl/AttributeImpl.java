package br.com.anteros.persistence.osql.impl;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.attribute.AttributeDescriptor;
import br.com.anteros.persistence.osql.attribute.AttributeDescriptorFactory;
import br.com.anteros.persistence.osql.attribute.AttributeRelationType;
import br.com.anteros.persistence.osql.condition.AbstractCondition;
import br.com.anteros.persistence.util.ReflectionUtils;

@SuppressWarnings("serial")
public class AttributeImpl<T> extends AbstractCondition<T> implements Attribute<T> {

    private final AttributeDescriptor<?> descriptor;
    private final Attribute<?> root;
    private transient AnnotatedElement annotatedElement;
    
    public AttributeImpl(Class<? extends T> type, String variable) {
        this(type, AttributeDescriptorFactory.createVariableAccessor(variable));
    }

    public AttributeImpl(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        super(type);
        this.descriptor = metadata;
        this.root = metadata.getRoot() != null ? metadata.getRoot() : this;
    }

    public AttributeImpl(Class<? extends T> type, Attribute<?> parent, String property) {
        this(type, AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Attribute<?>) {
            return ((Attribute<?>) o).getDescriptor().equals(descriptor);
        } else {
            return false;
        }
    }

    @Override
    public final AttributeDescriptor<?> getDescriptor() {
        return descriptor;
    }

    @Override
    public final Attribute<?> getRoot() {
        return root;
    }

    @Override
    public final AnnotatedElement getAnnotatedElement() {
        if (annotatedElement == null) {
            if (descriptor.getRelationType() == AttributeRelationType.PROPERTY) {
                Class<?> beanClass = descriptor.getParent().getType();
                String propertyName = descriptor.getName();
                annotatedElement = ReflectionUtils.getAnnotatedElement(beanClass, propertyName, getType());

            } else {
                annotatedElement = getType();
            }
        }
        return annotatedElement;
    }

    @Override
    public final <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

}
