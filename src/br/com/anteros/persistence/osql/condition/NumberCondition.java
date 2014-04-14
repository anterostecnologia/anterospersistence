package br.com.anteros.persistence.osql.condition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.util.MathUtils;

public abstract class NumberCondition<T extends Number & Comparable<?>> extends AbstractComparableCondition<T> {

	private static final NumberCondition<Double> random = NumberOperation.create(Double.class, Operators.RANDOM);

	public static <A extends Number & Comparable<?>> NumberCondition<A> max(Condition<A> left, Condition<A> right) {
		return NumberOperation.create(left.getType(), Operators.MAX, left, right);
	}

	public static <A extends Number & Comparable<?>> NumberCondition<A> min(Condition<A> left, Condition<A> right) {
		return NumberOperation.create(left.getType(), Operators.MIN, left, right);
	}

	public static NumberCondition<Double> random() {
		return random;
	}

	private NumberCondition<T> abs, sum, min, max, floor, ceil, round;

	private NumberCondition<Double> avg, sqrt;

	private NumberCondition<T> negation;

	public NumberCondition(Condition<T> mixin) {
		super(mixin);
	}

	@Override
	public NumberCondition<T> as(Attribute<T> alias) {
		return NumberOperation.create(getType(), Operators.ALIAS, sourceCondition, alias);
	}

	@Override
	public NumberCondition<T> as(String alias) {
		return NumberOperation.create(getType(), Operators.ALIAS, sourceCondition, new AttributeImpl<T>(getType(),
				alias));
	}

	public NumberCondition<T> abs() {
		if (abs == null) {
			abs = NumberOperation.create(getType(), Operators.ABS, sourceCondition);
		}
		return abs;
	}

	public <N extends Number & Comparable<?>> NumberCondition<T> add(Condition<N> right) {
		return NumberOperation.create(getType(), Operators.ADD, sourceCondition, right);
	}

	public <N extends Number & Comparable<N>> NumberCondition<T> add(N right) {
		return NumberOperation.create(getType(), Operators.ADD, sourceCondition, ConstantImpl.create(right));
	}

	public NumberCondition<Double> avg() {
		if (avg == null) {
			avg = NumberOperation.create(Double.class, Operators.AVG_AGG, sourceCondition);
		}
		return avg;
	}

	public NumberCondition<Byte> byteValue() {
		return castToNum(Byte.class);
	}

	@SuppressWarnings("unchecked")
	private T cast(Number number) {
		Class<T> type = (Class<T>) getType();
		if (type.equals(number.getClass())) {
			return (T) number;
		} else if (Byte.class.equals(type)) {
			return (T) Byte.valueOf(number.byteValue());
		} else if (Double.class.equals(type)) {
			return (T) Double.valueOf(number.doubleValue());
		} else if (Float.class.equals(type)) {
			return (T) Float.valueOf(number.floatValue());
		} else if (Integer.class.equals(type)) {
			return (T) Integer.valueOf(number.intValue());
		} else if (Long.class.equals(type)) {
			return (T) Long.valueOf(number.longValue());
		} else if (Short.class.equals(type)) {
			return (T) Short.valueOf(number.shortValue());
		} else if (BigInteger.class.equals(type)) {
			return (T) new BigInteger(String.valueOf(number.longValue()));
		} else if (BigDecimal.class.equals(type)) {
			return (T) new BigDecimal(number.toString());
		} else {
			throw new IllegalArgumentException("Tipo não suportado : " + type.getName());
		}
	}

	@Override
	public <A extends Number & Comparable<? super A>> NumberCondition<A> castToNum(Class<A> type) {
		if (type.equals(getType())) {
			return (NumberCondition<A>) this;
		} else {
			return NumberOperation.create(type, Operators.NUMCAST, sourceCondition, ConstantImpl.create(type));
		}
	}

	public NumberCondition<T> ceil() {
		if (ceil == null) {
			ceil = NumberOperation.create(getType(), Operators.CEIL, sourceCondition);
		}
		return ceil;
	}

	private Class<?> getDivisionType(Class<?> left, Class<?> right) {
		if (!left.equals(right)) {
			return Double.class;
		} else {
			return left;
		}
	}

	public <N extends Number & Comparable<?>> NumberCondition<T> divide(Condition<N> right) {
		Class<?> type = getDivisionType(getType(), right.getType());
		return NumberOperation.create((Class<T>) type, Operators.DIV, sourceCondition, right);
	}

	public <N extends Number & Comparable<?>> NumberCondition<T> divide(N right) {
		Class<?> type = getDivisionType(getType(), right.getClass());
		return NumberOperation.create((Class<T>) type, Operators.DIV, sourceCondition, ConstantImpl.create(right));
	}

	public NumberCondition<Double> doubleValue() {
		return castToNum(Double.class);
	}

	public NumberCondition<Float> floatValue() {
		return castToNum(Float.class);
	}

	public NumberCondition<T> floor() {
		if (floor == null) {
			floor = NumberOperation.create(getType(), Operators.FLOOR, sourceCondition);
		}
		return floor;
	}

	public final <A extends Number & Comparable<?>> BooleanCondition goe(A right) {
		return goe(ConstantImpl.create(cast(right)));
	}


	public <A extends Number & Comparable<?>> BooleanCondition goe(Condition<A> right) {
		return BooleanOperation.create(Operators.GOE, sourceCondition, right);
	}

	public BooleanCondition goeAll(CollectionCondition<?, ? super T> right) {
		return goe(ConditionUtils.<T> all(right));
	}

	public BooleanCondition goeAny(CollectionCondition<?, ? super T> right) {
		return goe(ConditionUtils.<T> any(right));
	}

	public final <A extends Number & Comparable<?>> BooleanCondition gt(A right) {
		return gt(ConstantImpl.create(cast(right)));
	}


	public final <A extends Number & Comparable<?>> BooleanCondition gt(Condition<A> right) {
		return BooleanOperation.create(Operators.GT, sourceCondition, right);
	}

	public BooleanCondition gtAll(CollectionCondition<?, ? super T> right) {
		return gt(ConditionUtils.<T> all(right));
	}

	public BooleanCondition gtAny(CollectionCondition<?, ? super T> right) {
		return gt(ConditionUtils.<T> any(right));
	}

	public final <A extends Number & Comparable<?>> BooleanCondition between(A from, A to) {
		if (from == null) {
			if (to != null) {
				return BooleanOperation.create(Operators.LOE, sourceCondition, ConstantImpl.create(to));
			} else {
				throw new IllegalArgumentException("Either from or to needs to be non-null");
			}
		} else if (to == null) {
			return BooleanOperation.create(Operators.GOE, sourceCondition, ConstantImpl.create(from));
		} else {
			return BooleanOperation.create(Operators.BETWEEN, sourceCondition, ConstantImpl.create(from),
					ConstantImpl.create(to));
		}
	}

	public final <A extends Number & Comparable<?>> BooleanCondition between(Condition<A> from, Condition<A> to) {
		if (from == null) {
			if (to != null) {
				return BooleanOperation.create(Operators.LOE, sourceCondition, to);
			} else {
				throw new IllegalArgumentException("Either from or to needs to be non-null");
			}
		} else if (to == null) {
			return BooleanOperation.create(Operators.GOE, sourceCondition, from);
		} else {
			return BooleanOperation.create(Operators.BETWEEN, sourceCondition, from, to);
		}
	}

	public final <A extends Number & Comparable<?>> BooleanCondition notBetween(A from, A to) {
		return between(from, to).not();
	}

	public final <A extends Number & Comparable<?>> BooleanCondition notBetween(Condition<A> from, Condition<A> to) {
		return between(from, to).not();
	}

	public NumberCondition<Integer> intValue() {
		return castToNum(Integer.class);
	}

	public BooleanCondition like(String str) {
		return BooleanOperation.create(Operators.LIKE, stringValue(), ConstantImpl.create(str));
	}

	public BooleanCondition like(Condition<String> str) {
		return BooleanOperation.create(Operators.LIKE, stringValue(), str);
	}

	public final <A extends Number & Comparable<?>> BooleanCondition loe(A right) {
		return loe(ConstantImpl.create(cast(right)));
	}

	public final <A extends Number & Comparable<?>> BooleanCondition loe(Condition<A> right) {
		return BooleanOperation.create(Operators.LOE, sourceCondition, right);
	}

	public BooleanCondition loeAll(CollectionCondition<?, ? super T> right) {
		return loe(ConditionUtils.<T> all(right));
	}

	public BooleanCondition loeAny(CollectionCondition<?, ? super T> right) {
		return loe(ConditionUtils.<T> any(right));
	}

	public NumberCondition<Long> longValue() {
		return castToNum(Long.class);
	}

	public final <A extends Number & Comparable<?>> BooleanCondition lt(A right) {
		return lt(ConstantImpl.create(cast(right)));
	}

	public final <A extends Number & Comparable<?>> BooleanCondition lt(Condition<A> right) {
		return BooleanOperation.create(Operators.LT, this, right);
	}

	public BooleanCondition ltAll(CollectionCondition<?, ? super T> right) {
		return lt(ConditionUtils.<T> all(right));
	}

	public BooleanCondition ltAny(CollectionCondition<?, ? super T> right) {
		return lt(ConditionUtils.<T> any(right));
	}

	public NumberCondition<T> max() {
		if (max == null) {
			max = NumberOperation.create(getType(), Operators.MAX_AGG, sourceCondition);
		}
		return max;
	}

	public NumberCondition<T> min() {
		if (min == null) {
			min = NumberOperation.create(getType(), Operators.MIN_AGG, sourceCondition);
		}
		return min;
	}

	public NumberCondition<T> mod(Condition<T> num) {
		return NumberOperation.create(getType(), Operators.MOD, sourceCondition, num);
	}

	public NumberCondition<T> mod(T num) {
		return NumberOperation.create(getType(), Operators.MOD, sourceCondition, ConstantImpl.create(num));
	}

	public <N extends Number & Comparable<?>> NumberCondition<T> multiply(Condition<N> right) {
		return NumberOperation.create(getType(), Operators.MULT, sourceCondition, right);
	}

	public <N extends Number & Comparable<N>> NumberCondition<T> multiply(N right) {
		return NumberOperation.create(getType(), Operators.MULT, sourceCondition, ConstantImpl.create(right));
	}

	public NumberCondition<T> negate() {
		if (negation == null) {
			negation = NumberOperation.create(getType(), Operators.NEGATE, sourceCondition);
		}
		return negation;
	}

	public NumberCondition<T> round() {
		if (round == null) {
			round = NumberOperation.create(getType(), Operators.ROUND, sourceCondition);
		}
		return round;
	}

	public NumberCondition<Short> shortValue() {
		return castToNum(Short.class);
	}

	public NumberCondition<Double> sqrt() {
		if (sqrt == null) {
			sqrt = NumberOperation.create(Double.class, Operators.SQRT, sourceCondition);
		}
		return sqrt;
	}

	public <N extends Number & Comparable<?>> NumberCondition<T> subtract(Condition<N> right) {
		return NumberOperation.create(getType(), Operators.SUB, sourceCondition, right);
	}

	public <N extends Number & Comparable<?>> NumberCondition<T> subtract(N right) {
		return NumberOperation.create(getType(), Operators.SUB, sourceCondition, ConstantImpl.create(right));
	}

	public NumberCondition<T> sum() {
		if (sum == null) {
			sum = NumberOperation.create(getType(), Operators.SUM_AGG, sourceCondition);
		}
		return sum;
	}

	@Override
	public BooleanCondition in(Number... numbers) {
		return super.in(convert(numbers));
	}

	@Override
	public BooleanCondition notIn(Number... numbers) {
		return super.notIn(convert(numbers));
	}

	private List<T> convert(Number... numbers) {
		List<T> list = new ArrayList<T>(numbers.length);
		for (int i = 0; i < numbers.length; i++) {
			list.add(MathUtils.cast(numbers[i], getType()));
		}
		return list;
	}

}
