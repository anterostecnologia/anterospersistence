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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.sql.format.tokenizer.SqlTokenizer;
import br.com.anteros.persistence.sql.format.tokenizer.Token;
import br.com.anteros.persistence.sql.format.tokenizer.TokenUtil;

public class SqlFormat implements ISqlFormat {
	private SqlFormatRule rule;
	private SqlTokenizer tokenizer;

	public SqlFormat(ISqlFormatRule rule) {
		this.rule = ((SqlFormatRule) rule);
	}

	public String format(String sql) throws SqlFormatException {
		return format(sql, 0);
	}

	public String format(String sql, int offset) throws SqlFormatException {
		try {
			return innerFormat(sql, offset);
		} catch (Exception e) {
			throw new SqlFormatException(e.getMessage());
		}
	}

	public String unFormat(String sql) {
		this.setTokenizer(new SqlTokenizer(sql, this.rule));

		StringBuilder sb = new StringBuilder();
		int beforeType = 0;

		for (Iterator<Token> it = this.getTokenizer(); it.hasNext();) {
			Token token = (Token) it.next();
			int type = token.getType();
			int subType = token.getSubType();
			String upper = token.getUpper();
			int len = sb.length();
			char lc = len == 0 ? 65535 : sb.charAt(len - 1);
			boolean insertSpace = true;

			switch (type) {
			case 20:
				if ("(".equals(upper)) {
					Token parentTokenInParen = token.getParentTokenInParen();
					if (parentTokenInParen == null)
						break;
					int parentType = parentTokenInParen.getType();
					int parentSubType = parentTokenInParen.getSubType();
					if ((parentSubType != 11) && (parentSubType != 12) && (parentType != 40))
						break;
					insertSpace = false;
				} else {
					if ((!")".equals(upper)) && (!"(*)".equals(upper)) && (!"(+)".equals(upper)))
						break;
					insertSpace = false;
				}
				break;
			case 60:
				if (!this.rule.isRemoveComment())
					break;
				insertSpace = false;
				break;
			case 70:
			case 80:
			case 90:
				insertSpace = false;
				break;
			default:
				insertSpace = true;
			}

			if ((insertSpace) && (lc != '(')) {
				if ((!this.rule.isNewLineBeforeComma()) || (!",".equals(upper))) {
					if ((this.rule.isNewLineBeforeComma()) || (lc != ',')) {
						if ((beforeType != 0) && (beforeType != 80)) {
							sb.append(' ');
						}
					}
				}
			}
			switch (type) {
			case 70:
			case 90:
				break;
			case 60:
				if (this.rule.isRemoveComment()) {
					break;
				}
			default:
				sb.append(convertString(token, 0, false));

				if ((type != 60) || (subType != 61))
					break;
				sb.append(this.rule.getOutNewLineCodeStr());
			}

			beforeType = type;
		}

		return sb.toString().trim();
	}

	public String unFormat2(String sql) throws SqlFormatException {
		try {
			String indentString = this.rule.getIndentString();
			this.rule.setIndentString("");

			String unFormat = innerFormat(sql, 0);
			this.rule.setIndentString(indentString);

			return unFormat;
		} catch (Exception e) {

			throw new SqlFormatException(e.getMessage());
		}
	}

	private String innerFormat(String sql, int offset) throws Exception {
		this.setTokenizer(new SqlTokenizer(sql, this.rule));

		int initIndent = offset == 0 ? 0 : (offset - 1) / this.rule.getIndentString().length() + 1;

		StringBuilder sb = new StringBuilder();
		int indent = initIndent;
		boolean isBetween = false;
		boolean isOnUsing = false;
		boolean isTrim = false;
		Token sqlToken = null;
		Stack<Integer> parenStack = new Stack<Integer>();
		List<Token> selectList = new ArrayList<Token>();

		for (Iterator<Token> it = this.getTokenizer(); it.hasNext();) {
			Token token = (Token) it.next();
			int type = token.getType();
			String upper = token.getUpper();
			Token parantTokenInParen = token.getParentTokenInParen();
			token.setIndent(indent);

			switch (type) {
			case 10:
				if (sqlToken == null) {
					sqlToken = token;
				}

				if ("SELECT".equals(upper)) {
					if ((!"SELECT".equals(sqlToken.getUpper())) && (selectList.isEmpty())) {
						newLine(sb);
						indent++;
					}
					append(sb, token, indent);
					newLine(sb);
					indent += 2;
					selectList.add(token);
				} else if (("UPDATE".equals(upper)) || ("DELETE".equals(upper))) {
					if ("CREATE".equals(sqlToken.getUpper())) {
						append(sb, token, indent);
					} else {
						append(sb, token, indent);
						newLine(sb);
						indent += 2;
					}
				} else if (("FROM".endsWith(upper)) || ("WHERE".equals(upper)) || ("HAVING".equals(upper)) || ("ORDER BY".equals(upper))
						|| ("GROUP BY".equals(upper)) || ("SET".equals(upper)) || ("VALUES".equals(upper)) || ("WITH CHECK OPTION".equals(upper))
						|| ("WITH READ ONLY".equals(upper))) {
					if ((isTrim) && ("FROM".endsWith(upper)) && (parantTokenInParen != null) && ("TRIM".equals(parantTokenInParen.getUpper()))) {
						append(sb, token, indent);
						isTrim = false;
					} else {
						newLine(sb);

						if (isOnUsing) {
							indent -= 3;
							isOnUsing = false;
						} else {
							indent--;
						}

						append(sb, token, indent);
						newLine(sb);
						indent++;
					}
				} else if (("AND".equals(upper)) || ("OR".equals(upper))) {
					if ((isBetween) && ("AND".equals(upper))) {
						if (this.rule.isBetweenSpecialFormat()) {
							newLine(sb);
							append(sb, token, indent + 1);
							token.setIndent(indent + 1);
						} else {
							append(sb, token, indent);
						}
						isBetween = false;
					} else if (!this.rule.isNewLineBeforeAndOr()) {
						append(sb, token, indent);
						newLine(sb);
					} else {
						newLine(sb);
						append(sb, token, indent);
					}
				} else if (("WHEN".equals(upper)) || ("ELSE".equals(upper)) || ("INCREMENT BY".equals(upper)) || ("START WITH".equals(upper))
						|| ("MAXVALUE".equals(upper)) || ("NOMAXVALUE".equals(upper)) || ("MINVALUE".equals(upper)) || ("NOMINVALUE".equals(upper))
						|| ("CYCLE".equals(upper)) || ("NOCYCLE".equals(upper)) || ("CACHE".equals(upper))) {
					newLine(sb);
					append(sb, token, indent);
				} else if (("CREATE".equals(upper)) || ("INSERT".equals(upper)) || ("INTO".equals(upper)) || ("DROP".equals(upper))
						|| ("TRUNCATE".equals(upper)) || ("MERGE".equals(upper)) || ("ALTER".equals(upper)) || ("CREATE OR REPLACE".equals(upper))) {
					append(sb, token, indent);
					newLine(sb);
					indent++;
				} else if ("END".equals(upper)) {
					newLine(sb);
					indent--;
					append(sb, token, indent);
				} else if (("CASE".equals(upper)) || ("SEQUENCE".equals(upper)) || ((this.rule.isDecodeSpecialFormat()) && ("DECODE".equals(upper)))) {
					append(sb, token, indent);
					indent++;
				} else if ("BETWEEN".equals(upper)) {
					isBetween = true;
					append(sb, token, indent);
				} else if (("USING".equals(upper)) || ("ON".equals(upper))) {
					if ("CREATE".equals(sqlToken.getUpper())) {
						append(sb, token, indent);
					} else {
						newLine(sb);
						if (!isOnUsing) {
							indent++;
						}
						append(sb, token, indent);
						isOnUsing = true;
					}
				} else if (("JOIN".equals(upper)) || (upper.startsWith("FULL")) || (upper.startsWith("LEFT")) || (upper.startsWith("RIGHT"))) {
					if (isOnUsing) {
						indent -= 2;
						isOnUsing = false;
					}
					indent++;
					newLine(sb);
					append(sb, token, indent);
				} else if (("UNION".equals(upper)) || ("UNION ALL".equals(upper)) || ("INTERSECT".equals(upper)) || ("EXCEPT".equals(upper))
						|| ("MINUS".equals(upper))) {
					newLine(sb);
					indent = getUnionIndent(token, indent, selectList);
					append(sb, token, indent);
					newLine(sb);
				} else if (("WHEN MATCHED THEN".equals(upper)) || ("FOR UPDATE".equals(upper))) {
					isOnUsing = false;
					newLine(sb);
					indent--;
					append(sb, token, indent);
					indent++;
				} else if ("WHEN NOT MATCHED THEN".equals(upper)) {
					newLine(sb);
					indent -= 3;
					append(sb, token, indent);
					indent++;
				} else if ("TRIM".equals(upper)) {
					isTrim = true;
					append(sb, token, indent);
				} else if ("MODIFY".equals(upper)) {
					newLine(sb);
					indent++;
					append(sb, token, indent);
				} else if (("GRANT".equals(upper)) || ("REVOKE".equals(upper))) {
					append(sb, token, indent);
					newLine(sb);
					indent += 2;
				} else if ("PARTITION BY".equals(upper)) {
					newLine(sb);
					append(sb, token, indent);
					indent++;
					newLine(sb);
				} else {
					append(sb, token, indent);
				}

				break;
			case 20:
				if (",".equals(upper)) {
					if (isOnUsing) {
						indent -= 2;
						isOnUsing = false;
					}

					if (!checkNewLineInParen(token)) {
						append(sb, token, indent);
					} else if (this.rule.isNewLineBeforeComma()) {
						newLine(sb);
						append(sb, token, indent);
					} else {
						append(sb, token, indent);
						newLine(sb);
					}
				} else if ("(".equals(upper)) {
					append(sb, token, indent);

					if (checkNewLineInParen(token)) {
						newLine(sb);
						indent++;
					}

					parenStack.push(new Integer(indent));
				} else if (")".equals(upper)) {
					try {
						indent = ((Integer) parenStack.pop()).intValue();
					} catch (RuntimeException e) {
						ExceptionHandler.handleException("')'に対する'('がありません。", token);
					}

					if (checkNewLineInParen(token)) {
						newLine(sb);
						indent--;
					}

					append(sb, token, indent);
				} else {
					append(sb, token, indent);
				}
				break;
			case 30:
			case 40:
			case 50:
			case 60:
				append(sb, token, indent);
				break;
			case 80:
				indent = initIndent;
				selectList.clear();
				sqlToken = null;
				append(sb, token, indent);
				break;
			case 90:
				if (sb.length() != 0)
					newLine(sb);
				append(sb, token, indent);
				break;
			case -1:
				append(sb, token, indent);
			case 70:
			}

		}

		return sb.toString().trim();
	}

	private StringBuilder append(StringBuilder sb, Token token, int indent) {
		int type = token.getType();

		if ((type == 60) && (this.rule.isRemoveComment()))
			return sb;
		if ((type == 90) && (this.rule.isRemoveEmptyLine())) {
			return sb;
		}
		String upper = token.getUpper();
		Token parentToken = token.getParentTokenInParen();
		char c = sb.length() == 0 ? 65535 : sb.charAt(sb.length() - 1);
		boolean isHeadLine = false;

		if ((c == 65535) || (c == this.rule.getOutNewLineEnd())) {
			isHeadLine = true;

			if ((type != 90) || (this.rule.isIndentEmptyLine())) {
				indent(sb, indent);
			}

		} else if (c == ',') {
			if ((this.rule.isInSpecialFormat()) && (parentToken != null) && ("IN".equals(parentToken.getUpper())) && (token.isValueOnlyInParen())) {
				sb.append(' ');
			} else if ((this.rule.isDecodeSpecialFormat()) && (parentToken != null) && ("DECODE".equals(parentToken.getUpper()))) {
				int elementIndexInParen = token.getElementIndexInParen();
				if (elementIndexInParen % 2 != 0) {
					sb.append(' ');
				}
			} else if ((!this.rule.isNewLineFunctionParen()) && (parentToken != null) && (parentToken.getSubType() == 12)) {
				sb.append(' ');
			} else if ((parentToken != null) && (parentToken.getSubType() == 11) && (!this.rule.isNewLineDataTypeParen())) {
				sb.append(' ');
			}

		} else if ("(".equals(upper)) {
			if (c != '(') {
				if (TokenUtil.isOperatorChar(c)) {
					sb.append(' ');
				} else if ((parentToken == null)
						|| ((parentToken.getSubType() != 11) && (parentToken.getSubType() != 12) && (parentToken.getType() != 40))) {
					sb.append(' ');
				}
			}
		} else if ((!")".equals(upper)) && (c != '(') && (!"(*)".equals(upper)) && (!"(+)".equals(upper)) && (!",".equals(upper))) {
			if ((type != 80) && (token.getSubType() != 62)) {
				sb.append(' ');
			}
		}

		String fixString = convertString(token, indent, isHeadLine);

		if ((this.rule.isWordBreak()) && (type != 60)) {
			int lineLength = getLineLength(sb) + fixString.length();
			if (this.rule.getWidth() < lineLength) {
				if (this.rule.getWidth() >= this.rule.getIndentString().length() * (indent + 1) + fixString.length()) {
					sb.deleteCharAt(sb.length() - 1);
					newLine(sb);
					indent(sb, indent + 1);
					token.setIndent(indent + 1);
				}
			}
		}

		sb.append(fixString);

		return sb;
	}

	private StringBuilder newLine(StringBuilder sb) {
		int start = sb.length() - this.rule.getOutNewLineCodeStr().length();
		if (start < 0) {
			return sb.append(this.rule.getOutNewLineCodeStr());
		}
		if (sb.indexOf(this.rule.getOutNewLineCodeStr(), start) != -1)
			return sb;
		return sb.append(this.rule.getOutNewLineCodeStr());
	}

	private StringBuilder indent(StringBuilder sb, int indent) {
		if (indent <= 0) {
			return sb;
		}
		do
			sb.append(this.rule.getIndentString());
		while (indent-- > 0);

		return sb;
	}

	private String convertString(Token token, int indent, boolean isHeadLine) {
		String str = "";
		int type = token.getType();

		switch (type) {
		case 10:
		case 40:
			int convertType = type == 10 ? this.rule.getConvertKeyword() : this.rule.getConvertName();

			switch (convertType) {
			case 0:
				str = token.getCustom();
				break;
			case 1:
				str = token.getUpper();
				break;
			case 2:
				str = token.getCustom().toLowerCase();
				break;
			case 3:
				if ('"' == token.getCustom().charAt(0))
					str = "\"" + StringUtils.capitalize(token.getCustom().substring(1));
				else
					str = StringUtils.capitalize(token.getCustom());
			}

			break;
		case 60:
			if (token.getSubType() == 61)
				str = token.getCustom() + this.rule.getOutNewLineCodeStr();
			else {
				str = formatMultiComment(token.getCustom(), indent, isHeadLine);
			}
			break;
		case 90:
			str = this.rule.getOutNewLineCodeStr();
			break;
		case 80:
			switch (this.rule.getOutSqlSeparator()) {
			case 0:
				str = token.getCustom();
				break;
			case 1:
				str = token.getCustom().replace(';', this.rule.getOutSqlSeparatorChar());
				break;
			case 2:
				str = token.getCustom().replace('/', this.rule.getOutSqlSeparatorChar());
			}

			break;
		default:
			str = token.getCustom();
		}

		return str;
	}

	private int getLineLength(StringBuilder sb) {
		if (sb == null) {
			return 0;
		}
		int start = sb.lastIndexOf(this.rule.getOutNewLineCodeStr());
		if (start == -1) {
			return 0;
		}
		return sb.length() - start - this.rule.getOutNewLineCodeStr().length();
	}

	private boolean checkNewLineInParen(Token token) {
		Token parentToken = token.getParentTokenInParen();
		if (parentToken != null) {
			String parentUpper = parentToken.getUpper();

			if ("DEFAULT".equals(parentUpper)) {
				return false;
			}

			if ((this.rule.isDecodeSpecialFormat()) && ("DECODE".equals(parentUpper))) {
				if (")".equals(token.getCustom())) {
					return true;
				}
				int elementIndexInParen = token.getElementIndexInParen();
				return (elementIndexInParen & 1) == 1;
			}

			if ((!this.rule.isNewLineFunctionParen()) && (parentToken.getSubType() == 12)) {
				return false;
			}

			if ((!this.rule.isNewLineDataTypeParen()) && (parentToken.getSubType() == 11)) {
				return false;
			}

			if ((this.rule.isInSpecialFormat()) && ("IN".equals(parentUpper)) && (token.isValueOnlyInParen())) {
				return false;
			}

		}

		return (token.getElementLengthInParen() != 1) || (!token.isValueOnlyInParen());
	}

	private String formatMultiComment(String str, int indent, boolean isHeadLine) {
		StringBuilder sb = new StringBuilder();
		if (!isHeadLine) {
			newLine(sb);
			indent(sb, indent);
		}

		String[] strs = str.split(TokenUtil.NEW_LINES_REGEX);
		for (int i = 0; i < strs.length; i++) {
			if (i != 0) {
				indent(sb, indent);
			}
			sb.append(StringUtils.leftTrim(strs[i], TokenUtil.WORD_SEPARATE));
			newLine(sb);
		}
		return sb.toString();
	}

	private int getUnionIndent(Token token, int indent, List<Token> selectList) {
		int depthParen = token.getDepthParen();

		if (selectList.size() == 0) {
			return indent;
		}
		for (int i = selectList.size() - 1; i >= 0; i--) {
			Token selectToken = (Token) selectList.get(i);
			if (selectToken.getDepthParen() == depthParen) {
				return selectToken.getIndent();
			}
		}
		return indent;
	}

	public SqlTokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(SqlTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
}
