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

package br.com.anteros.persistence.session.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityManaged;
import br.com.anteros.persistence.metadata.FieldEntityValue;
import br.com.anteros.persistence.metadata.annotation.type.CascadeType;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.descriptor.type.SQLStatementType;
import br.com.anteros.persistence.metadata.identifier.Identifier;
import br.com.anteros.persistence.metadata.identifier.IdentifierGenerator;
import br.com.anteros.persistence.metadata.identifier.IdentifierGeneratorFactory;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.metadata.type.EntityStatus;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.SQLPersister;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionValidatior;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.lock.LockMode;
import br.com.anteros.persistence.session.lock.OptimisticLockException;
import br.com.anteros.persistence.sql.command.CommandSQL;
import br.com.anteros.persistence.sql.command.Delete;
import br.com.anteros.persistence.sql.command.DeleteCommandSQL;
import br.com.anteros.persistence.sql.command.Insert;
import br.com.anteros.persistence.sql.command.InsertCommandSQL;
import br.com.anteros.persistence.sql.command.Select;
import br.com.anteros.persistence.sql.command.Update;
import br.com.anteros.persistence.sql.command.UpdateCommandSQL;
import br.com.anteros.persistence.util.AnterosBeanValidationHelper;
import br.com.anteros.persistence.validation.version.Versioning;

public class SQLPersisterImpl implements SQLPersister {

	private Logger LOG = LoggerProvider.getInstance().getLogger(SQLPersister.class);

	private SQLSession session;

	private SQLSessionValidatior validator;

	public Object save(SQLSession session, Object object) throws Exception {
		if (getValidator() != null)
			getValidator().validateBean(object);
		this.session = session;
		return save(object, null);
	}

	public void remove(SQLSession session, Object object) throws Exception {
		this.session = session;
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(object.getClass());

		if (entityCache == null) {
			throw new SQLSessionException("Objeto não pode ser removido pois a classe " + object.getClass().getName()
					+ " não foi localizada no Cache de Entidades.");
		}

		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(object);
		if (entityManaged == null) {
			if (session.getIdentifier(object).hasIdentifier())
				throw new SQLSessionException("Objeto não pode ser removido pois o mesmo não foi recuperado pela Sessão ou não possuí Chave(ID) definida.");
		}
		if ((entityManaged != null) && (EntityStatus.DELETED.equals(entityManaged.getStatus())))
			session.flush();
		List<CommandSQL> commands = getCommandsToDeleteObject(object, entityCache);
		if (commands != null) {
			entityManaged.setStatus(EntityStatus.DELETED);
			session.getCommandQueue().addAll(commands);
		}
	}

	public void remove(SQLSession session, Object[] objects) throws Exception {
		for (Object obj : objects)
			remove(session, obj);
	}

	protected Object save(Object object, List<CommandSQL> stackCommands) throws Exception {
		EntityCache entityCache = session.getEntityCacheManager().getEntityCache(object.getClass());
		if (entityCache == null)
			throw new SQLSessionException("Objeto não pode ser salvo pois a classe " + object.getClass().getName()
					+ " não foi localizada no Cache de Entidades.");

		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(object);

		/*
		 * Se o objeto estiver sendo gerenciado então faz o update no objeto
		 */
		if (entityManaged != null) {
			/*
			 * Se o objeto foi selecionado parcialmente não permite alteração
			 */
			if (entityManaged.getStatus().equals(EntityStatus.READ_ONLY))
				throw new SQLSessionException("Objeto " + object.getClass().getSuperclass() + " ID " + session.getIdentifier(object).getDatabaseColumns()
						+ " não pode ser salvo pois é somente para leitura.");

			saveUsingStatement(entityCache, SQLStatementType.UPDATE, object, stackCommands);
		} else {
			/*
			 * Se o objeto não estiver sendo gerenciado porém já possuí um ID será preciso verificar se o ID já existe
			 * no banco de dados
			 */
			if (session.getIdentifier(object).hasIdentifier()) {
				if (!existsRecordInDatabaseTable(entityCache.getTableName(), session.getIdentifier(object).getDatabaseColumns()))
					saveUsingStatement(entityCache, SQLStatementType.INSERT, object, stackCommands);
				else
					saveUsingStatement(entityCache, SQLStatementType.UPDATE, object, stackCommands);
			} else
				saveUsingStatement(entityCache, SQLStatementType.INSERT, object, stackCommands);
		}
		return object;
	}

	protected boolean existsRecordInDatabaseTable(String tableName, Map<String, Object> identifier) throws Exception {
		Select select = new Select(session.getDialect());
		select.addColumn("count(*)", "numRows");
		select.addTableName(tableName);
		ArrayList<NamedParameter> params = new ArrayList<NamedParameter>();
		boolean appendOperator = false;
		for (String key : identifier.keySet()) {
			Object value = identifier.get(key);
			if (appendOperator)
				select.and();
			select.addCondition(key, "=", ":P" + key);
			params.add(new NamedParameter("P" + key, value));
			appendOperator = true;
		}

		ResultSet rs = session.createQuery(select.toStatementString(), params.toArray(new NamedParameter[] {})).executeQuery();
		if (rs.next()) {
			boolean result = rs.getInt("numRows") > 0;
			rs.close();
			rs.getStatement().close();
			return result;
		}
		rs.close();
		return false;
	}

	protected void saveUsingStatement(EntityCache entityCache, SQLStatementType statement, Object object, List<CommandSQL> stackCommands) throws Exception {
		List<CommandSQL> commands = null;
		if (statement.equals(SQLStatementType.INSERT))
			commands = getCommandsToInsertObject(object, entityCache);
		else if (statement.equals(SQLStatementType.UPDATE))
			commands = getCommandsToUpdateObject(object, entityCache);
		else if (statement.equals(SQLStatementType.DELETE))
			commands = getCommandsToDeleteObject(object, entityCache);

		if (commands != null) {
			List<CommandSQL> commandsToAdd = new ArrayList<CommandSQL>();
			for (CommandSQL command : commands) {
				if (command instanceof InsertCommandSQL) {
					if (((InsertCommandSQL) command).getIdentifierPostInsert() != null)
						command.execute();
					else
						commandsToAdd.add(command);
				} else {
					commandsToAdd.add(command);
					command.setEntityManaged();
				}
			}
			if (stackCommands != null)
				stackCommands.addAll(commandsToAdd);
			else
				session.getCommandQueue().addAll(commandsToAdd);
		}
	}

	protected List<CommandSQL> getCommandsToInsertObject(Object targetObject, EntityCache entityCache) throws Exception {
		List<CommandSQL> result = new ArrayList<CommandSQL>();
		try {
			LinkedHashMap<String, NamedParameter> namedParameters = new LinkedHashMap<String, NamedParameter>();
			IdentifierPostInsert identifierPostInsert = null;
			DescriptionColumn identifyColumn = null;
			insertParametersKey(targetObject, entityCache, namedParameters);
			insertCommonsParameters(targetObject, entityCache, namedParameters);
			insertRelationships(targetObject, entityCache);
			insertObject(targetObject, entityCache, result, namedParameters, identifierPostInsert, identifyColumn);
			insertChildrenCollections(targetObject, entityCache, result, identifierPostInsert, identifyColumn, session.getIdentifier(targetObject)
					.getDatabaseColumns());
		} finally {
			session.getCacheIdentifier().remove(targetObject);
		}
		return result;
	}

	protected void insertObject(Object targetObject, EntityCache entityCache, List<CommandSQL> result, LinkedHashMap<String, NamedParameter> namedParameters,
			IdentifierPostInsert identifierPostInsert, DescriptionColumn identifyColumn) {
		/*
		 * Salva o objeto
		 */
		if (entityCache.hasDiscriminatorColumn())
			namedParameters.put(entityCache.getDiscriminatorColumn().getColumnName(), new NamedParameter(entityCache.getDiscriminatorColumn().getColumnName(),
					entityCache.getDiscriminatorValue()));

		result.add(new InsertCommandSQL(session, generateSql(entityCache.getTableName(), SQLStatementType.INSERT,
				NamedParameter.convertToList(namedParameters.values())), NamedParameter.convertToList(namedParameters.values()), targetObject, entityCache,
				entityCache.getTableName(), session.isShowSql(), identifierPostInsert, identifyColumn, entityCache
						.getDescriptionSqlByType(SQLStatementType.INSERT)));
	}

	protected void insertRelationships(Object targetObject, EntityCache entityCache) throws Exception {
		Object fieldValue;
		/*
		 * Salva os relacionamentos FK
		 */
		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			/*
			 * Se for um RelationShip
			 */
			fieldValue = descriptionField.getObjectValue(targetObject);
			if (fieldValue != null) {
				if (descriptionField.isRelationShip())
					saveRelationShip(fieldValue, descriptionField);
			}
		}
	}

	protected void insertChildrenCollections(Object targetObject, EntityCache entityCache, List<CommandSQL> result, IdentifierPostInsert identifierPostInsert,
			DescriptionColumn identifyColumn, Map<String, Object> primaryKeyOwner) throws Exception, IllegalAccessException {
		Object fieldValue;
		/*
		 * Salva as coleções filhas do objeto
		 */
		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			/*
			 * Se for uma coleção ou joinTable (muitos para muitos)
			 */
			fieldValue = descriptionField.getObjectValue(targetObject);
			if (fieldValue != null) {
				if (descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable()) {
					if (descriptionField.isMapTable()) {
						if (fieldValue instanceof Map) {
							for (Object key : ((Map<?, ?>) fieldValue).keySet()) {
								Object value = ((Map<?, ?>) fieldValue).get(key);
								result.addAll(getSQLMapTableCommands(key, value, SQLStatementType.INSERT, descriptionField, identifyColumn,
										identifierPostInsert, primaryKeyOwner));
							}
						}
					} else if (descriptionField.isCollectionTable()) {
						for (Object value : ((Collection<?>) fieldValue))
							result.addAll(getSQLCollectionTableCommands(value, SQLStatementType.INSERT, descriptionField, identifyColumn, identifierPostInsert,
									primaryKeyOwner));
					} else if (descriptionField.isJoinTable()) {
						for (Object value : ((Collection<?>) fieldValue))
							result.addAll(getSQLJoinTableCommands(value, SQLStatementType.INSERT, descriptionField, identifyColumn, identifierPostInsert,
									primaryKeyOwner));
					} else if (descriptionField.isCollectionEntity()) {
						if (fieldValue instanceof Collection) {
							for (Object value : ((Collection<?>) fieldValue)) {
								if (value != null) {
									if ((descriptionField.getMappedBy() != null) && (!"".equals(descriptionField.getMappedBy()))) {
										Field mappedByField = descriptionField.getTargetEntity().getDescriptionField(descriptionField.getMappedBy()).getField();
										mappedByField.set(value, targetObject);
									}
									if ((Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.ALL) || Arrays.asList(
											descriptionField.getCascadeTypes()).contains(CascadeType.SAVE)))
										save(value, result);
								}
							}
						}
					}
				}
			}
		}
	}

	protected void insertParametersKey(Object targetObject, EntityCache entityCache, LinkedHashMap<String, NamedParameter> namedParameters) throws Exception {
		IdentifierPostInsert identifierPostInsert = null;
		DescriptionColumn identifyColumn = null;
		/*
		 * Gera os campos da chave do insert
		 */
		NamedParameter namedParameterField = null;
		for (DescriptionField fieldModified : entityCache.getDescriptionFields()) {
			if (!fieldModified.isAnyCollectionOrMap() && !fieldModified.isVersioned() && !fieldModified.isJoinTable()) {
				for (DescriptionColumn columnModified : fieldModified.getDescriptionColumns()) {
					if (columnModified.isPrimaryKey()) {
						namedParameterField = fieldModified.getNamedParameterFromDatabaseObjectValue(session, targetObject, columnModified);
						/*
						 * Se a coluna possuí um Generator e o valor ainda não foi gerado
						 */
						if ((columnModified.hasGenerator()) && (namedParameterField.getValue() == null)) {
							IdentifierGenerator identifierGenerator = IdentifierGeneratorFactory.createGenerator(session, columnModified);
							/*
							 * Se o Generator for IDENTIY será gerado após a execução do INSERT pelo getGeneratedKeys
							 * nativo JDBC
							 */
							if (identifierGenerator instanceof IdentifierPostInsert) {
								identifierPostInsert = (IdentifierPostInsert) identifierGenerator;
								identifyColumn = columnModified;
								Map<DescriptionColumn, IdentifierPostInsert> identifierTemp = new LinkedHashMap<DescriptionColumn, IdentifierPostInsert>();
								identifierTemp.put(identifyColumn, identifierPostInsert);
								session.getCacheIdentifier().put(targetObject, identifierTemp);
							} else {
								/*
								 * Gera o próximo número da sequência
								 */
								namedParameterField.setValue(identifierGenerator.generate());
								/*
								 * Seta o valor no objeto
								 */
								ReflectionUtils.setObjectValueByFieldName(targetObject, columnModified.getField().getName(), namedParameterField.getValue());
							}
						}
						if ((namedParameterField.getValue() == null) && (identifierPostInsert == null))
							throw new SQLSessionException("Coluna chave " + columnModified.getColumnName() + " da tabela " + entityCache.getTableName()
									+ " deve ter um valor. Verifique se o Campo " + columnModified.getField().getName() + " da classe "
									+ entityCache.getEntityClass() + " possui anotação para geração do identificador.");
						namedParameters.put(columnModified.getColumnName(), namedParameterField);
					}
				}
			}
		}
	}

	protected void insertCommonsParameters(Object targetObject, EntityCache entityCache, LinkedHashMap<String, NamedParameter> namedParameters)
			throws Exception {
		/*
		 * Gera os demais campos do insert
		 */
		for (DescriptionField fieldModified : entityCache.getDescriptionFields()) {
			if (!fieldModified.isAnyCollectionOrMap() && !fieldModified.isVersioned() && !fieldModified.isJoinTable()) {
				if ((fieldModified.getObjectValue(targetObject) != null) || (fieldModified.hasGenerator())) {
					for (DescriptionColumn columnModified : fieldModified.getDescriptionColumns()) {
						if (!columnModified.isPrimaryKey()) {
							namedParameters.put(columnModified.getColumnName(),
									fieldModified.getNamedParameterFromDatabaseObjectValue(session, targetObject, columnModified));
						}
					}
				}
			}
		}
	}

	protected List<CommandSQL> getCommandsToUpdateObject(Object targetObject, EntityCache entityCache) throws Exception {
		List<DescriptionField> fieldsModified = entityCache.getDescriptionFieldsExcludingIds();
		boolean hasFieldsModified = true;
		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(targetObject);

		if (entityManaged != null) {
			if (!entityCache.isExistsDescriptionSQL()) {
				List<DescriptionField> fieldModified = entityCache.getFieldsModified(session, targetObject);
				fieldsModified = fieldModified;
				hasFieldsModified = (fieldsModified != null) && (fieldsModified.size() > 0);
			}
		}

		ArrayList<NamedParameter> namedParameters = new ArrayList<NamedParameter>();
		List<CommandSQL> result = new ArrayList<CommandSQL>();

		updateRelationships(targetObject, entityCache);

		if ((hasFieldsModified)
				|| ((entityManaged != null) && (entityManaged.containsLockMode(LockMode.OPTIMISTIC_FORCE_INCREMENT, LockMode.PESSIMISTIC_FORCE_INCREMENT,
						LockMode.WRITE)))) {
			if (hasFieldsModified)
				updateCommonsParameters(targetObject, entityCache, fieldsModified, namedParameters);

			Object oldVersion = updateVersion(targetObject, entityCache, namedParameters, entityManaged);
			updateParametersKey(targetObject, entityCache, namedParameters);
			updateObject(targetObject, entityCache, namedParameters, result, oldVersion);
		} else if ((entityManaged != null) && (entityManaged.containsLockMode(LockMode.OPTIMISTIC, LockMode.READ))) {
			if (entityCache.isVersioned()) {
				Identifier<Object> identifier = session.getIdentifier(targetObject);
				Map<String, Object> params = identifier.getDatabaseColumnsValues();
				params.put(entityCache.getVersionColumnName(), entityManaged.getOldVersion());
				if (!existsRecordInDatabaseTable(entityCache.getTableName(), params)) {
					throw new OptimisticLockException("Não foi possível alterar a Entidade " + entityCache.getSimpleName()
							+ " pois foi a mesma não foi encontrada ou possui uma nova versão. Id " + identifier.getDatabaseValues() + " Versão "
							+ entityManaged.getOldVersion());
				}
			}
		}

		updateRelationshipsOnCollectionFields(targetObject, entityCache, result, entityManaged, session.getIdentifier(targetObject).getDatabaseColumns());

		return result;
	}

	protected void updateRelationshipsOnCollectionFields(Object targetObject, EntityCache entityCache, List<CommandSQL> result, EntityManaged entityManaged,
			Map<String, Object> primaryKeyOwner) throws IllegalAccessException, Exception {
		FieldEntityValue[] sourceList = null;
		FieldEntityValue[] targetList = null;
		FieldEntityValue lastFieldEntityValue = null;
		FieldEntityValue newFieldEntityValue = null;
		Object newColumnValue;
		/*
		 * Salva as chaves estrangeiras que foram alteradas e que possuam CascadeType = ALL ou SAVE. Se foi adicionado
		 * ou removido filhos nas coleções gera DMLObject dos itens para execução posterior Se for um objeto gerenciado
		 * compara lastValues com newValues e gera insert,update,delete
		 */
		if (entityManaged != null) {
			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				/*
				 * Se for uma coleção de entidades ou relacionamento joinTable (muitos para muitos)
				 */
				if (descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable()) {
					newColumnValue = descriptionField.getField().get(targetObject);
					if ((session.isProxyObject(newColumnValue)) && (!session.proxyIsInitialized(newColumnValue))) {
						continue;
					}

					lastFieldEntityValue = entityCache.getLastFieldEntityValue(session, targetObject, descriptionField.getName());
					newFieldEntityValue = descriptionField.getFieldEntityValue(session, targetObject);
					sourceList = null;
					targetList = null;
					if (lastFieldEntityValue != null)
						sourceList = (FieldEntityValue[]) lastFieldEntityValue.getValue();
					if (newFieldEntityValue != null)
						targetList = (FieldEntityValue[]) newFieldEntityValue.getValue();
					/*
					 * Gera insert ou delete de item na coleção caso o field tenha sido anotado com Cascade = ALL, SAVE
					 * ou DELETE_ORPHAN
					 */
					boolean nullValues = (lastFieldEntityValue == null) && (newFieldEntityValue == null);
					if (!nullValues) {
						/*
						 * Gera update dos itens da coleção caso o field tenha sido anotado com Cascade = ALL ou SAVE
						 */
						updateChangedItems(sourceList, targetList, descriptionField);

						if (((sourceList == null) && (targetList != null)) || ((sourceList != null) && (targetList == null))
								|| (lastFieldEntityValue.compareTo(newFieldEntityValue) != 0)) {
							/*
							 * Se existe em A(old) e não existe em B(new) gera delete e se for entidade delete somente
							 * se Cascade=DELETE_ORPHAN
							 */
							deleteItensRemoved(result, primaryKeyOwner, sourceList, targetList, descriptionField);

							/*
							 * Se existe em B(new) e não existe em A(old) gera insert se Cascade=ALL ou SAVE
							 */
							insertNewItens(result, primaryKeyOwner, sourceList, targetList, descriptionField);
						}
					}
				}
			}
		} else {
			/*
			 * Se for um objeto não gerenciado deleta as listas (list,map) e insere novamente. Caso seja uma lista de
			 * entidades insere ou atualiza os itens da lista.
			 */
			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				/*
				 * Se for uma coleção de entidades ou relacionamento joinTable (muitos para muitos) deleta os itens
				 * atuais e insere novamente
				 */
				if (descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable()) {
					Object fieldValue = descriptionField.getObjectValue(targetObject);
					if (fieldValue != null) {
						if (descriptionField.isMapTable()) {
							/*
							 * Remove todos os itens da Coleção
							 */
							result.addAll(getSQLMapTableCommands(null, null, SQLStatementType.DELETE_ALL, descriptionField, null, null, primaryKeyOwner));
							/*
							 * Insere novamente
							 */
							for (Object key : ((Map<?, ?>) fieldValue).keySet()) {
								Object value = ((Map<?, ?>) fieldValue).get(key);
								result.addAll(getSQLMapTableCommands(key, value, SQLStatementType.INSERT, descriptionField, null, null, primaryKeyOwner));
							}
						} else if (descriptionField.isCollectionTable()) {
							/*
							 * Remove todos os itens da coleção
							 */
							result.addAll(getSQLCollectionTableCommands(null, SQLStatementType.DELETE_ALL, descriptionField, null, null, primaryKeyOwner));
							/*
							 * Insere os itens
							 */
							for (Object value : (Collection<?>) fieldValue)
								result.addAll(getSQLCollectionTableCommands(value, SQLStatementType.INSERT, descriptionField, null, null, primaryKeyOwner));
						} else if (descriptionField.isCollectionEntity()) {
							for (Object entity : (Collection<?>) fieldValue) {
								if ((descriptionField.getMappedBy() != null) && (!"".equals(descriptionField.getMappedBy()))) {
									Field mappedByField = descriptionField.getTargetEntity().getDescriptionField(descriptionField.getMappedBy()).getField();
									mappedByField.set(entity, targetObject);
								}
								session.save(entity);
							}
						} else if (descriptionField.isJoinTable()) {
							/*
							 * Remove todos os itens da coleção
							 */
							result.addAll(getSQLJoinTableCommands(null, SQLStatementType.DELETE_ALL, descriptionField, null, null, primaryKeyOwner));
							/*
							 * Insere os itens
							 */
							for (Object value : (Collection<?>) fieldValue)
								result.addAll(getSQLJoinTableCommands(value, SQLStatementType.INSERT, descriptionField, null, null, primaryKeyOwner));
						}
					}
				}
			}
		}
	}

	protected void updateChangedItems(FieldEntityValue[] sourceList, FieldEntityValue[] targetList, DescriptionField descriptionField) throws Exception {
		/*
		 * Gera update dos itens da coleção caso o field tenha sido anotado com Cascade = ALL ou SAVE
		 */
		if (descriptionField.isCollectionEntity() || descriptionField.isJoinTable()) {
			if ((Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.ALL) || Arrays.asList(descriptionField.getCascadeTypes()).contains(
					CascadeType.SAVE))
					&& (sourceList != null)) {
				for (FieldEntityValue sourceValue : sourceList) {
					if (targetList != null) {
						for (FieldEntityValue targetValue : targetList) {
							if (targetValue.getName().equals(sourceValue.getName()) && (targetValue.getSource() != null)) {
								EntityManaged tempEntityManaged = session.getPersistenceContext().getEntityManaged(targetValue.getSource());

								if (((tempEntityManaged != null) && (!tempEntityManaged.getStatus().equals(EntityStatus.READ_ONLY)))
										|| (tempEntityManaged == null))
									save(session, targetValue.getSource());
							}
						}
					}
				}
			}
		}
	}

	protected void insertNewItens(List<CommandSQL> result, Map<String, Object> primaryKeyOwner, FieldEntityValue[] sourceList, FieldEntityValue[] targetList,
			DescriptionField descriptionField) throws Exception {
		/*
		 * Se existe em B(new) e não existe em A(old) gera insert se Cascade=ALL ou SAVE
		 */
		if (Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.ALL)
				|| Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.SAVE)) {
			if (targetList != null) {
				for (FieldEntityValue targetValue : targetList) {
					boolean found = false;
					if (sourceList != null) {
						for (FieldEntityValue sourceValue : sourceList) {
							if (sourceValue.compareTo(targetValue) == 0) {
								found = true;
								break;
							}
						}
					}
					/*
					 * não existe B(new) em A(old) gera insert
					 */
					if (!found) {
						if (descriptionField.isMapTable()) {
							Object key = targetValue.getSource();
							Object value = ((Map<?, ?>) targetValue.getValue()).get(key);
							result.addAll(getSQLMapTableCommands(key, value, SQLStatementType.INSERT, descriptionField, null, null, primaryKeyOwner));
						} else if (descriptionField.isCollectionTable())
							result.addAll(getSQLCollectionTableCommands(targetValue.getSource(), SQLStatementType.INSERT, descriptionField, null, null,
									primaryKeyOwner));
						else if (descriptionField.isCollectionEntity())
							if ((Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.ALL) || Arrays.asList(
									descriptionField.getCascadeTypes()).contains(CascadeType.SAVE)))
								save(session, targetValue.getSource());
							else if (descriptionField.isJoinTable())
								result.addAll(getSQLJoinTableCommands(targetValue.getSource(), SQLStatementType.INSERT, descriptionField, null, null,
										primaryKeyOwner));

					}

				}
			}
		}
	}

	protected void deleteItensRemoved(List<CommandSQL> result, Map<String, Object> primaryKeyOwner, FieldEntityValue[] sourceList,
			FieldEntityValue[] targetList, DescriptionField descriptionField) throws Exception {
		/*
		 * Se existe em A(old) e não existe em B(new) gera delete e se for entidade delete somente se
		 * Cascade=DELETE_ORPHAN
		 */
		if (sourceList != null) {
			for (FieldEntityValue sourceValue : sourceList) {
				boolean found = false;
				if (targetList != null) {
					for (FieldEntityValue targetValue : targetList) {
						if (targetValue.compareTo(sourceValue) == 0) {
							found = true;
							break;
						}
					}
				}
				/*
				 * não existe A(old) em B(new) gera delete
				 */
				if (!found) {
					if (descriptionField.isMapTable()) {
						Object key = sourceValue.getSource();
						Object value = ((Map<?, ?>) sourceValue.getValue()).get(key);
						result.addAll(getSQLMapTableCommands(key, value, SQLStatementType.DELETE, descriptionField, null, null, primaryKeyOwner));
					} else if (descriptionField.isCollectionTable())
						result.addAll(getSQLCollectionTableCommands(sourceValue.getSource(), SQLStatementType.DELETE, descriptionField, null, null,
								primaryKeyOwner));
					else if (descriptionField.isCollectionEntity())
						if ((Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.ALL)
								|| Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.DELETE) || Arrays.asList(
								descriptionField.getCascadeTypes()).contains(CascadeType.DELETE_ORPHAN))) {
							save(session, sourceValue.getSource());
							remove(session, sourceValue.getSource());
						} else if (descriptionField.isJoinTable())
							result.addAll(getSQLJoinTableCommands(sourceValue.getSource(), SQLStatementType.DELETE, descriptionField, null, null,
									primaryKeyOwner));
				}

			}
		}
	}

	protected void updateObject(Object targetObject, EntityCache entityCache, ArrayList<NamedParameter> namedParameters, List<CommandSQL> result,
			Object oldVersion) {
		/*
		 * Seta a versão anterior caso o objeto tenha sido anotado com
		 * 
		 * @Version
		 */
		if (entityCache.isVersioned()) {
			if (oldVersion != null) {
				namedParameters.add(new NamedParameter(entityCache.getVersionColumnName(), oldVersion, true));
			}
		}
		String sql = generateSql(entityCache.getTableName(), SQLStatementType.UPDATE, namedParameters);
		result.add(new UpdateCommandSQL(session, sql, namedParameters, targetObject, entityCache, entityCache.getTableName(), session.isShowSql(), entityCache
				.getDescriptionSqlByType(SQLStatementType.UPDATE)));
	}

	protected void updateRelationships(Object targetObject, EntityCache entityCache) throws Exception {
		/*
		 * Salva primeiro os relacionamentos(FK) com o objeto
		 */
		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			if (descriptionField.isRelationShip()) {
				Object fieldValue = descriptionField.getObjectValue(targetObject);
				if (fieldValue != null)
					saveRelationShip(fieldValue, descriptionField);
			}
		}
	}

	protected void updateParametersKey(Object targetObject, EntityCache entityCache, ArrayList<NamedParameter> namedParameters) throws Exception {
		/*
		 * Gera as chaves do update.
		 */
		Map<String, Object> primaryKey = session.getIdentifier(targetObject).getDatabaseColumns();
		for (DescriptionColumn column : entityCache.getPrimaryKeyColumns()) {
			if (session.getPersistenceContext().isExistsEntityManaged(targetObject))
				namedParameters.add(new NamedParameter(column.getColumnName(), entityCache.getLastValueByColumn(session, targetObject, column), true));
			else
				namedParameters.add(new NamedParameter(column.getColumnName(), primaryKey.get(column.getColumnName()), true));
		}
	}

	protected Object updateVersion(Object targetObject, EntityCache entityCache, ArrayList<NamedParameter> namedParameters, EntityManaged entityManaged)
			throws Exception, IllegalAccessException, InvocationTargetException {
		Object oldVersion = null;
		/*
		 * Incrementa a versão atual caso o objeto tenha sido anotado com
		 * 
		 * @Version e a estratégia do lock seja OPTIMIST_FORCE_INCREMENT, PESSIMISTIC_FORCE_INCREMENT ou WRITE
		 */
		if (entityCache.isVersioned()) {
			Object newVersion = null;
			if (entityManaged != null) {
				oldVersion = entityManaged.getOldVersion();
				newVersion = entityManaged.getOldVersion();
				if (entityManaged.containsLockMode(LockMode.OPTIMISTIC_FORCE_INCREMENT, LockMode.PESSIMISTIC_FORCE_INCREMENT, LockMode.WRITE)) {
					newVersion = Versioning.incrementVersion(entityManaged.getCurrentVersion(), entityCache.getVersionColumn().getField().getType());
					entityManaged.setCurrentVersion(newVersion);
					ReflectionUtils.setObjectValueByField(targetObject, entityCache.getVersionColumn().getField(), newVersion);
				}
			} else {
				oldVersion = ReflectionUtils.getFieldValueByName(targetObject, entityCache.getVersionColumn().getField().getName());
				newVersion = Versioning.incrementVersion(oldVersion, entityCache.getVersionColumn().getField().getType());
			}
			namedParameters.add(new NamedParameter(entityCache.getVersionColumn().getColumnName(), newVersion));
		}
		return oldVersion;
	}

	protected void updateCommonsParameters(Object targetObject, EntityCache entityCache, List<DescriptionField> fieldsModified,
			ArrayList<NamedParameter> namedParameters) throws SQLSessionException, IllegalAccessException, InvocationTargetException, Exception {
		Object newColumnValue;
		/*
		 * Gera os campos do update.
		 */
		Map<String, Object> foreignKey = null;
		for (DescriptionField fieldModified : fieldsModified) {
			if (!fieldModified.isAnyCollectionOrMap() && !fieldModified.isVersioned() && (!fieldModified.isJoinTable())) {
				if (fieldModified.isPrimaryKey())
					throw new SQLSessionException("Não é permitido alterar a chave de um Objeto sendo gerenciado. Verifique o campo " + fieldModified.getName()
							+ " do objeto da classe " + entityCache.getEntityClass().getName());

				newColumnValue = ReflectionUtils.getFieldValue(targetObject, fieldModified.getField());
				if (fieldModified.isInitialized(session, newColumnValue)) {
					if (fieldModified.getTargetEntity() != null) {
						if (session.isProxyObject(newColumnValue)) {
							if (!session.proxyIsInitialized(newColumnValue))
								continue;
						}
						foreignKey = new LinkedHashMap<String, Object>();
						Map<String, Object> foreignKeyTemporary = new LinkedHashMap<String, Object>();
						if (newColumnValue != null)
							foreignKeyTemporary = session.getIdentifier(newColumnValue).getDatabaseColumns();

						for (String key : foreignKeyTemporary.keySet()) {
							Object value = foreignKeyTemporary.get(key);
							if (value == null)
								foreignKey.put(key, getIdentifierPostInsertObject(newColumnValue, key));
							else
								foreignKey.put(key, value);
						}
					} else
						foreignKey = null;
					for (DescriptionColumn columnModified : fieldModified.getDescriptionColumns()) {
						if (!columnModified.isDiscriminatorColumn()) {
							boolean allowChangeField = true;
							if (session.getPersistenceContext().isExistsEntityManaged(targetObject) && (!entityCache.isExistsDescriptionSQL()))
								allowChangeField = entityCache.fieldCanbeChanged(session, targetObject, columnModified.getField().getName());

							if (allowChangeField) {
								if (columnModified.isForeignKey())
									namedParameters.add(new NamedParameter(columnModified.getColumnName(), foreignKey.get(columnModified
											.getReferencedColumnName())));
								else {
									namedParameters.add(fieldModified.getNamedParameterFromDatabaseObjectValue(session, targetObject, columnModified));
								}
							}
						}
					}
				}
			}
		}
	}

	protected List<CommandSQL> getCommandsToDeleteObject(Object targetObject, EntityCache entityCache) throws Exception {
		List<NamedParameter> keyParameters = new ArrayList<NamedParameter>();
		List<CommandSQL> result = new ArrayList<CommandSQL>();

		EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(targetObject);

		/*
		 * Gera as chaves do update ou delete. Se for delete remove os itens das coleções antes de remover o objeto.
		 */
		Map<String, Object> primaryKeyOwner = session.getIdentifier(targetObject).getDatabaseColumns();
		for (String key : primaryKeyOwner.keySet())
			keyParameters.add(new NamedParameter(key, primaryKeyOwner.get(key), true));

		/*
		 * Remove os itens das coleções antes de remover o objeto.
		 */

		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			if (descriptionField.isAnyCollectionOrMap() || descriptionField.isJoinTable()) {
				Object fieldValue = descriptionField.getObjectValue(targetObject);
				if (fieldValue != null) {
					String tableName = descriptionField.getTableName();
					if ((tableName == null) || ("".equals(tableName)))
						tableName = descriptionField.getTargetEntity().getTableName();

					if (descriptionField.isCollectionEntity()) {
						if (!(Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.ALL)
								|| Arrays.asList(descriptionField.getCascadeTypes()).contains(CascadeType.DELETE) || Arrays.asList(
								descriptionField.getCascadeTypes()).contains(CascadeType.DELETE_ORPHAN)))
							continue;

						if (fieldValue instanceof Collection) {
							for (Object value : ((Collection<?>) fieldValue)) {
								remove(session, value);
							}
						}
					} else
						result.add(new DeleteCommandSQL(session, generateSql(tableName, SQLStatementType.DELETE, keyParameters), keyParameters, null, null,
								tableName, session.isShowSql(), descriptionField.getDescriptionSqlByType(SQLStatementType.DELETE)));
				}
			}
		}

		/*
		 * Seta a versão anterior caso o objeto tenha sido anotado com
		 * 
		 * @Version
		 */
		if (entityCache.isVersioned())
			keyParameters.add(new NamedParameter(entityCache.getVersionColumnName(), entityManaged.getOldVersion(), true));

		result.add(new DeleteCommandSQL(session, generateSql(entityCache.getTableName(), SQLStatementType.DELETE, keyParameters), keyParameters, targetObject,
				entityCache, entityCache.getTableName(), session.isShowSql(), entityCache.getDescriptionSqlByType(SQLStatementType.DELETE)));

		return result;
	}

	protected void saveRelationShip(Object fieldValue, DescriptionField field) throws Exception {
		/*
		 * Se for uma entidade de relacionamento ForeignKey e Cascade = ALL ou SAVE e o objeto não for somente leitura
		 * salva o objeto
		 */
		if (Arrays.asList(field.getCascadeTypes()).contains(CascadeType.ALL) || Arrays.asList(field.getCascadeTypes()).contains(CascadeType.SAVE)) {
			EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(fieldValue);
			if (entityManaged != null) {
				if (!entityManaged.getStatus().equals(EntityStatus.READ_ONLY))
					save(session, fieldValue);
			} else
				save(session, fieldValue);
		}
	}

	protected String generateSql(String tableName, SQLStatementType statement, List<NamedParameter> parameters) {
		if (statement.equals(SQLStatementType.INSERT)) {
			if (NamedParameter.getNames(parameters).length > 0)
				return new Insert(session.getDialect()).setTableName(tableName).addColumns(NamedParameter.getNames(parameters)).toStatementString();
		} else if (statement.equals(SQLStatementType.UPDATE)) {
			if (NamedParameter.getNames(parameters).length > 0)
				return new Update(session.getDialect()).setTableName(tableName).addColumns(NamedParameter.getNames(parameters))
						.addPrimaryKeyColumns(NamedParameter.getNamesKey(parameters)).toStatementString();
		} else if (statement.equals(SQLStatementType.DELETE)) {
			if (NamedParameter.getNamesKey(parameters).length > 0)
				return new Delete().setTableName(tableName).addPrimaryKeyColumns(NamedParameter.getNamesKey(parameters)).toStatementString();
		}

		return "";
	}

	protected List<CommandSQL> getSQLMapTableCommands(Object key, Object value, SQLStatementType statement, DescriptionField field,
			DescriptionColumn identifyColumn, IdentifierPostInsert identifierPostInsert, Map<String, Object> primaryKeyOwner) {
		List<CommandSQL> result = new ArrayList<CommandSQL>();
		if (key != null) {
			ArrayList<NamedParameter> namedParameters = new ArrayList<NamedParameter>();
			if (statement.equals(SQLStatementType.INSERT)) {
				for (DescriptionColumn descriptionColumn : field.getDescriptionColumns()) {
					if (descriptionColumn.isForeignKey()) {
						if (descriptionColumn.getColumnName().equals(identifyColumn == null ? null : identifyColumn.getColumnName()))
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), identifierPostInsert));
						else
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), primaryKeyOwner.get(descriptionColumn
									.getReferencedColumnName())));
					} else {
						Object parameterValue = null;
						if (descriptionColumn.isMapKeyColumn())
							parameterValue = key;
						else
							parameterValue = value;

						if (field.isEnumerated())
							namedParameters
									.add(new NamedParameter(descriptionColumn.getColumnName(), descriptionColumn.getValueEnum(parameterValue.toString())));
						else if (field.isTemporalDate())
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), parameterValue, TemporalType.DATE));
						else if (field.isTemporalDateTime())
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), parameterValue, TemporalType.DATE_TIME));
						else
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), parameterValue));
					}
				}
				result.add(new InsertCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), null, null, field.getDescriptionSqlByType(statement)));
			} else if (statement.equals(SQLStatementType.DELETE)) {
				for (DescriptionColumn descriptionColumn : field.getDescriptionColumns()) {
					if (descriptionColumn.isPrimaryKey()) {
						if (descriptionColumn.isForeignKey()) {
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), primaryKeyOwner.get(descriptionColumn
									.getReferencedColumnName()), true));
						} else {
							Object parameterValue = null;
							if (descriptionColumn.isMapKeyColumn())
								parameterValue = key;
							else
								parameterValue = value;

							if (field.isEnumerated())
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), descriptionColumn.getValueEnum(parameterValue
										.toString())));
							else if (field.isTemporalDate())
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), parameterValue, TemporalType.DATE));
							else if (field.isTemporalDateTime())
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), parameterValue, TemporalType.DATE_TIME));
							else
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), parameterValue));

						}
					}
				}
				result.add(new DeleteCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), field.getDescriptionSqlByType(statement)));
			} else if (statement.equals(SQLStatementType.DELETE_ALL)) {
				for (DescriptionColumn column : field.getDescriptionColumns()) {
					if (column.isPrimaryKey()) {
						if (column.isForeignKey()) {
							namedParameters.add(new NamedParameter(column.getColumnName(), primaryKeyOwner.get(column.getReferencedColumnName()), true));
						}
					}
				}
				result.add(new DeleteCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), field.getDescriptionSqlByType(statement)));
			}
		}
		return result;
	}

	protected List<CommandSQL> getSQLJoinTableCommands(Object value, SQLStatementType statement, DescriptionField field, DescriptionColumn identifyColumn,
			IdentifierPostInsert identifierPostInsert, Map<String, Object> primaryKeyOwner) throws Exception {
		List<CommandSQL> result = new ArrayList<CommandSQL>();
		if (value != null) {
			ArrayList<NamedParameter> namedParameters = new ArrayList<NamedParameter>();
			if (statement.equals(SQLStatementType.INSERT)) {
				if (Arrays.asList(field.getCascadeTypes()).contains(CascadeType.ALL) || Arrays.asList(field.getCascadeTypes()).contains(CascadeType.SAVE)) {
					EntityManaged entityManaged = session.getPersistenceContext().getEntityManaged(value);
					if (entityManaged != null) {
						if (!entityManaged.getStatus().equals(EntityStatus.READ_ONLY))
							save(session, value);
					} else
						save(session, value);
				} else {
					if (!session.getIdentifier(value).hasIdentifier())
						throw new SQLSessionException("Objeto " + value.getClass()
								+ " não possuí anotação para salvar em cascata e também não possuí identificador. ");
				}
				Map<String, Object> foreignKeyRight = session.getIdentifier(value).getDatabaseColumns();

				for (DescriptionColumn descriptionColumn : field.getDescriptionColumns()) {
					if (descriptionColumn.isForeignKey()) {
						if (descriptionColumn.getColumnName().equals(identifyColumn == null ? null : identifyColumn.getColumnName()))
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), identifierPostInsert));
						else {
							if (foreignKeyRight.containsKey(descriptionColumn.getColumnName()))
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(),
										foreignKeyRight.get(descriptionColumn.getColumnName())));
							else
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), primaryKeyOwner.get(descriptionColumn
										.getReferencedColumnName())));
						}
					}
				}
				result.add(new InsertCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), null, null, field.getDescriptionSqlByType(statement)));
			} else if (statement.equals(SQLStatementType.DELETE)) {
				Map<String, Object> foreignKeyRight = session.getIdentifier(value).getColumns();
				for (DescriptionColumn descriptionColumn : field.getDescriptionColumns()) {
					if (descriptionColumn.isPrimaryKey()) {
						if (descriptionColumn.isForeignKey()) {
							if (foreignKeyRight.containsKey(descriptionColumn.getColumnName()))
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(),
										foreignKeyRight.get(descriptionColumn.getColumnName()), true));
							else
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), primaryKeyOwner.get(descriptionColumn
										.getReferencedColumnName()), true));
						} else
							namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), value, true));
					}
				}
				result.add(new DeleteCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), field.getDescriptionSqlByType(statement)));
			} else if (statement.equals(SQLStatementType.DELETE_ALL)) {
				Map<String, Object> foreignKeyRight = session.getIdentifier(value).getColumns();
				for (DescriptionColumn descriptionColumn : field.getDescriptionColumns()) {
					if (descriptionColumn.isPrimaryKey()) {
						if (descriptionColumn.isForeignKey()) {
							if (foreignKeyRight.containsKey(descriptionColumn.getColumnName()))
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(),
										foreignKeyRight.get(descriptionColumn.getColumnName()), true));
							else
								namedParameters.add(new NamedParameter(descriptionColumn.getColumnName(), primaryKeyOwner.get(descriptionColumn
										.getReferencedColumnName()), true));
						}
					}
				}
				result.add(new DeleteCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), field.getDescriptionSqlByType(statement)));
			}
		}
		return result;
	}

	protected List<CommandSQL> getSQLCollectionEntityCommand(Object value, SQLStatementType statement) throws Exception {
		List<CommandSQL> result = new ArrayList<CommandSQL>();
		if (value != null) {
			if (statement.equals(SQLStatementType.INSERT))
				result.addAll(getCommandsToInsertObject(value, session.getEntityCacheManager().getEntityCache(value.getClass())));
			else if (statement.equals(SQLStatementType.DELETE))
				result.addAll(getCommandsToDeleteObject(value, session.getEntityCacheManager().getEntityCache(value.getClass())));
		}
		return result;
	}

	protected List<CommandSQL> getSQLCollectionTableCommands(Object value, SQLStatementType statement, DescriptionField field,
			DescriptionColumn identifyColumn, IdentifierPostInsert identifierPostInsert, Map<String, Object> primaryKeyOwner) {
		List<CommandSQL> result = new ArrayList<CommandSQL>();
		if (value != null) {
			ArrayList<NamedParameter> namedParameters = new ArrayList<NamedParameter>();
			if (statement.equals(SQLStatementType.INSERT)) {
				for (DescriptionColumn column : field.getDescriptionColumns()) {
					if (column.isForeignKey()) {
						if (column.getColumnName().equals(identifyColumn == null ? null : identifyColumn.getColumnName())) {
							namedParameters.add(new NamedParameter(column.getColumnName(), identifierPostInsert));
						} else
							namedParameters.add(new NamedParameter(column.getColumnName(), primaryKeyOwner.get(column.getReferencedColumnName())));
					} else
						namedParameters.add(new NamedParameter(column.getColumnName(), value));
				}
				result.add(new InsertCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), null, null, field.getDescriptionSqlByType(statement)));
			} else if (statement.equals(SQLStatementType.DELETE)) {
				for (DescriptionColumn column : field.getDescriptionColumns()) {
					if (column.isPrimaryKey()) {
						if (column.isForeignKey())
							namedParameters.add(new NamedParameter(column.getColumnName(), primaryKeyOwner.get(column.getReferencedColumnName()), true));
						else
							namedParameters.add(new NamedParameter(column.getColumnName(), value, true));
					}
				}
				result.add(new DeleteCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), field.getDescriptionSqlByType(statement)));
			} else if (statement.equals(SQLStatementType.DELETE_ALL)) {
				for (DescriptionColumn column : field.getDescriptionColumns()) {
					if (column.isPrimaryKey()) {
						if (column.isForeignKey())
							namedParameters.add(new NamedParameter(column.getColumnName(), primaryKeyOwner.get(column.getReferencedColumnName()), true));
					}
				}
				result.add(new DeleteCommandSQL(session, generateSql(field.getTableName(), statement, namedParameters), namedParameters, null, null, field
						.getTableName(), session.isShowSql(), field.getDescriptionSqlByType(statement)));
			}
		}
		return result;
	}

	protected IdentifierPostInsert getIdentifierPostInsertObject(Object object, String columnName) {
		/*
		 * Procura nos comandos Insert da fila
		 */
		for (CommandSQL command : session.getCommandQueue()) {
			if (command instanceof InsertCommandSQL) {
				if (command.getTargetObject().equals(object)) {
					if (((InsertCommandSQL) command).getIdentifyColumn() != null) {
						if (((InsertCommandSQL) command).getIdentifyColumn().getColumnName().equals(columnName))
							return ((InsertCommandSQL) command).getIdentifierPostInsert();
					}
				}
			}
		}
		/*
		 * Procura no entityCache de identificadores
		 */
		Map<DescriptionColumn, IdentifierPostInsert> identifierTemp = session.getCacheIdentifier().get(object);
		if (identifierTemp != null) {
			Iterator<DescriptionColumn> it = identifierTemp.keySet().iterator();
			while (it.hasNext()) {
				DescriptionColumn column = it.next();
				if (column.getColumnName().equals(columnName))
					return identifierTemp.get(column);
			}
		}
		return null;
	}

	public void save(SQLSession session, Class<?> clazz, String[] columns, String[] values) throws Exception {
		throw new SQLSessionException("Método não suportado.");
	}

	@Override
	public SQLSessionValidatior getValidator() {
		if (AnterosBeanValidationHelper.isBeanValidationPresent()) {
			if (validator == null)
				validator = new SQLSessionValidatorImpl();
		}
		return validator;
	}

}
