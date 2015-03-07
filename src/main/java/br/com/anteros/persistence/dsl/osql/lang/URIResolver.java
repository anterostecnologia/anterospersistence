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
