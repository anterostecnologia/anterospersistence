package br.com.anteros.persistence.osql.condition;

import java.util.IdentityHashMap;
import java.util.Map;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.AttributeRelationType;

public class CodeTemplates {

	public static final CodeTemplates DEFAULT = new CodeTemplates();

	private final Map<Operator<?>, CodeTemplate> codeTemplates = new IdentityHashMap<Operator<?>, CodeTemplate>(150);

	private final Map<Operator<?>, Integer> precedence = new IdentityHashMap<Operator<?>, Integer>(150);

	private final CodeTemplateFactory codeTemplateFactory;

	private final char escape;

	protected CodeTemplates() {
		this('\\');
	}

	protected CodeTemplates(char escape) {
		this.escape = escape;
		codeTemplateFactory = new CodeTemplateFactory(escape);

		add(Operators.LIST, "{0}, {1}", 40);
		add(Operators.SINGLETON, "{0}", 40);
		add(Operators.WRAPPED, "({0})");

		add(Operators.AND, "{0} && {1}", 36);
		add(Operators.NOT, "!{0}", 3);
		add(Operators.OR, "{0} || {1}", 38);
		add(Operators.XNOR, "{0} xnor {1}", 39);
		add(Operators.XOR, "{0} xor {1}", 39);

		add(Operators.COLUMN_IS_EMPTY, "empty({0})");
		add(Operators.COLUMN_SIZE, "size({0})");

		add(Operators.ARRAY_SIZE, "size({0})");

		add(Operators.MAP_SIZE, "size({0})");
		add(Operators.MAP_IS_EMPTY, "empty({0})");
		add(Operators.CONTAINS_KEY, "containsKey({0},{1})");
		add(Operators.CONTAINS_VALUE, "containsValue({0},{1})");

		add(Operators.BETWEEN, "{0} between {1} and {2}", 30);
		add(Operators.GOE, "{0} >= {1}", 20);
		add(Operators.GT, "{0} > {1}", 21);
		add(Operators.LOE, "{0} <= {1}", 22);
		add(Operators.LT, "{0} < {1}", 23);

		add(Operators.NEGATE, "-{0}", 6);
		add(Operators.ADD, "{0} + {1}", 13);
		add(Operators.DIV, "{0} / {1}", 8);
		add(Operators.MOD, "{0} % {1}", 10);
		add(Operators.MULT, "{0} * {1}", 7);
		add(Operators.SUB, "{0} - {1}", 12);

		add(Operators.EQ, "{0} = {1}", 18);
		add(Operators.EQ_IGNORE_CASE, "eqIc({0},{1})", 18);
		add(Operators.INSTANCE_OF, "{0}.class = {1}");
		add(Operators.NE, "{0} != {1}", 25);
		add(Operators.IN, "{0} in {1}", 27);
		add(Operators.NOT_IN, "{0} not in {1}", 27);
		add(Operators.IS_NULL, "{0} is null", 26);
		add(Operators.IS_NOT_NULL, "{0} is not null", 26);
		add(Operators.ALIAS, "{0} as {1}", 0);

		add(Operators.EXISTS, "exists({0})");

		add(Operators.NUMCAST, "cast({0},{1})");
		add(Operators.STRING_CAST, "str({0})");

		add(Operators.CONCAT, "{0} + {1}", 37);
		add(Operators.LOWER, "lower({0})");
		add(Operators.SUBSTR_1ARG, "substring({0},{1})");
		add(Operators.SUBSTR_2ARGS, "substring({0},{1},{2})");
		add(Operators.TRIM, "trim({0})");
		add(Operators.UPPER, "upper({0})");
		add(Operators.MATCHES, "matches({0},{1})");
		add(Operators.MATCHES_IC, "matchesIgnoreCase({0},{1})");
		add(Operators.STARTS_WITH, "startsWith({0},{1})");
		add(Operators.STARTS_WITH_IC, "startsWithIgnoreCase({0},{1})");
		add(Operators.ENDS_WITH, "endsWith({0},{1})");
		add(Operators.ENDS_WITH_IC, "endsWithIgnoreCase({0},{1})");
		add(Operators.STRING_CONTAINS, "contains({0},{1})");
		add(Operators.STRING_CONTAINS_IC, "containsIc({0},{1})");
		add(Operators.CHAR_AT, "charAt({0},{1})");
		add(Operators.STRING_LENGTH, "length({0})");
		add(Operators.INDEX_OF, "indexOf({0},{1})");
		add(Operators.INDEX_OF_2ARGS, "indexOf({0},{1},{2})");
		add(Operators.STRING_IS_EMPTY, "empty({0})");
		add(Operators.LIKE, "{0} like {1}", 26);
		add(Operators.LIKE_ESCAPE, "{0} like {1} escape '{2s}'", 26);

		add(Operators.LEFT, "left({0},{1})");
		add(Operators.RIGHT, "right({0},{1})");
		add(Operators.LTRIM, "ltrim({0})");
		add(Operators.RTRIM, "rtrim({0})");
		add(Operators.LOCATE, "locate({0},{1})");
		add(Operators.LOCATE2, "locate({0},{1},{2s})");
		add(Operators.LPAD, "lpad({0},{1})");
		add(Operators.RPAD, "rpad({0},{1})");
		add(Operators.LPAD2, "lpad({0},{1},'{2s}')");
		add(Operators.RPAD2, "rpad({0},{1},'{2s}')");

		add(Operators.SYSDATE, "sysdate");
		add(Operators.CURRENT_DATE, "current_date()");
		add(Operators.CURRENT_TIME, "current_time()");
		add(Operators.CURRENT_TIMESTAMP, "current_timestamp()");
		add(Operators.DATE, "date({0})");

		add(Operators.MILLISECOND, "millisecond({0})");
		add(Operators.SECOND, "second({0})");
		add(Operators.MINUTE, "minute({0})");
		add(Operators.HOUR, "hour({0})");
		add(Operators.WEEK, "week({0})");
		add(Operators.MONTH, "month({0})");
		add(Operators.YEAR, "year({0})");
		add(Operators.YEAR_MONTH, "yearMonth({0})");
		add(Operators.YEAR_WEEK, "yearweek({0})");
		add(Operators.DAY_OF_WEEK, "dayofweek({0})");
		add(Operators.DAY_OF_MONTH, "dayofmonth({0})");
		add(Operators.DAY_OF_YEAR, "dayofyear({0})");

		add(Operators.ADD_YEARS, "add_years({0},{1})");
		add(Operators.ADD_MONTHS, "add_months({0},{1})");
		add(Operators.ADD_WEEKS, "add_weeks({0},{1})");
		add(Operators.ADD_DAYS, "add_days({0},{1})");
		add(Operators.ADD_HOURS, "add_hours({0},{1})");
		add(Operators.ADD_MINUTES, "add_minutes({0},{1})");
		add(Operators.ADD_SECONDS, "add_seconds({0},{1})");

		add(Operators.DIFF_YEARS, "diff_years({0},{1})");
		add(Operators.DIFF_MONTHS, "diff_months({0},{1})");
		add(Operators.DIFF_WEEKS, "diff_weeks({0},{1})");
		add(Operators.DIFF_DAYS, "diff_days({0},{1})");
		add(Operators.DIFF_HOURS, "diff_hours({0},{1})");
		add(Operators.DIFF_MINUTES, "diff_minutes({0},{1})");
		add(Operators.DIFF_SECONDS, "diff_seconds({0},{1})");

		add(Operators.TRUNC_YEAR, "trunc_year({0})");
		add(Operators.TRUNC_MONTH, "trunc_month({0})");
		add(Operators.TRUNC_WEEK, "trunc_week({0})");
		add(Operators.TRUNC_DAY, "trunc_day({0})");
		add(Operators.TRUNC_HOUR, "trunc_hour({0})");
		add(Operators.TRUNC_MINUTE, "trunc_minute({0})");
		add(Operators.TRUNC_SECOND, "trunc_second({0})");

		add(Operators.ABS, "abs({0})");
		add(Operators.ACOS, "acos({0})");
		add(Operators.ASIN, "asin({0})");
		add(Operators.ATAN, "atan({0})");
		add(Operators.CEIL, "ceil({0})");
		add(Operators.COS, "cos({0})");
		add(Operators.COSH, "cosh({0})");
		add(Operators.COT, "cot({0})");
		add(Operators.COTH, "coth({0})");
		add(Operators.DEG, "degrees({0})");
		add(Operators.TAN, "tan({0})");
		add(Operators.TANH, "tanh({0})");
		add(Operators.SQRT, "sqrt({0})");
		add(Operators.SIGN, "sign({0})");
		add(Operators.SIN, "sin({0})");
		add(Operators.SINH, "sinh({0})");
		add(Operators.ROUND, "round({0})");
		add(Operators.ROUND2, "round({0},{1})");
		add(Operators.RAD, "radians({0})");
		add(Operators.RANDOM, "random()");
		add(Operators.RANDOM2, "random({0})");
		add(Operators.POWER, "pow({0},{1})");
		add(Operators.MIN, "min({0},{1})");
		add(Operators.MAX, "max({0},{1})");
		add(Operators.LOG, "log({0},{1})");
		add(Operators.LN, "ln({0})");
		add(Operators.FLOOR, "floor({0})");
		add(Operators.EXP, "exp({0})");

		add(AttributeRelationType.PROPERTY, "{0}.{1s}");
		add(AttributeRelationType.VARIABLE, "{0s}");
		add(AttributeRelationType.DELEGATE, "{0}");
		add(Operators.ORDINAL, "ordinal({0})");

		for (AttributeRelationType type : new AttributeRelationType[] { AttributeRelationType.LISTVALUE,
				AttributeRelationType.MAPVALUE, AttributeRelationType.MAPVALUE_CONSTANT }) {
			add(type, "{0}.get({1})");
		}
		add(AttributeRelationType.ARRAYVALUE, "{0}[{1}]");
		add(AttributeRelationType.COLLECTION_ANY, "any({0})");
		add(AttributeRelationType.LISTVALUE_CONSTANT, "{0}.get({1s})");
		add(AttributeRelationType.ARRAYVALUE_CONSTANT, "{0}[{1s}]");

		add(Operators.CASE, "case {0} end", 0);
		add(Operators.CASE_WHEN, "when {0} then {1} {2}", 0);
		add(Operators.CASE_ELSE, "else {0}");

		add(Operators.CASE_EQ, "case {0} {1} end");
		add(Operators.CASE_EQ_WHEN, "when {1} then {2} {3}");
		add(Operators.CASE_EQ_ELSE, "else {0}");

		add(Operators.COALESCE, "coalesce({0})");

		add(Operators.NULLIF, "nullif({0},{1})");

		add(Operators.EXISTS, "exists {0}");

		add(Operators.BOOLEAN_ALL, "all({0})");
		add(Operators.BOOLEAN_ANY, "any({0})");
		add(Operators.AVG_AGG, "avg({0})");
		add(Operators.MAX_AGG, "max({0})");
		add(Operators.MIN_AGG, "min({0})");
		add(Operators.SUM_AGG, "sum({0})");
		add(Operators.COUNT_AGG, "count({0})");
		add(Operators.COUNT_DISTINCT_AGG, "count(distinct {0})");
		add(Operators.COUNT_DISTINCT_ALL_AGG, "count(distinct *)");
		add(Operators.COUNT_ALL_AGG, "count(*)");

		add(Operators.AVG_IN_COL, "avg({0})");
		add(Operators.MAX_IN_COL, "max({0})");
		add(Operators.MIN_IN_COL, "min({0})");

		add(Operators.ANY, "any {0}");
		add(Operators.ALL, "all {0}");
	}

	protected final void add(Operator<?> op, String pattern) {
		codeTemplates.put(op, codeTemplateFactory.create(pattern));
		if (!precedence.containsKey(op)) {
			precedence.put(op, -1);
		}
	}

	protected final void add(Operator<?> op, String pattern, int pre) {
		codeTemplates.put(op, codeTemplateFactory.create(pattern));
		precedence.put(op, pre);
	}

	public final char getEscapeChar() {
		return escape;
	}

	public final CodeTemplate getTemplate(Operator<?> op) {
		return codeTemplates.get(op);
	}

	public final int getPrecedence(Operator<?> op) {
		return precedence.get(op).intValue();
	}

}
