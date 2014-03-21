package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

public class SetAttribute<E, Q extends SimpleCondition<? super E>> extends CollectionAttributeBase<Set<E>, E, Q> {

    private final Class<E> elementType;

    private final AttributeImpl<Set<E>> sourceAttribute;
    
    
    private transient Q any;
    
    private final Class<Q> queryType;

    public SetAttribute(Class<? super E> type, Class<Q> queryType, String variable) {
        this(type, queryType, AttributeDescriptorFactory.createVariableAccessor(variable));
    }
    
    public SetAttribute(Class<? super E> type, Class<Q> queryType, Attribute parent, String property) {
        this(type, queryType, AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }
    
    public SetAttribute(Class<? super E> type, Class<Q> queryType, AttributeDescriptor<?> metadata) {
        this(type, queryType, metadata, AttributeInits.DIRECT);
    }
    
    @SuppressWarnings("unchecked")
    public SetAttribute(Class<? super E> type, Class<Q> queryType, AttributeDescriptor<?> metadata, AttributeInits inits) {
        super(new AttributeImpl<Set<E>>((Class)Set.class, metadata), inits);
        this.elementType = (Class<E>)type;
        this.queryType = queryType;
        this.sourceAttribute = (AttributeImpl<Set<E>>)sourceCondition;
    }
    
    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(sourceAttribute, context);
    }
    
    @Override
    public Q any() {
        if (any == null) {
            any = newInstance(queryType, AttributeDescriptorFactory.createCollectionAnyAccessor(this));
        }
        return any;
    }

    public Class<E> getElementType() {
        return elementType;
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
    
    @Override
    public Class<?> getParameter(int index) {
        if (index == 0) {
            return elementType;
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }

}
