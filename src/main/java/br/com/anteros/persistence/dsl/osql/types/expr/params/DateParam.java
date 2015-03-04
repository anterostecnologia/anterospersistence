package br.com.anteros.persistence.dsl.osql.types.expr.params;

import java.util.Date;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class DateParam extends Param<Date> {

	private static final long serialVersionUID = 1L;

	public DateParam(String name) {
		super(Date.class, name);
	}

	public DateParam() {
		super(Date.class);
	}
}