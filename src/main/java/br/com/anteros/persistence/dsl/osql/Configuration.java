package br.com.anteros.persistence.dsl.osql;

import java.io.InputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.dsl.osql.types.IndexHint;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.schema.definition.type.ColumnDatabaseType;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.sql.dialect.DatabaseDialect;

public final class Configuration {

	protected final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	protected final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	protected final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

	private EntityCacheManager entityCacheManager;
	private DatabaseDialect dialect;
	private SQLTemplates templates;
	private int nextAliasTableName = 0;
	private int nextAliasColumnName = 0;

	public Configuration(SQLSession session) {
		this.entityCacheManager = session.getEntityCacheManager();
		this.dialect = session.getDialect();
		this.templates = session.getDialect().getTemplateSQL();
	}

	public Configuration(EntityCacheManager entityCacheManager, DatabaseDialect dialect, SQLTemplates templates) {
		this.entityCacheManager = entityCacheManager;
		this.dialect = dialect;
		this.templates = templates;
	}

	public EntityCacheManager getEntityCacheManager() {
		return entityCacheManager;
	}

	public void setEntityCacheManager(EntityCacheManager entityCacheManager) {
		this.entityCacheManager = entityCacheManager;
	}

	public DatabaseDialect getDialect() {
		return dialect;
	}

	public void setDialect(DatabaseDialect dialect) {
		this.dialect = dialect;
	}

	public SQLTemplates getTemplates() {
		return templates;
	}

	public void setTemplates(SQLTemplates templates) {
		this.templates = templates;
	}

	public String convertJavaToDatabaseType(Class<?> javaType) {
		return dialect.convertJavaToDatabaseType(javaType).getName();
	}

	/**
	 * Retorna número do próximo alias de coluna.
	 * 
	 * @return Próximo número
	 */
	public int getNextAliasColumnName() {
		return nextAliasColumnName;
	}

	/**
	 * Gera um alias aleatório para uso nas tabelas do SQL.
	 * 
	 * @return
	 */
	public String makeNextAliasTableName() {
		nextAliasTableName++;
		String result = "T_B_" + String.valueOf(nextAliasTableName);
		return result;
	}

	/**
	 * Gera um alias aleatório para um alias de uma tabela para uso em uma coluna.
	 * 
	 * @param alias
	 *            Alias da tabela
	 * @return Alias para coluna gerado.
	 */
	public String makeNextAliasName(String alias) {
		nextAliasColumnName++;
		return adpatAliasColumnName(alias) + "_COL_" + String.valueOf(nextAliasColumnName);
	}

	/**
	 * Ajusta o nome do alias gerado para a coluna para não ultrapassar o máximo de caracteres permitido pelo dialeto do
	 * banco de dados.
	 * 
	 * @param aliasColumnNamePrefix
	 *            Prefixo do nome da coluna
	 * @return Alias ajustado
	 */
	private String adpatAliasColumnName(String aliasColumnNamePrefix) {
		int maximumNameLength = dialect.getMaxColumnNameSize() - 8;
		String result = adjustName(aliasColumnNamePrefix);

		if (result.length() > maximumNameLength) {
			result = StringUtils.removeAllButAlphaNumericToFit(aliasColumnNamePrefix, maximumNameLength);
			if (result.length() > maximumNameLength) {
				String onlyAlphaNumeric = StringUtils.removeAllButAlphaNumericToFit(aliasColumnNamePrefix, 0);
				result = StringUtils.shortenStringsByRemovingVowelsToFit(onlyAlphaNumeric, "", maximumNameLength);
				if (result.length() > maximumNameLength) {
					String shortenedName = StringUtils.removeVowels(onlyAlphaNumeric);
					if (shortenedName.length() >= maximumNameLength) {
						result = StringUtils.truncate(shortenedName, maximumNameLength);
					} else {
						result = StringUtils.truncate(shortenedName, maximumNameLength - shortenedName.length());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Ajusta o nome fazendo algumas correções.
	 * 
	 * @param name
	 *            Nome
	 * @return Nome ajustado
	 */
	private String adjustName(String name) {
		String adjustedName = name;
		if (adjustedName.indexOf(' ') != -1 || adjustedName.indexOf('\"') != -1 || adjustedName.indexOf('`') != -1) {
			StringBuilder buff = new StringBuilder();
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c != ' ' && c != '\"' && c != '`') {
					buff.append(c);
				}
			}
			adjustedName = buff.toString();
		}
		return adjustedName;
	}

	public String getIndexHint(IndexHint... indexes) {
		Map<String, String> _indexes = new LinkedHashMap<String, String>();
		for (IndexHint index : indexes) {
			_indexes.put(index.getIndexName(), index.getAlias());
		}

		return dialect.getIndexHint(_indexes);
	}

	public QueryFlag.Position getIndexHintPosition() {
		return dialect.getIndexHintPosition();
	}

	public String asLiteral(Object value) {
		if (value == null)
			return "";
		ColumnDatabaseType columnDatabaseType = dialect.convertJavaToDatabaseType(value.getClass());
		if (columnDatabaseType != null) {
			return templates.serialize(convertObjectToLiteral(value), columnDatabaseType.getSqlType());
		} else {
			throw new IllegalArgumentException("Unsupported literal type " + value.getClass().getName());
		}
	}

	protected String convertObjectToLiteral(Object value) {
		if (value instanceof Calendar) {
			return dateTimeFormatter.print(((Calendar) value).getTimeInMillis());
		} else if (value instanceof DateTime) {
			return dateTimeFormatter.print((DateTime) value);
		} else if (value instanceof Date) {
			return dateFormatter.print(((Date) value).getTime());
		} else if (value instanceof java.sql.Date) {
			return dateFormatter.print(((java.sql.Date) value).getTime());
		} else if (value instanceof InputStream) {
			return value.toString();
		} else if (value instanceof Timestamp) {
			return dateTimeFormatter.print(((Timestamp) value).getTime());
		} else if (value instanceof Time) {
			return timeFormatter.print(((Time) value).getTime());
		} else if (value instanceof String) {
			return value.toString();
		} else if (value instanceof Enum<?>) {
			return convertEnumToValue((Enum<?>) value);
		}
		return value.toString();
	}

	protected String escapeLiteral(String str) {
		StringBuilder builder = new StringBuilder();
		for (char ch : str.toCharArray()) {
			if (ch == '\n') {
				builder.append("\\n");
				continue;
			} else if (ch == '\r') {
				builder.append("\\r");
				continue;
			} else if (ch == '\'') {
				builder.append("''");
				continue;
			}
			builder.append(ch);
		}
		return builder.toString();
	}

	public String convertEnumToValue(Enum<?> en) {
		return entityCacheManager.convertEnumToValue(en);
	}

}
