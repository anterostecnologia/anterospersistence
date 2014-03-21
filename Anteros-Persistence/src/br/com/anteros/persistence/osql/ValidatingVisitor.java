package br.com.anteros.persistence.osql;

import java.io.Serializable;
import java.util.Set;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.JoinCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.Operation;
import br.com.anteros.persistence.osql.query.QueryDescriptor;
import static br.com.anteros.persistence.util.CollectionUtils.*;

@SuppressWarnings("serial")
public final class ValidatingVisitor implements Visitor<Set<Condition<?>>, Set<Condition<?>>>, Serializable {

    public static final ValidatingVisitor DEFAULT = new ValidatingVisitor();

    private final String errorTemplate;

    public ValidatingVisitor() {
        this.errorTemplate = "Undeclared path '%s'. Add this path as a source to the query to be able to reference it.";
    }

    public ValidatingVisitor(String errorTemplate) {
        this.errorTemplate = errorTemplate;
    }

    @Override
    public Set<Condition<?>> visit(ConstantCondition<?> expr, Set<Condition<?>> known) {
        return known;
    }

    @Override
    public Set<Condition<?>> visit(FactoryCondition<?> expr, Set<Condition<?>> known) {
        for (Condition<?> arg : expr.getArguments()) {
            known = arg.accept(this, known);
        }
        return known;
    }

    @Override
    public Set<Condition<?>> visit(Operation<?> expr, Set<Condition<?>> known) {
        if (expr.getOperator() == Operators.ALIAS) {
            known = add(known, expr.getArgument(1));
        }
        for (Condition<?> arg : expr.getArguments()) {
            known = arg.accept(this, known);
        }
        return known;
    }

    @Override
    public Set<Condition<?>> visit(ParameterCondition<?> expr, Set<Condition<?>> known) {
        return known;
    }

    @Override
    public Set<Condition<?>> visit(Attribute<?> expr, Set<Condition<?>> known) {
        if (!known.contains(expr.getRoot())) {
            throw new IllegalArgumentException(String.format(errorTemplate,  expr.getRoot()));
        }
        return known;
    }

    @Override
    public Set<Condition<?>> visit(SubQueryCondition<?> expr, Set<Condition<?>> known) {
        Set<Condition<?>> old = known;
        final QueryDescriptor md = expr.getDescriptor();
        known = visitJoins(md.getJoins(), known);
        for (Condition<?> p : md.getProjection()) {
            known = p.accept(this, known);
        }
        for (OrderBy<?> o : md.getOrderBy()) {
            known = o.getTarget().accept(this, known);
        }
        for (Condition<?> g : md.getGroupBy()) {
            known = g.accept(this, known);
        }
        if (md.getHaving() != null) {
            known = md.getHaving().accept(this, known);
        }
        if (md.getWhere() != null) {
            known = md.getWhere().accept(this, known);
        }
        return old;
    }


    @Override
    public Set<Condition<?>> visit(CodeTemplateCondition<?> expr, Set<Condition<?>> known) {
        for (Object arg : expr.getArguments()) {
            if (arg instanceof Condition<?>) {
                known = ((Condition<?>)arg).accept(this, known);
            }
        }
        return known;
    }

    private Set<Condition<?>> visitJoins(Iterable<JoinCondition> joins, Set<Condition<?>> known) {
        for (JoinCondition j : joins) {
            final Condition<?> expr = j.getTarget();
            if (expr instanceof Attribute && ((AttributeImpl)expr).getDescriptor().isRoot()) {
                known = add(known, expr);
            } else {
                known = expr.accept(this, known);
            }
            if (j.getCondition() != null) {
                known = j.getCondition().accept(this, known);
            }
        }
        return known;
    }

}
