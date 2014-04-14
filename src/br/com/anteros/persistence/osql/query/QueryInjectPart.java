package br.com.anteros.persistence.osql.query;

import java.io.Serializable;

import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.impl.TemplateConditionImpl;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class QueryInjectPart implements Serializable {
	private final QueryInjectPosition position;

	private final Condition<?> injectPart;

	public QueryInjectPart(QueryInjectPosition position, String flag) {
		this(position, TemplateConditionImpl.create(Object.class, flag));
	}

	public QueryInjectPart(QueryInjectPosition position, Condition<?> injectPart) {
		this.position = position;
		this.injectPart = injectPart;
	}

	public QueryInjectPosition getPosition() {
		return position;
	}

	public Condition<?> getPart() {
		return injectPart;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(position, injectPart);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof QueryInjectPart) {
			QueryInjectPart other = (QueryInjectPart) obj;
			return other.position.equals(position) && other.injectPart.equals(injectPart);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return position + " : " + injectPart;
	}
}
