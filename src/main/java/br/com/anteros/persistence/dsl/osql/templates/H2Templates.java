package br.com.anteros.persistence.dsl.osql.templates;

import br.com.anteros.persistence.dsl.osql.SQLTemplates;
import br.com.anteros.persistence.dsl.osql.types.Ops;


public class H2Templates extends SQLTemplates {

    public static Builder builder() {
        return new Builder() {
            @Override
            protected SQLTemplates build(char escape, boolean quote) {
                return new H2Templates(escape, quote);
            }
        };
    }

    public H2Templates() {
        this('\\', false);
    }

    public H2Templates(boolean quote) {
        this('\\', quote);
    }

    public H2Templates(char escape, boolean quote) {
        super("\"", escape, quote);
        setNativeMerge(true);
        setLimitRequired(true);
        setCountDistinctMultipleColumns(true);

        add(Ops.MathOps.ROUND, "round({0},0)");
        add(Ops.TRIM, "trim(both from {0})");

        add(Ops.DateTimeOps.DAY_OF_WEEK, "day_of_week({0})");

        add(Ops.MathOps.LN, "log({0})");
        add(Ops.MathOps.LOG, "(log({0}) / log({1}))");
        add(Ops.MathOps.COTH, "(cosh({0}) / sinh({0}))");

        add(Ops.DateTimeOps.DATE, "convert({0}, date)");
    }

}
