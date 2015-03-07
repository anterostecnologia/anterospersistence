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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Adapter implementation for Iterator and CloseableIterator instances
 * 
 * @author sasa
 * 
 */
public class IteratorAdapter<T> implements CloseableIterator<T> {

    private final Iterator<T> iter;
    
    private final Closeable closeable;

    public IteratorAdapter(Iterator<T> iter) {
        this.iter = iter;
        this.closeable = iter instanceof Closeable ? (Closeable)iter : null;
    }
    
    public IteratorAdapter(Iterator<T> iter, Closeable closeable) {
        this.iter = iter;
        this.closeable = closeable;
    }

    public static <T> List<T> asList(Iterator<T> iter) {
        List<T> list = new ArrayList<T>();
        try {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        } finally {
            if (iter instanceof Closeable) {
                try {
                    ((Closeable) iter).close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return list;
    }

    public List<T> asList() {
        return asList(iter);
    }

    public void close(){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public boolean hasNext() {
        return iter.hasNext();
    }

    public T next() {
        return iter.next();
    }

    public void remove() {
        iter.remove();
    }
}
