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
package br.com.anteros.persistence.metadata;

import java.lang.reflect.Field;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.Columns;
import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;

public class DatabaseTypesUtil {
	private DatabaseTypesUtil() {
	}

	public static void getSQLDataTypeFromField(Field field, String originalColumnName, DescriptionColumn column) {

		Field originalField = getFieldByColumnName(field, originalColumnName, null);

		Column annotation = originalField.getAnnotation(Column.class);

		column.setLength(annotation.length());
		column.setPrecision(annotation.precision());
		column.setScale(annotation.scale());

	}

	public static void getSQLDataTypeFromFieldForeignKey(Field field, String originalColumnName,
			DescriptionColumn column) {
		Field originalField = getFieldByColumnName(field, originalColumnName, null);
		if (!originalField.isAnnotationPresent(Id.class) && !originalField.isAnnotationPresent(CompositeId.class)) {
			throw new RuntimeException("Coluna " + originalColumnName + " não encontrado na classe "
					+ field.getType().getSimpleName()+" ou a coluna encontrada não é um ID.");
		}
		Column annotation = originalField.getAnnotation(Column.class);
		column.setLength(annotation.length());
		column.setPrecision(annotation.precision());
		column.setScale(annotation.scale());
	}

	public static void getSQLDataTypeFromClass(Class<?> clazz, String originalColumnName, DescriptionColumn column) {

		Field originalField = getFieldByColumnName(clazz, originalColumnName, null);

		Column annotation = originalField.getAnnotation(Column.class);

		column.setLength(annotation.length());
		column.setPrecision(annotation.precision());
		column.setScale(annotation.scale());

	}

	private static Field getFieldByColumnName(Field field, String originalColumnName, Field sourceField) {
		return getFieldByColumnName(field.getType(), originalColumnName, sourceField);
	}

	private static Field getFieldByColumnName(Class<?> clazz, String originalColumnName, Field sourceField) {
		Field[] fields = ReflectionUtils.getAllDeclaredFields(clazz);

		for (Field field : fields) {
			if ((field.isAnnotationPresent(Column.class) || (field.isAnnotationPresent(Columns.class)))) {
				Column[] columns = null;
				if (field.isAnnotationPresent(Column.class))
					columns = new Column[] { field.getAnnotation(Column.class) };
				else
					columns = field.getAnnotation(Columns.class).columns();
				for (Column columnAnnotation : columns) {
					if (originalColumnName.equals(columnAnnotation.name())) {
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
							if (clazz.getName().equals(field.getType().getName())) {
								return getFieldByColumnName(field,
										"".equals(columnAnnotation.inversedColumn()) ? columnAnnotation.name()
												: columnAnnotation.inversedColumn(), field);
							} else
								return getFieldByColumnName(field,
										"".equals(columnAnnotation.inversedColumn()) ? columnAnnotation.name()
												: columnAnnotation.inversedColumn(), null);
						}
						return field;
					}
				}
			}
		}

		throw new RuntimeException("Coluna " + originalColumnName + " não encontrado na classe "
				+ clazz.getSimpleName());

	}

}
