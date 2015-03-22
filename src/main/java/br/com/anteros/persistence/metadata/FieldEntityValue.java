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

package br.com.anteros.persistence.metadata;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.utils.ReflectionUtils;

@SuppressWarnings("unchecked")
public class FieldEntityValue implements Comparable<FieldEntityValue> {

	private String name;
	private Object value;
	private Object source;

	public FieldEntityValue(String fieldName, Object value, Object source) {
		this.name = fieldName;
		this.value = value;
		this.source = source;
	}

	public int compareTo(FieldEntityValue target) {
		if (target == null)
			return -1;
		if (value instanceof FieldEntityValue[]) {
			if (((FieldEntityValue[]) value).length != ((FieldEntityValue[]) target.getValue()).length) {
				return -1;
			}
			for (int i = 0; i < ((FieldEntityValue[]) value).length; i++) {
				FieldEntityValue sourceField = ((FieldEntityValue[]) value)[i];
				boolean found = false;
				for (int k = 0; k < ((FieldEntityValue[]) target.getValue()).length; k++) {
					FieldEntityValue targetField = ((FieldEntityValue[]) target.getValue())[k];
					if (sourceField.compareTo(targetField) == 0) {
						found = true;
						break;
					}
				}
				if (!found)
					return -1;
			}
		} else if (value instanceof Map) {
			if (((Map<?, ?>) value).size() != ((Map<?, ?>) target.getValue()).size())
				return -1;

			Object targetvalue;
			Object sourceValue;
			Map<String, Object> mapValues = ((Map<String, Object>) value);
			for (String sourceKey : mapValues.keySet()) {
				sourceValue = mapValues.get(sourceKey);
				targetvalue = ((Map<?, ?>) target.getValue()).get(sourceKey);
				if ((sourceValue != null) || (targetvalue != null)) {
					if (targetvalue != null) {
						if (compareObject(sourceValue, targetvalue) != 0)
							return -1;
					} else
						return -1;
				}
			}
		}
		return 0;
	}

	
	@SuppressWarnings("rawtypes")
	private int compareObject(Object source, Object target) {
		if ((source == null) && (target == null))
			return 0;

		if ((source == null) && (target != null))
			return -1;

		if ((source != null) && (target == null))
			return 1;

		if (source instanceof String) {
			return ((String) source).compareTo((String) target);
		} else if (source instanceof Integer) {
			return ((Integer) source).compareTo((Integer) target);
		} else if (source instanceof Double) {
			return ((Double) source).compareTo((Double) target);
		} else if (source instanceof Long) {
			return ((Long) source).compareTo((Long) target);
		} else if (source instanceof Short) {
			return ((Short) source).compareTo((Short) target);
		} else if (source instanceof Boolean) {
			return ((Boolean) source).compareTo((Boolean) target);
		} else if (source instanceof Character) {
			return ((Character) source).compareTo((Character) target);
		} else if (source instanceof Float) {
			return ((Float) source).compareTo((Float) target);
		} else if (source instanceof Byte) {
			return ((Byte) source).compareTo((Byte) target);
		} else if (source instanceof BigInteger) {
			return ((BigInteger) source).compareTo((BigInteger) target);
		} else if (source instanceof BigDecimal) {
			return ((BigDecimal) source).compareTo((BigDecimal) target);
		} else if (source instanceof Date) {
			return ((Date) source).compareTo((Date) target);
		} else if (source instanceof Enum) {
			return ((Enum) source).compareTo((Enum) target);
		} else if (source instanceof byte[]) {
			return (Arrays.equals((byte[]) source, (byte[]) target) == true ? 0 : -1);
		} else {
			if ((source != null) && (target != null)) {
				if ((source.getClass() != target.getClass()))
					return -1;
				Field[] allDeclaredFields = ReflectionUtils.getAllDeclaredFields(source.getClass());
				for (Field field : allDeclaredFields) {
					try {
						Object sourceValue = field.get(source);
						Object targetValue = field.get(target);
						int result = compareObject(sourceValue, targetValue);
						if (result != 0)
							return result;
					} catch (Exception e) {
						return -1;
					}
				}
				return 0;
			}
		}
		return 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public static FieldEntityValue[] getEntityValuesDifference(FieldEntityValue[] source, FieldEntityValue[] target) {
		List<FieldEntityValue> result = new ArrayList<FieldEntityValue>();
		for (FieldEntityValue entitySource : source) {
			boolean found = false;
			for (FieldEntityValue entityTarget : target) {
				if (entitySource.compareTo(entityTarget) == 0)
					found = true;
			}
			if (!found)
				result.add(entitySource);
		}
		return result.toArray(new FieldEntityValue[] {});
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(name).append(" tipo->").append((value instanceof FieldEntityValue[] ? "COLLECTION" : "SIMPLE")).append(" valor->");
		if (value instanceof FieldEntityValue[]) {
			result.append("{");
			boolean appendComma = false;
			for (FieldEntityValue v : (FieldEntityValue[]) value) {
				if (appendComma)
					result.append(", ");
				result.append(v.toString());
				appendComma = true;
			}
			result.append("}");
		} else
			result.append(value);
		return result.toString();
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
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
		FieldEntityValue other = (FieldEntityValue) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
