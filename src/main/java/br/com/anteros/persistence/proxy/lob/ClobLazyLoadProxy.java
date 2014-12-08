package br.com.anteros.persistence.proxy.lob;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.proxy.AnterosProxyLob;
import br.com.anteros.persistence.proxy.AnterosProxyObject;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.lob.AnterosClob;

public class ClobLazyLoadProxy implements InvocationHandler {

	private static final Class<?>[] PROXY_INTERFACES = new Class[] { Clob.class, AnterosProxyObject.class,
			AnterosProxyLob.class };
	private EntityCache entityCache;
	private SQLSession session;
	private Boolean initialized = Boolean.FALSE;
	private DescriptionField descriptionFieldOwner;
	private AnterosClob target;
	private Object owner;
	private Map<String, Object> columKeyValues;

	public ClobLazyLoadProxy(SQLSession session, Object owner, EntityCache entityCache,
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
			initializeClob();
			return null;
		}

		if ("initializeAndReturnObject".equals(method.getName())) {
			initializeClob();
			return target;
		}
		if (!initialized)
			initializeClob();
		return method.invoke(target, args);
	}

	private void initializeClob() throws Exception {
		target = (AnterosClob) session.createQuery("").loadData(entityCache, owner, descriptionFieldOwner,
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

	public static Clob createProxy(SQLSession session, Object owner, EntityCache entityCache,
			Map<String, Object> columKeyValues, DescriptionField descriptionFieldOwner) {
		return (Clob) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), PROXY_INTERFACES,
				new ClobLazyLoadProxy(session, owner, entityCache, columKeyValues, descriptionFieldOwner));
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
