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
package br.com.anteros.persistence.metadata.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.metadata.comparator.DependencyComparator;

public class ModelConfiguration {

	private Map<Class<? extends Serializable>, EntityConfiguration> entities = new LinkedHashMap<Class<? extends Serializable>, EntityConfiguration>();

	public EntityConfiguration addEntity(Class<? extends Serializable> sourceClazz) {
		EntityConfiguration entity = new EntityConfiguration(sourceClazz, this);
		entities.put(sourceClazz, entity);
		return entity;
	}

	public EntityConfiguration addEnum(Class<? extends Serializable> sourceClazz) {
		EntityConfiguration entity = new EntityConfiguration(sourceClazz, this);
		entities.put(sourceClazz, entity);
		return entity;
	}

	public Map<Class<? extends Serializable>, EntityConfiguration> getEntities() {
		return entities;
	}

	public void loadAnnotationsByClass(Class<? extends Serializable> sourceClazz) {
		EntityConfiguration entity = new EntityConfiguration(sourceClazz, this);
		entity.loadAnnotations();
		entities.put(sourceClazz, entity);

	}

	public void sortByDependency() {
		List<Class<? extends Serializable>> clazzes = new ArrayList<Class<? extends Serializable>>(entities.keySet());
		Collections.sort(clazzes, new DependencyComparator());
		Map<Class<? extends Serializable>, EntityConfiguration> newEntities = new LinkedHashMap<Class<? extends Serializable>, EntityConfiguration>();
		for (Class<? extends Serializable> sourceClazz : clazzes)
			newEntities.put(sourceClazz, entities.get(sourceClazz));
		entities = newEntities;
	}

}
