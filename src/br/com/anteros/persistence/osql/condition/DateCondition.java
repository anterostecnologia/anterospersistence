package br.com.anteros.persistence.osql.condition;

import java.util.Date;

import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.Attribute;
import br.com.anteros.persistence.osql.impl.AttributeImpl;
import br.com.anteros.persistence.osql.operation.DateOperation;
import br.com.anteros.persistence.osql.operation.NumberOperation;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class DateCondition<T extends Comparable> extends TemporalCondition<T> {

	private static final DateCondition<Date> CURRENT_DATE = currentDate(Date.class);

	public static DateCondition<Date> currentDate() {
		return CURRENT_DATE;
	}

	public static <T extends Comparable> DateCondition<T> currentDate(Class<T> cl) {
		return DateOperation.create(cl, Operators.CURRENT_DATE);
	}

	private NumberCondition<Integer> dayOfMonth, dayOfWeek, dayOfYear;

	private DateCondition min, max;

	private NumberCondition<Integer> week, month, year, yearMonth, yearWeek;

	public DateCondition(Condition<T> mixin) {
		super(mixin);
	}

	@Override
	public DateCondition<T> as(Attribute<T> alias) {
		return DateOperation.create((Class<T>) getType(), Operators.ALIAS, sourceCondition, alias);
	}

	@Override
	public DateCondition<T> as(String alias) {
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

	public DateCondition<T> max() {
		if (max == null) {
			max = DateOperation.create(getType(), Operators.MAX_AGG, sourceCondition);
		}
		return max;
	}

	public DateCondition<T> min() {
		if (min == null) {
			min = DateOperation.create(getType(), Operators.MIN_AGG, sourceCondition);
		}
		return min;
	}

	public NumberCondition<Integer> month() {
		if (month == null) {
			month = NumberOperation.create(Integer.class, Operators.MONTH, sourceCondition);
		}
		return month;
	}

	public NumberCondition<Integer> week() {
		if (week == null) {
			week = NumberOperation.create(Integer.class, Operators.WEEK, sourceCondition);
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
