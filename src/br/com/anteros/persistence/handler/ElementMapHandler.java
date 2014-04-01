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
package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCacheException;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.util.ObjectUtils;
import br.com.anteros.persistence.util.ReflectionUtils;

public class ElementMapHandler implements ResultSetHandler {

	private DescriptionField descriptionField;

	public ElementMapHandler(DescriptionField descriptionField) {
		this.descriptionField = descriptionField;
	}

	@Override
	public Object handle(ResultSet rs) throws Exception {
		Map mapResult = null;
		Object keyValue = null;
		Object value = null;
		if (rs.next()) {
			mapResult = new LinkedHashMap();
			do {
				for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
					if (!descriptionColumn.isPrimaryKey()) {
						keyValue = rs.getObject(descriptionField.getMapKeyColumn().getColumnName());
						value = rs.getObject(descriptionField.getElementColumn().getColumnName());
						if (descriptionColumn.getEnumType() != null) {
							if (!"".equals(value) && (value != null)) {
								String enumValue = descriptionColumn.getEnumValue((String) value);
								if ((enumValue == null) || (enumValue.equals(""))) {
									throw new EntityCacheException("Valor "
											+ value
											+ " não encontrado na lista do Enum do campo "
											+ descriptionField.getName()
											+ (descriptionField.getEntityCache() == null ? "" : " da classe "
													+ descriptionField.getEntityCache().getEntityClass().getName()) + ". Verifique se o tipo enum "
											+ descriptionField.getField().getType()
											+ "  foi customizado e se foi adicionado na lista de classes anotadas.");
								}
								keyValue = Enum.valueOf((Class<? extends Enum>) ReflectionUtils.getEnumKeyTypedMap(descriptionField.getField()),
										enumValue);
								mapResult.put(keyValue, value);
							}
						} else {
							mapResult.put(ObjectUtils.convert(keyValue, ReflectionUtils.getEnumKeyTypedMap(descriptionField.getField())), value);
						}
					}
				}

			} while (rs.next());
		}
		return mapResult;
	}

}
