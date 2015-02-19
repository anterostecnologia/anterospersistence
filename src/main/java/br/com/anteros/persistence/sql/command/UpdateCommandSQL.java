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
package br.com.anteros.persistence.sql.command;

import java.sql.SQLException;
import java.util.List;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionSQL;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.exception.SQLSessionException;

public class UpdateCommandSQL extends CommandSQL {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(UpdateCommandSQL.class.getName());

	public UpdateCommandSQL(SQLSession session, String sql, List<NamedParameter> params, Object targetObject, EntityCache entityCache,
			String targetTableName, boolean showSql, DescriptionSQL descriptionSQL) {
		super(session, sql, params, targetObject, entityCache, targetTableName, showSql, descriptionSQL);
	}

	@Override
	public void execute() throws Exception {
		/*
		 * Executa o SQL
		 */
		if (StringUtils.isNotEmpty(this.getSql())) {
			/*
			 * Troca os parâmetros que aguardam o identificador de outro objeto pelo valor do identificador gerado
			 */
			for (NamedParameter namedParameter : namedParameters) {
				if (namedParameter.getValue() instanceof IdentifierPostInsert)
					namedParameter.setValue(((IdentifierPostInsert) namedParameter.getValue()).generate());
			}
			try {
				if ((descriptionSQL != null) && descriptionSQL.isCallable()) {
					ProcedureResult result = null;
					try {
						result = queryRunner.executeProcedure(session, session.getDialect(), descriptionSQL.getCallableType(),
								descriptionSQL.getSql(), NamedParameter.toArray(namedParameters), showSql, 0, session.clientId());
						/*
						 * Verifica se houve sucesso na execução
						 */
						Object successValue;
						if (descriptionSQL.getCallableType() == CallableType.PROCEDURE)
							successValue = result.getOutPutParameter(descriptionSQL.getSuccessParameter());
						else
							successValue = result.getFunctionResult();

						if (showSql) {
							LOG.debug("RESULT = " + successValue);
							LOG.debug("");
						}

						if (!descriptionSQL.getSuccessValue().equalsIgnoreCase(successValue.toString()))
							throw new SQLSessionException(successValue.toString());
					} finally {
						if (result != null)
							result.close();
					}
				} else {
					int rowsUpdated;
					if (descriptionSQL != null)
						rowsUpdated = queryRunner.update(session.getConnection(), descriptionSQL.getSql(),
								descriptionSQL.processParameters(namedParameters), showSql, session.getListeners(), session.clientId());
					else
						rowsUpdated = queryRunner.update(this.getSession().getConnection(), sql, NamedParameter.getAllValues(namedParameters),
								showSql, session.getListeners(), session.clientId());
					if (rowsUpdated == 0) {
						if (entityCache.isVersioned())
							throw new SQLException("Objeto foi atualizado ou removido por outra transação. " + this.getObjectId());
						else
							throw new SQLException("Não foi possível atualizar o objeto " + this.getObjectId()
									+ " pois o mesmo não foi encontrado. Verifique os parâmetros.");
					}
				}

				/*
				 * Se o objeto alvo não for uma entidade for um List<String> ou Map<String,Object> retorna
				 */

				if (targetObject == null)
					return;
			} catch (SQLException ex) {
				throw session.getDialect().convertSQLException(ex, "", sql);
			}
		}
		setEntityManaged();
	}

	@Override
	public boolean isNewEntity() {
		return false;
	}

}
