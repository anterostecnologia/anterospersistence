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
package br.com.anteros.persistence.metadata;

import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.Columns;
import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.configuration.ColumnConfiguration;
import br.com.anteros.persistence.metadata.configuration.EntityConfiguration;
import br.com.anteros.persistence.metadata.configuration.FieldConfiguration;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;

public class DatabaseTypesUtil {
	private DatabaseTypesUtil() {
	}

	public static void getSQLDataTypeFromField(FieldConfiguration field, String originalColumnName,
			DescriptionColumn column) {

		FieldConfiguration originalField = getFieldByColumnName(field, originalColumnName, null);
		if (originalField != null) {
			ColumnConfiguration columnConfiguration = originalField.getColumnByName(originalColumnName);
			column.setLength(columnConfiguration.getLength());
			column.setPrecision(columnConfiguration.getPrecision());
			column.setScale(columnConfiguration.getScale());
		}

	}

	public static void getSQLDataTypeFromFieldForeignKey(FieldConfiguration field, String originalColumnName,
			DescriptionColumn column) {
		FieldConfiguration originalField = getFieldByColumnName(field, originalColumnName, null);

		if ((originalField == null)
				|| (!originalField.isAnnotationPresent(Id.class) && !originalField
						.isAnnotationPresent(CompositeId.class))) {
			throw new RuntimeException("Coluna " + originalColumnName + " não encontrado na classe "
					+ field.getType().getSimpleName() + " ou a coluna encontrada não é um ID.");
		} else {
			ColumnConfiguration columnConfiguration = originalField.getColumnByName(originalColumnName);
			column.setLength(columnConfiguration.getLength());
			column.setPrecision(columnConfiguration.getPrecision());
			column.setScale(columnConfiguration.getScale());
		}
	}

	private static FieldConfiguration getFieldByColumnName(FieldConfiguration field, String originalColumnName,
			FieldConfiguration sourceField) {
		EntityConfiguration entityConfiguration = field.getEntityConfigurationBySourceClass(field.getType());
		return getFieldByColumnName(entityConfiguration, originalColumnName, sourceField);
	}

	private static FieldConfiguration getFieldByColumnName(EntityConfiguration entityConfiguration,
			String originalColumnName, FieldConfiguration sourceField) {
		FieldConfiguration[] fields = entityConfiguration.getAllFields();

		for (FieldConfiguration field : fields) {
			if ((field.isAnnotationPresent(Column.class) || (field.isAnnotationPresent(Columns.class)))) {
				for (ColumnConfiguration columnConfiguration : field.getColumns()) {
					if (originalColumnName.equals(columnConfiguration.getName())) {
						if (sourceField != null) {
							if (sourceField.getName().equals(field.getName()))
								continue;
						}
						if (field.isAnnotationPresent(ForeignKey.class)) {
							/*
							 * Verifica se a coluna é um autorelacionamento -
							 * caso seja ignora as fks quando buscar o field
							 * para não entrar em loop
							 */
							if (entityConfiguration.getSourceClazz().getName().equals(field.getType().getName())) {
								return getFieldByColumnName(
										field,
										"".equals(columnConfiguration.getInversedColumn()) ? columnConfiguration
												.getName() : columnConfiguration.getInversedColumn(), field);
							} else
								return getFieldByColumnName(
										field,
										"".equals(columnConfiguration.getInversedColumn()) ? columnConfiguration
												.getName() : columnConfiguration.getInversedColumn(), null);
						}
						return field;
					}
				}
			}
		}

		throw new RuntimeException("Coluna " + originalColumnName + " não encontrado na classe "
				+ entityConfiguration.getSourceClazz().getSimpleName());

	}

}
