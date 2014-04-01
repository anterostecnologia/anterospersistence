package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.DateCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

@SuppressWarnings({"unchecked"})
public class DateAttribute<T extends Comparable> extends DateCondition<T> implements Attribute<T> {

    private final AttributeImpl<T> sourceAttribute;

    public DateAttribute(Class<? extends T> type, Attribute<T> parent, String property) {
        this(type, AttributeDescriptorFactory.createPropertyAccessor(parent, property));
    }

    public DateAttribute(Class<? extends T> type, AttributeDescriptor<?> metadata) {
        super(new AttributeImpl<T>(type, metadata));
        this.sourceAttribute = (AttributeImpl<T>)sourceCondition;
    }

    public DateAttribute(Class<? extends T> type, String var) {
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
