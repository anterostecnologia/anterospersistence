/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package br.com.anteros.persistence.dsl.osql.lang;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Iterator with Closeable
 * 
 * @author tiwe
 * @version $Id$
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
    
    /**
     * Closes this iterator and releases any system resources associated
     * with it. If the iterator is already closed then invoking this 
     * method has no effect. 
     */
    void close();

}
