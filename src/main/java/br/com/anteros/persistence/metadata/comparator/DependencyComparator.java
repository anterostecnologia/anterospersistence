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
package br.com.anteros.persistence.metadata.comparator;

import java.util.Comparator;

public class DependencyComparator implements Comparator<Class<?>> {

    public int compare(Class<?> c1, Class<?> c2) {
        if(c1 == null) {
            if(c2 == null) {
                return 0;
            } else {
                // Sort nullos primeiro
                return 1;
            }
        } else if(c2 == null) {
            // Sort nulos primeiro
            return -1;
        }
        
        //Neste ponto, sabemos que c1 e c2 não são nulos
        if(c1.equals(c2)) {
            return 0;
        }
        
        // Neste ponto, c1 e c2 não são nulos e não iguais, vamos  
        // compará-los para ver qual é "superior" na hierarquia de classes
        boolean c1Lower = c2.isAssignableFrom(c1);
        boolean c2Lower = c1.isAssignableFrom(c2);
        
        if(c1Lower && !c2Lower) {
            return 1;
        } else if(c2Lower && !c1Lower) {
            return -1;
        } 
        
        // Não importa, classificar de forma consistente em classname
        return c1.getName().compareTo(c2.getName());
    }
}
