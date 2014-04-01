package br.com.anteros.persistence.osql.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.persistence.osql.JoinInjectPart;
import br.com.anteros.persistence.osql.JoinType;
import br.com.anteros.persistence.osql.OrderBy;
import br.com.anteros.persistence.osql.QueryModifiers;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.JoinCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.Predicate;

public interface QueryDescriptor extends Serializable {

	void addGroupBy(Condition<?> o);

	void addHaving(Predicate o);

	void addJoin(JoinType joinType, Condition<?> expr);

	void addJoinInjectPart(JoinInjectPart part);

	void addJoinCondition(Predicate o);

	void addOrderBy(OrderBy<?> o);

	void addProjection(Condition<?> o);

	void addWhere(Predicate o);

	void clearOrderBy();

	void clearProjection();

	void clearWhere();

	QueryDescriptor clone();

	List<Condition<?>> getGroupBy();

	Predicate getHaving();

	List<JoinCondition> getJoins();

	List<OrderBy<?>> getOrderBy();

	List<Condition<?>> getProjection();

	Map<ParameterCondition<?>, Object> getParameters();

	Predicate getWhere();

	boolean isDistinct();

	boolean isUnique();

	void reset();

	void setDistinct(boolean distinct);

	void setLimit(Long limit);

	void setOffset(Long offset);

	void setUnique(boolean unique);

	<T> void setParameter(ParameterCondition<T> parameter, T value);

	void addPart(QueryInjectPart part);

	boolean hasPart(QueryInjectPart part);

	void removePart(QueryInjectPart part);

	Set<QueryInjectPart> getParts();

	void setValidate(boolean value);

	void setModifiers(QueryModifiers restriction);

	QueryModifiers getModifiers();
}
