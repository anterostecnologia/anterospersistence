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
package br.com.anteros.persistence.sql.parser;

import br.com.anteros.persistence.sql.parser.exception.ParserException;

public interface ISqlParser {

	public final static int SCOPE_DEFAULT = 0;

	public final static int SCOPE_SELECT = 1;

	public final static int SCOPE_FROM = 2;

	public final static int SCOPE_WHERE = 3;

	public final static int SCOPE_ORDER = 4;

	public final static int SCOPE_BY = 4;

	public final static int SCOPE_INSERT = 10;

	public final static int SCOPE_INTO = 11;

	public final static int SCOPE_INTO_COLUMNS = 12;

	public final static int SCOPE_VALUES = 13;

	public final static int SCOPE_UPDATE = 20;

	public final static int SCOPE_SET = 21;

	public final static int SCOPE_DELETE = 30;

	public final static int SCOPE_CREATE = 50;

	public final static int SCOPE_DROP = 60;

	public final static int SCOPE_TARGET = 70;

	public abstract void parse(INode node) throws ParserException;

	public abstract int getScope();

	public abstract String dump(INode node);

	public abstract String dumpXml(INode node);

	public abstract boolean isCanceled();

}
