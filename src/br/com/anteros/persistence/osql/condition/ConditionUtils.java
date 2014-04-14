package br.com.anteros.persistence.osql.condition;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.impl.OperationImpl;
import br.com.anteros.persistence.osql.operation.Operation;
import br.com.anteros.persistence.osql.operation.PredicateOperation;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("unchecked")
public final class ConditionUtils {

	public static <T> Condition<T> all(CollectionCondition<?, ? super T> col) {
		return OperationImpl.create((Class<T>) col.getParameter(0), Operators.ALL, col);
	}

	public static <T> Condition<T> any(CollectionCondition<?, ? super T> col) {
		return OperationImpl.create((Class<T>) col.getParameter(0), Operators.ANY, col);
	}

	public static Predicate allOf(Collection<Predicate> exprs) {
		Predicate rv = null;
		for (Predicate b : exprs) {
			if (b != null)
				rv = rv == null ? b : ConditionUtils.and(rv, b);
		}
		return rv;
	}

	public static Predicate allOf(Predicate... exprs) {
		Predicate rv = null;
		for (Predicate b : exprs) {
			if (b != null)
				rv = rv == null ? b : ConditionUtils.and(rv, b);
		}
		return rv;
	}

	public static Predicate and(Predicate left, Predicate right) {
		left = (Predicate) extract(left);
		right = (Predicate) extract(right);
		if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else {
			return PredicateOperation.create(Operators.AND, left, right);
		}
	}

	public static Predicate anyOf(Collection<Predicate> exprs) {
		Predicate rv = null;
		for (Predicate b : exprs) {
			if (b != null) {
				rv = rv == null ? b : ConditionUtils.or(rv, b);
			}
		}
		return rv;
	}

	public static Predicate anyOf(Predicate... exprs) {
		Predicate rv = null;
		for (Predicate b : exprs) {
			if (b != null) {
				rv = rv == null ? b : ConditionUtils.or(rv, b);
			}
		}
		return rv;
	}

	public static <D> Condition<D> as(Condition<D> source, Attribute<D> alias) {
		return OperationImpl.create(alias.getType(), Operators.ALIAS, source, alias);
	}

	public static <D> Condition<D> as(Condition<D> source, String alias) {
		return as(source, new AttributeImpl<D>(source.getType(), alias));
	}

	public static Condition<Long> count(Condition<?> source) {
		return OperationImpl.create(Long.class, Operators.COUNT_AGG, source);
	}

	public static <D> Predicate eqConst(Condition<D> left, D constant) {
		return eq(left, ConstantImpl.create(constant));
	}

	public static <D> Predicate eq(Condition<D> left, Condition<? extends D> right) {
		return PredicateOperation.create(Operators.EQ, left, right);
	}

	public static <D> Predicate in(Condition<D> left, CollectionCondition<?, ? extends D> right) {
		return PredicateOperation.create(Operators.IN, left, right);
	}

	public static <D> Predicate in(Condition<D> left, Collection<? extends D> right) {
		if (right.size() == 1) {
			return eqConst(left, right.iterator().next());
		} else {
			return PredicateOperation.create(Operators.IN, left, ConstantImpl.create(right));
		}
	}

	public static Predicate isNull(Condition<?> left) {
		return PredicateOperation.create(Operators.IS_NULL, left);
	}

	public static Predicate isNotNull(Condition<?> left) {
		return PredicateOperation.create(Operators.IS_NOT_NULL, left);
	}

	public static Condition<String> likeToRegex(Condition<String> expr) {
		return likeToRegex(expr, true);
	}

	public static Condition<String> likeToRegex(Condition<String> expr, boolean matchStartAndEnd) {
		if (expr instanceof ConstantCondition<?>) {
			final String like = expr.toString();
			final StringBuilder rv = new StringBuilder(like.length() + 4);
			if (matchStartAndEnd && !like.startsWith("%")) {
				rv.append('^');
			}
			for (int i = 0; i < like.length(); i++) {
				char ch = like.charAt(i);
				if (ch == '.' || ch == '*' || ch == '?') {
					rv.append('\\');
				} else if (ch == '%') {
					rv.append(".*");
					continue;
				} else if (ch == '_') {
					rv.append('.');
					continue;
				}
				rv.append(ch);
			}
			if (matchStartAndEnd && !like.endsWith("%")) {
				rv.append('$');
			}
			if (!like.equals(rv.toString())) {
				return ConstantImpl.create(rv.toString());
			}
		} else if (expr instanceof Operation<?>) {
			Operation<?> o = (Operation<?>) expr;
			if (o.getOperator() == Operators.CONCAT) {
				Condition<String> lhs = likeToRegex((Condition<String>) o.getArgument(0), false);
				Condition<String> rhs = likeToRegex((Condition<String>) o.getArgument(1), false);
				if (lhs != o.getArgument(0) || rhs != o.getArgument(1)) {
					return OperationImpl.create(String.class, Operators.CONCAT, lhs, rhs);
				}
			}
		}
		return expr;
	}

	public static <T> Condition<T> list(Class<T> clazz, Condition<?>... exprs) {
		return list(clazz, ImmutableList.copyOf(exprs));
	}

	public static <T> Condition<T> list(Class<T> clazz, List<? extends Condition<?>> exprs) {
		Condition<T> rv = (Condition<T>) exprs.get(0);
		if (exprs.size() == 1) {
			rv = OperationImpl.create(clazz, Operators.SINGLETON, rv, exprs.get(0));
		} else {
			for (int i = 1; i < exprs.size(); i++) {
				rv = OperationImpl.create(clazz, Operators.LIST, rv, exprs.get(i));
			}
		}

		return rv;
	}

	public static Condition<String> regexToLike(Condition<String> expr) throws Exception {
		if (expr instanceof ConstantCondition<?>) {
			final String str = expr.toString();
			final StringBuilder rv = new StringBuilder(str.length() + 2);
			boolean escape = false;
			for (int i = 0; i < str.length(); i++) {
				final char ch = str.charAt(i);
				if (!escape && ch == '.') {
					if (i < str.length() - 1 && str.charAt(i + 1) == '*') {
						rv.append('%');
						i++;
					} else {
						rv.append('_');
					}
					continue;
				} else if (!escape && ch == '\\') {
					escape = true;
					continue;
				} else if (!escape && (ch == '[' || ch == ']' || ch == '^' || ch == '.' || ch == '*')) {
					throw new Exception("'" + str + "' can't be converted to like form");
				} else if (escape && (ch == 'd' || ch == 'D' || ch == 's' || ch == 'S' || ch == 'w' || ch == 'W')) {
					throw new Exception("'" + str + "' can't be converted to like form");
				}
				rv.append(ch);
				escape = false;
			}
			if (!rv.toString().equals(str)) {
				return ConstantImpl.create(rv.toString());
			}
		} else if (expr instanceof Operation<?>) {
			Operation<?> o = (Operation<?>) expr;
			if (o.getOperator() == Operators.CONCAT) {
				Condition<String> lhs = regexToLike((Condition<String>) o.getArgument(0));
				Condition<String> rhs = regexToLike((Condition<String>) o.getArgument(1));
				if (lhs != o.getArgument(0) || rhs != o.getArgument(1)) {
					return OperationImpl.create(String.class, Operators.CONCAT, lhs, rhs);
				}
			}
		}
		return expr;
	}

	public static <D> Predicate neConst(Condition<D> left, D constant) {
		return ne(left, ConstantImpl.create(constant));
	}

	public static <D> Predicate ne(Condition<D> left, Condition<? super D> right) {
		return PredicateOperation.create(Operators.NE, left, right);
	}

	public static Predicate or(Predicate left, Predicate right) {
		left = (Predicate) extract(left);
		right = (Predicate) extract(right);
		if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else {
			return PredicateOperation.create(Operators.OR, left, right);
		}
	}

	public static ImmutableList<Condition<?>> distinctList(Condition<?>... args) {
		final ImmutableList.Builder<Condition<?>> builder = ImmutableList.builder();
		final Set<Condition<?>> set = new HashSet<Condition<?>>(args.length);
		for (Condition<?> arg : args) {
			if (set.add(arg)) {
				builder.add(arg);
			}
		}
		return builder.build();
	}

	public static ImmutableList<Condition<?>> distinctList(Condition<?>[]... args) {
		final ImmutableList.Builder<Condition<?>> builder = ImmutableList.builder();
		final Set<Condition<?>> set = new HashSet<Condition<?>>();
		for (Condition<?>[] arr : args) {
			for (Condition<?> arg : arr) {
				if (set.add(arg)) {
					builder.add(arg);
				}
			}
		}
		return builder.build();
	}

	public static <T> Condition<T> extract(Condition<T> expr) {
		if (expr != null) {
			final Class<?> clazz = expr.getClass();
			if (clazz == AttributeImpl.class || clazz == PredicateOperation.class || clazz == ConstantImpl.class) {
				return expr;
			} else {
				return (Condition<T>) expr.accept(ExtractorVisitor.DEFAULT, null);
			}
		} else {
			return null;
		}
	}

	private ConditionUtils() {
	}

}
