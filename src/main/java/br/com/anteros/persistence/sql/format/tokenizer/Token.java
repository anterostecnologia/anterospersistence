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

public class Token
{
  private String original;
  private String upper;
  private String custom;
  private int type;
  private int subType;
  private int x;
  private int y;
  private int index;
  private int elementLengthInParen;
  private int elementIndexInParen;
  private boolean valueOnlyInParen;
  private Token parentTokenInParen;
  private int depthParen;
  private int indent;

  public Token(String original, int x, int y, int index)
  {
    this.original = original;
    this.upper = "";
    this.custom = "";
    this.x = x;
    this.y = y;
    this.index = index;
    this.elementLengthInParen = 0;
    this.elementIndexInParen = 0;
    this.valueOnlyInParen = false;
    this.parentTokenInParen = null;
    this.depthParen = 0;
    this.indent = 0;
  }

  public Token(Token token)
  {
    this.original = token.getOriginal();
    this.upper = token.getUpper();
    this.custom = token.getCustom();
    this.x = token.getX();
    this.y = token.getY();
    this.index = token.getIndex();
    this.elementLengthInParen = token.getElementLengthInParen();
    this.elementIndexInParen = token.getElementIndexInParen();
    this.valueOnlyInParen = token.isValueOnlyInParen();
    this.parentTokenInParen = token.getParentTokenInParen();
    this.depthParen = token.getDepthParen();
    this.indent = token.getIndent();
  }

  public String getOriginal()
  {
    return this.original;
  }

  public void setOriginal(String original)
  {
    this.original = original;
  }

  public String getUpper()
  {
    return this.upper;
  }

  public void setUpper(String upper)
  {
    this.upper = upper.toUpperCase();
  }

  public String getCustom()
  {
    return this.custom;
  }

  public void setCustom(String custom)
  {
    this.custom = custom;
    setUpper(custom);
  }

  public int getType()
  {
    return this.type;
  }

  public void setType(int type)
  {
    this.type = type;
  }

  public int getIndex()
  {
    return this.index;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public int getX()
  {
    return this.x;
  }

  public void setX(int x)
  {
    this.x = x;
  }

  public int getY()
  {
    return this.y;
  }

  public void setY(int y)
  {
    this.y = y;
  }

  public int getSubType()
  {
    return this.subType;
  }

  public void setSubType(int subType)
  {
    this.subType = subType;
  }

  public int getElementLengthInParen()
  {
    return this.elementLengthInParen;
  }

  public void setElementLengthInParen(int elementLengthInParen)
  {
    this.elementLengthInParen = elementLengthInParen;
  }

  public boolean isValueOnlyInParen()
  {
    return this.valueOnlyInParen;
  }

  public void setValueOnlyInParen(boolean valueOnlyInParen)
  {
    this.valueOnlyInParen = valueOnlyInParen;
  }

  public int getOriginalLength() {
    return this.original.length();
  }

  public int getCustomLength() {
    return this.custom.length();
  }

  public Token getParentTokenInParen()
  {
    return this.parentTokenInParen;
  }

  public void setParentTokenInParen(Token parentTokenInParen)
  {
    this.parentTokenInParen = parentTokenInParen;
  }

  public int getElementIndexInParen()
  {
    return this.elementIndexInParen;
  }

  public void setElementIndexInParen(int elementIndexInParen)
  {
    this.elementIndexInParen = elementIndexInParen;
  }

  public int getDepthParen()
  {
    return this.depthParen;
  }

  public void setDepthParen(int depthParen)
  {
    this.depthParen = depthParen;
  }

  public int getIndent()
  {
    return this.indent;
  }

  public void setIndent(int indent)
  {
    this.indent = indent;
  }

  public String toString()
  {
	  StringBuilder buffer = new StringBuilder();
    buffer.append("[Token:");
    buffer.append(" original: ");
    buffer.append(TokenUtil.debugString(this.original));
    buffer.append(" upper: ");
    buffer.append(TokenUtil.debugString(this.upper));
    buffer.append(" custom: ");
    buffer.append(TokenUtil.debugString(this.custom));
    buffer.append(" type: ");
    buffer.append(TokenUtil.debugTypeString(this.type));
    buffer.append(" subType: ");
    buffer.append(TokenUtil.debugSubTypeString(this.subType));
    buffer.append(" x: ");
    buffer.append(this.x);
    buffer.append(" y: ");
    buffer.append(this.y);
    buffer.append(" index: ");
    buffer.append(this.index);
    buffer.append(" elementLengthInParen: ");
    buffer.append(this.elementLengthInParen);
    buffer.append(" elementIndexInParen: ");
    buffer.append(this.elementIndexInParen);
    buffer.append(" valueOnlyInParen: ");
    buffer.append(this.valueOnlyInParen);
    buffer.append(" parentTokenInParen: ");
    buffer.append("[" + (
      this.parentTokenInParen != null ? 
      this.parentTokenInParen.getCustom() : "null") + "]");
    buffer.append(" depthParen: ");
    buffer.append(this.depthParen);
    buffer.append(" indent: ");
    buffer.append(this.indent);
    buffer.append("]");
    return buffer.toString();
  }
}
