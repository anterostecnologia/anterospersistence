package br.com.anteros.persistence.metadata.accessor;

public interface PropertyAccessor<S, V> {

	public void set(S source, V value);
	   public V get(S source);
}
