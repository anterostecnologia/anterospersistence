package br.com.anteros.persistence.parameter;

import br.com.anteros.persistence.schema.definition.type.StoredParameterType;

public class OutputNamedParameter extends NamedParameter {

	private int dataTypeSql;
	private StoredParameterType type;

	public OutputNamedParameter(String name, StoredParameterType type) {
		super(name);
		this.type = type;
		if (type == StoredParameterType.IN)
			throw new RuntimeException("Tipo de parâmetro de saída inválido. " + type);
	}
	
	public OutputNamedParameter(String name, StoredParameterType type, int dataTypeSql) {
		this(name,type);
		this.dataTypeSql = dataTypeSql;
	}

	public int getDataTypeSql() {
		return dataTypeSql;
	}

	public void setDataTypeSql(int sqlType) {
		this.dataTypeSql = sqlType;
	}

	public StoredParameterType getType() {
		return type;
	}

	public void setType(StoredParameterType type) {
		this.type = type;
	}

}
