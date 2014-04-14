package br.com.anteros.persistence.osql.condition;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.impl.ConstantImpl;
import br.com.anteros.persistence.osql.operation.BooleanOperation;
import br.com.anteros.persistence.osql.operation.ComparableOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.StringOperation;

public abstract class StringCondition extends ComparableCondition<String> {

    private  NumberCondition<Integer> length;

    private  StringCondition lower, trim, upper;

    private  StringCondition min, max;

    private  BooleanCondition isempty;

    public StringCondition(Condition<String> sourceCondition) {
        super(sourceCondition);
    }

    @Override
    public StringCondition as(Attribute<String> alias) {
        return StringOperation.create(Operators.ALIAS, sourceCondition, alias);
    }

    @Override
    public StringCondition as(String alias) {
        return as(new AttributeImpl<String>(String.class, alias));
    }

    public StringCondition append(Condition<String> str) {
        return StringOperation.create(Operators.CONCAT, sourceCondition, str);
    }

    public StringCondition append(String str) {
        return append(ConstantImpl.create(str));
    }

    public SimpleCondition<Character> charAt(Condition<Integer> i) {
        return ComparableOperation.create(Character.class, Operators.CHAR_AT, sourceCondition, i);
    }

    public SimpleCondition<Character> charAt(int i) {
        return charAt(ConstantImpl.create(i));
    }
    
    public StringCondition concat(Condition<String> str) {
        return append(str);
    }

    public StringCondition concat(String str) {
        return append(str);
    }

    public BooleanCondition contains(Condition<String> str) {
        return BooleanOperation.create(Operators.STRING_CONTAINS, sourceCondition, str);
    }

    public BooleanCondition contains(String str) {
        return contains(ConstantImpl.create(str));
    }

    public BooleanCondition containsIgnoreCase(Condition<String> str) {
        return BooleanOperation.create(Operators.STRING_CONTAINS_IC, sourceCondition, str);
    }

    public BooleanCondition containsIgnoreCase(String str) {
        return containsIgnoreCase(ConstantImpl.create(str));
    }

    public BooleanCondition endsWith(Condition<String> str) {
        return BooleanOperation.create(Operators.ENDS_WITH, sourceCondition, str);
    }

    public BooleanCondition endsWithIgnoreCase(Condition<String> str) {
        return BooleanOperation.create(Operators.ENDS_WITH_IC, sourceCondition, str);
    }

    public BooleanCondition endsWith(String str) {
        return endsWith(ConstantImpl.create(str));
    }

    public BooleanCondition endsWithIgnoreCase(String str) {
        return endsWithIgnoreCase(ConstantImpl.create(str));
    }

    public BooleanCondition equalsIgnoreCase(Condition<String> str) {
        return BooleanOperation.create(Operators.EQ_IGNORE_CASE, sourceCondition, str);
    }

    public BooleanCondition equalsIgnoreCase(String str) {
        return equalsIgnoreCase(ConstantImpl.create(str));
    }

    public NumberCondition<Integer> indexOf(Condition<String> str) {
        return NumberOperation.create(Integer.class, Operators.INDEX_OF, sourceCondition, str);
    }

    public NumberCondition<Integer> indexOf(String str) {
        return indexOf(ConstantImpl.create(str));
    }

    public NumberCondition<Integer> indexOf(String str, int i) {
        return indexOf(ConstantImpl.create(str), i);
    }

    public NumberCondition<Integer> indexOf(Condition<String> str, int i) {
        return NumberOperation.create(Integer.class, Operators.INDEX_OF_2ARGS, sourceCondition, str, ConstantImpl.create(i));
    }

    public BooleanCondition isEmpty() {
        if (isempty == null) {
            isempty = BooleanOperation.create(Operators.STRING_IS_EMPTY, sourceCondition);
        }
        return isempty;
    }

    public BooleanCondition isNotEmpty() {
        return isEmpty().not();
    }

    public NumberCondition<Integer> length() {
        if (length == null) {
            length = NumberOperation.create(Integer.class, Operators.STRING_LENGTH, sourceCondition);
        }
        return length;
    }


    public BooleanCondition like(String str) {
        return BooleanOperation.create(Operators.LIKE, this, ConstantImpl.create(str));
    }


    public BooleanCondition like(Condition<String> str) {
        return BooleanOperation.create(Operators.LIKE, sourceCondition, str);
    }
    
    public BooleanCondition like(String str, char escape) {
        return BooleanOperation.create(Operators.LIKE_ESCAPE, sourceCondition, ConstantImpl.create(str), ConstantImpl.create(escape));
    }

    public BooleanCondition like(Condition<String> str, char escape) {
        return BooleanOperation.create(Operators.LIKE_ESCAPE, sourceCondition, str, ConstantImpl.create(escape));
    }

    public NumberCondition<Integer> locate(Condition<String> str) {
        return NumberOperation.create(Integer.class, Operators.LOCATE, str, sourceCondition);
    }
    
    public NumberCondition<Integer> locate(String str) {
        return NumberOperation.create(Integer.class, Operators.LOCATE, ConstantImpl.create(str), sourceCondition);
    }
    
    public NumberCondition<Integer> locate(Condition<String> str, NumberCondition<Integer> start) {
        return NumberOperation.create(Integer.class, Operators.LOCATE2, str, sourceCondition, start);
    }
    
    public NumberCondition<Integer> locate(String str, int start) {
        return NumberOperation.create(Integer.class, Operators.LOCATE2, ConstantImpl.create(str), sourceCondition, ConstantImpl.create(start));
    }
    
    public StringCondition lower() {
        if (lower == null) {
            lower = StringOperation.create(Operators.LOWER, sourceCondition);
        }
        return lower;
    }

    public BooleanCondition matches(Condition<String> regex) {
        return BooleanOperation.create(Operators.MATCHES, sourceCondition, regex);
    }

    public BooleanCondition matches(String regex) {
        return matches(ConstantImpl.create(regex));
    }

    public StringCondition max() {
        if (max == null) {
            max = StringOperation.create(Operators.MAX_AGG, sourceCondition);
        }
        return max;
    }

    public StringCondition min() {
        if (min == null) {
            min = StringOperation.create(Operators.MIN_AGG, sourceCondition);
        }
        return min;
    }
    
    public BooleanCondition notEqualsIgnoreCase(Condition<String> str) {
        return equalsIgnoreCase(str).not();
    }


    public BooleanCondition notEqualsIgnoreCase(String str) {
        return equalsIgnoreCase(str).not();
    }
    
    public BooleanCondition notLike(String str) {
        return like(str).not();
    }

    public BooleanCondition notLike(Condition<String> str) {
        return like(str).not();
    }
        
    public BooleanCondition notLike(String str, char escape) {
        return like(str, escape).not();
    }

    public BooleanCondition notLike(Condition<String> str, char escape) {
        return like(str, escape).not();
    }
    
    public StringCondition prepend(Condition<String> str) {
        return StringOperation.create(Operators.CONCAT, str, sourceCondition);
    }

    public StringCondition prepend(String str) {
        return prepend(ConstantImpl.create(str));
    }

    public BooleanCondition startsWith(Condition<String> str) {
        return BooleanOperation.create(Operators.STARTS_WITH, sourceCondition, str);
    }

    public BooleanCondition startsWithIgnoreCase(Condition<String> str) {
        return BooleanOperation.create(Operators.STARTS_WITH_IC, sourceCondition, str);
    }

    public BooleanCondition startsWith(String str) {
        return startsWith(ConstantImpl.create(str));
    }

    public BooleanCondition startsWithIgnoreCase(String str) {
        return startsWithIgnoreCase(ConstantImpl.create(str));
    }

    @Override
    public StringCondition stringValue() {
        return this;
    }

    public StringCondition substring(int beginIndex) {
        return StringOperation.create(Operators.SUBSTR_1ARG, sourceCondition, ConstantImpl.create(beginIndex));
    }

    public StringCondition substring(int beginIndex, int endIndex) {
        return StringOperation.create(Operators.SUBSTR_2ARGS, sourceCondition, ConstantImpl.create(beginIndex), ConstantImpl.create(endIndex));
    }
    
    public StringCondition substring(Condition<Integer> beginIndex, int endIndex) {
        return StringOperation.create(Operators.SUBSTR_2ARGS, sourceCondition, beginIndex, ConstantImpl.create(endIndex));
    }
    
    public StringCondition substring(int beginIndex, Condition<Integer> endIndex) {
        return StringOperation.create(Operators.SUBSTR_2ARGS, sourceCondition, ConstantImpl.create(beginIndex), endIndex);
    }

    public StringCondition substring(Condition<Integer> beginIndex) {
        return StringOperation.create(Operators.SUBSTR_1ARG, sourceCondition, beginIndex);
    }

    public StringCondition substring(Condition<Integer> beginIndex, Condition<Integer> endIndex) {
        return StringOperation.create(Operators.SUBSTR_2ARGS, sourceCondition, beginIndex, endIndex);
    }
    
    public StringCondition toLowerCase() {
        return lower();
    }

    public StringCondition toUpperCase() {
        return upper();
    }

    public StringCondition trim() {
        if (trim == null) {
            trim = StringOperation.create(Operators.TRIM, sourceCondition);
        }
        return trim;
    }

    public StringCondition upper() {
        if (upper == null) {
            upper = StringOperation.create(Operators.UPPER, sourceCondition);
        }
        return upper;
    }

}
