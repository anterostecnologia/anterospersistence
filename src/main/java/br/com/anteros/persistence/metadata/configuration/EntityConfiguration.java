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
package br.com.anteros.persistence.metadata.configuration;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import br.com.anteros.core.converter.Converter;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.Comment;
import br.com.anteros.persistence.metadata.annotation.Convert;
import br.com.anteros.persistence.metadata.annotation.Converts;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.EnumValue;
import br.com.anteros.persistence.metadata.annotation.EnumValues;
import br.com.anteros.persistence.metadata.annotation.Index;
import br.com.anteros.persistence.metadata.annotation.Indexes;
import br.com.anteros.persistence.metadata.annotation.Inheritance;
import br.com.anteros.persistence.metadata.annotation.MappedSuperclass;
import br.com.anteros.persistence.metadata.annotation.NamedQueries;
import br.com.anteros.persistence.metadata.annotation.NamedQuery;
import br.com.anteros.persistence.metadata.annotation.ReadOnly;
import br.com.anteros.persistence.metadata.annotation.SQLDelete;
import br.com.anteros.persistence.metadata.annotation.SQLDeleteAll;
import br.com.anteros.persistence.metadata.annotation.SQLInsert;
import br.com.anteros.persistence.metadata.annotation.SQLUpdate;
import br.com.anteros.persistence.metadata.annotation.Table;
import br.com.anteros.persistence.metadata.annotation.UniqueConstraint;
import br.com.anteros.persistence.metadata.annotation.type.DiscriminatorType;
import br.com.anteros.persistence.metadata.annotation.type.InheritanceType;
import br.com.anteros.persistence.metadata.annotation.type.ScopeType;
import br.com.anteros.persistence.metadata.descriptor.type.ConnectivityType;
import br.com.anteros.synchronism.annotation.Remote;

public class EntityConfiguration {

	private Class<? extends Serializable> sourceClazz;
	private List<FieldConfiguration> fields = new LinkedList<FieldConfiguration>();
	private IndexConfiguration[] indexes = {};
	private UniqueConstraintConfiguration[] uniqueConstraints = {};
	private String tableName;
	private Set<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>();
	private InheritanceType inheritanceStrategy;
	private String discriminatorColumnName;
	private int discriminatorColumnLength;
	private DiscriminatorType discriminatorColumnType;
	private String discriminatorValue;
	private EnumValueConfiguration[] enumValues = {};
	private PersistenceModelConfiguration model;
	private NamedQueryConfiguration[] namedQueries = {};
	private ScopeType scope = ScopeType.TRANSACTION;
	private int maxTimeMemory = 0;
	private SQLInsertConfiguration sqlInsert;
	private SQLUpdateConfiguration sqlUpdate;
	private SQLDeleteConfiguration sqlDelete;
	private SQLDeleteAllConfiguration sqlDeleteAll;
	private String comment = "";
	private String schema = "";
	private String catalog = "";
	private RemoteConfiguration remote;
	private ConvertConfiguration[] converts = {};

	public EntityConfiguration(Class<? extends Serializable> sourceClazz, PersistenceModelConfiguration model) {
		this.sourceClazz = sourceClazz;
		this.model = model;
		annotations.add(Entity.class);
	}

	public Class<? extends Serializable> getSourceClazz() {
		return sourceClazz;
	}

	public FieldConfiguration addField(String fieldName) throws Exception {
		for (FieldConfiguration field : fields) {
			if (field.getName().equals(fieldName))
				throw new ConfigurationException("Campo " + fieldName + " já adicionado na Entidade "
						+ sourceClazz.getName());
		}

		if (ReflectionUtils.getFieldByName(sourceClazz, fieldName) == null)
			throw new ConfigurationException("Campo " + fieldName + " não encontrado na Classe "
					+ sourceClazz.getName());

		FieldConfiguration field = new FieldConfiguration(this, fieldName);
		fields.add(field);
		return field;
	}

	public EntityConfiguration table(String tableName) {
		this.tableName = tableName;
		annotations.add(Table.class);
		return this;
	}

	public EntityConfiguration table(String tableName, IndexConfiguration[] indexes) {
		this.tableName = tableName;
		this.indexes = indexes;
		annotations.add(Table.class);
		return this;
	}

	public EntityConfiguration table(String catalog, String schema, String tableName) {
		this.catalog = catalog;
		this.schema = schema;
		this.tableName = tableName;
		this.indexes = null;
		annotations.add(Table.class);
		return this;
	}

	public EntityConfiguration inheritance(InheritanceType strategy) {
		annotations.add(Inheritance.class);
		this.inheritanceStrategy = strategy;
		return this;
	}

	public EntityConfiguration mappedSuperclass() {
		annotations.add(MappedSuperclass.class);
		return this;
	}

	public EntityConfiguration readOnly() {
		annotations.add(ReadOnly.class);
		return this;
	}

	public EntityConfiguration discriminatorColumn(String name, int length, DiscriminatorType discriminatorType) {
		annotations.add(DiscriminatorColumn.class);
		this.discriminatorColumnName = name;
		this.discriminatorColumnLength = length;
		this.discriminatorColumnType = discriminatorType;
		return this;
	}

	public EntityConfiguration discriminatorValue(String value) {
		annotations.add(DiscriminatorValue.class);
		this.discriminatorValue = value;
		return this;
	}

	public EntityConfiguration enumValues(EnumValueConfiguration[] value) {
		annotations.add(EnumValues.class);
		this.enumValues = value;
		return this;
	}

	public String getTableName() {
		return tableName;
	}

	public IndexConfiguration[] getIndexes() {
		return indexes;
	}

	public List<FieldConfiguration> getFields() {
		return fields;
	}

	public Set<Class<? extends Annotation>> getAnnotations() {
		return annotations;
	}

	public InheritanceType getInheritanceStrategy() {
		return inheritanceStrategy;
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return annotations.contains(annotationClass);
	}

	public boolean isAnnotationPresent(Class[] annotationClasses) {
		for (Class c : annotationClasses) {
			if (isAnnotationPresent(c)) {
				return true;
			}
		}
		return false;
	}

	public String getDiscriminatorColumnName() {
		return discriminatorColumnName;
	}

	public int getDiscriminatorColumnLength() {
		return discriminatorColumnLength;
	}

	public String getDiscriminatorValue() {
		return discriminatorValue;
	}

	public EnumValueConfiguration[] getEnumValues() {
		return enumValues;
	}

	public FieldConfiguration[] getAllFields() {
		Set<FieldConfiguration> allFields = new LinkedHashSet<FieldConfiguration>();
		allFields.addAll(getFields());
		Class<?> clazz = sourceClazz;
		while ((clazz != null) && (clazz != Object.class)) {
			EntityConfiguration entityConfiguration = model.getEntities().get(clazz);
			if (entityConfiguration == null)
				break;
			allFields.addAll(entityConfiguration.getFields());
			clazz = clazz.getSuperclass();
		}

		return allFields.toArray(new FieldConfiguration[] {});
	}

	public PersistenceModelConfiguration getModel() {
		return model;
	}

	public int countNumberOfAnnotation(Class<? extends Annotation> annotationClass) {
		int result = 0;
		for (FieldConfiguration field : getFields()) {
			if (field.isAnnotationPresent(annotationClass))
				result++;
		}
		return result;
	}

	public boolean isEnum() {
		return sourceClazz.isEnum();
	}

	@Override
	public String toString() {
		return sourceClazz.getName();
	}

	public void loadAnnotations() {
		Annotation[] annotations = sourceClazz.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof Entity) {
				this.annotations.add(Entity.class);
			} else if (annotation instanceof Table) {
				table(((Table) annotation).catalog(), ((Table) annotation).schema(), ((Table) annotation).name());
				UniqueConstraint[] constraints = ((Table) annotation).uniqueConstraints();
				UniqueConstraintConfiguration[] uniqueConstraintsDef = null;
				if (constraints != null) {
					uniqueConstraintsDef = new UniqueConstraintConfiguration[constraints.length];
					for (int i = 0; i < constraints.length; i++)
						uniqueConstraintsDef[i] = new UniqueConstraintConfiguration(constraints[i].name(),
								constraints[i].columnNames());
				}
				uniqueConstraints(uniqueConstraintsDef);
			} else if ((annotation instanceof Indexes) || (annotation instanceof Index)
					|| (annotation instanceof Index.List)) {
				Index[] indexes = null;
				if (annotation instanceof Indexes)
					indexes = ((Indexes) annotation).value();
				else if (annotation instanceof Index)
					indexes = new Index[] { (Index) annotation };
				else if (annotation instanceof Index.List)
					indexes = ((Index.List) annotation).value();

				IndexConfiguration[] indexesConf = null;
				if (indexes != null) {
					indexesConf = new IndexConfiguration[indexes.length];
					for (int i = 0; i < indexes.length; i++)
						indexesConf[i] = new IndexConfiguration(indexes[i].name(), indexes[i].columnNames())
								.catalog(indexes[i].catalog()).schema(indexes[i].schema()).unique(indexes[i].unique());
				}
				if ((annotation instanceof Indexes) || (annotation instanceof Index.List))
					indexes(indexesConf);
				else
					index(indexesConf);

			} else if (annotation instanceof DiscriminatorColumn) {
				discriminatorColumn(((DiscriminatorColumn) annotation).name(),
						((DiscriminatorColumn) annotation).length(),
						((DiscriminatorColumn) annotation).discriminatorType());
			} else if (annotation instanceof DiscriminatorValue) {
				discriminatorValue(((DiscriminatorValue) annotation).value());
			} else if ((annotation instanceof EnumValues) || (annotation instanceof EnumValue.List)) {
				EnumValue[] values = null;
				if (annotation instanceof EnumValues) {
					values = ((EnumValues) annotation).value();
				} else {
					values = ((EnumValue.List) annotation).value();
				}
				if (values != null) {
					EnumValueConfiguration[] enValues = new EnumValueConfiguration[values.length];
					for (int i = 0; i < values.length; i++)
						enValues[i] = new EnumValueConfiguration(values[i].enumValue(), values[i].value());
					enumValues(enValues);
				}
			} else if ((annotation instanceof NamedQueries) || (annotation instanceof NamedQuery.List)
					|| (annotation instanceof NamedQuery)) {
				NamedQuery[] values = null;
				if (annotation instanceof NamedQueries) {
					values = ((NamedQueries) annotation).value();
				} else if (annotation instanceof NamedQuery.List) {
					values = ((NamedQuery.List) annotation).value();
				} else if (annotation instanceof NamedQueries) {
					values = new NamedQuery[] { ((NamedQuery) annotation) };
				}
				if (values != null) {
					NamedQueryConfiguration[] namedValues = new NamedQueryConfiguration[values.length];
					for (int i = 0; i < values.length; i++)
						namedValues[i] = new NamedQueryConfiguration(values[i]);
					namedQueries(namedValues);
				}
			} else if (annotation instanceof Inheritance) {
				inheritance(((Inheritance) annotation).strategy());
			} else if (annotation instanceof ReadOnly) {
				readOnly();
			} else if (annotation instanceof MappedSuperclass) {
				mappedSuperclass();
			} else if (annotation instanceof SQLInsert) {
				sqlInsert(new SQLInsertConfiguration((SQLInsert) annotation));
			} else if (annotation instanceof SQLDelete) {
				sqlDelete(new SQLDeleteConfiguration((SQLDelete) annotation));
			} else if (annotation instanceof SQLDeleteAll) {
				sqlDeleteAll(new SQLDeleteAllConfiguration((SQLDeleteAll) annotation));
			} else if (annotation instanceof SQLUpdate) {
				sqlUpdate(new SQLUpdateConfiguration((SQLUpdate) annotation));
			} else if (annotation instanceof Comment) {
				comment(((Comment) annotation).value());
			} else if (annotation instanceof Remote) {
				remote(new RemoteConfiguration((Remote) annotation));
			} else if ((annotation instanceof Converts) || (annotation instanceof Convert)
					|| (annotation instanceof Convert.List)) {
				Convert[] converts = null;
				if (annotation instanceof Converts)
					converts = ((Converts) annotation).value();
				else if (annotation instanceof Converter)
					converts = new Convert[] { (Convert) annotation };
				else if (annotation instanceof Convert.List)
					converts = ((Convert.List) annotation).value();

				ConvertConfiguration[] convertsConf = null;
				if (indexes != null) {
					convertsConf = new ConvertConfiguration[converts.length];
					for (int i = 0; i < converts.length; i++) {
						convertsConf[i] = new ConvertConfiguration(converts[i]);
					}
				}
				if ((annotation instanceof Converts) || (annotation instanceof Convert.List))
					converts(convertsConf);
				else
					convert(convertsConf);
			}
		}

		Field[] fields = sourceClazz.getDeclaredFields();
		for (Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				FieldConfiguration fieldConfiguration = new FieldConfiguration(this, field);
				fieldConfiguration.loadAnnotations();
				this.fields.add(fieldConfiguration);
			}
		}

	}

	public NamedQueryConfiguration[] getNamedQueries() {
		return namedQueries;
	}

	public EntityConfiguration namedQueries(NamedQueryConfiguration[] namedQueries) {
		this.namedQueries = namedQueries;
		return this;
	}

	public EntityConfiguration namedQueries(NamedQueryConfiguration namedQuery) {
		this.namedQueries = new NamedQueryConfiguration[] { namedQuery };
		return this;
	}

	public ScopeType getScope() {
		return scope;
	}

	public EntityConfiguration scope(ScopeType scope) {
		this.scope = scope;
		return this;
	}

	public int getMaxTimeMemory() {
		return maxTimeMemory;
	}

	public EntityConfiguration maxTimeMemory(int maxTimeMemory) {
		this.maxTimeMemory = maxTimeMemory;
		return this;
	}

	public SQLInsertConfiguration getSqlInsert() {
		return sqlInsert;
	}

	public EntityConfiguration sqlInsert(SQLInsertConfiguration sqlInsert) {
		this.annotations.add(SQLInsert.class);
		this.sqlInsert = sqlInsert;
		return this;
	}

	public SQLUpdateConfiguration getSqlUpdate() {
		return sqlUpdate;
	}

	public EntityConfiguration sqlUpdate(SQLUpdateConfiguration sqlUpdate) {
		this.annotations.add(SQLUpdate.class);
		this.sqlUpdate = sqlUpdate;
		return this;
	}

	public SQLDeleteConfiguration getSqlDelete() {
		return sqlDelete;
	}

	public EntityConfiguration sqlDelete(SQLDeleteConfiguration sqlDelete) {
		this.annotations.add(SQLDelete.class);
		this.sqlDelete = sqlDelete;
		return this;
	}

	public SQLDeleteAllConfiguration getSqlDeleteAll() {
		return sqlDeleteAll;
	}

	public EntityConfiguration sqlDeleteAll(SQLDeleteAllConfiguration sqlDeleteAll) {
		this.annotations.add(SQLDeleteAll.class);
		this.sqlDeleteAll = sqlDeleteAll;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public void comment(String comment) {
		this.comment = comment;
	}

	public EntityConfiguration indexes(IndexConfiguration[] indexes) {
		this.annotations.add(Indexes.class);
		this.indexes = indexes;
		return this;
	}

	public EntityConfiguration index(IndexConfiguration[] indexes) {
		this.annotations.add(Index.class);
		this.indexes = indexes;
		return this;
	}

	public EntityConfiguration index(IndexConfiguration index) {
		this.annotations.add(Index.class);
		this.indexes = new IndexConfiguration[] { index };
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public EntityConfiguration schema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getCatalog() {
		return catalog;
	}

	public EntityConfiguration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public UniqueConstraintConfiguration[] getUniqueConstraints() {
		return uniqueConstraints;
	}

	public EntityConfiguration uniqueConstraints(UniqueConstraintConfiguration[] uniqueConstraints) {
		this.annotations.add(UniqueConstraint.class);
		this.uniqueConstraints = uniqueConstraints;
		return this;
	}

	public EntityConfiguration remote(String displayLabel, String mobileActionExport, String mobileActionImport,
			RemoteParamConfiguration[] importParams, RemoteParamConfiguration[] exportParams,
			int exportOrderToSendData, String[] exportFields, ConnectivityType importConnectivityType,
			ConnectivityType exportConnectivityType) {
		annotations.add(Remote.class);
		this.remote = new RemoteConfiguration(displayLabel, mobileActionExport, mobileActionImport, importParams,
				exportParams, exportOrderToSendData, exportFields, importConnectivityType, exportConnectivityType);
		return this;
	}

	public EntityConfiguration remote(RemoteConfiguration remote) {
		annotations.add(Remote.class);
		this.remote = remote;
		return this;
	}

	public RemoteConfiguration getRemote() {
		return remote;
	}


	public DiscriminatorType getDiscriminatorColumnType() {
		return discriminatorColumnType;
	}

	public void setDiscriminatorColumnType(DiscriminatorType discriminatorColumnType) {
		this.discriminatorColumnType = discriminatorColumnType;
	}

	public EntityConfiguration getEntityConfigurationBySourceClass(Class<?> sourceClazz) {
		return model.getEntityConfigurationBySourceClass(sourceClazz);
	}
	

	public ConvertConfiguration[] getConverts() {
		return converts;
	}

	public EntityConfiguration converts(ConvertConfiguration[] converts) {
		this.converts = converts;
		this.annotations.add(Converts.class);
		return this;
	}

	public EntityConfiguration convert(ConvertConfiguration[] converts) {
		this.converts = converts;
		this.annotations.add(Converts.class);
		return this;
	}

	public EntityConfiguration convert(ConvertConfiguration convert) {
		this.converts = new ConvertConfiguration[] { convert };
		this.annotations.add(Convert.class);
		return this;
	}

}
