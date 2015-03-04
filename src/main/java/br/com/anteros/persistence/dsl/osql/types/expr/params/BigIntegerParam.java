package br.com.anteros.persistence.dsl.osql.types.expr.params;

import java.math.BigInteger;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;

public class BigIntegerParam extends Param<BigInteger> {

	private static final long serialVersionUID = 1L;

	public BigIntegerParam(String name) {
		super(BigInteger.class, name);
	}

	public BigIntegerParam() {
		super(BigInteger.class);
	}
}