package br.com.anteros.persistence.osql;

import java.util.Locale;

import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.impl.OperationImpl;

import com.google.common.base.Function;

public final class ConditionConverters {

    private static final ConstantCondition<String> PERCENT = ConstantImpl.create("%");

    private final char escape;

    public ConditionConverters(char escape) {
        this.escape = escape;
    }

    public final Function<Object,Object> toLowerCase =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                return OperationImpl.create(String.class, Operators.LOWER, (Condition)arg);
            } else {
                return arg.toString().toLowerCase(Locale.ENGLISH);
            }
        }
    };

    public final Function<Object,Object> toUpperCase =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                return OperationImpl.create(String.class, Operators.UPPER, (Condition)arg);
            } else {
                return arg.toString().toUpperCase(Locale.ENGLISH);
            }
        }
    };

    public final Function<Object,Object> toStartsWithViaLike =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                return OperationImpl.create(String.class, Operators.CONCAT, (Condition)arg, PERCENT);
            } else {
                return escapeForLike(arg.toString()) + "%";
            }
        }
    };

    public final Function<Object,Object> toStartsWithViaLikeLower =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                Condition<String> concated = OperationImpl.create(String.class, Operators.CONCAT, (Condition)arg, PERCENT);
                return OperationImpl.create(String.class, Operators.LOWER, concated);
            } else {
                return escapeForLike(arg.toString().toLowerCase(Locale.ENGLISH)) + "%";
            }
        }
    };

    public final Function<Object,Object> toEndsWithViaLike =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                return OperationImpl.create(String.class, Operators.CONCAT, PERCENT, (Condition)arg);
            } else {
                return "%" + escapeForLike(arg.toString());
            }
        }
    };

    public final Function<Object,Object> toEndsWithViaLikeLower =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                Condition<String> concated = OperationImpl.create(String.class, Operators.CONCAT, PERCENT, (Condition)arg);
                return OperationImpl.create(String.class, Operators.LOWER, concated);
            } else {
                return "%" + escapeForLike(arg.toString().toLowerCase(Locale.ENGLISH));
            }
        }
    };

    public final Function<Object,Object> toContainsViaLike =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                Condition<String> concated = OperationImpl.create(String.class, Operators.CONCAT, PERCENT, (Condition)arg);
                return OperationImpl.create(String.class, Operators.CONCAT, concated, PERCENT);
            } else {
                return "%" + escapeForLike(arg.toString()) + "%";
            }
        }
    };

    public final Function<Object,Object> toContainsViaLikeLower =
        new Function<Object,Object>() {
        @Override
        public Object apply(Object arg) {
            if (arg instanceof ConstantCondition) {
                return ConstantImpl.create(apply(arg.toString()).toString());
            } else if (arg instanceof Condition) {
                Condition<String> concated = OperationImpl.create(String.class, Operators.CONCAT, PERCENT, (Condition)arg);
                concated = OperationImpl.create(String.class, Operators.CONCAT, concated, PERCENT);
                return OperationImpl.create(String.class, Operators.LOWER, concated);
            } else {
                return "%" + escapeForLike(arg.toString().toLowerCase(Locale.ENGLISH)) + "%";
            }
        }
    };

    public String escapeForLike(String str) {
        final StringBuilder rv = new StringBuilder(str.length() + 3);
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            if (ch == escape || ch == '%' || ch == '_') {
                rv.append(escape);
            }
            rv.append(ch);
        }
        return rv.toString();
    }

}
