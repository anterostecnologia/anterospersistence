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

/*******************************************************************************
 * Copyright (c) 2007 - 2009 ZIGEN
 * Eclipse Public License - v 1.0
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package br.com.anteros.persistence.sql.parser.exception;

public class LoopException extends ParserException {

	private static final long serialVersionUID = 4665013740576449878L;

	public static final String message = "SQLParser Loop error.";

	int maxLoop = 0;

	public LoopException(int maxLoop) {
		super(message, "", 0, 0);
		this.maxLoop = maxLoop;
	}

	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		sb.append(" Max same word is ");
		sb.append(maxLoop);
		return sb.toString();
	}

}
