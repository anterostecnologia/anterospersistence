package br.com.anteros.persistence.sql.dialect.type;

import br.com.anteros.core.utils.StringUtils;

public class LimitClauseResult {

	public static int NONE_PARAMETER = 0;
	public static int FIRST_PARAMETER = 1;
	public static int SECOND_PARAMETER = 2;
	public static int LAST_PARAMETER = 3;
	public static int PREVIOUS_PARAMETER = 4;

	private String sql;
	private String parameterOffSet = "";
	private String parameterLimit = "";
	private boolean namedParameter;
	private int offSetParameterIndex = LAST_PARAMETER;
	private int limitParameterIndex = PREVIOUS_PARAMETER;
	private int limit;
	private int offset;

	public LimitClauseResult(String sql, String parameterLimit, String parameterOffSet, int limit, int offset) {
		this.sql = sql;
		this.parameterOffSet = parameterOffSet;
		this.parameterLimit = parameterLimit;
		this.namedParameter = ((!StringUtils.isEmpty(parameterOffSet)) || (!StringUtils.isEmpty(parameterLimit)));
		this.limit = limit;
		this.offset = offset;
	}

	public LimitClauseResult(String sql, int limitParameterIndex, int offSetParameterIndex, int limit, int offset) {
		this.sql = sql;
		this.namedParameter = false;
		this.offSetParameterIndex = offSetParameterIndex;
		this.limitParameterIndex = limitParameterIndex;
		this.limit = limit;
		this.offset = offset;
	}

	public String getSql() {
		return sql;
	}

	public String getParameterOffSet() {
		return parameterOffSet;
	}

	public String getParameterLimit() {
		return parameterLimit;
	}

	public boolean isNamedParameter() {
		return namedParameter;
	}

	public boolean isFirstParameters() {
		return (!namedParameter) && ((offSetParameterIndex >= 1) || (offSetParameterIndex >= 2));
	}

	public int getOffSetParameterIndex() {
		return offSetParameterIndex;
	}

	public int getLimitParameterIndex() {
		return limitParameterIndex;
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}

}
