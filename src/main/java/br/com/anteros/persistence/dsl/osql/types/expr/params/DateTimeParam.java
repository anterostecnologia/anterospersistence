package br.com.anteros.persistence.dsl.osql.types.expr.params;

import java.util.Date;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class DateTimeParam extends Param<Date> {

	private static final long serialVersionUID = 1L;

	public DateTimeParam(String name) {
		super(Date.class, name);
	}

	public DateTimeParam() {
		super(Date.class);
	}
}
