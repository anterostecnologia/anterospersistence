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
package br.com.anteros.persistence.dsl.osql.types.path;

import java.lang.reflect.AnnotatedElement;

import br.com.anteros.persistence.dsl.osql.types.Path;
import br.com.anteros.persistence.dsl.osql.types.PathImpl;
import br.com.anteros.persistence.dsl.osql.types.PathMetadata;
import br.com.anteros.persistence.dsl.osql.types.PathMetadataFactory;
import br.com.anteros.persistence.dsl.osql.types.Visitor;

/**
 * Um caminho para representar o discriminator column da entidade.
 * 
 * @author edson
 *
 */
public class DiscriminatorColumnPath implements Path<String> {

	private static final long serialVersionUID = 1L;
	private Class<?> discriminatorClass;
	private Path<?> root;
	private final PathMetadata<String> metadata;

	public Class<?> getDiscriminatorClass() {
		return discriminatorClass;
	}

	public DiscriminatorColumnPath(Path<?> root, Class<?> discriminatorClass) {
		this.discriminatorClass = discriminatorClass;
		this.root = root;
		this.metadata = PathMetadataFactory.forVariable(discriminatorClass.getName());
	}

	@Override
	public <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(this, context);
	}

	@Override
	public Class<? extends String> getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PathMetadata<?> getMetadata() {
		return metadata;
	}

	@Override
	public Path<?> getRoot() {
		return root;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		throw new UnsupportedOperationException();
	}

}
