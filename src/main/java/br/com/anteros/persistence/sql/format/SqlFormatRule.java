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
package br.com.anteros.persistence.sql.format;

import java.util.Arrays;

import br.com.anteros.core.utils.ArrayUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.sql.format.tokenizer.TokenUtil;

public class SqlFormatRule implements ISqlFormatRule {
	private int convertKeyword = 1;

	private int convertName = 0;

	private String indentString = "    ";

	private int outNewLineCode = 0;

	private String outNewLineCodeStr = System.getProperty("line.separator");

	private char outNewLineEnd = System.getProperty("line.separator").charAt(
			System.getProperty("line.separator").length() - 1);

	private int outSqlSeparator = 1;

	private char outSqlSeparatorChar = '/';

	private boolean newLineBeforeComma = true;

	private boolean newLineBeforeAndOr = true;

	private boolean newLineDataTypeParen = false;

	private boolean newLineFunctionParen = false;

	private boolean decodeSpecialFormat = true;

	private boolean inSpecialFormat = true;

	private boolean betweenSpecialFormat = false;

	private boolean removeComment = false;

	private boolean removeEmptyLine = false;

	private boolean indentEmptyLine = false;

	private boolean wordBreak = false;

	private int width = 80;

	private String[] functions = TokenUtil.KEYWORD_FUNCTION;

	private String[] dataTypes = TokenUtil.KEYWORD_DATATYPE;

	public int getConvertKeyword() {
		return this.convertKeyword;
	}

	public void setConvertKeyword(int convertKeyword) {
		this.convertKeyword = convertKeyword;
	}

	public int getConvertName() {
		return this.convertName;
	}

	public void setConvertName(int convertName) {
		this.convertName = convertName;
	}

	public String getIndentString() {
		return this.indentString;
	}

	public void setIndentString(String indentString) {
		this.indentString = (indentString == null ? "" : indentString);
	}

	public boolean isNewLineBeforeComma() {
		return this.newLineBeforeComma;
	}

	public void setNewLineBeforeComma(boolean newLineBeforeComma) {
		this.newLineBeforeComma = newLineBeforeComma;
	}

	public boolean isNewLineDataTypeParen() {
		return this.newLineDataTypeParen;
	}

	public void setNewLineDataTypeParen(boolean newLineDataTypeParen) {
		this.newLineDataTypeParen = newLineDataTypeParen;
	}

	public boolean isNewLineFunctionParen() {
		return this.newLineFunctionParen;
	}

	public void setNewLineFunctionParen(boolean newLineFunctionParen) {
		this.newLineFunctionParen = newLineFunctionParen;
	}

	public boolean isRemoveComment() {
		return this.removeComment;
	}

	public void setRemoveComment(boolean removeComment) {
		this.removeComment = removeComment;
	}

	public boolean isRemoveEmptyLine() {
		return this.removeEmptyLine;
	}

	public void setRemoveEmptyLine(boolean removeEmptyLine) {
		this.removeEmptyLine = removeEmptyLine;
	}

	public boolean isIndentEmptyLine() {
		return this.indentEmptyLine;
	}

	public void setIndentEmptyLine(boolean indentEmptyLine) {
		this.indentEmptyLine = indentEmptyLine;
	}

	public boolean isWordBreak() {
		return this.wordBreak;
	}

	public void setWordBreak(boolean wordBreak) {
		this.wordBreak = wordBreak;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getOutNewLineCodeStr() {
		return this.outNewLineCodeStr;
	}

	public char getOutNewLineEnd() {
		return this.outNewLineEnd;
	}

	public char getOutSqlSeparatorChar() {
		return this.outSqlSeparatorChar;
	}

	public boolean isNewLineBeforeAndOr() {
		return this.newLineBeforeAndOr;
	}

	public void setNewLineBeforeAndOr(boolean newLineBeforeAndOr) {
		this.newLineBeforeAndOr = newLineBeforeAndOr;
	}

	public String[] getFunctions() {
		return this.functions;
	}

	public String[] getDataTypes() {
		return this.dataTypes;
	}

	public boolean isDecodeSpecialFormat() {
		return this.decodeSpecialFormat;
	}

	public void setDecodeSpecialFormat(boolean decodeSpecialFormat) {
		this.decodeSpecialFormat = decodeSpecialFormat;
	}

	public boolean isInSpecialFormat() {
		return this.inSpecialFormat;
	}

	public void setInSpecialFormat(boolean inSpecialFormat) {
		this.inSpecialFormat = inSpecialFormat;
	}

	public boolean isBetweenSpecialFormat() {
		return this.betweenSpecialFormat;
	}

	public void setBetweenSpecialFormat(boolean betweenSpecialFormat) {
		this.betweenSpecialFormat = betweenSpecialFormat;
	}

	public int getOutSqlSeparator() {
		return this.outSqlSeparator;
	}

	public void setOutSqlSeparator(int outSqlSeparator) {
		this.outSqlSeparator = outSqlSeparator;

		switch (this.outSqlSeparator) {
		case 0:
			break;
		case 1:
			this.outSqlSeparatorChar = '/';
			break;
		case 2:
			this.outSqlSeparatorChar = ';';
			break;
		}
	}

	public void setFunctions(String[] functions) {
		functions = StringUtils.toUpperCase(functions);
		this.functions = functions;
		Arrays.sort(this.functions);
	}

	public void setDataTypes(String[] dataTypes) {
		dataTypes = StringUtils.toUpperCase(dataTypes);
		this.dataTypes = dataTypes;
		Arrays.sort(this.dataTypes);
	}

	public void addFunctions(String[] functions) {
		functions = StringUtils.toUpperCase(functions);
		this.functions = ((String[]) ArrayUtils.add(this.functions, functions, new String[0]));
		Arrays.sort(this.functions);
	}

	public void addDataTypes(String[] dataTypes) {
		dataTypes = StringUtils.toUpperCase(dataTypes);
		this.dataTypes = ((String[]) ArrayUtils.add(this.dataTypes, dataTypes, new String[0]));
		Arrays.sort(this.dataTypes);
	}

	public void subtractFunctions(String[] functions) {
		functions = StringUtils.toUpperCase(functions);
		this.functions = ((String[]) ArrayUtils.subtract(this.functions, functions, new String[0]));
	}

	public void subtractDataTypes(String[] dataTypes) {
		dataTypes = StringUtils.toUpperCase(dataTypes);
		this.dataTypes = ((String[]) ArrayUtils.subtract(this.dataTypes, dataTypes, new String[0]));
	}

	public int getOutNewLineCode() {
		return this.outNewLineCode;
	}

	public void setOutNewLineCode(int outNewLineCode) {
		this.outNewLineCode = outNewLineCode;

		switch (outNewLineCode) {
		case 0:
			this.outNewLineCodeStr = System.getProperty("line.separator");
			break;
		case 1:
		case 2:
		case 3:
			this.outNewLineCodeStr = TokenUtil.NEW_LINES[(outNewLineCode - 1)];
		}

		this.outNewLineEnd = this.outNewLineCodeStr.charAt(this.outNewLineCodeStr.length() - 1);
	}

	public boolean isKeyword(String str) {
		return (Arrays.binarySearch(TokenUtil.KEYWORD, str) >= 0) || (Arrays.binarySearch(this.functions, str) >= 0)
				|| (Arrays.binarySearch(this.dataTypes, str) >= 0);
	}

	public boolean isFunctions(String str) {
		return Arrays.binarySearch(this.functions, str) >= 0;
	}

	public boolean isDataTypes(String str) {
		return Arrays.binarySearch(this.dataTypes, str) >= 0;
	}

	public boolean isName(String str) {
		boolean b = isKeyword(str);
		b |= TokenUtil.isSymbol(str);
		b |= TokenUtil.isValue(str);
		b |= TokenUtil.isComment(str);
		b |= TokenUtil.isSqlSeparate(str.charAt(0));
		return !b;
	}
}
