package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.ParserVisitor;

public class OffsetNode extends KeywordNode {

	public OffsetNode(int offset, int length, int scope) {
		super("offset", offset, length, scope);
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}