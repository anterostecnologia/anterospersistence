package br.com.anteros.persistence.session.lock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.persistence.sql.command.Select;

public class LockManagerJDBC implements LockManager {

	@Override
	public void lock(SQLSession session, Object entity, LockOptions lockOptions) throws Exception {
		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(entity);
		if (entityManaged == null) {
			throw new LockException(
					"Entidade não está sendo gerenciada não é possível realizar o lock. Verifique se o objeto é novo ou se foi criado pela sessão.");
		}

		if (entityManaged.getStatus() == EntityStatus.READ_ONLY) {
			throw new LockException("Entidade somente leitura não é possível realizar o lock. ");
		}

		if (entityManaged.getStatus() == EntityStatus.DELETED) {
			throw new LockException("Entidade já foi deletada não é possível realizar o lock. ");
		}

		if (lockOptions.getLockMode().greaterThan(entityManaged.getLockMode())) {
			EntityCache entityCache = session.getEntityCacheManager().getEntityCache(entity.getClass());
			if (entityCache == null) {
				throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
			}

			validateLockOptions(lockOptions, entityCache);
			Identifier<Object> identifier = Identifier.create(session, entity, true);

			SQLQuery query = makeQuerySingleRecordLock(session, lockOptions, identifier);
			ResultSet resultSet = null;
			try {
				resultSet = query.executeQuery();
				if (resultSet.next()) {
					entityManaged.setLockMode(lockOptions.getLockMode());
				}
			} catch (SQLException ex) {
				throw session.getDialect().convertSQLException(ex, "Não foi possível obter o lock para entidade "+entityCache.getEntityClass().getSimpleName()+" Id " +identifier.getValues()+".", query.getSql());
			} finally {
				if (resultSet != null)
					resultSet.close();
			}
		}
	}

	protected void validateLockOptions(LockOptions lockOptions, EntityCache entityCache) {
		if ((!entityCache.isVersioned())
				&& (lockOptions.contains(LockMode.OPTIMISTIC, LockMode.OPTIMISTIC_FORCE_INCREMENT, LockMode.PESSIMISTIC_FORCE_INCREMENT))) {
			throw new LockException(
					"Tipo de lock ["
							+ lockOptions.getLockMode()
							+ "] inválido para a entidade pois ela não possue um controle de versão. Somente um lock do tipo PESSIMISTA poderá ser usado em entidades sem controle de versão. Classe "
							+ entityCache.getEntityClass());
		}
	}

	protected SQLQuery makeQuerySingleRecordLock(SQLSession session, LockOptions lockOptions, Identifier<Object> identifier) throws Exception {
		Select select = new Select(session.getDialect());
		select.addTableName(identifier.getEntityCache().getTableName());
		Map<String, Object> columns = identifier.getColumns();
		List<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean appendOperator = false;
		for (String column : columns.keySet()) {
			if (appendOperator)
				select.and();
			select.addColumn(column);
			select.addCondition(column, "=", ":P" + column);
			params.add(new NamedParameter("P" + column, columns.get(column)));
			appendOperator = true;
		}
		String sql = select.toString();
		sql = session.getDialect().applyLock(sql, lockOptions);
		SQLQuery query = session.createQuery(sql, lockOptions);
		query.setParameters(params);
		query.setReadOnly(true);
		return query;
	}
	
	@Override
	public String applyLock(SQLSession session, String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception {
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
		validateLockOptions(lockOptions, entityCache);
		return session.getDialect().applyLock(sql, lockOptions);
	}

}
