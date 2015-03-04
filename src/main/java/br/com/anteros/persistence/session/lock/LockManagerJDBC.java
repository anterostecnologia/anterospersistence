/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
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

/**
 * Implementação do gerenciador de bloqueios para JDBC.
 * 
 * @author edson
 *
 */
public class LockManagerJDBC implements LockManager {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void lock(SQLSession session, Object entity, LockOptions lockOptions) throws Exception {
		if (entity == null) {
			throw new LockException("Não é possível realizar o travamento em objetos nulos.");
		}

		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(entity);
		if (entityManaged == null) {
			throw new LockException(
					"Entidade não está sendo gerenciada não é possível realizar o bloqueio. Verifique se o objeto é novo ou se foi criado pela sessão.");
		}

		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(entity.getClass());
		if (entityCache == null) {
			throw new SQLSessionException("Classe não foi encontrada na lista de entidades gerenciadas. " + entity.getClass().getName());
		}

		/*
		 * Obtém o identificador da entidade
		 */
		Identifier<Object> identifier = Identifier.create(session, entity, true);

		if (entityManaged.getStatus() == EntityStatus.READ_ONLY) {
			throw new LockException("Entidade " + entityManaged.getEntityCache().getEntityClass().getSimpleName()
					+ " somente leitura não é possível realizar o bloqueio. Id " + identifier.getDatabaseValues());
		}

		if (entityManaged.getStatus() == EntityStatus.DELETED) {
			throw new LockException("Entidade " + entityManaged.getEntityCache().getEntityClass().getSimpleName()
					+ " já foi deletada não é possível realizar o travamento. " + identifier.getDatabaseValues());
		}

		/*
		 * Somente realiza o bloqueio se o LockMode for superior ao atual
		 */
		if (lockOptions.getLockMode().greaterThan(entityManaged.getLockMode())) {
			/*
			 * Realiza a validação da estratégia de bloqueio a ser usada para realizar o travamento da entidade.
			 */
			validateLockOptions(session, lockOptions, entityCache.getEntityClass());

			/*
			 * Realiza um consulta no banco de dados para realizar o travamento somente se a estratégia for pessimista
			 */
			if (lockOptions.contains(LockMode.PESSIMISTIC_FORCE_INCREMENT, LockMode.PESSIMISTIC_READ, LockMode.PESSIMISTIC_WRITE)) {
				/*
				 * Cria a consulta para realizar o travamento pessimista
				 */
				SQLQuery query = makeQuerySingleRecordLock(session, lockOptions, identifier);
				ResultSet resultSet = null;
				try {
					resultSet = query.executeQuery();
					if (resultSet.next()) {
						entityManaged.setLockMode(lockOptions.getLockMode());
					} else
						throw new LockAcquisitionException("Entidade " + entityManaged.getEntityCache().getEntityClass().getSimpleName()
								+ " não foi localizada no banco de dados e por isso não foi possível realizar o bloqueio. Id "
								+ identifier.getDatabaseValues());
				} catch (SQLException ex) {
					throw session.getDialect().convertSQLException(
							ex,
							"Não foi possível realizar o bloqueio da entidade " + entityCache.getEntityClass().getSimpleName() + " Id "
									+ identifier.getDatabaseValues() + ".", query.getSql());
				} finally {
					if (resultSet != null)
						resultSet.close();
				}
			} else {
				/*
				 * No caso de travamento otimista apenas altera o LockMode na entidade gerenciada no contexto da sessão.
				 */
				entityManaged.setLockMode(lockOptions.getLockMode());
			}
		}
	}

	/**
	 * Valida se é possível aplicar a estratégia de bloqueio para entidade.
	 * 
	 * @param lockOptions
	 *            Opções de bloqueio.
	 * @param entityCache
	 *            Entidade
	 */
	protected void validateLockOptions(SQLSession session, LockOptions lockOptions, Class<?> resultClass) {
		if (lockOptions == null)
			return;
		
		if (resultClass == null) {
			if (lockOptions.contains(LockMode.OPTIMISTIC, LockMode.OPTIMISTIC_FORCE_INCREMENT, LockMode.PESSIMISTIC_FORCE_INCREMENT)) {
				throw new LockException(
						"Tipo de travamento ["
								+ lockOptions.getLockMode()
								+ "] inválido para o sql. Somente um bloqueio do tipo PESSIMISTA poderá ser usado em seleções sem classe de resultado ou de várias entidades diferentes.");
			}
		} else {
			EntityCache entityCache = session.getEntityCacheManager().getEntityCache(resultClass);
			if (entityCache != null) {
				if ((!entityCache.isVersioned())
						&& (lockOptions.contains(LockMode.OPTIMISTIC, LockMode.OPTIMISTIC_FORCE_INCREMENT, LockMode.PESSIMISTIC_FORCE_INCREMENT))) {
					throw new LockException(
							"Tipo de travamento ["
									+ lockOptions.getLockMode()
									+ "] inválido para a entidade pois ela não possue um controle de versão. Somente um bloqueio do tipo PESSIMISTA poderá ser usado em entidades sem controle de versão. Classe "
									+ entityCache.getEntityClass());
				}
			}
		}
	}

	/**
	 * Cria a query que irá executar o bloqueio pessimista no banco de dados para a entidade usando o Id para travar o
	 * registro.
	 * 
	 * @param session
	 *            Sessão que está gerenciando a entidade.
	 * @param lockOptions
	 *            Opções de bloqueio.
	 * @param identifier
	 *            Identificador da entidade
	 * @return Consulta {@link SQLQuery}
	 * @throws Exception
	 *             Retorna um erro caso não consigo criar a consulta(query).
	 */
	protected SQLQuery makeQuerySingleRecordLock(SQLSession session, LockOptions lockOptions, Identifier<Object> identifier) throws Exception {
		/*
		 * Monta a instrução SQL para realizar o travamento da Entidade no banco de dados
		 */
		Select select = new Select(session.getDialect());
		select.addTableName(identifier.getEntityCache().getTableName());
		Map<String, Object> columns = identifier.getDatabaseColumns();
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
		/*
		 * Cria a consulta e atribui a estratégia de bloqueio
		 */
		SQLQuery query = session.createQuery(session.getDialect().applyLock(select.toStatementString(), lockOptions), lockOptions);
		query.setParameters(params);
		query.setReadOnly(true);
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String applyLock(SQLSession session, String sql, Class<?> resultClass, LockOptions lockOptions) throws Exception {
		if (lockOptions == null)
			return sql;
		validateLockOptions(session, lockOptions, resultClass);
		return session.getDialect().applyLock(sql, lockOptions);
	}

}
