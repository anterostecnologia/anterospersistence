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
package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.util.Collection;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.query.SQLQueryException;

public class ElementCollectionHandler implements ResultSetHandler {

	private DescriptionField descriptionField;

	public ElementCollectionHandler(DescriptionField descriptionField) {
		this.descriptionField = descriptionField;
	}

	@SuppressWarnings("unchecked")
	public Object handle(ResultSet rs) throws Exception {
		Collection<Object> collectionResult = null;

		if (rs.next()) {
			collectionResult = (Collection<Object>) ReflectionUtils.getConcreteImplementationFromCollection(descriptionField.getField().getType())
					.newInstance();
			do {
				for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
					if (descriptionColumn.isElementColumn()) {
						try {
							Object value = rs.getObject(descriptionColumn.getColumnName());
							value = descriptionColumn.convertToEntityAttribute(value);
							collectionResult.add(value);
						} catch (Exception e) {
							throw new SQLQueryException("Ocorreu um erro obtendo valor da coluna " + descriptionColumn.getColumnName()
									+ " criando coleção " + descriptionField.getField().getName() + " da entidade "
									+ descriptionField.getEntityCache().getEntityClass());
						}
					}
				}
			} while (rs.next());
		}
		return collectionResult;
	}

}
