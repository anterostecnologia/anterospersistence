package br.com.anteros.persistence.osql.attribute;

import java.util.List;

import br.com.anteros.persistence.osql.Visitor;
import br.com.anteros.persistence.osql.condition.Condition;
import br.com.anteros.persistence.osql.condition.ConstantCondition;
import br.com.anteros.persistence.osql.condition.FactoryCondition;
import br.com.anteros.persistence.osql.condition.ParameterCondition;
import br.com.anteros.persistence.osql.condition.SubQueryCondition;
import br.com.anteros.persistence.osql.condition.CodeTemplateCondition;
import br.com.anteros.persistence.osql.operation.Operation;

public final class AttributeExtractor implements Visitor<Attribute<?>,Void> {
    
  public static final AttributeExtractor DEFAULT = new AttributeExtractor();
  
  private AttributeExtractor() {}

  @Override
  public Attribute<?> visit(ConstantCondition<?> expr, Void context) {
      return null;
  }

  @Override
  public Attribute<?> visit(FactoryCondition<?> expr, Void context) {
      return visit(expr.getArguments());
  }

  @Override
  public Attribute<?> visit(Operation<?> expr, Void context) {
      return visit(expr.getArguments());
  }

  @Override
  public Attribute<?> visit(ParameterCondition<?> expr, Void context) {
      return null;
  }

  @Override
  public Attribute<?> visit(Attribute<?> expr, Void context) {
      return expr;
  }

  @Override
  public Attribute<?> visit(SubQueryCondition<?> expr, Void context) {
      return null;
  }

  @Override
  public Attribute<?> visit(CodeTemplateCondition<?> expr, Void context) {
      return visit(expr.getArguments());
  }

  private Attribute<?> visit(List<?> exprs) {
      for (Object e : exprs) {
          if (e instanceof Condition) {
        	  Attribute<?> path = ((Condition<?>)e).accept(this, null);
              if (path != null) {
                  return path;
              }    
          }            
      }
      return null;
  }

}
