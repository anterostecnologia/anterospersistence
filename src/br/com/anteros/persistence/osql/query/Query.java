package br.com.anteros.persistence.osql.query;

import br.com.anteros.persistence.osql.FilteredClause;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.attribute.EntityAttribute;
import br.com.anteros.persistence.osql.condition.CollectionCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.MapCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.Predicate;

public interface Query<Q extends Query<Q>> extends FilteredClause<Q> {
	  
    Q from(EntityAttribute<?>... sources);

    <P> Q innerJoin(EntityAttribute<P> target);

    <P> Q innerJoin(EntityAttribute<P> target, Attribute<P> alias);

    <P> Q innerJoin(CollectionCondition<?, P> target);

    <P> Q innerJoin(CollectionCondition<?,P> target, Attribute<P> alias);

    <P> Q innerJoin(MapCondition<?, P> target);

    <P> Q innerJoin(MapCondition<?, P> target, Attribute<P> alias);

    <P> Q join(EntityAttribute<P> target);

    <P> Q join(EntityAttribute<P> target, Attribute<P> alias);

    <P> Q join(CollectionCondition<?,P> target);

    <P> Q join(CollectionCondition<?,P> target, Attribute<P> alias);

    <P> Q join(MapCondition<?, P> target);

    <P> Q join(MapCondition<?, P> target, Attribute<P> alias);

    <P> Q leftJoin(EntityAttribute<P> target);

    <P> Q leftJoin(EntityAttribute<P> target, Attribute<P> alias);

    <P> Q leftJoin(CollectionCondition<?,P> target);

    <P> Q leftJoin(CollectionCondition<?,P> target, Attribute<P> alias);

    <P> Q leftJoin(MapCondition<?, P> target);

    <P> Q leftJoin(MapCondition<?, P> target, Attribute<P> alias);
    
    <P> Q rightJoin(EntityAttribute<P> target);

    <P> Q rightJoin(EntityAttribute<P> target, Attribute<P> alias);

    <P> Q rightJoin(CollectionCondition<?,P> target);

    <P> Q rightJoin(CollectionCondition<?,P> target, Attribute<P> alias);

    <P> Q rightJoin(MapCondition<?, P> target);

    <P> Q rightJoin(MapCondition<?, P> target, Attribute<P> alias);

    <P> Q fullJoin(EntityAttribute<P> target);

    <P> Q fullJoin(EntityAttribute<P> target, Attribute<P> alias);

    <P> Q fullJoin(CollectionCondition<?,P> target);

    <P> Q fullJoin(CollectionCondition<?,P> target, Attribute<P> alias);

    <P> Q fullJoin(MapCondition<?, P> target);

    <P> Q fullJoin(MapCondition<?, P> target, Attribute<P> alias);
    
    Q on(Predicate... condition);
    
	Q groupBy(Condition<?>... o);

    Q having(Predicate... o);
    
	Q limit(long limit);

	Q offset(long offset);

	Q orderBy(OrderBy<?>... o);

	<T> Q set(ParameterCondition<T> param, T value);

	Q distinct();

}
