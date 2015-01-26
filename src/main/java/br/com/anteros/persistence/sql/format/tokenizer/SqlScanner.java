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
package br.com.anteros.persistence.sql.format.tokenizer;

public class SqlScanner {
	private String sql;
	private int length;
	private int current;

	public SqlScanner(String sql) {
		this.sql = sql;
		this.length = (sql == null ? 0 : sql.length());
	}

	public String getSql() {
		return this.sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int getLength() {
		return this.length;
	}

	public int getCurrent() {
		return this.current;
	}

	public boolean hasNext() {
		return (this.sql != null) && (this.current < this.length);
	}

	public boolean hasNext(int i) {
		return (this.sql != null) && (this.current + i < this.length);
	}

	public char next() {
		if (!hasNext()) {
			return 65535;
		}
		return this.sql.charAt(this.current++);
	}

	public char peek() {
		if (!hasNext()) {
			return 65535;
		}
		return this.sql.charAt(this.current);
	}

	public char peek(int i) {
		if (!hasNext(i)) {
			return 65535;
		}
		return this.sql.charAt(this.current + i);
	}

	public int skipSpaceTab() {
		int count = 0;
		char c = peek();
		while ((c == ' ') || (c == '\t')) {
			next();
			c = peek();
			count++;
		}

		return count;
	}

	public boolean isPeekEquals(String str) {
		if ((this.sql == null) && (str == null)) {
			return true;
		}
		if ((this.length > 0) && (str == null)) {
			return false;
		}
		int len = str.length();
		if (this.length - this.current < len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (peek(i) != str.charAt(i))
				return false;
		}
		return true;
	}

	public boolean isPeekEquals(String[] strs) {
		if ((strs == null) || (strs.length == 0)) {
			return false;
		}
		for (int i = strs.length - 1; i >= 0; i--) {
			if (isPeekEquals(strs[i]))
				return true;
		}
		return false;
	}

	public boolean isPeekEqualsEx(String str) {
		if ((this.sql == null) && (str == null)) {
			return true;
		}
		if ((this.length > 0) && (str == null)) {
			return false;
		}
		int len = str.length();
		if (this.length - this.current < len) {
			return false;
		}
		int pos = 0;
		for (int i = 0; i < len; i++) {
			pos = skipTabSpaceNewLine(pos);
			if (peek(pos) != str.charAt(i))
				return false;
			pos++;
		}
		return true;
	}

	public boolean isPeekNextEqualsEx(String str) {
		if ((this.sql == null) && (str == null)) {
			return true;
		}
		if ((this.length > 0) && (str == null)) {
			return false;
		}
		int len = str.length();
		if (this.length - this.current < len) {
			return false;
		}
		int pos = 0;
		for (int i = 0; i < len; i++) {
			pos = skipTabSpaceNewLine(pos);
			if (peek(pos) != str.charAt(i))
				return false;
			pos++;
		}

		this.current += pos;
		return true;
	}

	public String getPeekNextEqualsExString(String[] strs) {
		if ((this.sql == null) && (strs == null)) {
			return null;
		}
		if ((this.length > 0) && (strs == null)) {
			return null;
		}
		for (int i = strs.length - 1; i >= 0; i--) {
			String str = strs[i];
			int len = str.length();
			if (this.length - this.current < len) {
				continue;
			}
			boolean isFind = true;
			int pos = 0;
			for (int j = 0; j < len; j++) {
				pos = skipTabSpaceNewLine(pos);
				if (peek(pos) != str.charAt(j)) {
					isFind = false;
					break;
				}
				pos++;
			}

			if (!isFind) {
				continue;
			}
			this.current += pos;
			return strs[i];
		}

		return null;
	}

	private int skipTabSpaceNewLine(int start) {
		int pos = start;
		char c = peek(pos);
		while ((TokenUtil.isWordSeparate(c)) || (TokenUtil.isNewLineChar(c))) {
			pos++;
			c = peek(pos);
		}
		return pos;
	}

	public String substring(int beginIndex) {
		int len = this.current - beginIndex;
		if (len < 0)
			return null;
		return this.sql.substring(beginIndex, this.current);
	}
}
