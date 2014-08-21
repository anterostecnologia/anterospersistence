package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.ParserVisitor;

public class LimitNode extends KeywordNode {

	public LimitNode(int offset, int length, int scope) {
		super("limit", offset, length, scope);
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}