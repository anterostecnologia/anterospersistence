package br.com.anteros.persistence.dsl.osql;

import br.com.anteros.persistence.dsl.osql.types.EntityPath;

public interface EntityPathResolver {

	<T> EntityPath<T> createPath(Class<T> domainClass);
}