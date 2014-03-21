package br.com.anteros.persistence.osql.attribute;

import br.com.anteros.persistence.osql.Operator;

public enum AttributeRelationType implements Operator<Object> {
	ARRAYVALUE, ARRAYVALUE_CONSTANT, COLLECTION_ANY, DELEGATE, LISTVALUE, LISTVALUE_CONSTANT, MAPVALUE, MAPVALUE_CONSTANT, PROPERTY, VARIABLE;

	@Override
	public String getId() {
		return name();
	}

}
