package br.com.anteros.persistence.osql.attribute;


@SuppressWarnings("serial")
public class EntityAttributeBase<T> extends BeanAttribute<T> implements EntityAttribute<T> {

    public EntityAttributeBase(Class<? extends T> type, String variable) {
        super(type, variable);
    }

    public EntityAttributeBase(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        super(type, metadata);
    }

    public EntityAttributeBase(Class<? extends T> type, AttributeDescriptor<?> metadata,  AttributeInits inits) {
        super(type, metadata, inits);
    }

    @Override
    public Object getDescriptor(Attribute<?> property) {
        return null;
    }

}
