/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package br.com.anteros.persistence.dsl.osql.lang;

import java.util.Collection;
import java.util.Map;

/**
 * General assertion utilities
 * 
 * @author sasa
 * @author tiwe
 * 
 */
public final class Assert {

    private static final String IS_FALSE = "is false";
    
    private static final String IS_TRUE = "is true";
    
    private static final String IS_EMPTY = "should not be empty";

    private static final String IS_NULL = "should not be null";

    private static final String HAS_NO_TEXT = "should have text";

    private Assert() {
    }

    /**
     * Assert that the given String has actual non-whitepsace text.
     * 
     * @param str
     * @param propOrMsg
     * @return
     */
    public static String hasText(String str, String propOrMsg) {
        boolean hasText = false;
        int strLen = hasLength(str, propOrMsg).length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                hasText = true;
            }
        }
        return assertThat(hasText, propOrMsg, HAS_NO_TEXT, str);
    }

    /**
     * Assert that the given object is not null
     * 
     * @param <T>
     * @param object
     * @param propOrMsg
     * @return
     */
    public static <T> T notNull(T object, String propOrMsg) {
        return assertThat(object != null, propOrMsg, IS_NULL, object);
    }

    /**
     * Assert that the given String is not empty
     * 
     * @param str
     * @param propOrMsg
     * @return
     */
    public static String hasLength(String str, String propOrMsg) {
        return assertThat(str != null && str.length() > 0, propOrMsg, IS_EMPTY,
                str);
    }

    /**
     * Assert that the given array is not empty
     * 
     * @param <T>
     * @param objects
     * @param propOrMsg
     * @return
     */
    public static <T> T[] notEmpty(T[] objects, String propOrMsg) {
        return assertThat(objects != null && objects.length > 0, propOrMsg,
                IS_EMPTY, objects);
    }

    /**
     * Assert that the given Map is not empty
     * 
     * @param <M>
     * @param map
     * @param propOrMsg
     * @return
     */
    public static <M extends Map<?, ?>> M notEmpty(M map, String propOrMsg) {
        return assertThat(!map.isEmpty(), propOrMsg, IS_EMPTY, map);
    }

    /**
     * Assert that the given Collection is not empty
     * 
     * @param <C>
     * @param col
     * @param propOrMsg
     * @return
     */
    public static <C extends Collection<?>> C notEmpty(C col, String propOrMsg) {
        return assertThat(!col.isEmpty(), propOrMsg, IS_EMPTY, col);
    }
    
    /**
     * Assert that the given condition is true
     * 
     * @param condition
     * @param propOrMsg
     * @return
     */
    public static boolean isTrue(boolean condition, String propOrMsg){
	return assertThat(condition, propOrMsg, IS_TRUE, condition);
    }
    
    /**
     * Assert that the given condition is true
     * 
     * @param condition
     * @param propOrMsg
     * @return
     */
    public static boolean isFalse(boolean condition, String propOrMsg){
	return assertThat(!condition, propOrMsg, IS_FALSE, condition);
    }

    /**
     * General assertion mwthos
     * 
     * @param <T>
     * @param condition
     * @param propOrMsg
     * @param msgSuffix
     * @param rv
     * @return
     */
    public static <T> T assertThat(boolean condition, String propOrMsg, String msgSuffix, T rv) {
        if (!condition) {
            if (propOrMsg.contains(" ")) {
                throw new IllegalArgumentException(propOrMsg);
            } else {
                throw new IllegalArgumentException(propOrMsg + " " + msgSuffix);
            }
        }
        return rv;
    }

}