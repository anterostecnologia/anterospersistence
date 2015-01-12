package br.com.anteros.persistence.session.query;

import java.sql.ResultSet;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

public class SimpleExpressionFieldMapper extends ExpressionFieldMapper {

	public SimpleExpressionFieldMapper(EntityCache targetEntityCache, DescriptionField descriptionField, String aliasColumnName) {
		super(targetEntityCache, descriptionField, aliasColumnName);
	}

	@Override
	public void execute(SQLSession session, ResultSet resultSet, EntityManaged entityManaged, Object targetObject, Cache transactionCache)
			throws Exception {
		Object value = getValueByColumnName(resultSet);
		descriptionField.setObjectValue(targetObject, value);
		if (entityManaged.getStatus() != EntityStatus.READ_ONLY) {
			/*
			 * Guarda o valor na lista de valores anteriores
			 */
			FieldEntityValue fieldEntityValue = descriptionField.getSimpleColumn().getFieldEntityValue(targetObject);
			entityManaged.addOriginalValue(fieldEntityValue);
			entityManaged.addLastValue(fieldEntityValue);

			/*
			 * Adiciona o campo na lista de campos que poderão ser alterados. Se o campo não for buscado no select não
			 * poderá ser alterado.
			 */
			entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
		}
	}

}
