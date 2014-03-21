package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import br.com.anteros.persistence.osql.ConditionException;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.AbstractMapCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

public class MapAttribute<K, V, E extends SimpleCondition<? super V>> extends AbstractMapCondition<K, V, E> implements Attribute<Map<K, V>> {

    private final Class<K> keyType;

    private final AttributeImpl<Map<K,V>> sourceAttribute;

    private final Class<E> queryType;

    
    private transient Constructor<E> constructor;

    private final Class<V> valueType;

    public MapAttribute(Class<? super K> keyType, Class<? super V> valueType, Class<E> queryType, String variable) {
        this(keyType, valueType, queryType, AttributeDescriptorFactory.createVariableAccessor(variable));
    }
    
    public MapAttribute(Class<? super K> keyType, Class<? super V> valueType, Class<E> queryType, Attribute parent, String property) {
        this(keyType, valueType, queryType, AttributeDescriptorFactory.createPropertyAccessor(parent, property));   
    }
    
    @SuppressWarnings("unchecked")
    public MapAttribute(Class<? super K> keyType, Class<? super V> valueType, Class<E> queryType, AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<Map<K,V>>((Class)Map.class, metadata));
        this.keyType = (Class<K>) keyType;
        this.valueType = (Class<V>) valueType;
        this.queryType = queryType;
        this.sourceAttribute = (AttributeImpl<Map<K,V>>)sourceCondition;
    }
    
    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(sourceAttribute, context);
    }

    protected AttributeDescriptor<K> createMapAccessor(K key) {
        return AttributeDescriptorFactory.createMapAccessor(this, key);
    }

    protected AttributeDescriptor<K> createMapAccessor(Condition<K> key) {
        return AttributeDescriptorFactory.createMapAccessor(this, key);
    }

    @Override
    public E get(Condition<K> key)  {
        try {
            AttributeDescriptor<K> md =  createMapAccessor(key);
            return newInstance(md);
        } catch (NoSuchMethodException e) {
            throw new ConditionException(e);
        } catch (InstantiationException e) {
            throw new ConditionException(e);
        } catch (IllegalAccessException e) {
            throw new ConditionException(e);
        } catch (InvocationTargetException e) {
            throw new ConditionException(e);
        }
    }

    @Override
    public E get(K key) {
        try {
            AttributeDescriptor<K> md =  createMapAccessor(key);
            return newInstance(md);
        } catch (NoSuchMethodException e) {
            throw new ConditionException(e);
        } catch (InstantiationException e) {
            throw new ConditionException(e);
        } catch (IllegalAccessException e) {
            throw new ConditionException(e);
        } catch (InvocationTargetException e) {
            throw new ConditionException(e);
        }
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    @Override
    public AttributeDescriptor<?> getDescriptor() {
        return sourceAttribute.getDescriptor();
    }

    @Override
    public Attribute getRoot() {
        return sourceAttribute.getRoot();
    }

    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return sourceAttribute.getAnnotatedElement();
    }

    private E newInstance(AttributeDescriptor<?> pm) throws NoSuchMethodException,
        InstantiationException, IllegalAccessException,
        InvocationTargetException {
        if (constructor == null) {
            if (Constants.isTyped(queryType)) {
                constructor = queryType.getConstructor(Class.class, AttributeDescriptor.class);
            } else {
                constructor = queryType.getConstructor(AttributeDescriptor.class);
            }
        }
        if (Constants.isTyped(queryType)) {
            return constructor.newInstance(getValueType(), pm);
        } else {
            return constructor.newInstance(pm);
        }
    }
    
    @Override
    public Class<?> getParameter(int index) {
        if (index == 0) {
            return keyType;
        } else if (index == 1) {    
            return valueType;
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }

}
