package br.com.anteros.persistence.osql.condition;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public final class CodeTemplate implements Serializable {

	public abstract static class Element implements Serializable {

		public abstract Object convert(List<?> arguments);
		public abstract boolean isString();

	}

	public static final class AsString extends Element {

		private final int index;

		private final String toString;

		public AsString(int index) {
			this.index = index;
			this.toString = index + "s";
		}

		@Override
		public Object convert(final List<?> arguments) {
			final Object argument = arguments.get(index);
			return argument instanceof ConstantCondition ? argument.toString() : argument;
		}

		@Override
		public boolean isString() {
			return true;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	public static final class StaticText extends Element {

		private final String text;

		private final String toString;

		public StaticText(String text) {
			this.text = text;
			this.toString = "'" + text + "'";
		}

		@Override
		public boolean isString() {
			return true;
		}

		@Override
		public Object convert(List<?> arguments) {
			return text;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	public static final class Transformed extends Element {

		private final int index;

		private final transient Function<Object, Object> transformer;

		private final String toString;

		public Transformed(int index, Function<Object, Object> transformer) {
			this.index = index;
			this.transformer = transformer;
			this.toString = String.valueOf(index);
		}

		@Override
		public Object convert(final List<?> arguments) {
			return transformer.apply(arguments.get(index));
		}

		@Override
		public boolean isString() {
			return false;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	public static final class ByIndex extends Element {

		private final int index;

		private final String toString;

		public ByIndex(int index) {
			this.index = index;
			this.toString = String.valueOf(index);
		}

		@Override
		public Object convert(final List<?> arguments) {
			final Object argument = arguments.get(index);
			if (argument instanceof Condition) {
				return ConditionUtils.extract((Condition<?>) argument);
			} else {
				return argument;
			}
		}

		public int getIndex() {
			return index;
		}

		@Override
		public boolean isString() {
			return false;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	private final ImmutableList<Element> elements;

	private final String template;

	CodeTemplate(String template, ImmutableList<Element> elements) {
		this.template = template;
		this.elements = elements;
	}

	public List<Element> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return template;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof CodeTemplate) {
			return ((CodeTemplate) o).template.equals(template);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return template.hashCode();
	}

}
