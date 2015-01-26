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
package br.com.anteros.persistence.proxy.collection;

import java.util.LinkedHashSet;

import br.com.anteros.persistence.proxy.AnterosProxyObject;

public class ProxiedSQLLazyLoadSet<T> extends LinkedHashSet<T> implements AnterosProxyCollection {

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public void initialize() {
	}

	@Override
	public Object initializeAndReturnObject() {
		return null;
	}

	@Override
	public boolean isProxied() {
		return false;
	}

}
