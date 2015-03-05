package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.Node;

public class CommentNode extends Node {

	public CommentNode(String name, int offset, int length, int scope) {
		super(name, offset, length, scope);
	}

	public CommentNode(String name) {
		super(name);
	}

}
