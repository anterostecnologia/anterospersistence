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
package br.com.anteros.persistence.proxy.lob;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.sql.NClob;
import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.proxy.AnterosProxyLob;
import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.lob.AnterosNClob;

public class NClobLazyLoadProxy implements InvocationHandler {

	private static final Class<?>[] PROXY_INTERFACES = new Class[] { Clob.class, AnterosProxyObject.class,
			AnterosProxyLob.class };
	private EntityCache entityCache;
	private SQLSession session;
	private Boolean initialized = Boolean.FALSE;
	private DescriptionField descriptionFieldOwner;
	private AnterosNClob target;
	private Object owner;
	private Map<String, Object> columKeyValues;

	public NClobLazyLoadProxy(SQLSession session, Object owner, EntityCache entityCache,
			Map<String, Object> columKeyValues, DescriptionField descriptionFieldOwner) {
		this.entityCache = entityCache;
		this.session = session;
		this.descriptionFieldOwner = descriptionFieldOwner;
		this.owner = owner;
		this.columKeyValues = columKeyValues;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("isInitialized".equals(method.getName())) {
			return isInitialized();
		}

		if ("initialize".equals(method.getName())) {
			initializeNClob();
			return null;
		}

		if ("initializeAndReturnObject".equals(method.getName())) {
			initializeNClob();
			return target;
		}

		if (!initialized)
			initializeNClob();
		return method.invoke(target, args);
	}

	private void initializeNClob() throws Exception {
		target = (AnterosNClob) session.createQuery("").loadData(entityCache, owner, descriptionFieldOwner,
				columKeyValues, null);

		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(owner);
		/*
		 * Caso o objeto possa ser gerenciado(objeto completo ou parcial que
		 * tenha sido buscado id no sql) adiciona o objeto no cache
		 */
		if (entityManaged != null) {
			if (entityManaged.getStatus() != EntityStatus.READ_ONLY) {
				/*
				 * Guarda o valor da chave do objeto result na lista de
				 * oldValues
				 */
				FieldEntityValue value = descriptionFieldOwner.getFieldEntityValue(session, owner, target);
				entityManaged.addOriginalValue(value);
				entityManaged.addLastValue(value);
				/*
				 * Adiciona o campo na lista de campos que poderão ser
				 * alterados. Se o campo não for buscado no select não poderá
				 * ser alterado.
				 */
				entityManaged.getFieldsForUpdate().add(descriptionFieldOwner.getField().getName());
			}
		}

		initialized = true;
	}

	public static NClob createProxy(SQLSession session, Object owner, EntityCache entityCache,
			Map<String, Object> columKeyValues, DescriptionField descriptionField) {
		return (NClob) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), PROXY_INTERFACES,
				new NClobLazyLoadProxy(session, owner, entityCache, columKeyValues, descriptionField));
	}

	public static boolean isLobProxy(Object object) {
		return Proxy.isProxyClass(object.getClass());
	}

	public static InvocationHandler getInvocationHandler(Object object) {
		return Proxy.getInvocationHandler(object);
	}

	public Boolean isInitialized() {
		return initialized;
	}

}