package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

@SuppressWarnings("unchecked")
public class SingleValueHandler implements ResultSetHandler {

	private Class type;
	private String aliasColumnName;
	private DescriptionField descriptionField;

	public SingleValueHandler(Class type, DescriptionField descriptionField, String aliasColumName) {
		this.type = type;
		this.aliasColumnName = aliasColumName;
		this.descriptionField = descriptionField;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<?> handle(ResultSet rs) throws Exception {
		List result = null;
		if (rs.next()) {
			result = new ArrayList();
			do {
				Object value = rs.getObject(aliasColumnName);
				if (descriptionField.hasConverts())
					value = descriptionField.getSimpleColumn().convertToEntityAttribute(value);
				else
					value = ObjectUtils.convert(value, type);
				result.add(value);
			} while (rs.next());
		}
		return result;
	}
}
