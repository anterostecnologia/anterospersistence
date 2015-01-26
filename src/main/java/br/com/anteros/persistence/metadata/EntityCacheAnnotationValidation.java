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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.annotation.Inheritance;
import br.com.anteros.persistence.metadata.annotation.MappedSuperclass;
import br.com.anteros.persistence.metadata.annotation.type.InheritanceType;
import br.com.anteros.persistence.metadata.configuration.EntityConfiguration;
import br.com.anteros.persistence.metadata.configuration.FieldConfiguration;

public class EntityCacheAnnotationValidation {

	public static String[] validateEntityConfiguration(Class<? extends Serializable> sourceClazz,
			EntityConfiguration entityConfiguration) {
		List<String> errors = new ArrayList<String>();

		/*
		 * Se não implementar java.io.Serializable
		 */
		if (!Serializable.class.isAssignableFrom(sourceClazz)) {
			errors.add("A classe " + sourceClazz.getName() + " não implemanta java.io.Serializable");
		}

		/*
		 * Se não existir a configuração Entity ou MappedSuperclass
		 */
		if (!entityConfiguration.isAnnotationPresent(Entity.class)
				&& !entityConfiguration.isAnnotationPresent(MappedSuperclass.class)) {
			errors.add("A annotatação Entity não está presente na classe " + sourceClazz.getName());
		}

		/*
		 * Se não existir a configuração Id ou CompositeId
		 */
		if (!entityConfiguration.isAnnotationPresent(Inheritance.class)
				&& !(entityConfiguration.isAnnotationPresent(DiscriminatorValue.class))) {
			FieldConfiguration[] fields = entityConfiguration.getAllFields();
			boolean foundAnnotationId = false;
			for (FieldConfiguration field : fields) {
				if ((field.isAnnotationPresent(Id.class)) || (field.isAnnotationPresent(CompositeId.class))) {
					foundAnnotationId = true;
					break;
				}
			}
			if (!foundAnnotationId)
				errors.add("Toda Entidade deve possuir um Id. Use Id ou CompositeId para definir a chave da entidade. "
						+ sourceClazz.getName());
		} else {
			if ((entityConfiguration.isAnnotationPresent(Inheritance.class))
					&& (!entityConfiguration.isAnnotationPresent(DiscriminatorColumn.class))) {
				errors.add("Ao usar a annotatação Inheritance em uma classe use também  a anotação DiscriminatorColumn para especificar o nome da coluna que irá identificar a herança. Classe "
						+ sourceClazz.getName());
			}
		}

		if (entityConfiguration.countNumberOfAnnotation(Id.class) > 1)
			errors.add("A Classe " + sourceClazz.getName()
					+ " não pode ter mais de uma configuração Id. Caso a chave seja composta use CompositeId.");

		if (entityConfiguration.countNumberOfAnnotation(CompositeId.class) == 1)
			errors.add("A Classe " + sourceClazz.getName()
					+ " possuí apenas uma configuração CompositeId. Se a chave for simples use Id.");

		/*
		 * Se existir MappedClass não definir a estratégia com
		 * 
		 * Inheritance.
		 */
		if (entityConfiguration.isAnnotationPresent(MappedSuperclass.class)
				&& !entityConfiguration.isAnnotationPresent(Inheritance.class)) {
			errors.add("A configuração MappedSuperclass deve vir acompanhada com a configuração Inheritance.");
		}

		if (entityConfiguration.isAnnotationPresent(MappedSuperclass.class)
				&& entityConfiguration.getInheritanceStrategy() != InheritanceType.TABLE_PER_CLASS) {
			errors.add("A configuração MappedSuperclass deve ser utilizada com InheritanceType.TABLE_PER_CLASS.");
		}

		return errors.toArray(new String[] {});
	}

	public static String[] validateFieldConfiguration(FieldConfiguration fieldConfiguration) {
		List<String> errors = new ArrayList<String>();

		return errors.toArray(new String[] {});
	}
}
