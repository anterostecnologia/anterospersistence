package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.ArrayCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.NumberCondition;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.NumberOperation;

import com.google.common.primitives.Primitives;

public class ArrayAttribute<A, E> extends SimpleCondition<A> implements Attribute<A>, ArrayCondition<A, E> {

    private final Class<E> componentType;

    private final AttributeImpl<A> sourceAttribute;

    private NumberCondition<Integer> size;

    public ArrayAttribute(Class<? super A> type, String variable) {
        this(type, AttributeDescriptorFactory.createVariableAccessor(variable));
    }
    
    public ArrayAttribute(Class<? super A> type, Attribute<?> parent, String property) {
        this(type, AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }
    
    @SuppressWarnings("unchecked")
    public ArrayAttribute(Class<? super A> type, AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<A>((Class)type, metadata));
        this.sourceAttribute = (AttributeImpl<A>)sourceCondition;
        this.componentType = Primitives.wrap((Class<E>)type.getComponentType());
    }

    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(sourceAttribute, context);
    }
    
    public SimpleAttribute<E> get(Condition<Integer> index) {
    	AttributeDescriptor<Integer> md = AttributeDescriptorFactory.createArrayAccessor(sourceAttribute, index);
        return new SimpleAttribute<E>(componentType, md);
    }

    public SimpleAttribute<E> get(int index) {
        AttributeDescriptor<Integer> md = AttributeDescriptorFactory.createArrayAccessor(sourceAttribute, index);
        return new SimpleAttribute<E>(componentType, md);
    }

    public Class<E> getElementType() {
        return componentType;
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

    public NumberCondition<Integer> size() {
        if (size == null) {
            size = NumberOperation.create(Integer.class, Operators.ARRAY_SIZE, sourceAttribute);
        }
        return size;
    }

}
