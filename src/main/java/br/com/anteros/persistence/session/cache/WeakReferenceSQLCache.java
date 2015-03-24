package br.com.anteros.persistence.session.cache;

import br.com.anteros.core.utils.ConcurrentWeakHashMap;

public class WeakReferenceSQLCache extends SQLCache {
	
	public WeakReferenceSQLCache() {
		cache = new ConcurrentWeakHashMap<Object,Object>();
	}

}
