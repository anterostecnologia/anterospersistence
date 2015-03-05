package br.com.anteros.persistence.dsl.osql.types.expr.params;

import br.com.anteros.persistence.dsl.osql.types.expr.Param;
import br.com.anteros.persistence.parameter.type.EnumeratedFormatSQL;

@SuppressWarnings("rawtypes")
public class EnumParam extends Param<Enum> {

	private static final long serialVersionUID = 1L;
	private EnumeratedFormatSQL format;

	public EnumParam(String name, EnumeratedFormatSQL format) {
		super(Enum.class, name);
		this.format = format;
	}

	public EnumParam(String name) {
		this(name, EnumeratedFormatSQL.STRING);
	}

	public EnumParam() {
		super(Enum.class);
	}

	public EnumeratedFormatSQL getFormat() {
		return format;
	}
}
