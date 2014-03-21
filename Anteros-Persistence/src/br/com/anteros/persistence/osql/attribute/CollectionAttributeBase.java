package br.com.anteros.persistence.osql.attribute;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import br.com.anteros.persistence.osql.ConditionException;
import br.com.anteros.persistence.osql.condition.AbstractCollectionCondition;
import br.com.anteros.persistence.osql.condition.SimpleCondition;
import br.com.anteros.persistence.osql.impl.AttributeImpl;

@SuppressWarnings("serial")
public abstract class CollectionAttributeBase<C extends Collection<E>, E, Q extends SimpleCondition<? super E>> extends
		AbstractCollectionCondition<C, E> implements Attribute<C> {

	private transient volatile Constructor<?> constructor;

	private volatile boolean usePathInits = false;

	private final AttributeInits inits;

	public CollectionAttributeBase(AttributeImpl<C> mixin, AttributeInits inits) {
		super(mixin);
		this.inits = inits;
	}

	public abstract Q any();

	@SuppressWarnings("unchecked")
	protected Q newInstance(Class<Q> queryType, AttributeDescriptor<?> pm) {
		try {
			if (constructor == null) {
				if (Constants.isTyped(queryType)) {
					try {
						constructor = queryType.getConstructor(Class.class, AttributeDescriptor.class,
								AttributeInits.class);
						usePathInits = true;
					} catch (NoSuchMethodException e) {
						constructor = queryType.getConstructor(Class.class, AttributeDescriptor.class);
					}
				} else {
					try {
						constructor = queryType.getConstructor(AttributeDescriptor.class, AttributeInits.class);
						usePathInits = true;
					} catch (NoSuchMethodException e) {
						constructor = queryType.getConstructor(AttributeDescriptor.class);
					}
				}
			}
			if (Constants.isTyped(queryType)) {
				if (usePathInits) {
					return (Q) constructor.newInstance(getElementType(), pm, inits);
				} else {
					return (Q) constructor.newInstance(getElementType(), pm);
				}

			} else {
				if (usePathInits) {
					return (Q) constructor.newInstance(pm, inits);
				} else {
					return (Q) constructor.newInstance(pm);
				}
			}
		} catch (NoSuchMethodException e) {
			throw new ConditionException(e);
		} catch (InstantiationException e) {
			throw new ConditionException(e);
		} catch (IllegalAccessException e) {
			throw new ConditionException(e);
		} catch (InvocationTargetException e) {
			throw new ConditionException(e);
		}

	}

}
