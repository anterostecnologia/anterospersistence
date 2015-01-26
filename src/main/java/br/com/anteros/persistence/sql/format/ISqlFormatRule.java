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

public abstract interface ISqlFormatRule
{
  public static final int CONVERT_STRING_NONE = 0;
  public static final int CONVERT_STRING_UPPERCASE = 1;
  public static final int CONVERT_STRING_LOWERCASE = 2;
  public static final int CONVERT_STRING_CAPITALCASE = 3;
  public static final int NEWLINE_CODE_SYSTEM = 0;
  public static final int NEWLINE_CODE_CRLF = 1;
  public static final int NEWLINE_CODE_CR = 2;
  public static final int NEWLINE_CODE_LF = 3;
  public static final int SQL_SEPARATOR_NONE = 0;
  public static final int SQL_SEPARATOR_SLASH = 1;
  public static final int SQL_SEPARATOR_SEMICOLON = 2;

  public abstract int getConvertKeyword();

  public abstract void setConvertKeyword(int paramInt);

  public abstract int getConvertName();

  public abstract void setConvertName(int paramInt);

  public abstract String getIndentString();

  public abstract void setIndentString(String paramString);

  public abstract boolean isNewLineBeforeComma();

  public abstract void setNewLineBeforeComma(boolean paramBoolean);

  public abstract boolean isNewLineBeforeAndOr();

  public abstract void setNewLineBeforeAndOr(boolean paramBoolean);

  public abstract boolean isNewLineDataTypeParen();

  public abstract void setNewLineDataTypeParen(boolean paramBoolean);

  public abstract boolean isNewLineFunctionParen();

  public abstract void setNewLineFunctionParen(boolean paramBoolean);

  public abstract boolean isDecodeSpecialFormat();

  public abstract void setDecodeSpecialFormat(boolean paramBoolean);

  public abstract boolean isInSpecialFormat();

  public abstract void setInSpecialFormat(boolean paramBoolean);

  public abstract boolean isBetweenSpecialFormat();

  public abstract void setBetweenSpecialFormat(boolean paramBoolean);

  public abstract boolean isRemoveComment();

  public abstract void setRemoveComment(boolean paramBoolean);

  public abstract boolean isRemoveEmptyLine();

  public abstract void setRemoveEmptyLine(boolean paramBoolean);

  public abstract boolean isIndentEmptyLine();

  public abstract void setIndentEmptyLine(boolean paramBoolean);

  public abstract boolean isWordBreak();

  public abstract void setWordBreak(boolean paramBoolean);

  public abstract int getWidth();

  public abstract void setWidth(int paramInt);

  public abstract int getOutNewLineCode();

  public abstract void setOutNewLineCode(int paramInt);

  public abstract int getOutSqlSeparator();

  public abstract void setOutSqlSeparator(int paramInt);

  public abstract String[] getFunctions();

  public abstract void setFunctions(String[] paramArrayOfString);

  public abstract void addFunctions(String[] paramArrayOfString);

  public abstract void subtractFunctions(String[] paramArrayOfString);

  public abstract String[] getDataTypes();

  public abstract void setDataTypes(String[] paramArrayOfString);

  public abstract void addDataTypes(String[] paramArrayOfString);

  public abstract void subtractDataTypes(String[] paramArrayOfString);
}
