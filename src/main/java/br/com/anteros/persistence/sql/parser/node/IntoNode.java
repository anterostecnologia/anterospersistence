/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2007 - 2009 ZIGEN
 * Eclipse Public License - v 1.0
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.exception.UnexpectedTokenException;

public class IntoNode extends KeywordNode {
	boolean isIntoOutfileCause;
	
	OutfileNode outfile;
	
	public IntoNode(String name, int offset, int length, int scope) {
		super(name, offset, length, scope);
	}

	public void addChild(INode n) {
		if (!isIntoOutfileCause) {
			super.addChild(n);

			if (n instanceof OutfileNode) {
				isIntoOutfileCause = true;
				outfile = (OutfileNode)n;
			}

		}else{
			throw new UnexpectedTokenException(n.getName(), offset, length);
		}

	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (isIntoOutfileCause) {
			sb.append(" ");
			sb.append(outfile.getName());
			sb.append(" ");
		}
		return getNodeClassName() + " text=\"" + sb.toString() + "\"";
	}

	public OutfileNode getOutfile() {
		return outfile;
	}

	public boolean hasASTOutfile(){
		return isIntoOutfileCause;
	}
}
