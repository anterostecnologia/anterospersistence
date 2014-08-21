/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package br.com.anteros.persistence.dsl.osql.lang;

/**
 * Typed pair of values
 * 
 * @author tiwe
 */
public class Pair<F, S> {

    private final F first;

    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> of(F f, S s) {
        return new Pair<F, S>(f, s);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Pair) {
            Pair p = (Pair)o;
            return equals(first, p.first) && equals(second, p.second);
        } else {
            return false;
        }
    }
    
    private static boolean equals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }    

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return 31 * (first != null ? first.hashCode() : 0) 
                   + (second != null ? second.hashCode() : 0);
    }

}
