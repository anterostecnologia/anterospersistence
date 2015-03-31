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

import java.io.Closeable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.dsl.osql.lang.CloseableIterator;
import br.com.anteros.persistence.dsl.osql.support.QueryMixin;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpressionUtils;
import br.com.anteros.persistence.dsl.osql.types.IndexHint;
import br.com.anteros.persistence.dsl.osql.types.Operation;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.params.DateParam;
import br.com.anteros.persistence.dsl.osql.types.expr.params.DateTimeParam;
import br.com.anteros.persistence.dsl.osql.types.expr.params.EnumParam;
import br.com.anteros.persistence.handler.ResultClassDefinition;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.parameter.EnumeratedParameter;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.parameter.type.EnumeratedFormatSQL;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.session.query.SQLQueryNoResultException;

/**
 * AbstractOSQLQuery é a classe base para implementações de consultas SQL.
 *
 * @author tiwe modified by: Edson Martins
 *
 * @param <Q>
 *            subtipo concreto
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

	protected LockOptions lockOptions = LockOptions.NONE;

	protected final Configuration configuration;

	protected List<IndexHint> indexHints = new ArrayList<IndexHint>();

	protected boolean readOnly = false;

	private long limit;

	private long offset;

	private boolean allowDuplicateObjects = false;

	public AbstractOSQLQuery(Configuration configuration) {
		this(null, configuration, new DefaultQueryMetadata().noValidate());
	}

	public AbstractOSQLQuery(SQLSession session, Configuration configuration) {
		this(session, configuration, new DefaultQueryMetadata().noValidate());
	}

	public AbstractOSQLQuery(SQLSession session, Configuration configuration, QueryMetadata metadata) {
		super(new QueryMixin<Q>(metadata, false), configuration.getTemplates());
		this.session = session;
		this.useLiterals = true;
		this.analyser = new SQLAnalyser(this.getMetadata(), configuration, null);
		this.configuration = configuration;
	}

	protected SQLSerializer createSerializer() {
		try {
			SQLSerializer serializer = new SQLSerializer(configuration, getAnalyser());

			serializer.setUseLiterals(useLiterals);
			return serializer;
		} catch (Exception ex) {
			throw new OSQLQueryException(ex);
		}
	}

	/**
	 * Retorna a instância do analisador das expressões Sql.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected SQLAnalyser getAnalyser() throws Exception {
		return analyser;
	}

	/**
	 * Configura se o sql deve ser gerado usando literais para conversão de tipos em string.
	 * 
	 * @param useLiterals
	 */
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
		validateSession();
		SQLQuery query = createQuery(null, true);
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

	/**
	 * Valida se a sessão sql foi configurado corretamente. É possível criar uma consulta sem atribuir a sessão e
	 * atribuí-la posteriormente no momento de usá-la criando um clone usando o método {@link #clone(SQLSession)}.
	 */
	protected void validateSession() {
		if (session == null)
			throw new OSQLQueryException("Sessão não configurada para execução da consulta.");
	}

	@Override
	public boolean exists() {
		validateSession();
		EntityPath<?> entityPath = (EntityPath<?>) queryMixin.getMetadata().getJoins().get(0).getTarget();
		return !limit(1).list(entityPath).isEmpty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <RT> List<RT> list(Expression<RT> expr) {
		validateSession();
		try {
			validateExpressions(expr);
			SQLQuery query = createQuery(expr);
			return (List<RT>) getResultList(query);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Valida as expressões passadas como argumento para projeção.
	 * 
	 * @param args
	 *            Expressões
	 */
	protected void validateExpressions(Expression<?>... args) {
		for (Expression<?> arg : args) {
			if (arg instanceof EntityPath<?>) {
				List<JoinExpression> joins = getMetadata().getJoins();
				for (JoinExpression expr : joins) {
					if ((expr.getTarget() != null) && (expr.getTarget() instanceof Operation<?>)) {
						throw new OSQLQueryException("Não é possível projetar entidades a partir de SQL's onde foram usadas SubQueries na cláusula From.");
					}
				}
			}
			if (ReflectionUtils.isCollection(arg.getType())) {
				throw new OSQLQueryException(
						"A expressão "
								+ arg
								+ " não pode ser usado para criação da consulta pois é uma coleção. Use uma junção para isto ou use o método List passando apenas a expressão que representa a coleção.");
			}
		}
	}

	private List<?> getResultList(SQLQuery query) throws Exception {
		query.allowDuplicateObjects(true);
		if (projection != null) {
			List<?> results = query.getResultList();
			List<Object> rv = new ArrayList<Object>(results.size());
			for (Object o : results) {
				if (o != null) {
					if (!o.getClass().isArray()) {
						o = new Object[] { o };
					}
					rv.add(projection.newInstance((Object[]) o));
				} else {
					rv.add(null);
				}
			}
			return rv;
		} else {
			return query.getResultList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <RT> RT uniqueResult(Expression<RT> expr) {
		validateSession();
		try {
			SQLQuery query = createQuery(expr);
			return (RT) getSingleResult(query);
		} catch (SQLQueryNoResultException se) {
			return null;
		} catch (OSQLQueryException oe) {
			throw oe;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Tuple uniqueResult(Expression<?>... args) {
		validateSession();
		try {
			validateExpressions(args);
			return uniqueResult(queryMixin.createProjection(args));
		} catch (SQLQueryNoResultException se) {
			return null;
		} catch (Exception e) {
			throw new OSQLQueryException(e);
		}

	}

	private Object getSingleResult(SQLQuery query) throws Exception {
		validateSession();
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
		validateSession();
		try {
			validateExpressions(args);
			return list(queryMixin.createProjection(args));
		} catch (Exception e) {
			throw new OSQLQueryException(e);
		}
	}

	public SQLQuery createQuery(Expression<?> expr) throws Exception {
		queryMixin.addProjection(expr);
		return createQuery(getModifiers(), false);
	}

	public SQLQuery createQuery(Expression<?> expr1, Expression<?> expr2, Expression<?>... rest) throws Exception {
		queryMixin.addProjection(expr1);
		queryMixin.addProjection(expr2);
		queryMixin.addProjection(rest);
		return createQuery(getModifiers(), false);
	}

	/**
	 * Expose the original JPA query for the given projection
	 *
	 * @param args
	 * @return
	 */
	public SQLQuery createQuery(Expression<?>[] args) {
		queryMixin.addProjection(args);
		return createQuery(getModifiers(), false);
	}

	private SQLQuery createQuery(QueryModifiers modifiers, boolean forCount) {
		SQLQuery query = null;
		try {
			/*
			 * Analisa as expressões
			 */
			analyser.setUnion(union);
			analyser.process();
			/*
			 * Serializa o SQL.
			 */
			SQLSerializer serializer = serialize(forCount);
			String sql = serializer.toString();
			System.out.println(sql);
			System.out.println();
			/*
			 * Cria a query para execução passando a lista de classes de resultados esperadas.
			 */
			query = session.createQuery(sql);
			query.addResultClassDefinition(analyser.getResultClassDefinitions().toArray(new ResultClassDefinition[] {}));
			query.setMaxResults(modifiers.getLimitAsInteger());
			query.setFirstResult(modifiers.getOffsetAsInteger());
			query.setLockOptions(lockOptions);
			query.allowDuplicateObjects(allowDuplicateObjects);
			query.nextAliasColumnName(configuration.getNextAliasColumnName());
			

		} catch (Exception e) {
			throw new OSQLQueryException("Não foi possível criar a query. ", e);
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

	/**
	 * Converte os parâmetros no formato de expressão para o formato da query de consulta.
	 * 
	 * @return Lista de parâmetros convertidos.
	 */
	private Object getParameters() {
		Set<Object> result = new LinkedHashSet<Object>();
		for (QueryMetadata metadata : analyser.getAllMetadatas()) {
			Map<ParamExpression<?>, Object> params = metadata.getParams();
			if (analyser.isNamedParameter()) {
				for (ParamExpression<?> param : params.keySet()) {
					if (param instanceof DateParam)
						result.add(new NamedParameter(param.getName(), params.get(param), TemporalType.DATE));
					else if ((param instanceof DateTimeParam) || (param.getType() == Date.class))
						result.add(new NamedParameter(param.getName(), params.get(param), TemporalType.DATE_TIME));
					else if (param.getType() == Enum.class)
						if (param instanceof EnumParam)
							result.add(new EnumeratedParameter(param.getName(), ((EnumParam) param).getFormat(), (Enum<?>) params.get(param)));
						else
							result.add(new EnumeratedParameter(param.getName(), EnumeratedFormatSQL.STRING, (Enum<?>) params.get(param)));
					else
						result.add(new NamedParameter(param.getName(), params.get(param)));
				}
			} else {
				for (ParamExpression<?> param : params.keySet()) {
					result.add(params.get(param));
				}
			}
		}
		return (result.size() == 0 ? null : result);
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
		/*
		 * Verifica se foi adicionado Join porém não foi adicionado condição
		 */
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
		return iterate(queryMixin.createProjection(args));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <RT> CloseableIterator<RT> iterate(Expression<RT> expr) {
		validateSession();
		try {
			validateExpressions(expr);
			SQLQuery query = createQuery(expr);
			Closeable closeable = null;
			Iterator<RT> iterator = ((List<RT>) getResultList(query)).iterator();
			if (projection != null) {
				return new TransformingIterator<RT>(iterator, closeable, projection);
			} else {
				return new IteratorAdapter<RT>(iterator, closeable);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public SearchResults<Tuple> listResults(Expression<?>... args) {
		return listResults(queryMixin.createProjection(args));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <RT> SearchResults<RT> listResults(Expression<RT> projection) {
		validateSession();
		try {
			validateExpressions(projection);
			queryMixin.addProjection(projection);
			SQLQuery query = createQuery(projection);
			query.setMaxResults(0);
			query.setFirstResult(0);
			long total = query.count();
			query.setMaxResults((int) limit);
			query.setFirstResult((int) offset);
			List<RT> list = (List<RT>) getResultList(query);
			if (total > 0) {
				QueryModifiers modifiers = new QueryModifiers(limit, offset);
				return new SearchResults<RT>(list, modifiers, total);
			} else {
				return SearchResults.emptyResults();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			reset();
		}
	}

	public SQLSession getSession() {
		return session;
	}

	public AbstractOSQLQuery<Q> setLockOptions(LockOptions lockOptions) {
		this.lockOptions = lockOptions;
		return this;
	}

	public LockOptions getLockOptions() {
		return lockOptions;
	}

	public AbstractOSQLQuery<Q> indexHint(IndexHint... indexes) {
		for (IndexHint index : indexes) {
			indexHints.add(index);
		}
		this.getMetadata().setIndexHints(indexHints);
		return this;
	}

	public AbstractOSQLQuery<Q> addIndexHint(String alias, String indexName) {
		return indexHint(new IndexHint(alias, indexName));
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public AbstractOSQLQuery<Q> readOnly(boolean readOnly) {
		this.readOnly = readOnly;
		return this;
	}

	@Override
	public Q limit(long limit) {
		this.limit = limit;
		return (Q) this;
	}

	@Override
	public Q offset(long offset) {
		this.offset = offset;
		return (Q) this;
	}

	public boolean isAllowDuplicateObjects() {
		return allowDuplicateObjects;
	}

	public void setAllowDuplicateObjects(boolean allowDuplicateObjects) {
		this.allowDuplicateObjects = allowDuplicateObjects;
	}

	protected QueryModifiers getModifiers() {
		if (limit == 0)
			return new QueryModifiers();

		return new QueryModifiers(limit, (offset == 0 ? 1 : offset));
	}

}
