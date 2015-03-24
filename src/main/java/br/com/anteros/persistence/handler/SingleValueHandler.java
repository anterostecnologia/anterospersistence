package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;

@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class SingleValueHandler implements ScrollableResultSetHandler {

	private Class type;
	private String aliasColumnName;
	private DescriptionField descriptionField;
	private int columIndex;

	public SingleValueHandler(Class type, DescriptionField descriptionField, String aliasColumName, int columnIndex) {
		this.type = type;
		this.aliasColumnName = aliasColumName;
		this.descriptionField = descriptionField;
		this.columIndex = columnIndex;
	}

	@Override
	public List<?> handle(ResultSet rs) throws Exception {
		List result = null;
		if (rs.next()) {
			result = new ArrayList();
			do {
				Object value = readRow(rs);
				result.add(value);
			} while (rs.next());
		}
		return result;
	}

	protected Object readRow(ResultSet rs) throws SQLException, Exception {
		Object value = rs.getObject(columIndex);
		if (descriptionField.hasConverts())
			value = descriptionField.getSimpleColumn().convertToEntityAttribute(value);
		else
			value = ObjectUtils.convert(value, type);
		return value;
	}

	@Override
	public Object[] readCurrentRow(ResultSet resultSet) throws Exception {
		Object value = readRow(resultSet);
		return new Object[] { value };
	}
}
