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
package br.com.anteros.persistence.metadata.identifier;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.sql.command.Insert;
import br.com.anteros.persistence.sql.command.Select;
import br.com.anteros.persistence.sql.command.Update;

public class TableGenerator implements IdentifierGenerator {

	private static Logger log = LoggerProvider.getInstance().getLogger(TableGenerator.class.getName());

	private SQLSession session;
	private String tableName;
	private String pkColumnName;
	private String value;
	private String valueColumnName;
	private Type type;
	private String catalog = null;
	private String schema = null;
	private Select select;
	private Update update;
	private Insert insert;
	private int initialValue;

	public TableGenerator(SQLSession session, String tableName, String pkColumnName, String valueColumnName, String value, Type type,
			String parameters, String catalog, String schema, int initialValue) {
		this.session = session;
		this.tableName = tableName;
		this.type = type;
		this.value = value;
		this.initialValue = initialValue;

		if (this.tableName.indexOf('.') < 0) {
			StringBuilder sb = new StringBuilder();
			if ((this.catalog != null) && ("".equals(this.catalog)))
				sb.append(session.getDialect().quote(this.catalog)).append(".");
			if ((this.schema != null) && ("".equals(this.schema)))
				sb.append(session.getDialect().quote(this.schema)).append(".");
			sb.append(session.getDialect().quote(tableName));
			this.tableName = sb.toString();
		}

		this.pkColumnName = session.getDialect().quote(pkColumnName);
		this.valueColumnName = session.getDialect().quote(valueColumnName);

		select = new Select(session.getDialect());
		select.addTableName(tableName);
		select.addColumn(valueColumnName);
		select.addCondition(pkColumnName, "=?");

		insert = new Insert(session.getDialect());
		insert.setTableName(tableName);
		insert.addColumns(new String[] { pkColumnName, valueColumnName });

		update = new Update(session.getDialect());
		update.setTableName(tableName);
		update.addColumn(valueColumnName);
		update.addWhereColumn(pkColumnName);
	}

	public Serializable generate() throws Exception {
		long currentValue = 1;
		try {
			String sql = select.toStatementString();
			ResultSet rsSelect = session.createQuery(sql, new Object[] { value }, LockOptions.PESSIMISTIC_WRITE).executeQuery();
			if (!rsSelect.next())
				currentValue = 0;
			else
				currentValue = rsSelect.getLong(1);
			rsSelect.close();
			rsSelect.getStatement().close();

			if (currentValue == 0) {
				currentValue += 1;
				session.update(insert.toStatementString(), new Object[] { value, currentValue });
			} else {
				currentValue += 1;
				session.update(update.toStatementString(), new Object[] { currentValue, value });
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		Long value = new Long(currentValue);
		if (type == Long.class)
			return value;
		else if (type == Integer.class)
			return new Integer(value.intValue());
		else if (type == Double.class)
			return new Double(value.doubleValue());
		else if (type == Float.class)
			return new Float(value.floatValue());
		else if (type == Long.class)
			return value;
		else if (type == BigDecimal.class)
			return new BigDecimal(value.longValue());
		return null;
	}

	public String getTableName() {
		return tableName;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public Object getValue() {
		return value;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public String getCatalog() {
		return catalog;
	}

	public String getSchema() {
		return schema;
	}

	public Select getSelect() {
		return select;
	}

	public Update getUpdate() {
		return update;
	}

	public Insert getInsert() {
		return insert;
	}

	public int getInitialValue() {
		return initialValue;
	}

}
