package br.com.anteros.persistence.session.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.EntityHandlerException;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.proxy.collection.DefaultSQLList;
import br.com.anteros.persistence.proxy.collection.DefaultSQLSet;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

public class CollectionExpressionFieldMapper extends ExpressionFieldMapper {

	protected Class<?> defaultClass;
	private String aliasTable;
	private String aliasDiscriminatorColumnName;
	private boolean isAbstract;
	private String[] aliasPrimaryKeyColumns;

	public CollectionExpressionFieldMapper(EntityCache targetEntityCache, DescriptionField descriptionField, String aliasTable,
			String aliasDiscriminatorColumnName, String[] aliasPrimaryKeyColumns) {
		super(targetEntityCache, descriptionField, "");
		this.aliasTable = aliasTable;
		this.aliasDiscriminatorColumnName = aliasDiscriminatorColumnName;
		this.isAbstract = !StringUtils.isEmpty(aliasDiscriminatorColumnName);
		this.aliasPrimaryKeyColumns = aliasPrimaryKeyColumns;

		if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class)) {
			defaultClass = DefaultSQLSet.class;
		} else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class)) {
			defaultClass = DefaultSQLList.class;
		}

	}

	@Override
	public void execute(SQLSession session, ResultSet resultSet, EntityManaged entityManaged, Object targetObject, Cache transactionCache)
			throws Exception {

		Object newObject = null;
		Object oldTargetObject = targetObject;
		targetObject = descriptionField.getObjectValue(targetObject);
		if (targetObject == null) {
			targetObject = defaultClass.newInstance();
			descriptionField.setObjectValue(oldTargetObject, targetObject);
		}

		if (isAbstract) {
			try {
				String discriminator = resultSet.getString(aliasDiscriminatorColumnName);
				EntityCache concreteEntityCache = session.getEntityCacheManager().getEntityCache(descriptionField.getTargetClass(), discriminator);
				String uniqueId = getUniqueId(resultSet);

				if (uniqueId == null)
					return;

				newObject = getObjectFromCache(session, concreteEntityCache, uniqueId, transactionCache);
				if (newObject == null) {
					newObject = concreteEntityCache.getEntityClass().newInstance();
					addObjectToCache(session, concreteEntityCache, newObject, uniqueId, transactionCache);

					if (targetObject instanceof Collection)
						((Collection) targetObject).add(newObject);
				}
			} catch (Exception e) {
				throw new EntityHandlerException("Para que seja criado o objeto da " + descriptionField.getTargetClass().getName()
						+ " será necessário adicionar no sql a coluna " + targetEntityCache.getDiscriminatorColumn().getColumnName()
						+ " que informe que tipo de classe será usada para instanciar o objeto.");
			}

		} else {
			if (targetEntityCache != null) {
				String uniqueId = getUniqueId(resultSet);
				if (uniqueId == null)
					return;

				newObject = getObjectFromCache(session, targetEntityCache, uniqueId, transactionCache);
				if (newObject == null) {
					newObject = targetEntityCache.getEntityClass().newInstance();
					addObjectToCache(session, targetEntityCache, newObject, uniqueId, transactionCache);

					if (targetObject instanceof Collection)
						((Collection) targetObject).add(newObject);
				}
				session.getPersistenceContext().addEntityManaged(newObject, true, false);
			} else {
				newObject = descriptionField.getTargetClass().newInstance();
				/*
				 * Adiciona o campo na lista de campos que poderão ser alterados. Se o campo não for buscado no select
				 * não poderá ser alterado.
				 */
				entityManaged.getFieldsForUpdate().add(descriptionField.getField().getName());
			}
		}

		for (ExpressionFieldMapper expField : children) {
			expField.execute(session, resultSet, entityManaged, newObject, transactionCache);
		}
	}

	protected String getUniqueId(ResultSet resultSet) throws SQLException {
		int index;
		StringBuilder uniqueIdTemp = new StringBuilder("");
		boolean appendSeparator = false;
		for (String aliasColumnName : aliasPrimaryKeyColumns) {
			index = resultSet.findColumn(aliasColumnName);
			if (index < 0) {
				throw new SQLException("NÃO ACHOU COLUNA " + aliasColumnName);
			}
			if (appendSeparator)
				uniqueIdTemp.append("_");
			uniqueIdTemp.append(resultSet.getObject(index));
			appendSeparator = true;
		}
		String result = uniqueIdTemp.toString();
		if (result.equals("null"))
			return null;
		return result;
	}

	@Override
	public String toString(int level) {
		StringBuilder sb = new StringBuilder(StringUtils.repeat(" ", level * 4) + descriptionField.getField().getName() + " -> "
				+ targetEntityCache.getEntityClass().getSimpleName() + " : " + aliasColumnName
				+ (aliasDiscriminatorColumnName == "" ? "" : " discriminator column " + aliasDiscriminatorColumnName));
		level = level + 1;
		for (ExpressionFieldMapper expressionFieldMapper : children) {
			sb.append("\n").append(expressionFieldMapper.toString(level));
		}
		return sb.toString();
	}

	public Class<?> getDefaultClass() {
		return defaultClass;
	}

	public void setDefaultClass(Class<?> defaultClass) {
		this.defaultClass = defaultClass;
	}

	public String getAliasTable() {
		return aliasTable;
	}

	public void setAliasTable(String aliasTable) {
		this.aliasTable = aliasTable;
	}

	public String getAliasDiscriminatorColumnName() {
		return aliasDiscriminatorColumnName;
	}

	public void setAliasDiscriminatorColumnName(String aliasDiscriminatorColumnName) {
		this.aliasDiscriminatorColumnName = aliasDiscriminatorColumnName;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public String[] getAliasPrimaryKeyColumns() {
		return aliasPrimaryKeyColumns;
	}

	public void setAliasPrimaryKeyColumns(String[] aliasPrimaryKeyColumns) {
		this.aliasPrimaryKeyColumns = aliasPrimaryKeyColumns;
	}

}
