/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.persistence.proxy.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import br.com.anteros.persistence.proxy.SimpleLazyLoadInterceptor;

public class SQLLazyLoadList<T> extends AbstractSQLList<T> {

	private SimpleLazyLoadInterceptor interceptor;
	private boolean initialized = false;

	public SQLLazyLoadList(SimpleLazyLoadInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public void add(int index, T object) {
		initializeList();
		super.add(index, object);
	}

	@Override
	public boolean addAll(Collection collection) {
		initializeList();
		return super.addAll(collection);
	}

	@Override
	public T get(int index) {
		initializeList();
		return super.get(index);
	}

	@Override
	public T remove(int index) {
		initializeList();
		return super.remove(index);
	}

	@Override
	public boolean removeAll(Collection collection) {
		initializeList();
		return super.removeAll(collection);
	}

	@Override
	public boolean add(T object) {
		initializeList();
		return super.add(object);
	}

	@Override
	public boolean addAll(int location, Collection collection) {
		initializeList();
		return super.addAll(location, collection);
	}

	@Override
	public int size() {
		initializeList();
		return super.size();
	}

	@Override
	public void clear() {
		initializeList();
		super.clear();
	}

	@Override
	public Iterator iterator() {
		initializeList();
		return super.iterator();
	}

	@Override
	public ListIterator listIterator() {
		initializeList();
		return super.listIterator();
	}

	@Override
	public ListIterator listIterator(int location) {
		initializeList();
		return super.listIterator(location);
	}

	@Override
	public int indexOf(Object object) {
		initializeList();
		return super.indexOf(object);
	}

	@Override
	public boolean contains(Object object) {
		initializeList();
		return super.contains(object);
	}

	@Override
	public boolean containsAll(Collection collection) {
		initializeList();
		return super.containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		initializeList();
		return super.isEmpty();
	}

	@Override
	public int lastIndexOf(Object object) {
		initializeList();
		return super.lastIndexOf(object);
	}

	@Override
	public boolean remove(Object object) {
		initializeList();
		return super.remove(object);
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		initializeList();
		super.removeRange(fromIndex, toIndex);
	}

	@Override
	public boolean retainAll(Collection collection) {
		initializeList();
		return super.retainAll(collection);
	}

	@Override
	public T set(int index, T object) {
		initializeList();
		return super.set(index, object);
	}

	@Override
	public Object[] toArray() {
		initializeList();
		return super.toArray();
	}

	@Override
	public Object[] toArray(Object[] contents) {
		initializeList();
		return super.toArray(contents);
	}

	@Override
	public List subList(int start, int end) {
		initializeList();
		return super.subList(start, end);
	}

	@Override
	public void trimToSize() {
		initializeList();
		super.trimToSize();
	}

	@Override
	public void ensureCapacity(int minimumCapacity) {
		initializeList();
		super.ensureCapacity(minimumCapacity);
	}

	@Override
	public Object clone() {
		initializeList();
		return super.clone();
	}

	@Override
	public boolean equals(Object object) {
		initializeList();
		return super.equals(object);
	}

	@Override
	public String toString() {
		initializeList();
		return super.toString();
	}

	private synchronized void initializeList() {
		if (!initialized) {
			initialized = true;
			try {
				List targetObject = (List) interceptor.getTargetObject();
				this.addAll(targetObject);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
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
		initializeList();
	}

}
