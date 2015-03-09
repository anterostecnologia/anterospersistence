package br.com.anteros.persistence.sql.parser.node;

public class CastFunctionNode extends FunctionNode {

	public CastFunctionNode(String functionName, int offset, int length, int scope) {
		super(functionName, offset, length, scope);
	}

}
