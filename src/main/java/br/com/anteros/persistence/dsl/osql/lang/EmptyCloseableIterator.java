/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package br.com.anteros.persistence.dsl.osql.lang;

import java.util.NoSuchElementException;

/**
 * Empty implementation of the CloseableIterator interface
 * 
 * @author tiwe
 * 
 */
public class EmptyCloseableIterator<T> implements CloseableIterator<T> {

    public boolean hasNext() {
        return false;
    }

    public T next() {
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void close(){

    }

}