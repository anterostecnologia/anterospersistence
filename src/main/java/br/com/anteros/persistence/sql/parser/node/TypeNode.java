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
package br.com.anteros.persistence.sql.parser.node;

import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.ParserVisitor;
import br.com.anteros.persistence.sql.parser.exception.UnexpectedTokenException;


public class TypeNode extends KeywordNode {
    boolean isPackageBody;

    TargetNode target;

	public TypeNode(String type, int offset, int length, int scope) {
		super(type, offset, length, scope);
	}

	public Object accept(ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

    public boolean isPackageBody() {
        return isPackageBody;
    }

    public void addChild(INode n){
    	if(target == null & n instanceof TargetNode){
    		super.addChild(n);
    		target = (TargetNode)n;
    	}else{
    		throw new UnexpectedTokenException(n.getName(), offset, length);
    	}
    }

    public void setPackageBody(boolean isPackageBody) {
    	if("package".equalsIgnoreCase(name)){
            this.isPackageBody = isPackageBody;
            if(isPackageBody){
                name = "package body";
            }else{
                name = "package";
            }
        }else{
            throw new IllegalStateException("This Node's ID is not package");
        }
    }

	public TargetNode getASTTarget() {
		return target;
	}

	public boolean hasTarget(){
		return target != null && !"".equals(target.toString());
	}
}
