package br.com.anteros.persistence.util;

import br.com.anteros.core.utils.AbstractCoreTranslate;

public class AnterosPersistenceTranslate extends AbstractCoreTranslate {
	
	private AnterosPersistenceTranslate(String messageBundleName) {
		super(messageBundleName);
	}	

	static {
		setInstance(new AnterosPersistenceTranslate("anterospersistence_messages"));
	}
}
