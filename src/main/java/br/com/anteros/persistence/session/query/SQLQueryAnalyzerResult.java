package br.com.anteros.persistence.session.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLQueryAnalyzerResult {

	private Set<SQLQueryAnalyserAlias> aliases;
	private List<ExpressionFieldMapper> expressionsFieldMapper;
	private Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases;
	private String parsedSql;
	private boolean allowApplyLockStrategy = false;
	
	public SQLQueryAnalyzerResult(String parsedSql, Set<SQLQueryAnalyserAlias> aliases,List<ExpressionFieldMapper> expressionsFieldMapper, Map<SQLQueryAnalyserAlias, Map<String, String[]>> columnAliases,boolean allowApplyLockStrategy) {
		this.aliases = aliases;
		this.columnAliases = columnAliases;
		this.parsedSql = parsedSql;
		this.expressionsFieldMapper = expressionsFieldMapper;
		this.allowApplyLockStrategy = allowApplyLockStrategy;
	}

	public Set<SQLQueryAnalyserAlias> getAliases() {
		return aliases;
	}

	public Map<SQLQueryAnalyserAlias, Map<String, String[]>> getColumnAliases() {
		return columnAliases;
	}

	public String getParsedSql() {
		return parsedSql;
	}

	public List<ExpressionFieldMapper> getExpressionsFieldMapper() {
		return expressionsFieldMapper;
	}

	public boolean isAllowApplyLockStrategy() {
		return allowApplyLockStrategy;
	}

}
