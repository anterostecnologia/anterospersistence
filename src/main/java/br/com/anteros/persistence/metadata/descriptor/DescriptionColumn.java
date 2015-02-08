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
package br.com.anteros.persistence.metadata.descriptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.annotation.type.BooleanType;
import br.com.anteros.persistence.metadata.annotation.type.DiscriminatorType;
import br.com.anteros.persistence.metadata.annotation.type.EnumType;
import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;
import br.com.anteros.persistence.metadata.annotation.type.ReturnType;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;
import br.com.anteros.persistence.metadata.descriptor.type.ColumnType;

/**
 * Classe responsável pela descrição da Coluna de uma entidade.
 * 
 */
public class DescriptionColumn {
	private Field field;
	private String columnName;
	private ColumnType columnType = ColumnType.NONE;
	private int dataType;
	private String expression;
	private EntityCache entityCache;
	private DescriptionField descriptionField;
	private String referencedColumnName;
	private List<DescriptionColumn> columns = new ArrayList<DescriptionColumn>();
	private boolean compositeId = false;
	private DescriptionColumn referencedColumn;
	private boolean foreignKey = false;
	private boolean enumerated = false;
	private boolean versioned = false;
	private boolean lob;
	private GeneratedType generatedType;
	private Map<GeneratedType, DescriptionGenerator> generators = new HashMap<GeneratedType, DescriptionGenerator>();
	private boolean required = false;
	private int precision = 0;
	private int length = 0;
	private boolean mapKeyColumn = false;
	private boolean elementColumn = false;
	private boolean inversedColumn = false;
	private TemporalType temporalType;
	private String referencedTableName;
	private boolean joinColumn;
	private int scale = 0;
	private String trueValue;
	private String falseValue;
	private BooleanType booleanType;
	private ReturnType booleanReturnType;
	private EnumType enumType;
	private Map<String, String> enumValues;
	private String tableName;
	private String defaultValue;
	private Class<?> elementCollectionType;
	private String columnDefinition;
	private boolean insertable = true;
	private boolean updatable = true;
	private boolean unique = false;
	private boolean isIdSynchronism = false;
	private boolean externalFile;
	private String dateTimePattern = "";
	private String datePattern = "";
	private String timePattern = "";
	private DiscriminatorType discriminatorType;

	public DescriptionColumn(EntityCache entityCache, Field field) {
		setField(field);
		this.tableName = entityCache.getTableName();
		this.entityCache = entityCache;
	}

	public DescriptionColumn(EntityCache cache) {
		this(cache, null);
	}

	public String getTrueValue() {
		return trueValue;
	}

	public void setTrueValue(String trueValue) {
		this.trueValue = trueValue;
	}

	public String getFalseValue() {
		return falseValue;
	}

	public void setFalseValue(String falseValue) {
		this.falseValue = falseValue;
	}

	public String getReferencedTableName() {
		return referencedTableName;
	}

	public void setReferencedTableName(String referencedTableName) {
		this.referencedTableName = referencedTableName;
	}

	public boolean isCompositeId() {
		return compositeId;
	}

	public void setCompositeId(boolean compositeId) {
		this.compositeId = compositeId;
	}

	public int getDataType() {
		return dataType;
	}

	public void setField(Field field) {
		if (field != null)
			field.setAccessible(true);
		this.field = field;
	}

	public DescriptionColumn(DescriptionColumn descriptionColumn) {
		this.columnName = descriptionColumn.getColumnName();
		this.columnType = descriptionColumn.getColumnType();
	}

	public DescriptionColumn() {
	}

	public DescriptionColumn(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Retorna field da Classe.
	 * 
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Retorna nome da Coluna.
	 */
	public String getColumnName() {
		return columnName;
	}

	public boolean isRequired() {
		return required;
	}

	/**
	 * 
	 * @param columnName
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public ColumnType getColumnType() {
		return columnType;
	}

	public EntityCache getEntityCache() {
		return entityCache;
	}

	public void setColumnType(ColumnType columnType) {
		this.columnType = columnType;
	}

	public Integer getIntValue(Object object) {
		try {
			return (Integer) field.get(object);
		} catch (Exception e) {
			return null;
		}
	}

	public Long getLongValue(Object object) {
		try {
			return field.getLong(object);
		} catch (Exception e) {
			return null;
		}
	}

	public String getStringValue(Object object) {
		try {
			return (String) field.get(object);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isDiscriminatorColumn() {
		return this.columnType == ColumnType.DISCRIMINATOR;
	}

	public boolean isForeignKey() {
		return foreignKey;
	}

	public int getColumnDataType() {
		return this.dataType;
	}

	public boolean isPrimaryKey() {
		return columnType == ColumnType.PRIMARY_KEY;
	}

	public boolean isNoneColumn() {
		return columnType == ColumnType.NONE;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return this.expression;
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	public void setDescriptionField(DescriptionField descriptionField) {
		this.descriptionField = descriptionField;
	}

	public DescriptionField getDescriptionField() {
		return descriptionField;
	}

	public boolean hasDescriptionField() {
		return this.descriptionField != null;
	}

	public void setReferencedColumnName(String inversedColumn) {
		this.referencedColumnName = inversedColumn;
	}

	public void setReferencedColumnName(DescriptionColumn referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	@Override
	public String toString() {
		return new StringBuilder("Entity=").append((entityCache == null ? "" : entityCache.getClass().getSimpleName())).append(" columnName=")
				.append(columnName).append(" compositeId=").append(compositeId).append(" referencedColumnName=").append(referencedColumnName)
				.append(" isPrimaryKey=").append(isPrimaryKey()).append(" isForeignKey=").append(isForeignKey()).append(" isDiscriminatorColumn=")
				.append(isDiscriminatorColumn()).append(" isNoneColumn=").append(isNoneColumn()).append(" targetClass=")
				.append((this.getDescriptionField() == null ? "<no descriptionField>" : this.getDescriptionField().getTargetClass())).toString();
	}

	public List<DescriptionColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<DescriptionColumn> columns) {
		this.columns = columns;
	}

	public DescriptionColumn getReferencedColumn() {
		return referencedColumn;
	}

	public void setReferencedColumn(DescriptionColumn inversedColumn) {
		this.referencedColumn = inversedColumn;
		if ((getLength() == 0) && (getPrecision() == 0)) {
			if (isForeignKey()) {
				setLength(getReferencedColumn().getLength());
				setPrecision(getReferencedColumn().getPrecision());
				setScale(getReferencedColumn().getScale());
			}
		}

	}

	public void setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
	}

	public void setVersioned(boolean versioned) {
		this.versioned = versioned;
	}

	public boolean isVersioned() {
		return versioned;
	}

	public void setLob(boolean blob) {
		this.lob = blob;
	}

	public boolean isLob() {
		return lob;
	}

	public boolean hasGenerator() {
		return (generatedType != null);
	}

	public String getSequenceName() {
		for (DescriptionGenerator descriptionGenerator : generators.values()) {
			if (descriptionGenerator.isSequenceGenerator()) {
				return descriptionGenerator.getSequenceName();
			}
		}
		return "";
	}

	public FieldEntityValue getFieldEntityValue(Object object) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(this.getColumnName(), ObjectUtils.cloneObject(ReflectionUtils.getFieldValueByName(object, this.getField().getName())));
		return new FieldEntityValue(this.getField().getName(), result, object);
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPrecision() {
		return precision;
	}

	public int getLength() {
		return length;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public void setMapKeyColumn(boolean mapKey) {
		this.mapKeyColumn = mapKey;
	}

	public boolean isMapKeyColumn() {
		return mapKeyColumn;
	}

	public void setElementColumn(boolean elementColumn) {
		this.elementColumn = elementColumn;
	}

	public boolean isElementColumn() {
		return elementColumn;
	}

	public boolean hasField() {
		return this.field != null;
	}

	public void setInversedJoinColumn(boolean inversedColumn) {
		this.inversedColumn = inversedColumn;
	}

	public boolean isInversedJoinColumn() {
		return inversedColumn;
	}

	public Map<GeneratedType, DescriptionGenerator> getGenerators() {
		return generators;
	}

	public void setGenerators(Map<GeneratedType, DescriptionGenerator> generators) {
		this.generators = generators;
	}

	public GeneratedType getGeneratedType() {
		return generatedType;
	}

	public void setGeneratedType(GeneratedType generatedType) {
		this.generatedType = generatedType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
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
		DescriptionColumn other = (DescriptionColumn) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		return true;
	}

	public void setTemporalType(TemporalType type) {
		this.temporalType = type;
	}

	public TemporalType getTemporalType() {
		return temporalType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public boolean isTemporalType() {
		return this.temporalType != null;
	}

	public boolean isAutoIncrement() {
		return this.generatedType == GeneratedType.IDENTITY || this.generatedType == GeneratedType.AUTO;
	}

	public String getTableName() {
		return this.entityCache.getTableName();
	}

	public void setJoinColumn(boolean joinColumn) {
		this.joinColumn = joinColumn;
	}

	public boolean isJoinColumn() {
		return joinColumn;
	}

	public boolean isBoolean() {
		return this.trueValue != null && this.falseValue != null;
	}

	public void setEnumType(EnumType type) {
		this.enumType = type;
	}

	public EnumType getEnumType() {
		return enumType;
	}

	public boolean isEnumerated() {
		return this.enumType != null;
	}

	public String getEnumValue(String value) {
		for (String key : this.enumValues.keySet()) {
			if (value.equals(enumValues.get(key)))
				return key;
		}
		return null;
	}

	public String getValueEnum(String key) {
		return this.enumValues.get(key);
	}

	public void setEnumValues(Map<String, String> enumValues) {
		this.enumValues = enumValues;
	}

	public Map<String, String> getEnumValues() {
		return enumValues;
	}

	public Object getColumnValue(Object object) throws Exception {
		if (this.isPrimaryKey()) {
			Map<String, Object> primaryKeysAndValues = this.getDescriptionField().getEntityCache().getPrimaryKeysAndValues(object);
			for (String key : primaryKeysAndValues.keySet()) {
				if (key.equals(this.columnName))
					return primaryKeysAndValues.get(key);
			}
		} else if (this.foreignKey) {
			Object foreignKey = descriptionField.getObjectValue(object);
			Map<String, Object> primaryKeysAndValues = this.getReferencedColumn().getEntityCache().getPrimaryKeysAndValues(foreignKey);
			for (String key : primaryKeysAndValues.keySet()) {
				if (key.equals(this.referencedColumnName))
					return primaryKeysAndValues.get(key);
			}
		}

		return descriptionField.getObjectValue(object);
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Class<?> getFieldType() {
		if (this.isJoinColumn()) {
			for (DescriptionColumn column : descriptionField.getEntityCache().getDescriptionColumns()) {
				if (column.getColumnName().equals(this.getColumnName()))
					return column.getFieldType();
			}
			if (descriptionField.getTargetEntity() != null) {
				for (DescriptionColumn column : descriptionField.getTargetEntity().getDescriptionColumns()) {
					if (column.getColumnName().equals(this.getReferencedColumnName()))
						return column.getFieldType();
				}
			}
		} else if (this.isForeignKey() && (descriptionField.getTargetEntity() != null)) {
			for (DescriptionColumn column : descriptionField.getTargetEntity().getPrimaryKeyColumns()) {
				if (column.getColumnName().equals(this.getReferencedColumnName()))
					return column.getFieldType();
			}
		} else if (this.isEnumerated()) {
			return String.class;
		} else if (this.isMapKeyColumn()) {
			return this.getElementCollectionType();
		}

		return field.getType();
	}

	public Class getElementCollectionType() {
		return elementCollectionType;
	}

	public void setElementCollectionType(Class elementCollectionType) {
		this.elementCollectionType = elementCollectionType;
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public void setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public BooleanType getBooleanType() {
		return booleanType;
	}

	public void setBooleanType(BooleanType booleanType) {
		this.booleanType = booleanType;
	}

	public ReturnType getBooleanReturnType() {
		return booleanReturnType;
	}

	public void setBooleanReturnType(ReturnType booleanReturnType) {
		this.booleanReturnType = booleanReturnType;
	}

	public boolean hasConvert() {
		return (getConvert() != null);
	}

	public boolean isIdSynchronism() {
		return isIdSynchronism;
	}

	public void setIdSynchronism(boolean isIdSynchronism) {
		this.isIdSynchronism = isIdSynchronism;
	}

	public boolean isExternalFile() {
		return externalFile;
	}

	public void setExternalFile(boolean externalFile) {
		this.externalFile = externalFile;
	}

	public String getDateTimePattern() {
		return dateTimePattern;
	}

	public void setDateTimePattern(String dateTimePattern) {
		this.dateTimePattern = dateTimePattern;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String getTimePattern() {
		return timePattern;
	}

	public void setTimePattern(String timePattern) {
		this.timePattern = timePattern;
	}

	public DiscriminatorType getDiscriminatorType() {
		return discriminatorType;
	}

	public void setDiscriminatorType(DiscriminatorType discriminatorType) {
		this.discriminatorType = discriminatorType;
	}

	public boolean hasDefaultValue() {
		if (defaultValue == null)
			return false;
		return (!"".equals(defaultValue));
	}

	public DescriptionConvert getConvert() {
		if ((descriptionField.getConverts() != null) && (descriptionField.getConverts().size() > 0)) {
			for (DescriptionConvert convert : descriptionField.getConverts()) {
				if (isMapKeyColumn()) {
					if (!StringUtils.isEmpty(convert.getAttributeName()) || (convert.getAttributeName().startsWith("key.")))
						return convert;
				} else if (isElementColumn()) {
					if (descriptionField.isAnyCollection())
						return convert;
					if (descriptionField.isMapTable()) {
						if (!StringUtils.isEmpty(convert.getAttributeName()) || (convert.getAttributeName().startsWith("value.")))
							return convert;
					}
				} else if (!descriptionField.isAnyCollectionOrMap()) {
					return convert;
				}
			}
		}
		return null;
	}

	public Object convertToEntityAttribute(Object value) throws Exception {
		if (!this.hasConvert())
			return value;
		DescriptionConvert convert = this.getConvert();
		AttributeConverter<?, ?> converter = convert.getConverter();
		value = ReflectionUtils.invokeMethod(converter, "convertToEntityAttribute", new Object[] { value },
				new Class[] { convert.getEntityAttributeType() });
		return value;
	}
	
	public Object convertToDatabaseColumn(Object value) throws Exception {
		if (!this.hasConvert())
			return value;
		DescriptionConvert convert = this.getConvert();
		AttributeConverter<?, ?> converter = convert.getConverter();
		value = ReflectionUtils.invokeMethod(converter, "convertToDatabaseColumn", new Object[] { value },
				new Class[] { convert.getEntityAttributeType() });
		return value;
	}
}
