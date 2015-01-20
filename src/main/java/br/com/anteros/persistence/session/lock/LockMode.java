/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.session.lock;

public enum LockMode {

	NONE(0), READ(5), WRITE(10),

	OPTIMISTIC(6),

	OPTIMISTIC_FORCE_INCREMENT(7),

	PESSIMISTIC_READ(12),

	PESSIMISTIC_WRITE(13),

	PESSIMISTIC_FORCE_INCREMENT(17);
	
	private final int level;

	private LockMode(int level) {
		this.level = level;
	}

	public boolean greaterThan(LockMode mode) {
		return level > mode.level;
	}

	public boolean lessThan(LockMode mode) {
		return level < mode.level;
	}

}
