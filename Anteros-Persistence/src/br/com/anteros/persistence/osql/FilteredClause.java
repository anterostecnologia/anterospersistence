package br.com.anteros.persistence.osql;

import br.com.anteros.persistence.osql.condition.Predicate;

public interface FilteredClause<C extends FilteredClause<C>> {
   
    C where(Predicate... o);

}
