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
package br.com.anteros.persistence.dsl.osql.oracle;

import java.util.Date;

import br.com.anteros.persistence.dsl.osql.types.expr.DateExpression;
import br.com.anteros.persistence.dsl.osql.types.expr.NumberExpression;
import br.com.anteros.persistence.dsl.osql.types.template.DateTemplate;
import br.com.anteros.persistence.dsl.osql.types.template.NumberTemplate;

/**
 * Convenience functions and constants for Oracle DB usage
 *
 * @author tiwe
 */
public final class OracleGrammar {

    private OracleGrammar() {}

    public static final NumberExpression<Integer> level = NumberTemplate.create(Integer.class, "level");

    public static final NumberExpression<Integer> rownum = NumberTemplate.create(Integer.class, "rownum");

    public static final NumberExpression<Integer> rowid = NumberTemplate.create(Integer.class, "rowid");

    public static final DateExpression<Date> sysdate = DateTemplate.create(Date.class, "sysdate");

}
