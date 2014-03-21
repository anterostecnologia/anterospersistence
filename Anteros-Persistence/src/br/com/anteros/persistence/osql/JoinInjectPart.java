package br.com.anteros.persistence.osql;

import java.io.Serializable;

import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.impl.TemplateConditionImpl;

@SuppressWarnings("serial")
public class JoinInjectPart implements Serializable {

	private final Condition<?> injectPart;

	private final JoinInjectPosition position;

	public JoinInjectPart(String injectPart) {
		this(injectPart,JoinInjectPosition.BEFORE_TARGET);
	}

	public JoinInjectPart(String injectPart, JoinInjectPosition position) {
		this(TemplateConditionImpl.create(Object.class, injectPart), position);
	}

	public JoinInjectPart(Condition<?> injectPart) {
		this(injectPart, JoinInjectPosition.BEFORE_TARGET);
	}

	public JoinInjectPart(Condition<?> injectPart, JoinInjectPosition position) {
		this.injectPart = injectPart;
		this.position = position;
	}

	@Override
	public int hashCode() {
		return injectPart.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof JoinInjectPart) {
			return ((JoinInjectPart) obj).injectPart.equals(injectPart);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return injectPart.toString();
	}

	public Condition<?> getPart() {
		return injectPart;
	}

	public JoinInjectPosition getPosition() {
		return position;
	}

}
