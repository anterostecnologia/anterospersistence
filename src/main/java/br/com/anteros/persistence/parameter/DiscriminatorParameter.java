package br.com.anteros.persistence.parameter;

import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;

public class DiscriminatorParameter extends SubstitutedParameter {

	public DiscriminatorParameter(String name, Class<?>[] value) {
		super(name, value);
	}

	@Override
	public String toString() {
		String result = "";
		for (Class<?> clazz : (Class<?>[]) this.getValue()) {
			if (!clazz.isAnnotationPresent(DiscriminatorValue.class)) {
				throw new NamedParameterException("A classe " + clazz.getName()
						+ " informada como parâmetro não possuí um configuração @DiscriminatorColumn.");
			}
			if (!"".equals(result))
				result += ",";
			result += "'" + clazz.getAnnotation(DiscriminatorValue.class).value() + "'";
		}
		return result;
	}

}
