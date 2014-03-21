package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ListCondition;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

public class ListAttribute<E, Q extends SimpleCondition<? super E>> extends CollectionAttributeBase<List<E>, E, Q> implements ListCondition<E, Q> {

    private final Map<Integer,Q> cache = new HashMap<Integer,Q>();

    private final Class<E> elementType;

    private final AttributeImpl<List<E>> sourceAttribute;

    private final Class<Q> queryType;

    
    private transient Q any;    

    public ListAttribute(Class<? super E> elementType, Class<Q> queryType, String variable) {
        this(elementType, queryType, AttributeDescriptorFactory.createVariableAccessor(variable));
    }
    
    public ListAttribute(Class<? super E> elementType, Class<Q> queryType, Attribute<?> parent, String property) {
        this(elementType, queryType, AttributeDescriptorFactory.createPropertyAccessor(parent, property));   
    }
    
    public ListAttribute(Class<? super E> elementType, Class<Q> queryType, AttributeDescriptor<?> metadata) {
        this(elementType, queryType, metadata, AttributeInits.DIRECT);
    }
    
    @SuppressWarnings("unchecked")
    public ListAttribute(Class<? super E> elementType, Class<Q> queryType, AttributeDescriptor<?> metadata, AttributeInits inits) {
        super(new AttributeImpl<List<E>>((Class)List.class, metadata), inits);
        this.elementType = (Class<E>)elementType;
        this.queryType = queryType;
        this.sourceAttribute = (AttributeImpl<List<E>>)sourceCondition;
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

    protected AttributeDescriptor<Integer> createListAccessor(int index) {
        return AttributeDescriptorFactory.createListAccessor(this, index);
    }

    protected AttributeDescriptor<Integer> createListAccessor(Condition<Integer> index) {
        return AttributeDescriptorFactory.createListAccessor(this, index);
    }

    private Q create(int index) {
        AttributeDescriptor<Integer> md = createListAccessor(index);
        return newInstance(queryType, md);
    }

    @Override
    public Q get(Condition<Integer> index) {
        AttributeDescriptor<Integer> md = createListAccessor(index);
        return newInstance(queryType, md);
    }


    @Override
    public Q get(int index) {
        if (cache.containsKey(index)) {
            return cache.get(index);
        } else {
            Q rv = create(index);
            cache.put(index, rv);
            return rv;
        }
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
