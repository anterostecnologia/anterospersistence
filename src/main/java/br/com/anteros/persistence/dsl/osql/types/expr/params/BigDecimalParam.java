package br.com.anteros.persistence.dsl.osql.types.expr.params;

import java.math.BigDecimal;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class BigDecimalParam extends Param<BigDecimal> {

	private static final long serialVersionUID = 1L;

	public BigDecimalParam(String name) {
		super(BigDecimal.class, name);
	}

	public BigDecimalParam() {
		super(BigDecimal.class);
	}
}