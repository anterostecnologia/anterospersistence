package br.com.anteros.persistence.osql.condition;

import java.util.List;

public interface CodeTemplateCondition<T> extends Condition<T> {

	Object getArgument(int index);

	List<?> getArguments();

	CodeTemplate getTemplate();

}
