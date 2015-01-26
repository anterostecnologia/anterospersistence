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
package br.com.anteros.persistence.validation.version;

import java.lang.reflect.Type;
import java.util.Date;

public class Versioning {

	public static Object incrementVersion(Object currentVersion, Type type) {
		Object result = null;
		if (type == Integer.class) {
			if (currentVersion == null)
				result = new Integer(1);
			else {
				result = new Integer(((Integer) currentVersion).intValue()+1);
			}
		} else if (type == Long.class) {
			if (currentVersion == null)
				result = new Long(1);
			else {
				result = new Long(((Long) currentVersion).longValue()+1);
			}
		} else if (type == Short.class) {
			if (currentVersion == null)
				result = Short.valueOf("1");
			else {
				result = new Short((short) (((Short) currentVersion).shortValue()+1));
			}
		} else if (type == Date.class) {
			result = new Date();
		}
		return result;
	}
}
