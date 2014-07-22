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
package br.com.anteros.persistence.util;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ReflectionUtils {
	private static boolean loggedAccessibleWarning = false;
	private static boolean CACHE_METHODS = true;
	private static final Class<?>[] EMPTY_CLASS_PARAMETERS = new Class<?>[0];
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final Map<Object, Object> cache = Collections.synchronizedMap(new HashMap<Object, Object>());
	private static final Map<Class<?>, Field[]> cacheFields = Collections.synchronizedMap(new WeakHashMap<Class<?>, Field[]>());
	private static final Map<Class<?>, Class<?>[]> cacheInterfaces = Collections.synchronizedMap(new WeakHashMap<Class<?>, Class<?>[]>());
	private static final Map<Class<?>, Method[]> cacheMethods = Collections.synchronizedMap(new HashMap<Class<?>, Method[]>());

	public static boolean isProperty(Method m, Type boundType) {
		return ReflectionUtils.isPropertyType(boundType) && !m.isSynthetic() && !m.isBridge() && (!Modifier.isStatic(m.getModifiers()))
				&& m.getParameterTypes().length == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is"));
	}

	public static boolean isProperty(Field f, Type boundType) {
		return (!Modifier.isStatic(f.getModifiers())) && (!Modifier.isTransient(f.getModifiers())) && !f.isSynthetic()
				&& ReflectionUtils.isPropertyType(boundType);
	}

	private static boolean isPropertyType(Type type) {
		return !ReflectionUtils.isVoid(type);
	}

	public static boolean isVoid(Type type) {
		return void.class.equals(type);
	}

	public static Class<?> getConcreteImplementationFromCollection(Class<?> clazz) {
		if (clazz == List.class)
			return ArrayList.class;
		else if (clazz == Map.class)
			return HashMap.class;
		throw new RuntimeException("Não foi lozalizada uma implementação concreata para " + clazz.getName());
	}

	public static boolean isPublicStaticFinal(Field field) {
		int modifiers = field.getModifiers();
		return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
	}

	public static boolean isEqualsMethod(Method method) {
		if (method == null || !method.getName().equals("equals")) {
			return false;
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		return (paramTypes.length == 1 && paramTypes[0] == Object.class);
	}

	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
	}

	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
	}

	public static boolean isObjectMethod(Method method) {
		try {
			Object.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
			return true;
		} catch (SecurityException ex) {
			return false;
		} catch (NoSuchMethodException ex) {
			return false;
		}
	}

	public static boolean isImplementsInterface(Class<?> clazz, Class<?> interf) {
		if (clazz == interf)
			return true;
		Class<?>[] allInterfaces = getAllInterfaces(clazz);
		for (Class<?> c : allInterfaces) {
			if (c.equals(interf))
				return true;
		}
		return false;
	}

	public static boolean existsField(Class<?> clazz, String name) {
		Field[] fields = getAllDeclaredFields(clazz);
		for (Field field : fields) {
			if (name.equals(field.getName()))
				return true;

		}
		return false;
	}

	public static Object getObjectByFieldName(Object object, String name) throws Exception {
		Field field = ReflectionUtils.getFieldByName(object.getClass(), name);

		if (field != null) {
			return field.get(object);
		}
		return null;
	}

	public static void setObjectValueByFieldName(Object object, String name, Object value) throws Exception {
		ReflectionUtils.setObjectValueByField(object, ReflectionUtils.getFieldByName(object.getClass(), name), value);
	}

	public static void setObjectValueByField(Object object, Field field, Object value) throws Exception {
		if (field != null) {
			field.setAccessible(true);
			field.set(object, value);
		}
	}

	public static Field[] getAllDeclaredFields(Class<?> clazz) {
		Class<?> searchClazz = clazz;
		Field[] allFields = cacheFields.get(clazz);
		if (allFields == null) {
			List<Field> accum = new LinkedList<Field>();
			while ((searchClazz != null) && (searchClazz != Object.class)) {
				Field[] f = searchClazz.getDeclaredFields();
				for (int i = 0; i < f.length; i++) {
					accum.add(f[i]);
				}
				searchClazz = searchClazz.getSuperclass();
			}
			allFields = accum.toArray(new Field[accum.size()]);
			cacheFields.put(clazz, allFields);
		}
		return allFields;
	}

	public static Method[] getAllDeclaredMethods(Class<?> clazz) {
		Class<?> searchClazz = clazz;
		Method[] allMethods = cacheMethods.get(clazz);
		if (allMethods == null) {
			List<Method> accum = new LinkedList<Method>();
			while ((searchClazz != null) && (searchClazz != Object.class)) {
				Method[] f = searchClazz.getDeclaredMethods();
				for (int i = 0; i < f.length; i++) {
					accum.add(f[i]);
				}
				searchClazz = searchClazz.getSuperclass();
			}
			allMethods = accum.toArray(new Method[accum.size()]);
			cacheMethods.put(clazz, allMethods);
		}
		return allMethods;
	}

	public static Class<?>[] getAllInterfaces(Class<?> clazz) {
		Class<?> searchClazz = clazz;
		Class<?>[] allInterfaces = cacheInterfaces.get(clazz);
		if (allInterfaces == null) {
			List<Class<?>> accum = new LinkedList<Class<?>>();
			while ((searchClazz != null) && (searchClazz != Object.class)) {
				Class<?>[] f = searchClazz.getInterfaces();
				for (int i = 0; i < f.length; i++) {
					accum.add(f[i]);
				}
				searchClazz = searchClazz.getSuperclass();
			}
			allInterfaces = (Class[]) accum.toArray(new Class[accum.size()]);
			cacheInterfaces.put(clazz, allInterfaces);
		}
		return allInterfaces;
	}

	public static void makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier.isFinal(field
				.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	public static void makeAccessible(Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
			method.setAccessible(true);
		}
	}

	public static void makeAccessible(Constructor<?> ctor) {
		if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
			ctor.setAccessible(true);
		}
	}

	public static Field getFieldByName(Class<?> clazz, String name) {
		Field[] fields = getAllDeclaredFields(clazz);
		for (Field field : fields) {
			if (name.equals(field.getName())) {
				field.setAccessible(true);
				return field;
			}

		}
		return null;
	}

	public static Field getFieldByMethodAcessor(Class<?> clazz, Method method) {
		Field[] fields = getAllDeclaredFields(clazz);
		for (Field field : fields) {
			if (method.getName().equals("get" + StringUtils.capitalize(field.getName()))) {
				field.setAccessible(true);
				return field;
			}

		}
		return null;
	}

	public static Field getFieldByMethodAcessor(Class<?> clazz, String methodName) {
		Field[] fields = getAllDeclaredFields(clazz);
		for (Field field : fields) {
			if (methodName.equals("get" + StringUtils.capitalize(field.getName()))) {
				field.setAccessible(true);
				return field;
			}

		}
		return null;
	}

	public static Object getFieldValue(Object object, Field field) throws SecurityException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		/*field.setAccessible(true);
		String name = field.getName();
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		Class<?> type = field.getType();
		if (boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
			name = "is" + name;
		} else {
			name = "get" + name;
		}
		Object value = null;
		try {
			Method method = field.getDeclaringClass().getMethod(name);
			value = method.invoke(object);
		} catch (NoSuchMethodException e) {*/
			//value = field.get(object);
		//}
		return field.get(object);
	}

	public static Object getFieldValueByName(Object object, String name) throws SecurityException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Field field = ReflectionUtils.getFieldByName(object.getClass(), name);
		if (field != null)
			return ReflectionUtils.getFieldValue(object, field);
		return null;
	}

	public static Map<String, Method> getGetters(final Class<?> clazz) {
		final Map<String, Method> accessors = new HashMap<String, Method>();
		final Method[] methods = clazz.getMethods();

		for (int i = 0; i < methods.length; i++) {
			String name;
			String methodName;
			final Method method = methods[i];

			methodName = method.getName();
			if (!methodName.startsWith("get"))
				continue;
			if (method.getParameterTypes().length != 0)
				continue;

			name = methodName.substring("get".length()).toLowerCase();
			if (name.length() == 0)
				continue;
			accessors.put(name, method);
		}
		return accessors;
	}

	public static Map<String, Method> getSetters(Class<?> clazz) {
		final Map<String, Method> accessors = new HashMap<String, Method>();
		final Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			String name;
			String methodName;
			Method method = methods[i];

			methodName = method.getName();
			if (!methodName.startsWith("set"))
				continue;
			if (method.getParameterTypes().length != 1)
				continue;

			name = methodName.substring("set".length()).toLowerCase();
			accessors.put(name, method);
		}

		return accessors;
	}

	public static synchronized void setCacheMethods(boolean cacheMethods) {
		CACHE_METHODS = cacheMethods;
		if (!CACHE_METHODS) {
			clearCache();
		}
	}

	public static synchronized int clearCache() {
		int size = cache.size();
		cache.clear();
		return size;
	}

	public static Object invokeMethod(Object object, String methodName, Object arg) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {

		Object[] args = { arg };
		return invokeMethod(object, methodName, args);

	}

	public static Object invokeMethod(Object object, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {

		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		for (int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return invokeMethod(object, methodName, args, parameterTypes);

	}

	public static Object invokeMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		if (parameterTypes == null) {
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		}
		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}

		Method method = getMatchingAccessibleMethod(object.getClass(), methodName, parameterTypes);
		if (method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
		}
		return method.invoke(object, args);
	}

	public static Object invokeExactMethod(Object object, String methodName, Object arg) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {

		Object[] args = { arg };
		return invokeExactMethod(object, methodName, args);

	}

	public static Object invokeExactMethod(Object object, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		for (int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return invokeExactMethod(object, methodName, args, parameterTypes);

	}

	public static Object invokeExactMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}

		if (parameterTypes == null) {
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		}

		Method method = getAccessibleMethod(object.getClass(), methodName, parameterTypes);
		if (method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
		}
		return method.invoke(object, args);

	}

	public static Object invokeExactStaticMethod(Class<?> objectClass, String methodName, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}

		if (parameterTypes == null) {
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		}

		Method method = getAccessibleMethod(objectClass, methodName, parameterTypes);
		if (method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + objectClass.getName());
		}
		return method.invoke(null, args);

	}

	public static Object invokeStaticMethod(Class<?> objectClass, String methodName, Object arg) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		Object[] args = { arg };
		return invokeStaticMethod(objectClass, methodName, args);

	}

	public static Object invokeStaticMethod(Class<?> objectClass, String methodName, Object[] args) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		for (int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return invokeStaticMethod(objectClass, methodName, args, parameterTypes);

	}

	public static Object invokeStaticMethod(Class<?> objectClass, String methodName, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		if (parameterTypes == null) {
			parameterTypes = EMPTY_CLASS_PARAMETERS;
		}
		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}

		Method method = getMatchingAccessibleMethod(objectClass, methodName, parameterTypes);
		if (method == null) {
			throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + objectClass.getName());
		}
		return method.invoke(null, args);
	}

	public static Object invokeExactStaticMethod(Class<?> objectClass, String methodName, Object arg) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		Object[] args = { arg };
		return invokeExactStaticMethod(objectClass, methodName, args);

	}

	public static Object invokeExactStaticMethod(Class<?> objectClass, String methodName, Object[] args) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		if (args == null) {
			args = EMPTY_OBJECT_ARRAY;
		}
		int arguments = args.length;
		Class<?>[] parameterTypes = new Class[arguments];
		for (int i = 0; i < arguments; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return invokeExactStaticMethod(objectClass, methodName, args, parameterTypes);

	}

	public static Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?> parameterType) {
		Class<?>[] parameterTypes = { parameterType };
		return getAccessibleMethod(clazz, methodName, parameterTypes);
	}

	public static Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		try {
			MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, true);
			Method method = getCachedMethod(md);
			if (method != null) {
				return method;
			}

			method = getAccessibleMethod(clazz, clazz.getMethod(methodName, parameterTypes));
			cacheMethod(md, method);
			return method;
		} catch (NoSuchMethodException e) {
			return (null);
		}
	}

	public static Method getAccessibleMethod(Method method) {
		if (method == null) {
			return (null);
		}
		return getAccessibleMethod(method.getDeclaringClass(), method);
	}

	public static Method getAccessibleMethod(Class<?> clazz, Method method) {
		if (method == null) {
			return (null);
		}
		if (!Modifier.isPublic(method.getModifiers())) {
			return (null);
		}

		boolean sameClass = true;
		if (clazz == null) {
			clazz = method.getDeclaringClass();
		} else {
			sameClass = clazz.equals(method.getDeclaringClass());
			if (!method.getDeclaringClass().isAssignableFrom(clazz)) {
				throw new IllegalArgumentException(clazz.getName() + " is not assignable from " + method.getDeclaringClass().getName());
			}
		}
		if (Modifier.isPublic(clazz.getModifiers())) {
			if (!sameClass && !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
				setMethodAccessible(method);
			}
			return (method);
		}

		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		method = getAccessibleMethodFromInterfaceNest(clazz, methodName, parameterTypes);
		if (method == null) {
			method = getAccessibleMethodFromSuperclass(clazz, methodName, parameterTypes);
		}

		return (method);

	}

	private static Method getAccessibleMethodFromSuperclass(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {

		Class<?> parentClazz = clazz.getSuperclass();
		while (parentClazz != null) {
			if (Modifier.isPublic(parentClazz.getModifiers())) {
				try {
					return parentClazz.getMethod(methodName, parameterTypes);
				} catch (NoSuchMethodException e) {
					return null;
				}
			}
			parentClazz = parentClazz.getSuperclass();
		}
		return null;
	}

	private static Method getAccessibleMethodFromInterfaceNest(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {

		Method method = null;
		for (; clazz != null; clazz = clazz.getSuperclass()) {

			Class<?>[] interfaces = clazz.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				if (!Modifier.isPublic(interfaces[i].getModifiers())) {
					continue;
				}
				try {
					method = interfaces[i].getDeclaredMethod(methodName, parameterTypes);
				} catch (NoSuchMethodException e) {
				}
				if (method != null) {
					return method;
				}
				method = getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
				if (method != null) {
					return method;
				}
			}
		}
		return (null);
	}

	public static Method getMatchingAccessibleMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {

		MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, false);

		try {
			Method method = getCachedMethod(md);
			if (method != null) {
				return method;
			}
			method = clazz.getMethod(methodName, parameterTypes);
			setMethodAccessible(method);
			cacheMethod(md, method);
			return method;

		} catch (NoSuchMethodException e) {
		}

		int paramSize = parameterTypes.length;
		Method bestMatch = null;
		Method[] methods = clazz.getMethods();
		float bestMatchCost = Float.MAX_VALUE;
		float myCost = Float.MAX_VALUE;
		for (int i = 0, size = methods.length; i < size; i++) {
			if (methods[i].getName().equals(methodName)) {
				Class<?>[] methodsParams = methods[i].getParameterTypes();
				int methodParamSize = methodsParams.length;
				if (methodParamSize == paramSize) {
					boolean match = true;
					for (int n = 0; n < methodParamSize; n++) {
						if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
							match = false;
							break;
						}
					}

					if (match) {
						Method method = getAccessibleMethod(clazz, methods[i]);
						if (method != null) {
							setMethodAccessible(method);
							myCost = getTotalTransformationCost(parameterTypes, method.getParameterTypes());
							if (myCost < bestMatchCost) {
								bestMatch = method;
								bestMatchCost = myCost;
							}
						}
					}
				}
			}
		}
		if (bestMatch != null) {
			cacheMethod(md, bestMatch);
		} else {
		}

		return bestMatch;
	}

	private static void setMethodAccessible(Method method) {
		try {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
		} catch (SecurityException se) {
			if (!loggedAccessibleWarning) {
				try {
					String specVersion = System.getProperty("java.specification.version");
					if (specVersion.charAt(0) == '1'
							&& (specVersion.charAt(2) == '0' || specVersion.charAt(2) == '1' || specVersion.charAt(2) == '2' || specVersion.charAt(2) == '3')) {
					}
				} catch (SecurityException e) {
				}
				loggedAccessibleWarning = true;
			}
		}
	}

	private static float getTotalTransformationCost(Class<?>[] srcArgs, Class<?>[] destArgs) {

		float totalCost = 0.0f;
		for (int i = 0; i < srcArgs.length; i++) {
			Class<?> srcClass, destClass;
			srcClass = srcArgs[i];
			destClass = destArgs[i];
			totalCost += getObjectTransformationCost(srcClass, destClass);
		}

		return totalCost;
	}

	private static float getObjectTransformationCost(Class<?> srcClass, Class<?> destClass) {
		float cost = 0.0f;
		while (destClass != null && !destClass.equals(srcClass)) {
			if (destClass.isInterface() && isAssignmentCompatible(destClass, srcClass)) {
				cost += 0.25f;
				break;
			}
			cost++;
			destClass = destClass.getSuperclass();
		}

		if (destClass == null) {
			cost += 1.5f;
		}

		return cost;
	}

	public static final boolean isAssignmentCompatible(Class<?> parameterType, Class<?> parameterization) {
		if (parameterType.isAssignableFrom(parameterization)) {
			return true;
		}
		if (parameterType.isPrimitive()) {
			Class<?> parameterWrapperClazz = getPrimitiveWrapper(parameterType);
			if (parameterWrapperClazz != null) {
				return parameterWrapperClazz.equals(parameterization);
			}
		}

		return false;
	}

	public static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
		if (boolean.class.equals(primitiveType)) {
			return Boolean.class;
		} else if (float.class.equals(primitiveType)) {
			return Float.class;
		} else if (long.class.equals(primitiveType)) {
			return Long.class;
		} else if (int.class.equals(primitiveType)) {
			return Integer.class;
		} else if (short.class.equals(primitiveType)) {
			return Short.class;
		} else if (byte.class.equals(primitiveType)) {
			return Byte.class;
		} else if (double.class.equals(primitiveType)) {
			return Double.class;
		} else if (char.class.equals(primitiveType)) {
			return Character.class;
		} else {

			return null;
		}
	}

	public static Class<?> getPrimitiveType(Class<?> wrapperType) {
		if (Boolean.class.equals(wrapperType)) {
			return boolean.class;
		} else if (Float.class.equals(wrapperType)) {
			return float.class;
		} else if (Long.class.equals(wrapperType)) {
			return long.class;
		} else if (Integer.class.equals(wrapperType)) {
			return int.class;
		} else if (Short.class.equals(wrapperType)) {
			return short.class;
		} else if (Byte.class.equals(wrapperType)) {
			return byte.class;
		} else if (Double.class.equals(wrapperType)) {
			return double.class;
		} else if (Character.class.equals(wrapperType)) {
			return char.class;
		} else {
			return null;
		}
	}

	public static Class<?> toNonPrimitiveClass(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			Class<?> primitiveClazz = ReflectionUtils.getPrimitiveWrapper(clazz);
			if (primitiveClazz != null) {
				return primitiveClazz;
			} else {
				return clazz;
			}
		} else {
			return clazz;
		}
	}

	private static Method getCachedMethod(MethodDescriptor md) {
		if (CACHE_METHODS) {
			Reference<?> methodRef = (Reference<?>) cache.get(md);
			if (methodRef != null) {
				return (Method) methodRef.get();
			}
		}
		return null;
	}

	private static void cacheMethod(MethodDescriptor md, Method method) {
		if (CACHE_METHODS) {
			if (method != null)
				cache.put(md, new WeakReference<Method>(method));
		}
	}

	public static Class<?> getGenericType(Field field) {
		try {
			Type genericFieldType = field.getGenericType();
			ParameterizedType aType = (ParameterizedType) genericFieldType;
			return (Class<?>) aType.getActualTypeArguments()[0];
		} catch (Exception e) {
			return null;
		}
	}

	public static List<Class<?>> getGenericMapTypes(Field field) {
		List<Class<?>> result = null;
		try {
			Type genericFieldType = field.getGenericType();
			ParameterizedType aType = (ParameterizedType) genericFieldType;

			result = new ArrayList<Class<?>>();
			for (int i = 0, size = aType.getActualTypeArguments().length; i < size; i++) {
				result.add((Class<?>) aType.getActualTypeArguments()[i]);
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean containsInTypedMap(Field field, Class<?>[] classes) {
		Class<?> clazz = getGenericMapTypes(field).get(0);
		for (Class<?> c : classes)
			if (c == clazz)
				return true;
		return false;
	}

	public static boolean containsEnumInKeyTypedMap(Field field) {
		Type type = field.getGenericType();
		ParameterizedType pt = (ParameterizedType) type;
		if (pt.getActualTypeArguments().length > 0) {
			return ((Class<?>) pt.getActualTypeArguments()[0]).isEnum();
		}
		return false;
	}

	public static Class<?> getEnumKeyTypedMap(Field field) {
		Type type = field.getGenericType();
		ParameterizedType pt = (ParameterizedType) type;
		if (pt.getActualTypeArguments().length > 0) {
			return ((Class<?>) pt.getActualTypeArguments()[0]);
		}
		return null;
	}

	public static boolean containsInKeyTypedMap(Field field, Class<?>[] classes) {
		Type type = field.getGenericType();
		ParameterizedType pt = (ParameterizedType) type;
		if (pt.getActualTypeArguments().length > 0) {
			Class<?> clazz = (Class<?>) pt.getActualTypeArguments()[0];
			for (Class<?> c : classes)
				if (c == clazz)
					return true;
		}
		return false;
	}

	private static class MethodDescriptor {
		private Class<?> cls;
		private String methodName;
		private Class<?>[] paramTypes;
		private boolean exact;
		private int hashCode;

		public MethodDescriptor(Class<?> cls, String methodName, Class<?>[] paramTypes, boolean exact) {
			if (cls == null) {
				throw new IllegalArgumentException("Class cannot be null");
			}
			if (methodName == null) {
				throw new IllegalArgumentException("Method Name cannot be null");
			}
			if (paramTypes == null) {
				paramTypes = EMPTY_CLASS_PARAMETERS;
			}

			this.cls = cls;
			this.methodName = methodName;
			this.paramTypes = paramTypes;
			this.exact = exact;

			this.hashCode = methodName.length();
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof MethodDescriptor)) {
				return false;
			}
			MethodDescriptor md = (MethodDescriptor) obj;

			return (exact == md.exact && methodName.equals(md.methodName) && cls.equals(md.cls) && java.util.Arrays.equals(paramTypes, md.paramTypes));
		}

		public int hashCode() {
			return hashCode;
		}
	}

	public static boolean fieldIsNull(Object object, Field field) {
		try {
			field.setAccessible(true);
			return field.get(object) == null;
		} catch (Exception e) {
			return true;
		}

	}

	public static boolean isExtendsClass(Class<?> superClass, Class<?> childClass) {
		if (childClass.equals(superClass))
			return true;

		return superClass.isAssignableFrom(childClass);
	}

	public static boolean isAbstractClass(Class<?> clazz) {
		return (Modifier.isAbstract(clazz.getModifiers()));
	}

	public static boolean isCollection(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}

	public static boolean isImplementsMap(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}

	public static boolean isPublic(Class<?> clazz, Member member) {
		return Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(clazz.getModifiers());
	}

	public static Method[] getAllMethods(Class<?> objectClass) {
		List<Method> accum = new LinkedList<Method>();
		while (objectClass != Object.class) {
			Method[] f = objectClass.getDeclaredMethods();
			for (int i = 0; i < f.length; i++) {
				accum.add(f[i]);
			}
			objectClass = objectClass.getSuperclass();
		}
		Method[] allMethods = accum.toArray(new Method[accum.size()]);
		return allMethods;
	}

	public static Method findMethodObject(Class<?> objectClass, String methodName) {
		Method[] allMethods = getAllMethods(objectClass);

		for (Method method : allMethods) {
			if (method.getName().toUpperCase().equals(methodName.toUpperCase())) {
				return method;
			}
		}

		return null;
	}

	public static void invokeMethodWithParameterString(Object object, String methodName, String value) throws Exception {
		Method method = ReflectionUtils.findMethodObject(object.getClass(), methodName);
		if (method != null) {
			if (method.getParameterTypes().length > 0) {
				if (method.getParameterTypes()[0] == String.class) {
					method.invoke(object, value);
				} else if (method.getParameterTypes()[0] == Long.class) {
					method.invoke(object, new Long(value.trim()));
				} else if (method.getParameterTypes()[0] == Integer.class) {
					method.invoke(object, new Integer(value.trim()));
				} else if (method.getParameterTypes()[0] == Double.class) {
					method.invoke(object, new Double(value.trim()));
				} else if (method.getParameterTypes()[0] == BigInteger.class) {
					method.invoke(object, new BigInteger(value.trim()));
				} else if (method.getParameterTypes()[0] == BigDecimal.class) {
					method.invoke(object, new BigDecimal(value.trim()));
				} else if (method.getParameterTypes()[0] == Float.class) {
					method.invoke(object, new Float(value.trim()));
				} else if (method.getParameterTypes()[0] == long.class) {
					method.invoke(object, new Long(value.trim()).longValue());
				} else if (method.getParameterTypes()[0] == int.class) {
					method.invoke(object, new Integer(value.trim()).intValue());
				} else if (method.getParameterTypes()[0] == double.class) {
					method.invoke(object, new Double(value.trim()).doubleValue());
				} else if (method.getParameterTypes()[0] == float.class) {
					method.invoke(object, new Float(value.trim()).floatValue());
				}
			}
		}

	}

	public static int countNumberOfAnnotation(Class<?> sourceClass, Class<? extends Annotation> annotationClass) {
		Field[] fields = getAllDeclaredFields(sourceClass);
		int countAnnotation = 0;
		for (Field field : fields) {
			if (field.isAnnotationPresent(annotationClass))
				countAnnotation++;
		}
		return countAnnotation;
	}

	public static boolean isEnum(Class<?> clazz) {
		return clazz.isEnum();
	}

	public static Field[] getFieldsObjectReflection(Class<?> objectClass) {
		List<Field> accum = new LinkedList<Field>();
		while (objectClass != Object.class) {
			Field[] f = objectClass.getDeclaredFields();
			for (int i = 0; i < f.length; i++) {
				accum.add(f[i]);
			}
			objectClass = objectClass.getSuperclass();
		}
		Field[] allFields = (Field[]) accum.toArray(new Field[accum.size()]);
		return allFields;
	}
	
	private static final AnnotatedElement EMPTY = new Annotations();
	
	public static AnnotatedElement getAnnotatedElement(Class<?> beanClass, String propertyName, Class<?> propertyClass) {
        Field field = getFieldOrNull(beanClass, propertyName);
        Method method = getGetterOrNull(beanClass, propertyName, propertyClass);
        if (field == null || field.getAnnotations().length == 0) {
            return (method != null && method.getAnnotations().length > 0) ? method : EMPTY;
        } else if (method == null || method.getAnnotations().length == 0) {
            return field;
        } else {
            return new Annotations(field, method);
        }
    }
	
	public static Field getFieldOrNull(Class<?> beanClass, String propertyName) {
        while (beanClass != null && !beanClass.equals(Object.class)) {
            try {
                return beanClass.getDeclaredField(propertyName);
            } catch (SecurityException e) {
            } catch (NoSuchFieldException e) {
            }
            beanClass = beanClass.getSuperclass();
        }
        return null;
    }

    public static Method getGetterOrNull(Class<?> beanClass, String name) {
        Method method = getGetterOrNull(beanClass, name, Object.class);
        if (method != null) {
            return method;
        } else {
            return getGetterOrNull(beanClass, name, Boolean.class);
        }
    }
    
    public static Method getGetterOrNull(Class<?> beanClass, String name, Class<?> type) {
        String methodName = ((type.equals(Boolean.class) || type.equals(boolean.class)) ? "is" : "get") + StringUtils.capitalize(name);
        while (beanClass != null && !beanClass.equals(Object.class)) {
            try {
                return beanClass.getDeclaredMethod(methodName);
            } catch (SecurityException e) { 
            } catch (NoSuchMethodException e) { 
            }
            beanClass = beanClass.getSuperclass();
        }
        return null;

    }

}