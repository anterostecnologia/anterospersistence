package br.com.anteros.persistence.osql.query;

import br.com.anteros.persistence.osql.Operator;
import br.com.anteros.persistence.osql.Operators;
import br.com.anteros.persistence.osql.attribute.AttributeRelationType;
import br.com.anteros.persistence.osql.condition.CodeTemplates;

public class OSQLCodeTemplates extends CodeTemplates {

	public static final char DEFAULT_ESCAPE = '!';

	public static final OSQLCodeTemplates DEFAULT = new OSQLCodeTemplates();

	//VER AQUI private final QueryHandler queryHandler;

	protected OSQLCodeTemplates() {
		//VER AQUI this(DEFAULT_ESCAPE, DefaultQueryHandler.DEFAULT);
	}

	protected OSQLCodeTemplates(char escape) {
		//VER AQUI this(escape, DefaultQueryHandler.DEFAULT);
	}

	protected OSQLCodeTemplates(char escape, /*VER AQUI QueryHandler*/ Object queryHandler) {
		super(escape);
		//VER AQUI this.queryHandler = queryHandler;

		// CHECKSTYLE:OFF
		// boolean
		add(Operators.AND, "{0} and {1}", 36);
		add(Operators.NOT, "not {0}", 3);
		add(Operators.OR, "{0} or {1}", 38);
		add(Operators.XNOR, "{0} xnor {1}", 39);
		add(Operators.XOR, "{0} xor {1}", 39);

		// comparison
		add(Operators.BETWEEN, "{0} between {1} and {2}", 30);

		// numeric
		add(Operators.SQRT, "sqrt({0})");
		add(Operators.MOD, "mod({0},{1})", 0);

		// various
		add(Operators.NE, "{0} <> {1}", 25);
		add(Operators.IS_NULL, "{0} is null", 26);
		add(Operators.IS_NOT_NULL, "{0} is not null", 26);
		//VER AQUI  add(JPQLOperators.CAST, "cast({0} as {1s})");
		add(Operators.NUMCAST, "cast({0} as {1s})");

		// collection
		// VER AQUI add(JPQLOperators.MEMBER_OF, "{0} member of {1}");
		// VER AQUI add(JPQLOperators.NOT_MEMBER_OF, "{0} not member of {1}");

		add(Operators.IN, "{0} in {1}");
		add(Operators.NOT_IN, "{0} not in {1}");
		add(Operators.COLUMN_IS_EMPTY, "{0} is empty");
		add(Operators.COLUMN_SIZE, "size({0})");
		add(Operators.ARRAY_SIZE, "size({0})");

		// string
		add(Operators.LIKE, "{0} like {1} escape '" + escape + "'", 1);
		add(Operators.CONCAT, "concat({0},{1})", 0);
		add(Operators.MATCHES, "{0} like {1}  escape '" + escape + "'", 27); // TODO
																				// :
																				// support
																				// real
																				// regexes
		add(Operators.MATCHES_IC, "{0} like {1} escape '" + escape + "'", 27); // TODO
																				// :
																				// support
																				// real
																				// regexes
		add(Operators.LOWER, "lower({0})");
		add(Operators.SUBSTR_1ARG, "substring({0},{1s}+1)", 1);
		add(Operators.SUBSTR_2ARGS, "substring({0},{1s}+1,{2s}-{1s})", 1);
		add(Operators.TRIM, "trim({0})");
		add(Operators.UPPER, "upper({0})");
		add(Operators.EQ_IGNORE_CASE, "{0l} = {1l}");
		add(Operators.CHAR_AT, "cast(substring({0},{1s}+1,1) as char)");
		add(Operators.STRING_IS_EMPTY, "length({0}) = 0");

		add(Operators.STRING_CONTAINS, "{0} like {%1%} escape '" + escape + "'");
		add(Operators.STRING_CONTAINS_IC, "{0l} like {%%1%%} escape '" + escape + "'");
		add(Operators.ENDS_WITH, "{0} like {%1} escape '" + escape + "'");
		add(Operators.ENDS_WITH_IC, "{0l} like {%%1} escape '" + escape + "'");
		add(Operators.STARTS_WITH, "{0} like {1%} escape '" + escape + "'");
		add(Operators.STARTS_WITH_IC, "{0l} like {1%%} escape '" + escape + "'");
		add(Operators.INDEX_OF, "locate({1},{0})-1");
		add(Operators.INDEX_OF_2ARGS, "locate({1},{0},{2s}+1)-1");

		// date time
		add(Operators.SYSDATE, "sysdate");
		add(Operators.CURRENT_DATE, "current_date");
		add(Operators.CURRENT_TIME, "current_time");
		add(Operators.CURRENT_TIMESTAMP, "current_timestamp");

		add(Operators.MILLISECOND, "0"); // NOT supported in HQL
		add(Operators.SECOND, "second({0})");
		add(Operators.MINUTE, "minute({0})");
		add(Operators.HOUR, "hour({0})");
		add(Operators.DAY_OF_MONTH, "day({0})");
		add(Operators.MONTH, "month({0})");
		add(Operators.YEAR, "year({0})");

		add(Operators.YEAR_MONTH, "year({0}) * 100 + month({0})");

		// path types
		add(AttributeRelationType.PROPERTY, "{0}.{1s}");
		add(AttributeRelationType.VARIABLE, "{0s}");

		// case for eq
		add(Operators.CASE_EQ, "case {1} end");
		add(Operators.CASE_EQ_WHEN, "when {0} = {1} then {2} {3}");
		add(Operators.CASE_EQ_ELSE, "else {0}");

		add(Operators.INSTANCE_OF, "type({0}) = {1}");
		// VER AQUI add(JPQLOperators.TYPE, "type({0})");

		// VER AQUI add(JPQLOperators.INDEX, "index({0})");

		// CHECKSTYLE:ON
	}

	public boolean wrapElements(Operator<?> operator) {
		return false;
	}

	public boolean isTypeAsString() {
		// TODO : get rid of this when Hibernate supports type(alias)
		return false;
	}

	public String getTypeForCast(Class<?> cl) {
		return cl.getSimpleName().toLowerCase();
	}

	public boolean isEnumInPathSupported() {
		return true;
	}

	public boolean isPathInEntitiesSupported() {
		return true;
	}

	public boolean isSelect1Supported() {
		return false;
	}

	public String getExistsProjection() {
		return null;
	}

	public boolean wrapConstant(Object constant) {
		// related : https://hibernate.onjira.com/browse/HHH-6913
		return false;
	}

	public boolean isWithForOn() {
		return false;
	}

	//VER AQUI public QueryHandler getQueryHandler() {
	//	return queryHandler;
	//}

}
