package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.cache.PersistenceMetadataCache;
import br.com.anteros.persistence.session.cache.SQLCache;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzer;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzerException;
import br.com.anteros.persistence.session.query.SQLQueryAnalyzerResult;

public class MultiSelectHandler implements ResultSetHandler {

	private static final boolean LOAD_ALL_FIELDS = false;
	private static final boolean ALLOW_DUPLICATE_OBJECTS = true;
	private List<ResultClassDefinition> definitions;
	private Map<ResultClassDefinition, EntityHandler> cacheHandler = new HashMap<ResultClassDefinition, EntityHandler>();
	private SQLSession session;
	private String sql;
	private String parsedSql;
	private int numberOfColumnAlias;

	public MultiSelectHandler(SQLSession session, String sql, List<ResultClassDefinition> definitions, int numberOfColumnAlias) throws SQLQueryAnalyzerException {
		this.definitions = definitions;
		this.session = session;
		this.sql = sql;
		this.numberOfColumnAlias = numberOfColumnAlias;
		this.parsedSql = parseSqlForResultClass();
	}

	@Override
	public Object handle(ResultSet resultSet) throws Exception {
		List<Object[]> result = new ArrayList<Object[]>();
		SQLCache transactionCache = new SQLCache();

		EntityHandler.validateDuplicateColumns(resultSet, null);

		if (resultSet.next()) {
			try {
				/*
				 * Faz o loop para processar os registros do ResultSet
				 */
				do {
					List<Object> record = new ArrayList<Object>();

					/*
					 * Primeira etapa para montagem dos objetos
					 */
					for (ResultClassDefinition rcd : definitions) {
						if (session.getEntityCacheManager().isEntity(rcd.getResultClass())) {

							/*
							 * Obtém a análise do sql para a classe de resultado
							 */
							SQLQueryAnalyzerResult analyzerResult = getAnalyzerResult(rcd);

							/*
							 * Obtém o Handler para a criação do objeto
							 */
							EntityHandler entityHandler = getEntityHandler(rcd, analyzerResult, transactionCache);

							/*
							 * Se a classe passada for uma entidade abstrata localiza no entityCache a classe
							 * correspondente ao discriminator colum
							 */
							EntityCache entityCache = entityHandler.getEntityCacheByResultSetRow(rcd.getResultClass(), resultSet);
							/*
							 * Processa a linha do resultSet montando o objeto da classe de resultado
							 */
							List<Object> resultHandler = new ArrayList<Object>();
							if (entityCache != null) {
								entityHandler.handleRow(resultSet, resultHandler, entityCache, LOAD_ALL_FIELDS);
							}
							if (resultHandler.size() > 0)
								record.add(resultHandler.get(0));
							else
								record.add(null);
						} else {
							Object value = resultSet.getObject(rcd.getSimpleColumn().getColumnIndex());
							record.add(ObjectUtils.convert(value, rcd.getResultClass()));
						}
					}

					/*
					 * Segunda etapa para finzalizar montagem dos objetos carregando as coleções e relacionamentos
					 */
					int index = 0;
					for (ResultClassDefinition rcd : definitions) {
						if (session.getEntityCacheManager().isEntity(rcd.getResultClass())) {
							/*
							 * Obtém a análise do sql para a classe de resultado
							 */
							SQLQueryAnalyzerResult analyzerResult = getAnalyzerResult(rcd);

							/*
							 * Obtém o Handler para a criação do objeto
							 */
							EntityHandler entityHandler = getEntityHandler(rcd, analyzerResult, transactionCache);

							/*
							 * Se a classe passada for uma entidade abstrata localiza no entityCache a classe
							 * correspondente ao discriminator colum
							 */
							EntityCache entityCache = entityHandler.getEntityCacheByResultSetRow(rcd.getResultClass(), resultSet);
							Object targetObject = record.get(index);
							if (targetObject != null) {
								entityHandler.loadCollectionsRelationShipAndLob(targetObject, entityCache, resultSet);
							}

						}
						index++;
					}

					result.add(record.toArray());
				} while (resultSet.next());

			} catch (SQLException ex) {
				throw new MultiSelectHandlerException("Erro processando MultiSelectHandler." + ex.getMessage());
			}
		}

		return result;
	}

	SQLQueryAnalyzerResult getAnalyzerResult(ResultClassDefinition rcd, String parsedSql, String originalSql) throws SQLQueryAnalyzerException {
		SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance().get(
				rcd.getResultClass().getName() + ":" + originalSql);
		if (analyzerResult == null) {
			analyzerResult = new SQLQueryAnalyzer(session.getEntityCacheManager(), session.getDialect(), SQLQueryAnalyzer.IGNORE_NOT_USED_ALIAS_TABLE)
					.analyze(parsedSql, rcd.getResultClass());
			PersistenceMetadataCache.getInstance().put(rcd.getResultClass().getName() + ":" + originalSql, analyzerResult);
		}
		return analyzerResult;
	}

	SQLQueryAnalyzerResult getAnalyzerResult(ResultClassDefinition rcd) throws SQLQueryAnalyzerException {
		SQLQueryAnalyzerResult analyzerResult = (SQLQueryAnalyzerResult) PersistenceMetadataCache.getInstance().get(
				rcd.getResultClass().getName() + ":" + sql);
		if (analyzerResult == null) {
			throw new MultiSelectHandlerException("Não foi possível encontrar a análise do SQL para a classe de resultado "
					+ rcd.getResultClass().getName());
		}
		return analyzerResult;
	}

	private EntityHandler getEntityHandler(ResultClassDefinition rcd, SQLQueryAnalyzerResult analyzerResult, SQLCache transactionCache)
			throws Exception {
		EntityHandler result = cacheHandler.get(rcd);
		if (result == null) {
			result = session.createNewEntityHandler(rcd.getResultClass(), analyzerResult.getExpressionsFieldMapper(),
					analyzerResult.getColumnAliases(), transactionCache, ALLOW_DUPLICATE_OBJECTS, null, 0, 0, false, LockOptions.NONE);
			cacheHandler.put(rcd, result);
		}
		return result;
	}

	protected String parseSqlForResultClass() throws SQLQueryAnalyzerException {

		String parsedSql = sql;
		for (ResultClassDefinition rcd : definitions) {
			if (session.getEntityCacheManager().isEntity(rcd.getResultClass())) {
				SQLQueryAnalyzerResult analyzerResult = getAnalyzerResult(rcd, parsedSql, sql);
				parsedSql = analyzerResult.getParsedSql();
			}
		}

		return parsedSql;
	}

	public String getParsedSql() {
		return parsedSql;
	}

}
