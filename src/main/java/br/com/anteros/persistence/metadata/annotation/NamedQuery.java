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
package br.com.anteros.persistence.metadata.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.LockOptions;

/*
 * 
 * A consulta nomeada é uma consulta definida estaticamente com uma string de consulta imutável predefinida.  Usando consultas estáticas em vez de consultas dinâmicas 
 * podemos melhorar a organização do código, separando as sequências de consulta SQL a partir do código Java. 
 * Ela também reforça o uso da consulta com parâmetros em vez de incorporar literais de forma dinâmica na consulta o que resulta em consultas mais eficientes.
 * 
 * Consultas nomeadas têm duas pequenas vantagens:
 * - sua sintaxe é verificada quando a fábrica de sessão é criada, fazendo com que o aplicativo venha a 
 *   falhar rapidamente em caso de um erro (o que provavelmente indica que sua aplicação carece de alguns testes de unidade)
 * - elas podem ser acessadas ​​e utilizadas a partir de vários lugares 
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface NamedQuery {

	String name();
	String query();
	CallableType callableType() default CallableType.NONE;
	LockMode lockMode() default LockMode.NONE;
	int lockTimeout() default LockOptions.NO_WAIT;
	Class<?> resultClass() default Object.class;
	
	/**
	 * Defines several {@code @NamedQuery} annotations on the same element.
	 */
	@Target({ ElementType.TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		NamedQuery[] value();
	}
}
