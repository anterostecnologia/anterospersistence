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
package br.com.anteros.persistence.metadata.accessor.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.accessor.PropertyAccessor;
import br.com.anteros.persistence.metadata.accessor.PropertyAccessorFactory;

@SuppressWarnings("rawtypes")
public class PropertyAcessorFactoryImpl implements PropertyAccessorFactory {

	private final ClassPool pool;

	public PropertyAcessorFactoryImpl() {
		pool = new ClassPool();
		pool.appendSystemPath();
	}

	@Override
	public Map<String, PropertyAccessor> createAccessors(Class<?> clazz) throws Exception {
		Field[] fields = ReflectionUtils.getAllDeclaredFields(clazz);

		Map<String, PropertyAccessor> temp = new HashMap<String, PropertyAccessor>();
		for (Field field : fields) {
			PropertyAccessor accessor = createAccessor(clazz, field);
			temp.put(field.getName(), accessor);
		}

		return Collections.unmodifiableMap(temp);
	}

	@Override
	public PropertyAccessor createAccessor(Class<?> clazz, Field field) throws Exception {
		Method setterAccessor = ReflectionUtils.getSetterAccessor(clazz, field);
		Method getterAccessor = ReflectionUtils.getGetterAccessor(clazz, field);

		final String classTemplate = "%s_%s_accessor";
		final String getTemplate = "public Object get(Object source) { return ((%s)source).%s(); }";
		final String setTemplate = "public void set(Object dest, Object value) { return ((%s)dest).%s((%s) value); }";

		final String getMethod = String.format(getTemplate, clazz.getName(), getterAccessor.getName());
		String setMethod;
		if (field.getType().getName().equals("[B")) {
			setMethod = String.format(setTemplate, clazz.getName(), setterAccessor.getName(), "byte[]");
		} else if (field.getType() == Byte[].class) {
			setMethod = String.format(setTemplate, clazz.getName(), setterAccessor.getName(), "Byte[]");
		} else {
			setMethod = String
					.format(setTemplate, clazz.getName(), setterAccessor.getName(), field.getType().getName());
		}

		final String className = String.format(classTemplate, clazz.getName(), field.getName());

		pool.importPackage(clazz.getName());
		
		CtClass ctClass = pool.makeClass(className);
		ctClass.addMethod(CtNewMethod.make(getMethod, ctClass));
		ctClass.addMethod(CtNewMethod.make(setMethod, ctClass));
		ctClass.setInterfaces(new CtClass[] { pool.get(PropertyAccessor.class.getName()) });
		Class<?> generated = ctClass.toClass();
		return (PropertyAccessor) generated.newInstance();
	}

}
