package br.com.anteros.persistence.dsl.osql.types.expr.params;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class EnumParam extends Param<Enum> {

	private static final long serialVersionUID = 1L;

	public EnumParam(String name) {
		super(Enum.class, name);
	}

	public EnumParam() {
		super(Enum.class);
	}

}
