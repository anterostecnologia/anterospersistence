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
package br.com.anteros.persistence.metadata;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.CompactHashSet;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.accessor.PropertyAccessorFactory;
import br.com.anteros.persistence.metadata.annotation.BooleanValue;
import br.com.anteros.persistence.metadata.annotation.Cache;
import br.com.anteros.persistence.metadata.annotation.Cascade;
import br.com.anteros.persistence.metadata.annotation.CollectionTable;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.Columns;
import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.Convert;
import br.com.anteros.persistence.metadata.annotation.Converts;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.EnumValues;
import br.com.anteros.persistence.metadata.annotation.Enumerated;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.GeneratedValue;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.annotation.Index;
import br.com.anteros.persistence.metadata.annotation.Indexes;
import br.com.anteros.persistence.metadata.annotation.Inheritance;
import br.com.anteros.persistence.metadata.annotation.JoinTable;
import br.com.anteros.persistence.metadata.annotation.Lob;
import br.com.anteros.persistence.metadata.annotation.MapKeyColumn;
import br.com.anteros.persistence.metadata.annotation.MapKeyConvert;
import br.com.anteros.persistence.metadata.annotation.MapKeyEnumerated;
import br.com.anteros.persistence.metadata.annotation.MapKeyTemporal;
import br.com.anteros.persistence.metadata.annotation.NamedQueries;
import br.com.anteros.persistence.metadata.annotation.NamedQuery;
import br.com.anteros.persistence.metadata.annotation.OrderBy;
import br.com.anteros.persistence.metadata.annotation.SQLDelete;
import br.com.anteros.persistence.metadata.annotation.SQLDeleteAll;
import br.com.anteros.persistence.metadata.annotation.SQLInsert;
import br.com.anteros.persistence.metadata.annotation.SQLUpdate;
import br.com.anteros.persistence.metadata.annotation.SequenceGenerator;
import br.com.anteros.persistence.metadata.annotation.Table;
import br.com.anteros.persistence.metadata.annotation.TableGenerator;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.metadata.annotation.Version;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.annotation.type.CascadeType;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;
import br.com.anteros.persistence.metadata.annotation.type.InheritanceType;
import br.com.anteros.persistence.metadata.comparator.DependencyComparator;
import br.com.anteros.persistence.metadata.configuration.ColumnConfiguration;
import br.com.anteros.persistence.metadata.configuration.ConvertConfiguration;
import br.com.anteros.persistence.metadata.configuration.ConverterConfiguration;
import br.com.anteros.persistence.metadata.configuration.EntityConfiguration;
import br.com.anteros.persistence.metadata.configuration.EnumValueConfiguration;
import br.com.anteros.persistence.metadata.configuration.FieldConfiguration;
import br.com.anteros.persistence.metadata.configuration.IndexConfiguration;
import br.com.anteros.persistence.metadata.configuration.JoinColumnConfiguration;
import br.com.anteros.persistence.metadata.configuration.JoinTableConfiguration;
import br.com.anteros.persistence.metadata.configuration.NamedQueryConfiguration;
import br.com.anteros.persistence.metadata.configuration.PersistenceModelConfiguration;
import br.com.anteros.persistence.metadata.configuration.RemoteParamConfiguration;
import br.com.anteros.persistence.metadata.configuration.SQLInsertIdConfiguration;
import br.com.anteros.persistence.metadata.configuration.UniqueConstraintConfiguration;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionConvert;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.descriptor.DescriptionGenerator;
import br.com.anteros.persistence.metadata.descriptor.DescriptionIndex;
import br.com.anteros.persistence.metadata.descriptor.DescriptionMappedBy;
import br.com.anteros.persistence.metadata.descriptor.DescriptionNamedQuery;
import br.com.anteros.persistence.metadata.descriptor.DescriptionSQL;
import br.com.anteros.persistence.metadata.descriptor.DescriptionUniqueConstraint;
import br.com.anteros.persistence.metadata.descriptor.ParamDescription;
import br.com.anteros.persistence.metadata.descriptor.type.ColumnType;
import br.com.anteros.persistence.metadata.descriptor.type.FieldType;
import br.com.anteros.persistence.metadata.descriptor.type.SQLStatementType;
import br.com.anteros.persistence.metadata.exception.EntityCacheManagerException;
import br.com.anteros.persistence.parameter.NamedParameterParserResult;
import br.com.anteros.persistence.session.cache.PersistenceMetadataCache;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzerException;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzerResult;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;
import br.com.anteros.persistence.sql.statement.NamedParameterStatement;
import br.com.anteros.persistence.util.AnterosPersistenceTranslate;
import br.com.anteros.synchronism.annotation.IdSynchronism;
import br.com.anteros.synchronism.annotation.Remote;

/**
 * Classe responsável por gerenciar cache das entidades e conversores.
 * 
 */
@SuppressWarnings("unchecked")
public class EntityCacheManager {
	private Map<Class<? extends Serializable>, EntityCache> entities = new LinkedHashMap<Class<? extends Serializable>, EntityCache>();
	private Set<ConverterCache> converters = new CompactHashSet<ConverterCache>();
	private boolean loaded = false;
	private boolean validate = true;
	private PropertyAccessorFactory propertyAccessorFactory;
	private DatabaseDialect databaseDialect;

	public EntityCacheManager() {
	}

	/**
	 * Método utilizado para ler as configurações das Entidades.
	 * 
	 * param clazzes throws Exception
	 */
	public void load(List<Class<? extends Serializable>> clazzes, boolean validate, PropertyAccessorFactory propertyAccessorFactory,
			DatabaseDialect databaseDialect) throws Exception {
		this.propertyAccessorFactory = propertyAccessorFactory;
		if (!isLoaded()) {
			Collections.sort(clazzes, new DependencyComparator());

			PersistenceModelConfiguration modelConfiguration = new PersistenceModelConfiguration();
			for (Class<? extends Serializable> sourceClazz : clazzes) {
				modelConfiguration.loadAnnotationsByClass(sourceClazz);
			}
			modelConfiguration.createOmmitedOrDefaultSettings();
			this.validate = validate;
			load(modelConfiguration, propertyAccessorFactory, databaseDialect);
		}
	}

	/**
	 * Método utilizado para ler as configurações das classes configuradas no modelo. param modelConfiguration throws
	 * Exception
	 */
	public void load(PersistenceModelConfiguration modelConfiguration, PropertyAccessorFactory propertyAccessorFactory, DatabaseDialect databaseDialect)
			throws Exception {
		this.propertyAccessorFactory = propertyAccessorFactory;
		this.databaseDialect = databaseDialect;
		if (!isLoaded()) {

			modelConfiguration.sortByDependency();

			for (ConverterConfiguration converter : modelConfiguration.getConverters()) {
				addConverter(new ConverterCache((AttributeConverter<?, ?>) converter.getConverter().newInstance(), converter.getEntityAttributeType(),
						converter.getDatabaseColumnType(), converter.isAutoApply()));
			}

			for (Class<? extends Serializable> sourceClazz : modelConfiguration.getEntities().keySet()) {
				if (!sourceClazz.isEnum()) { // Se não é um Enum é uma Entidade
					addEntityClass(sourceClazz, loadBasicConfigurations(sourceClazz, modelConfiguration.getEntities().get(sourceClazz)));
				}
			}

			for (EntityCache entityCache : entities.values())
				loadConfigurationsSuperClass(entityCache, modelConfiguration);

			for (EntityCache entityCache : entities.values())
				loadRemainderConfigurations(entityCache);

			if (validate)
				validateAfterLoadConfigurations();

			analyzeNamedQueries();

			this.loaded = true;
		}
	}

	private void analyzeNamedQueries() throws SQLQueryAnalyzerException {
		for (EntityCache entityCache : entities.values()) {
			if (entityCache.hasNamedQueries()) {
				for (DescriptionNamedQuery namedQuery : entityCache.getDescriptionNamedQueries()) {
					SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance().get(
							namedQuery.getResultClass().getName() + ":" + namedQuery.getQuery());
					if (analyzerResult == null) {
						analyzerResult = new SQLQueryAnalyzer(this, databaseDialect, !SQLQueryAnalyzer.IGNORE_NOT_USED_ALIAS_TABLE).analyze(
								namedQuery.getQuery(), namedQuery.getResultClass());
						PersistenceMetadataCache.getInstance().put(namedQuery.getResultClass().getName() + ":" + namedQuery.getQuery(), analyzerResult);
					}
				}
			}
		}
	}

	protected void validateAfterLoadConfigurations() throws Exception {

		for (EntityCache entityCache : entities.values()) {
			/*
			 * Valida se os parâmetros usados nas configurações
			 * 
			 * SQLInsert,SQLUpdate,SQLDelete,SQLDeleteAll existem na lista de colunas da classe.
			 */
			for (DescriptionSQL descriptionSQL : entityCache.getDescriptionSql().values()) {
				NamedParameterParserResult parserResult = (NamedParameterParserResult) PersistenceMetadataCache.getInstance().get(
						"NamedParameters:" + descriptionSQL.getSql());
				if (parserResult == null) {
					parserResult = NamedParameterStatement.parse(descriptionSQL.getSql(), null);
					PersistenceMetadataCache.getInstance().put("NamedParameters:" + descriptionSQL.getSql(), parserResult);
				}
				if (parserResult != null) {
					for (String param : parserResult.getParsedParams().keySet()) {
						if ((entityCache.getDescriptionColumnByName(param) == null) && (!descriptionSQL.getSuccessParameter().equalsIgnoreCase(param))
								&& (descriptionSQL.getParametersId().get(param) == null))
							throw new EntityCacheException(AnterosPersistenceTranslate.getMessage(this.getClass(), "descriptionSql.parameter.not.found", param,
									descriptionSQL.getSql(), descriptionSQL.getSqlType(), entityCache.getEntityClass().getName()));
					}
				}
			}

			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				/*
				 * Valida se o campo simples foi marcado como requerido mas está numa classe que é uma herança
				 */
				if (descriptionField.isSimple() && descriptionField.getSimpleColumn().isRequired()) {
					if ((descriptionField.getEntityCache() == entityCache) && (entityCache.isInheritance())
							&& !descriptionField.getSimpleColumn().hasDefaultValue()
							&& !(descriptionField.hasPrimaryKey() && descriptionField.isRelationShip())) {
						throw new EntityCacheException(
								"O campo "
										+ descriptionField.getName()
										+ " da classe "
										+ entityCache.getEntityClass().getName()
										+ " está configurado como requerido porém está numa classe herança. Informe um valor default para o campo ou altere o atributo required para false na anotação @Column.");
					}
				}

				/*
				 * Valida se os parâmetros usados nas configurações
				 * 
				 * SQLInsert,SQLUpdate,SQLDelete,SQLDeleteAll do campo existem na lista de colunas da classe do campo.
				 */
				for (DescriptionSQL descriptionSQL : descriptionField.getDescriptionSql().values()) {
					NamedParameterParserResult parserResult = (NamedParameterParserResult) PersistenceMetadataCache.getInstance().get(
							"NamedParameters:" + descriptionSQL.getSql());
					if (parserResult == null) {
						parserResult = NamedParameterStatement.parse(descriptionSQL.getSql(), null);
						PersistenceMetadataCache.getInstance().put("NamedParameters:" + descriptionSQL.getSql(), parserResult);
					}
					if (parserResult != null) {
						for (String param : parserResult.getParsedParams().keySet()) {
							if (entityCache.getDiscriminatorColumn() != null) {
								if (entityCache.getDiscriminatorColumn().getColumnName().equalsIgnoreCase(param))
									continue;
							}
							if ((descriptionField.getDescriptionColumnByName(param) == null) && (!descriptionSQL.getSuccessParameter().equalsIgnoreCase(param))
									&& (descriptionSQL.getParametersId().get(param) == null))
								throw new EntityCacheException("O parâmetro " + param + " usado no sql " + descriptionSQL.getSql() + " da configuração de "
										+ descriptionSQL.getSqlType() + " no campo " + descriptionField.getName()
										+ " não foi encontrado na lista de colunas da classe " + descriptionField.getTargetClass().getName());

						}
					}
				}

				if (descriptionField.isRelationShip() || descriptionField.isAnyCollectionOrMap()) {
					EntityCache referencedCache = descriptionField.getTargetEntity();
					if (referencedCache == null) {
						throw new EntityCacheException(
								"A classe da chave estrangeira não foi não encontrada na lista de entidades gerenciadas. Verifique o Campo "
										+ descriptionField.getName() + " da classe " + entityCache.getEntityClass().getName());
					}
					/*
					 * Verifica se as colunas da chave estrangeira estão na classe referenciada
					 */
					for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
						if (column.isForeignKey()) {
							List<EntityCache> entitiesCache = getEntityCachesByTableName(referencedCache.getTableName());
							DescriptionColumn referencedColumn = null;
							for (EntityCache ec : entitiesCache) {
								referencedColumn = ec.getDescriptionColumnByColumnName(column.getReferencedColumnName());
								if (referencedColumn != null)
									break;
							}

							if ((referencedColumn == null) || !(referencedColumn.isPrimaryKey())) {
								throw new EntityCacheException("A coluna " + column.getReferencedColumnName() + " referenciada no campo "
										+ descriptionField.getName() + " da classe " + descriptionField.getEntityCache().getEntityClass().getName()
										+ " não foi encontrada na classe " + referencedCache.getEntityClass().getName() + " ou não é um ID.");
							}
						}
					}

					/*
					 * Verifica se as colunas da chave primária fazem parte da chave estrangeira
					 */
//					if (descriptionField.isRelationShip()) {
//						List<EntityCache> entitiesCache = getEntityCachesByTableName(referencedCache.getTableName());
//						DescriptionColumn referenceColumn = null;
//						for (EntityCache ec : entitiesCache) {
//							for (DescriptionColumn rc : ec.getPrimaryKeyColumns()) {
//								if (!descriptionField.hasReferencedDescriptionColumn(rc.getColumnName())) {
//									referenceColumn = rc;
//									break;
//								}
//							}
//							if (referenceColumn != null)
//								break;
//						}
//						if (referenceColumn != null) {
//							throw new EntityCacheException("A coluna " + referenceColumn.getColumnName() + " não foi encontrada referenciada no campo "
//									+ descriptionField.getName() + " da classe " + descriptionField.getEntityCache().getEntityClass().getName()
//									+ " verifique se as colunas da chave estrangeira estão corretas.");
//						}
//					}
				}
			}
		}

	}

	public boolean isLoaded() {
		return loaded;
	}

	private void loadConfigurationsSuperClass(EntityCache entityCache, PersistenceModelConfiguration modelConfiguration) throws Exception {
		Class<?> sourceClazz = entityCache.getEntityClass();
		EntityCache cacheSuper;

		/*
		 * Se superclasse != de Object.class e ela não possuir Inheritance
		 */
		EntityConfiguration entityConfigurationSuper = modelConfiguration.getEntities().get(sourceClazz.getSuperclass());
		if ((sourceClazz.getSuperclass() != Object.class && (entityConfigurationSuper == null || (!entityConfigurationSuper
				.isAnnotationPresent(Inheritance.class) && !entityConfigurationSuper.isAnnotationPresent(DiscriminatorValue.class))))) {
			throw new EntityCacheException("A classe " + sourceClazz + " é uma subclasse de " + sourceClazz.getSuperclass()
					+ ", que não possui Inheritance definida ou não foi adicionada nas configurações.");
		} else if ((entityConfigurationSuper != null)
				&& ((entityConfigurationSuper.isAnnotationPresent(Inheritance.class)) || (entityConfigurationSuper
						.isAnnotationPresent(DiscriminatorValue.class)))) {
			/*
			 * Recupera annotações da superclass e inclui na subclasse.
			 */

			cacheSuper = entities.get(sourceClazz.getSuperclass());
			List<DescriptionField> temporaryListFields = new LinkedList<DescriptionField>();
			temporaryListFields.addAll(cacheSuper.getDescriptionFields());
			for (DescriptionField f : cacheSuper.getDescriptionFields()) {
				if (entityCache.getDescriptionField(f.getName()) != null) {
					throw new EntityCacheException("Encontrado campo " + f.getName() + " duplicado na classe " + sourceClazz
							+ ". Verifique se o mesmo já não existe na super classe.");
				}
			}
			try {
				temporaryListFields.addAll(entityCache.getDescriptionFields());
				entityCache.getDescriptionFields().clear();
				entityCache.getDescriptionFields().addAll(temporaryListFields);

				entityCache.addAllUniqueConstraints(cacheSuper.getUniqueConstraints());
				entityCache.addAllDescriptionColumn(cacheSuper.getDescriptionColumns());
				entityCache.addAllDescriptionIndex(cacheSuper.getDescriptionIndexes());
			} catch (Exception ex) {
				throw new EntityCacheException("Erro lendo configuração da classe " + sourceClazz.getName() + ". " + Arrays.toString(ex.getStackTrace()));
			}
		}

		/*
		 * Possui DiscriminatorValue
		 */
		EntityConfiguration entityConfiguration = modelConfiguration.getEntities().get(sourceClazz);
		if (entityConfiguration.isAnnotationPresent(DiscriminatorValue.class)) {
			cacheSuper = entities.get(sourceClazz.getSuperclass());
			if (cacheSuper == null) {
				throw new EntityCacheException("A Entidade " + sourceClazz.getName()
						+ " possui a configuração DiscriminatorValue mas não herda de uma outra Entidade ou a Entidade herdada não foi localizada.");
			}
			entityCache.setTableName(cacheSuper.getTableName());
			entityCache.setDiscriminatorValue(entityConfiguration.getDiscriminatorValue());
		}
	}

	/**
	 * Executa leitura e validação do restante das configurações: Inheritance,
	 * 
	 * DiscriminatorValue, DiscriminatorColumn
	 * 
	 * param entityCache throws EntityCacheException
	 */
	private void loadRemainderConfigurations(EntityCache cache) throws EntityCacheException {

		/*
		 * Percorre DescriptionFields, se FetchMode.ONE_TO_MANY, seta a targetEntity
		 */
		for (DescriptionField descriptionField : cache.getDescriptionFields()) {
			/*
			 * Se for MappedBy
			 */
			if (descriptionField.isJoinTable()) {
				for (DescriptionColumn c : descriptionField.getDescriptionColumns()) {
					boolean found = false;
					for (DescriptionColumn column : descriptionField.getEntityCache().getPrimaryKeyColumns()) {
						if (column.getColumnName().equals(c.getColumnName()))
							found = true;
					}
					if (!found) {
						EntityCache anotherEntityCache = this.getEntityCache(descriptionField.getTargetClass());
						if (anotherEntityCache != null) {
							for (DescriptionColumn descriptionColumn : anotherEntityCache.getPrimaryKeyColumns()) {
								if (descriptionColumn.getColumnName().equals(c.getReferencedColumnName()))
									found = true;
							}
						}
					}
					if (!found)
						throw new EntityCacheException("A coluna " + c.getColumnName()
								+ " não faz parte da chave das entidades relacionadas. Verifique o campo  " + descriptionField.getName() + " na classe "
								+ descriptionField.getEntityCache().getEntityClass().getName());
				}
			}
			if (descriptionField.hasModeType()) {
				EntityCache refCache = getEntityCache(descriptionField.getTargetClass());
				if ((refCache == null)
						&& ((descriptionField.getModeType() == FetchMode.ONE_TO_MANY) || (descriptionField.getModeType() == FetchMode.FOREIGN_KEY) || (descriptionField
								.getModeType() == FetchMode.MANY_TO_MANY)))
					throw new EntityCacheException("A classe " + descriptionField.getFieldClass().getName()
							+ " não foi encontrada na lista de classes configuradas. Verifique o campo " + descriptionField.getName() + " da Classe "
							+ descriptionField.getEntityCache().getEntityClass().getName());
				descriptionField.setTargetEntity(refCache);
				if (descriptionField.getModeType() == FetchMode.ONE_TO_MANY) {
					DescriptionMappedBy mapped;
					try {
						mapped = descriptionField.getDescriptionMappedBy();
						mapped.setEntityCache(entities.get(descriptionField.getTargetClass()));
					} catch (Exception ex) {
						throw new EntityCacheException("Erro lendo classe " + cache.getEntityClass().getName() + ". " + " campo " + descriptionField.getName()
								+ " " + ex.getMessage());
					}
					if (mapped.getEntityCache().getDescriptionField(mapped.getMappedBy()) == null) {
						throw new EntityCacheException("O mapeamento do campo " + descriptionField.getName() + " da classe " + cache.getEntityClass().getName()
								+ " está incorreto. O mapeamento configurado em mappedBy=" + mapped.getMappedBy() + " não foi encontrado na classe "
								+ mapped.getEntityCache().getEntityClass().getName());
					}
					descriptionField.setDescriptionMappedBy(mapped);
				} else if (descriptionField.getModeType() == FetchMode.FOREIGN_KEY) {
					for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
						DescriptionColumn refColumn = refCache.getDescriptionColumnByName(column.getReferencedColumnName());
						if (refColumn == null)
							throw new EntityCacheException("A Coluna referenciada " + column.getReferencedColumnName() + ", do field "
									+ descriptionField.getField().getName() + " na entidade " + cache.getEntityClass().getName()
									+ " não foi localizada na entidade " + descriptionField.getTargetClass().getName());

						column.setReferencedColumn(refColumn);
					}
				} else if (descriptionField.getModeType() == FetchMode.MANY_TO_MANY) {
					for (DescriptionColumn column : descriptionField.getDescriptionColumns()) {
						if (column.isInversedJoinColumn())
							refCache = getEntityCache(descriptionField.getTargetClass());
						else
							refCache = descriptionField.getEntityCache();

						descriptionField.setTargetEntity(refCache);
						DescriptionColumn refColumn = refCache.getDescriptionColumnByColumnName(column.getReferencedColumnName());
						if (refColumn == null)
							throw new EntityCacheException("A Coluna referenciada " + column.getReferencedColumnName() + ", do field "
									+ descriptionField.getField().getName() + " na entidade " + cache.getEntityClass().getName()
									+ " não foi localizada na entidade " + descriptionField.getTargetClass().getName());

						column.setReferencedColumn(refColumn);
						column.setReferencedTableName(refCache.getTableName());
					}
				}
			}

		}
	}

	@SuppressWarnings("unused")
	private boolean existsEntityClass(Class<?> clazz) {
		return getEntityCache(clazz) != null;
	}

	/**
	 * Executa leitura e validação das configurações básicas: Column, Table,
	 * 
	 * Entity, Fetch, etc.
	 * 
	 * 
	 * param sourceClazz return throws Exception
	 */
	private EntityCache loadBasicConfigurations(Class<? extends Serializable> sourceClazz, EntityConfiguration entityConfiguration) throws Exception {
		/**
		 * Valida as configurações básicas
		 */
		validateBasicConfiguration(sourceClazz, entityConfiguration);

		/**
		 * Cria EntityCache (metadata) que irá representar(descrever) a classe
		 */
		EntityCache entityCache = new EntityCache(sourceClazz);
		String tableName, schema, catalog;

		entityCache.setAbstractClass(ReflectionUtils.isAbstractClass(sourceClazz));

		/*
		 * Possui a configuração Inheritance
		 */
		if (entityConfiguration.isAnnotationPresent(Inheritance.class)) {
			/*
			 * Estratégia de herança SINGLE_TABLE
			 */
			if (entityConfiguration.getInheritanceStrategy() == InheritanceType.SINGLE_TABLE) {
				DescriptionColumn descriptionColumn = new DescriptionColumn(entityCache);
				descriptionColumn.setLength(entityConfiguration.getDiscriminatorColumnLength());
				descriptionColumn.setColumnName(entityConfiguration.getDiscriminatorColumnName());
				descriptionColumn.setColumnType(ColumnType.DISCRIMINATOR);
				descriptionColumn.setRequired(true);
				descriptionColumn.setDiscriminatorType(entityConfiguration.getDiscriminatorColumnType());
				entityCache.addDiscriminatorColumn(descriptionColumn);
			}
		}

		/*
		 * Possui configuração Cache
		 */
		if (entityConfiguration.isAnnotationPresent(Cache.class)) {
			entityCache.setCacheScope(entityConfiguration.getScope());
			entityCache.setMaxTimeCache(entityConfiguration.getMaxTimeMemory());
		}

		/*
		 * Se possuir NamedQueries ou NamedQuery
		 */
		if (entityConfiguration.isAnnotationPresent(NamedQueries.class) || entityConfiguration.isAnnotationPresent(NamedQuery.class))
			readNamedQuery(entityCache, entityConfiguration);

		/*
		 * Se possuir Remote
		 */
		if (entityConfiguration.isAnnotationPresent(Remote.class)) {
			entityCache.setMobileActionExport(entityConfiguration.getRemote().getMobileActionExport());
			entityCache.setMobileActionImport(entityConfiguration.getRemote().getMobileActionImport());
			entityCache.setDisplayLabel(entityConfiguration.getRemote().getDisplayLabel());
			entityCache.setExportOrderToSendData(entityConfiguration.getRemote().getExportOrderToSendData());
			entityCache.setExportColumns(entityConfiguration.getRemote().getExportColumns());
			entityCache.setExportConnectivityType(entityConfiguration.getRemote().getExportConnectivityType());
			entityCache.setImportConnectivityType(entityConfiguration.getRemote().getImportConnectivityType());

			RemoteParamConfiguration[] exportParams = entityConfiguration.getRemote().getExportParams();
			for (RemoteParamConfiguration param : exportParams)
				entityCache.getExportParams().put(param.getParamOrder(),
						new ParamDescription(param.getParamName(), param.getParamOrder(), param.getParamValue()));

			RemoteParamConfiguration[] importParams = entityConfiguration.getRemote().getImportParams();
			for (RemoteParamConfiguration param : importParams)
				entityCache.getImportParams().put(param.getParamOrder(),
						new ParamDescription(param.getParamName(), param.getParamOrder(), param.getParamValue()));
		}

		/*
		 * Se possuir SQLInsert, SQLDelete, SQLDeleteAll ou SQLUpdate na classe
		 */
		readConfigurationSQL(sourceClazz, entityConfiguration, entityCache,
				new Class[] { SQLInsert.class, SQLUpdate.class, SQLDelete.class, SQLDeleteAll.class });

		tableName = sourceClazz.getSimpleName().toLowerCase();
		schema = "";
		catalog = "";
		/*
		 * Adiciona as constraints únicas
		 */
		if (entityConfiguration.isAnnotationPresent(Table.class)) {
			tableName = entityConfiguration.getTableName();
			catalog = entityConfiguration.getCatalog();
			schema = entityConfiguration.getSchema();
			for (UniqueConstraintConfiguration uniqueConstraint : entityConfiguration.getUniqueConstraints()) {
				DescriptionUniqueConstraint descriptionUniqueConstraint = new DescriptionUniqueConstraint(entityCache);
				descriptionUniqueConstraint.name(uniqueConstraint.getName()).columnNames(uniqueConstraint.getColumnNames());
				entityCache.addUniqueConstraint(descriptionUniqueConstraint);
			}
		}

		entityCache.setTableName(tableName);
		entityCache.setCatalog(catalog);
		entityCache.setSchema(schema);

		/*
		 * Adiciona os índices
		 */
		if (entityConfiguration.isAnnotationPresent(Indexes.class) || entityConfiguration.isAnnotationPresent(Index.class)) {
			IndexConfiguration[] indexes = entityConfiguration.getIndexes();
			if (indexes != null) {
				for (IndexConfiguration index : indexes) {
					entityCache.getIndexes().add(
							new DescriptionIndex(entityCache).name(index.getName()).columnNames(index.getColumnNames()).schema(index.getSchema())
									.catalog(index.getCatalog()).unique(index.isUnique()));
				}
			}
		}

		/*
		 * Adicionas os conversores de campos entidade x banco de dados
		 */
		readConvertConfiguration(entityCache, entityConfiguration);

		for (FieldConfiguration fieldConfiguration : entityConfiguration.getFields()) {
			/*
			 * Se possuir Transient
			 */
			if ((fieldConfiguration.isAnnotationPresent(Transient.class))
					|| (fieldConfiguration.getName().toLowerCase().startsWith("$javassist_read_write_handler"))
					|| (Modifier.isStatic(fieldConfiguration.getField().getModifiers())))
				continue;

			validateBasicFieldConfiguration(sourceClazz, fieldConfiguration);

			/*
			 * Se possuir Fetch
			 */
			if (fieldConfiguration.isAnnotationPresent(Fetch.class) && !fieldConfiguration.isAnnotationPresent(ForeignKey.class)
					&& !fieldConfiguration.isAnnotationPresent(JoinTable.class)) {

				/*
				 * Se FetchMode.ELEMENT_COLLECTION
				 */
				if (fieldConfiguration.getFetch().getMode() == FetchMode.ELEMENT_COLLECTION) {
					try {
						readElementCollectionConfiguration(fieldConfiguration, entityCache);
					} catch (Exception e) {
						throw new EntityCacheManagerException("Não foi possível ler as configurações ELEMENT COLLECTION do campo "
								+ fieldConfiguration.getName() + " da classe " + entityCache.getEntityClass().getName() + " - " + e.getMessage());
					}
				} else
					readFetchConfigurations(entityCache, fieldConfiguration);
			}

			/*
			 * Se possuir ForeignKey
			 */
			if (fieldConfiguration.isAnnotationPresent(ForeignKey.class) && !fieldConfiguration.isAnnotationPresent(Columns.class)
					&& !fieldConfiguration.isAnnotationPresent(CompositeId.class))
				readForeignKeyConfiguration(fieldConfiguration, entityCache, entityConfiguration.getModel());

			/*
			 * Se não possuir ForeignKey, Fetch e CompositeId ou Transient será uma coluna normal
			 */
			if (!fieldConfiguration.isAnnotationPresent(ForeignKey.class) && !fieldConfiguration.isAnnotationPresent(Fetch.class)
					&& !fieldConfiguration.isAnnotationPresent(CompositeId.class) && !fieldConfiguration.isAnnotationPresent(JoinTable.class))
				readColumnConfiguration(fieldConfiguration, entityCache, entityConfiguration.getModel());

			/*
			 * Se possuir Columns ou CompositeId
			 */
			if (fieldConfiguration.isAnnotationPresent(Columns.class) || fieldConfiguration.isAnnotationPresent(CompositeId.class))
				readCompositeIdConfiguration(fieldConfiguration, entityCache, entityConfiguration.getModel());

			/*
			 * Se possuir JoinTable
			 */
			if (fieldConfiguration.isAnnotationPresent(JoinTable.class))
				readJoinTableConfiguration(fieldConfiguration, entityCache);

			if (fieldConfiguration.isAnnotationPresent(Remote.class) && (!fieldConfiguration.isAnnotationPresent(JoinTable.class)))
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
						+ " possui configuração Remote e deve estar acompanhada da configuração JoinTable ");

			/*
			 * Se possuir SQLInsert, SQLDelete, SQLDeleteAll ou SQLUpdate no field
			 */
			readConfigurationSQL(sourceClazz, entityCache, new Class[] { SQLInsert.class, SQLUpdate.class, SQLDelete.class, SQLDeleteAll.class },
					fieldConfiguration);

			/*
			 * Adiciona os índices
			 */
			if (fieldConfiguration.isAnnotationPresent(Indexes.class) || entityConfiguration.isAnnotationPresent(Index.class)) {
				IndexConfiguration[] indexes = fieldConfiguration.getIndexes();
				if (indexes != null) {
					DescriptionField df = entityCache.getDescriptionField(fieldConfiguration.getField().getName());
					if (df != null) {
						for (IndexConfiguration index : indexes)
							df.getIndexes().add(
									new DescriptionIndex(entityCache).name(index.getName()).columnNames(index.getColumnNames()).schema(index.getSchema())
											.catalog(index.getCatalog()).unique(index.isUnique()));
					}
				}
			}

			/*
			 * Adicionas os conversores
			 */
			readConvertConfiguration(entityCache, fieldConfiguration);

		}

		return entityCache;
	}

	protected void readConvertConfiguration(EntityCache entityCache, FieldConfiguration fieldConfiguration) throws EntityCacheException,
			InstantiationException, IllegalAccessException {
		/*
		 * Adiciona os conversores
		 */
		if (fieldConfiguration.isAnnotationPresent(Converts.class) || fieldConfiguration.isAnnotationPresent(Convert.class)) {
			ConvertConfiguration[] converts = fieldConfiguration.getConverts();
			if (converts != null) {
				DescriptionField df = entityCache.getDescriptionField(fieldConfiguration.getField().getName());
				if (df != null) {
					for (ConvertConfiguration convert : converts) {
						ConverterCache converterCache = getConverterByCache(convert.getConverter());
						df.getConverts().add(
								new DescriptionConvert(converterCache.getConverter(), convert.getAttributeName(), converterCache.getEntityAttributeType(),
										converterCache.getDatabaseColumnType()));
					}
				}
			}
		}

	}

	public ConverterCache getConverterByCache(Class<?> converter) {
		for (ConverterCache converterCache : converters) {
			if (converterCache.getConverter().getClass().equals(converter))
				return converterCache;
		}
		return null;
	}

	protected void readConvertConfiguration(EntityCache entityCache, EntityConfiguration entityConfiguration) throws EntityCacheException,
			InstantiationException, IllegalAccessException {
		/*
		 * Adiciona os conversores anotados na classe
		 */
		if (entityConfiguration.isAnnotationPresent(Converts.class) || entityConfiguration.isAnnotationPresent(Convert.class)) {
			ConvertConfiguration[] converts = entityConfiguration.getConverts();
			if (converts != null) {
				for (ConvertConfiguration convert : converts) {
					if (StringUtils.isEmpty(convert.getAttributeName())) {
						throw new EntityCacheException("Conversores configurados em uma entidade devem ter o nome do atributo.");
					}
					ConverterCache converterCache = getConverterByCache(convert.getConverter());
					entityCache.getConverts().add(
							new DescriptionConvert(converterCache.getConverter(), convert.getAttributeName(), converterCache.getEntityAttributeType(),
									converterCache.getDatabaseColumnType()));
				}
			}
		}
	}

	protected void validateBasicFieldConfiguration(Class<? extends Serializable> sourceClazz, FieldConfiguration fieldConfiguration)
			throws EntityCacheException {
		if (fieldConfiguration.getAnnotations().size() == 0)
			throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
					+ " não possuí nenhuma configuração. Caso o campo não seja persistido configurar como Transient.");

		if (!ReflectionUtils.hasGetterAccessor(sourceClazz, fieldConfiguration.getField())) {
			throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
					+ " não possuí um método acessor (GET) configurado. Defina os métodos acessores para todos os campos das entidades.");
		}

		if (!ReflectionUtils.hasSetterAccessor(sourceClazz, fieldConfiguration.getField())) {
			throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
					+ " não possuí um método acessor (SET) configurado. Defina os métodos acessores para todos os campos das entidades.");
		}

		if (validate) {
			/*
			 * Se field Date, deve possuir a configuração Temporal
			 */
			if (fieldConfiguration.getType() == java.util.Date.class && !fieldConfiguration.isAnnotationPresent(Temporal.class))
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
						+ " é do tipo java.util.Date, mas não possui a configuração Temporal.");

			/*
			 * Se possuir precisão, mas não possui tamanho
			 */
			if (fieldConfiguration.isAnnotationPresent(Column.class) && fieldConfiguration.getColumns().iterator().next().getScale() > 0
					&& fieldConfiguration.getColumns().iterator().next().getPrecision() < 1)
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
						+ " foi definido escala(decimais) " + fieldConfiguration.getColumns().iterator().next().getScale() + " e tamanho 0");

			/*
			 * Valida se é um tipo primitivo
			 */
			if (fieldConfiguration.getType().isPrimitive())
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
						+ " é um tipo primitivo. Utilize somente classes Wrapper's. Ex: Long, Integer, Short, Double, etc.");

			/*
			 * Verifica se o tipo é Boolean e se possui uma configuração
			 * 
			 * BooleanValue
			 */
			if ((fieldConfiguration.getType().equals(BooleanValue.class) && (!fieldConfiguration.isAnnotationPresent(BooleanValue.class))))
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + sourceClazz.getName()
						+ " é do tipo Boolean e não possuí a configuração BooleanValue.");

			if (fieldConfiguration.isAnnotationPresent(OrderBy.class) && !fieldConfiguration.isAnnotationPresent(Fetch.class))
				throw new EntityCacheException("A configuração Order deve estar acompanhada com a configuração Fetch. Campo " + fieldConfiguration
						+ " da classe " + sourceClazz.getName());

			/*
			 * Se possuir JoinTable, deve estar acompanhada com Fetch
			 */
			if (fieldConfiguration.isAnnotationPresent(JoinTable.class)) {
				if (!fieldConfiguration.isAnnotationPresent(Fetch.class))
					throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
							+ " deve ser possuir a configuração Fetch.");

				if (fieldConfiguration.getFetch().getMode() != FetchMode.MANY_TO_MANY)
					throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
							+ " deve ser informado FetchMode.MANY_TO_MANY");
				if (!(ReflectionUtils.isImplementsInterface(fieldConfiguration.getType(), Collection.class) || ReflectionUtils.isImplementsInterface(
						fieldConfiguration.getType(), Set.class))) {
					throw new EntityCacheException("A configuração Fetch com FechMode=MANY_TO_MANY só pode ser usada em coleções. Verifique o campo "
							+ fieldConfiguration.getName() + " da classe " + sourceClazz.getName());
				}
			}

			/*
			 * Se possuir Fetch
			 */
			if (fieldConfiguration.isAnnotationPresent(Fetch.class)) {
				/*
				 * Se FetchMode.ELEMENT_COLLECTION
				 */
				if (fieldConfiguration.getFetch().getMode() == FetchMode.ELEMENT_COLLECTION) {
					if (!fieldConfiguration.isAnnotationPresent(CollectionTable.class)) {
						throw new EntityCacheException(
								"O campo "
										+ fieldConfiguration.getName()
										+ " da entidade "
										+ fieldConfiguration.getType().getName()
										+ " possui a configuração ElementCollection, mas não possui a configuração CollectionTable para informar os atributos necessários para armazenar os dados.");
					}
					if (!fieldConfiguration.getCollectionTable().hasJoinColumns()) {
						throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
								+ " possui a configuração CollectionTable, informe as colunas de junção(JoinColumn).");
					}

					if (!fieldConfiguration.isAnnotationPresent(Column.class)) {
						throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
								+ " possui a configuração CollectionTable, mas não possui a configuração Column para informar os atributos da coluna.");
					}
					if (!((ReflectionUtils.isImplementsInterface(fieldConfiguration.getType(), Collection.class)
							|| ReflectionUtils.isImplementsInterface(fieldConfiguration.getType(), Set.class) || ReflectionUtils.isImplementsInterface(
							fieldConfiguration.getType(), Map.class)))) {
						throw new EntityCacheException(
								"A configuração Fetch com FechMode=ELEMENT_COLLECTION só pode ser usada em coleções ou maps. Verifique o campo "
										+ fieldConfiguration.getName() + " da classe " + sourceClazz.getName());
					}

					/*
					 * Se implementa Map, deve possuir MapKeyColumn
					 */
					if (ReflectionUtils.isImplementsMap(fieldConfiguration.getType())) {
						if (!fieldConfiguration.isAnnotationPresent(MapKeyColumn.class)) {
							throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
									+ " é uma implemtaçãoo de java.util.Map e deve ser informada a configuração MapKeyColumn.");
						}

						/*
						 * Se possuir MapKeyColumn deve ser do tipo String.class,Integer.class, Long.class, Date.class
						 * ou Enum
						 */
						if (!ReflectionUtils.containsInTypedMap(fieldConfiguration.getField(), new Class<?>[] { String.class, Integer.class, Long.class,
								Date.class, Enum.class, Float.class, BigDecimal.class, BigInteger.class, Double.class })) {
							throw new EntityCacheException(
									"O campo "
											+ fieldConfiguration.getName()
											+ " da entidade "
											+ fieldConfiguration.getType().getName()
											+ ", possui a configuração MapKeyColumn deve ser do tipo String.class, Integer, Long, Date, Enum, Float, BigDecimal, BigInteger ou Double.");
						}

					}
				}

				/*
				 * Se possui MapKeyEnumerated deve ser do tipo Map
				 */
				if (fieldConfiguration.isAnnotationPresent(MapKeyEnumerated.class)) {
					if (!ReflectionUtils.isImplementsMap(fieldConfiguration.getType())) {
						throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
								+ ", possui a configuração MapKeyEnumerated. Esta configuração só pode ser usado com campos do tipo Map.");
					}

				}

				/*
				 * Se possui MapKeyEnumerated deve ser do tipo Map
				 */
				if (fieldConfiguration.isAnnotationPresent(MapKeyEnumerated.class)) {
					if (!ReflectionUtils.isImplementsMap(fieldConfiguration.getType())) {
						throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
								+ ", possui a configuração MapKeyEnumerated. Esta configuração só pode ser usado com campos do tipo Map.");
					}

					if (!ReflectionUtils.containsEnumInKeyTypedMap(fieldConfiguration.getField())) {
						throw new EntityCacheException(
								"O campo "
										+ fieldConfiguration.getName()
										+ " da entidade "
										+ fieldConfiguration.getType().getName()
										+ ", possui a configuração MapKeyEnumerated. Esta configuração só pode ser usado com campos do tipo Map e que tenham no campo Chave(Key) um tipo Enum.");
					}
				}

				/*
				 * Se possui MapKeyTemporal deve ser do tipo Map
				 */
				if (fieldConfiguration.isAnnotationPresent(MapKeyTemporal.class)) {
					if (!ReflectionUtils.isImplementsMap(fieldConfiguration.getType())) {
						throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
								+ ", possui a configuração MapKeyTemporal. Esta configuração só pode ser usado com campos do tipo Map.");
					}

					if (!ReflectionUtils.containsInKeyTypedMap(fieldConfiguration.getField(), new Class<?>[] { java.util.Date.class })) {
						throw new EntityCacheException(
								"O campo "
										+ fieldConfiguration.getName()
										+ " da entidade "
										+ fieldConfiguration.getType().getName()
										+ ", possui a configuração MapKeyEnumerated. Esta configuração só pode ser usado com campos do tipo Map e que tenham no campo Chave(Key) um tipo Data.");
					}
				}

				/*
				 * Se possui EnumValues
				 */
				if (fieldConfiguration.isAnnotationPresent(EnumValues.class)) {
					throw new EntityCacheException("A configuração EnumValues não é permitido para campos. Deve ser usada somente em classes Enum->"
							+ fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName());

				}

				/*
				 * Se FetchMode.ONE_TO_MANY vazio
				 */
				if (fieldConfiguration.getFetch().getMode() == FetchMode.ONE_TO_MANY && "".equals(fieldConfiguration.getFetch().getMappedBy())) {
					throw new EntityCacheException("Ao utilizar FetchMode.ONE_TO_MANY deve-se informar o argumento mappedBy. Campo " + fieldConfiguration);
				} else if (fieldConfiguration.getFetch().getMode() == FetchMode.ONE_TO_MANY) {
					if (!(ReflectionUtils.isImplementsInterface(fieldConfiguration.getType(), Collection.class) || ReflectionUtils.isImplementsInterface(
							fieldConfiguration.getType(), Set.class))) {
						throw new EntityCacheException("A configuração Fetch com FechMode=ONE_TO_MANY só pode ser usada em coleções. Verifique o campo "
								+ fieldConfiguration.getName() + " da classe " + sourceClazz.getName());
					}
				} else if (fieldConfiguration.getFetch().getMode() == FetchMode.SELECT) {
					/*
					 * Se FetchMode.SELECT
					 */
					if ("".equals(fieldConfiguration.getFetch().getStatement()))
						/*
						 * Se FetchMode.SELECT vazio
						 */
						throw new EntityCacheException("Ao utilizar FetchMode.SELECT, deve-se informar o argumento statement. Campo " + fieldConfiguration);

					if (fieldConfiguration.isAnnotationPresent(OrderBy.class))
						throw new EntityCacheException("A configuração OrderBy não deve ser utilizada junto com FetchMode.SELECT. Campo " + fieldConfiguration);

				}

			}

			/*
			 * Se possuir CompositeId. é obrigatório o uso de Column ou
			 * 
			 * Columns
			 */
			if (fieldConfiguration.isAnnotationPresent(CompositeId.class)) {
				if (!fieldConfiguration.isAnnotationPresent(Column.class) && !fieldConfiguration.isAnnotationPresent(Columns.class)) {
					throw new EntityCacheException("O campo " + fieldConfiguration
							+ ", possui a configuração CompositeId, que obrigatoriamente deve estar acompanhada por Column ou Columns");
				}

				/*
				 * Se ForeignKey não estiver com FetchType.EAGER
				 */
				if (fieldConfiguration.isAnnotationPresent(ForeignKey.class)) {
					if (fieldConfiguration.getForeignKey().getType() == FetchType.LAZY)
						throw new EntityCacheException("A configuração CompositeId do campo " + fieldConfiguration
								+ " deve ser usado obrigatoriamente com FetchType.EAGER.");
				}
			}

			/*
			 * Se possuir Cascade e o field não for uma Collection ou ForeignKey, e não possuir Fetch
			 */
			if (fieldConfiguration.isAnnotationPresent(Cascade.class)) {
				if (!ReflectionUtils.isCollection(fieldConfiguration.getType()) && !fieldConfiguration.isAnnotationPresent(ForeignKey.class)) {
					throw new EntityCacheException(
							"O campo "
									+ fieldConfiguration.getName()
									+ " do tipo "
									+ fieldConfiguration.getType().getName()
									+ " da entidade "
									+ sourceClazz.getSimpleName()
									+ " possui a configuração Cascade que é permitida somente para implementações de java.util.Collection (Set, List) ou com a configuração ForeignKey.");

				}

				if (!fieldConfiguration.isAnnotationPresent(Fetch.class) && (!fieldConfiguration.isAnnotationPresent(ForeignKey.class))) {
					throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
							+ " deve estar acompanhado da configuração Fetch ou ForeignKey.");
				}
			}

			/*
			 * Se possui Lob, deve ser do tipo Byte[], byte[], implementar java.io.Serializable, Character[], char[] ou
			 * java.lang.String
			 */
			if (fieldConfiguration.isAnnotationPresent(Lob.class)) {
				if (fieldConfiguration.getType() != byte[].class && fieldConfiguration.getType() != Byte[].class
						&& Serializable.class.isAssignableFrom(fieldConfiguration.getType()) && fieldConfiguration.getType() != Character[].class
						&& fieldConfiguration.getType() != char[].class && fieldConfiguration.getType() != java.lang.String.class) {
					throw new EntityCacheException(
							"O campo "
									+ fieldConfiguration.getName()
									+ " da entidade "
									+ fieldConfiguration.getType().getName()
									+ " possui a configuração Lob e deve ser do tipo Byte[], byte[], implementar java.io.Serializable, Character[], char[] ou java.lang.String.");
				}

			}
		}

		/*
		 * Se possuir Version, deve ser do tipo Integer, Long, Short ou Date
		 */
		if (fieldConfiguration.isAnnotationPresent(Version.class)) {
			if ((fieldConfiguration.getType() != Integer.class) && (fieldConfiguration.getType() != Long.class)
					&& (fieldConfiguration.getType() != Short.class) && (fieldConfiguration.getType() != Date.class)) {
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da Entidade " + sourceClazz.getName()
						+ " possui Version que pode ser utilizado somente com atributos do tipo Long, Integer, Short e Date.");
			}
		}

		/*
		 * Se possuir SequenceGenerator e TableGenerator
		 */
		if (fieldConfiguration.isAnnotationPresent(SequenceGenerator.class) || fieldConfiguration.isAnnotationPresent(TableGenerator.class)) {
			/*
			 * Se não possuir GenerateValue
			 */
			if (!fieldConfiguration.isAnnotationPresent(GeneratedValue.class)) {
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
						+ " possui SequenceGenerator/TableGenerator e deve estar acompanhdo da configuração GenerateValue ");
			}

			/*
			 * Se possuir TableGenerator, GeneratedType deve ser TABLE
			 */
			if (fieldConfiguration.isAnnotationPresent(TableGenerator.class)
					&& ((fieldConfiguration.getGeneratedType() != GeneratedType.TABLE) && (fieldConfiguration.getGeneratedType() != GeneratedType.AUTO))) {
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
						+ " possui TableGenerator  e o GeneratedType deve ser: TABLE ou AUTO");
			}

			/*
			 * Se possuir SequenceGenerator, GeneratedType deve ser SEQUENCE
			 */
			if (fieldConfiguration.isAnnotationPresent(SequenceGenerator.class)
					&& (fieldConfiguration.getGeneratedType() != GeneratedType.SEQUENCE && fieldConfiguration.getGeneratedType() != GeneratedType.AUTO)) {
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
						+ " possui SequenceGenerator e o GeneratedType deve ser: SEQUENCE ou AUTO");
			}
		}

		/*
		 * Se possuir MapKeyConvert
		 */
		if (fieldConfiguration.isAnnotationPresent(MapKeyConvert.class)) {
			if (!ReflectionUtils.isImplementsMap(fieldConfiguration.getType())) {
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da entidade " + fieldConfiguration.getType().getName()
						+ ", possui a configuração MapKeyConvert. Esta configuração só pode ser usado com campos do tipo Map.");
			}
		}
	}

	protected void validateBasicConfiguration(Class<? extends Serializable> sourceClazz, EntityConfiguration entityConfiguration) throws EntityCacheException {
		if (validate) {
			String[] errors = EntityCacheAnnotationValidation.validateEntityConfiguration(sourceClazz, entityConfiguration);
			if (errors.length > 0)
				throw new EntityCacheException(errors[0]);
		}
	}

	private void readConfigurationSQL(Class<?> sourceClazz, EntityConfiguration entityConfiguration, EntityCache entityCache, Class<?>[] annotationClazz)
			throws EntityCacheException {
		for (Class clazz : annotationClazz) {
			if (entityConfiguration.isAnnotationPresent(clazz)) {
				DescriptionSQL descriptionSQL = new DescriptionSQL();
				if (clazz == SQLInsert.class) {
					descriptionSQL.setCallable(entityConfiguration.getSqlInsert().isCallable());
					descriptionSQL.setCallableType(entityConfiguration.getSqlInsert().getCallableType());
					descriptionSQL.setSql(entityConfiguration.getSqlInsert().getSql());
					descriptionSQL.setSqlType(SQLStatementType.INSERT);
					descriptionSQL.setSuccessParameter(entityConfiguration.getSqlInsert().getSuccessParameter());
					descriptionSQL.setSuccessValue(entityConfiguration.getSqlInsert().getSuccessValue());
					for (SQLInsertIdConfiguration id : entityConfiguration.getSqlInsert().getParameterId())
						descriptionSQL.getParametersId().put(id.getParameterId(), id.getColumnName());
					entityCache.getDescriptionSql().put(SQLStatementType.INSERT, descriptionSQL);
				} else if (clazz == SQLUpdate.class) {
					descriptionSQL.setCallable(entityConfiguration.getSqlUpdate().isCallable());
					descriptionSQL.setCallableType(entityConfiguration.getSqlUpdate().getCallableType());
					descriptionSQL.setSql(entityConfiguration.getSqlUpdate().getSql());
					descriptionSQL.setSqlType(SQLStatementType.UPDATE);
					descriptionSQL.setSuccessParameter(entityConfiguration.getSqlUpdate().getSuccessParameter());
					descriptionSQL.setSuccessValue(entityConfiguration.getSqlUpdate().getSuccessValue());
					entityCache.getDescriptionSql().put(SQLStatementType.UPDATE, descriptionSQL);
				} else if (clazz == SQLDelete.class) {
					descriptionSQL.setCallable(entityConfiguration.getSqlDelete().isCallable());
					descriptionSQL.setCallableType(entityConfiguration.getSqlDelete().getCallableType());
					descriptionSQL.setSql(entityConfiguration.getSqlDelete().getSql());
					descriptionSQL.setSqlType(SQLStatementType.DELETE);
					descriptionSQL.setSuccessParameter(entityConfiguration.getSqlDelete().getSuccessParameter());
					descriptionSQL.setSuccessValue(entityConfiguration.getSqlDelete().getSuccessValue());
					entityCache.getDescriptionSql().put(SQLStatementType.DELETE, descriptionSQL);
				} else if (clazz == SQLDeleteAll.class) {
					descriptionSQL.setCallable(entityConfiguration.getSqlDeleteAll().isCallable());
					descriptionSQL.setCallableType(entityConfiguration.getSqlDeleteAll().getCallableType());
					descriptionSQL.setSql(entityConfiguration.getSqlDeleteAll().getSql());
					descriptionSQL.setSqlType(SQLStatementType.DELETE_ALL);
					descriptionSQL.setSuccessParameter(entityConfiguration.getSqlDeleteAll().getSuccessParameter());
					descriptionSQL.setSuccessValue(entityConfiguration.getSqlDeleteAll().getSuccessValue());
					entityCache.getDescriptionSql().put(SQLStatementType.DELETE_ALL, descriptionSQL);
				}
				if (descriptionSQL.isCallable()) {
					if ((descriptionSQL.getSql() == null) || ("".equals(descriptionSQL.getSql())))
						throw new EntityCacheException("Informe o SQL na configuração " + clazz.getSimpleName() + " da classe " + sourceClazz.getName());

					if (descriptionSQL.getCallableType() == null)
						throw new EntityCacheException("Informe o tipo do procedimento (PROCEDURE, FUNCTION) na configuração " + clazz.getSimpleName()
								+ " da classe " + sourceClazz.getName());

					if (descriptionSQL.getCallableType() == CallableType.PROCEDURE) {
						if (descriptionSQL.getSuccessParameter() == null) {
							throw new EntityCacheException(
									"Informe o NOME do parâmetro que informa se houve sucesso na execução do procedimento na configuração "
											+ clazz.getSimpleName() + " da classe " + sourceClazz.getName());
						}
						if (descriptionSQL.getSuccessValue() == null) {
							throw new EntityCacheException(
									"Informe o VALOR do parâmetro que informa se houve sucesso na execução do procedimento na configuração "
											+ clazz.getSimpleName() + " da classe " + sourceClazz.getName());
						}
					} else if (descriptionSQL.getCallableType() == CallableType.FUNCTION) {
						if (descriptionSQL.getSuccessValue() == null)
							throw new EntityCacheException("Informe o VALOR do parâmetro que informa se houve sucesso na execução da função na configuração "
									+ clazz.getSimpleName() + " da classe " + sourceClazz.getName());
					}
				}
			}
		}
	}

	private void readConfigurationSQL(Class<?> sourceClazz, EntityCache cache, Class<?>[] annotationClazz, FieldConfiguration fieldConfiguration)
			throws EntityCacheException {
		for (Class<?> fieldClazz : annotationClazz) {
			if (fieldConfiguration.isAnnotationPresent(fieldClazz)) {
				if (!((ReflectionUtils.isImplementsInterface(fieldConfiguration.getType(), Collection.class)
						|| ReflectionUtils.isImplementsInterface(fieldConfiguration.getType(), Set.class) || ReflectionUtils.isImplementsInterface(
						fieldConfiguration.getType(), Map.class)))) {
					throw new EntityCacheException("A configuração " + sourceClazz.getSimpleName()
							+ " só pode ser usada em coleções ou maps. Verifique o campo " + fieldConfiguration.getName() + " da classe "
							+ sourceClazz.getName());
				}

				DescriptionField descField = cache.getDescriptionField(fieldConfiguration.getName());
				DescriptionSQL descriptionSQL = new DescriptionSQL();
				if (fieldClazz == SQLInsert.class) {
					descriptionSQL.setCallable(fieldConfiguration.getSqlInsert().isCallable());
					descriptionSQL.setCallableType(fieldConfiguration.getSqlInsert().getCallableType());
					descriptionSQL.setSql(fieldConfiguration.getSqlInsert().getSql());
					descriptionSQL.setSqlType(SQLStatementType.INSERT);
					descriptionSQL.setSuccessParameter(fieldConfiguration.getSqlInsert().getSuccessParameter());
					descriptionSQL.setSuccessValue(fieldConfiguration.getSqlInsert().getSuccessValue());
					for (SQLInsertIdConfiguration id : fieldConfiguration.getSqlInsert().getParameterId()) {
						descriptionSQL.getParametersId().put(id.getParameterId(), id.getColumnName());
					}
					descField.getDescriptionSql().put(SQLStatementType.INSERT, descriptionSQL);
				} else if (fieldClazz == SQLUpdate.class) {
					descriptionSQL.setCallable(fieldConfiguration.getSqlUpdate().isCallable());
					descriptionSQL.setCallableType(fieldConfiguration.getSqlUpdate().getCallableType());
					descriptionSQL.setSql(fieldConfiguration.getSqlUpdate().getSql());
					descriptionSQL.setSqlType(SQLStatementType.UPDATE);
					descriptionSQL.setSuccessParameter(fieldConfiguration.getSqlUpdate().getSuccessParameter());
					descriptionSQL.setSuccessValue(fieldConfiguration.getSqlUpdate().getSuccessValue());
					descField.getDescriptionSql().put(SQLStatementType.UPDATE, descriptionSQL);
				} else if (fieldClazz == SQLDelete.class) {
					descriptionSQL.setCallable(fieldConfiguration.getSqlDelete().isCallable());
					descriptionSQL.setCallableType(fieldConfiguration.getSqlDelete().getCallableType());
					descriptionSQL.setSql(fieldConfiguration.getSqlDelete().getSql());
					descriptionSQL.setSqlType(SQLStatementType.DELETE);
					descriptionSQL.setSuccessParameter(fieldConfiguration.getSqlDelete().getSuccessParameter());
					descriptionSQL.setSuccessValue(fieldConfiguration.getSqlDelete().getSuccessValue());
					descField.getDescriptionSql().put(SQLStatementType.DELETE, descriptionSQL);
				} else if (fieldClazz == SQLDeleteAll.class) {
					descriptionSQL.setCallable(fieldConfiguration.getSqlDeleteAll().isCallable());
					descriptionSQL.setCallableType(fieldConfiguration.getSqlDeleteAll().getCallableType());
					descriptionSQL.setSql(fieldConfiguration.getSqlDeleteAll().getSql());
					descriptionSQL.setSqlType(SQLStatementType.DELETE_ALL);
					descriptionSQL.setSuccessParameter(fieldConfiguration.getSqlDeleteAll().getSuccessParameter());
					descriptionSQL.setSuccessValue(fieldConfiguration.getSqlDeleteAll().getSuccessValue());
					descField.getDescriptionSql().put(SQLStatementType.DELETE_ALL, descriptionSQL);
				}
				if (descriptionSQL.isCallable()) {
					if ((descriptionSQL.getSql() == null) || ("".equals(descriptionSQL.getSql()))) {
						throw new EntityCacheException("Informe o SQL na configuração SQLUpdate da classe " + sourceClazz.getName());
					}
					if (descriptionSQL.getCallableType() == null) {
						throw new EntityCacheException("Informe o tipo do procedimento (PROCEDURE, FUNCTION) na configuração SQLUpdate da classe "
								+ sourceClazz.getName());
					}
					if (descriptionSQL.getCallableType() == CallableType.PROCEDURE) {
						if (descriptionSQL.getSuccessParameter() == null) {
							throw new EntityCacheException(
									"Informe o NOME do parâmetro que informa se houve sucesso na execução do procedimento na configuração SQLUpdate no campo "
											+ fieldConfiguration.getName() + " da classe " + sourceClazz.getName());
						}
						if (descriptionSQL.getSuccessValue() == null) {
							throw new EntityCacheException(
									"Informe o VALOR do parâmetro que informa se houve sucesso na execução do procedimento na configuração SQLUpdate no campo "
											+ fieldConfiguration.getName() + " da classe " + sourceClazz.getName());
						}
					} else if (descriptionSQL.getCallableType() == CallableType.FUNCTION) {
						if (descriptionSQL.getSuccessValue() == null) {
							throw new EntityCacheException(
									"Informe o VALOR do parâmetro que informa se houve sucesso na execução da função na configuração SQLUpdate no campo "
											+ fieldConfiguration.getName() + " da classe " + sourceClazz.getName());
						}
					}
				}

			}
		}
	}

	private void readNamedQuery(EntityCache entityCache, EntityConfiguration entityConfiguration) {
		if (entityConfiguration.isAnnotationPresent(NamedQueries.class) || entityConfiguration.isAnnotationPresent(NamedQuery.class)) {
			/*
			 * Se possuir NamedQueries
			 */
			NamedQueryConfiguration[] queries = entityConfiguration.getNamedQueries();
			DescriptionNamedQuery namedQuery;
			for (NamedQueryConfiguration nq : queries) {
				namedQuery = new DescriptionNamedQuery();
				namedQuery.setName(nq.getName());
				namedQuery.setQuery(nq.getQuery());
				namedQuery.setCallableType(nq.getCallableType());
				namedQuery.setLockTimeout(nq.getLockTimeout());
				if ((nq.getResultClass() == null) || (nq.getResultClass() == Object.class))
					namedQuery.setResultClass(entityCache.getEntityClass());
				else
					namedQuery.setResultClass(nq.getResultClass());
				entityCache.addNamedQuery(namedQuery);
			}
		}
	}

	/**
	 * Método para ler configuração ElementCollection
	 * 
	 * throws Exception
	 * 
	 */
	private void readElementCollectionConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache) throws Exception {
		DescriptionField descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
		if (propertyAccessorFactory != null)
			descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));
		descriptionField.setFetchMode(fieldConfiguration.getFetch().getMode());
		descriptionField.setTableName(fieldConfiguration.getCollectionTable().getName());
		descriptionField.setSchema(fieldConfiguration.getCollectionTable().getSchema());
		descriptionField.setCatalog(fieldConfiguration.getCollectionTable().getCatalog());
		descriptionField.setFetchType(fieldConfiguration.getFetch().getType());
		descriptionField.setFieldType(FieldType.COLLECTION_TABLE);
		descriptionField.setTargetClass(entityCache.getEntityClass());
		descriptionField.setCascadeTypes(new CascadeType[] { CascadeType.ALL });
		descriptionField.setComment(fieldConfiguration.getComment());

		/*
		 * Adiciona os indices
		 */
		if (fieldConfiguration.isAnnotationPresent(Indexes.class) || fieldConfiguration.isAnnotationPresent(Index.class)) {
			IndexConfiguration[] indexes = fieldConfiguration.getIndexes();
			if (indexes != null) {
				for (IndexConfiguration index : indexes)
					descriptionField.getIndexes().add(
							new DescriptionIndex(entityCache).name(index.getName()).columnNames(index.getColumnNames()).schema(index.getSchema())
									.catalog(index.getCatalog()).unique(index.isUnique()));
			}
		}

		/*
		 * Adiciona as constraints únicas da collection table
		 */
		for (UniqueConstraintConfiguration uniqueConstraint : fieldConfiguration.getCollectionTable().getUniqueConstraints()) {
			DescriptionUniqueConstraint descriptionUniqueConstraint = new DescriptionUniqueConstraint(entityCache);
			descriptionUniqueConstraint.name(uniqueConstraint.getName()).columnNames(uniqueConstraint.getColumnNames());
			descriptionField.addUniqueConstraint(descriptionUniqueConstraint);
		}

		/*
		 * Se a coluna/colunas forem configuradas como únicas adiciona constraint única
		 */
		if ((fieldConfiguration.isAnnotationPresent(Column.class)) || (fieldConfiguration.isAnnotationPresent(Columns.class))) {
			DescriptionUniqueConstraint descriptionUniqueConstraint = new DescriptionUniqueConstraint(entityCache);
			descriptionUniqueConstraint.columnNames(fieldConfiguration.getUniqueColumnNames());
			if (descriptionUniqueConstraint.getColumnNames().length > 0)
				descriptionField.addUniqueConstraint(descriptionUniqueConstraint);
		}

		/*
		 * Se possuir Order
		 */
		if (fieldConfiguration.isAnnotationPresent(OrderBy.class))
			descriptionField.setOrderByClause(fieldConfiguration.getOrderByClause());

		DescriptionColumn descriptionColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
		descriptionColumn.setColumnName("".equals(fieldConfiguration.getSimpleColumn().getName()) ? "VALUE" : fieldConfiguration.getSimpleColumn().getName());
		descriptionColumn.setReferencedColumnName(descriptionColumn.getColumnName());
		descriptionColumn.setColumnDefinition(fieldConfiguration.getSimpleColumn().getColumnDefinition());
		descriptionColumn.setDescriptionField(descriptionField);
		descriptionColumn.setElementColumn(true);
		descriptionColumn.setLength(fieldConfiguration.getSimpleColumn().getLength());
		descriptionColumn.setPrecision(fieldConfiguration.getSimpleColumn().getPrecision());
		descriptionColumn.setRequired(fieldConfiguration.getSimpleColumn().isRequired());

		if (fieldConfiguration.isAnnotationPresent(MapKeyColumn.class)) {
			Class<?> clazz = ReflectionUtils.getGenericMapTypes(descriptionField.getField()).get(1);
			descriptionColumn.setElementCollectionType(clazz);
		} else {
			descriptionColumn.setColumnType(ColumnType.PRIMARY_KEY);
			descriptionColumn.setRequired(true);

			ParameterizedType listType = (ParameterizedType) descriptionField.getField().getGenericType();
			Class<?> clazz = (Class<?>) listType.getActualTypeArguments()[0];
			descriptionColumn.setElementCollectionType(clazz);
		}

		if (fieldConfiguration.isAnnotationPresent(MapKeyEnumerated.class)) {
			descriptionColumn.setEnumType(fieldConfiguration.getEnumeratedType());
		}

		if (fieldConfiguration.isAnnotationPresent(MapKeyTemporal.class)) {
			descriptionColumn.setTemporalType(fieldConfiguration.getTemporalType());
		}

		DescriptionColumn descriptionJoinColumn;
		boolean compositeId = fieldConfiguration.getCollectionTable().getJoinColumns().length > 1;
		for (JoinColumnConfiguration j : fieldConfiguration.getCollectionTable().getJoinColumns()) {
			descriptionJoinColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
			descriptionJoinColumn.setColumnName(j.getName());
			descriptionJoinColumn.setReferencedColumnName((j.getReferencedColumnName() == null || "".equals(j.getReferencedColumnName())) ? j.getName() : j
					.getReferencedColumnName());
			descriptionJoinColumn.setReferencedTableName(entityCache.getTableName());
			descriptionJoinColumn.setColumnType(ColumnType.PRIMARY_KEY);
			descriptionJoinColumn.setRequired(true);
			descriptionJoinColumn.setCompositeId(compositeId);
			descriptionJoinColumn.setForeignKey(true);
			descriptionJoinColumn.setColumnDefinition(j.getColumnDefinition());
			DescriptionColumn columnOwner = entityCache.getDescriptionColumnByColumnName(j.getName());
			if (columnOwner != null) {
				descriptionJoinColumn.setElementCollectionType(columnOwner.getFieldType());
			}

			descriptionField.addDescriptionColumns(descriptionJoinColumn);
		}

		descriptionField.addDescriptionColumns(descriptionColumn);

		if (fieldConfiguration.isAnnotationPresent(MapKeyColumn.class)) {
			DescriptionColumn descriptionColumnKey = new DescriptionColumn();
			descriptionColumnKey.setMapKeyColumn(true);
			descriptionColumnKey.setColumnType(ColumnType.PRIMARY_KEY);
			descriptionColumnKey.setRequired(true);
			descriptionColumnKey.setColumnName(fieldConfiguration.getMapKeyColumnName());
			descriptionColumnKey.setColumnDefinition(descriptionColumnKey.getColumnDefinition());
			descriptionColumnKey.setReferencedColumnName(descriptionColumnKey.getColumnName());
			Class<?> clazz = ReflectionUtils.getGenericMapTypes(descriptionField.getField()).get(0);
			descriptionColumnKey.setElementCollectionType(clazz);
			descriptionField.addDescriptionColumns(descriptionColumnKey);
			descriptionField.setFieldType(FieldType.COLLECTION_MAP_TABLE);

		}

		entityCache.addDescriptionField(descriptionField);
	}

	/**
	 * Método para ler as configurações do JoinTable
	 * 
	 * throws Exception
	 * 
	 */
	private void readJoinTableConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache) throws Exception {
		JoinTableConfiguration joinTableConfiguration = fieldConfiguration.getJoinTable();

		String tableName = joinTableConfiguration.getName();
		/*
		 * Se não definiu um nome para tabela, assuma classePai_classeFilha
		 */
		if ("".equals(tableName)) {
			tableName = fieldConfiguration.getType().getSimpleName().toLowerCase() + "_" + fieldConfiguration.getType().getSimpleName().toLowerCase();
		}

		DescriptionField descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
		if (propertyAccessorFactory != null)
			descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));

		descriptionField.setFieldType(FieldType.JOIN_TABLE);
		descriptionField.setTableName(tableName);
		descriptionField.setComment(fieldConfiguration.getComment());
		readRemoteConfiguration(descriptionField, fieldConfiguration, entityCache);

		if (fieldConfiguration.isAnnotationPresent(Indexes.class) || fieldConfiguration.isAnnotationPresent(Index.class)) {
			IndexConfiguration[] indexes = fieldConfiguration.getIndexes();
			if (indexes != null) {
				for (IndexConfiguration index : indexes)
					descriptionField.getIndexes().add(
							new DescriptionIndex(entityCache).name(index.getName()).columnNames(index.getColumnNames()).schema(index.getSchema())
									.catalog(index.getCatalog()).unique(index.isUnique()));
			}
		}

		/*
		 * Adiciona as constraints únicas da jointable
		 */
		for (UniqueConstraintConfiguration uniqueConstraint : joinTableConfiguration.getUniqueConstraints()) {
			DescriptionUniqueConstraint descriptionUniqueConstraint = new DescriptionUniqueConstraint(entityCache);
			descriptionUniqueConstraint.name(uniqueConstraint.getName()).columnNames(uniqueConstraint.getColumnNames());
			descriptionField.addUniqueConstraint(descriptionUniqueConstraint);
		}

		/*
		 * Se a coluna/colunas forem configuradas como únicas adiciona constraint única
		 */
		if ((fieldConfiguration.isAnnotationPresent(Column.class)) || (fieldConfiguration.isAnnotationPresent(Columns.class))) {
			DescriptionUniqueConstraint descriptionUniqueConstraint = new DescriptionUniqueConstraint(entityCache);
			descriptionUniqueConstraint.columnNames(joinTableConfiguration.getUniqueColumnNames());
			if (descriptionUniqueConstraint.getColumnNames().length > 0)
				descriptionField.addUniqueConstraint(descriptionUniqueConstraint);
		}

		/*
		 * Se possuir Fetch
		 */
		if (fieldConfiguration.isAnnotationPresent(Fetch.class)) {
			descriptionField.setFetchMode(fieldConfiguration.getFetch().getMode());
			descriptionField.setFetchType(fieldConfiguration.getFetch().getType());
			descriptionField.setTargetClass(fieldConfiguration.getFetch().getTargetEntity());
			descriptionField.setStatement(fieldConfiguration.getFetch().getStatement());
		} else {
			descriptionField.setFetchMode(FetchMode.MANY_TO_MANY);
			descriptionField.setFetchType(FetchType.LAZY);
			descriptionField.setTargetClass(void.class);
		}

		DescriptionColumn descriptionColumn;

		if (descriptionField.getTargetClass() == void.class)
			descriptionField.setTargetClass(ReflectionUtils.getGenericType(fieldConfiguration.getField()));

		descriptionField.setTableName(joinTableConfiguration.getName());

		JoinColumnConfiguration[] joinColumns = joinTableConfiguration.getJoinColumns();
		boolean compositeId = joinColumns.length > 1;
		for (JoinColumnConfiguration joinColumn : joinColumns) {
			descriptionColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
			descriptionColumn.setColumnName(joinColumn.getName());
			descriptionColumn.setReferencedTableName(entityCache.getTableName());
			descriptionColumn.setColumnType(ColumnType.PRIMARY_KEY);
			descriptionColumn.setRequired(true);
			descriptionColumn.setCompositeId(compositeId);
			descriptionColumn.setForeignKey(true);
			descriptionColumn
					.setReferencedColumnName(((joinColumn.getReferencedColumnName() == null || joinColumn.getReferencedColumnName().equals(""))) ? joinColumn
							.getName() : joinColumn.getReferencedColumnName());
			descriptionColumn.setJoinColumn(true);
			descriptionColumn.setColumnDefinition(joinColumn.getColumnDefinition());
			descriptionField.addDescriptionColumns(descriptionColumn);
		}

		for (JoinColumnConfiguration joinColumn : joinTableConfiguration.getInversedJoinColumns()) {
			descriptionColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
			descriptionColumn.setColumnName(joinColumn.getName());
			descriptionColumn.setColumnType(ColumnType.PRIMARY_KEY);
			descriptionColumn.setRequired(true);
			descriptionColumn.setCompositeId(compositeId);
			descriptionColumn.setForeignKey(true);
			descriptionColumn
					.setReferencedColumnName((joinColumn.getReferencedColumnName() == null || joinColumn.getReferencedColumnName().equals("")) ? joinColumn
							.getName() : joinColumn.getReferencedColumnName());
			descriptionColumn.setInversedJoinColumn(true);
			descriptionColumn.setColumnDefinition(joinColumn.getColumnDefinition());
			descriptionField.addDescriptionColumns(descriptionColumn);
		}

		/*
		 * Se possuir Cascade
		 */
		if (fieldConfiguration.isAnnotationPresent(Cascade.class))
			descriptionField.setCascadeTypes(fieldConfiguration.getCascadeTypes());

		/*
		 * Se possuir Order
		 */
		if (fieldConfiguration.isAnnotationPresent(OrderBy.class))
			descriptionField.setOrderByClause(fieldConfiguration.getOrderByClause());

		entityCache.addDescriptionField(descriptionField);
	}

	/**
	 * Método para ler as configurações do CompositeId
	 * 
	 * throws Exception
	 * 
	 * @param model
	 * 
	 */
	private void readCompositeIdConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache, PersistenceModelConfiguration model)
			throws Exception {
		DescriptionField descriptionField = null;
		if (fieldConfiguration.isAnnotationPresent(Column.class)) {
			ColumnConfiguration columnConfiguration = fieldConfiguration.getColumns().iterator().next();
			String columnName = columnConfiguration.getName();
			DescriptionColumn descriptionColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
			descriptionColumn.setRequired(fieldConfiguration.isAnnotationPresent(CompositeId.class));
			descriptionColumn.setColumnName(columnName);
			descriptionColumn.setCompositeId(fieldConfiguration.isAnnotationPresent(CompositeId.class));
			descriptionColumn.setExpression(fieldConfiguration.getName());
			descriptionColumn.setReferencedColumnName(columnConfiguration.getInversedColumn().equals("") ? columnConfiguration.getName() : columnConfiguration
					.getInversedColumn());
			descriptionColumn.setLength(columnConfiguration.getLength());
			descriptionColumn.setPrecision(columnConfiguration.getPrecision());
			descriptionColumn.setScale(columnConfiguration.getScale());
			descriptionColumn.setColumnDefinition(columnConfiguration.getColumnDefinition());

			if (fieldConfiguration.isAnnotationPresent(CompositeId.class)) {
				descriptionColumn.setColumnType(ColumnType.PRIMARY_KEY);
				descriptionColumn.setRequired(true);
			}

			descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
			if (propertyAccessorFactory != null)
				descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));

			descriptionField.addDescriptionColumns(descriptionColumn);
			descriptionField.setComment(fieldConfiguration.getComment());

			entityCache.addDescriptionColumn(descriptionColumn);

			/*
			 * Se possuir Enumerated
			 */
			if (fieldConfiguration.isAnnotationPresent(Enumerated.class)) {
				readEnumeratedConfiguration(fieldConfiguration, entityCache, model, descriptionColumn);
			}

			/*
			 * Se for ForeignKey
			 */

			if (fieldConfiguration.isAnnotationPresent(ForeignKey.class)) {
				descriptionField.setFieldType(FieldType.RELATIONSHIP);
				descriptionField.setFetchType(fieldConfiguration.getForeignKey().getType());
				descriptionField.setFetchMode(fieldConfiguration.getForeignKey().getMode());
				descriptionField.setStatement(fieldConfiguration.getForeignKey().getStatement());
				descriptionField.setTargetClass(fieldConfiguration.getType());
				descriptionField.setForeignKeyName(fieldConfiguration.getForeignKey().getName());
				descriptionColumn.setDescriptionField(descriptionField);
				descriptionColumn.setForeignKey(true);
				readRemoteConfiguration(descriptionField, fieldConfiguration, entityCache);
			} else
				readGeneratorConfiguration(fieldConfiguration, entityCache, descriptionColumn);

			entityCache.addDescriptionField(descriptionField);

		} else if (fieldConfiguration.isAnnotationPresent(Columns.class)) {
			ColumnConfiguration[] columnsConfiguration = fieldConfiguration.getColumns().toArray(new ColumnConfiguration[] {});
			DescriptionColumn descComposite;
			/*
			 * Se possuir Columns e ForeignKey Adiciona na Coleção de ForeignKeys foreignColumns
			 */
			if (fieldConfiguration.isAnnotationPresent(ForeignKey.class)) {
				descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
				if (propertyAccessorFactory != null)
					descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));

				descriptionField.setFieldType(FieldType.RELATIONSHIP);
				descriptionField.setFetchType(fieldConfiguration.getForeignKey().getType());
				descriptionField.setFetchMode(fieldConfiguration.getForeignKey().getMode());
				descriptionField.setStatement(fieldConfiguration.getForeignKey().getStatement());
				descriptionField.setTargetClass(fieldConfiguration.getType());
				descriptionField.setComment(fieldConfiguration.getComment());
				descriptionField.setForeignKeyName(fieldConfiguration.getForeignKey().getName());
				entityCache.addDescriptionField(descriptionField);
				readRemoteConfiguration(descriptionField, fieldConfiguration, entityCache);
			}

			for (ColumnConfiguration columnConfiguration : columnsConfiguration) {
				descComposite = new DescriptionColumn(entityCache, fieldConfiguration.getField());
				descComposite.setColumnName(columnConfiguration.getName());
				descComposite.setReferencedColumnName(columnConfiguration.getInversedColumn().equals("") ? columnConfiguration.getName() : columnConfiguration
						.getInversedColumn());
				descComposite.setRequired(fieldConfiguration.isAnnotationPresent(CompositeId.class));
				descComposite.setCompositeId(fieldConfiguration.isAnnotationPresent(CompositeId.class));
				descComposite.setColumnDefinition(columnConfiguration.getColumnDefinition());

				if ((fieldConfiguration.isAnnotationPresent(CompositeId.class)) || (fieldConfiguration.isAnnotationPresent(Id.class))) {
					descComposite.setColumnType(ColumnType.PRIMARY_KEY);
					descComposite.setRequired(true);
				}

				if (fieldConfiguration.isAnnotationPresent(ForeignKey.class)) {
					descComposite.setForeignKey(true);
					descriptionField.addDescriptionColumns(descComposite);
				}

				entityCache.addDescriptionColumn(descComposite);
			}
		}

		/*
		 * Se possuir Cascade
		 */
		if (fieldConfiguration.isAnnotationPresent(Cascade.class))
			descriptionField.setCascadeTypes(fieldConfiguration.getCascadeTypes());
	}

	/**
	 * Método para ler as configurações de uma Coluna normal (Coluna de dados)
	 * 
	 * throws Exception
	 * 
	 */
	private void readColumnConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache, PersistenceModelConfiguration model) throws Exception {
		String columnName = fieldConfiguration.getName().toLowerCase();
		String inversedColumn = columnName;

		DescriptionColumn descriptionColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
		descriptionColumn.setExternalFile(fieldConfiguration.isExternalFile());

		if (fieldConfiguration.isAnnotationPresent(Column.class)) {
			ColumnConfiguration column = fieldConfiguration.getSimpleColumn();
			columnName = column.getName();
			inversedColumn = column.getInversedColumn();
			descriptionColumn.setRequired(column.isRequired());
			descriptionColumn.setColumnDefinition(column.getColumnDefinition());
			if ((fieldConfiguration.getType() != java.util.Date.class) && (fieldConfiguration.getType() != java.sql.Date.class)) {
				descriptionColumn.setLength(column.getLength());
				descriptionColumn.setPrecision(column.getPrecision());
				descriptionColumn.setScale(column.getScale());
			}
			if ((descriptionColumn.getLength() == 0) && (fieldConfiguration.getType() == String.class)) {
				descriptionColumn.setLength(250);
				descriptionColumn.setPrecision(0);
				descriptionColumn.setScale(0);
			}
		}

		DescriptionField descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
		if (propertyAccessorFactory != null)
			descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));

		descriptionField.addDescriptionColumns(descriptionColumn);
		descriptionField.setComment(fieldConfiguration.getComment());
		readRemoteConfiguration(descriptionField, fieldConfiguration, entityCache);
		descriptionColumn.setIdSynchronism(fieldConfiguration.isAnnotationPresent(IdSynchronism.class));
		descriptionColumn.setColumnName(columnName);
		descriptionColumn.setExpression(fieldConfiguration.getName());
		descriptionColumn.setReferencedColumnName(inversedColumn);
		descriptionColumn.setDefaultValue(fieldConfiguration.getColumns().iterator().next().getDefaultValue());

		if (fieldConfiguration.isAnnotationPresent(Temporal.class)) {
			descriptionColumn.setTemporalType(fieldConfiguration.getTemporalType());
			descriptionColumn.setDatePattern(fieldConfiguration.getSimpleColumn().getDatePattern());
			descriptionColumn.setDateTimePattern(fieldConfiguration.getSimpleColumn().getDateTimePattern());
			descriptionColumn.setTimePattern(fieldConfiguration.getSimpleColumn().getTimePattern());
		}

		/*
		 * Se possuir Enumerated
		 */
		if (fieldConfiguration.isAnnotationPresent(Enumerated.class)) {
			readEnumeratedConfiguration(fieldConfiguration, entityCache, model, descriptionColumn);
		}

		/*
		 * Se possuir Version
		 */
		if (fieldConfiguration.isAnnotationPresent(Version.class)) {
			descriptionColumn.setVersioned(true);
		}

		/*
		 * Se possuir Lob
		 */
		if (fieldConfiguration.isAnnotationPresent(Lob.class)) {
			descriptionColumn.setLob(true);
			descriptionField.setFetchType(fieldConfiguration.getFetch().getType());
		}

		/*
		 * Se possuir Boolean
		 */
		if (fieldConfiguration.isAnnotationPresent(BooleanValue.class)) {
			if (validate) {
				if (fieldConfiguration.getType() != Boolean.class)
					throw new EntityCacheException("A configuração BooleanValue somente pode ser usada com campos do tipo Boolean.class. Verifique o campo "
							+ fieldConfiguration.getName() + " da classe " + entityCache.getEntityClass().getName());
			}

			descriptionColumn.setTrueValue(fieldConfiguration.getTrueValue());
			descriptionColumn.setFalseValue(fieldConfiguration.getFalseValue());
			descriptionColumn.setBooleanType(fieldConfiguration.getBooleanType());
			descriptionColumn.setBooleanReturnType(fieldConfiguration.getBooleanReturnType());
		}

		/*
		 * Se possuir Id ou CompositeId. define como PrimaryKey e adiciona na coleção de descriptioncolumns
		 */
		if (fieldConfiguration.isAnnotationPresent(Id.class)
				|| (fieldConfiguration.isAnnotationPresent(CompositeId.class) && fieldConfiguration.isAnnotationPresent(Column.class))) {
			descriptionColumn.setColumnType(ColumnType.PRIMARY_KEY);
			descriptionColumn.setRequired(true);

			/*
			 * Possuir CompositeId
			 */
			if (fieldConfiguration.isAnnotationPresent(CompositeId.class) && fieldConfiguration.isAnnotationPresent(Column.class)
					&& fieldConfiguration.isAnnotationPresent(ForeignKey.class)) {
				descriptionColumn.setCompositeId(true);
				descriptionColumn.setReferencedColumnName(columnName);
			}

			/*
			 * Se possuir Enumerated
			 */
			if (fieldConfiguration.isAnnotationPresent(Enumerated.class)) {
				readEnumeratedConfiguration(fieldConfiguration, entityCache, model, descriptionColumn);
			}

			/*
			 * Se possuir sequences
			 */
			readGeneratorConfiguration(fieldConfiguration, entityCache, descriptionColumn);
		}

		/*
		 * Se a coluna/colunas forem configuradas como únicas adiciona constraint única
		 */
		if ((fieldConfiguration.isAnnotationPresent(Column.class)) || (fieldConfiguration.isAnnotationPresent(Columns.class))) {
			DescriptionUniqueConstraint descriptionUniqueConstraint = new DescriptionUniqueConstraint(entityCache);
			descriptionUniqueConstraint.columnNames(fieldConfiguration.getUniqueColumnNames());
			if (descriptionUniqueConstraint.getColumnNames().length > 0)
				entityCache.addUniqueConstraint(descriptionUniqueConstraint);
		}

		descriptionColumn.setColumnName(columnName);
		descriptionColumn.setDefaultValue(fieldConfiguration.getColumns().iterator().next().getDefaultValue());
		entityCache.addDescriptionColumn(descriptionColumn);

		entityCache.addDescriptionField(descriptionField);
	}

	public void readEnumeratedConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache, PersistenceModelConfiguration model,
			DescriptionColumn descriptionColumn) throws EntityCacheException {
		if (fieldConfiguration.isAnnotationPresent(Enumerated.class)) {
			Map<String, String> enumValues = new HashMap<String, String>();

			Class<?> enumClass = fieldConfiguration.getType();
			if (!ReflectionUtils.isExtendsClass(Enum.class, enumClass)) {
				throw new EntityCacheException("O campo " + fieldConfiguration.getName() + " da classe " + entityCache.getEntityClass().getName()
						+ " configurado com Enumerated deve ser do tipo Enum.");
			}

			/*
			 * Se possuir EnumValue na Classe de Enum
			 */
			EntityConfiguration enumConfiguration = model.getEntities().get(enumClass);
			if (enumConfiguration != null) {
				if (enumConfiguration.isAnnotationPresent(EnumValues.class)) {
					EnumValueConfiguration[] enumValuesConfiguration = enumConfiguration.getEnumValues();

					/*
					 * Se quantidade de constantes da Classe de Enum difere da quantidade de EnumValue.
					 */
					if (enumValuesConfiguration.length != enumClass.getEnumConstants().length)
						throw new EntityCacheException("A quantidade de valores definidos no Enum " + enumClass.getName()
								+ " difere da quantidade de valores definidos na configuração EnumValues.\nEnumValues->"
								+ Arrays.toString(enumValuesConfiguration) + "\n" + enumClass.getName() + "->" + Arrays.toString(enumClass.getEnumConstants()));

					for (EnumValueConfiguration value : enumValuesConfiguration)
						enumValues.put(value.getEnumValue(), value.getValue());

				} else {
					for (Object value : enumClass.getEnumConstants())
						enumValues.put(value.toString(), value.toString());
				}
			} else {
				for (Object value : enumClass.getEnumConstants())
					enumValues.put(value.toString(), value.toString());
			}

			descriptionColumn.setEnumType(fieldConfiguration.getEnumeratedType());
			descriptionColumn.setEnumValues(enumValues);
		}
	}

	private void readGeneratorConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache, DescriptionColumn descriptionColumn)
			throws EntityCacheException {
		/*
		 * Se possuir TableGenerator ou SequenceGenerator ou se possuir
		 * 
		 * GeneratedValue vai criar um DescriptionSequence
		 */
		if (fieldConfiguration.isAnnotationPresent(GeneratedValue.class)) {

			/*
			 * Se possuir GeneratedValue, adiciona um sequence na DescriptionColumn
			 */
			if (fieldConfiguration.isAnnotationPresent(GeneratedValue.class)) {
				descriptionColumn.setGeneratedType(fieldConfiguration.getGeneratedType());

				/*
				 * Se for um generator IDENTIFY
				 */
				if (descriptionColumn.getGeneratedType().equals(GeneratedType.IDENTITY)) {
					DescriptionGenerator descriptionGenerator = new DescriptionGenerator();
					descriptionGenerator.setGeneratedType(GeneratedType.IDENTITY);
					descriptionColumn.getGenerators().put(GeneratedType.IDENTITY, descriptionGenerator);
				}

			}

			/*
			 * Se possuir TableGenerator
			 */
			if (fieldConfiguration.isAnnotationPresent(TableGenerator.class)) {
				if (StringUtils.isEmpty(fieldConfiguration.getTableGenerator().getValue()))
					throw new EntityCacheException("Informe o valor para o TableGenerator do campo " + fieldConfiguration.getName() + " da Classe "
							+ entityCache.getEntityClass().getName());

				DescriptionGenerator descriptionGenerator = new DescriptionGenerator();

				descriptionGenerator.setCatalog(fieldConfiguration.getTableGenerator().getCatalog());
				descriptionGenerator.setInitialValue(fieldConfiguration.getTableGenerator().getInitialValue());
				descriptionGenerator.setPkColumnName(fieldConfiguration.getTableGenerator().getPkColumnName());
				descriptionGenerator.setSchema(fieldConfiguration.getTableGenerator().getSchema());
				descriptionGenerator.setTableName(fieldConfiguration.getTableGenerator().getName());
				descriptionGenerator.setValueColumnName(fieldConfiguration.getTableGenerator().getValueColumnName());
				descriptionGenerator.setGeneratedType(GeneratedType.TABLE);
				descriptionGenerator.setValue(fieldConfiguration.getTableGenerator().getValue());
				descriptionColumn.getGenerators().put(GeneratedType.TABLE, descriptionGenerator);
			} else {
				/*
				 * Valor default caso não encontre TableGenerator
				 */
				DescriptionGenerator descriptionGenerator = new DescriptionGenerator();
				descriptionGenerator.setInitialValue(1);
				descriptionGenerator.setPkColumnName("GEN_ID");
				descriptionGenerator.setValue(entityCache.getTableName() + "_" + descriptionColumn.getColumnName());
				descriptionGenerator.setTableName("ANTEROS_SEQUENCES");
				descriptionGenerator.setValueColumnName("GEN_VALUE");
				descriptionGenerator.setCatalog(entityCache.getCatalog());
				descriptionGenerator.setSchema(entityCache.getSchema());
				descriptionGenerator.setGeneratedType(GeneratedType.TABLE);
				descriptionGenerator.setValue(entityCache.getTableName() + "_" + descriptionColumn.getColumnName());
				descriptionColumn.getGenerators().put(GeneratedType.TABLE, descriptionGenerator);
			}

			/*
			 * Se possuir SequenceGenerator
			 */
			if (fieldConfiguration.isAnnotationPresent(SequenceGenerator.class)) {
				DescriptionGenerator descriptionGenerator = new DescriptionGenerator();
				descriptionGenerator.setCatalog(fieldConfiguration.getSequenceGenerator().getCatalog());
				descriptionGenerator.setInitialValue(fieldConfiguration.getSequenceGenerator().getInitialValue());
				descriptionGenerator.setSchema(fieldConfiguration.getSequenceGenerator().getSchema());
				descriptionGenerator.setStartsWith(fieldConfiguration.getSequenceGenerator().getStartsWith());
				descriptionGenerator.setSequenceName(fieldConfiguration.getSequenceGenerator().getSequenceName());
				descriptionGenerator.setGeneratedType(GeneratedType.SEQUENCE);
				descriptionColumn.getGenerators().put(GeneratedType.SEQUENCE, descriptionGenerator);
			} else {
				/*
				 * Valor default caso o sequence não tenha sido configurado
				 */
				DescriptionGenerator descriptionGenerator = new DescriptionGenerator();
				descriptionGenerator.setInitialValue(1);
				descriptionGenerator.setSequenceName("ANTEROS_SEQ" + entityCache.getTableName());
				descriptionGenerator.setCatalog(entityCache.getCatalog());
				descriptionGenerator.setSchema(entityCache.getSchema());
				descriptionGenerator.setGeneratedType(GeneratedType.SEQUENCE);
				descriptionColumn.getGenerators().put(GeneratedType.SEQUENCE, descriptionGenerator);
			}

		}
	}

	/**
	 * Se possuir ForeignKey Cria um DescriptionField. Verifica de possui Fetch e seta suas propiedades no
	 * DescriptionField. Se não possuir CompositeId cria um DescriptionColumn e adiciona na coleção de ForeignKeys. Por
	 * fim, faz a união das coleções de DescriptionField com ForeignKeys
	 * 
	 * throws Exception
	 */
	private void readForeignKeyConfiguration(FieldConfiguration fieldConfiguration, EntityCache entityCache, PersistenceModelConfiguration model)
			throws Exception {
		try {
			DescriptionColumn descriptionColumn = new DescriptionColumn(entityCache, fieldConfiguration.getField());
			descriptionColumn.setColumnName(fieldConfiguration.getName().toLowerCase());
			descriptionColumn.setForeignKey(true);
			FieldConfiguration foreingKeyField = getIdFieldConfiguration(fieldConfiguration.getType(), model);
			if (foreingKeyField == null)
				throw new EntityCacheException("Campo " + fieldConfiguration.getName() + "(" + descriptionColumn.getReferencedColumnName()
						+ ") não encontrado na classe " + fieldConfiguration.getType() + " ou a classe não foi adicionada nas configurações.");

			if (fieldConfiguration.isAnnotationPresent(Column.class)) {
				ColumnConfiguration simpleColumn = fieldConfiguration.getSimpleColumn();
				descriptionColumn.setColumnName(simpleColumn.getName());
				descriptionColumn.setLength(simpleColumn.getLength());
				descriptionColumn.setColumnDefinition(simpleColumn.getColumnDefinition());
				descriptionColumn.setPrecision(simpleColumn.getPrecision());
				descriptionColumn.setScale(simpleColumn.getScale());
				descriptionColumn.setRequired(simpleColumn.isRequired());
				descriptionColumn.setReferencedColumnName("".equals(simpleColumn.getInversedColumn()) ? simpleColumn.getName() : simpleColumn
						.getInversedColumn());
			} else {
				if (foreingKeyField.isAnnotationPresent(Column.class)) {

				}
			}
			if (fieldConfiguration.isAnnotationPresent(Id.class)) {
				descriptionColumn.setColumnType(ColumnType.PRIMARY_KEY);
				descriptionColumn.setRequired(true);
			}

			DescriptionField descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
			if (propertyAccessorFactory != null)
				descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));

			descriptionField.setFieldType(FieldType.RELATIONSHIP);
			descriptionField.setTargetClass(fieldConfiguration.getType());
			descriptionField.setFetchType(fieldConfiguration.getForeignKey().getType());
			descriptionField.setFetchMode(fieldConfiguration.getForeignKey().getMode());
			descriptionField.setComment(fieldConfiguration.getComment());
			descriptionField.setForeignKeyName(fieldConfiguration.getForeignKey().getName());

			if (fieldConfiguration.isAnnotationPresent(Fetch.class)) {
				descriptionField.setFetchMode(fieldConfiguration.getFetch().getMode());
				descriptionField.setFetchType(fieldConfiguration.getFetch().getType());
				descriptionField.setStatement(fieldConfiguration.getFetch().getStatement());

				if (fieldConfiguration.isAnnotationPresent(OrderBy.class))
					descriptionField.setOrderByClause(fieldConfiguration.getOrderByClause());
			}

			descriptionColumn.setExpression(fieldConfiguration.getName() + "." + foreingKeyField.getName());
			descriptionColumn.setDescriptionField(descriptionField);
			descriptionColumn.setIdSynchronism(fieldConfiguration.isAnnotationPresent(IdSynchronism.class));
			try {
				DatabaseTypesUtil.getSQLDataTypeFromFieldForeignKey(fieldConfiguration, descriptionColumn.getReferencedColumnName(), descriptionColumn);
			} catch (RuntimeException ex) {
				throw new EntityCacheException(ex.getMessage()
						+ ". Verifique se a coluna possuí o mesmo nome na outra entidade relacionada. Caso seja diferente informe o inversedColumn.");
			}
			descriptionField.addDescriptionColumns(descriptionColumn);

			/*
			 * Se possuir Cascade
			 */
			if (fieldConfiguration.isAnnotationPresent(Cascade.class))
				descriptionField.setCascadeTypes(fieldConfiguration.getCascadeTypes());

			entityCache.addDescriptionField(descriptionField);
			entityCache.addDescriptionColumn(descriptionColumn);

			if ((descriptionField.getFetchType() == FetchType.LAZY) && (descriptionField.isRelationShip()) && !(descriptionField.isRequired())) {
				throw new EntityCacheException(
						"Não é permitido usar LAZY em chaves estrangeiras que não sejam obrigatórias pois isto inviabiliza a comparação do objeto com nulo devido ao uso de proxy.");
			}

		} catch (Exception ex) {
			throw new EntityCacheException("Erro lendo configuração ForeignKey  do campo " + fieldConfiguration.getName() + " da classe "
					+ entityCache.getEntityClass().getName() + ". " + ex.getMessage());
		}
	}

	private void readFetchConfigurations(EntityCache entityCache, FieldConfiguration fieldConfiguration) throws Exception {
		DescriptionField descriptionField = new DescriptionField(entityCache, fieldConfiguration.getField());
		if (propertyAccessorFactory != null)
			descriptionField.setPropertyAccessor(propertyAccessorFactory.createAccessor(entityCache.getEntityClass(), fieldConfiguration.getField()));

		descriptionField.setFieldType(FieldType.RELATIONSHIP);
		descriptionField.setFetchMode(fieldConfiguration.getFetch().getMode());
		descriptionField.setComment(fieldConfiguration.getComment());
		if (fieldConfiguration.isAnnotationPresent(ForeignKey.class))
			descriptionField.setForeignKeyName(fieldConfiguration.getForeignKey().getName());

		descriptionField.setTargetClass(fieldConfiguration.getFetch().getTargetEntity());

		readRemoteConfiguration(descriptionField, fieldConfiguration, entityCache);
		/*
		 * Verifica se é uma coleção. Se não estiver tipada recupera o tipo e seta no TargetEntity
		 */
		if (ReflectionUtils.isCollection(fieldConfiguration.getType())) {
			descriptionField.setFieldType(FieldType.COLLECTION_ENTITY);
			if (descriptionField.getTargetClass() == void.class)
				descriptionField.setTargetClass(ReflectionUtils.getGenericType(fieldConfiguration.getField()));
		}

		if ((descriptionField.getTargetClass() == void.class) && (descriptionField.getModeType() == FetchMode.FOREIGN_KEY)) {
			descriptionField.setTargetClass(fieldConfiguration.getField().getType());
		}

		/*
		 * Se targetEntity não possuir tipo, retorna Exception
		 */
		if (descriptionField.getModeType() == FetchMode.ONE_TO_MANY && descriptionField.getTargetClass() == void.class)
			throw new EntityCacheException("O campo " + fieldConfiguration + " da entidade " + fieldConfiguration.getType().getName()
					+ " deve ser tipado usando generics ou informado o tipo através do argumento targetEntity da configuração Fetch.");

		/*
		 * Se possuir Cascade
		 */
		if (fieldConfiguration.isAnnotationPresent(Cascade.class))
			descriptionField.setCascadeTypes(fieldConfiguration.getCascadeTypes());

		if ((descriptionField.getModeType() == FetchMode.ONE_TO_MANY) || (descriptionField.getModeType() == FetchMode.FOREIGN_KEY))
			descriptionField.setDescriptionMappedBy(new DescriptionMappedBy(fieldConfiguration.getFetch().getMappedBy()));

		descriptionField.setFetchType(fieldConfiguration.getFetch().getType());

		if (fieldConfiguration.isAnnotationPresent(OrderBy.class))
			descriptionField.setOrderByClause(fieldConfiguration.getOrderByClause());
		entityCache.addDescriptionField(descriptionField);

	}

	private void readRemoteConfiguration(DescriptionField descriptionField, FieldConfiguration fieldConfiguration, EntityCache entityCache) {
		if (fieldConfiguration.isAnnotationPresent(Remote.class)) {
			descriptionField.setMobileActionExport(fieldConfiguration.getRemote().getMobileActionExport());
			descriptionField.setMobileActionImport(fieldConfiguration.getRemote().getMobileActionImport());
			descriptionField.setDisplayLabel(fieldConfiguration.getRemote().getDisplayLabel());
			descriptionField.setExportOrderToSendData(fieldConfiguration.getRemote().getExportOrderToSendData());
			descriptionField.setExportColumns(fieldConfiguration.getRemote().getExportColumns());
			descriptionField.setExportConnectivityType(fieldConfiguration.getRemote().getExportConnectivityType());
			descriptionField.setImportConnectivityType(fieldConfiguration.getRemote().getImportConnectivityType());

			RemoteParamConfiguration[] exportParams = fieldConfiguration.getRemote().getExportParams();
			for (RemoteParamConfiguration param : exportParams)
				descriptionField.getExportParams().put(param.getParamOrder(),
						new ParamDescription(param.getParamName(), param.getParamOrder(), param.getParamValue()));

			RemoteParamConfiguration[] importParams = fieldConfiguration.getRemote().getImportParams();
			for (RemoteParamConfiguration param : importParams)
				descriptionField.getImportParams().put(param.getParamOrder(),
						new ParamDescription(param.getParamName(), param.getParamOrder(), param.getParamValue()));
		}
	}

	/**
	 * Adiciona uma classe no entityCache.
	 * 
	 * param clazz param entityCache
	 */
	private void addEntityClass(Class<? extends Serializable> clazz, EntityCache cache) {
		entities.put(clazz, cache);
	}

	private void addConverter(ConverterCache converter) {
		converters.add(converter);
	}

	private FieldConfiguration getIdFieldConfiguration(Class<?> clazz, PersistenceModelConfiguration model) {
		EntityConfiguration entityConfiguration = model.getEntities().get(clazz);
		if (entityConfiguration == null)
			return null;

		FieldConfiguration field = null;
		for (FieldConfiguration fieldConfiguration : entityConfiguration.getFields()) {
			if (fieldConfiguration.isAnnotationPresent(Id.class))
				field = fieldConfiguration;
			else if (fieldConfiguration.isAnnotationPresent(CompositeId.class) && !fieldConfiguration.isAnnotationPresent(ForeignKey.class))
				field = fieldConfiguration;

		}
		if (clazz.getSuperclass() != Object.class) {
			field = getIdFieldConfiguration(clazz.getSuperclass(), model);
		}
		return field;
	}

	public EntityCache[] getEntitiesBySuperClass(Class<?> superClass) {
		List<EntityCache> result = new ArrayList<EntityCache>();
		for (EntityCache entityCache : entities.values()) {
			if ((ReflectionUtils.isExtendsClass(superClass, entityCache.getEntityClass())) && (entityCache.getEntityClass() != superClass))
				result.add(entityCache);
		}
		return result.toArray(new EntityCache[] {});
	}

	public EntityCache getEntitySuperClass(Class<?> clazz) {
		for (EntityCache entityCache : entities.values()) {
			if ((ReflectionUtils.isExtendsClass(entityCache.getEntityClass(), clazz)) && (entityCache.getEntityClass() != clazz))
				if (!entityCache.isInheritance()) {
					return entityCache;
				}
		}
		return null;
	}

	public EntityCache[] getEntitiesBySuperClass(EntityCache cache) {
		return getEntitiesBySuperClass(cache.getEntityClass());
	}

	public EntityCache[] getEntitiesBySuperClassIncluding(Class<?> superClass) {
		List<EntityCache> result = new ArrayList<EntityCache>();
		for (EntityCache entityCache : entities.values()) {
			if ((ReflectionUtils.isExtendsClass(superClass, entityCache.getEntityClass())))
				result.add(entityCache);
		}
		return result.toArray(new EntityCache[] {});
	}

	public EntityCache[] getEntitiesBySuperClassIncluding(EntityCache cache) {
		return getEntitiesBySuperClassIncluding(cache.getEntityClass());
	}

	public DescriptionField getDescriptionFieldByTableName(String tableName) {
		for (EntityCache entityCache : entities.values()) {
			for (DescriptionField descField : entityCache.getDescriptionFields()) {
				if ((descField.getTableName() != null) && (descField.getTableName().equalsIgnoreCase(tableName))) {
					return descField;
				}
			}
		}
		return null;
	}

	/**
	 * Retorna EntityCache da Classe
	 * 
	 * param clazz return
	 */
	public EntityCache getEntityCache(Class<? extends Object> clazz) {
		return this.entities.get(clazz);
	}

	public Map<Class<? extends Serializable>, EntityCache> getEntities() {
		return entities;
	}

	public void setEntities(Map<Class<? extends Serializable>, EntityCache> entities) {
		this.entities = entities;
	}

	/**
	 * Retorna todas as EntityCache
	 * 
	 * return
	 */
	public List<EntityCache> getEntitiesCache() {
		return new ArrayList<EntityCache>(this.entities.values());
	}

	/**
	 * Retorna AnnotationCahce
	 * 
	 * param className return
	 */
	public EntityCache getEntityCacheByClassName(String className) {
		for (EntityCache entityCache : entities.values()) {
			if (className.equalsIgnoreCase(entityCache.getEntityClass().getSimpleName()))
				return entityCache;
		}
		return null;
	}

	/**
	 * Retorna EntityCache da Tabela
	 * 
	 * param tableName return
	 */
	public EntityCache getEntityCacheByTableName(String tableName) {
		int count = countEntityCacheByTableName(tableName);
		if (countEntityCacheByTableName(tableName) > 1) {
			throw new EntityCacheManagerException("Foram encontradas " + count + " classes com o mesmo nome de tabela " + tableName);
		}

		if ((tableName != null) && (!"".equals(tableName))) {
			for (EntityCache entityCache : entities.values()) {
				if ((tableName.equalsIgnoreCase(entityCache.getTableName())) && (!entityCache.hasDiscriminatorValue()))
					return entityCache;
			}
		}
		return null;
	}

	public int countEntityCacheByTableName(String tableName) {
		int result = 0;
		if ((tableName != null) && (!"".equals(tableName))) {
			for (EntityCache entityCache : entities.values()) {
				if ((tableName.equalsIgnoreCase(entityCache.getTableName())) && (!entityCache.hasDiscriminatorValue()))
					result++;
			}
		}
		return result;
	}

	public EntityCache getEntityCacheByName(String name) {
		if ((name != null) && (!"".equals(name))) {
			for (EntityCache entityCache : entities.values()) {
				if ((name.equalsIgnoreCase(entityCache.getEntityClass().getName())) && (!entityCache.hasDiscriminatorValue()))
					return entityCache;
			}
		}
		return null;
	}

	/**
	 * Retorna EntityCache da Tabela
	 * 
	 * param tableName return
	 */
	public List<EntityCache> getEntityCachesByTableName(String tableName) {
		List<EntityCache> result = new ArrayList<EntityCache>();
		if ((tableName != null) && (!"".equals(tableName))) {
			for (EntityCache entityCache : entities.values()) {
				if (tableName.equalsIgnoreCase(entityCache.getTableName()))
					result.add(entityCache);
			}
		}
		return result;
	}

	/**
	 * Retorna EntityCache[] da Tabela de classes concretas
	 * 
	 * param tableName return
	 */
	public EntityCache[] getAllConcreteEntityCacheByTableName(String tableName) {
		ArrayList<EntityCache> result = new ArrayList<EntityCache>();
		for (EntityCache annotionCache : entities.values()) {
			if (tableName.equalsIgnoreCase(annotionCache.getTableName()) && (!ReflectionUtils.isAbstractClass(annotionCache.getEntityClass())))
				result.add(annotionCache);
		}
		return result.toArray(new EntityCache[] {});
	}

	/**
	 * Retorna AnntationCache do Valor da DiscriminatorColumn
	 * 
	 * param columnValue return
	 */
	public EntityCache getEntityCacheByColumnValue(String columnValue) {
		for (EntityCache entityCache : entities.values()) {
			if (entityCache.hasDiscriminatorValue()) {
				if (columnValue.equalsIgnoreCase(entityCache.getDiscriminatorValue()))
					return entityCache;
			}
		}
		return null;
	}

	public EntityCache getEntityCache(Class<?> abstractClazz, String discriminatorValue) {
		for (EntityCache entityCache : entities.values()) {
			if (discriminatorValue.equals(entityCache.getDiscriminatorValue()) && ReflectionUtils.isExtendsClass(abstractClazz, entityCache.getEntityClass()))
				return entityCache;
		}
		throw new RuntimeException("Não existe classe com o valor " + discriminatorValue);
	}

	/**
	 * Retorna todas DescriptionColumn da Superclasse
	 * 
	 */
	public Set<DescriptionColumn> allDescriptionColumnsBySuperClass(Class<?> clazz) {
		Set<DescriptionColumn> columns = new HashSet<DescriptionColumn>();
		EntityCache[] caches = getEntitiesBySuperClass(clazz);
		for (EntityCache entityCache : caches)
			columns.addAll(entityCache.getDescriptionColumns());
		return columns;

	}

	public boolean isEntity(Object value) {
		return getEntityCache(value.getClass()) != null;
	}

	public boolean isEntity(Class<?> clazz) {
		return getEntityCache(clazz) != null;
	}

	public boolean isCompositeId(Object value) {
		EntityCache cache = getEntityCache(value.getClass());
		return cache.isCompositeId();
	}

	public List<DescriptionField> getAllDescriptionFieldBySuperclass(EntityCache cache) {
		List<DescriptionField> fields = new ArrayList<DescriptionField>();
		EntityCache[] caches = getAllConcreteEntityCacheByTableName(cache.getTableName());
		for (EntityCache entityCache : caches) {
			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				if (!fields.contains(descriptionField))
					fields.add(descriptionField);
			}

		}
		return fields;
	}

	public Class<?> getAnyConcreteClass(Class<?> sourceClass) {
		EntityCache sourceEntityCache = getEntityCache(sourceClass);
		if (sourceEntityCache == null)
			return null;
		EntityCache[] allConcrete = getAllConcreteEntityCacheByTableName(sourceEntityCache.getTableName());
		if (allConcrete.length == 0)
			return null;
		else
			return allConcrete[0].getEntityClass();
	}

	public String convertEnumToValue(Enum<?> en) {
		for (EntityCache entityCache : getEntities().values()) {
			for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
				if (descriptionField.getFieldClass() == en.getClass()) {
					return descriptionField.getEnumValue(en.toString());
				}
			}
		}
		return en.toString();
	}

}
