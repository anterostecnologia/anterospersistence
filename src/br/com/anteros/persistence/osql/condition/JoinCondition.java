package br.com.anteros.persistence.osql.condition;

import java.io.Serializable;
import java.util.Set;

import br.com.anteros.persistence.osql.JoinInjectPart;
import br.com.anteros.persistence.osql.JoinType;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public final class JoinCondition implements Serializable {

	private final Predicate condition;

	private final ImmutableSet<JoinInjectPart> flags;

	private final Condition<?> target;

	private final JoinType type;

	public JoinCondition(JoinType type, Condition<?> target) {
		this(type, target, null, ImmutableSet.<JoinInjectPart> of());
	}

	public JoinCondition(JoinType type, Condition<?> target, Predicate condition, Set<JoinInjectPart> flags) {
		this.type = type;
		this.target = target;
		this.condition = condition;
		this.flags = ImmutableSet.copyOf(flags);
	}

	public Predicate getCondition() {
		return condition;
	}

	public Condition<?> getTarget() {
		return target;
	}

	public JoinType getType() {
		return type;
	}

	public boolean hasFlag(JoinInjectPart flag) {
		return flags.contains(flag);
	}

	public Set<JoinInjectPart> getFlags() {
		return flags;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(type).append(" ").append(target);
		if (condition != null) {
			builder.append(" on ").append(condition);
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(condition, target, type);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof JoinCondition) {
			JoinCondition j = (JoinCondition) o;
			return Objects.equal(condition, j.condition) && Objects.equal(target, j.target)
					&& Objects.equal(type, j.type);
		} else {
			return false;
		}
	}

}
