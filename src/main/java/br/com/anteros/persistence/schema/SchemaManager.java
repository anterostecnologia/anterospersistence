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
package br.com.anteros.persistence.schema;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.metadata.annotation.type.BooleanType;
import br.com.anteros.persistence.metadata.annotation.type.DiscriminatorType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionColumn;
import br.com.anteros.persistence.metadata.descriptor.DescriptionConvert;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.metadata.descriptor.DescriptionIndex;
import br.com.anteros.persistence.metadata.descriptor.DescriptionUniqueConstraint;
import br.com.anteros.persistence.metadata.identifier.IdentifierGenerator;
import br.com.anteros.persistence.metadata.identifier.IdentifierGeneratorFactory;
import br.com.anteros.persistence.metadata.identifier.SequenceGenerator;
import br.com.anteros.persistence.metadata.identifier.TableGenerator;
import br.com.anteros.persistence.schema.definition.ColumnSchema;
import br.com.anteros.persistence.schema.definition.ForeignKeySchema;
import br.com.anteros.persistence.schema.definition.GeneratorSchema;
import br.com.anteros.persistence.schema.definition.IndexSchema;
import br.com.anteros.persistence.schema.definition.ObjectSchema;
import br.com.anteros.persistence.schema.definition.SequenceGeneratorSchema;
import br.com.anteros.persistence.schema.definition.StoredFunctionSchema;
import br.com.anteros.persistence.schema.definition.StoredProcedureSchema;
import br.com.anteros.persistence.schema.definition.TableGeneratorSchema;
import br.com.anteros.persistence.schema.definition.TableSchema;
import br.com.anteros.persistence.schema.definition.UniqueKeySchema;
import br.com.anteros.persistence.schema.definition.ViewSchema;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.schema.exception.SchemaGeneratorException;
import br.com.anteros.persistence.schema.type.TableCreationType;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.dialect.ForeignKeyMetadata;
import br.com.anteros.persistence.sql.dialect.IndexMetadata;

public class SchemaManager implements Comparator<TableSchema> {

	protected SQLSession session;
	protected EntityCacheManager entityCacheManager;
	protected Writer createSchemaWriter;
	protected Writer dropSchemaWriter;
	protected Set<TableSchema> tables;
	protected Set<ViewSchema> views;
	protected Set<StoredProcedureSchema> procedures;
	protected Set<StoredFunctionSchema> functions;
	protected Set<GeneratorSchema> sequences;
	protected boolean ignoreDatabaseException;
	protected boolean createReferentialIntegrity;

	public SchemaManager(SQLSession session, EntityCacheManager entityCacheManager, boolean createReferentialIntegrity) {
		this.session = session;
		this.entityCacheManager = entityCacheManager;
		this.createReferentialIntegrity = createReferentialIntegrity;
	}

	/**
	 * Gera o schema
	 * 
	 * @throws Exception
	 */
	public void buildTablesSchema() throws Exception {
		if (tables == null) {
			tables = new LinkedHashSet<TableSchema>();
			views = new LinkedHashSet<ViewSchema>();
			sequences = new LinkedHashSet<GeneratorSchema>();
			procedures = new LinkedHashSet<StoredProcedureSchema>();
			functions = new LinkedHashSet<StoredFunctionSchema>();

			for (EntityCache entityCache : entityCacheManager.getEntities().values()) {

				String tableName = entityCache.getTableName();

				/*
				 * Pega os campos da entidade
				 */
				List<DescriptionField> fields = entityCache.getDescriptionFields();
				/*
				 * Se a entidade possuir discriminator column é uma herança pega os campos do pai
				 */
				if (entityCache.hasDiscriminatorValue())
					fields = entityCacheManager.getAllDescriptionFieldBySuperclass(entityCache);

				/*
				 * Se a tabela possuí campos. Pode ocorrer de ter uma entidade abstrata que ainda não possuí
				 * implementação
				 */
				if (fields.size() > 0) {
					TableSchema table = getTable(tableName);
					if (table == null) {
						table = new TableSchema();
						table.setName(tableName);
						tables.add(table);
					}
					/*
					 * Adiciona as colunas na tabela baseado nos campos da entidade
					 */

					ColumnSchema newColumn = null;
					for (DescriptionField descriptionField : fields) {
						/*
						 * Adiciona as colunas
						 */
						if (descriptionField.hasDescriptionColumn() && !descriptionField.isMapTable() && !descriptionField.isJoinTable()
								&& !descriptionField.isCollectionTable()) {
							for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
								newColumn = convertDescriptionColumnToColumnSchema(descriptionField, descriptionColumn);
								newColumn.setTable(table);
								if (!table.existsColumn(newColumn))
									table.addColumn(newColumn);

								/*
								 * Adiciona as sequências e tabelas de sequências
								 */
								if (descriptionColumn.hasGenerator()) {
									IdentifierGenerator generator = IdentifierGeneratorFactory.createGenerator(session, descriptionColumn);
									if (generator instanceof SequenceGenerator) {
										SequenceGeneratorSchema sequenceGeneratorSchema = new SequenceGeneratorSchema();
										sequenceGeneratorSchema.setName(((SequenceGenerator) generator).getSequenceName());
										sequenceGeneratorSchema.setInitialValue(((SequenceGenerator) generator).getInitialValue());
										sequenceGeneratorSchema.setCacheSize(0);
										sequenceGeneratorSchema.setIncrementSize(1);
										sequences.add(sequenceGeneratorSchema);
									} else if (generator instanceof TableGenerator) {
										TableGeneratorSchema tableGeneratorSchema = new TableGeneratorSchema();
										tableGeneratorSchema.setCatalogName(((TableGenerator) generator).getCatalog());
										tableGeneratorSchema.setSchemaName(((TableGenerator) generator).getSchema());
										tableGeneratorSchema.setName(((TableGenerator) generator).getTableName());
										tableGeneratorSchema.setPkColumnName(((TableGenerator) generator).getPkColumnName());
										tableGeneratorSchema.setValueColumnName(((TableGenerator) generator).getValueColumnName());
										sequences.add(tableGeneratorSchema);
									} else if (generator instanceof IdentifierGenerator) {
										newColumn.setAutoIncrement(true);
									}
								}
							}
						}

						/*
						 * Adiciona as chaves estrangeiras das colunas
						 */
						if (descriptionField.isRelationShip() && createReferentialIntegrity && !descriptionField.isMappedBy()) {
							ForeignKeySchema foreignKeySchema = new ForeignKeySchema(table, descriptionField.getForeignKeyName());
							foreignKeySchema.setReferencedTable(new TableSchema().setName(descriptionField.getTargetEntity().getTableName()));
							foreignKeySchema.setTable(table);
							if (StringUtils.isEmpty(foreignKeySchema.getName())) {
								try {
									foreignKeySchema.setName(generateForeignKeyConstraintName(table, table.getName(),
											descriptionField.getColumnsToString()));
								} catch (Exception e) {
									throw new SchemaGeneratorException("Não foi possível montar a chave estrangeira do campo ["
											+ descriptionField.getName() + "] da classe "
											+ descriptionField.getEntityCache().getEntityClass().getName());
								}
							}

							/*
							 * Adiciona as colunas na mesma da tabela relacionada pois alguns bancos validam a ordem dos
							 * campos. Ex: MySql
							 */
							for (DescriptionColumn column : descriptionField.getTargetEntity().getPrimaryKeyColumns()) {
								for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
									if (descriptionColumn.getReferencedColumnName().equalsIgnoreCase(column.getColumnName())) {
										foreignKeySchema.addColumns(
												new ColumnSchema(descriptionColumn.getColumnName(), descriptionField.getFieldClass()),
												new ColumnSchema(descriptionColumn.getReferencedColumn().getColumnName(), descriptionColumn
														.getReferencedColumn().getFieldType()));
									}
								}
							}
							if (foreignKeySchema.getColumns().size() > 0) {
								if (!table.existsForeignKey(foreignKeySchema))
									table.addForeignKey(foreignKeySchema);
							}
						}
						/*
						 * Adiciona os índices do campo
						 */
						if (descriptionField.getIndexes().size() > 0) {
							for (DescriptionIndex descriptionIndex : descriptionField.getIndexes()) {
								IndexSchema indexSchema = new IndexSchema();
								indexSchema.setName(descriptionIndex.getName());
								if (StringUtils.isEmpty(indexSchema.getName()))
									indexSchema.setName(generationIndexName(table.getName(), descriptionIndex.getColumnNames()[0], null));
								indexSchema.setTable(table);
								indexSchema.setUnique(descriptionIndex.isUnique());
								for (String colName : descriptionIndex.getColumnNames())
									indexSchema.addColumn(new ColumnSchema().setName(colName));

								if (!table.existsIndex(indexSchema))
									table.addIndex(indexSchema);
							}
						}
					}

					/*
					 * Adiciona a coluna para o DiscriminatorColumn
					 */
					if (entityCache.hasDiscriminatorColumn()) {
						newColumn = new ColumnSchema();
						ColumnDatabaseType dbType = null;
						if (DiscriminatorType.INTEGER.equals(entityCache.getDiscriminatorColumn().getDiscriminatorType())) {
							dbType = session.getDialect().convertJavaToDatabaseType(Integer.class);
						} else if (DiscriminatorType.CHAR.equals(entityCache.getDiscriminatorColumn().getDiscriminatorType())) {
							dbType = session.getDialect().convertJavaToDatabaseType(Character.class);
						} else {
							dbType = session.getDialect().convertJavaToDatabaseType(String.class);
						}
						newColumn.setAutoIncrement(false);
						newColumn.setName(entityCache.getDiscriminatorColumn().getColumnName());
						newColumn.setTypeSql(dbType.getName());
						newColumn.setType(String.class);
						newColumn.setNullable(false);
						if (entityCache.getDiscriminatorColumn().getLength() > 0)
							newColumn.setSize(entityCache.getDiscriminatorColumn().getLength());
						else
							newColumn.setSize(dbType.getDefaultSize());
						newColumn.setTable(table);
						if (!table.existsColumn(newColumn))
							table.addColumn(newColumn);
					}

					/*
					 * Adiciona a chave primária
					 */
					if (table.getPrimaryKey() == null) {
						DescriptionField[] pkFields = entityCache.getPrimaryKeyFields();
						for (DescriptionField descriptionField : pkFields) {
							for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
								table.addPrimaryKey(descriptionColumn.getColumnName(), descriptionField.getFieldClass());
								if (table.getPrimaryKey() != null) {
									table.getPrimaryKey().setName(generatePrimaryKeyConstraintName(tableName));
								}
							}
						}
					}

					/*
					 * Verifica campos da entidade que possuam tabelas anotadas
					 */
					createTablesOnFields(entityCache);

					/*
					 * Adiciona os índices da tabela
					 */
					for (DescriptionIndex descriptionIndex : entityCache.getIndexes()) {
						IndexSchema indexSchema = new IndexSchema();
						indexSchema.setName(descriptionIndex.getName());
						indexSchema.setTable(table);
						indexSchema.setUnique(descriptionIndex.isUnique());
						if (StringUtils.isEmpty(indexSchema.getName()))
							indexSchema.setName(generationIndexName(table.getName(), descriptionIndex.getColumnNames()[0], null));
						for (String colName : descriptionIndex.getColumnNames())
							indexSchema.addColumn(new ColumnSchema().setName(colName));
						if (!table.existsIndex(indexSchema))
							table.addIndex(indexSchema);
					}

					/*
					 * Adiciona as constraints únicas da tabela
					 */
					int serial = 0;
					for (DescriptionUniqueConstraint descriptionUniqueConstraint : entityCache.getUniqueConstraints()) {
						UniqueKeySchema uniqueKeySchema = new UniqueKeySchema();
						uniqueKeySchema.setName(descriptionUniqueConstraint.getName());
						if (StringUtils.isEmpty(uniqueKeySchema.getName()))
							uniqueKeySchema.setName(generateUniqueKeyConstraintName(table.getName(), serial,
									descriptionUniqueConstraint.getColumnNames()[0]));
						uniqueKeySchema.setTable(table);
						serial++;
						for (String colName : descriptionUniqueConstraint.getColumnNames())
							uniqueKeySchema.addColumn(new ColumnSchema().setName(colName));
						if (!table.existsUniqueKey(uniqueKeySchema))
							table.addUniqueKey(uniqueKeySchema);
					}
				}
			}

			Set<TableSchema> newList = new LinkedHashSet<TableSchema>();
			for (TableSchema tableSchema : tables) {
				buildDependencies(tables, tableSchema, newList);
				/*
				 * Verifica se todas as chaves estrangeiras possuem um indice para as colunas, se não encontrar
				 * adiciona.
				 */
				for (ForeignKeySchema fk : tableSchema.getForeignKeys()) {
					if (!tableSchema.existsIndex(fk.getColumnNames())) {
						IndexSchema index = new IndexSchema();
						index.addColumns(fk.getColumns());
						index.setTable(tableSchema);
						index.setName(generationIndexName(tableSchema.getName(), fk.getColumnNames()[0], null));
						tableSchema.addIndex(index);
					}
				}
			}

			tables = newList;
		}
	}

	protected void buildDependencies(Set<TableSchema> tables, TableSchema sourceTable, Set<TableSchema> newList) throws SchemaGeneratorException {

		if (sourceTable.getForeignKeys().size() == 0) {
			newList.add(sourceTable);
		}
		for (ForeignKeySchema foreignKeySchema : sourceTable.getForeignKeys()) {
			TableSchema targetTable = getTable(foreignKeySchema.getReferencedTable().getName());
			if (targetTable == null) {
				throw new SchemaGeneratorException("Ocorreu um erro gerando dependências da tabela " + sourceTable.getName()
						+ ". Não foi localizada tabela " + foreignKeySchema.getReferencedTable()
						+ ". Verifique se possuí alguma implementação CONCRETA para a tabela.");
			}
			if (!newList.contains(targetTable) && (!sourceTable.equals(targetTable)))
				buildDependencies(tables, targetTable, newList);
			newList.add(sourceTable);
		}

	}

	/**
	 * Verifica se uma tabela existe na lista de tabelas
	 * 
	 * @param tableName
	 *            Tabela
	 * @return
	 */
	public TableSchema getTable(String tableName) {
		for (TableSchema tableSchema : tables) {
			if (tableSchema.getName().equalsIgnoreCase(tableName))
				return tableSchema;
		}
		return null;
	}

	/**
	 * Converte campos com tabelas de uma Entidade em Tabelas do banco de dados
	 * 
	 * @param entityCache
	 *            Entidade
	 * @throws SchemaGeneratorException
	 */
	protected void createTablesOnFields(EntityCache entityCache) throws SchemaGeneratorException {
		String tableName;
		TableSchema table;
		ColumnSchema newColumn;
		for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
			if (descriptionField.isMapTable() || descriptionField.isCollectionTable() || descriptionField.isJoinTable()) {
				/*
				 * Cria tabela
				 */
				tableName = descriptionField.getTableName();
				table = new TableSchema();
				table.setName(tableName);
				if (!tables.contains(table)) {
					tables.add(table);
					ForeignKeySchema foreignKeySchema = null;
					ForeignKeySchema foreignKeySchemaTarget = null;
					/*
					 * Colunas e chave primária
					 */
					for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
						newColumn = convertDescriptionColumnToColumnSchema(descriptionField, descriptionColumn);
						if (!table.existsColumn(newColumn))
							table.addColumn(newColumn);
						/*
						 * Adiciona chave primária
						 */
						if (descriptionColumn.isPrimaryKey()) {
							table.addPrimaryKey(descriptionColumn.getColumnName(), descriptionColumn.getFieldType());
						}
					}

					if (descriptionField.isMapTable() || descriptionField.isCollectionTable()) {
						/*
						 * Adiciona as colunas e a chave estrangeira
						 */
						foreignKeySchema = new ForeignKeySchema(table, descriptionField.getForeignKeyName());
						foreignKeySchema.setReferencedTable(new TableSchema().setName(descriptionField.getTargetEntity().getTableName()));

						if (StringUtils.isEmpty(foreignKeySchema.getName()))
							foreignKeySchema.setName(generateForeignKeyConstraintName(table, table.getName(), descriptionField.getColumnsToString()));

						/*
						 * Colunas e chave primária
						 */
						for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
							/*
							 * Monta chave estrangeira
							 */
							if (descriptionColumn.isForeignKey()) {
								try {
									DescriptionColumn referenColumn = descriptionColumn.getReferencedColumn();
									if (referenColumn == null)
										referenColumn = entityCache.getDescriptionColumnByName(descriptionColumn.getReferencedColumnName());
									foreignKeySchema.addColumns(
											new ColumnSchema(descriptionColumn.getColumnName(), descriptionColumn.getFieldType()), new ColumnSchema(
													descriptionColumn.getReferencedColumnName(), referenColumn.getFieldType()));
								} catch (Exception e) {
									throw new SchemaGeneratorException("Não foi possível montar a chave estrangeira do campo ["
											+ descriptionField.getName() + "] da classe "
											+ descriptionColumn.getEntityCache().getEntityClass().getName());
								}
							}
						}
					} else if (descriptionField.isJoinTable()) {
						/*
						 * Adiciona as colunas e a chave estrangeira
						 */
						try {
							foreignKeySchema = new ForeignKeySchema(table, "");
							foreignKeySchema.setReferencedTable(new TableSchema().setName(entityCache.getTableName()));

							if (StringUtils.isEmpty(foreignKeySchema.getName()))
								foreignKeySchema.setName(generateForeignKeyConstraintName(table, table.getName(), descriptionField
										.getLastJoinColumn().getColumnName()));
						} catch (Exception e) {
							throw new SchemaGeneratorException("Não foi possível montar a chave estrangeira do campo de origem ["
									+ descriptionField.getName() + "] da classe " + entityCache.getEntityClass().getName());
						}

						try {
							foreignKeySchemaTarget = new ForeignKeySchema(table, "");
							foreignKeySchemaTarget.setReferencedTable(new TableSchema().setName(descriptionField.getTargetEntity().getTableName()));

							if (StringUtils.isEmpty(foreignKeySchemaTarget.getName()))
								foreignKeySchemaTarget.setName(generateForeignKeyConstraintName(table, table.getName(), descriptionField
										.getLastInversedColumn().getColumnName()));
						} catch (Exception e) {
							throw new SchemaGeneratorException("Não foi possível montar a chave estrangeira do campo de destino ["
									+ descriptionField.getName() + "] da classe " + entityCache.getEntityClass().getName());
						}

						/*
						 * Colunas e chave primária
						 */
						for (DescriptionColumn descriptionColumn : descriptionField.getDescriptionColumns()) {
							/*
							 * Monta chave estrangeira
							 */
							try {
								if (descriptionColumn.isForeignKey() && descriptionColumn.isJoinColumn()) {
									DescriptionColumn referencedColumn = descriptionColumn.getReferencedColumn();
									if (referencedColumn == null)
										referencedColumn = entityCache.getDescriptionColumnByName(descriptionColumn.getReferencedColumnName());
									foreignKeySchema.addColumns(
											new ColumnSchema(descriptionColumn.getColumnName(), descriptionColumn.getFieldType()), new ColumnSchema(
													descriptionColumn.getReferencedColumnName(), referencedColumn.getFieldType()));
								} else if (descriptionColumn.isForeignKey() && descriptionColumn.isInversedJoinColumn()) {
									DescriptionColumn referenColumn = descriptionColumn.getReferencedColumn();
									if (referenColumn == null)
										referenColumn = descriptionField.getTargetEntity().getDescriptionColumnByName(
												descriptionColumn.getReferencedColumnName());
									foreignKeySchemaTarget.addColumns(
											new ColumnSchema(descriptionColumn.getColumnName(), descriptionColumn.getFieldType()), new ColumnSchema(
													descriptionColumn.getReferencedColumnName(), referenColumn.getFieldType()));
								}
							} catch (Exception e) {
								throw new SchemaGeneratorException("Não foi possível montar a chave estrangeira do campo ["
										+ descriptionField.getName() + "] da classe " + descriptionColumn.getEntityCache().getEntityClass().getName());
							}
						}
					}

					/*
					 * Adiciona chave estrangeira
					 */
					if (createReferentialIntegrity) {
						if (foreignKeySchema != null) {
							if (foreignKeySchema.getColumns().size() > 0) {
								if (!table.existsForeignKey(foreignKeySchema))
									table.addForeignKey(foreignKeySchema);
							}
						}
						if (foreignKeySchemaTarget != null) {
							if (foreignKeySchemaTarget.getColumns().size() > 0) {
								if (!table.existsForeignKey(foreignKeySchemaTarget))
									table.addForeignKey(foreignKeySchemaTarget);
							}
						}
					}

					/*
					 * Adiciona os índices da tabela
					 */
					for (DescriptionIndex descriptionIndex : descriptionField.getIndexes()) {
						IndexSchema indexSchema = new IndexSchema();
						indexSchema.setName(descriptionIndex.getName());
						indexSchema.setTable(table);
						for (String colName : descriptionIndex.getColumnNames())
							indexSchema.addColumn(new ColumnSchema().setName(colName));
						if (!table.existsIndex(indexSchema))
							table.addIndex(indexSchema);
					}

					/*
					 * Adiciona as constraints únicas da tabela
					 */
					for (DescriptionUniqueConstraint descriptionUniqueConstraint : descriptionField.getUniqueConstraints()) {
						UniqueKeySchema uniqueKeySchema = new UniqueKeySchema();
						uniqueKeySchema.setName(descriptionUniqueConstraint.getName());
						uniqueKeySchema.setTable(table);
						for (String colName : descriptionUniqueConstraint.getColumnNames())
							uniqueKeySchema.addColumn(new ColumnSchema().setName(colName));
						if (!table.existsUniqueKey(uniqueKeySchema))
							table.addUniqueKey(uniqueKeySchema);
					}
				}
			}
		}
	}

	/**
	 * Converte um Descritor de um campo/coluna numa coluna do banco de dados
	 * 
	 * @param descriptionField
	 *            Descritor de campo
	 * @param descriptionColumn
	 *            Descritor de coluna
	 * @return Coluna do banco de dados
	 * @throws SchemaGeneratorException
	 */
	protected ColumnSchema convertDescriptionColumnToColumnSchema(DescriptionField descriptionField, DescriptionColumn descriptionColumn)
			throws SchemaGeneratorException {
		String columnName = descriptionColumn.getColumnName();
		ColumnSchema newColumn = new ColumnSchema();
		newColumn.setType(descriptionField.getFieldClass());
		ColumnDatabaseType dbType;
		if (descriptionColumn.hasConvert()) {
			DescriptionConvert convert = descriptionColumn.getConvert();
			if (convert == null)
				throw new SchemaGeneratorException("Não foi encontrado um conversor para a coluna " + descriptionColumn.getColumnName()
						+ " do  campo " + descriptionField.getField().getName() + " na Classe " + descriptionField.getEntityCache().getEntityClass());
			dbType = session.getDialect().convertJavaToDatabaseType(convert.getDatabaseColumnType());
		} else if (descriptionColumn.isEnumerated() || descriptionColumn.isDiscriminatorColumn()) {
			dbType = session.getDialect().convertJavaToDatabaseType(String.class);
		} else if (descriptionColumn.getDescriptionField().isElementCollection() && !descriptionColumn.isForeignKey()) {
			dbType = session.getDialect().convertJavaToDatabaseType(descriptionColumn.getElementCollectionType());
		} else if (descriptionColumn.isBoolean()) {
			if (descriptionColumn.getBooleanType() == BooleanType.INTEGER) {
				dbType = session.getDialect().convertJavaToDatabaseType(Integer.class);
			} else if (descriptionColumn.getBooleanType() == BooleanType.STRING) {
				dbType = session.getDialect().convertJavaToDatabaseType(String.class);
			} else {
				dbType = session.getDialect().convertJavaToDatabaseType(descriptionColumn.getFieldType());
			}
		} else if (descriptionColumn.isLob() && descriptionColumn.getFieldType() == String.class) {
			dbType = session.getDialect().convertJavaToDatabaseType(byte[].class);
		} else {
			dbType = session.getDialect().convertJavaToDatabaseType(descriptionColumn.getFieldType());
		}

		if (dbType == null)
			throw new SchemaGeneratorException("Tipo " + descriptionField.getFieldClass().getSimpleName() + " "
					+ (descriptionField.getFieldClass().isEnum() ? "(ENUM)" : "")
					+ " não disponível para este banco de dados. Verifique o campo ["+descriptionField.getField().getName()+"] na classe "
					+ descriptionField.getEntityCache().getEntityClass().getName()
					+ " se o mesmo não é uma chave estrangeira ou se a classe do tipo " + descriptionField.getFieldClass().getName()
					+ " possuí algum campo incorreto.");

		if (dbType.isSizeAllowed() || dbType.isSizeRequired()) {
			if (descriptionColumn.isBoolean()) {
				if ((descriptionColumn.getBooleanType() == BooleanType.INTEGER) || (descriptionColumn.getBooleanType() == BooleanType.STRING)) {
					newColumn.setSize(Math.max(descriptionColumn.getTrueValue().length(), descriptionColumn.getFalseValue().length()));
				}
			} else {
				if (descriptionColumn.getLength() > 0)
					newColumn.setSize(descriptionColumn.getLength());
				if (descriptionColumn.getPrecision() > 0)
					newColumn.setSize(descriptionColumn.getPrecision());
				if (descriptionColumn.getScale() > 0)
					newColumn.setSubSize(descriptionColumn.getScale());
			}
			/*
			 * Se for uma chave estrangeira e não foi definido tamanho, assume tamanho da coluna na tabela de referência
			 */
			if (descriptionColumn.isForeignKey()) {
				if (dbType.isSizeRequired() && (newColumn.getSize() == 0)) {
					DescriptionColumn refColumn = descriptionColumn.getReferencedColumn();
					if (refColumn == null)
						refColumn = descriptionField.getEntityCache().getDescriptionColumnByName(descriptionColumn.getReferencedColumnName());
					newColumn.setSize(refColumn.getLength());
					if (newColumn.getSize() == 0) {
						newColumn.setSize(refColumn.getPrecision());
						newColumn.setSubSize(refColumn.getScale());
					}
				}
			}
			/*
			 * Se mesmo assim não possuí ainda um tamanho definido, assume o valor default do tipo no dialeto do banco
			 * de dados
			 */
			if (dbType.isSizeRequired() && (newColumn.getSize() == 0)) {
				newColumn.setSize(dbType.getDefaultSize());
				if (newColumn.getSubSize() == 0)
					newColumn.setSubSize(dbType.getDefaultSubSize());
			}
		}

		if ((dbType.isAllowsNull()) && (!descriptionColumn.isRequired())) {
			newColumn.setNullable(true);
		} else {
			newColumn.setNullable(false);
		}

		newColumn.setDefaultValue(descriptionColumn.getDefaultValue());
		newColumn.setName(columnName);
		newColumn.setTypeSql(dbType.getName());
		if (session.getDialect().supportsIdentity())
			newColumn.setAutoIncrement(descriptionColumn.isAutoIncrement());
		newColumn.setComment(descriptionField.getComment());
		if (descriptionField.hasGenerator()) {
			newColumn.setSequenceName(descriptionField.getSequenceName());
		}

		newColumn.setPrimaryKey(descriptionColumn.isPrimaryKey());
		newColumn.setForeignKey(descriptionColumn.isForeignKey());

		return newColumn;
	}

	protected Writer getDropSchemaWriter() {
		if (null == dropSchemaWriter) {
			return createSchemaWriter;
		} else {
			return dropSchemaWriter;
		}
	}

	public void closeDDLWriter() throws SchemaGeneratorException {
		closeDDLWriter(createSchemaWriter);
		closeDDLWriter(dropSchemaWriter);
		createSchemaWriter = null;
		dropSchemaWriter = null;
	}

	public void closeDDLWriter(Writer schemaWriter) throws SchemaGeneratorException {
		if (schemaWriter == null) {
			return;
		}
		try {
			schemaWriter.flush();
			schemaWriter.close();
		} catch (java.io.IOException ioException) {
			throw new SchemaGeneratorException(ioException.getMessage());
		}
	}

	protected void finalize() throws Throwable {
		this.closeDDLWriter();
	}

	public void redirectOutputDDLToDatabase() {
		this.createSchemaWriter = null;
		this.dropSchemaWriter = null;
	}

	public void redirectOutputDDLToFile(String fileName) throws SchemaGeneratorException {
		try {
			this.createSchemaWriter = new java.io.FileWriter(fileName);
		} catch (java.io.IOException ioException) {
			throw new SchemaGeneratorException(ioException.getMessage());
		}
	}

	public void redirectOutputCreateDDLToFile(String fileName) throws SchemaGeneratorException {
		try {
			this.createSchemaWriter = new java.io.FileWriter(fileName);
		} catch (java.io.IOException ioException) {
			throw new SchemaGeneratorException(ioException.getMessage());
		}
	}

	public void redirectOutputDropDDLToFile(String fileName) throws SchemaGeneratorException {
		try {
			this.dropSchemaWriter = new java.io.FileWriter(fileName);
		} catch (java.io.IOException ioException) {
			throw new SchemaGeneratorException(ioException.getMessage());
		}
	}

	public void redirectOutputDDLToWriter(Writer schemaWriter) {
		this.createSchemaWriter = schemaWriter;
	}

	/**
	 * Redireciona saída dos comandos de criação DDL
	 * 
	 * @param createWriter
	 */
	public void redirectOutputCreateDDLToWriter(Writer createWriter) {
		this.createSchemaWriter = createWriter;
	}

	/**
	 * Redireciona saída de comandos de remoção DDL
	 * 
	 * @param dropWriter
	 */
	public void redirectOutputDropDDLToWriter(Writer dropWriter) {
		this.dropSchemaWriter = dropWriter;
	}

	/**
	 * Cria um objeto no schema
	 * 
	 * @param objectSchema
	 *            Objeto
	 * @throws Exception
	 */
	public void createObject(ObjectSchema objectSchema) throws Exception {
		if (isWriteToDatabase()) {
			objectSchema.createOnDatabase(session);
			objectSchema.afterCreateObject(session, null);
		} else {
			objectSchema.createObject(session, createSchemaWriter);
			writeEndDelimiter(createSchemaWriter);
			writeLineSeparator(createSchemaWriter);
			objectSchema.afterCreateObject(session, createSchemaWriter);
		}

	}

	/**
	 * Remove um objeto do schema
	 * 
	 * @param objectSchema
	 *            Objeto
	 * @throws Exception
	 */
	public void dropObject(ObjectSchema objectSchema) throws Exception {
		if (isWriteToDatabase()) {
			objectSchema.beforeDropObject(session, null);
			objectSchema.dropFromDatabase(session);
		} else {
			objectSchema.beforeDropObject(session, getDropSchemaWriter());
			objectSchema.dropObject(session, getDropSchemaWriter());
		}
	}

	/**
	 * Cria as constraints únicas da tabela
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @throws Exception
	 */
	public void createUniqueKeyConstraintsFromTable(TableSchema tableSchema) throws Exception {
		if (!session.getDialect().supportsUniqueKeyConstraints())
			return;

		if (session.getDialect().requiresUniqueConstraintCreationOnTableCreate())
			return;

		for (UniqueKeySchema uniqueKey : tableSchema.getUniqueKeys()) {
			createUniqueKeyConstraint(uniqueKey);
		}
	}

	/**
	 * Cria as constraints PrimaryKey da tabela
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @throws Exception
	 */
	public void createPrimaryKeyConstraintFromTable(TableSchema tableSchema) throws Exception {
		if (session.getDialect().supportsPrimaryKeyConstraintOnTableCreate())
			return;

		if (isWriteToDatabase()) {
			tableSchema.getPrimaryKey().createOnDatabase(session);
		} else {
			tableSchema.getPrimaryKey().createObject(session, createSchemaWriter);
			writeEndDelimiter(createSchemaWriter);
		}
	}

	/**
	 * Cria a constraint única
	 * 
	 * @param uniqueKey
	 *            Constraint
	 * @throws Exception
	 */
	protected void createUniqueKeyConstraint(UniqueKeySchema uniqueKey) throws Exception {
		if (!session.getDialect().supportsUniqueKeyConstraints())
			return;

		if (session.getDialect().requiresUniqueConstraintCreationOnTableCreate())
			return;

		if (isWriteToDatabase()) {
			uniqueKey.createOnDatabase(session);
		} else {
			uniqueKey.createObject(session, createSchemaWriter);
			writeEndDelimiter(createSchemaWriter);
		}
	}

	/**
	 * Cria as chaves estrangeiras da tabela
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @throws Exception
	 */
	public void createForeignKeyConstraintsFromTable(TableSchema tableSchema) throws Exception {
		if (!session.getDialect().supportsForeignKeyConstraints())
			return;
		if (!createReferentialIntegrity)
			return;

		if (session.getDialect().requiresForeignKeyConstraintCreationOnTableCreate())
			return;

		for (ForeignKeySchema foreignKey : tableSchema.getForeignKeys()) {
			createForeignKeyConstraint(foreignKey);
		}
	}

	/**
	 * Cria a chave estrangeira
	 * 
	 * @param foreignKey
	 *            Chave estrangeira
	 * @throws Exception
	 * @throws IOException
	 */
	protected void createForeignKeyConstraint(ForeignKeySchema foreignKey) throws Exception, IOException {
		if (!session.getDialect().supportsForeignKeyConstraints())
			return;

		if (session.getDialect().requiresForeignKeyConstraintCreationOnTableCreate())
			return;

		if (!createReferentialIntegrity)
			return;

		if (isWriteToDatabase()) {
			foreignKey.createOnDatabase(session);
		} else {
			foreignKey.createObject(session, createSchemaWriter);
			writeEndDelimiter(createSchemaWriter);
		}
	}

	/**
	 * Adiciona separador no nome do arquivo
	 * 
	 * @param appLocation
	 *            Nome da aplicação
	 * @return Nome com separador
	 */
	protected static String addFileSeperator(String appLocation) {
		int strLength = appLocation.length();
		if (appLocation.substring(strLength - 1, strLength).equals(File.separator)) {
			return appLocation;
		} else {
			return appLocation + File.separator;
		}
	}

	/**
	 * Cria as tabelas do schema
	 * 
	 * @throws Exception
	 */
	public void createTables(boolean writeCommentsHeader) throws Exception {
		buildTablesSchema();

		if (writeCommentsHeader) {
			writeCommentsHeader("CREATE TABLES");
		}
		/*
		 * Cria as sequences
		 */
		createSequences();

		/*
		 * Cria as tabelas
		 */
		boolean writeCommentTable = true;
		for (TableSchema tableSchema : tables) {
			writeCommentTable = writeHeaderCreateTable(writeCommentTable);
			try {
				createObject(tableSchema);
			} catch (Exception ex) {
				if (!isIgnoreDatabaseException()) {
					throw ex;
				}
			}
		}
		if (!writeCommentTable)
			writeLineSeparator(createSchemaWriter);

		/*
		 * Cria as constraints das tabelas
		 */
		createConstraints();

	}

	protected void createConstraints() throws Exception {
		/*
		 * Cria as constraints únicas
		 */
		boolean writeCommentPrimaryKey = true;
		for (TableSchema tableSchema : tables) {
			if (tableSchema.getPrimaryKey() != null) {
				if (!isWriteToDatabase() && writeCommentPrimaryKey) {
					createSchemaWriter.write("/******************************************************************************/\n");
					createSchemaWriter.write("/*                        Primary Key Constraints                             */\n");
					createSchemaWriter.write("/******************************************************************************/\n");
					writeCommentPrimaryKey = false;
				}
				try {
					createPrimaryKeyConstraintFromTable(tableSchema);
				} catch (Exception ex) {
					if (!isIgnoreDatabaseException()) {
						throw ex;
					}
				}
			}
		}
		if (!writeCommentPrimaryKey)
			writeLineSeparator(createSchemaWriter);

		/*
		 * Cria as constraints únicas
		 */
		boolean writeCommentUniqueKey = true;
		for (TableSchema tableSchema : tables) {
			if (tableSchema.getUniqueKeys().size() > 0) {
				if (!isWriteToDatabase() && writeCommentUniqueKey) {
					createSchemaWriter.write("/******************************************************************************/\n");
					createSchemaWriter.write("/*                             Unique Constraints                             */\n");
					createSchemaWriter.write("/******************************************************************************/\n");
					writeCommentUniqueKey = false;
				}
				try {
					createUniqueKeyConstraintsFromTable(tableSchema);
				} catch (Exception ex) {
					if (!isIgnoreDatabaseException()) {
						throw ex;
					}
				}
			}
		}
		if (!writeCommentUniqueKey)
			writeLineSeparator(createSchemaWriter);

		/*
		 * Cria as chaves estrangeiras
		 */
		boolean writeCommentForeignKey = true;
		for (TableSchema tableSchema : tables) {
			if (tableSchema.getForeignKeys().size() > 0) {
				if (!isWriteToDatabase() && writeCommentForeignKey) {
					createSchemaWriter.write("/******************************************************************************/\n");
					createSchemaWriter.write("/*                                Foreign Keys                                */\n");
					createSchemaWriter.write("/******************************************************************************/\n");
					writeCommentForeignKey = false;
				}

				try {
					createForeignKeyConstraintsFromTable(tableSchema);

				} catch (Exception ex) {
					if (!isIgnoreDatabaseException()) {
						throw ex;
					}
				}
			}
		}
		if (!writeCommentForeignKey)
			writeLineSeparator(createSchemaWriter);
	}

	/**
	 * Gera o delimitador de final de linha
	 * 
	 * @param schemaWriter
	 * @throws IOException
	 */
	public void writeEndDelimiter(Writer schemaWriter) throws IOException {
		schemaWriter.write(session.getDialect().getBatchDelimiterString() + "\n");
		schemaWriter.flush();
	}

	/**
	 * Remove e cria novamente as tabelas do schema
	 * 
	 * @param dontReplaceSequenceTable
	 * @throws Exception
	 */
	public void replaceTables(boolean dontReplaceSequenceTable) throws Exception {
		buildTablesSchema();
		replaceTables(dontReplaceSequenceTable, false);
	}

	/**
	 * Remove e cria novamente as tabelas do schema
	 * 
	 * @param replaceSequenceTable
	 *            Indica se é para recriar sequências em forma de tabela
	 * @param replaceSequences
	 *            Indica se é para recriar as sequências
	 * @throws Exception
	 */
	public void replaceTables(boolean replaceSequenceTable, boolean replaceSequences) throws Exception {
		buildTablesSchema();
		writeCommentsHeader("DROP AND CREATE TABLES");

		/*
		 * Remove as tabelas
		 */
		dropTables();
		/*
		 * Cria novamente as tabelas
		 */
		createTables(false);
	}

	protected void writeCommentsHeader(String mode) throws IOException {
		if (!isWriteToDatabase()) {
			String dialect = session.getDialect().name() + " ";
			getDropSchemaWriter().write("/******************************************************************************/\n");
			getDropSchemaWriter().write("/*                                                                            */\n");
			getDropSchemaWriter().write(
					"/*       Generated by Anteros Java Persistence " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
							+ "            */\n");
			getDropSchemaWriter().write("/*       Dialect  : " + dialect + StringUtils.repeat(" ", 80 - 20 - dialect.length() - 2) + "*/\n");
			getDropSchemaWriter().write("/*       Mode     : " + mode + StringUtils.repeat(" ", 80 - 20 - mode.length() - 2) + "*/\n");
			getDropSchemaWriter().write("/*                                                                            */\n");
			getDropSchemaWriter().write("/******************************************************************************/\n");
			getDropSchemaWriter().write("\n");
		}
	}

	/**
	 * Remove as tabelas do schema
	 * 
	 * @throws Exception
	 */
	public void dropTables() throws Exception {
		buildTablesSchema();
		/*
		 * Remove as constraints
		 */
		dropConstraints();
		/*
		 * Remove as tabelas
		 */
		boolean writeCommentTable = true;

		TableSchema[] tablesAsArray = tables.toArray(new TableSchema[] {});

		for (int i = tablesAsArray.length - 1; i >= 0; i--) {
			if (!isWriteToDatabase() && writeCommentTable) {
				getDropSchemaWriter().write("/******************************************************************************/\n");
				getDropSchemaWriter().write("/*                           Drop Tables and indexes                          */\n");
				getDropSchemaWriter().write("/******************************************************************************/\n");
				writeCommentTable = false;
			}
			try {
				dropObject(tablesAsArray[i]);
				if (!isWriteToDatabase()) {
					writeEndDelimiter(getDropSchemaWriter());
				}
			} catch (Exception ex) {
				if (!isIgnoreDatabaseException()) {
					throw ex;
				}
			}
		}

		if (!writeCommentTable)
			writeLineSeparator(getDropSchemaWriter());

		dropSequences();
	}

	/**
	 * Remove as constraints das tabelas do schema
	 * 
	 * @throws Exception
	 */
	protected void dropConstraints() throws Exception {

		/*
		 * Remove as chaves estrangeiras
		 */
		boolean writeCommentForeignKey = true;
		for (TableSchema tableSchema : tables) {
			if (tableSchema.getForeignKeys().size() > 0) {
				if (!isWriteToDatabase() && writeCommentForeignKey) {
					getDropSchemaWriter().write("/******************************************************************************/\n");
					getDropSchemaWriter().write("/*                           Drop Foreign Keys                                */\n");
					getDropSchemaWriter().write("/******************************************************************************/\n");
					writeCommentForeignKey = false;
				}

				try {
					dropForeignKeyConstraints(tableSchema);
				} catch (Exception ex) {
					if (!isIgnoreDatabaseException()) {
						throw ex;
					}
				}
			}
		}
		if (!writeCommentForeignKey)
			writeLineSeparator(getDropSchemaWriter());

		/*
		 * Remove as constraints únicas
		 */
		boolean writeCommentUniqueKey = true;
		for (TableSchema tableSchema : tables) {
			if (tableSchema.getUniqueKeys().size() > 0) {
				if (!isWriteToDatabase() && writeCommentUniqueKey) {
					getDropSchemaWriter().write("/******************************************************************************/\n");
					getDropSchemaWriter().write("/*                           Drop Unique Constraints                          */\n");
					getDropSchemaWriter().write("/******************************************************************************/\n");
					writeCommentUniqueKey = false;
				}
				try {
					dropUniqueKeyConstraints(tableSchema);
				} catch (Exception ex) {
					if (!isIgnoreDatabaseException()) {
						throw ex;
					}
				}
			}
		}
		if (!writeCommentUniqueKey)
			writeLineSeparator(getDropSchemaWriter());

	}

	/**
	 * Remove as constraints únicas de uma tabela no schema
	 * 
	 * @param tableSchema
	 * @throws Exception
	 */
	public void dropUniqueKeyConstraints(TableSchema tableSchema) throws Exception {
		if (!session.getDialect().supportsUniqueKeyConstraints())
			return;

		for (UniqueKeySchema uniqueKey : tableSchema.getUniqueKeys()) {
			if (isWriteToDatabase()) {
				uniqueKey.dropFromDatabase(session);
			} else {
				uniqueKey.dropObject(session, getDropSchemaWriter());
				if (!isWriteToDatabase()) {
					writeEndDelimiter(getDropSchemaWriter());
				}
			}
		}
	}

	/**
	 * Remove as chaves estrangeiras de uma tabela no schema
	 * 
	 * @param tableSchema
	 *            Tabela
	 * @throws Exception
	 */
	public void dropForeignKeyConstraints(TableSchema tableSchema) throws Exception {
		if (!session.getDialect().supportsForeignKeyConstraints())
			return;
		if (!session.getDialect().supportsDropForeignKeyConstraints())
			return;

		for (ForeignKeySchema foreignKey : tableSchema.getForeignKeys()) {
			if (isWriteToDatabase()) {
				foreignKey.dropFromDatabase(session);
			} else {
				foreignKey.dropObject(session, getDropSchemaWriter());
				if (!isWriteToDatabase())
					writeEndDelimiter(getDropSchemaWriter());
			}
		}
	}

	/**
	 * Extende as tabelas do schema
	 */
	public void extendTables(boolean generateFKConstraints) throws Exception {
		buildTablesSchema();

		writeCommentsHeader("EXTEND TABLES");

		/*
		 * Cria as sequências
		 */
		createSequences();

		/*
		 * Verifica se tabelas, indices e colunas existem
		 */
		boolean writeCommentCreateTable = true;
		for (TableSchema tableSchema : tables) {
			if (!session.getDialect().checkTableExists(session.getConnection(), tableSchema.getName())) {
				try {
					/*
					 * Cria a tabela
					 */
					writeCommentCreateTable = writeHeaderCreateTable(writeCommentCreateTable);
					createObject(tableSchema);
				} catch (Exception ex) {
					if (!isIgnoreDatabaseException()) {
						throw ex;
					}
				}
			} else {
				Map<String, IndexMetadata> allIndexes = session.getDialect().getAllIndexesByTable(session.getConnection(), tableSchema.getName());

				/*
				 * Verifica se a coluna existe na tabela. Se não existir adiciona.
				 */
				String[] columnNames = session.getDialect().getColumnNamesFromTable(session.getConnection(), tableSchema.getName());
				for (ColumnSchema columnSchema : tableSchema.getColumns()) {
					boolean found = false;
					for (String colName : columnNames) {
						if (colName.equalsIgnoreCase(columnSchema.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						try {
							/*
							 * Adiciona a coluna na tabela
							 */
							writeCommentCreateTable = writeHeaderCreateTable(writeCommentCreateTable);
							if (isWriteToDatabase())
								tableSchema.addColumnOnDatabase(session, columnSchema);
							else {
								tableSchema.addColumn(session, columnSchema, createSchemaWriter);
								writeEndDelimiter(createSchemaWriter);
							}
						} catch (Exception ex) {
							if (!isIgnoreDatabaseException()) {
								throw ex;
							}
						}
					}
				}

				/*
				 * Verifica se existe o índice. Se não existir cria.
				 */
				for (IndexSchema indexSchema : tableSchema.getIndexes()) {
					if (!checkIndexExists(indexSchema.getName(), indexSchema.getColumnNames().toArray(new String[] {}), allIndexes)) {
						try {
							if (isWriteToDatabase()) {
								indexSchema.createOnDatabase(session);
							} else {
								indexSchema.createObject(session, createSchemaWriter);
								createSchemaWriter.write(session.getDialect().getBatchDelimiterString() + "\n");
							}
						} catch (Exception ex) {
							if (!isIgnoreDatabaseException()) {
								throw ex;
							}
						}
					}
				}
			}
		}

		/*
		 * Verifica as constraints
		 */
		boolean writeCommentUniqueConstraint = true;
		for (TableSchema tableSchema : tables) {
			Map<String, IndexMetadata> allIndexes = session.getDialect().getAllIndexesByTable(session.getConnection(), tableSchema.getName());
			/*
			 * Verifica se existe a chave única. Se não existir cria.
			 */
			for (UniqueKeySchema uniqueKeySchema : tableSchema.getUniqueKeys()) {
				if (!checkUniqueKeyExists(uniqueKeySchema.getName(), uniqueKeySchema.getColumnNames().toArray(new String[] {}), allIndexes)) {
					if (!isWriteToDatabase() && writeCommentUniqueConstraint) {
						createSchemaWriter.write("/******************************************************************************/\n");
						createSchemaWriter.write("/*                             Unique Constraints                             */\n");
						createSchemaWriter.write("/******************************************************************************/\n");
						writeCommentUniqueConstraint = false;
					}
					try {
						createUniqueKeyConstraint(uniqueKeySchema);
					} catch (Exception ex) {
						if (!isIgnoreDatabaseException()) {
							throw ex;
						}
					}
				}
				if (!isWriteToDatabase())
					createSchemaWriter.flush();
			}
		}
		if (!writeCommentUniqueConstraint)
			writeLineSeparator(createSchemaWriter);

		boolean writeCommentForeignKey = true;
		for (TableSchema tableSchema : tables) {
			Map<String, ForeignKeyMetadata> allFks = session.getDialect().getAllForeignKeysByTable(session.getConnection(), tableSchema.getName());

			/*
			 * Verifica se existe a chave estrangeira. Se não existir cria.
			 */
			for (ForeignKeySchema foreignKeySchema : tableSchema.getForeignKeys()) {
				if (!isWriteToDatabase() && writeCommentForeignKey) {
					createSchemaWriter.write("/******************************************************************************/\n");
					createSchemaWriter.write("/*                                Foreign Keys                                */\n");
					createSchemaWriter.write("/******************************************************************************/\n");
					writeCommentForeignKey = false;
				}
				if (!checkForeignKeyExists(foreignKeySchema.getName(), foreignKeySchema.getColumnNames(), allFks)) {
					try {
						createForeignKeyConstraint(foreignKeySchema);
					} catch (Exception ex) {
						if (!isIgnoreDatabaseException()) {
							throw ex;
						}
					}
				}
				if (!isWriteToDatabase())
					createSchemaWriter.flush();
			}
		}

		if (!writeCommentForeignKey)
			writeLineSeparator(createSchemaWriter);

	}

	protected boolean checkIndexExists(String indexName, String[] columns, Map<String, IndexMetadata> indexes) {
		if (indexes.containsKey(indexName.toLowerCase()))
			return true;
		if (indexes.containsKey(indexName.toUpperCase()))
			return true;

		IndexMetadata index = null;
		for (String k : indexes.keySet()) {
			index = indexes.get(k);
			if (index.containsAllColumns(columns))
				return true;
		}
		return false;
	}

	protected boolean checkUniqueKeyExists(String indexName, String[] columns, Map<String, IndexMetadata> indexes) {
		if (indexes.containsKey(indexName.toLowerCase()))
			return true;
		if (indexes.containsKey(indexName.toUpperCase()))
			return true;

		IndexMetadata index = null;
		for (String k : indexes.keySet()) {
			index = indexes.get(k);
			if (index.containsAllColumns(columns))
				return true;
		}
		return false;
	}

	protected boolean checkForeignKeyExists(String indexName, String[] columns, Map<String, ForeignKeyMetadata> fks) {
		if (fks.containsKey(indexName.toLowerCase()))
			return true;
		if (fks.containsKey(indexName.toUpperCase()))
			return true;

		ForeignKeyMetadata fk = null;
		for (String k : fks.keySet()) {
			fk = fks.get(k);
			if (fk.containsAllColumns(columns))
				return true;
		}
		return false;
	}

	protected boolean writeHeaderCreateTable(boolean writeCommentCreateTable) throws IOException {
		if (!isWriteToDatabase() && writeCommentCreateTable) {
			createSchemaWriter.write("/******************************************************************************/\n");
			createSchemaWriter.write("/*                                   Tables                                   */\n");
			createSchemaWriter.write("/******************************************************************************/\n");
			writeCommentCreateTable = false;
		}
		return writeCommentCreateTable;
	}

	/**
	 * Cria as sequências
	 * 
	 * @throws Exception
	 */
	public void createSequences() throws Exception {
		buildTablesSchema();
		boolean writeCommentSequence = true;
		for (ObjectSchema sequenceSchema : sequences) {
			if (sequenceSchema instanceof SequenceGeneratorSchema) {
				if (session.getDialect().checkSequenceExists(session.getConnection(), sequenceSchema.getName()))
					continue;
			} else if (sequenceSchema instanceof TableGeneratorSchema) {
				if (session.getDialect().checkTableExists(session.getConnection(), sequenceSchema.getName()))
					continue;
			}
			if (!isWriteToDatabase() && writeCommentSequence) {
				createSchemaWriter.write("/******************************************************************************/\n");
				createSchemaWriter.write("/*                 Create Sequences or Table Sequences                        */\n");
				createSchemaWriter.write("/******************************************************************************/\n");
				writeCommentSequence = false;
			}
			try {
				createObject(sequenceSchema);
			} catch (Exception ex) {
				if (!isIgnoreDatabaseException()) {
					throw ex;
				}
			}
		}
		if (!writeCommentSequence)
			writeLineSeparator(createSchemaWriter);
	}

	/**
	 * Remove a sequências
	 * 
	 * @throws Exception
	 */
	public void dropSequences() throws Exception {
		buildTablesSchema();
		boolean writeCommentSequence = true;
		for (ObjectSchema sequenceSchema : sequences) {
			if (!isWriteToDatabase() && writeCommentSequence) {
				getDropSchemaWriter().write("/******************************************************************************/\n");
				getDropSchemaWriter().write("/*                 Drop Sequences or Table Sequences                          */\n");
				getDropSchemaWriter().write("/******************************************************************************/\n");
				writeCommentSequence = false;
			}
			try {
				dropObject(sequenceSchema);
				if (!isWriteToDatabase())
					writeEndDelimiter(getDropSchemaWriter());
			} catch (Exception ex) {
				if (!isIgnoreDatabaseException()) {
					throw ex;
				}
			}
		}
		if (!writeCommentSequence)
			writeLineSeparator(getDropSchemaWriter());
	}

	/**
	 * Gerar e executa os comandos DDL no banco de dados conforme configurada a estratégia de geração.
	 * 
	 * @param ddlType
	 * @throws Exception
	 */
	public void writeDDLToDatabase(TableCreationType ddlType) throws Exception {
		if (ddlType == null || ddlType == TableCreationType.CREATE) {
			createTables(true);
		} else if (ddlType == TableCreationType.DROP) {
			replaceTables(true, true);
		} else if (ddlType == TableCreationType.EXTEND) {
			extendTables(true);
		}
	}

	/**
	 * Gerar os comandos DDL em um script sql conforme configurada a estratégia deodo de geração.
	 * 
	 * @param ddlType
	 *            Modo de geração: CRIAR, REMOVER E CRIAR, CRIAR OU EXTENDER
	 * @param appLocation
	 *            Localizaçãoi da aplicação
	 * @param createDDLJdbc
	 *            Nome para o script de criação DDL
	 * @param dropDDLJdbc
	 *            Nome para o script de remoção DDL
	 * @throws Exception
	 *             Exceção
	 */
	public void writeDDLsToFiles(TableCreationType ddlType, String appLocation, String createDDLJdbc, String dropDDLJdbc) throws Exception {
		appLocation = addFileSeperator(appLocation);
		if (null != createDDLJdbc) {
			String createJdbcFileName = appLocation + createDDLJdbc;
			this.createSchemaWriter = new java.io.FileWriter(createJdbcFileName);
		}

		if ((null != dropDDLJdbc) && (!dropDDLJdbc.equals(createDDLJdbc)) && (!"".equals(dropDDLJdbc))) {
			String dropJdbcFileName = appLocation + dropDDLJdbc;
			this.dropSchemaWriter = new java.io.FileWriter(dropJdbcFileName);
		}

		if (ddlType == null || ddlType == TableCreationType.CREATE) {
			createTables(true);
		} else if (ddlType == TableCreationType.DROP) {
			replaceTables(true, true);
		} else if (ddlType == TableCreationType.EXTEND) {
			extendTables(true);
		}
		closeDDLWriter();
	}

	/**
	 * Retorna se está gerando no banco de dados
	 * 
	 * @return
	 */
	public boolean isWriteToDatabase() {
		return ((this.createSchemaWriter == null) && (this.dropSchemaWriter == null));
	}

	/**
	 * Gera um separador de linha no script sql
	 * 
	 * @param schemaWriter
	 *            Saída
	 * @throws IOException
	 */
	public void writeLineSeparator(Writer schemaWriter) throws IOException {
		schemaWriter.write("\n");
	}

	/**
	 * Informa se é para ignorar exceções na geração do schema no banco de dados
	 * 
	 * @return
	 */
	public boolean isIgnoreDatabaseException() {
		return ignoreDatabaseException;
	}

	public void setIgnoreDatabaseException(boolean ignoreDatabaseException) {
		this.ignoreDatabaseException = ignoreDatabaseException;
	}

	/**
	 * Gera um nome para a chave estrangeira
	 * 
	 * @param tableName
	 *            Nome da tabela
	 * @param columnName
	 *            Nome da coluna
	 * @return Nome gerado
	 */
	public String generateForeignKeyConstraintName(TableSchema table, String tableName, String columnName) {
		int maximumNameLength = session.getDialect().getMaxForeignKeyNameSize();
		String startDelimiter = "";
		String endDelimiter = "";
		boolean useDelimiters = !session.getDialect().getStartDelimiter().equals("")
				&& (tableName.startsWith(session.getDialect().getStartDelimiter()) || columnName.startsWith(session.getDialect().getStartDelimiter()));
		if (useDelimiters) {
			startDelimiter = session.getDialect().getStartDelimiter();
			endDelimiter = session.getDialect().getEndDelimiter();
		}
		String adjustedTableName = adjustTableName(tableName);
		String adjustedColumnName = adjustColumnName(columnName);
		String foreignKeyName = startDelimiter + "FK_" + adjustedTableName + "_" + adjustedColumnName + endDelimiter;
		if (foreignKeyName.length() > maximumNameLength) {
			foreignKeyName = startDelimiter + adjustedTableName + "_" + adjustedColumnName + endDelimiter;
			if (foreignKeyName.length() > maximumNameLength) {
				foreignKeyName = startDelimiter
						+ StringUtils.removeAllButAlphaNumericToFit(adjustedTableName + adjustedColumnName, maximumNameLength) + endDelimiter;
				if (foreignKeyName.length() > maximumNameLength) {
					String onlyAlphaNumericTableName = StringUtils.removeAllButAlphaNumericToFit(adjustedTableName, 0);
					String onlyAlphaNumericColumnName = StringUtils.removeAllButAlphaNumericToFit(adjustedColumnName, 0);
					foreignKeyName = startDelimiter
							+ StringUtils.shortenStringsByRemovingVowelsToFit(onlyAlphaNumericTableName, onlyAlphaNumericColumnName,
									maximumNameLength) + endDelimiter;
					if (foreignKeyName.length() > maximumNameLength) {
						String shortenedColumnName = StringUtils.removeVowels(onlyAlphaNumericColumnName);
						String shortenedTableName = StringUtils.removeVowels(onlyAlphaNumericTableName);
						int delimiterLength = startDelimiter.length() + endDelimiter.length();
						if (shortenedColumnName.length() + delimiterLength >= maximumNameLength) {
							foreignKeyName = startDelimiter + StringUtils.truncate(shortenedColumnName, maximumNameLength - delimiterLength)
									+ endDelimiter;
						} else {
							foreignKeyName = startDelimiter
									+ StringUtils.truncate(shortenedTableName, maximumNameLength - shortenedColumnName.length() - delimiterLength)
									+ shortenedColumnName + endDelimiter;
						}
					}
				}
			}
		}
		return foreignKeyName;
	}

	/**
	 * Gera um nome para a chave única
	 * 
	 * @param tableName
	 *            Nome da tabela
	 * @param serialNumber
	 *            Número de sequência
	 * @param columnName
	 *            Nome da coluna
	 * @return Nome gerado
	 */
	public String generateUniqueKeyConstraintName(String tableName, int serialNumber, String columnName) {
		int maximumNameLength = session.getDialect().getMaxUniqueKeyNameSize();
		String uniqueKeyName = "UNQ_" + tableName + "_" + serialNumber + "_" + columnName;
		if (uniqueKeyName.length() > maximumNameLength) {
			uniqueKeyName = tableName + serialNumber;
			if (uniqueKeyName.length() > maximumNameLength) {
				uniqueKeyName = StringUtils.removeAllButAlphaNumericToFit(tableName + serialNumber, maximumNameLength);
				if (uniqueKeyName.length() > maximumNameLength) {
					String onlyAlphaNumericTableName = StringUtils.removeAllButAlphaNumericToFit(tableName, 0);
					String serialName = String.valueOf(serialNumber);
					uniqueKeyName = StringUtils.shortenStringsByRemovingVowelsToFit(onlyAlphaNumericTableName, serialName, maximumNameLength);
					if (uniqueKeyName.length() > maximumNameLength) {
						String shortenedTableName = StringUtils.removeVowels(onlyAlphaNumericTableName);
						uniqueKeyName = StringUtils.truncate(shortenedTableName, maximumNameLength - serialName.length()) + serialName;
					}
				}
			}
		}
		return uniqueKeyName;
	}

	/**
	 * Gera um nome para a chave primária
	 * 
	 * @param tableName
	 *            Nome da tabela
	 * @param serialNumber
	 *            Número de sequência
	 * @param columnName
	 *            Nome da coluna
	 * @return Nome gerado
	 */
	public String generatePrimaryKeyConstraintName(String tableName) {
		int maximumNameLength = session.getDialect().getMaxPrimaryKeyNameSize();
		String primaryKeyName = "PK_" + tableName;
		if (primaryKeyName.length() > maximumNameLength) {
			primaryKeyName = tableName;
			if (primaryKeyName.length() > maximumNameLength) {
				primaryKeyName = StringUtils.removeAllButAlphaNumericToFit(tableName, maximumNameLength);
			}
		}
		return primaryKeyName;
	}

	/**
	 * Gera um nome para o índice da tabela
	 * 
	 * @param tableName
	 *            Nome da tabela
	 * @param columName
	 *            Nome da coluna
	 * @param indexPrefix
	 *            Prefixo para o nome do indices
	 * @return Nome gerado
	 */
	public String generationIndexName(String tableName, String columName, String indexPrefix) {
		int maximumNameLength = session.getDialect().getMaxIndexKeyNameSize();
		String startDelimiter = "";
		String endDelimiter = "";
		boolean useDelimiters = !session.getDialect().getStartDelimiter().equals("")
				&& (tableName.startsWith(session.getDialect().getStartDelimiter()) || columName.startsWith(session.getDialect().getStartDelimiter()));
		if (useDelimiters) {
			startDelimiter = session.getDialect().getStartDelimiter();
			endDelimiter = session.getDialect().getEndDelimiter();
		}
		String adjustedTableName = adjustTableName(tableName);
		String adjustedColumnName = adjustColumnName(columName);
		if (indexPrefix == null) {
			indexPrefix = "IX_";
		}
		String indexName = startDelimiter + indexPrefix + adjustedTableName + "_" + adjustedColumnName + endDelimiter;
		if (indexName.length() > maximumNameLength) {
			indexName = startDelimiter + adjustedTableName + "_" + adjustedColumnName + endDelimiter;
			if (indexName.length() > maximumNameLength) {
				indexName = startDelimiter + StringUtils.removeAllButAlphaNumericToFit(adjustedTableName + adjustedColumnName, maximumNameLength)
						+ endDelimiter;
				if (indexName.length() > maximumNameLength) {
					String onlyAlphaNumericTableName = StringUtils.removeAllButAlphaNumericToFit(adjustedTableName, 0);
					String onlyAlphaNumericColumnName = StringUtils.removeAllButAlphaNumericToFit(adjustedColumnName, 0);
					indexName = startDelimiter
							+ StringUtils.shortenStringsByRemovingVowelsToFit(onlyAlphaNumericTableName, onlyAlphaNumericColumnName,
									maximumNameLength) + endDelimiter;
					if (indexName.length() > maximumNameLength) {
						String shortenedColumnName = StringUtils.removeVowels(onlyAlphaNumericColumnName);
						String shortenedTableName = StringUtils.removeVowels(onlyAlphaNumericTableName);
						int delimiterLength = startDelimiter.length() + endDelimiter.length();
						if (shortenedColumnName.length() + delimiterLength >= maximumNameLength) {
							indexName = startDelimiter + StringUtils.truncate(shortenedColumnName, maximumNameLength - delimiterLength)
									+ endDelimiter;
						} else {
							indexName = startDelimiter
									+ StringUtils.truncate(shortenedTableName, maximumNameLength - shortenedColumnName.length() - delimiterLength)
									+ shortenedColumnName + endDelimiter;
						}
					}
				}
			}
		}
		return indexName;
	}

	/**
	 * Ajusta nome da coluna
	 * 
	 * @param columnName
	 *            Nome da coluna
	 * @return Nome ajustado
	 */
	protected String adjustColumnName(String columnName) {
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < columnName.length(); i++) {
			char c = columnName.charAt(i);
			if (c != ' ' && c != '\"' && c != '`') {
				buff.append(c);
			}
		}
		String adjustedFieldName = buff.toString();
		return adjustedFieldName;
	}

	/**
	 * Ajusta nome da tabela
	 * 
	 * @param tableName
	 *            Nome da tabela
	 * @return Nome ajustado
	 */
	protected String adjustTableName(String tableName) {
		String adjustedTableName = tableName;
		if (adjustedTableName.indexOf(' ') != -1 || adjustedTableName.indexOf('\"') != -1 || adjustedTableName.indexOf('`') != -1) {
			StringBuilder buff = new StringBuilder();
			for (int i = 0; i < tableName.length(); i++) {
				char c = tableName.charAt(i);
				if (c != ' ' && c != '\"' && c != '`') {
					buff.append(c);
				}
			}
			adjustedTableName = buff.toString();
		}
		return adjustedTableName;
	}

	public int compare(TableSchema o1, TableSchema o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				// Sort nullos primeiro
				return 1;
			}
		} else if (o2 == null) {
			// Sort nulos primeiro
			return -1;
		}

		// Neste ponto, sabemos que o1 e o2 não são nulos
		if (o1.equals(o2)) {
			return 0;
		}

		// Neste ponto, o1 e o2 não são nulos e não iguais, vamos
		// compará-los para ver qual é "superior" na hierarquia de tabelas
		boolean o2Lower = o2.depends(o1);
		boolean o1Lower = o1.depends(o2);

		if (o1Lower && !o2Lower) {
			return 1;
		} else if (o2Lower && !o1Lower) {
			return -1;
		}
		return 0;

	}

	public Set<String> getStoredProcedureNames() throws Exception {
		return session.getDialect().getStoredProcedureNames(session.getConnection());
	}

	public Set<String> getFunctionNames() throws Exception {
		return session.getDialect().getStoredFunctionNames(session.getConnection());
	}

	public Set<String> getStoredProcedureNames(String procedureNamePattern) throws Exception {
		return session.getDialect().getStoredProcedureNames(session.getConnection(), procedureNamePattern);
	}

	public Set<String> getFunctionNames(String functionNamePattern) throws Exception {
		return session.getDialect().getStoredFunctionNames(session.getConnection(), functionNamePattern);
	}

	public Set<StoredProcedureSchema> getStoredProcedures() throws Exception {
		return session.getDialect().getStoredProcedures(session.getConnection());
	}

	public Set<StoredProcedureSchema> getStoredProcedures(boolean dontGetParameters) throws Exception {
		return session.getDialect().getStoredProcedures(session.getConnection(), dontGetParameters);
	}

	public Set<StoredProcedureSchema> getStoredProcedures(String procedureNamePattern) throws Exception {
		return session.getDialect().getStoredProcedures(session.getConnection());
	}

	public Set<StoredProcedureSchema> getStoredProcedures(String procedureNamePattern, boolean dontGetParameters) throws Exception {
		return session.getDialect().getStoredProcedures(session.getConnection(), procedureNamePattern, dontGetParameters);
	}

	public Set<StoredFunctionSchema> getStoredFunctions() throws Exception {
		return session.getDialect().getStoredFunctions(session.getConnection());
	}

	public Set<StoredFunctionSchema> getStoredFunctions(boolean dontGetParameters) throws Exception {
		return session.getDialect().getStoredFunctions(session.getConnection(), dontGetParameters);
	}

	public Set<StoredFunctionSchema> getStoredFunctions(String functionNamePattern) throws Exception {
		return session.getDialect().getStoredFunctions(session.getConnection(), functionNamePattern, false);
	}

	public Set<StoredFunctionSchema> getStoredFunctions(String functionNamePattern, boolean dontGetParameters) throws Exception {
		return session.getDialect().getStoredFunctions(session.getConnection(), functionNamePattern, dontGetParameters);
	}

	public StoredProcedureSchema getStoredProcedureByName(String procedureName) throws Exception {
		Set<StoredProcedureSchema> result = getStoredProcedures(procedureName);
		if (result.size() > 0)
			return result.iterator().next();
		return null;
	}

	public StoredFunctionSchema getStoredFunctionByName(String functionName) throws Exception {
		Set<StoredFunctionSchema> result = getStoredFunctions(functionName);
		if (result.size() > 0)
			return result.iterator().next();
		return null;
	}

	public Set<TableSchema> getTables() {
		return tables;
	}

	public Set<ViewSchema> getViews() {
		return views;
	}

	public Set<GeneratorSchema> getSequences() {
		return sequences;
	}

	public Set<StoredProcedureSchema> getProcedures() {
		return procedures;
	}

	public Set<StoredFunctionSchema> getFunctions() {
		return functions;
	}

	public StoredProcedureSchema getProcedure(String procedureName) {
		if (procedures != null) {
			for (StoredProcedureSchema procedure : procedures) {
				if (procedure.getName().equalsIgnoreCase(procedureName))
					return procedure;
			}
		}
		return null;
	}

	public StoredFunctionSchema getFunction(String functionName) {
		if (procedures != null) {
			for (StoredFunctionSchema function : functions) {
				if (function.getName().equalsIgnoreCase(functionName))
					return function;
			}
		}
		return null;
	}

}
