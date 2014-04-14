package br.com.anteros.persistence.osql.condition;

import java.util.Date;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.DateTimeOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;

@SuppressWarnings({"unchecked"})
public abstract class DateTimeCondition<T extends Comparable> extends TemporalCondition<T> {

    private static final DateTimeCondition<Date> CURRENT_DATE = currentDate(Date.class);

    private static final DateTimeCondition<Date> CURRENT_TIMESTAMP = currentTimestamp(Date.class);

    public static DateTimeCondition<Date> currentDate() {
        return CURRENT_DATE;
    }

    public static <T extends Comparable> DateTimeCondition<T> currentDate(Class<T> cl) {
        return DateTimeOperation.<T>create(cl, Operators.CURRENT_DATE);
    }

    public static DateTimeCondition<Date> currentTimestamp() {
        return CURRENT_TIMESTAMP;
    }

    public static <T extends Comparable> DateTimeCondition<T> currentTimestamp(Class<T> cl) {
        return DateTimeOperation.create(cl, Operators.CURRENT_TIMESTAMP);
    }


    private NumberCondition<Integer> dayOfMonth, dayOfWeek, dayOfYear;

    private NumberCondition<Integer> hours, minutes, seconds, milliseconds;


    private DateTimeCondition<T> min, max;

    private NumberCondition<Integer> week, month, year, yearMonth, yearWeek;

    public DateTimeCondition(Condition<T> sourceCondition) {
        super(sourceCondition);
    }

    @Override
    public DateTimeCondition<T> as(Attribute<T> alias) {
        return DateTimeOperation.create((Class<T>)getType(), Operators.ALIAS, sourceCondition, alias);
    }

    @Override
    public DateTimeCondition<T> as(String alias) {
        return as(new AttributeImpl<T>(getType(), alias));
    }

    public NumberCondition<Integer> dayOfMonth() {
        if (dayOfMonth == null) {
            dayOfMonth = NumberOperation.create(Integer.class, Operators.DAY_OF_MONTH, sourceCondition);
        }
        return dayOfMonth;
    }

    public NumberCondition<Integer> dayOfWeek() {
        if (dayOfWeek == null) {
            dayOfWeek = NumberOperation.create(Integer.class, Operators.DAY_OF_WEEK, sourceCondition);
        }
        return dayOfWeek;
    }

    public NumberCondition<Integer> dayOfYear() {
        if (dayOfYear == null) {
            dayOfYear = NumberOperation.create(Integer.class, Operators.DAY_OF_YEAR, sourceCondition);
        }
        return dayOfYear;
    }

    public NumberCondition<Integer> hour() {
        if (hours == null) {
            hours = NumberOperation.create(Integer.class, Operators.HOUR, sourceCondition);
        }
        return hours;
    }

    public DateTimeCondition<T> max() {
        if (max == null) {
            max = DateTimeOperation.create((Class<T>)getType(), Operators.MAX_AGG, sourceCondition);
        }
        return max;
    }

    public NumberCondition<Integer> milliSecond() {
        if (milliseconds == null) {
            milliseconds = NumberOperation.create(Integer.class, Operators.MILLISECOND, sourceCondition);
        }
        return milliseconds;
    }

    public DateTimeCondition<T> min() {
        if (min == null) {
            min = DateTimeOperation.create((Class<T>)getType(), Operators.MIN_AGG, sourceCondition);
        }
        return min;
    }

    public NumberCondition<Integer> minute() {
        if (minutes == null) {
            minutes = NumberOperation.create(Integer.class, Operators.MINUTE, sourceCondition);
        }
        return minutes;
    }

    public NumberCondition<Integer> month() {
        if (month == null) {
            month = NumberOperation.create(Integer.class, Operators.MONTH, sourceCondition);
        }
        return month;
    }

    public NumberCondition<Integer> second() {
        if (seconds == null) {
            seconds = NumberOperation.create(Integer.class, Operators.SECOND, sourceCondition);
        }
        return seconds;
    }

    public NumberCondition<Integer> week() {
        if (week == null) {
            week = NumberOperation.create(Integer.class, Operators.WEEK,  sourceCondition);
        }
        return week;
    }

    public NumberCondition<Integer> year() {
        if (year == null) {
            year = NumberOperation.create(Integer.class, Operators.YEAR, sourceCondition);
        }
        return year;
    }

    public NumberCondition<Integer> yearMonth() {
        if (yearMonth == null) {
            yearMonth = NumberOperation.create(Integer.class, Operators.YEAR_MONTH, sourceCondition);
        }
        return yearMonth;
    }

    public NumberCondition<Integer> yearWeek() {
        if (yearWeek == null) {
            yearWeek = NumberOperation.create(Integer.class, Operators.YEAR_WEEK, sourceCondition);
        }
        return yearWeek;
    }

}
