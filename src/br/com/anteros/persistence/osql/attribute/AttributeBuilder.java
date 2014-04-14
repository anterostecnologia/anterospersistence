package br.com.anteros.persistence.osql.attribute;

import java.util.Map;

import br.com.anteros.persistence.osql.condition.SimpleCondition;

import com.google.common.collect.Maps;

@SuppressWarnings({"serial","unchecked"})
public final class AttributeBuilder<T> extends EntityAttributeBase<T> {

    private final Map<String, AttributeBuilder<?>> properties = Maps.newHashMap();

    private final Map<Attribute<?>, Object> propertyDescriptor = Maps.newHashMap();

    public AttributeBuilder(Class<? extends T> type, AttributeDescriptor<?> attributeDescriptor) {
        super(type, attributeDescriptor);
    }

    public AttributeBuilder(Class<? extends T> type, String variable) {
        super(type, AttributeDescriptorFactory.createVariableAccessor(variable));
    }

    private <P extends Attribute<?>> P  addMetadataOf(P newPath, Attribute<?> path) {
        if (path.getDescriptor().getParent() instanceof EntityAttribute) {
            EntityAttribute<?> parent = (EntityAttribute<?>)path.getDescriptor().getParent();
            propertyDescriptor.put(newPath, parent.getDescriptor(path));
        }
        return newPath;
    }

    protected void validate(String property) {
    }

    @Override
    public Object getDescriptor(Attribute<?> property) {
        return propertyDescriptor.get(property);
    }

    public AttributeBuilder<Object> get(String property) {
        AttributeBuilder<Object> path = (AttributeBuilder) properties.get(property);
        if (path == null) {
            validate(property);
            path = new AttributeBuilder<Object>(Object.class, createPropertyAccessor(property));
            properties.put(property, path);
        }
        return path;
    }

   public <A> AttributeBuilder<A> get(String property, Class<A> type) {
        AttributeBuilder<A> path = (AttributeBuilder<A>) properties.get(property);
        if (path == null || !type.isAssignableFrom(path.getType())) {
            validate(property);
            path = new AttributeBuilder<A>(type, createPropertyAccessor(property));
            properties.put(property, path);
        }
        return path;
    }

    public <A, E> ArrayAttribute<A, E> getArray(String property, Class<A> type) {
        validate(property);
        return super.createArray(property, type);
    }

    public BooleanAttribute get(BooleanAttribute path) {
        BooleanAttribute newPath = getBoolean(toString(path));
        return addMetadataOf(newPath, path);
    }

    public BooleanAttribute getBoolean(String propertyName) {
        validate(propertyName);
        return super.createBoolean(propertyName);
    }

    public <A> CollectionAttribute<A, AttributeBuilder<A>> getCollection(String property, Class<A> type) {
        validate(property);
        return super.<A, AttributeBuilder<A>>createCollection(property, type, AttributeBuilder.class, AttributeInits.DIRECT);
    }

    public <A, E extends SimpleCondition<A>> CollectionAttribute<A, E> getCollection(String property, Class<A> type, Class<E> queryType) {
        validate(property);
        return super.<A, E>createCollection(property, type, queryType, AttributeInits.DIRECT);
    }

    public <A extends Comparable<?>> ComparableAttribute<A> get(ComparableAttribute<A> path) {
        ComparableAttribute<A> newPath = getComparable(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A extends Comparable<?>> ComparableAttribute<A> getComparable(String property, Class<A> type) {
        validate(property);
        return super.createComparable(property, type);
    }

    public <A extends Comparable<?>> DateAttribute<A> get(DateAttribute<A> path) {
        DateAttribute<A> newPath = getDate(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A extends Comparable<?>> DateAttribute<A> getDate(String property, Class<A> type) {
        validate(property);
        return super.createDate(property, type);
    }

    public <A extends Comparable<?>> DateTimeAttribute<A> get(DateTimeAttribute<A> path) {
        DateTimeAttribute<A> newPath = getDateTime(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A extends Comparable<?>> DateTimeAttribute<A> getDateTime(String property, Class<A> type) {
        validate(property);
        return super.createDateTime(property, type);
    }

    public <A extends Enum<A>> EnumAttribute<A> getEnum(String property, Class<A> type) {
        validate(property);
        return super.createEnum(property, type);
    }

    public <A extends Enum<A>> EnumAttribute<A> get(EnumAttribute<A> path) {
        EnumAttribute<A> newPath = getEnum(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A> ListAttribute<A, AttributeBuilder<A>> getList(String property, Class<A> type) {
        validate(property);
        return super.<A, AttributeBuilder<A>>createList(property, type, AttributeBuilder.class, AttributeInits.DIRECT);
    }

    public <A, E extends SimpleCondition<A>> ListAttribute<A, E> getList(String property, Class<A> type, Class<E> queryType) {
        validate(property);
        return super.<A, E>createList(property, type, queryType, AttributeInits.DIRECT);
    }

    public <K, V> MapAttribute<K, V, AttributeBuilder<V>> getMap(String property, Class<K> key, Class<V> value) {
        validate(property);
        return super.<K,V,AttributeBuilder<V>>createMap(property, key, value, AttributeBuilder.class);
    }

    public <K, V, E extends SimpleCondition<V>> MapAttribute<K, V, E> getMap(String property, Class<K> key, Class<V> value, Class<E> queryType) {
        validate(property);
        return super.<K,V,E>createMap(property, key, value, queryType);
    }

    public <A extends Number & Comparable<?>> NumberAttribute<A> get(NumberAttribute<A> path) {
        NumberAttribute<A> newPath = getNumber(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A extends Number & Comparable<?>> NumberAttribute<A> getNumber(String property, Class<A> type) {
        validate(property);
        return super.createNumber(property, type);
    }

    public <A> SetAttribute<A, AttributeBuilder<A>> getSet(String property, Class<A> type) {
        validate(property);
        return super.<A, AttributeBuilder<A>>createSet(property, type, AttributeBuilder.class, AttributeInits.DIRECT);
    }

    public <A, E extends SimpleCondition<A>> SetAttribute<A, E> getSet(String property, Class<A> type, Class<E> queryType) {
        validate(property);
        return super.<A, E>createSet(property, type, queryType, AttributeInits.DIRECT);
    }

    public <A> SimpleAttribute<A> get(Attribute path) {
        SimpleAttribute<A> newPath = getSimple(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A> SimpleAttribute<A> getSimple(String property, Class<A> type) {
        validate(property);
        return super.createSimple(property, type);
    }

    public StringAttribute get(StringAttribute path) {
        StringAttribute newPath = getString(toString(path));
        return addMetadataOf(newPath, path);
    }

    public StringAttribute getString(String property) {
        validate(property);
        return super.createString(property);
    }

    public <A extends Comparable<?>> TimeAttribute<A> get(TimeAttribute<A> path) {
        TimeAttribute<A> newPath = getTime(toString(path), (Class<A>)path.getType());
        return addMetadataOf(newPath, path);
    }

    public <A extends Comparable<?>> TimeAttribute<A> getTime(String property, Class<A> type) {
        validate(property);
        return super.createTime(property, type);
    }

    private String toString(Attribute<?> path) {
        return path.getDescriptor().getElement().toString();
    }

}
