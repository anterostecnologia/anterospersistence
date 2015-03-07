/*******************************************************************************
 * Copyright 2011, Mysema Ltd
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
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
