package br.com.anteros.persistence.osql.attribute;

import java.io.Serializable;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public final class AttributeDescriptor<T> implements Serializable{

    private final Object element;

    private final int hashCode;

    private final Attribute<?> parent, root;

    private final AttributeRelationType relationType;

    public AttributeDescriptor(Attribute<?> parent, Object element, AttributeRelationType relationType) {
        this.parent = parent;
        this.element = element;
        this.relationType = relationType;
        this.hashCode = 31 * element.hashCode() + relationType.hashCode();
        this.root = parent != null ? parent.getRoot() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AttributeDescriptor<?>) {
            AttributeDescriptor<?> p = (AttributeDescriptor<?>) obj;
            return element.equals(p.element) &&
                    relationType == p.relationType &&
                    Objects.equal(parent, p.parent);
        } else {
            return false;
        }

    }

    public Object getElement() {
        return element;
    }

    public String getName() {
        if (relationType == AttributeRelationType.VARIABLE || relationType == AttributeRelationType.PROPERTY) {
            return (String)element;
        } else {
            throw new IllegalStateException("name property not available for path of type " + relationType +
                    ". Use getElement() to access the generic path element.");
        }
    }

    public Attribute<?> getParent() {
        return parent;
    }

    public AttributeRelationType getRelationType() {
        return relationType;
    }


    public Attribute<?> getRoot() {
        return root;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean isRoot() {
        return parent == null || (relationType == AttributeRelationType.DELEGATE && parent.getDescriptor().isRoot());
    }

}
