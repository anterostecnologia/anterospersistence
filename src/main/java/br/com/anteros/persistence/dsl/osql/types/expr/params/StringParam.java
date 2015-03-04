package br.com.anteros.persistence.dsl.osql.types.expr.params;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class StringParam extends Param<String> {

	private static final long serialVersionUID = 1L;

	public StringParam(String name) {
		super(String.class, name);
	}

	public StringParam() {
		super(String.class);
	}
}
