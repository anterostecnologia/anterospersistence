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
 *******************************************************************************/
package br.com.anteros.persistence.session.lock;

/**
 * Define a forma que deverá ser aplicado a estratégia de bloqueio(lock) para as Entidades.
 * 
 * @author edson
 *
 */
public enum LockScope {

	/**
	 * Este valor define o comportamento padrão de bloqueio pessimista. Serão bloqueados apenas os campos da Entidade
	 * representados por suas respectivas colunas no banco de dados. No caso dos relacionamentos de chave estrangeira
	 * será bloqueado o valor do campo mas não o estado das entidades referenciadas. Não serão bloqueados as coleções de
	 * elementos mapeadas nos campos da Entidade.
	 * 
	 */
	NORMAL,
	/**
	 * Além do comportamento para LockScope.NORMAL, coleções de elementos também serão bloqueadas se for usado
	 * LockMode.EXTENDED. O estado de entidades referenciadas por essas relações não será bloqueado (a menos que essas
	 * entidades sejam explicitamente bloqueadas). O bloqueio de uma relacionamento ou coleção de elementos geralmente
	 * bloqueia apenas as linhas na tabela de junção.
	 */
	EXTENDED
}
