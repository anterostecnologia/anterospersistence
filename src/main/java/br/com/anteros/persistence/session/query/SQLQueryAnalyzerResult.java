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
package br.com.anteros.persistence.session.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.CompactHashSet;
import br.com.anteros.persistence.handler.EntityHandler;

/**
 * Classe que representa o resultado da análise do SQL. Contém as informações analisadas para uso na criação
 * dos objetos pelo {@link EntityHandler}
 * 
 * @author edson
 *
 */
public class SQLQueryAnalyzerResult {

	private Set<SQLQueryAnalyserAlias> aliases;
	private List<ExpressionFieldMapper> expressionsFieldMapper;
	private Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases;
	private String parsedSql;
	private boolean allowApplyLockStrategy = false;
	
	public SQLQueryAnalyzerResult(String parsedSql, Set<SQLQueryAnalyserAlias> aliases,List<ExpressionFieldMapper> expressionsFieldMapper, Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases,boolean allowApplyLockStrategy) {
		this.aliases = aliases;
		this.columnAliases = columnAliases;
		this.parsedSql = parsedSql;
		this.expressionsFieldMapper = expressionsFieldMapper;
		this.allowApplyLockStrategy = allowApplyLockStrategy;
	}

	public Set<SQLQueryAnalyserAlias> getAliases() {
		return aliases;
	}

	public Map<SQLQueryAnalyserAlias, Map<String, String[]>> getColumnAliases() {
		return columnAliases;
	}

	public String getParsedSql() {
		return parsedSql;
	}

	public List<ExpressionFieldMapper> getExpressionsFieldMapper() {
		return expressionsFieldMapper;
	}

	public boolean isAllowApplyLockStrategy() {
		return allowApplyLockStrategy;
	}

	/**
	 * Retorna uma lista dos aliases das colunas que serão usados para realizar o bloqueio de colunas específicas. 
	 * @param aliasesToLock Alias informados pelo usuário.
	 * @return Lista de aliases gerados.
	 */
	public Set<String> getColumnNamesToLock(Set<String> aliasesToLock) {
		Set<String> result = new CompactHashSet<String>();
		for (String alias : aliasesToLock){
			if (alias.indexOf(".")>0){
				result.add(alias);
			} else {
				for (SQLQueryAnalyserAlias al : columnAliases.keySet()){
					if (al.getAlias().equalsIgnoreCase(alias)){
						for (String columnName : columnAliases.get(al).keySet()){
							result.add(al.getAlias()+"."+columnName);
						}
					}
				}
			}
		}
		return result;
	}

}
