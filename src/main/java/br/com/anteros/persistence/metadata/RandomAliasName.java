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
package br.com.anteros.persistence.metadata;

public class RandomAliasName {
	private static RandomAliasName instance;

	private RandomAliasName() {
	}

	public static String randomColumnName() {
		return random("c_o_l_");
	}

	public static String randomTableName() {
		return random("t_a_b_");
	}

	public static String random(String aliasName) {
		if (instance == null)
			instance = new RandomAliasName();
		return aliasName.concat(instance.getRandom());
	}

	private String getRandom() {
		return String.valueOf((int) (1 + (Math.random() * 999999)));
	}

	public static String randomConstraint(String columnName) {
		return random(columnName+"_");
	}
}
