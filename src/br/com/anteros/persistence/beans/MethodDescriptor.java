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
package br.com.anteros.persistence.beans;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A MethodDescriptor describes a particular method that a Java Bean
 * supports for external access from other components.
 */

public class MethodDescriptor extends FeatureDescriptor {

	private Reference methodRef;

	private String[] paramNames;

	private List params;

	private ParameterDescriptor parameterDescriptors[];

	/**
	 * Constructs a <code>MethodDescriptor</code> from a <code>Method</code>.
	 * 
	 * @param method
	 *            The low-level method information.
	 */
	public MethodDescriptor(Method method) {
		this(method, null);
	}

	/**
	 * Constructs a <code>MethodDescriptor</code> from a <code>Method</code>
	 * providing descriptive information for each
	 * of the method's parameters.
	 * 
	 * @param method
	 *            The low-level method information.
	 * @param parameterDescriptors
	 *            Descriptive information for each of the
	 *            method's parameters.
	 */
	public MethodDescriptor(Method method, ParameterDescriptor parameterDescriptors[]) {
		setName(method.getName());
		setMethod(method);
		this.parameterDescriptors = parameterDescriptors;
	}

	/**
	 * Gets the method that this MethodDescriptor encapsualtes.
	 * 
	 * @return The low-level description of the method
	 */
	public synchronized Method getMethod() {
		Method method = getMethod0();
		if(method == null){
			Class cls = getClass0();
			if(cls != null){
				Class[] params = getParams();
				if(params == null){
					for(int i = 0; i < 3; i++){
						// Find methods for up to 2 params. We are guessing
						// here.
						// This block should never execute unless the
						// classloader
						// that loaded the argument classes disappears.
						method = BeanUtils.findAccessibleMethodIncludeInterfaces(cls, getName(), i, null);
						if(method != null){
							break;
						}
					}
				} else{
					method = BeanUtils.findAccessibleMethodIncludeInterfaces(cls, getName(), params.length, params);
				}
				setMethod(method);
			}
		}
		return method;
	}

	private synchronized void setMethod(Method method) {
		if(method == null){
			return;
		}
		if(getClass0() == null){
			setClass0(method.getDeclaringClass());
		}
		setParams(method.getParameterTypes());
		methodRef = createReference(method, true);
	}

	private Method getMethod0() {
		return (Method) getObject(methodRef);
	}

	private synchronized void setParams(Class[] param) {
		if(param == null){
			return;
		}
		paramNames = new String[param.length];
		params = new ArrayList(param.length);
		for(int i = 0; i < param.length; i++){
			paramNames[i] = param[i].getName();
			params.add(new WeakReference(param[i]));
		}
	}

	// pp getParamNames used as an optimization to avoid
	// method.getParameterTypes.
	String[] getParamNames() {
		return paramNames;
	}

	private synchronized Class[] getParams() {
		Class[] clss = new Class[params.size()];

		for(int i = 0; i < params.size(); i++){
			Reference ref = (Reference) params.get(i);
			Class cls = (Class) ref.get();
			if(cls == null){
				return null;
			} else{
				clss[i] = cls;
			}
		}
		return clss;
	}

	/**
	 * Gets the ParameterDescriptor for each of this MethodDescriptor's
	 * method's parameters.
	 * 
	 * @return The locale-independent names of the parameters. May return
	 *         a null array if the parameter names aren't known.
	 */
	public ParameterDescriptor[] getParameterDescriptors() {
		return parameterDescriptors;
	}

	/*
	 * public String toString() {
	 * String message = "name=" + getName();
	 * Class cls = getClass0();
	 * if (cls != null) {
	 * message += ", class=";
	 * message += cls.getName();
	 * }
	 * String[] names = getParamNames();
	 * if (names != null) {
	 * for (int i = 0; i < names.length; i++) {
	 * message += ", param=" + names[i];
	 * }
	 * }
	 * return message;
	 * }
	 */

	/*
	 * Package-private constructor
	 * Merge two method descriptors. Where they conflict, give the
	 * second argument (y) priority over the first argument (x).
	 * 
	 * @param x The first (lower priority) MethodDescriptor
	 * 
	 * @param y The second (higher priority) MethodDescriptor
	 */

	MethodDescriptor(MethodDescriptor x, MethodDescriptor y) {
		super(x, y);

		methodRef = x.methodRef;
		if(y.methodRef != null){
			methodRef = y.methodRef;
		}
		params = x.params;
		if(y.params != null){
			params = y.params;
		}
		paramNames = x.paramNames;
		if(y.paramNames != null){
			paramNames = y.paramNames;
		}

		parameterDescriptors = x.parameterDescriptors;
		if(y.parameterDescriptors != null){
			parameterDescriptors = y.parameterDescriptors;
		}
	}

	/*
	 * Package-private dup constructor
	 * This must isolate the new object from any changes to the old object.
	 */
	MethodDescriptor(MethodDescriptor old) {
		super(old);

		methodRef = old.methodRef;
		params = old.params;
		paramNames = old.paramNames;

		if(old.parameterDescriptors != null){
			int len = old.parameterDescriptors.length;
			parameterDescriptors = new ParameterDescriptor[len];
			for(int i = 0; i < len; i++){
				parameterDescriptors[i] = new ParameterDescriptor(old.parameterDescriptors[i]);
			}
		}
	}

}