package br.com.anteros.persistence.osql.condition;

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.SimpleOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.StringOperation;

public final class CaseForEqBuilder<D> {

	private static class CaseElement<D> {

		private final Condition<? extends D> eq;

		private final Condition<?> target;

		public CaseElement(Condition<? extends D> eq, Condition<?> target) {
			this.eq = eq;
			this.target = target;
		}

		public Condition<? extends D> getEq() {
			return eq;
		}

		public Condition<?> getTarget() {
			return target;
		}

	}

	private final Condition<D> base;

	private final Condition<? extends D> other;

	private final List<CaseElement<D>> caseElements = new ArrayList<CaseElement<D>>();

	private Class<?> type;

	public CaseForEqBuilder(Condition<D> base, Condition<? extends D> other) {
		this.base = base;
		this.other = other;
	}

	public <T> Cases<T, Condition<T>> then(Condition<T> then) {
		type = then.getType();
		return new Cases<T, Condition<T>>() {
			@Override
			protected Condition<T> createResult(Class<T> type, Condition<T> last) {
				return SimpleOperation.create(type, Operators.CASE_EQ, base, last);
			}
		}.when(other).then(then);
	}

	public <T> Cases<T, Condition<T>> then(T then) {
		return then(ConstantImpl.create(then));
	}

	public <T> Cases<T, Condition<T>> thenNull() {
		return then((Condition<T>) NullCondition.DEFAULT);
	}

	public <T extends Number & Comparable<?>> Cases<T, NumberCondition<T>> then(T then) {
		return thenNumber(ConstantImpl.create(then));
	}

	public <T extends Number & Comparable<?>> Cases<T, NumberCondition<T>> then(NumberCondition<T> then) {
		return thenNumber(then);
	}

	public <T extends Number & Comparable<?>> Cases<T, NumberCondition<T>> thenNumber(Condition<T> then) {
		type = then.getType();
		return new Cases<T, NumberCondition<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			protected NumberCondition<T> createResult(Class<T> type, Condition<T> last) {
				return NumberOperation.create(type, Operators.CASE_EQ, base, last);
			}

		}.when(other).then(then);
	}

	public Cases<String, StringCondition> then(StringCondition then) {
		return thenString(then);
	}

	public Cases<String, StringCondition> then(String then) {
		return thenString(ConstantImpl.create(then));
	}

	private Cases<String, StringCondition> thenString(Condition<String> then) {
		type = then.getType();
		return new Cases<String, StringCondition>() {
			@SuppressWarnings("unchecked")
			@Override
			protected StringCondition createResult(Class<String> type, Condition<String> last) {
				return StringOperation.create(Operators.CASE_EQ, base, last);
			}

		}.when(other).then(then);
	}

	public abstract class Cases<T, Q extends Condition<T>> {

		public CaseWhen<T, Q> when(Condition<? extends D> when) {
			return new CaseWhen<T, Q>(this, when);
		}

		public CaseWhen<T, Q> when(D when) {
			return when(ConstantImpl.create(when));
		}

		@SuppressWarnings("unchecked")
		public Q otherwise(Condition<T> otherwise) {
			caseElements.add(0, new CaseElement<D>(null, otherwise));
			Condition<T> last = null;
			for (CaseElement<D> element : caseElements) {
				if (last == null) {
					last = SimpleOperation.create((Class<T>) type, Operators.CASE_EQ_ELSE, element.getTarget());
				} else {
					last = SimpleOperation.create((Class<T>) type, Operators.CASE_EQ_WHEN, base, element.getEq(),
							element.getTarget(), last);
				}
			}
			return createResult((Class<T>) type, last);
		}

		protected abstract Q createResult(Class<T> type, Condition<T> last);

		public Q otherwise(T otherwise) {
			return otherwise(ConstantImpl.create(otherwise));
		}
	}

	public class CaseWhen<T, Q extends Condition<T>> {

		private final Cases<T, Q> cases;

		private final Condition<? extends D> when;

		public CaseWhen(Cases<T, Q> cases, Condition<? extends D> when) {
			this.cases = cases;
			this.when = when;
		}

		public Cases<T, Q> then(Condition<T> then) {
			caseElements.add(0, new CaseElement<D>(when, then));
			return cases;
		}

		public Cases<T, Q> then(T then) {
			return then(ConstantImpl.create(then));
		}

	}

}
