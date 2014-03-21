package br.com.anteros.persistence.osql.attribute;

@SuppressWarnings("unchecked")
public class ComparableEntityAttribute<T extends Comparable> extends ComparableAttribute<T> implements EntityAttribute<T> {

    public ComparableEntityAttribute(Class<? extends T> type, Attribute parent, String property) {
        super(type, parent, property);
    }

    public ComparableEntityAttribute(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        super(type, metadata);
    }

    public ComparableEntityAttribute(Class<? extends T> type, String var) {
        super(type, var);
    }

    @Override
    public Object getDescriptor(Attribute property) {
        return null;
    }

}
