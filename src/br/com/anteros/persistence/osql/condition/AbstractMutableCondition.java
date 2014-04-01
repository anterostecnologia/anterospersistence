package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.HashCodeVisitor;
import br.com.anteros.persistence.osql.ToStringVisitor;


public abstract class AbstractMutableCondition<T> implements Condition<T> {

    private static final long serialVersionUID = -6830426684911919114L;

    private final Class<? extends T> type;

    public AbstractMutableCondition(Class<? extends T> type) {
        this.type = type;
    }
    
    public final Class<? extends T> getType() {
        return type;
    }
    
    public final int hashCode() {
        return accept(HashCodeVisitor.DEFAULT, null);
    }
    
    @Override
    public final String toString() {
        return accept(ToStringVisitor.DEFAULT, CodeTemplates.DEFAULT);
    }

}
