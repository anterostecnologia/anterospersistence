package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.NumberOperation;

public final class MathConditions {

    public static <A extends Number & Comparable<?>> NumberCondition<Double> acos(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.ACOS, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> asin(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.ASIN, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> atan(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.ATAN, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> cos(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.COS, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> cosh(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.COSH, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> cot(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.COT, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> coth(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.COTH, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> degrees(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.DEG, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> exp(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.EXP, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> ln(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.LN, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> log(Condition<A> num, int base) {
        return NumberOperation.create(Double.class, Operators.LOG, num, ConstantImpl.create(base));
    }

    public static <A extends Number & Comparable<?>> NumberCondition<A> max(Condition<A> left, Condition<A> right) {
        return NumberCondition.max(left, right);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<A> min(Condition<A> left, Condition<A> right) {
        return NumberCondition.min(left, right);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> power(Condition<A> num, int exponent) {
        return NumberOperation.create(Double.class, Operators.POWER, num, ConstantImpl.create(exponent));
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> radians(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.RAD, num);
    }

    public static NumberCondition<Double> random() {
        return NumberCondition.random();
    }

    public static NumberCondition<Double> random(int seed) {
        return NumberOperation.create(Double.class, Operators.RANDOM2, ConstantImpl.create(seed));
    }

    public static <A extends Number & Comparable<?>> NumberCondition<A> round(Condition<A> num) {
        return NumberOperation.create(num.getType(), Operators.ROUND, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<A> round(Condition<A> num, int s) {
        return NumberOperation.create(num.getType(), Operators.ROUND2, num, ConstantImpl.create(s));
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Integer> sign(Condition<A> num) {
        return NumberOperation.create(Integer.class, Operators.SIGN, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> sin(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.SIN, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> sinh(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.SINH, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> tan(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.TAN, num);
    }

    public static <A extends Number & Comparable<?>> NumberCondition<Double> tanh(Condition<A> num) {
        return NumberOperation.create(Double.class, Operators.TANH, num);
    }

    private MathConditions() {}
}
