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
package br.com.anteros.persistence.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.sql.binder.DateParameterBinding;
import br.com.anteros.persistence.sql.binder.DateTimeParameterBinding;

/**
 * 
 * @author Edson Martins - Anteros
 *
 */
public class NamedParameter {

	private String name;
	private Object value;
	private boolean key;
	private TemporalType temporalType;
	
	public NamedParameter(String name) {
		this.name = name;
		this.key = false;
	}

	public NamedParameter(String name, Object value) {
		this.name = name;
		this.value = value;
		this.key = false;
	}

	public NamedParameter(String name, Object value, TemporalType temporalType) {
		this.name = name;
		this.value = value;
		this.key = false;
		this.temporalType = temporalType;
	}

	public NamedParameter(String name, Object value, boolean key) {
		this.name = name;
		this.value = value;
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		if (temporalType != null && temporalType == TemporalType.DATE)
			return new DateParameterBinding(value);
		else if (temporalType != null && temporalType == TemporalType.DATE_TIME)
			return new DateTimeParameterBinding(value);
		else
			return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}

	public static NamedParameterList list() {
		return new NamedParameterList();
	}

	public static String[] getAllNames(List<NamedParameter> parameters) {
		List<String> result = new ArrayList<String>();
		for (NamedParameter param : parameters) {
			result.add(param.getName());
		}
		return result.toArray(new String[] {});
	}

	public static Object[] getAllValues(List<NamedParameter> parameters) {
		List<Object> result = new ArrayList<Object>();
		for (NamedParameter param : parameters)
			result.add(param.getValue());
		return result.toArray(new Object[] {});
	}

	public static String[] getNames(List<NamedParameter> parameters) {
		List<String> result = new ArrayList<String>();
		for (NamedParameter param : parameters) {
			if (!param.isKey())
				result.add(param.getName());
		}
		return result.toArray(new String[] {});
	}

	public static String[] getNames(Collection<NamedParameter> parameters) {
		List<String> result = new ArrayList<String>();
		for (NamedParameter param : parameters) {
			if (!param.isKey())
				result.add(param.getName());
		}
		return result.toArray(new String[] {});
	}

	public static Object[] getValues(List<NamedParameter> parameters) {
		List<Object> result = new ArrayList<Object>();
		for (NamedParameter param : parameters) {
			if (!param.isKey())
				result.add(param.getValue());
		}
		return result.toArray(new Object[] {});
	}

	public static String[] getNamesKey(List<NamedParameter> parameters) {
		List<String> result = new ArrayList<String>();
		for (NamedParameter param : parameters) {
			if (param.isKey())
				result.add(param.getName());
		}
		return result.toArray(new String[] {});
	}

	public static String[] getNamesKey(Collection<NamedParameter> parameters) {
		List<String> result = new ArrayList<String>();
		for (NamedParameter param : parameters) {
			if (param.isKey())
				result.add(param.getName());
		}
		return result.toArray(new String[] {});
	}

	public static Object[] getValuesKey(List<NamedParameter> parameters) {
		List<Object> result = new ArrayList<Object>();
		for (NamedParameter param : parameters) {
			if (param.isKey())
				result.add(param.getValue());
		}
		return result.toArray(new Object[] {});
	}

	public static List<NamedParameter> convertToList(Collection<NamedParameter> parameters) {
		List<NamedParameter> result = new ArrayList<NamedParameter>();
		for (NamedParameter param : parameters) {
			result.add(param);
		}
		return result;
	}
	
	public static NamedParameter[] toArray(List<NamedParameter> parameters){
		if (parameters==null)
			return null;
		return parameters.toArray(new NamedParameter[]{});
	}

	public static NamedParameter getNamedParameterByName(Collection<NamedParameter> parameters, String name) {
		for (NamedParameter param : parameters) {
			if (param.getName().equalsIgnoreCase(name))
				return param;
		}
		return null;
	}
	
	public static NamedParameter getNamedParameterByName(NamedParameter[] parameters, String name) {
		for (NamedParameter param : parameters) {
			if (param.getName().equalsIgnoreCase(name))
				return param;
		}
		return null;
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public static NamedParameter[] convert(Map<String, Object> parameter) {
		List<NamedParameter> result = new ArrayList<NamedParameter>();
		for (String parameterName : parameter.keySet())
			result.add(new NamedParameter(parameterName, parameter.get(parameterName)));

		return result.toArray(new NamedParameter[] {});
	}
	
	public static boolean hasOutputParameters(NamedParameter[] parameters){
		if (parameters ==null)
			return false;
		
		for (NamedParameter parameter : parameters){
			if (parameter instanceof OutputNamedParameter)
				return true;
		}
		return false;
	}
	
	public static boolean hasOutputParameters(Collection<NamedParameter> parameters){
		if (parameters ==null)
			return false;
		
		for (NamedParameter parameter : parameters){
			if (parameter instanceof OutputNamedParameter)
				return true;
		}
		return false;
	}


	public TemporalType getTemporalType() {
		return temporalType;
	}

	public void setTemporalType(TemporalType temporalType) {
		this.temporalType = temporalType;
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
		NamedParameter other = (NamedParameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public static int countOutputParameters(NamedParameter[] parameters){
		if (parameters ==null)
			return 0;
		int result = 0;
		for (NamedParameter parameter : parameters){
			if (parameter instanceof OutputNamedParameter)
				result++;
		}
		return result;
	}
	
	public static int countOutputParameters(Collection<NamedParameter> parameters){
		if (parameters ==null)
			return 0;
		int result = 0;
		for (NamedParameter parameter : parameters){
			if (parameter instanceof OutputNamedParameter)
				result++;
		}
		return result;
	}

}
