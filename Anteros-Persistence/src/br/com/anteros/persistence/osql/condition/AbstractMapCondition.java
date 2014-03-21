package br.com.anteros.persistence.osql.condition;

import java.util.Map;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;

@SuppressWarnings({"serial","rawtypes","unchecked"})
public abstract class AbstractMapCondition<K, V, Q extends SimpleCondition<? super V>> extends AbstractCondition<Map<K,V>> implements MapCondition<K,V> {

    private volatile NumberCondition<Integer> size;
    private volatile BooleanCondition empty;

    public AbstractMapCondition(Condition<Map<K, V>> mixin) {
        super(mixin);
    }

    public final BooleanCondition contains(K key, V value) {
        return get(key).eq(value);
    }

	public final BooleanCondition contains(Condition<K> key, Condition<V> value) {
        return get(key).eq((Condition)value);
    }

    public final BooleanCondition containsKey(Condition<K> key) {
        return BooleanOperation.create(Operators.CONTAINS_KEY, sourceCondition, key);
    }

    public final BooleanCondition containsKey(K key) {
        return BooleanOperation.create(Operators.CONTAINS_KEY, sourceCondition, ConstantImpl.create(key));
    }

    public final BooleanCondition containsValue(Condition<V> value) {
        return BooleanOperation.create(Operators.CONTAINS_VALUE, sourceCondition, value);
    }

    public final BooleanCondition containsValue(V value) {
        return BooleanOperation.create(Operators.CONTAINS_VALUE, sourceCondition, ConstantImpl.create(value));
    }

    public abstract Q get(Condition<K> key);

    public abstract Q get(K key);

    public final BooleanCondition isEmpty() {
        if (empty == null) {
            empty = BooleanOperation.create(Operators.MAP_IS_EMPTY, sourceCondition);
        }
        return empty;
    }

    public final BooleanCondition isNotEmpty() {
        return isEmpty().not();
    }

    public final NumberCondition<Integer> size() {
        if (size == null) {
            size = NumberOperation.create(Integer.class, Operators.MAP_SIZE, sourceCondition);
        }
        return size;
    }

}
