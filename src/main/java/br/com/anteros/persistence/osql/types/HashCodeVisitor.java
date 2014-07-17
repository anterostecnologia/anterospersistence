package br.com.anteros.persistence.osql.types;

/**
 * HashCodeVisitor is used for hashCode generation in {@link Expression}
 * implementations.
 *
 * @author tiwe
 */
public final class HashCodeVisitor implements Visitor<Integer, Void> {

	public static final HashCodeVisitor DEFAULT = new HashCodeVisitor();

	private HashCodeVisitor() {
	}

	public Integer visit(Constant<?> expr, Void context) {
		return expr.getConstant().hashCode();
	}

	public Integer visit(FactoryExpression<?> expr, Void context) {
		int result = expr.getType().hashCode();
		return 31 * result + expr.getArgs().hashCode();
	}

	public Integer visit(Operation<?> expr, Void context) {
		int result = expr.getOperator().hashCode();
		return 31 * result + expr.getArgs().hashCode();
	}

	public Integer visit(ParamExpression<?> expr, Void context) {
		return expr.getName().hashCode();
	}

	public Integer visit(Path<?> expr, Void context) {
		return expr.getMetadata().hashCode();
	}

	public Integer visit(SubQueryExpression<?> expr, Void context) {
		return expr.getMetadata().hashCode();
	}

	public Integer visit(TemplateExpression<?> expr, Void context) {
		int result = expr.getTemplate().hashCode();
		return 31 * result + expr.getArgs().hashCode();
	}

}
