/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.anteros.persistence.osql.types;

import java.util.List;

/**
 * FactoryExpression represents factory expressions such as JavaBean or
 * Constructor projections
 * 
 * @author tiwe
 *
 * @param <T>
 *            type of projection
 */
public interface FactoryExpression<T> extends Expression<T> {

	/**
	 * Get the invocation arguments
	 *
	 * @return
	 */
	List<Expression<?>> getArgs();

	/**
	 * Create a projection with the given arguments
	 *
	 * @param args
	 * @return
	 */

	T newInstance(Object... args);

}