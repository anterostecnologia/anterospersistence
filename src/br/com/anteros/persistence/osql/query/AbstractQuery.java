package br.com.anteros.persistence.osql.query;

import java.util.Map;

import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.QueryModifiers;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.attribute.EntityAttribute;
import br.com.anteros.persistence.osql.condition.CollectionCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.MapCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.Predicate;
import br.com.anteros.persistence.session.SQLSession;

public abstract class AbstractQuery<Q extends AbstractQuery<Q>> implements Query<Q> {

	private Map<Object, String> constants;

	protected final QueryMixin<Q> queryMixin;

	private final OSQLCodeTemplates templates;

	protected final SQLSession session;

	public AbstractQuery(QueryDescriptor descriptor, OSQLCodeTemplates templates, SQLSession session) {
		this.queryMixin = new OSQLQueryMixin<Q>(descriptor);
		this.queryMixin.setSelf((Q) this);
		this.templates = templates;
		this.session = session;
	}

	protected OSQLCodeTemplates getTemplates() {
		return templates;
	}

	protected String buildQueryString(boolean forCountRow) {
		if (queryMixin.getDescriptor().getJoins().isEmpty()) {
			throw new IllegalArgumentException("No joins given");
		}
		OSQLSerializer serializer = new OSQLSerializer(templates, session);
		serializer.serialize(queryMixin.getDescriptor(), forCountRow, null);
		constants = serializer.getConstantToLabel();
		return serializer.toString();
	}

	protected void reset() {
		queryMixin.getDescriptor().reset();
	}

	/*
	 * VER AQUI @Override public boolean exists() { if
	 * (templates.isSelect1Supported()) { return
	 * limit(1).singleResult(NumberTemplate.ONE) != null; } else {
	 * EntityAttribute<?> EntityAttribute = (EntityAttribute<?>)
	 * queryMixin.getDescriptor().getJoins().get(0).getTarget(); return
	 * !limit(1).list(EntityAttribute).isEmpty(); } }
	 */

	public Q from(EntityAttribute<?> arg) {
		return queryMixin.from(arg);
	}

	public Q from(EntityAttribute<?>... args) {
		return queryMixin.from(args);
	}

	public <P> Q fullJoin(CollectionCondition<?, P> target) {
		return queryMixin.fullJoin(target);
	}

	public <P> Q fullJoin(CollectionCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.fullJoin(target, alias);
	}

	public <P> Q fullJoin(EntityAttribute<P> target) {
		return queryMixin.fullJoin(target);
	}

	public <P> Q fullJoin(EntityAttribute<P> target, Attribute<P> alias) {
		return queryMixin.fullJoin(target, alias);
	}

	public <P> Q fullJoin(MapCondition<?, P> target) {
		return queryMixin.fullJoin(target);
	}

	public <P> Q fullJoin(MapCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.fullJoin(target, alias);
	}

	protected Map<Object, String> getConstants() {
		return constants;
	}

	public <P> Q innerJoin(CollectionCondition<?, P> target) {
		return queryMixin.innerJoin(target);
	}

	public <P> Q innerJoin(CollectionCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.innerJoin(target, alias);
	}

	public <P> Q innerJoin(EntityAttribute<P> target) {
		return queryMixin.innerJoin(target);
	}

	public <P> Q innerJoin(EntityAttribute<P> target, Attribute<P> alias) {
		return queryMixin.innerJoin(target, alias);
	}

	public <P> Q innerJoin(MapCondition<?, P> target) {
		return queryMixin.innerJoin(target);
	}

	public <P> Q innerJoin(MapCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.innerJoin(target, alias);
	}

	public <P> Q join(CollectionCondition<?, P> target) {
		return queryMixin.join(target);
	}

	public <P> Q join(CollectionCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.join(target, alias);
	}

	public <P> Q join(EntityAttribute<P> target) {
		return queryMixin.join(target);
	}

	public <P> Q join(EntityAttribute<P> target, Attribute<P> alias) {
		return queryMixin.join(target, alias);
	}

	public <P> Q join(MapCondition<?, P> target) {
		return queryMixin.join(target);
	}

	public <P> Q join(MapCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.join(target, alias);
	}

	public <P> Q leftJoin(CollectionCondition<?, P> target) {
		return queryMixin.leftJoin(target);
	}

	public <P> Q leftJoin(CollectionCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.leftJoin(target, alias);
	}

	public <P> Q leftJoin(EntityAttribute<P> target) {
		return queryMixin.leftJoin(target);
	}

	public <P> Q leftJoin(EntityAttribute<P> target, Attribute<P> alias) {
		return queryMixin.leftJoin(target, alias);
	}

	public <P> Q leftJoin(MapCondition<?, P> target) {
		return queryMixin.leftJoin(target);
	}

	public <P> Q leftJoin(MapCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.leftJoin(target, alias);
	}

	public <P> Q rightJoin(CollectionCondition<?, P> target) {
		return queryMixin.rightJoin(target);
	}

	public <P> Q rightJoin(CollectionCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.rightJoin(target, alias);
	}

	public <P> Q rightJoin(EntityAttribute<P> target) {
		return queryMixin.rightJoin(target);
	}

	public <P> Q rightJoin(EntityAttribute<P> target, Attribute<P> alias) {
		return queryMixin.rightJoin(target, alias);
	}

	public <P> Q rightJoin(MapCondition<?, P> target) {
		return queryMixin.rightJoin(target);
	}

	public <P> Q rightJoin(MapCondition<?, P> target, Attribute<P> alias) {
		return queryMixin.rightJoin(target, alias);
	}

	public Q on(Predicate condition) {
		return queryMixin.on(condition);
	}

	public Q on(Predicate... conditions) {
		return queryMixin.on(conditions);
	}

	protected void setConstants(Map<Object, String> constants) {
		this.constants = constants;
	}

	protected String toCountRowsString() {
		return buildQueryString(true);
	}

	protected String toQueryString() {
		return buildQueryString(false);
	}

	@Override
	public String toString() {
		return buildQueryString(false).trim();
	}

	public QueryDescriptor getDescriptor() {
		return queryMixin.getDescriptor();
	}
	
    public Q distinct() {
        return queryMixin.distinct();
    }
    
    public Q groupBy(Condition<?> e) {
        return queryMixin.groupBy(e);
    }
    
    public Q groupBy(Condition<?>... o) {
        return queryMixin.groupBy(o);
    }

    public Q having(Predicate e) {
        return queryMixin.having(e);
    }

    public Q having(Predicate... o) {
        return queryMixin.having(o);
    }
   
    public Q orderBy(OrderBy<?> o) {
        return queryMixin.orderBy(o);
    }

    public Q orderBy(OrderBy<?>... o) {
        return queryMixin.orderBy(o);
    }
    
    public Q where(Predicate o) {
        return queryMixin.where(o);
    }

    public Q where(Predicate... o) {
        return queryMixin.where(o);
    }

     public Q limit(long limit) {
        return queryMixin.limit(limit);
    }

    public Q offset(long offset) {
        return queryMixin.offset(offset);
    }

    public Q restrict(QueryModifiers modifiers) {
        return queryMixin.restrict(modifiers);
    }

    public <P> Q set(ParameterCondition<P> param, P value) {
        return queryMixin.set(param, value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof AbstractQuery) {
        	AbstractQuery q = (AbstractQuery)o;
            return q.queryMixin.equals(queryMixin);
        } else {
            return false;
        }
    }
  
    @Override
    public int hashCode() {
        return queryMixin.hashCode();
    }



}
