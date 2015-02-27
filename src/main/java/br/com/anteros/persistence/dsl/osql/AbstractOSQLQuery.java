/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.dsl.osql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.dsl.osql.lang.CloseableIterator;
import br.com.anteros.persistence.dsl.osql.support.QueryMixin;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpressionUtils;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.path.CollectionPathBase;
import br.com.anteros.persistence.dsl.osql.types.path.EntityPathBase;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.TypedSQLQuery;

/**
 * AbstractSQLQuery is the base type for SQL query implementations
 *
 * @author tiwe
 *
 * @param <Q>
 *            concrete subtype
 */
public abstract class AbstractOSQLQuery<Q extends AbstractOSQLQuery<Q>> extends ProjectableSQLQuery<Q> {

	private static Logger logger = LoggerProvider.getInstance().getLogger(AbstractOSQLQuery.class.getName());

	private static final QueryFlag rowCountFlag = new QueryFlag(QueryFlag.Position.AFTER_PROJECTION, ", count(*) over() ");

	private final SQLSession session;

	protected boolean useLiterals;

	protected FactoryExpression<?> projection;

	protected Expression<?> lastJoinAdded = null;

	protected JoinType lastJoinTypeAdded = null;

	protected Expression<?> joinTarget = null;

	protected boolean lastJoinConditionAdded = false;

	protected SQLAnalyser analyser;

	public AbstractOSQLQuery(SQLSession session, SQLTemplates templates) {
		this(session, templates, new DefaultQueryMetadata().noValidate());
	}

	public AbstractOSQLQuery(SQLSession session, SQLTemplates templates, QueryMetadata metadata) {
		super(new QueryMixin<Q>(metadata, false), templates);
		this.session = session;
		this.useLiterals = true;
	}

	/**
	 * If you use forUpdate() with a backend that uses page or row locks, rows examined by the query are write-locked
	 * until the end of the current transaction.
	 *
	 * Not supported for SQLite and CUBRID
	 *
	 * @return
	 */
	public Q forUpdate() {
		return addFlag(SQLOps.FOR_UPDATE_FLAG);
	}

	protected SQLSerializer createSerializer() {
		SQLSerializer serializer = new SQLSerializer(session.getEntityCacheManager(), templates, getAnalyser());
		serializer.setUseLiterals(useLiterals);
		return serializer;
	}

	protected SQLAnalyser getAnalyser() {
		if (analyser == null) {
			analyser = new SQLAnalyser(this);
			analyser.process();
		}
		return analyser;
	}

	public void setUseLiterals(boolean useLiterals) {
		this.useLiterals = useLiterals;
	}

	@Override
	protected void clone(Q query) {
		super.clone(query);
		this.useLiterals = query.useLiterals;
	}

	@Override
	public Q clone() {
		return this.clone(this.session);
	}

	public abstract Q clone(SQLSession session);

	@Override
	public long count() {
		TypedSQLQuery<?> query = createQuery(null, true, null);
		reset();
		try {
			ResultSet rs = query.executeQuery();
			rs.next();
			Long value = rs.getLong(1);
			rs.close();
			return value;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists() {
		EntityPath<?> entityPath = (EntityPath<?>) queryMixin.getMetadata().getJoins().get(0).getTarget();
		return !limit(1).list(entityPath).isEmpty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <RT> List<RT> list(Expression<RT> expr) {
		try {
			TypedSQLQuery<?> query = createQuery(expr);
			return (List<RT>) getResultList(query);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void validateExpressions(Expression<?>... args) {
		for (Expression<?> arg : args) {
			if (ReflectionUtils.isCollection(arg.getType())) {
				throw new OSQLException(
						"A expressão "
								+ arg
								+ " não pode ser usado para criação da consulta pois é uma coleção. Use uma junção para isto ou use o método List passando apenas a expressão que representa a coleção.");
			}
		}
	}

	private List<?> getResultList(TypedSQLQuery<?> query) throws Exception {
		if (projection != null) {
			if (projection instanceof FactoryExpression) {
				query.allowDuplicateObjects(true);
				List<?> results = query.getResultList();
				for (Object o : results)
					System.out.println(o);
				List<Object> rv = new ArrayList<Object>(results.size());

				for (Object o : results) {
					List<Object> values = new ArrayList<Object>();
					EntityCache entityCache = session.getEntityCacheManager().getEntityCache(o.getClass());
					for (Expression<?> arg : projection.getArgs()) {
						DescriptionField field = entityCache.getDescriptionField(((Path<?>) arg).getMetadata().getElement().toString());
						values.add(field.getObjectValue(o));
					}
					rv.add(projection.newInstance(values.toArray()));
				}

				return rv;
			} else
				return null;
		} else {
			query.allowDuplicateObjects(true);
			return query.getResultList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <RT> RT uniqueResult(Expression<RT> expr) {
		try {
			TypedSQLQuery<?> query = createQuery(expr);
			return (RT) getSingleResult(query);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Tuple uniqueResult(Expression<?>... args) {
		validateExpressions(args);
		return uniqueResult(queryMixin.createProjection(args));
	}

	private Object getSingleResult(SQLQuery query) throws Exception {
		if (projection != null) {
			Object result = query.getSingleResult();
			if (result != null) {
				if (!result.getClass().isArray()) {
					result = new Object[] { result };
				}
				return projection.newInstance((Object[]) result);
			} else {
				return null;
			}
		} else {
			return query.getSingleResult();
		}
	}

	@Override
	public List<Tuple> list(Expression<?>... args) {
		validateExpressions(args);
		return list(queryMixin.createProjection(args));
	}

	public TypedSQLQuery<?> createQuery(Expression<?> expr) throws Exception {
		queryMixin.addProjection(adaptExpressionPatternAnteros(expr));
		return createQuery(getMetadata().getModifiers(), false, getResultClassByExpression(expr));
	}

	public TypedSQLQuery<?> createQuery(Expression<?> expr1, Expression<?> expr2, Expression<?>... rest) throws Exception {
		queryMixin.addProjection(adaptExpressionPatternAnteros(expr1));
		queryMixin.addProjection(adaptExpressionPatternAnteros(expr2));
		queryMixin.addProjection(adaptExpressionPatternAnteros(rest));
		return createQuery(getMetadata().getModifiers(), false, getResultClassByExpression(expr1));
	}

	protected Class<?> getResultClassByExpression(Expression<?>... expr) {
		if (expr.length > 0) {
			if (expr[0] instanceof EntityPathBase) {
				return getClassByEntityPath((EntityPath<?>) expr[0]);
			}
			if (queryMixin.getMetadata().getJoins().size() > 0) {
				JoinExpression expression = queryMixin.getMetadata().getJoins().get(0);
				return getClassByEntityPath((EntityPath<?>) expression.getTarget());
			}
		}
		return null;
	}

	/**
	 * Expose the original JPA query for the given projection
	 *
	 * @param args
	 * @return
	 */
	public TypedSQLQuery<?> createQuery(Expression<?>[] args) {
		queryMixin.addProjection(args);
		return createQuery(getMetadata().getModifiers(), false, getResultClassByExpression(args));
	}

	private TypedSQLQuery<?> createQuery(QueryModifiers modifiers, boolean forCount, Class<?> resultClass) {
		SQLSerializer serializer = serialize(forCount);
		String queryString = serializer.toString();
		System.out.println(queryString);
		System.out.println("---------------------------------");
		TypedSQLQuery<?> query;
		try {
			query = session.createQuery(queryString, resultClass);
		} catch (Exception e) {
			throw new OSQLException("Não foi possível criar a query. ", e);
		}

		List<? extends Expression<?>> projection = getMetadata().getProjection();

		FactoryExpression<?> wrapped = projection.size() > 1 ? FactoryExpressionUtils.wrap(projection) : null;

		if (!forCount && ((projection.size() == 1 && projection.get(0) instanceof FactoryExpression) || wrapped != null)) {
			this.projection = (FactoryExpression<?>) projection.get(0);
			if (wrapped != null) {
				this.projection = wrapped;
				getMetadata().clearProjection();
				getMetadata().addProjection(wrapped);
			}
		}

		return query;
	}

	protected Expression<?>[] getAllFieldExpressions(EntityPathBase<?> path) throws Exception {
		List<Expression<?>> result = new ArrayList<Expression<?>>();

		Field[] fields = ReflectionUtils.getAllDeclaredFields(path.getClass());

		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers())) {
				if ((ReflectionUtils.isExtendsClass(Path.class, field.getType()))
						&& (!ReflectionUtils.isExtendsClass(EntityPath.class, field.getType()))
						&& ((!ReflectionUtils.isExtendsClass(CollectionPathBase.class, field.getType())))) {
					Expression<?> expr = (Expression<?>) field.get(path);
					result.add(expr);
				}
			}
		}

		return result.toArray(new Expression<?>[] {});
	}

	public Class<?> getClassByEntityPath(EntityPath<?> path) {
		Type mySuperclass = path.getClass().getGenericSuperclass();
		return (Class<?>) ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
	}

	protected Expression<?>[] adaptExpressionPatternAnteros(Expression<?>... expr) throws Exception {
		List<Expression<?>> result = new ArrayList<Expression<?>>();
		for (Expression<?> e : expr) {
			if (e instanceof EntityPathBase) {
				Expression<?>[] expressions = getAllFieldExpressions((EntityPathBase<?>) e);
				for (Expression<?> e1 : expressions) {
					result.add(e1);
				}
			} else
				result.add(e);
		}
		return result.toArray(new Expression<?>[] {});
	}

	/*
	 * Sobrescrevendo métodos para controlar quando não foi adicionado condição para o Join. Desta forma assume que o
	 * join será feito com a primeira tabela do From e adiciona-se o Join automagicamente.
	 */
	@Override
	public Q groupBy(Expression<?>... o) {
		checkLastJoinAdded();
		return super.groupBy(o);
	}

	@Override
	public Q groupBy(Expression<?> e) {
		checkLastJoinAdded();
		return super.groupBy(e);
	}

	@Override
	public Q having(Predicate... o) {
		checkLastJoinAdded();
		return super.having(o);
	}

	@Override
	public Q having(Predicate e) {
		checkLastJoinAdded();
		return super.having(e);
	}

	@Override
	public Q join(EntityPath<?> target) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.JOIN;
		return super.join(target);
	}

	@Override
	public Q join(SubQueryExpression<?> target, Path<?> alias) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.JOIN;
		return super.join(target, alias);
	}

	@Override
	public Q leftJoin(EntityPath<?> target) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.LEFTJOIN;
		return super.leftJoin(target);
	}

	@Override
	public Q leftJoin(SubQueryExpression<?> target, Path<?> alias) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.LEFTJOIN;
		return super.leftJoin(target, alias);
	}

	@Override
	public Q rightJoin(EntityPath<?> target) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.RIGHTJOIN;
		return super.rightJoin(target);
	}

	@Override
	public Q rightJoin(SubQueryExpression<?> target, Path<?> alias) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.RIGHTJOIN;
		return super.rightJoin(target, alias);
	}

	@Override
	public Q innerJoin(EntityPath<?> target) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.INNERJOIN;
		return super.innerJoin(target);
	}

	@Override
	public Q innerJoin(SubQueryExpression<?> target, Path<?> alias) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.INNERJOIN;
		return super.innerJoin(target, alias);
	}

	@Override
	public Q fullJoin(EntityPath<?> target) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.FULLJOIN;
		return super.fullJoin(target);
	}

	@Override
	public Q fullJoin(SubQueryExpression<?> target, Path<?> alias) {
		checkLastJoinAdded();
		lastJoinAdded = target;
		lastJoinTypeAdded = JoinType.FULLJOIN;
		return super.fullJoin(target, alias);
	}

	@Override
	public Q where(Predicate... o) {
		checkLastJoinAdded();
		return super.where(o);
	}

	@Override
	public Q where(Predicate o) {
		checkLastJoinAdded();
		return super.where(o);
	}

	@Override
	public Q on(Predicate condition) {
		lastJoinConditionAdded = true;
		return super.on(condition);
	}

	@Override
	public Q on(Predicate... conditions) {
		lastJoinConditionAdded = true;
		return super.on(conditions);
	}

	@Override
	public Q from(Expression<?> arg) {
		if (joinTarget == null)
			joinTarget = arg;
		return super.from(arg);
	}

	@Override
	public Q from(Expression<?>... args) {
		if (joinTarget == null)
			joinTarget = args[0];
		return super.from(args);
	}

	private void checkLastJoinAdded() {
		// Verifica se foi adicionado Join porém não foi adicionado condição
		if ((lastJoinAdded != null) && (!lastJoinConditionAdded) && (joinTarget != null)) {
			if ((lastJoinAdded instanceof EntityPath) && (joinTarget instanceof EntityPath)) {
			}
		}
		lastJoinAdded = null;
		lastJoinConditionAdded = false;
	}

	protected void reset() {
		queryMixin.getMetadata().reset();
	}

	@Override
	public CloseableIterator<Tuple> iterate(Expression<?>... args) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <RT> CloseableIterator<RT> iterate(Expression<RT> expr) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <RT> SearchResults<RT> listResults(Expression<RT> projection) {
		throw new UnsupportedOperationException();
	}

	public SQLSession getSession() {
		return session;
	}

}
