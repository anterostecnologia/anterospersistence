/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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

/**
 * Classe responsável por criar uma coleção de objetos e atribuir ao objeto alvo.
 * 
 * @author edson
 *
 */
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

		/*
		 * Armazena a classe que deverá ser instanciada para representar a coleção de objetos de acordo com o tipo do campo 
		 * na entidade.
		 */
		if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), Set.class)) {
			defaultClass = DefaultSQLSet.class;
		} else if (ReflectionUtils.isImplementsInterface(descriptionField.getField().getType(), List.class)) {
			defaultClass = DefaultSQLList.class;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute(SQLSession session, ResultSet resultSet, EntityManaged entityManaged, Object targetObject, Cache transactionCache)
			throws Exception {
		
		if (!session.getEntityCacheManager().getEntityCache(targetObject.getClass()).containsDescriptionField(descriptionField))
			return;

		Object newObject = null;
		/*
		 * Guarda o objeto alvo
		 */
		Object oldTargetObject = targetObject;
		/*
		 * Pega o valor do campo no objeto alvo
		 */
		targetObject = descriptionField.getObjectValue(targetObject);
		/*
		 * Se for nulo instancia de acordo com a classe default obtida do campo na entidade.
		 */
		if (targetObject == null) {
			targetObject = defaultClass.newInstance();
			/*
			 * Atribui a coleção ao campo no objeto alvo
			 */
			descriptionField.setObjectValue(oldTargetObject, targetObject);
		}

		/*
		 * Se o classe do objeto que será armazenado na lista for abstrata
		 */
		if (isAbstract) {
			try {
				/*
				 * Pega o valor da coluna no resultSet relativo ao discriminatorColumn o que é necessário para
				 * determinar qual classe concreta será instanciada
				 */
				String discriminator = resultSet.getString(aliasDiscriminatorColumnName);
				/*
				 * Se não encontrou o valor não cria o objeto e retorna
				 */
				if (discriminator==null)
					return;
				
				/*
				 * Se encontrou o valor do discriminator busca a classe concreta referente ao valor.
				 */
				EntityCache concreteEntityCache = session.getEntityCacheManager().getEntityCache(descriptionField.getTargetClass(), discriminator);
				/*
				 * Busca os valores da chave do objeto no resultSet.
				 */
				String uniqueId = getUniqueId(resultSet);
				/*
				 * Caso não encontre não cria o objeto e retorna
				 */
				if (uniqueId == null)
					return;
				/*
				 * Busca o objeto no cache da transação SQL.
				 */
				newObject = getObjectFromCache(session, concreteEntityCache, uniqueId, transactionCache);
				if (newObject == null) {
					/*
					 * Se não encontrou instancia um novo objeto
					 */
					newObject = concreteEntityCache.getEntityClass().newInstance();
					/*
					 * Adiciona o objeto instanciado no cache com sua chave única para ser usado quando houver
					 * necessidade em outro ponto da árvore do objeto principal evitando assim criar objetos
					 * repetidos para a mesma chave.
					 */
					addObjectToCache(session, concreteEntityCache, newObject, uniqueId, transactionCache);		
					/*
					 * Adiciona o objeto instanciado na coleção
					 */
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
				/*
				 * Busca os valores da chave do objeto no resultSet.
				 */
				String uniqueId = getUniqueId(resultSet);
				/*
				 * Caso não encontre não cria o objeto e retorna
				 */
				if (uniqueId == null)
					return;
				/*
				 * Busca o objeto no cache da transação SQL.
				 */
				newObject = getObjectFromCache(session, targetEntityCache, uniqueId, transactionCache);
				if (newObject == null) {
					/*
					 * Se não encontrou instancia um novo objeto
					 */
					newObject = targetEntityCache.getEntityClass().newInstance();
					/*
					 * Adiciona o objeto instanciado no cache com sua chave única para ser usado quando houver
					 * necessidade em outro ponto da árvore do objeto principal evitando assim criar objetos
					 * repetidos para a mesma chave.
					 */
					addObjectToCache(session, targetEntityCache, newObject, uniqueId, transactionCache);
					/*
					 * Adiciona o objeto instanciado na coleção
					 */
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
		/*
		 * Executa lista de expressões filhas para atribuir os valores ao novo objeto e assim sucessivamente até
		 * terminar a árvore de expressões.
		 */
		for (ExpressionFieldMapper expField : children) {
			expField.execute(session, resultSet, entityManaged, newObject, transactionCache);
		}
	}

	/**
	 * Retorna a chave única do objeto buscando os valores no resultSet.
	 * @param resultSet Resultado do SQL
	 * @return Chave única
	 * @throws SQLException
	 */
	protected String getUniqueId(ResultSet resultSet) throws SQLException {
		int index;
		StringBuilder uniqueIdTemp = new StringBuilder("");
		boolean appendSeparator = false;
		for (String aliasColumnName : aliasPrimaryKeyColumns) {
			/*
			 * Busca índice da coluna dentro do resultSet
			 */
			index = resultSet.findColumn(aliasColumnName);
			if (index < 0) {
				/*
				 * Esta exception não deverá ocorrer nunca pois as colunas estão sendo parseadas pela análise do SQL.
				 * Se isto ocorrer pode ser um erro na análise.
				 */
				throw new SQLException("NÃO ACHOU COLUNA " + aliasColumnName);
			}
			/*
			 * Concatena o valor da coluna na chave do objeto
			 */
			if (appendSeparator)
				uniqueIdTemp.append("_");
			uniqueIdTemp.append(resultSet.getObject(index));
			appendSeparator = true;
		}
		/*
		 * Retorna o chave única. Se for uma string "null" retorna como nula
		 */
		return (uniqueIdTemp.toString().equals("null") ? null : uniqueIdTemp.toString());
	}

	@Override
	public String toString(int level) {
		StringBuilder sb = new StringBuilder(StringUtils.repeat(" ", level * 4) + descriptionField.getField().getName() + " -> "
				+ targetEntityCache.getEntityClass().getSimpleName() + " : " + aliasColumnName
				+ ("".equals(aliasDiscriminatorColumnName) ? "" : " discriminator column " + aliasDiscriminatorColumnName));
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
