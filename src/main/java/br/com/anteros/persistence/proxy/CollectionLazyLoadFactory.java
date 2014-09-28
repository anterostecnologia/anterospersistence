package br.com.anteros.persistence.proxy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.proxy.collection.AnterosPersistentCollection;
import br.com.anteros.persistence.proxy.collection.SQLLazyLoadList;
import br.com.anteros.persistence.proxy.collection.SQLLazyLoadMap;
import br.com.anteros.persistence.proxy.collection.SQLLazyLoadSet;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

public class CollectionLazyLoadFactory implements LazyLoadFactory {

	@Override
	public Object createProxy(SQLSession session, Object targetObject, DescriptionField descriptionField,
			EntityCache targetEntityCache,
			Map<String, Object> columnKeyValues, Cache transactionCache) throws Exception {
		Object newObject = null;
		CollectionLazyLoadInterceptor lazyLoadInterceptor = new CollectionLazyLoadInterceptor(session, targetEntityCache,
				columnKeyValues,
				transactionCache, targetObject, descriptionField);
		if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class))
			newObject = new SQLLazyLoadSet(lazyLoadInterceptor);
		else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class))
			newObject = new SQLLazyLoadList(lazyLoadInterceptor);
		else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Map.class))
			newObject = new SQLLazyLoadMap(session,targetEntityCache,descriptionField);
		return newObject;
	}

	@Override
	public boolean proxyIsInitialized(Object object) throws Exception {
		if (object instanceof AnterosPersistentCollection)
			return ((AnterosPersistentCollection) object).isInitialized();
		return false;
	}

	@Override
	public boolean isProxyObject(Object object) throws Exception {
		if (object instanceof AnterosPersistentCollection) 
			return ((AnterosPersistentCollection) object).isProxied();
		return false;
	}

}
