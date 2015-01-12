package br.com.anteros.persistence.session.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.handler.EntityHandlerException;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.annotation.type.ScopeType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.Cache;

public abstract class ExpressionFieldMapper {

	protected EntityCache targetEntityCache;
	protected DescriptionField descriptionField;
	protected String aliasColumnName;
	protected List<ExpressionFieldMapper> children = new ArrayList<ExpressionFieldMapper>();

	public ExpressionFieldMapper(EntityCache targetEntityCache,
			DescriptionField descriptionField, String aliasColumnName) {
		this.targetEntityCache = targetEntityCache;
		this.descriptionField = descriptionField;
		this.aliasColumnName = aliasColumnName;
	}

	public abstract void execute(SQLSession session, ResultSet resultSet, EntityManaged entityManaged, Object targetObject, Cache transactionCache) throws Exception;



	public EntityCache getTargetEntityCache() {
		return targetEntityCache;
	}

	public void setTargetEntityCache(EntityCache targetEntityCache) {
		this.targetEntityCache = targetEntityCache;
	}

	public DescriptionField getDescriptionField() {
		return descriptionField;
	}

	public void setDescriptionField(DescriptionField descriptionField) {
		this.descriptionField = descriptionField;
	}

	public String getAliasColumnName() {
		return aliasColumnName;
	}

	public void setAliasColumnName(String aliasColumnName) {
		this.aliasColumnName = aliasColumnName;
	}


	public List<ExpressionFieldMapper> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public ExpressionFieldMapper addChild(ExpressionFieldMapper child){
		children.add(child);
		return this;
	}

	public ExpressionFieldMapper getExpressionFieldByName(String name) {
		for (ExpressionFieldMapper child : children){
			if (child.getDescriptionField().getField().getName().equalsIgnoreCase(name)){
				return child;
			}
		}
		return null;
	}

	public String toString(int level){
		StringBuilder sb = new StringBuilder(StringUtils.repeat(" ", level*4) + descriptionField.getField().getName()+" -> "+targetEntityCache.getEntityClass().getSimpleName()+" : "+aliasColumnName);
		level = level + 1;
		for (ExpressionFieldMapper expressionFieldMapper : children){
			sb.append("\n").append(expressionFieldMapper.toString(level));
		}
		return sb.toString();
	}
	
	protected Object getValueByColumnName(ResultSet resultSet) throws EntityHandlerException {
		try {
			Object value = resultSet.getObject(aliasColumnName);
			if ((value instanceof Date) || (value instanceof java.sql.Date))
				value = resultSet.getTimestamp(aliasColumnName);
			return value;
		} catch (SQLException ex) {
			throw new EntityHandlerException("Erro processando campo " + descriptionField.getField().getName()
					+ " na classe " + targetEntityCache.getEntityClass().getName() + " coluna " + aliasColumnName
					+ ". " + ex.getMessage());
		}
	}

	protected Object getObjectFromCache(SQLSession session, EntityCache targetEntityCache, String uniqueId, Cache transactionCache) {
		Object result = null;
		/*
		 * Se a classe for abstrata pega todas as implementações não abstratas e
		 * verifica se existe um objeto da classe + ID no entityCache
		 */
		if (ReflectionUtils.isAbstractClass(targetEntityCache.getEntityClass())) {
			EntityCache[] entitiesCache = session.getEntityCacheManager().getEntitiesBySuperClass(targetEntityCache);
			for (EntityCache entityCache : entitiesCache) {
				result = transactionCache.get(entityCache.getEntityClass().getName() + "_" + uniqueId);
				if (result != null)
					break;
				result = session.getPersistenceContext().getObjectFromCache(
						entityCache.getEntityClass().getName() + "_" + uniqueId);
				if (result != null)
					break;
			}
		} else {
			/*
			 * Caso não seja abstrata localiza classe+ID no entityCache
			 */
			result = transactionCache.get(targetEntityCache.getEntityClass().getName() + "_" + uniqueId);

			if (result == null)
				result = session.getPersistenceContext().getObjectFromCache(
						targetEntityCache.getEntityClass().getName() + "_" + uniqueId);
		}
		return result;
	}
	

	protected void addObjectToCache(SQLSession session, EntityCache entityCache, Object targetObject, String uniqueId, Cache transactionCache) {
		/*
		 * Adiciona o objeto no Cache da sessão ou da transação para evitar
		 * buscar o objeto novamente no mesmo processamento
		 */
		if ((entityCache.getCacheScope().equals(ScopeType.TRANSACTION)) && (transactionCache != null)) {
			transactionCache.put(entityCache.getEntityClass().getName() + "_" + uniqueId, targetObject,
					entityCache.getMaxTimeCache());
		} else {
			session.getPersistenceContext().addObjectToCache(entityCache.getEntityClass().getName() + "_" + uniqueId,
					targetObject, entityCache.getMaxTimeCache());
		}
	}
	
	@Override
	public String toString() {
		return (targetEntityCache==null?"":targetEntityCache.getEntityClass())+":"+descriptionField.getField().getName()+":"+aliasColumnName;
	}
	
}
