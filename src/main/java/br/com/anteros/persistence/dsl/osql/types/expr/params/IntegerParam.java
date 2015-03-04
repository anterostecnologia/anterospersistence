package br.com.anteros.persistence.dsl.osql.types.expr.params;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class IntegerParam extends Param<Integer> {

	private static final long serialVersionUID = 1L;

	public IntegerParam(String name) {
		super(Integer.class, name);
	}

	public IntegerParam() {
		super(Integer.class);
	}
}