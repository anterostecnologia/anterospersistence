package br.com.anteros.persistence.dsl.osql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.dsl.osql.types.Constant;
import br.com.anteros.persistence.dsl.osql.types.EntityPath;
import br.com.anteros.persistence.dsl.osql.types.Expression;
import br.com.anteros.persistence.dsl.osql.types.FactoryExpression;
import br.com.anteros.persistence.dsl.osql.types.Operation;
import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.ParamExpression;
import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.dsl.osql.types.SubQueryExpression;
import br.com.anteros.persistence.dsl.osql.types.TemplateExpression;
import br.com.anteros.persistence.dsl.osql.types.Visitor;
import br.com.anteros.persistence.dsl.osql.types.expr.BooleanExpression;

public class SQLAnalyser implements Visitor<Void, Void> {

	private AbstractOSQLQuery<?> query;
	private Map<Path<?>, EntityPath<?>> aliases = new HashMap<Path<?>, EntityPath<?>>();
	private int nextAliasTableName = 1;

	public SQLAnalyser(AbstractOSQLQuery<?> query) {
		this.query = query;
	}

	@Override
	public Void visit(Constant<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(FactoryExpression<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(Operation<?> expr, Void context) {
		for (Expression<?> arg : expr.getArgs()) {
			arg.accept(this, null);
		}
		return null;
	}

	@Override
	public Void visit(ParamExpression<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(Path<?> expr, Void context) {
		try {
			makePossibleJoins(expr);
		} catch (Exception e) {
			throw new SQLAnalyserException(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Void visit(SubQueryExpression<?> expr, Void context) {
		return null;
	}

	@Override
	public Void visit(TemplateExpression<?> expr, Void context) {
		return null;
	}

	public void process() {
		QueryMetadata metadata = query.getMetadata();

		final List<Expression<?>> select = metadata.getProjection();
		final Predicate where = metadata.getWhere();
		final Predicate having = metadata.getHaving();
		final List<OrderSpecifier<?>> orderBy = metadata.getOrderBy();

		for (Expression<?> expr : select) {
			expr.accept(this, null);
		}
		if (where != null)
			where.accept(this, null);
		if (having != null)
			having.accept(this, null);
		if (orderBy != null) {
			for (OrderSpecifier<?> ord : orderBy) {
				ord.getTarget().accept(this, null);
			}
		}

	}

	protected void makePossibleJoins(Path<?> expr) throws Exception {
		if (expr.getMetadata().isRoot())
			return;
		makePossibleJoins(expr.getMetadata().getParent());
		if (expr instanceof EntityPath) {
			if (!aliases.containsKey(expr)) {
				String alias = makeNextAliasTableName();
				EntityPath<?> newPath = (EntityPath<?>) ReflectionUtils.invokeConstructor(expr.getClass(), alias);
				BooleanExpression boExpression = (BooleanExpression) ReflectionUtils.invokeMethod(expr, "eq", newPath);
				query.leftJoin(newPath).on(boExpression);
				aliases.put(expr, newPath);
			}
		}

	}

	public Map<Path<?>, EntityPath<?>> getAliases() {
		return aliases;
	}

	private String makeNextAliasTableName() {
		String result = "T_B_" + String.valueOf(nextAliasTableName);
		nextAliasTableName++;
		return result;
	}

	public EntityPath<?> getAliasByEntityPath(Path<?> path) {
		if (path instanceof EntityPath<?>) {
			return aliases.get(path);
		} else {
			EntityPath<?> result = aliases.get(path.getMetadata().getParent());
			if (result == null)
				result = (EntityPath<?>) path.getMetadata().getParent();
			return result;
		}
	}
}
