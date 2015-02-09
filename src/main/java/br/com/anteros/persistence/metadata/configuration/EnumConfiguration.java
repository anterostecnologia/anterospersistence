package br.com.anteros.persistence.metadata.configuration;

import java.io.Serializable;

public class EnumConfiguration extends EntityConfiguration {

	public EnumConfiguration(Class<? extends Serializable> sourceClazz, PersistenceModelConfiguration model) {
		super(sourceClazz, model);
	}


}
