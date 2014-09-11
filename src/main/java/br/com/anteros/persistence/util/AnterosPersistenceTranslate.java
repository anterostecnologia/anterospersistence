package br.com.anteros.persistence.util;

import br.com.anteros.core.utils.AbstractCoreTranslate;

public class AnterosPersistenceTranslate extends AbstractCoreTranslate {
	
	public AnterosPersistenceTranslate(String messageBundleName) {
		super(messageBundleName);
	}

	private static AnterosPersistenceTranslate translate;
	
	public static AnterosPersistenceTranslate getInstance(){
		if (translate==null){
			translate = new AnterosPersistenceTranslate("anterospersistence_messages");
		}
		return translate;
	}
}
