package br.com.anteros.persistence.osql;

import com.google.common.base.Objects;

import br.com.anteros.persistence.osql.condition.Predicate;

public final class BooleanBuilder implements Predicate, Cloneable  {

    private static final long serialVersionUID = -4129485177345542519L;

    private Predicate predicate;

    public BooleanBuilder() {  }

    public BooleanBuilder(Predicate initial) {
        predicate = (Predicate)ConditionUtils.extract(initial);
    }

    @Override
    public <R,C> R accept(Visitor<R,C> v, C context) {
        if (predicate != null) {
            return predicate.accept(v, context);
        } else {
            return null;
        }
    }

    public BooleanBuilder and(Predicate right) {
        if (right != null) {
            if (predicate == null) {
                predicate = right;
            } else {
                predicate = ConditionUtils.and(predicate, right);
            }
        }
        return this;
    }

    public BooleanBuilder andAnyOf(Predicate... args) {
        if (args.length > 0) {
            and(ConditionUtils.anyOf(args));
        }
        return this;
    }

    public BooleanBuilder andNot(Predicate right) {
        return and(right.not());
    }

    @Override
    public BooleanBuilder clone() throws CloneNotSupportedException{
        return (BooleanBuilder) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BooleanBuilder) {
            return Objects.equal(((BooleanBuilder)o).getValue(), predicate);
        } else {
            return false;
        }
    }

    public Predicate getValue() {
        return predicate;
    }

    @Override
    public int hashCode() {
        return predicate != null ? predicate.hashCode() : 0;
    }

    public boolean hasValue() {
        return predicate != null;
    }

    @Override
    public BooleanBuilder not() {
        if (predicate != null) {
            predicate = predicate.not();
        }
        return this;
    }

    public BooleanBuilder or(Predicate right) {
        if (right != null) {
            if (predicate == null) {
                predicate = right;
            } else {
                predicate = ConditionUtils.or(predicate, right);
            }
        }
        return this;
    }

    public BooleanBuilder orAllOf(Predicate... args) {
        if (args.length > 0) {
            or(ConditionUtils.allOf(args));
        }
        return this;
    }

    public BooleanBuilder orNot(Predicate right) {
        return or(right.not());
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public String toString() {
        return predicate != null ? predicate.toString() : super.toString();
    }

}
