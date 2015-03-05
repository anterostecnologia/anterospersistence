package br.com.anteros.persistence.dsl.osql.types.path;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.PathMetadata;
import br.com.anteros.persistence.dsl.osql.types.Visitor;

public class DiscriminatorColumnPath implements Path<String> {

	private static final long serialVersionUID = 1L;
	private Class<?> discriminatorClass;
	private Path<?> root;

	public Class<?> getDiscriminatorClass() {
		return discriminatorClass;
	}

	public DiscriminatorColumnPath(Path<?> root, Class<?> discriminatorClass) {
		this.discriminatorClass = discriminatorClass;
		this.root = root;
	}

	@Override
	public <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(this, context);
	}

	@Override
	public Class<? extends String> getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathMetadata<?> getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path<?> getRoot() {
		return root;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		// TODO Auto-generated method stub
		return null;
	}

}
