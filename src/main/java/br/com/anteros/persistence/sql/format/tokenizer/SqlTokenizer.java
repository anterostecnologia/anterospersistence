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

import java.util.Iterator;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.sql.format.SqlFormatRule;

public class SqlTokenizer implements Iterator<Token> {
	protected SqlScanner scanner;
	protected SqlFormatRule rule;
	private TokenList tokenList;
	protected Iterator<Token> it;
	private boolean pushedBack = false;
	private Token token;

	public SqlTokenizer(String sql, SqlFormatRule rule) {
		init(sql, rule);
	}

	public SqlTokenizer() {
	}

	protected void init(String sql, SqlFormatRule rule) {
		this.scanner = new SqlScanner(sql);
		this.rule = rule;
		this.setTokenList(new TokenList());

		parse();

		optimize();
		

		this.it = this.getTokenList().iterator();
	}

	public boolean hasNext() {
		if (this.pushedBack) {
			return true;
		}
		return this.it.hasNext();
	}

	public Token next() {
		if (this.pushedBack) {
			this.pushedBack = false;
			return this.token;
		}
		if (this.it.hasNext()) {
			this.token = this.it.next();
			return this.token;
		}
		return null;
	}

	public void pushBack() {
		if (this.token != null)
			this.pushedBack = true;
	}

	protected boolean isCanceled() {
		return false;
	}

	protected void parse() {
		Token token = new Token("", 0, 0, 0);
		token.setType(0);
		this.getTokenList().add(token);
		int x = 0;
		int y = 0;
		int beforeType = -1;
		int depthParen = 0;

		while (this.scanner.hasNext()) {
			if (isCanceled())
				return;

			int skipCount = this.scanner.skipSpaceTab();
			if (!this.scanner.hasNext()) {
				return;
			}
			x += skipCount;
			int incY = 0;

			StringBuilder sb = new StringBuilder();

			int index = this.scanner.getCurrent();
			int type = -1;
			int subType = 0;

			char c = this.scanner.peek();
			if (c == '"') {
				do {
					sb.append(this.scanner.next());
					c = this.scanner.peek();
				} while ((c != '"') && (this.scanner.hasNext()));

				if (this.scanner.hasNext())
					sb.append(this.scanner.next());
				type = 40;
			} else if (c == '\'') {
				sb.append(this.scanner.next());
				c = this.scanner.peek();
				do {
					if (this.scanner.isPeekEquals("''")) {
						sb.append(this.scanner.next());
						sb.append(this.scanner.next());
						c = this.scanner.peek();
					} else {
						if ((c == '\'') && (this.scanner.peek(1) != '\'')) {
							break;
						}
						sb.append(this.scanner.next());
						c = this.scanner.peek();
					}
				} while (((c != '\'') && (this.scanner.hasNext())) || (this.scanner.isPeekEquals("''")));

				if (this.scanner.hasNext())
					sb.append(this.scanner.next());
				type = 50;
				subType = 51;
			} else if (this.scanner.isPeekEquals("--")) {
				sb.append(this.scanner.next());
				c = this.scanner.peek();
				do {
					sb.append(this.scanner.next());
					c = this.scanner.peek();
				} while ((this.scanner.hasNext()) && (!this.scanner.isPeekEquals(TokenUtil.NEW_LINES)));

				type = 60;
				subType = 61;
			} else if (this.scanner.isPeekEquals("/*")) {
				sb.append(this.scanner.next());
				c = this.scanner.peek();
				do {
					sb.append(this.scanner.next());
					c = this.scanner.peek();

					if (this.scanner.isPeekEquals(TokenUtil.NEW_LINES)) {
						if (this.scanner.isPeekEquals("\r\n")) {
							sb.append(this.scanner.next());
						}
						incY++;
					}
				} while ((!this.scanner.isPeekEquals("*/")) && (this.scanner.hasNext()));
				if (this.scanner.hasNext()) {
					sb.append(this.scanner.next());
					sb.append(this.scanner.next());
				}
				type = 60;
				subType = 62;
			} else if (this.scanner.isPeekNextEqualsEx("(*)")) {
				sb.append("(*)");
				type = 20;
			} else if (this.scanner.isPeekNextEqualsEx("(+)")) {
				sb.append("(+)");
				type = 20;
			} else if (this.scanner.isPeekEquals(TokenUtil.NEW_LINES)) {
				if ((beforeType == 70) || (beforeType == 90))
					type = 90;
				else {
					type = 70;
				}
				index = this.scanner.getCurrent() - skipCount;

				if (this.scanner.isPeekEquals("\r\n")) {
					sb.append(this.scanner.next());
				}
				sb.append(this.scanner.next());
				incY++;
			} else if ((Character.isDigit(c))
					|| (((c == '.') || (c == '+') || (c == '-')) && (Character.isDigit(this.scanner.peek(1))))) {
				do {
					sb.append(c);
					this.scanner.next();
					c = this.scanner.peek();
				} while (TokenUtil.isNumberChar(c));
				type = 50;
				subType = 52;
			} else if (TokenUtil.isOperatorChar(c)) {
				String str = this.scanner.getPeekNextEqualsExString(TokenUtil.OPERATOR);

				if (str != null) {
					sb.append(str);
					if ("(".equals(str)) {
						depthParen++;
					}
				} else {
					sb.append(this.scanner.next());
				}
				type = 30;
			} else if ((TokenUtil.isBindVariable(c)) && (!this.scanner.isPeekEqualsEx("::"))) {
				sb.append(this.scanner.next());
				type = 50;
				subType = 53;
			} else if (TokenUtil.isSymbolChar(c)) {
				String str = this.scanner.getPeekNextEqualsExString(TokenUtil.SYMBOL);

				if (str != null) {
					sb.append(str);
					if ("(".equals(str)) {
						depthParen++;
					}
				} else {
					sb.append(this.scanner.next());
				}

				type = 20;
			} else if (TokenUtil.isNameChar(c)) {
				do {
					sb.append(c);
					this.scanner.next();
					c = this.scanner.peek();
				} while ((TokenUtil.isNameChar(c)) /*&& (c != '￿')*/);

				String upper = sb.toString().toUpperCase();
				
				if (this.rule.isKeyword(upper)) {
					if (TokenUtil.isSpecialValue(upper))
						type = TokenUtil.TYPE_VALUE;
					else {
						type = TokenUtil.TYPE_KEYWORD;
					}
				} else {
					type = TokenUtil.TYPE_NAME;
				}
			} else {
				sb.append(this.scanner.next());
			}

			String original = this.scanner.substring(index);
			token = new Token(original, x, y, index);

			switch (type) {
			case 10:
				token.setCustom(sb.toString());

				if (this.rule.isDataTypes(token.getUpper())) {
					subType = 11;
				} else {
					if (!this.rule.isFunctions(token.getUpper()))
						break;
					subType = 12;
				}
				break;
			case 70:
			case 90:
				token.setCustom(StringUtils.leftTrim(token.getOriginal(), TokenUtil.WORD_SEPARATE));
				x = 0;
				break;
			default:
				token.setCustom(sb.toString());
			}

			token.setType(type);
			token.setSubType(subType);
			token.setDepthParen(depthParen);

			this.getTokenList().add(token);

			beforeType = type;

			x += original.length();
			y += incY;

			if (")".equals(sb.toString())) {
				depthParen--;
			}

		}

		token = new Token("", x, y, this.scanner.getLength());
		token.setType(100);
		this.getTokenList().add(token);
	}

	protected void optimize() {
		for (int i = 0; i < this.getTokenList().size(); i++) {
			if (isCanceled())
				return;

			Token current = this.getTokenList().getToken(i);
			int next1Index = this.getTokenList().getNextValidTokenIndex(i, 1);
			int next2Index = this.getTokenList().getNextValidTokenIndex(i, 2);
			Token next1 = this.getTokenList().getToken(next1Index);
			Token next2 = this.getTokenList().getToken(next2Index);

			if ((current == null) || (next1 == null)) {
				continue;
			}
			int currentType = current.getType();
			String currentUpper = current.getUpper();
			int next1Type = next1.getType();
			String next1Upper = next1.getUpper();
			int next2Type = next2 == null ? -1 : next2.getType();
			String next2Upper = next2 == null ? null : next2.getUpper();

			if ((next2 != null) && ((currentType == 10) || (currentType == 40) || (currentType == 50))
					&& (".".equals(next1Upper))
					&& ((currentType == 10) || (next2Type == 40) || (next2Type == 50) || ("*".equals(next2Upper)))) {
				current.setType(40);
				current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
						next2.getIndex() + next2.getOriginalLength()));
				current.setCustom(current.getCustom() + "." + next2.getCustom());
				this.getTokenList().removeToken(i + 1, next2Index);
				i--;
			} else if ((".".equals(currentUpper)) && (next1.getSubType() == 52)) {
				current.setType(50);
				current.setSubType(52);
				current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
						next1.getIndex() + next1.getOriginalLength()));
				current.setCustom("." + next1.getCustom());
				this.getTokenList().removeToken(i + 1, next1Index);
			} else if ((("N".equals(currentUpper)) || ("Q".equals(currentUpper)) || ("NQ".equals(currentUpper)))
					&& (next1.getSubType() == 51)) {
				current.setType(50);
				current.setSubType(51);
				current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
						next1.getIndex() + next1.getOriginalLength()));
				current.setCustom(current.getCustom() + next1.getCustom());
				this.getTokenList().removeToken(i + 1, next1Index);
				i -= 2;
			} else if (((currentType == 10) || (currentType == 40) || (currentType == 50)) && (".".equals(next1Upper))) {
				current.setType(40);
				current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
						next1.getIndex() + next1.getOriginalLength()));
				current.setCustom(current.getCustom() + ".");
				this.getTokenList().removeToken(i + 1, next1Index);
			} else if ((current.getSubType() == 53) && (":".equals(currentUpper)) && (next1Type == 40)) {
				current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
						next1.getIndex() + next1.getOriginalLength()));
				current.setCustom(":" + next1.getCustom());
				this.getTokenList().removeToken(i + 1, next1Index);
			} else {
				setSqlSeparator(current, i);
			}

		}

		for (int i = 0; i < this.getTokenList().size() - 1; i++) {
			if (isCanceled())
				return;

			Token current = this.getTokenList().getToken(i);
			int next1Index = this.getTokenList().getNextValidTokenIndex(i, 1);
			Token next1 = this.getTokenList().getToken(next1Index);

			if (current.getType() != 10) {
				continue;
			}
			if ((next1 == null) || (next1.getType() != 10)) {
				continue;
			}
			int next2Index = this.getTokenList().getNextValidTokenIndex(i, 2);
			int next3Index = this.getTokenList().getNextValidTokenIndex(i, 3);
			Token next2 = this.getTokenList().getToken(next2Index);
			Token next3 = this.getTokenList().getToken(next3Index);

			if ((next3 != null) && (next3.getType() == 10) && (next2 != null) && (next2.getType() == 10)) {
				StringBuilder sb4 = new StringBuilder();
				sb4.append(current.getCustom()).append(' ');
				sb4.append(next1.getCustom()).append(' ');
				sb4.append(next2.getCustom()).append(' ');
				sb4.append(next3.getCustom());

				if (TokenUtil.isMultiKeyword(sb4.toString().toUpperCase())) {
					current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
							next3.getIndex() + next3.getOriginalLength()));
					current.setCustom(sb4.toString());
					this.getTokenList().set(i, current);
					this.getTokenList().removeToken(i + 1, next3Index);
					continue;
				}
			}

			if ((next2 != null) && (next2.getType() == 10)) {
				StringBuilder sb3 = new StringBuilder();
				sb3.append(current.getCustom()).append(' ');
				sb3.append(next1.getCustom()).append(' ');
				sb3.append(next2.getCustom());

				if (TokenUtil.isMultiKeyword(sb3.toString().toUpperCase())) {
					current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
							next2.getIndex() + next2.getOriginalLength()));
					current.setCustom(sb3.toString());
					this.getTokenList().set(i, current);
					this.getTokenList().removeToken(i + 1, next2Index);
					continue;
				}

			}

			StringBuilder sb2 = new StringBuilder();
			sb2.append(current.getCustom()).append(' ');
			sb2.append(next1.getCustom());

			if (TokenUtil.isMultiKeyword(sb2.toString().toUpperCase())) {
				current.setOriginal(this.scanner.getSql().substring(current.getIndex(),
						next1.getIndex() + next1.getOriginalLength()));
				current.setCustom(sb2.toString());
				this.getTokenList().set(i, current);
				this.getTokenList().removeToken(i + 1, next1Index);
			}

		}

		int size = this.getTokenList().size();
		for (int i = 0; i < size; i++) {
			if (isCanceled())
				return;

			Token token = this.getTokenList().getToken(i);
			if (token.getType() != 20) {
				continue;
			}
			if (!"(".equals(token.getUpper()))
				continue;
			setInParenInfo(i);
		}
	}

	private void setSqlSeparator(Token token, int index) {
		String upper = token.getUpper();
		if ((upper.length() != 1) || (!TokenUtil.isSqlSeparate(upper.charAt(0)))) {
			return;
		}
		int len = this.getTokenList().size();
		for (int i = index; i < len; i++) {
			if (isCanceled())
				return;

			Token current = this.getTokenList().getToken(i);
			int type = current.getType();
			upper = current.getUpper();

			switch (type) {
			case TokenUtil.TYPE_NAME:
			case TokenUtil.TYPE_VALUE:
			case TokenUtil.TYPE_SQL_SEPARATE:
				return;
			case TokenUtil.TYPE_SYMBOL:
				if ("(".equals(upper)) {
					continue;
				}
				if (TokenUtil.isSqlSeparate(upper.charAt(0)))
					break;
				return;
			case TokenUtil.TYPE_OPERATOR:
				if (TokenUtil.isSqlSeparate(upper.charAt(0)))
					continue;
				return;
			case TokenUtil.TYPE_KEYWORD:
				if (TokenUtil.isBeginSqlKeyword(upper))
					break;
				return;
			case TokenUtil.TYPE_END_SQL:
				break;
			}

		}

		token.setType(TokenUtil.TYPE_SQL_SEPARATE);

		Token beforeToken = this.getTokenList().getToken(index - 1);
		if (beforeToken != null) {
			int beforeType = beforeToken.getType();

			switch (beforeType) {
			case TokenUtil.TYPE_NEW_LINE:
				token.setOriginal(beforeToken.getOriginal() + token.getOriginal());
				token.setCustom(this.rule.getOutNewLineCodeStr() + token.getCustom());
				token.setIndex(beforeToken.getIndex());
				token.setX(beforeToken.getX());
				token.setY(beforeToken.getY());
				this.getTokenList().remove(index - 1);
				index--;
				break;
			case 0:
			case TokenUtil.TYPE_SQL_SEPARATE:
				token.setCustom(token.getCustom());
				break;
			default:
				token.setCustom(this.rule.getOutNewLineCodeStr() + token.getCustom());
			}

		}

		Token nextToken = this.getTokenList().getToken(index + 1);
		if (nextToken != null) {
			int nextType = nextToken.getType();
			switch (nextType) {
			case TokenUtil.TYPE_NEW_LINE:
				token.setOriginal(token.getOriginal() + nextToken.getOriginal());
				token.setCustom(token.getCustom() + this.rule.getOutNewLineCodeStr());
				this.getTokenList().remove(index + 1);
			case TokenUtil.TYPE_END_SQL:
				break;
			default:
				token.setOriginal(token.getOriginal());
				token.setCustom(token.getCustom() + this.rule.getOutNewLineCodeStr());
			}
		}
	}

	private void setInParenInfo(int startPos) {
		int deep = 1;
		int elementLength = 0;
		boolean valueOnly = true;
		int size = this.getTokenList().size();
		Token parentTokenInParen = this.getTokenList().getParentTokenInParen(startPos);

		Token nextToken = this.getTokenList().getToken(startPos - 1);
		if ((nextToken != null) && (!")".equals(nextToken.getUpper()))) {
			elementLength++;
		}

		for (int i = startPos + 1; i < size; i++) {
			if (isCanceled())
				return;

			Token current = this.getTokenList().getToken(i);
			int type = current.getType();
			String upper = current.getUpper();

			if ("(".equals(upper)) {
				deep++;
				current.setElementIndexInParen(0);
			} else {
				if (")".equals(upper)) {
					deep--;
					current.setElementIndexInParen(0);

					if (deep != 0)
						continue;
					for (int j = startPos; j <= i; j++) {
						current = this.getTokenList().getToken(j);
						current.setElementLengthInParen(elementLength);
						current.setValueOnlyInParen(valueOnly);
						current.setParentTokenInParen(parentTokenInParen);
					}
					return;
				}

				current.setElementIndexInParen(elementLength);

				switch (type) {
				case 10:
					valueOnly = false;
					break;
				case 30:
				case 40:
				case 50:
					break;
				case 20:
					if ((deep == 1) && (",".equals(upper))) {
						current.setElementIndexInParen(elementLength);
						elementLength++;
					} else {
						if (!"(".equals(upper))
							continue;
						valueOnly = false;
					}
				}
			}
		}
	}

	public void remove() {
	}

	public TokenList getTokenList() {
		return tokenList;
	}

	public void setTokenList(TokenList tokenList) {
		this.tokenList = tokenList;
	}
}
