package br.com.anteros.persistence.osql;

import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.operation.Operation;

public final class HashCodeVisitor implements Visitor<Integer, Void> {

	public static final HashCodeVisitor DEFAULT = new HashCodeVisitor();

	private HashCodeVisitor() {
	}

	@Override
	public Integer visit(ConstantCondition<?> expr, Void context) {
		return expr.getConstant().hashCode();
	}

	@Override
	public Integer visit(FactoryCondition<?> expr, Void context) {
		int result = expr.getType().hashCode();
		return 31 * result + expr.getArguments().hashCode();
	}

	@Override
	public Integer visit(Operation<?> expr, Void context) {
		int result = expr.getOperator().hashCode();
		return 31 * result + expr.getArguments().hashCode();
	}

	@Override
	public Integer visit(ParameterCondition<?> expr, Void context) {
		return expr.getName().hashCode();
	}

	@Override
	public Integer visit(Attribute<?> expr, Void context) {
		return expr.getDescriptor().hashCode();
	}

	@Override
	public Integer visit(SubQueryCondition<?> expr, Void context) {
		return expr.getDescriptor().hashCode();
	}

	@Override
	public Integer visit(CodeTemplateCondition<?> expr, Void context) {
		int result = expr.getTemplate().hashCode();
		return 31 * result + expr.getArguments().hashCode();
	}

}
