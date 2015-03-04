package br.com.anteros.persistence.dsl.osql.types.expr.params;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class LongParam extends Param<Long> {

	private static final long serialVersionUID = 1L;

	public LongParam(String name) {
		super(Long.class, name);
	}

	public LongParam() {
		super(Long.class);
	}
}