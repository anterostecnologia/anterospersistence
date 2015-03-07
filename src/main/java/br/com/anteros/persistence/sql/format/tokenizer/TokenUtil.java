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

import java.util.Arrays;

public class TokenUtil
{
  public static final String NEW_LINE_SYSTEM = System.getProperty("line.separator");

  public static final String[] NEW_LINES = { "\r\n", "\r", "\n" };

  public static final String[] DEBUG_NEW_LINES = { "★CRLF★", "★CR★", "★LF★" };
  public static final String NEW_LINES_REGEX;
  public static final char[] NEW_LINE_CHAR;
  public static final int TYPE_BEGIN_SQL = 0;
  public static final int TYPE_KEYWORD = 10;
  public static final int TYPE_SYMBOL = 20;
  public static final int TYPE_OPERATOR = 30;
  public static final int TYPE_NAME = 40;
  public static final int TYPE_VALUE = 50;
  public static final int TYPE_COMMENT = 60;
  public static final int TYPE_NEW_LINE = 70;
  public static final int TYPE_SQL_SEPARATE = 80;
  public static final int TYPE_EMPTY_LINE = 90;
  public static final int TYPE_END_SQL = 100;
  public static final int TYPE_UNKNOWN = -1;
  public static final int SUBTYPE_DEFAULT = 0;
  public static final int SUBTYPE_KEYWORD_DATATYPE = 11;
  public static final int SUBTYPE_KEYWORD_FUNCTION = 12;
  public static final int SUBTYPE_VALUE_STRING = 51;
  public static final int SUBTYPE_VALUE_NUMERIC = 52;
  public static final int SUBTYPE_VALUE_BIND = 53;
  public static final int SUBTYPE_COMMENT_SINGLE = 61;
  public static final int SUBTYPE_COMMENT_MULTI = 62;
  public static final String[] KEYWORD = { "ACCESS", "ADD", "ALL", "ALTER", 
    "AND", "ANY", "AS", "ASC", "AUDIT", "BETWEEN", "BEGIN", "BOTH", 
    "BY", "CACHE", "CASCADE", "CASE", "CHAR", "CHECK", "CLUSTER", 
    "COLUMN", "COMMENT", "COMMIT", "COMPRESS", "CONNECT", "CONSTRAINT", 
    "CREATE", "CROSS", "CURRENT", "CYCLE", "DATE", "DECIMAL", 
    "DECLARE", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", 
    "END", "ESCAPE", "EXCEPT", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT", 
    "FUNCTION", "FOR", "FOREIGN", "FROM", "GRANT", "GROUP", "HAVING", 
    "IDENTIFIED", "IF", "IMMEDIATE", "IN", "INCREMENT", "INDEX", 
    "INITIAL", "INNER", "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", 
    "JOIN", "KEY", "LEADING", "LEVEL", "LEFT", "LIKE", "LIMIT", "LOCK", "LONG", 
    "MERGE", "MATCH", "MATCHED", "MAXEXTENTS", "MAXVALUE", "MINUS", 
    "MINVALUE", "MLSLABEL", "MODE", "MODIFY", "NATURAL", "NOAUDIT", 
    "NOCOMPRESS", "NOCYCLE", "NOMAXVALUE", "NOMINVALUE", "NOT", 
    "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE", "OFFSET", "ON", "ONLINE", 
    "ONLY", "OPTION", "OR", "ORDER", "OUTER", "OVER", "PACKAGE", 
    "PARTITION", "PCTFREE", "PRIMARY", "PRIOR", "PRIVILEGES", 
    "PROCEDURE", "PUBLIC", "RAW", "READ", "RENAME", "RESOURCE", 
    "RETURN", "REVOKE", "RIGHT", "ROLLBACK", "ROW", "ROWID", "ROWNUM", 
    "ROWS", "SCHEMA", "SELECT", "SEQUENCE", "SESSION", "SET", "SHARE", 
    "SIZE", "SMALLINT", "SHOW", "START", "SUCCESSFUL", "SYNONYM", 
    "SYSDATE", "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TO", 
    "TRAILING", "TRIGGER", "TRUNCATE", "TYPE", "UID", "UNION", 
    "UNIQUE", "UPDATE", "USER", "USING", "VALIDATE", "VALUES", 
    "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER", "WHEN", "WHERE", "WITH" };

  public static final String[] KEYWORD_DATATYPE = { "BFILE", "BINARY_DOUBLE", 
    "BINARY_FLOAT", "BLOB", "CHAR", "CHARACTER", "CHAR VARYING", 
    "CHARACTER VARYING", "CLOB", "DATE", "DEC", "DECIMAL", 
    "DOUBLE PRECISION", "INTERVAL YEAR TO MONTH", "INT", "INTEGER", 
    "INTERVAL", "INTERVAL DAY TO SECOND", "LONG", "LONG RAW", 
    "NATIONAL CHAR", "NATIONAL CHARACTER", 
    "NATIONAL CHARACTER VARYING", "NATIONAL CHAR VARYING", "NCHAR", 
    "NCHAR VARYING", "NUMBER", "NUMERIC", "NVARCHAR2", "RAW", "REAL", 
    "ROWID", "SMALLINT", "TIME", "TIMESTAMP", 
    "TIMESTAMP WITH LOCAL TIMEZONE", "TIMESTAMP WITH TIMEZONE", 
    "VARCHAR", "VARCHAR2" };

  public static final String[] KEYWORD_FUNCTION = { "ABS", "ACOS", 
    "ADD_MONTHS", "ASCII", "ASIN", "ATAN", "AVG", "CAST", "CEIL", 
    "CHARTOROWID", "CHECK", "CHR", "COALESCE", "CONCAT", "CONVERT", 
    "COS", "COSH", "COUNT", "DECODE", "DUMP", "EXP", "FLOOR", 
    "GREATEST", "HEXTORAW", "INITCAP", "INSTR", "INSTRB", "LAST_DAY", 
    "LEAST", "LENGTH", "LENGTHB", "LN", "LOG", "LOWER", "LPAD", 
    "LTRIM", "MAX", "MIN", "MOD", "MONTHS_BETWEEN", "NEXT_DAY", 
    "NULLIF", "NVL", "NVL2", "POWER", "RAWTOHEX", "REPLACE", "ROUND", 
    "ROWIDTOCHAR", "ROW_NUMBER", "RPAD", "RTRIM", "SIGN", "SIN", 
    "SINH", "SQRT", "STDDEV", "SUBSTR", "SUBSTRB", "SUM", "SYSDATE", 
    "TAN", "TANH", "TO_CHAR", "TO_DATE", "TO_MULTI_BYTE", "TO_NUMBER", 
    "TO_SINGLE_BYTE", "TRIM", "TRUNC", "UID", "UPPER", "USER", 
    "USERENV", "VARIANCE", "VSIZE" };

  public static final String[] BEGIN_SQL_KEYWORD = { "ALTER", "COMMENT", 
    "CREATE", "DELETE", "DROP", "GRANT", "INSERT", "MARGE", "REVOKE", 
    "SELECT", "TRUNCATE", "UPDATE" };

  public static final String[] MULTI_KEYWORD = { "CREATE OR REPLACE", 
    "CREATE", "CROSS JOIN", "COMMENT ON", "FOR UPDATE", "FULL JOIN", 
    "FULL OUTER JOIN", "GROUP BY", "INCREMENT BY", "INNER JOIN", 
    "JOIN", "LEFT JOIN", "LEFT OUTER JOIN", "NATURAL JOIN", "ORDER BY", 
    "PARTITION BY", "RIGHT JOIN", "RIGHT OUTER JOIN", "START WITH", 
    "UNION ALL", "WHEN MATCHED THEN", "WHEN NOT MATCHED THEN", 
    "WITH CHECK OPTION", "WITH READ ONLY" };

  public static final String[] SPECIAL_VALUE = { "NULL", "SYSDATE" };

  public static final String[] SYMBOL = { "(", ")", "||", ".", ",", "::" };
  public static final char[] SYMBOL_CHAR;
  public static final String[] OPERATOR = { "!=", "*", "+", "-", "/", "<", 
    "<=", "<>", "=", ">", ">=", "^=" };
  public static final char[] OPERATOR_CHAR;
  public static final char[] BIND_VARIABLE = { ':', '?' };

  public static final String[] COMMENT = { "--", "/*", "*/" };

  public static final char[] WORD_SEPARATE = { ' ', '\t' };

  public static final char[] SQL_SEPARATE = { '/', ';' };

  static
  {
    SYMBOL_CHAR = getCharTable(SYMBOL).toCharArray();
    OPERATOR_CHAR = getCharTable(OPERATOR).toCharArray();
    NEW_LINE_CHAR = getCharTable(NEW_LINES).toCharArray();

    Arrays.sort(KEYWORD);
    Arrays.sort(KEYWORD_FUNCTION);
    Arrays.sort(KEYWORD_DATATYPE);
    Arrays.sort(BEGIN_SQL_KEYWORD);
    Arrays.sort(MULTI_KEYWORD);
    Arrays.sort(SPECIAL_VALUE);
    Arrays.sort(SYMBOL);
    Arrays.sort(SYMBOL_CHAR);
    Arrays.sort(BIND_VARIABLE);
    Arrays.sort(OPERATOR);
    Arrays.sort(OPERATOR_CHAR);
    Arrays.sort(COMMENT);
    Arrays.sort(WORD_SEPARATE);
    Arrays.sort(SQL_SEPARATE);
    Arrays.sort(NEW_LINE_CHAR);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < NEW_LINES.length; i++) {
      if (i != 0)
        sb.append('|');
      sb.append(NEW_LINES[i]);
    }
    NEW_LINES_REGEX = sb.toString();
  }

  private static String getCharTable(String[] strs)
  {
	  StringBuilder sb = new StringBuilder();
    for (int i = 0; i < strs.length; i++) {
      String str = strs[i];
      int len = strs[i].length();
      for (int j = 0; j < len; j++) {
        char c = str.charAt(j);
        if (sb.indexOf(Character.toString(c)) == -1)
          sb.append(c);
      }
    }
    return sb.toString();
  }

  public static boolean isBeginSqlKeyword(String str)
  {
    return Arrays.binarySearch(BEGIN_SQL_KEYWORD, str) >= 0;
  }

  public static boolean isMultiKeyword(String str)
  {
    return Arrays.binarySearch(MULTI_KEYWORD, str) >= 0;
  }

  public static boolean isSpecialValue(String str)
  {
    return Arrays.binarySearch(SPECIAL_VALUE, str) >= 0;
  }

  public static boolean isSymbol(String str)
  {
    return Arrays.binarySearch(SYMBOL, str) >= 0;
  }

  public static boolean isBindVariable(char c)
  {
    return Arrays.binarySearch(BIND_VARIABLE, c) >= 0;
  }

  public static boolean isValue(String str)
  {
    if (str == null)
      return false;
    return (str.startsWith("\"")) && (str.endsWith("\""));
  }

  public static boolean isComment(String str)
  {
    return Arrays.binarySearch(COMMENT, str) >= 0;
  }

  public static boolean isWordSeparate(char c)
  {
    return Arrays.binarySearch(WORD_SEPARATE, c) >= 0;
  }

  public static boolean isSqlSeparate(char c)
  {
    return Arrays.binarySearch(SQL_SEPARATE, c) >= 0;
  }

  public static boolean isNameChar(char c)
  {
    if (Character.isLetterOrDigit(c))
      return true;
    return (c == '_') || (c == '$') || (c == '#');
  }

  public static boolean isNumberChar(char c)
  {
    if (Character.isDigit(c))
      return true;
    switch (c) {
    case '+':
    case '-':
    case '.':
    case 'D':
    case 'E':
    case 'F':
    case 'd':
    case 'e':
    case 'f':
      return true;
    }

    return false;
  }

  public static boolean isSymbolChar(char c)
  {
    return Arrays.binarySearch(SYMBOL_CHAR, c) >= 0;
  }

  public static boolean isOperator(String str)
  {
    return Arrays.binarySearch(OPERATOR, str) >= 0;
  }

  public static boolean isOperatorChar(char c)
  {
    return Arrays.binarySearch(OPERATOR_CHAR, c) >= 0;
  }

  public static String debugTypeString(int type)
  {
    String str = "";
    switch (type) {
    case 0:
      str = "BEGIN_SQL";
      break;
    case 60:
      str = "COMMENT";
      break;
    case 90:
      str = "EMPTY_LINE";
      break;
    case 100:
      str = "END_SQL";
      break;
    case 10:
      str = "KEYWORD";
      break;
    case 40:
      str = "NAME";
      break;
    case 70:
      str = "NEW_LINE";
      break;
    case 30:
      str = "OPERATOR";
      break;
    case 80:
      str = "SQL_SEPARATE";
      break;
    case 20:
      str = "SYMBOL";
      break;
    case 50:
      str = "VALUE";
      break;
    case -1:
      str = "UNKNOWN";
    }

    return "[" + str + "]";
  }

  public static String debugSubTypeString(int subType)
  {
    String str = "";
    switch (subType) {
    case 62:
      str = "COMMENT_MULTI";
      break;
    case 61:
      str = "COMMENT_SINGLE";
      break;
    case 0:
      str = "DEFAULT";
      break;
    case 11:
      str = "KEYWORD_DATATYPE";
      break;
    case 12:
      str = "KEYWORD_FUNCTION";
      break;
    case 53:
      str = "VALUE_BIND";
      break;
    case 52:
      str = "VALUE_NUMERIC";
      break;
    case 51:
      str = "VALUE_STRING";
    }

    return "[" + str + "]";
  }

  public static String debugString(String str)
  {
    String debugString = str;
    for (int i = 0; i < NEW_LINES.length; i++) {
      debugString = debugString.replaceAll(NEW_LINES[i], 
        DEBUG_NEW_LINES[i]);
    }
    return "[" + debugString + "]";
  }

  public static boolean isNewLineChar(char c)
  {
    return Arrays.binarySearch(NEW_LINE_CHAR, c) >= 0;
  }

  public static boolean isValidToken(Token token)
  {
    if (token == null) {
      return false;
    }
    switch (token.getType()) {
    case 10:
    case 20:
    case 30:
    case 40:
    case 50:
    case 80:
      return true;
    }
    return false;
  }
}
