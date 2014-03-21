package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.BooleanCondition;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;

@SuppressWarnings("serial")
public class BeanAttribute<T> extends SimpleCondition<T> implements Attribute<T> {

    private final Map<Class<?>, Object> casts = new HashMap<Class<?>, Object>();

    private final AttributeInits inits;

    private final AttributeImpl<T> sourceAttribute;

    public BeanAttribute(Class<? extends T> type, String variable) {
        this(type, AttributeDescriptorFactory.createVariableAccessor(variable), null);
    }

    public BeanAttribute(Class<? extends T> type, Attribute<?> parent,  String property) {
        this(type, AttributeDescriptorFactory.createPropertyAccessor(parent, property), null);
    }

    public BeanAttribute(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        this(type, metadata, null);
    }

    public BeanAttribute(Class<? extends T> type, AttributeDescriptor<?> metadata, AttributeInits inits) {
        super(new AttributeImpl<T>(type, metadata));
        this.sourceAttribute = (AttributeImpl<T>)sourceCondition;
        this.inits = inits;
    }

    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
    }

    public <U extends BeanAttribute<? extends T>> U as(Class<U> clazz) throws Exception {
        try {
            if (!casts.containsKey(clazz)) {
                AttributeDescriptor<T> metadata;
                if (sourceAttribute.getDescriptor().getRelationType() != AttributeRelationType.COLLECTION_ANY) {
                    metadata = AttributeDescriptorFactory.createDelegateAccessor(sourceAttribute);
                } else {
                    metadata = (AttributeDescriptor)sourceAttribute.getDescriptor();
                }
                U rv;
                if (inits != null && sourceAttribute.getDescriptor().getRelationType() != AttributeRelationType.VARIABLE) {
                    rv = clazz.getConstructor(AttributeDescriptor.class, AttributeInits.class).newInstance(metadata, inits);
                } else {
                    rv = clazz.getConstructor(AttributeDescriptor.class).newInstance(metadata);
                }
                casts.put(clazz, rv);
                return rv;
            } else {
                return (U) casts.get(clazz);
            }

        } catch (InstantiationException e) {
            throw new Exception(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new Exception(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new Exception(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    public <P extends Attribute<?>> P add(P path) {
        return path;
    }

    public <A, E> ArrayAttribute<A, E> createArray(String property, Class<? super A> type) {
        return add(new ArrayAttribute<A, E>(type, createPropertyAccessor(property)));
    }

    public BooleanAttribute createBoolean(String property) {
        return add(new BooleanAttribute(createPropertyAccessor(property)));
    }

    public <A, Q extends SimpleCondition<? super A>> CollectionAttribute<A, Q> createCollection(String property, Class<? super A> type, Class<? super Q> queryType, AttributeInits inits) {
        return add(new CollectionAttribute<A, Q>(type, (Class) queryType, createPropertyAccessor(property), inits));
    }

    public <A extends Comparable<?>> ComparableAttribute<A> createComparable(String property, Class<? super A> type) {
        return add(new ComparableAttribute<A>((Class) type, createPropertyAccessor(property)));
    }

    public <A extends Enum<A>> EnumAttribute<A> createEnum(String property, Class<A> type) {
        return add(new EnumAttribute<A>(type, createPropertyAccessor(property)));
    }

    public <A extends Comparable<?>> DateAttribute<A> createDate(String property, Class<? super A> type) {
        return add(new DateAttribute<A>((Class) type, createPropertyAccessor(property)));
    }

    public <A extends Comparable<?>> DateTimeAttribute<A> createDateTime(String property, Class<? super A> type) {
        return add(new DateTimeAttribute<A>((Class) type, createPropertyAccessor(property)));
    }

    public <A, E extends SimpleCondition<? super A>> ListAttribute<A, E> createList(String property, Class<? super A> type, Class<? super E> queryType, AttributeInits inits) {
        return add(new ListAttribute<A, E>(type, (Class) queryType, createPropertyAccessor(property), inits));
    }

    public <K, V, E extends SimpleCondition<? super V>> MapAttribute<K, V, E> createMap(String property, Class<? super K> key, Class<? super V> value, Class<? super E> queryType) {
        return add(new MapAttribute<K, V, E>(key, value, (Class) queryType, createPropertyAccessor(property)));
    }

    public <A extends Number & Comparable<?>> NumberAttribute<A> createNumber(String property, Class<? super A> type) {
        return add(new NumberAttribute<A>((Class) type, createPropertyAccessor(property)));
    }

    public <A, E extends SimpleCondition<? super A>> SetAttribute<A, E> createSet(String property, Class<? super A> type, Class<? super E> queryType, AttributeInits inits) {
        return add(new SetAttribute<A, E>(type, (Class) queryType, createPropertyAccessor(property), inits));
    }

    public <A> SimpleAttribute<A> createSimple(String property, Class<? super A> type) {
        return add(new SimpleAttribute<A>((Class<A>) type, createPropertyAccessor(property)));
    }

    public StringAttribute createString(String property) {
        return add(new StringAttribute(createPropertyAccessor(property)));
    }

    public <A extends Comparable<?>> TimeAttribute<A> createTime(String property, Class<? super A> type) {
        return add(new TimeAttribute<A>((Class) type, createPropertyAccessor(property)));
    }

    public AttributeDescriptor<?> createPropertyAccessor(String property) {
        return AttributeDescriptorFactory.createPropertyAccessor(this, property);
    }

    @Override
    public AttributeDescriptor<?> getDescriptor() {
        return sourceAttribute.getDescriptor();
    }

    @Override
    public Attribute<?> getRoot() {
        return sourceAttribute.getRoot();
    }

    public <B extends T> BooleanCondition instanceOf(Class<B> type) {
        return BooleanOperation.create(Operators.INSTANCE_OF, sourceAttribute, ConstantImpl.create(type));
    }

    public BooleanCondition instanceOfAny(Class... types) {
        BooleanCondition[] exprs = new BooleanCondition[types.length];
        for (int i = 0; i < types.length; i++) {
            exprs[i] = this.instanceOf(types[i]);
        }
        return BooleanCondition.anyOf(exprs);
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return sourceAttribute.getAnnotatedElement();
    }

}
