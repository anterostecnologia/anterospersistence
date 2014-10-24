package br.com.anteros.persistence.proxy.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import br.com.anteros.persistence.proxy.SimpleLazyLoadInterceptor;

public class SQLLazyLoadSet<T> extends AbstractSQLSet<T> {

	private SimpleLazyLoadInterceptor interceptor;
	private boolean initialized = false;

	public SQLLazyLoadSet(SimpleLazyLoadInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public boolean add(T object) {
		initializeSet();
		return super.add(object);
	}
	
	@Override
	public boolean addAll(Collection collection) {
		initializeSet();
		return super.addAll(collection);
	}
	
	@Override
	public void clear() {
		initializeSet();
		super.clear();
	}
	
	@Override
	public Object clone() {
		initializeSet();
		return super.clone();
	}
	
	@Override
	public boolean contains(Object object) {
		initializeSet();
		return super.contains(object);
	}
	
	@Override
	public boolean containsAll(Collection collection) {
		initializeSet();
		return super.containsAll(collection);
	}
	
	@Override
	public boolean equals(Object object) {
		initializeSet();
		return super.equals(object);
	}
	
	@Override
	public boolean isEmpty() {
		initializeSet();
		return super.isEmpty();
	}
	
	@Override
	public Iterator iterator() {
		initializeSet();
		return super.iterator();
	}
	
	@Override
	public boolean remove(Object object) {
		initializeSet();
		return super.remove(object);
	}
	
	@Override
	public boolean removeAll(Collection collection) {
		initializeSet();
		return super.removeAll(collection);
	}
	
	@Override
	public boolean retainAll(Collection collection) {
		initializeSet();
		return super.retainAll(collection);
	}
	
	@Override
	public int size() {
		initializeSet();
		return super.size();
	}
	
	@Override
	public Object[] toArray() {
		initializeSet();
		return super.toArray();
	}
	
	@Override
	public Object[] toArray(Object[] contents) {
		initializeSet();
		return super.toArray(contents);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	private synchronized void initializeSet() {
		if (!initialized) {
			initialized = true;
			try {
				Set targetObject = (Set) interceptor.getTargetObject();
				this.addAll(targetObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	@Override
	public boolean isProxied() {
		return true;
	}
	
	@Override
	public void initialize() {
		initializeSet();
	}

}
