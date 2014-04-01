package br.com.anteros.persistence.osql.query;

import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.osql.JoinInjectPart;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.CollectionCondition;
import br.com.anteros.persistence.osql.condition.Condition;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class OSQLQueryMixin<T> extends QueryMixin<T> {

    private final Set<Attribute<?>> attributes = Sets.newHashSet();

    private final Map<Condition<?>, Attribute<?>> aliases = Maps.newHashMap();

    public static final JoinInjectPart FETCH = new JoinInjectPart("fetch ");

    public static final JoinInjectPart FETCH_ALL_PROPERTIES = new JoinInjectPart(" fetch all properties");

    public OSQLQueryMixin() {}

    public OSQLQueryMixin(QueryDescriptor descriptor) {
        super(descriptor);
    }

    public OSQLQueryMixin(T self, QueryDescriptor descriptor) {
        super(self, descriptor);
    }

    public T fetch() {
        addJoinPart(FETCH);
        return getSelf();
    }

    public T fetchAll() {
        addJoinPart(FETCH_ALL_PROPERTIES);
        return getSelf();
    }

    @Override
    protected <D> Condition<D> createAlias(Condition<?> expr, Attribute<?> alias) {
        aliases.put(expr, alias);
        return super.createAlias(expr, alias);
    }

    private <T> Class<T> getElementTypeOrType(Attribute<T> attribute) {
        if (attribute instanceof CollectionCondition) {
            return ((CollectionCondition)attribute).getParameter(0);
        } else {
            return (Class<T>) attribute.getType();
        }
    }

            

}
