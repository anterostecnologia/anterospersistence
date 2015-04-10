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
package br.com.anteros.persistence.translation;

import br.com.anteros.core.translation.AbstractCoreTranslate;
import br.com.anteros.core.translation.TranslateMessage;

public class AnterosPersistenceTranslate extends AbstractCoreTranslate {
	

	private static AnterosPersistenceTranslate singleton;

	public static AnterosPersistenceTranslate getInstance() {
        if ( singleton == null )
            singleton = new AnterosPersistenceTranslate(AnterosPersistenceTranslateMessages.class);

        return (AnterosPersistenceTranslate) singleton;
    }    
	
	public AnterosPersistenceTranslate(Class<? extends TranslateMessage> translateClass) {
		super(translateClass);
	}
}
