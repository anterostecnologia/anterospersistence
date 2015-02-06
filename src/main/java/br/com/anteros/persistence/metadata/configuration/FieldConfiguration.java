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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.Assert;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.BooleanValue;
import br.com.anteros.persistence.metadata.annotation.Cascade;
import br.com.anteros.persistence.metadata.annotation.CollectionTable;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.Columns;
import br.com.anteros.persistence.metadata.annotation.Comment;
import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.Convert;
import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.annotation.Converts;
import br.com.anteros.persistence.metadata.annotation.Enumerated;
import br.com.anteros.persistence.metadata.annotation.ExternalFile;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.GeneratedValue;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.annotation.Index;
import br.com.anteros.persistence.metadata.annotation.Indexes;
import br.com.anteros.persistence.metadata.annotation.JoinTable;
import br.com.anteros.persistence.metadata.annotation.Lob;
import br.com.anteros.persistence.metadata.annotation.MapKeyColumn;
import br.com.anteros.persistence.metadata.annotation.MapKeyEnumerated;
import br.com.anteros.persistence.metadata.annotation.MapKeyTemporal;
import br.com.anteros.persistence.metadata.annotation.OrderBy;
import br.com.anteros.persistence.metadata.annotation.SQLDelete;
import br.com.anteros.persistence.metadata.annotation.SQLDeleteAll;
import br.com.anteros.persistence.metadata.annotation.SQLInsert;
import br.com.anteros.persistence.metadata.annotation.SQLUpdate;
import br.com.anteros.persistence.metadata.annotation.SequenceGenerator;
import br.com.anteros.persistence.metadata.annotation.TableGenerator;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.metadata.annotation.UniqueConstraint;
import br.com.anteros.persistence.metadata.annotation.Version;
import br.com.anteros.persistence.metadata.annotation.type.BooleanType;
import br.com.anteros.persistence.metadata.annotation.type.CascadeType;
import br.com.anteros.persistence.metadata.annotation.type.EnumType;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;
import br.com.anteros.persistence.metadata.annotation.type.ReturnType;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.metadata.descriptor.type.ConnectivityType;
import br.com.anteros.synchronism.annotation.IdSynchronism;
import br.com.anteros.synchronism.annotation.Remote;

public class FieldConfiguration {

	private Set<ColumnConfiguration> columns = new LinkedHashSet<ColumnConfiguration>();
	private String name;
	private Set<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>();
	private EnumType enumeratedType;
	private FetchConfiguration fetch;
	private ForeignKeyConfiguration foreignKey;
	private GeneratedType generatedType;
	private JoinTableConfiguration joinTable;
	private String mapKeyColumnName;
	private String orderByClause;
	private TemporalType temporalType;
	private String trueValue;
	private String falseValue;
	private BooleanType booleanType;
	private ReturnType booleanReturnType;
	private CascadeType[] cascadeTypes;
	private Class<?> type;
	private EntityConfiguration entity;
	private Field field;
	private CollectionTableConfiguration collectionTable;
	private TableGeneratorConfiguration tableGenerator;
	private boolean idSynchronism = false;
	private SequenceGeneratorConfiguration sequenceGenerator;
	private SQLInsertConfiguration sqlInsert;
	private SQLUpdateConfiguration sqlUpdate;
	private SQLDeleteConfiguration sqlDelete;
	private SQLDeleteAllConfiguration sqlDeleteAll;
	private boolean version = false;
	private String comment = "";
	private IndexConfiguration[] indexes;
	private ConvertConfiguration[] converts;
	private String convert;
	private String mapKeyConvert;
	private RemoteConfiguration remote;
	private boolean externalFile;

	public boolean isExternalFile() {
		return externalFile;
	}

	public void externalFile(boolean externalFile) {
		this.externalFile = externalFile;
	}

	public FieldConfiguration(EntityConfiguration entity) {
		this.entity = entity;
	}

	public FieldConfiguration(EntityConfiguration entity, String name) {
		this.entity = entity;
		this.name = name;
		this.field = ReflectionUtils.getFieldByName(entity.getSourceClazz(), name);
		if (this.field != null)
			this.type = this.field.getType();
	}

	public FieldConfiguration(EntityConfiguration entity, Field field) {
		Assert.notNull(field, "Parâmetro field é obrigatório. Erro criando FieldConfiguration.");
		this.entity = entity;
		if (field != null)
			this.name = field.getName();
		this.field = field;
		if (this.field != null)
			this.type = this.field.getType();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FieldConfiguration column(String name, int length, int precision, int scale, boolean required, String inversedColumn,
			boolean exportColumn, String defaultValue) {
		columns.add(new ColumnConfiguration(name, length, precision, scale, required, inversedColumn, exportColumn, defaultValue));
		annotations.add(Column.class);
		return this;
	}

	public FieldConfiguration column(String name, int length, int precision, int scale, boolean required, String inversedColumn) {
		columns.add(new ColumnConfiguration(name, length, precision, scale, required, inversedColumn));
		annotations.add(Column.class);
		return this;
	}

	public FieldConfiguration column(String name) {
		columns.add(new ColumnConfiguration(name));
		annotations.add(Column.class);
		return this;
	}

	public FieldConfiguration column(String name, int length, int precision, int scale, boolean required) {
		columns.add(new ColumnConfiguration(name, length, precision, scale, required));
		annotations.add(Column.class);
		return this;
	}

	public FieldConfiguration column(ColumnConfiguration column) {
		columns.add(column);
		annotations.add(Column.class);
		if (columns.size() > 1) {
			annotations.remove(Column.class);
			annotations.add(Columns.class);
		}

		return this;
	}

	public FieldConfiguration columns(ColumnConfiguration[] columns) {
		for (ColumnConfiguration column : columns)
			this.columns.add(column);
		annotations.add(Columns.class);
		return this;
	}

	public FieldConfiguration compositeId() {
		annotations.add(CompositeId.class);
		return this;
	}

	public FieldConfiguration enumerated(EnumType value) {
		annotations.add(Enumerated.class);
		this.enumeratedType = value;
		return this;
	}

	public FieldConfiguration fetch(FetchType type, FetchMode mode, String mappedBy, Class<?> targetEntity, String statement) {
		annotations.add(Fetch.class);
		this.fetch = new FetchConfiguration(statement, type, mode, mappedBy, targetEntity);
		return this;
	}

	public FieldConfiguration fetch(FetchConfiguration fechConfiguration) {
		annotations.add(Fetch.class);
		this.fetch = fechConfiguration;
		return this;
	}

	public FieldConfiguration foreignKey(String statement, FetchType type, FetchMode mode, String mappedBy, boolean useIndex) {
		annotations.add(ForeignKey.class);
		this.foreignKey = new ForeignKeyConfiguration(statement, type, mode, mappedBy, useIndex);
		return this;
	}

	public FieldConfiguration foreignKey(ForeignKeyConfiguration foreignKeyConfiguration) {
		annotations.add(ForeignKey.class);
		this.foreignKey = foreignKeyConfiguration;
		return this;
	}

	public FieldConfiguration foreignKey() {
		annotations.add(ForeignKey.class);
		this.foreignKey = new ForeignKeyConfiguration();
		return this;
	}

	public FieldConfiguration generatedValue(GeneratedType strategy) {
		annotations.add(GeneratedValue.class);
		this.generatedType = strategy;
		return this;
	}

	public FieldConfiguration id() {
		annotations.add(Id.class);
		return this;
	}

	public FieldConfiguration joinTable(String name, JoinColumnConfiguration[] joinColumns, JoinColumnConfiguration[] inversedJoinColumns) {
		annotations.add(JoinTable.class);
		this.joinTable = new JoinTableConfiguration(name, joinColumns, inversedJoinColumns);
		return this;
	}

	public FieldConfiguration joinTable(JoinTableConfiguration joinTableConfiguration) {
		annotations.add(JoinTable.class);
		this.joinTable = joinTableConfiguration;
		return this;
	}

	public FieldConfiguration sequenceGenerator(String sequenceName, String catalog, int initialValue, int startsWith, String schema) {
		annotations.add(SequenceGenerator.class);
		this.sequenceGenerator = new SequenceGeneratorConfiguration(sequenceName, catalog, initialValue, startsWith, schema);
		return this;
	}

	public FieldConfiguration sequenceGenerator(SequenceGeneratorConfiguration sequenceGeneratorConfiguration) {
		annotations.add(SequenceGenerator.class);
		this.sequenceGenerator = sequenceGeneratorConfiguration;
		return this;
	}

	public FieldConfiguration lob(FetchType fetchType) {
		annotations.add(Lob.class);
		this.fetch = new FetchConfiguration(fetchType);
		return this;
	}

	public FieldConfiguration mapKeyColumn(String name) {
		annotations.add(MapKeyColumn.class);
		this.mapKeyColumnName = name;
		return this;
	}

	public FieldConfiguration mapKeyEnumerated(EnumType enumType) {
		annotations.add(MapKeyEnumerated.class);
		this.enumeratedType = enumType;
		return this;
	}

	public FieldConfiguration mapKeyTemporal(TemporalType temporalType) {
		annotations.add(MapKeyTemporal.class);
		this.temporalType = temporalType;
		return this;
	}

	public FieldConfiguration orderBy(String clause) {
		annotations.add(OrderBy.class);
		this.orderByClause = clause;
		return this;
	}

	public FieldConfiguration temporal(TemporalType type) {
		annotations.add(Temporal.class);
		this.temporalType = type;
		return this;
	}

	public FieldConfiguration transientField() {
		annotations.add(Transient.class);
		return this;
	}

	public FieldConfiguration booleanValue(String trueValue, String falseValue, BooleanType type) {
		annotations.add(BooleanValue.class);
		this.trueValue = trueValue;
		this.falseValue = falseValue;
		this.booleanType = type;
		return this;
	}

	public FieldConfiguration cascade(CascadeType[] values) {
		annotations.add(Cascade.class);
		this.cascadeTypes = values;
		return this;
	}

	public FieldConfiguration cascade(CascadeType type) {
		annotations.add(Cascade.class);
		this.cascadeTypes = new CascadeType[] { type };
		return this;
	}

	public FieldConfiguration collectionTable(String name, JoinColumnConfiguration[] joinColumns) {
		annotations.add(CollectionTable.class);
		this.collectionTable = new CollectionTableConfiguration(name, joinColumns);
		return this;
	}

	public FieldConfiguration collectionTable(CollectionTableConfiguration collectionTableConfiguration) {
		annotations.add(CollectionTable.class);
		this.collectionTable = collectionTableConfiguration;
		return this;
	}

	public FieldConfiguration tableGenerator(TableGeneratorConfiguration tableGeneratorConfiguration) {
		annotations.add(TableGenerator.class);
		this.tableGenerator = tableGeneratorConfiguration;
		return this;
	}

	public boolean isAnnotationPresent(Class annotationClass) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldConfiguration other = (FieldConfiguration) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Set<ColumnConfiguration> getColumns() {
		return columns;
	}

	public Set<Class<? extends Annotation>> getAnnotations() {
		return annotations;
	}

	public EnumType getEnumeratedType() {
		return enumeratedType;
	}

	public FetchConfiguration getFetch() {
		return fetch;
	}

	public ForeignKeyConfiguration getForeignKey() {
		return foreignKey;
	}

	public GeneratedType getGeneratedType() {
		return generatedType;
	}

	public JoinTableConfiguration getJoinTable() {
		return joinTable;
	}

	public String getMapKeyColumnName() {
		return mapKeyColumnName;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

	public TemporalType getTemporalType() {
		return temporalType;
	}

	public String getTrueValue() {
		return trueValue;
	}

	public String getFalseValue() {
		return falseValue;
	}

	public CascadeType[] getCascadeTypes() {
		return cascadeTypes;
	}

	public Class<?> getType() {
		return type;
	}

	public Field getField() {
		return field;
	}

	public EntityConfiguration getEntity() {
		return entity;
	}

	public CollectionTableConfiguration getCollectionTable() {
		return collectionTable;
	}

	public TableGeneratorConfiguration getTableGenerator() {
		return tableGenerator;
	}

	@Override
	public String toString() {
		return field.getName() + " [" + columns.toString() + "]";
	}

	public void loadAnnotations() {
		Annotation[] annotations = field.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof BooleanValue) {
				booleanValue(((BooleanValue) annotation).trueValue(), ((BooleanValue) annotation).falseValue(), ((BooleanValue) annotation).type());
			} else if (annotation instanceof Cascade) {
				cascade(((Cascade) annotation).values());
			} else if (annotation instanceof MapKeyEnumerated) {
				cascade(((Cascade) annotation).values());
			} else if (annotation instanceof MapKeyTemporal) {
				cascade(((Cascade) annotation).values());
			} else if (annotation instanceof CollectionTable) {
				collectionTable(new CollectionTableConfiguration(((CollectionTable) annotation)));
			} else if (annotation instanceof Column) {
				ColumnConfiguration newColumn = new ColumnConfiguration(((Column) annotation));
				newColumn.unique(field.isAnnotationPresent(UniqueConstraint.class));
				column(newColumn);
			} else if ((annotation instanceof Indexes) || (annotation instanceof Index)) {
				Index[] indexes = null;
				if (annotation instanceof Indexes)
					indexes = ((Indexes) annotation).value();
				else
					indexes = new Index[] { (Index) annotation };

				IndexConfiguration[] indexesConf = null;
				if (indexes != null) {
					indexesConf = new IndexConfiguration[indexes.length];
					for (int i = 0; i < indexes.length; i++) {
						indexesConf[i] = new IndexConfiguration(indexes[i].name(), indexes[i].columnNames()).catalog(indexes[i].catalog())
								.schema(indexes[i].schema()).unique(indexes[i].unique());
					}
				}
				if (annotation instanceof Indexes)
					indexes(indexesConf);
				else
					index(indexesConf);
			} else if ((annotation instanceof Columns) || (annotation instanceof Column.List)) {
				Column[] cols = null;
				if (annotation instanceof Columns) {
					cols = ((Columns) annotation).columns();
				} else {
					cols = ((Column.List) annotation).value();
				}
				ColumnConfiguration[] colsConf = null;
				if (cols != null) {
					colsConf = new ColumnConfiguration[cols.length];
					for (int i = 0; i < cols.length; i++) {
						colsConf[i] = new ColumnConfiguration(cols[i]);
					}
					columns(colsConf);
				}
			} else if (annotation instanceof CompositeId) {
				compositeId();
			} else if (annotation instanceof Enumerated) {
				enumerated(((Enumerated) annotation).value());
			} else if (annotation instanceof Fetch) {
				fetch(new FetchConfiguration(((Fetch) annotation)));
			} else if (annotation instanceof ForeignKey) {
				foreignKey(new ForeignKeyConfiguration(((ForeignKey) annotation)));
			} else if (annotation instanceof GeneratedValue) {
				generatedValue(((GeneratedValue) annotation).strategy());
			} else if (annotation instanceof Id) {
				id();
			} else if (annotation instanceof JoinTable) {
				joinTable(new JoinTableConfiguration(((JoinTable) annotation)));
			} else if (annotation instanceof Lob) {
				lob(((Lob) annotation).type());
			} else if (annotation instanceof MapKeyColumn) {
				mapKeyColumn(((MapKeyColumn) annotation).name());
			} else if (annotation instanceof OrderBy) {
				orderBy(((OrderBy) annotation).clause());
			} else if (annotation instanceof TableGenerator) {
				tableGenerator(new TableGeneratorConfiguration((TableGenerator) annotation));
			} else if (annotation instanceof Temporal) {
				temporal(((Temporal) annotation).type());
			} else if (annotation instanceof Transient) {
				transientField();
			} else if (annotation instanceof SQLInsert) {
				sqlInsert(new SQLInsertConfiguration((SQLInsert) annotation));
			} else if (annotation instanceof SQLDelete) {
				sqlDelete(new SQLDeleteConfiguration((SQLDelete) annotation));
			} else if (annotation instanceof SQLDeleteAll) {
				sqlDeleteAll(new SQLDeleteAllConfiguration((SQLDeleteAll) annotation));
			} else if (annotation instanceof SQLUpdate) {
				sqlUpdate(new SQLUpdateConfiguration((SQLUpdate) annotation));
			} else if (annotation instanceof Remote) {
				remote(new RemoteConfiguration((Remote) annotation));
			} else if (annotation instanceof SequenceGenerator) {
				sequenceGenerator(new SequenceGeneratorConfiguration((SequenceGenerator) annotation));
			} else if (annotation instanceof Comment) {
				comment(((Comment) annotation).value());
			} else if (annotation instanceof ExternalFile) {
				externalFile(true);
			} else if ((annotation instanceof Converts) || (annotation instanceof Convert) || (annotation instanceof Convert.List)) {
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
			} else if (annotation instanceof IdSynchronism) {
				idSynchronism();
			}
		}

		/*
		 * Se possuir configuração Index com apenas um indice o qual não foi definido columnNames assume as colunas do
		 * campo como colunas do indice
		 */
		if ((getIndexes() != null) && (getIndexes().length == 1)) {
			List<String> cns = new ArrayList<String>();
			for (ColumnConfiguration columnConfiguration : columns) {
				cns.add(columnConfiguration.getName());
			}
			getIndexes()[0].columns(cns.toArray(new String[] {}));
		}
	}

	public boolean isIdSynchronism() {
		return idSynchronism;
	}

	public SequenceGeneratorConfiguration getSequenceGenerator() {
		return sequenceGenerator;
	}

	public SQLInsertConfiguration getSqlInsert() {
		return sqlInsert;
	}

	public FieldConfiguration sqlInsert(SQLInsertConfiguration sqlInsert) {
		this.annotations.add(SQLInsert.class);
		this.sqlInsert = sqlInsert;
		return this;
	}

	public SQLUpdateConfiguration getSqlUpdate() {
		return sqlUpdate;
	}

	public FieldConfiguration sqlUpdate(SQLUpdateConfiguration sqlUpdate) {
		this.annotations.add(SQLUpdate.class);
		this.sqlUpdate = sqlUpdate;
		return this;
	}

	public SQLDeleteConfiguration getSqlDelete() {
		return sqlDelete;
	}

	public FieldConfiguration sqlDelete(SQLDeleteConfiguration sqlDelete) {
		this.annotations.add(SQLDelete.class);
		this.sqlDelete = sqlDelete;
		return this;
	}

	public SQLDeleteAllConfiguration getSqlDeleteAll() {
		return sqlDeleteAll;
	}

	public FieldConfiguration sqlDeleteAll(SQLDeleteAllConfiguration sqlDeleteAll) {
		this.annotations.add(SQLDeleteAll.class);
		this.sqlDeleteAll = sqlDeleteAll;
		return this;
	}

	public boolean isVersion() {
		return version;
	}

	public FieldConfiguration version() {
		this.annotations.add(Version.class);
		this.version = true;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public void comment(String comment) {
		this.comment = comment;
	}

	public IndexConfiguration[] getIndexes() {
		return indexes;
	}

	public void indexes(IndexConfiguration[] indexes) {
		this.indexes = indexes;
	}

	public FieldConfiguration index(IndexConfiguration[] indexes) {
		this.annotations.add(Index.class);
		this.indexes = indexes;
		return this;
	}

	public FieldConfiguration index(IndexConfiguration index) {
		this.annotations.add(Index.class);
		this.indexes = new IndexConfiguration[] { index };
		return this;
	}

	public ColumnConfiguration getSimpleColumn() {
		if (columns.size() > 0) {
			return columns.iterator().next();
		}
		return null;
	}

	public String[] getColumnNames() {
		List<String> result = new ArrayList<String>();
		for (ColumnConfiguration column : columns) {
			result.add(column.getName());
		}
		return result.toArray(new String[] {});
	}

	public String[] getUniqueColumnNames() {
		List<String> result = new ArrayList<String>();
		for (ColumnConfiguration column : columns) {
			if (column.isUnique())
				result.add(column.getName());
		}
		return result.toArray(new String[] {});
	}

	public boolean isForeignKey() {
		return (foreignKey != null);
	}

	public BooleanType getBooleanType() {
		return booleanType;
	}

	public FieldConfiguration setBooleanType(BooleanType booleanType) {
		this.booleanType = booleanType;
		return this;
	}

	public ReturnType getBooleanReturnType() {
		return booleanReturnType;
	}

	public FieldConfiguration booleanReturnType(ReturnType booleanReturnType) {
		this.booleanReturnType = booleanReturnType;
		return this;
	}

	public ConvertConfiguration[] getConverts() {
		return converts;
	}

	public FieldConfiguration converts(ConvertConfiguration[] converts) {
		if (converts == null)
			return this;
		this.converts = converts;
		this.annotations.add(Converts.class);
		return this;
	}

	public FieldConfiguration convert(ConvertConfiguration[] converts) {
		if (converts == null)
			return this;
		this.converts = converts;
		this.annotations.add(Converts.class);
		return this;
	}

	public FieldConfiguration convert(ConvertConfiguration convert) {
		if (convert == null)
			return this;
		this.converts = new ConvertConfiguration[] { convert };
		this.annotations.add(Convert.class);
		return this;
	}

	public FieldConfiguration remote(String displayLabel, String mobileActionExport, String mobileActionImport,
			RemoteParamConfiguration[] importParams, RemoteParamConfiguration[] exportParams, int exportOrderToSendData, String[] exportFields,
			ConnectivityType importConnectivityType, ConnectivityType exportConnectivityType) {
		annotations.add(Remote.class);
		this.remote = new RemoteConfiguration(displayLabel, mobileActionExport, mobileActionImport, importParams, exportParams,
				exportOrderToSendData, exportFields, importConnectivityType, exportConnectivityType);
		return this;
	}

	public FieldConfiguration remote(RemoteConfiguration remote) {
		annotations.add(Remote.class);
		this.remote = remote;
		return this;
	}

	public RemoteConfiguration getRemote() {
		return remote;
	}

	public FieldConfiguration idSynchronism() {
		annotations.add(IdSynchronism.class);
		this.idSynchronism = true;
		return this;
	}

	public String getConvert() {
		return convert;
	}

	public FieldConfiguration convert(String convert) {
		this.convert = convert;
		return this;
	}

	public String getMapKeyConvert() {
		return mapKeyConvert;
	}

	public FieldConfiguration mapKeyConvert(String mapKeyConvert) {
		this.mapKeyConvert = mapKeyConvert;
		return this;
	}

	public boolean isSimpleField() {
		return ReflectionUtils.isSimpleField(field);

	}

	public boolean isId() {
		return isAnnotationPresent(Id.class);
	}

	public boolean isCompositeId() {
		return isAnnotationPresent(CompositeId.class);
	}

	public EntityConfiguration getEntityConfigurationBySourceClass(Class<?> sourceClazz) {
		return entity.getEntityConfigurationBySourceClass(sourceClazz);
	}

	public ColumnConfiguration getColumnByName(String columnName) {
		for (ColumnConfiguration column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}
		return null;
	}
	
	public boolean isMap(){
		return ReflectionUtils.isImplementsInterface(this.getField().getType(), Map.class);
	}
	
	public boolean isCollection(){
		return ReflectionUtils.isImplementsInterface(this.getField().getType(), Collection.class);
	}

	public boolean isTransient() {
		return this.isAnnotationPresent(new Class[] { Transient.class });
	}
	
	public boolean isCollectionOrMap(){
		return isCollection() || isMap();
	}

}
