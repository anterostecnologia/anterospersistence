package br.com.anteros.persistence.osql.condition;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.SimpleOperation;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.DateOperation;
import br.com.anteros.persistence.osql.operation.DateTimeOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.StringOperation;
import br.com.anteros.persistence.osql.operation.TimeOperation;

public final class CaseBuilder {

	private static class CaseElement<A> {

		private final BooleanCondition condition;

		private final Condition<A> target;

		public CaseElement(BooleanCondition condition, Condition<A> target) {
			this.condition = condition;
			this.target = target;
		}

		public BooleanCondition getCondition() {
			return condition;
		}

		public Condition<A> getTarget() {
			return target;
		}

	}

	public abstract static class Cases<A, Q extends Condition<A>> {

		private final List<CaseElement<A>> cases = new ArrayList<CaseElement<A>>();

		private final Class<A> type;

		public Cases(Class<A> type) {
			this.type = type;
		}

		Cases<A, Q> addCase(BooleanCondition condition, Condition<A> expr) {
			cases.add(0, new CaseElement<A>(condition, expr));
			return this;
		}

		protected abstract Q createResult(Class<A> type, Condition<A> last);

		public Q otherwise(A constant) {
			if (constant != null) {
				return otherwise(ConstantImpl.create(constant));
			} else {
				return otherwise((Condition) NullCondition.DEFAULT);
			}
		}

		public Q otherwise(Condition<A> expr) {
			if (expr == null) {
				expr = (Condition) NullCondition.DEFAULT;
			}
			cases.add(0, new CaseElement<A>(null, expr));
			Condition<A> last = null;
			for (CaseElement<A> element : cases) {
				if (last == null) {
					last = SimpleOperation.create(type, Operators.CASE_ELSE, element.getTarget());
				} else {
					last = SimpleOperation.create(type, Operators.CASE_WHEN, element.getCondition(),
							element.getTarget(), last);
				}
			}
			return createResult(type, last);
		}

		public CaseWhen<A, Q> when(BooleanCondition b) {
			return new CaseWhen<A, Q>(this, b);
		}

	}

	public static class CaseWhen<A, Q extends Condition<A>> {

		private final BooleanCondition b;

		private final Cases<A, Q> cases;

		public CaseWhen(Cases<A, Q> cases, BooleanCondition b) {
			this.cases = cases;
			this.b = b;
		}

		public Cases<A, Q> then(A constant) {
			return then(ConstantImpl.create(constant));
		}

		public Cases<A, Q> then(Condition<A> expr) {
			return cases.addCase(b, expr);
		}
	}

	public static class Initial {

		private final BooleanCondition when;

		public Initial(BooleanCondition b) {
			this.when = b;
		}

		@SuppressWarnings("unchecked")
		public <A> Cases<A, SimpleCondition<A>> then(Condition<A> expr) {
			return new Cases<A, SimpleCondition<A>>((Class) expr.getType()) {
				@Override
				protected SimpleCondition<A> createResult(Class<A> type, Condition<A> last) {
					return SimpleOperation.create(type, Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public <A> Cases<A, SimpleCondition<A>> then(A constant) {
			return then(ConstantImpl.create(constant));
		}

		public Cases<Boolean, BooleanCondition> then(BooleanCondition expr) {
			return thenBoolean(expr);
		}

		private Cases<Boolean, BooleanCondition> thenBoolean(Condition<Boolean> expr) {
			return new Cases<Boolean, BooleanCondition>(Boolean.class) {
				@Override
				protected BooleanCondition createResult(Class<Boolean> type, Condition<Boolean> last) {
					return BooleanOperation.create(Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public Cases<Boolean, BooleanCondition> then(boolean b) {
			return thenBoolean(ConstantImpl.create(b));
		}

		public <T extends Comparable> Cases<T, DateCondition<T>> then(DateCondition<T> expr) {
			return thenDate(expr);
		}

		private <T extends Comparable> Cases<T, DateCondition<T>> thenDate(Condition<T> expr) {
			return new Cases<T, DateCondition<T>>((Class) expr.getType()) {
				@Override
				protected DateCondition<T> createResult(Class<T> type, Condition<T> last) {
					return DateOperation.create(type, Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public Cases<java.sql.Date, DateCondition<java.sql.Date>> thenDate(java.sql.Date date) {
			return thenDate(ConstantImpl.create(date));
		}

		public <T extends Comparable> Cases<T, DateTimeCondition<T>> then(DateTimeCondition<T> expr) {
			return thenDateTime(expr);
		}

		private <T extends Comparable> Cases<T, DateTimeCondition<T>> thenDateTime(Condition<T> expr) {
			return new Cases<T, DateTimeCondition<T>>((Class) expr.getType()) {
				@Override
				protected DateTimeCondition<T> createResult(Class<T> type, Condition<T> last) {
					return DateTimeOperation.create(type, Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public Cases<Timestamp, DateTimeCondition<Timestamp>> thenDateTime(Timestamp ts) {
			return thenDateTime(ConstantImpl.create(ts));
		}

		public Cases<java.util.Date, DateTimeCondition<java.util.Date>> thenDateTime(java.util.Date date) {
			return thenDateTime(ConstantImpl.create(date));
		}

		public <A extends Number & Comparable<?>> Cases<A, NumberCondition<A>> then(NumberCondition<A> expr) {
			return thenNumber(expr);
		}

		@SuppressWarnings("unchecked")
		private <A extends Number & Comparable<?>> Cases<A, NumberCondition<A>> thenNumber(Condition<A> expr) {
			return new Cases<A, NumberCondition<A>>((Class) expr.getType()) {
				@Override
				protected NumberCondition<A> createResult(Class<A> type, Condition<A> last) {
					return NumberOperation.create(type, Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public <A extends Number & Comparable<?>> Cases<A, NumberCondition<A>> then(A num) {
			return thenNumber(ConstantImpl.create(num));
		}

		public Cases<String, StringCondition> then(StringCondition expr) {
			return thenString(expr);
		}

		private Cases<String, StringCondition> thenString(Condition<String> expr) {
			return new Cases<String, StringCondition>(String.class) {
				@Override
				protected StringCondition createResult(Class<String> type, Condition<String> last) {
					return StringOperation.create(Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public Cases<String, StringCondition> then(String str) {
			return thenString(ConstantImpl.create(str));
		}

		public <T extends Comparable> Cases<T, TimeCondition<T>> then(TimeCondition<T> expr) {
			return thenTime(expr);
		}

		private <T extends Comparable> Cases<T, TimeCondition<T>> thenTime(Condition<T> expr) {
			return new Cases<T, TimeCondition<T>>((Class) expr.getType()) {
				@Override
				protected TimeCondition<T> createResult(Class<T> type, Condition<T> last) {
					return TimeOperation.create(type, Operators.CASE, last);
				}

			}.addCase(when, expr);
		}

		public Cases<Time, TimeCondition<Time>> then(Time time) {
			return thenTime(ConstantImpl.create(time));
		}

	}

	public Initial when(BooleanCondition b) {
		return new Initial(b);
	}

}
