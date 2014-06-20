/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package br.com.anteros.persistence.osql.lang;

import java.net.URI;

/**
 * URIResolver provides URI resolving functionality
 * 
 * @author tiwe
 * 
 */
public final class URIResolver {

    private URIResolver() {
    }

    private static final String VALID_SCHEME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

    /**
     * 
     * @param url
     * @return
     */
    public static boolean isAbsoluteURL(String url) {
        if (url == null) {
            return false;
        } else {
            int colonPos = url.indexOf(':');
            if (colonPos == -1) {
                return false;
            } else {
                for (int i = 0; i < colonPos; i++) {
                    if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    /**
     * 
     * @param base
     * @param url
     * @return
     */
    public static String resolve(String base, String url) {
        if (isAbsoluteURL(url)) {
            return url;
        } else if (url.startsWith("?")) {
            if (base.contains("?")) {
                return base.substring(0, base.lastIndexOf('?')) + url;
            } else {
                return base + url;
            }
        } else if (url.startsWith("#")) {
            if (base.contains("#")) {
                return base.substring(0, base.lastIndexOf('#')) + url;
            } else {
                return base + url;
            }
        } else {
            return URI.create(base).resolve(url).toString();
        }
    }

}
