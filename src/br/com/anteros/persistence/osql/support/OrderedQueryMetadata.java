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
package br.com.anteros.persistence.osql.support;

import java.util.List;

import br.com.anteros.persistence.osql.DefaultQueryMetadata;
import br.com.anteros.persistence.osql.JoinExpression;
import br.com.anteros.persistence.osql.JoinType;
import br.com.anteros.persistence.osql.types.Expression;

import com.google.common.collect.Lists;

/**
 * OrderedQueryMetadata performs no metadata validation and ensures that FROM elements are before 
 * JOIN elements
 * 
 * @author tiwe
 *
 */
public class OrderedQueryMetadata extends DefaultQueryMetadata {
    
    private static final long serialVersionUID = 6326236143414219377L;

    private List<JoinExpression> joins;
    
    public OrderedQueryMetadata() {
        super();
        noValidate();
    }
    
    @Override
    public void addJoin(JoinType joinType, Expression<?> expr) {
        joins = null;
        super.addJoin(joinType, expr);
    }
    
    @Override
    public List<JoinExpression> getJoins() {
        if (joins == null) {
            joins = Lists.newArrayList();
            int separator = 0; 
            for (JoinExpression j : super.getJoins()) {
                if (j.getType() == JoinType.DEFAULT) {
                    joins.add(separator++, j);
                } else {
                    joins.add(j);                    
                }
            }             
        }        
        return joins;
    }
}
