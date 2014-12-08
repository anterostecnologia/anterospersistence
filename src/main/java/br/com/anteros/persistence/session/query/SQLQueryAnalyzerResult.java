package br.com.anteros.persistence.session.query;

import java.util.Map;
import java.util.Set;

public class SQLQueryAnalyzerResult {

	private Set<SQLQueryAnalyserAlias> aliases;
	private Map<String, String> expressions;
	private Map<SQLQueryAnalyserAlias, Map<String, String>> columnAliases;
	private String parsedSql;
	
	public SQLQueryAnalyzerResult(String parsedSql, Set<SQLQueryAnalyserAlias> aliases, Map<String, String> expressions, Map<SQLQueryAnalyserAlias, Map<String, String>> columnAliases) {
		this.aliases = aliases;
		this.expressions = expressions;
		this.columnAliases = columnAliases;
		this.parsedSql = parsedSql;
	}

	public Set<SQLQueryAnalyserAlias> getAliases() {
		return aliases;
	}

	public Map<String, String> getExpressions() {
		return expressions;
	}

	public Map<SQLQueryAnalyserAlias, Map<String, String>> getColumnAliases() {
		return columnAliases;
	}

	public String getParsedSql() {
		return parsedSql;
	}

}
