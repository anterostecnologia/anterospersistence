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

import java.util.LinkedList;

public class TokenList extends LinkedList<Token>
{
  private static final long serialVersionUID = -7668336470690425446L;

  public Token getToken(int index)
  {
    if ((index < 0) || (size() - 1 < index))
      return null;
    return (Token)super.get(index);
  }

  public Token getFirstToken() {
    return (Token)super.getFirst();
  }

  public Token getLastToken() {
    return (Token)super.getLast();
  }

  public int getNextValidTokenIndex(int start, int index)
  {
    start++;
    if ((start < 0) || (size() - 1 < start) || (index <= 0)) {
      return -1;
    }
    int count = 0;
    int len = size();
    for (int i = start; i < len; i++) {
      Token token = getToken(i);
      switch (token.getType()) {
      case 70:
        break;
      default:
        count++;
        if (index > count) continue;
        return i;
      }
    }

    return -1;
  }

  public void removeToken(int start, int end)
  {
    if ((start < 0) || (size() - 1 < start) || (end < 0) || 
      (size() - 1 < end)) {
      return;
    }
    if (start > end) {
      return;
    }
    for (int i = end; i >= start; i--)
      remove(i);
  }

  public Token getParentTokenInParen(int index)
  {
    if (index - 1 <= 0) {
      return null;
    }
    for (int i = index - 1; i >= 0; i--) {
      Token token = (Token)super.get(i);

      switch (token.getType())
      {
      case 10:
        return token;
      case 40:
        return token;
      case 60:
      case 70:
      case 90:
        break;
      default:
        return null;
      }
    }

    return null;
  }

  public String toString()
  {
	  StringBuilder buffer = new StringBuilder();
    buffer.append("[TokenList:");

    buffer.append(" modCount: ");
    buffer.append(this.modCount);
    buffer.append("]");
    return buffer.toString();
  }
}
