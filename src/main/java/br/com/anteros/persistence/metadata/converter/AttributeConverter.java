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
package br.com.anteros.persistence.metadata.converter;



/**
 * Uma classe que implementa esta interface pode ser usada para converter um atributo
 * de uma entidade para uma coluna do banco de dados e vice-versa.
 * X e Y podem ser tipos Java.
 *
 * @param X  O tipo de atributo na entidade
 * @param Y  O tipo de coluna no banco de dados
 */
public interface AttributeConverter<X,Y> {

    /**
     * Converte o valor do atributo na entidade
     * para um valor da coluna do banco de dados.
     *
     * @param attribute  Valor do atributo da entidade para converter
     * @return  Valor convertido para a coluna do banco de dados
     */
    public Y convertToDatabaseColumn (X attribute);

    /**
     * Converte o valor da coluna do banco de dados para um atributo
     * da entidade.
     * 
     * @param dbData  valor da coluna no banco de dados para converter
     * @return  valor convertido para o atributo da entidade
     */
    public X convertToEntityAttribute (Y dbData);
}
