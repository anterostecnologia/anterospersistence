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
package br.com.anteros.persistence.metadata.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.CompactHashSet;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.BooleanValue;
import br.com.anteros.persistence.metadata.annotation.CollectionTable;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.Columns;
import br.com.anteros.persistence.metadata.annotation.Convert;
import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.annotation.Converts;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Enumerated;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.Inheritance;
import br.com.anteros.persistence.metadata.annotation.JoinTable;
import br.com.anteros.persistence.metadata.annotation.Lob;
import br.com.anteros.persistence.metadata.annotation.MapKeyColumn;
import br.com.anteros.persistence.metadata.annotation.Table;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.metadata.annotation.type.BooleanType;
import br.com.anteros.persistence.metadata.annotation.type.DiscriminatorType;
import br.com.anteros.persistence.metadata.annotation.type.EnumType;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.metadata.annotation.type.InheritanceType;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.metadata.comparator.DependencyComparator;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;
import br.com.anteros.persistence.metadata.exception.EntityCacheManagerException;

public class PersistenceModelConfiguration {

	private Map<Class<? extends Serializable>, EntityConfiguration> entities = new LinkedHashMap<Class<? extends Serializable>, EntityConfiguration>();
	private Set<ConverterConfiguration> converters = new CompactHashSet<ConverterConfiguration>();

	public EntityConfiguration addEntity(Class<? extends Serializable> sourceClazz) {
		EntityConfiguration entity = new EntityConfiguration(sourceClazz, this);
		entity.loadAnnotations();
		entities.put(sourceClazz, entity);
		return entity;
	}
	
	public EnumConfiguration addEnum(Class<? extends Serializable> sourceClazz) {
		EnumConfiguration enumConfiguration = new EnumConfiguration(sourceClazz, this);
		enumConfiguration.loadAnnotations();
		entities.put(sourceClazz, enumConfiguration);
		return enumConfiguration;
	}

	public ConverterConfiguration addConverter(Class<? extends Serializable> sourceClazz) throws InstantiationException, IllegalAccessException {
		ConverterConfiguration converter = new ConverterConfiguration(sourceClazz);
		converter.loadAnnotations();
		converters.add(converter);
		return converter;
	}

	public Map<Class<? extends Serializable>, EntityConfiguration> getEntities() {
		return entities;
	}

	public void loadAnnotationsByClass(Class<? extends Serializable> sourceClazz) throws InstantiationException, IllegalAccessException {
		if ((sourceClazz.isAnnotationPresent(Converter.class)) && (!ReflectionUtils.isImplementsInterface(sourceClazz, AttributeConverter.class))) {
			throw new EntityCacheManagerException("A classe " + sourceClazz
					+ " configurada como um Converter não implementa a interface AttributeConverter.");
		}

		if (ReflectionUtils.isImplementsInterface(sourceClazz, AttributeConverter.class)) {
			addConverter(sourceClazz);
		} else {
			addEntity(sourceClazz);
		}
	}

	public void sortByDependency() {
		List<Class<? extends Serializable>> clazzes = new ArrayList<Class<? extends Serializable>>(entities.keySet());
		Collections.sort(clazzes, new DependencyComparator());
		Map<Class<? extends Serializable>, EntityConfiguration> newEntities = new LinkedHashMap<Class<? extends Serializable>, EntityConfiguration>();
		for (Class<? extends Serializable> sourceClazz : clazzes)
			newEntities.put(sourceClazz, entities.get(sourceClazz));
		entities = newEntities;
	}

	public void createOmmitedOrDefaultSettings() {
		sortByDependency();

		for (Class<?> sourceClazz : getEntities().keySet()) {
			EntityConfiguration entityConfiguration = getEntities().get(sourceClazz);
			for (FieldConfiguration fieldConfiguration : entityConfiguration.getFields()) {

				/*
				 * Verifica configuração básica
				 */
				checkBasicConfigurations(fieldConfiguration);
			}
		}

		for (Class<?> sourceClazz : getEntities().keySet()) {
			EntityConfiguration entityConfiguration = getEntities().get(sourceClazz);

			EntityConfiguration entityConfigurationBySuperClass = getEntityConfigurationBySourceClass(sourceClazz.getSuperclass());
			/*
			 * Se não foi definido @Table e não é uma herança, define o padrão
			 */
			if (!entityConfiguration.isAnnotationPresent(new Class[] { Table.class }) && (entityConfigurationBySuperClass == null)) {
				entityConfiguration.table(entityConfiguration.getSourceClazz().getSimpleName());
			}

			/*
			 * Se é uma classe abstrata e não foi definido @DiscriminatorColumn define o padrão
			 */
			if (ReflectionUtils.isAbstractClass(entityConfiguration.getSourceClazz())
					&& !entityConfiguration.isAnnotationPresent(DiscriminatorColumn.class)) {
				entityConfiguration.discriminatorColumn("DTYPE", 31, DiscriminatorType.STRING);
			}

			/*
			 * Se é uma classe abstrata e não foi definido @Inheritance define o padrão
			 */
			if (ReflectionUtils.isAbstractClass(entityConfiguration.getSourceClazz()) && !entityConfiguration.isAnnotationPresent(Inheritance.class)) {
				entityConfiguration.inheritance(InheritanceType.SINGLE_TABLE);
			}

			/*
			 * Se é uma classe herdada e não foi definido o @DiscriminatorValue
			 */
			if (!entityConfiguration.isAnnotationPresent(DiscriminatorValue.class) && (entityConfigurationBySuperClass != null)) {
				entityConfiguration.discriminatorValue(sourceClazz.getSimpleName());
			}

			for (FieldConfiguration fieldConfiguration : entityConfiguration.getFields()) {

				/*
				 * Se não for um campo simples ou coleção/map pode ser uma chave estrangeira (MANY_TO_ONE, ONE_TO_ONE)
				 */
				checkForeignKeyIsConfigured(fieldConfiguration);

				/*
				 * Se for uma coleção e não foi definido @Fetch Verifica se é um relacionamento ONE_TO_MANY,
				 * MANY_TO_MANY ou ELEMENT_COLLECTION
				 */
				checkCollectionIsConfigured(entityConfiguration, fieldConfiguration);

				/*
				 * Se for um map e não foi definido @Fetch Verifica se é um relacionamento ELEMENT_COLLECTION
				 */
				checkMapIsConfigured(entityConfiguration, fieldConfiguration);

				/*
				 * Verifica se possuí um Convert configurado nos campos simples. Caso não tenha e não seja um campo
				 * simples, coleção , map ou FK configura um Convert se possível
				 */
				checkConvertSimpleFieldIsConfigured(entityConfiguration, fieldConfiguration);
			}
		}
	}

	protected void checkConvertSimpleFieldIsConfigured(EntityConfiguration entityConfiguration, FieldConfiguration fieldConfiguration) {
		/*
		 * Verifica se possuí um Convert configurado. Caso não tenha e não seja um campo simples, coleção, map ou FK
		 * configura um Convert se possível
		 */
		if ((!fieldConfiguration.isAnnotationPresent(new Class[] { Convert.class, Converts.class, Convert.List.class }))
				&& !fieldConfiguration.isCollectionOrMap() && (!fieldConfiguration.isForeignKey()) && (!fieldConfiguration.isSimpleField())) {
			fieldConfiguration.convert(findConvertToField(entityConfiguration, fieldConfiguration));
		}
	}

	protected ConvertConfiguration[] findConvertToField(EntityConfiguration entityConfiguration, FieldConfiguration fieldConfiguration) {
		/*
		 * Procura por um @Convert na Entidade
		 */
		for (ConvertConfiguration convert : entityConfiguration.getConverts()) {
			if (fieldConfiguration.getField().getName().equals(convert.getAttributeName())) {
				return new ConvertConfiguration[] { convert };
			}
		}

		/*
		 * Procura por um @Converter global
		 */
		if (fieldConfiguration.isMap()) {
			List<ConvertConfiguration> result = new ArrayList<ConvertConfiguration>();
			List<Class<?>> types = ReflectionUtils.getGenericMapTypes(fieldConfiguration.getField());
			if (types.size() < 2)
				return null;
			for (ConverterConfiguration converter : converters) {
				if ((converter.getEntityAttributeType().equals(types.get(0))) && (!ReflectionUtils.isSimple(types.get(0)))) {
					result.add(new ConvertConfiguration(converter.getConverter(), "key"));
				}
				if ((converter.getEntityAttributeType().equals(types.get(1))) && (!ReflectionUtils.isSimple(types.get(1)))) {
					result.add(new ConvertConfiguration(converter.getConverter(), "value"));
				}
			}
			return result.toArray(new ConvertConfiguration[] {});
		} else {
			Class<?> genericType = fieldConfiguration.getField().getType();
			if (fieldConfiguration.isCollection())
				genericType = ReflectionUtils.getGenericType(fieldConfiguration.getField());

			for (ConverterConfiguration converter : converters) {
				if (converter.getEntityAttributeType().equals(genericType)) {
					return new ConvertConfiguration[] { new ConvertConfiguration(converter.getConverter()) };
				}
			}
		}

		return null;
	}

	protected void checkForeignKeyHasColumns(FieldConfiguration fieldConfiguration) {
		if (fieldConfiguration.isAnnotationPresent(ForeignKey.class)
				&& ((!fieldConfiguration.isAnnotationPresent(new Class[] { Column.class, Columns.class, Column.List.class })))) {
			Class<?> fkClass = fieldConfiguration.getField().getType();
			EntityConfiguration sourceConfiguration = getEntityConfigurationBySourceClass(fkClass);
			if (sourceConfiguration != null) {
				FieldConfiguration[] fieldConfigurations = sourceConfiguration.getAllFields();
				for (FieldConfiguration fld : fieldConfigurations) {
					/*
					 * Se for chave primária usa colunas para a foreignkey omitida
					 */
					if (fld.isId() || (fld.isCompositeId())) {
						for (ColumnConfiguration column : fld.getColumns()) {
							fieldConfiguration.column(column);
						}
					}
				}
			}
		}
	}

	protected void checkMapIsConfigured(EntityConfiguration entityConfiguration, FieldConfiguration fieldConfiguration) {
		if (fieldConfiguration.isMap() && !fieldConfiguration.isTransient()) {
			if (!fieldConfiguration.isAnnotationPresent(Fetch.class)) {
				fieldConfiguration.fetch(FetchType.LAZY, FetchMode.ELEMENT_COLLECTION, "", null, "");
			}

			if (!fieldConfiguration.isAnnotationPresent(new Class[] { Column.class, Columns.class })) {
				fieldConfiguration.column(fieldConfiguration.getField().getName(), 255, 0, 0, true);
			}

			if (!fieldConfiguration.isAnnotationPresent(CollectionTable.class)) {
				List<JoinColumnConfiguration> joinColumns = getJoinColumns(entityConfiguration);
				fieldConfiguration.collectionTable(entityConfiguration.getTableName() + "_" + fieldConfiguration.getField().getName(),
						joinColumns.toArray(new JoinColumnConfiguration[] {}));
			}

			if (!fieldConfiguration.isAnnotationPresent(MapKeyColumn.class)) {
				fieldConfiguration.mapKeyColumn(fieldConfiguration.getField().getName() + "_KEY");
			}

			List<Class<?>> types = ReflectionUtils.getGenericMapTypes(fieldConfiguration.getField());
			if (types.size() == 2) {
				if ((!ReflectionUtils.isSimple(types.get(0)) || (!ReflectionUtils.isSimple(types.get(1))))) {
					fieldConfiguration.convert(findConvertToField(entityConfiguration, fieldConfiguration));
				}
			}
		}
	}

	protected void checkCollectionIsConfigured(EntityConfiguration entityConfiguration, FieldConfiguration fieldConfiguration) {
		if (fieldConfiguration.isCollection() && !fieldConfiguration.isTransient()) {
			Class<?> genericType = ReflectionUtils.getGenericType(fieldConfiguration.getField());
			/*
			 * Se for um tipo simples então é um ELEMENT_COLLECTION
			 */
			if (ReflectionUtils.isSimple(genericType)) {
				if (!fieldConfiguration.isAnnotationPresent(Fetch.class)) {
					fieldConfiguration.fetch(FetchType.LAZY, FetchMode.ELEMENT_COLLECTION, "", null, "");
				}

				if (!fieldConfiguration.isAnnotationPresent(new Class[] { Column.class, Columns.class })) {
					fieldConfiguration.column(fieldConfiguration.getField().getName(), 255, 0, 0, true);
				}

				if (!fieldConfiguration.isAnnotationPresent(new Class[] { CollectionTable.class })) {
					List<JoinColumnConfiguration> joinColumns = getJoinColumns(entityConfiguration);
					fieldConfiguration.collectionTable(entityConfiguration.getTableName() + "_" + fieldConfiguration.getField().getName(),
							joinColumns.toArray(new JoinColumnConfiguration[] {}));
				}
			} else {
				/*
				 * Obtém a configuração do tipo de classe da coleção
				 */
				EntityConfiguration sourceConfiguration = getEntityConfigurationBySourceClass(genericType);
				if (sourceConfiguration != null) {
					FieldConfiguration fieldFound = findFieldCandidate(fieldConfiguration, genericType, sourceConfiguration);

					/*
					 * Se encontrou o campo é um ONE_TO_MANY
					 */
					if (fieldFound != null) {
						if (!fieldConfiguration.isAnnotationPresent(Fetch.class)) {
							fieldConfiguration.fetch(FetchType.LAZY, FetchMode.ONE_TO_MANY, fieldFound.getField().getName(), genericType, "");
						}
					} else {
						/*
						 * Caso não tenha encontrado o campo assume MANY_TO_MANY
						 */
						if (!fieldConfiguration.isAnnotationPresent(Fetch.class)) {
							fieldConfiguration.fetch(FetchType.LAZY, FetchMode.MANY_TO_MANY, "", genericType, "");
						}

						if (!fieldConfiguration.isAnnotationPresent(JoinTable.class)) {
							List<JoinColumnConfiguration> entityJoinColumns = getJoinColumns(entityConfiguration);
							List<JoinColumnConfiguration> sourceJoinColumns = getJoinColumns(sourceConfiguration);
							entityJoinColumns.addAll(sourceJoinColumns);

							fieldConfiguration.joinTable(entityConfiguration.getSourceClazz().getSimpleName() + "_"
									+ sourceConfiguration.getSourceClazz().getSimpleName(),
									entityJoinColumns.toArray(new JoinColumnConfiguration[] {}),
									entityJoinColumns.toArray(new JoinColumnConfiguration[] {}));
						}
					}
				} else {
					/*
					 * Como não encontrou uma entidade como FK do tipo da collection pode ser um tipo a ser convertido
					 */
					fieldConfiguration.convert(findConvertToField(entityConfiguration, fieldConfiguration));
				}
			}
		}
	}

	protected List<JoinColumnConfiguration> getJoinColumns(EntityConfiguration entityConfiguration) {
		List<JoinColumnConfiguration> joinColumns = new ArrayList<JoinColumnConfiguration>();
		for (FieldConfiguration fc : entityConfiguration.getAllFields()) {
			if (fc.isId()) {
				String name = fc.getSimpleColumn().getName();
				joinColumns.add(new JoinColumnConfiguration(entityConfiguration.getSourceClazz().getSimpleName() + "_ID", name));
			} else if (fc.isCompositeId()) {
				for (ColumnConfiguration cl : fc.getColumns()) {
					joinColumns.add(new JoinColumnConfiguration(entityConfiguration.getSourceClazz().getSimpleName() + "_" + cl.getName(), cl
							.getName()));
				}
			}
		}
		return joinColumns;
	}

	protected boolean isForeignKey(FieldConfiguration fieldConfiguration) {
		if (!fieldConfiguration.isSimpleField() && !fieldConfiguration.isCollectionOrMap() && !fieldConfiguration.isForeignKey()
				&& !fieldConfiguration.isTransient()) {
			Class<?> fkClass = fieldConfiguration.getField().getType();
			EntityConfiguration sourceConfiguration = getEntityConfigurationBySourceClass(fkClass);
			/*
			 * Se encontrou a configuração para a classe é uma chave estrangeira
			 */
			if (sourceConfiguration != null) {
				return true;
			}
		}
		return fieldConfiguration.isAnnotationPresent(new Class[] { ForeignKey.class, Transient.class });
	}

	protected void checkForeignKeyIsConfigured(FieldConfiguration fieldConfiguration) {
		if (isForeignKey(fieldConfiguration)) {
			fieldConfiguration.foreignKey();
		}

		/*
		 * Se a anotação @ForeignKey estiver presente e foi omitido
		 * 
		 * @Column ou @Columns assume como default a(s) coluna(s) da classe estrangeira
		 */
		checkForeignKeyHasColumns(fieldConfiguration);
	}

	protected void checkBasicConfigurations(FieldConfiguration fieldConfiguration) {
		if (fieldConfiguration.isTransient())
			return;

		/*
		 * Se não foi definido nome da coluna ou definido campo como transient e não é uma coleção.
		 */
		if (!fieldConfiguration.isAnnotationPresent(new Class[] { Column.class, Columns.class }) && (fieldConfiguration.isSimpleField())) {
			fieldConfiguration.column(fieldConfiguration.getField().getName());
		}

		/*
		 * Se o campo é boolean e não foi definido @BooleanValue
		 */
		if (((fieldConfiguration.getField().getType() == Boolean.class || fieldConfiguration.getField().getType() == boolean.class))
				&& (!fieldConfiguration.isAnnotationPresent(BooleanValue.class))) {
			fieldConfiguration.booleanValue("S", "N", BooleanType.STRING);
		}

		/*
		 * Se o campo é um Enum e não foi definido @Enumerated
		 */
		if (Enum.class.isAssignableFrom(fieldConfiguration.getType()) && (!fieldConfiguration.isAnnotationPresent(Enumerated.class))) {
			fieldConfiguration.enumerated(EnumType.STRING);
		}

		/*
		 * Se o campo é um Lob e não foi definido @Lob
		 */
		if (ReflectionUtils.isLobField(fieldConfiguration.getField()) && !fieldConfiguration.isAnnotationPresent(Lob.class)) {
			fieldConfiguration.lob(FetchType.LAZY);
		}

		/*
		 * Se o cmapo é uma Data e não foi definido @Temporal
		 */
		if (ReflectionUtils.isDateTimeField(fieldConfiguration.getField()) && !fieldConfiguration.isAnnotationPresent(Temporal.class)) {
			fieldConfiguration.temporal(TemporalType.DATE);
		}
	}

	protected FieldConfiguration findFieldCandidate(FieldConfiguration fieldConfiguration, Class<?> genericType,
			EntityConfiguration sourceConfiguration) {
		/*
		 * Verifica se existe um campo na classe da coleção que seja do MESMO TIPO da classe sendo analisada e que possa
		 * ter o nome do atributo igual ao nome da classe
		 */
		FieldConfiguration fieldFound = null;
		for (FieldConfiguration fc : sourceConfiguration.getAllFields()) {
			if (sourceConfiguration.getSourceClazz() == genericType) {
				if (fc.getField().getName().equalsIgnoreCase(fieldConfiguration.getField().getName())) {
					fieldFound = fc;
					break;
				}
			}
		}

		if (fieldFound == null) {
			/*
			 * Verifica se existe um campo na classe da coleção que seja HERANÇA do tipo da classe sendo analisada e que
			 * possa ter o nome do atributo igual ao nome da classe
			 */
			for (FieldConfiguration fc : sourceConfiguration.getAllFields()) {
				if (ReflectionUtils.isExtendsClass(genericType, sourceConfiguration.getSourceClazz())) {
					if (fc.getField().getName().equalsIgnoreCase(fieldConfiguration.getField().getName())) {
						fieldFound = fc;
						break;
					}
				}
			}

			if (fieldFound == null) {
				/*
				 * Verifica se existe um campo na classe da coleção que seja MESMO TIPO ou HERANÇA da classe sendo
				 * analisada
				 */
				for (FieldConfiguration fc : sourceConfiguration.getAllFields()) {
					if ((sourceConfiguration.getSourceClazz() == genericType)
							|| (ReflectionUtils.isExtendsClass(genericType, sourceConfiguration.getSourceClazz()))) {
						fieldFound = fc;
						break;
					}
				}
			}
		}
		return fieldFound;
	}

	public EntityConfiguration getEntityConfigurationBySourceClass(Class<?> sourceClazz) {
		return getEntities().get(sourceClazz);
	}

	public Set<ConverterConfiguration> getConverters() {
		return converters;
	}

	public void setConverters(Set<ConverterConfiguration> converters) {
		this.converters = converters;
	}

}
