package br.com.anteros.persistence.osql.condition;

import java.sql.Time;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.NumberOperation;
import br.com.anteros.persistence.osql.operation.TimeOperation;

@SuppressWarnings({"unchecked"})
public abstract class TimeCondition<T extends Comparable> extends TemporalCondition<T> {

    private static final TimeCondition<Time> CURRENT_TIME = currentTime(Time.class);

    private volatile NumberCondition<Integer> hours, minutes, seconds, milliseconds;

    public TimeCondition(Condition<T> mixin) {
        super(mixin);
    }

    @Override
    public TimeCondition<T> as(Attribute<T> alias) {
        return TimeOperation.create((Class<T>)getType(),Operators.ALIAS, sourceCondition, alias);
    }

    @Override
    public TimeCondition<T> as(String alias) {
        return as(new AttributeImpl<T>(getType(), alias));
    }
    
    public NumberCondition<Integer> hour() {
        if (hours == null) {
            hours = NumberOperation.create(Integer.class, Operators.HOUR, sourceCondition);
        }
        return hours;
    }

    public NumberCondition<Integer> minute() {
        if (minutes == null) {
            minutes = NumberOperation.create(Integer.class, Operators.MINUTE, sourceCondition);
        }
        return minutes;
    }

    public NumberCondition<Integer> second() {
        if (seconds == null) {
            seconds = NumberOperation.create(Integer.class, Operators.SECOND, sourceCondition);
        }
        return seconds;
    }

    public NumberCondition<Integer> milliSecond() {
        if (milliseconds == null) { 
            milliseconds = NumberOperation.create(Integer.class, Operators.MILLISECOND, sourceCondition);
        }
        return milliseconds;
    }

    public static TimeCondition<Time> currentTime() {
        return CURRENT_TIME;
    }

    public static <T extends Comparable> TimeCondition<T> currentTime(Class<T> cl) {
        return TimeOperation.create(cl, Operators.CURRENT_TIME);
    }

}
