/*
 * Copyright 2011, Mysema Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.anteros.persistence.osql.types.expression;

import java.util.Date;



import br.com.anteros.persistence.osql.types.Expression;
import br.com.anteros.persistence.osql.types.Ops;
import br.com.anteros.persistence.osql.types.Path;
import br.com.anteros.persistence.osql.types.PathImpl;

/**
 * DateExpression represents Date expressions
 * The date representation is compatible with the Gregorian calendar.
 *
 * @param <T> expression type
 *
 * @author tiwe
 * @see <a href="http://en.wikipedia.org/wiki/Gregorian_calendar">Gregorian calendar</a>
 */
@SuppressWarnings({"unchecked"})
public abstract class DateExpression<T extends Comparable> extends TemporalExpression<T> {

    private static final DateExpression<Date> CURRENT_DATE = currentDate(Date.class);

    private static final long serialVersionUID = 6054664454254721302L;

    /**
     * Get an expression representing the current date as a DateExpression instance
     *
     * @return
     */
    public static DateExpression<Date> currentDate() {
        return CURRENT_DATE;
    }

    /**
     * Get an expression representing the current date as a DateExpression instance
     *
     * @return
     */
    public static <T extends Comparable> DateExpression<T> currentDate(Class<T> cl) {
        return DateOperation.create(cl, Ops.DateTimeOps.CURRENT_DATE);
    }

    
    private volatile NumberExpression<Integer> dayOfMonth, dayOfWeek, dayOfYear;

    
    private volatile DateExpression min, max;

    
    private volatile NumberExpression<Integer> week, month, year, yearMonth, yearWeek;

    public DateExpression(Expression<T> mixin) {
        super(mixin);
    }

    @Override
    public DateExpression<T> as(Path<T> alias) {
        return DateOperation.create((Class<T>)getType(), Ops.ALIAS, mixin, alias);
    }

    @Override
    public DateExpression<T> as(String alias) {
        return as(new PathImpl<T>(getType(), alias));
    }

    /**
     * Get a day of month expression (range 1-31)
     *
     * @return
     */
    public NumberExpression<Integer> dayOfMonth() {
        if (dayOfMonth == null) {
            dayOfMonth = NumberOperation.create(Integer.class, Ops.DateTimeOps.DAY_OF_MONTH, mixin);
        }
        return dayOfMonth;
    }

    /**
     * Get a day of week expression (range 1-7 / SUN-SAT)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return
     */
    public NumberExpression<Integer> dayOfWeek() {
        if (dayOfWeek == null) {
            dayOfWeek = NumberOperation.create(Integer.class, Ops.DateTimeOps.DAY_OF_WEEK, mixin);
        }
        return dayOfWeek;
    }

    /**
     * Get a day of year expression (range 1-356)
     * <p>NOT supported in JDOQL and not in Derby</p>
     *
     * @return
     */
    public NumberExpression<Integer> dayOfYear() {
        if (dayOfYear == null) {
            dayOfYear = NumberOperation.create(Integer.class, Ops.DateTimeOps.DAY_OF_YEAR, mixin);
        }
        return dayOfYear;
    }

    /**
     * Get the maximum value of this expression (aggregation)
     *
     * @return max(this)
     */
    public DateExpression<T> max() {
        if (max == null) {
            max = DateOperation.create(getType(), Ops.AggOps.MAX_AGG, mixin);
        }
        return max;
    }

    /**
     * Get the minimum value of this expression (aggregation)
     *
     * @return min(this)
     */
    public DateExpression<T> min() {
        if (min == null) {
            min = DateOperation.create(getType(), Ops.AggOps.MIN_AGG, mixin);
        }
        return min;
    }

    /**
     * Get a month expression (range 1-12 / JAN-DEC)
     *
     * @return
     */
    public NumberExpression<Integer> month() {
        if (month == null) {
            month = NumberOperation.create(Integer.class, Ops.DateTimeOps.MONTH, mixin);
        }
        return month;
    }

    /**
     * Get a week expression
     *
     * @return
     */
    public NumberExpression<Integer> week() {
        if (week == null) {
            week = NumberOperation.create(Integer.class, Ops.DateTimeOps.WEEK,  mixin);
        }
        return week;
    }

    /**
     * Get a year expression
     *
     * @return
     */
    public NumberExpression<Integer> year() {
        if (year == null) {
            year = NumberOperation.create(Integer.class, Ops.DateTimeOps.YEAR, mixin);
        }
        return year;
    }

    /**
     * Get a year / month expression
     *
     * @return
     */
    public NumberExpression<Integer> yearMonth() {
        if (yearMonth == null) {
            yearMonth = NumberOperation.create(Integer.class, Ops.DateTimeOps.YEAR_MONTH, mixin);
        }
        return yearMonth;
    }

    /**
     * Get a ISO yearweek expression
     *
     * @return
     */
    public NumberExpression<Integer> yearWeek() {
        if (yearWeek == null) {
            yearWeek = NumberOperation.create(Integer.class, Ops.DateTimeOps.YEAR_WEEK, mixin);
        }
        return yearWeek;
    }
}
