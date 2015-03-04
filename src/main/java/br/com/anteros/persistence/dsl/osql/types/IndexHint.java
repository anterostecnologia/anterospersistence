package br.com.anteros.persistence.dsl.osql.types;

public class IndexHint {

	private String alias;
	private String indexName;
	
	public IndexHint(EntityPath<?> alias, String indexName) {
		this(alias.getMetadata().getName(),indexName);
	}
	
	public IndexHint(String alias, String indexName) {
		this.alias = alias;
		this.indexName = indexName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	
	

}
