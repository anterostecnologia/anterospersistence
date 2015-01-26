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
package br.com.anteros.persistence.sql.dialect.type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.persistence.sql.dialect.SQLiteDialect;

/**
 * Classe wrapper utilizada para representar um objeto Data no banco de dados.
 * 
 * @author Edson Martins - Anteros
 * 
 */

public class SQLiteDate {

	private Date date;
	private SimpleDateFormat dateFormat;

	public SQLiteDate(Date date, TemporalType temporalType) {
		createDateFormat(temporalType);
		this.date = date;
	}

	public SQLiteDate(String date, TemporalType temporalType) {
		createDateFormat(temporalType);
		try {
			this.date = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna data formatada para gravação no Banco de Dados
	 * 
	 * @return
	 */
	public String getFormatted() {
		return dateFormat.format(date);
	}

	public Date getDate() {
		return date;
	}

	private void createDateFormat(TemporalType temporalType) {
		if (temporalType == TemporalType.DATE)
			this.dateFormat = new SimpleDateFormat(SQLiteDialect.DATE_PATTERN);
		else if (temporalType == TemporalType.DATE_TIME)
			this.dateFormat = new SimpleDateFormat(
					SQLiteDialect.DATETIME_PATTERN);
		else
			this.dateFormat = new SimpleDateFormat(SQLiteDialect.TIME_PATTERN);
	}

	@Override
	public String toString() {
		return getFormatted();
	}

}
