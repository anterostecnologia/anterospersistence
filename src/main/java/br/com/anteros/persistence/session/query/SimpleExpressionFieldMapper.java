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
package br.com.anteros.persistence.session.query;

import java.sql.ResultSet;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

/**
 * Classe responsável por atribuir os valores dos campos simples ao objeto alvo.
 * 
 * @author edson
 *
 */
public class SimpleExpressionFieldMapper extends ExpressionFieldMapper {

	public SimpleExpressionFieldMapper(EntityCache targetEntityCache, DescriptionField descriptionField, String aliasColumnName) {
		super(targetEntityCache, descriptionField, aliasColumnName);
	}

	@Override
	public void execute(SQLSession session, ResultSet resultSet, EntityManaged entityManaged, Object targetObject, Cache transactionCache)
			throws Exception {

		if (!session.getEntityCacheManager().getEntityCache(targetObject.getClass()).containsDescriptionField(descriptionField))
			return;

		/*
		 * Obtém o valor da coluna no resultSet.
		 */
		Object value = getValueByColumnName(resultSet);
		
		value = descriptionField.getSimpleColumn().convertToEntityAttribute(value);
		
		/*
		 * Atribui o valor no field do objeto alvo.
		 */
		descriptionField.setObjectValue(targetObject, value);
		if (entityManaged.getStatus() != EntityStatus.READ_ONLY) {
			/*
			 * Guarda o valor na lista de valores anteriores
			 */
			FieldEntityValue fieldEntityValue = descriptionField.getSimpleColumn().getFieldEntityValue(targetObject);
			entityManaged.addOriginalValue(fieldEntityValue);
			entityManaged.addLastValue(fieldEntityValue);

			/*
			 * Adiciona o campo na lista de campos que poderão ser alterados. Se o campo não for buscado no select não
			 * poderá ser alterado.
			 */
			entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
		}
	}

}
