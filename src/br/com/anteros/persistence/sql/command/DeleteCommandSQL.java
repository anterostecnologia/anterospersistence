/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.persistence.sql.command;

import java.util.List;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.metadata.descriptor.DescriptionSQL;
import br.com.anteros.persistence.metadata.identifier.IdentifierPostInsert;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.util.StringUtils;

public class DeleteCommandSQL extends CommandSQL {

	public DeleteCommandSQL(SQLSession session, String sql, List<NamedParameter> namedParameters, Object targetObject,
			EntityCache entityCache, String targetTableName, boolean showSql, DescriptionSQL descriptionSQL) {
		super(session, sql, namedParameters, targetObject, entityCache, targetTableName, showSql, descriptionSQL);
	}

	@Override
	public void execute() throws Exception {
		/*
		 * Troca os parâmetros que aguardam o identificador de outro objeto pelo
		 * valor do identificador gerado
		 */
		for (NamedParameter parameter : namedParameters) {
			if (parameter.getValue() instanceof IdentifierPostInsert)
				parameter.setValue(((IdentifierPostInsert) parameter.getValue()).generate());
		}
		/*
		 * Executa o SQL
		 */
		if (StringUtils.isNotEmpty(this.getSql())) {
			if ((descriptionSQL != null) && descriptionSQL.isCallable()) {
				
				ProcedureResult result = queryRunner.executeProcedure(session, session.getDialect(),
						descriptionSQL.getCallableType(), descriptionSQL.getSql(),
						descriptionSQL.getInputParameters(namedParameters),
						descriptionSQL.getOutputParameters(namedParameters), showSql, 0, session.clientId());
				/*
				 * Verifica se houve sucesso na execução
				 */
				Object successValue;
				if (descriptionSQL.getCallableType() == CallableType.PROCEDURE)
					successValue = result.getOutPutParameter(descriptionSQL.getSuccessParameter());
				else
					successValue = result.getFunctionResult();

				if (showSql) {
					System.out.println("RESULT = " + successValue);
					System.out.println("");
				}

				if (!descriptionSQL.getSuccessValue().equals(successValue.toString()))
					throw new SQLSessionException(successValue.toString());
			} else {
				if (descriptionSQL != null)
					queryRunner.update(session.getConnection(), descriptionSQL.getSql(),
							descriptionSQL.processParameters(namedParameters), showSql,session.getListeners(), session.clientId());
				else
					queryRunner.update(this.getSession().getConnection(), sql,
							NamedParameter.getAllValues(namedParameters), showSql,session.getListeners(),session.clientId());
			}
			/*
			 * Se o objeto alvo não for uma entidade for um List<String> ou
			 * Map<String,Object> remove da lista de entidades gerenciadas
			 */
			if (targetObject != null) {
				session.getPersistenceContext().removeEntityManaged(targetObject);
			}
		}
	}

	@Override
	public boolean isNewEntity() {
		return false;
	}

}
