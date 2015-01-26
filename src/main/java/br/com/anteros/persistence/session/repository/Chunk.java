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
package br.com.anteros.persistence.session.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import br.com.anteros.core.utils.Assert;

public abstract class Chunk<T> implements Slice<T>, Serializable {

	private static final long serialVersionUID = 867755909294344406L;

	private final List<T> content = new ArrayList<T>();
	private final Pageable pageable;

	public Chunk(List<T> content, Pageable pageable) {

		Assert.notNull(content, "Content must not be null!");

		this.content.addAll(content);
		this.pageable = pageable;
	}

	public int getNumber() {
		return pageable == null ? 0 : pageable.getPageNumber();
	}

	public int getSize() {
		return pageable == null ? 0 : pageable.getPageSize();
	}

	public int getNumberOfElements() {
		return content.size();
	}

	public boolean hasPrevious() {
		return getNumber() > 0;
	}

	public boolean isFirst() {
		return !hasPrevious();
	}

	public boolean isLast() {
		return !hasNext();
	}

	public Pageable nextPageable() {
		return hasNext() ? pageable.next() : null;
	}

	public Pageable previousPageable() {

		if (hasPrevious()) {
			return pageable.previousOrFirst();
		}

		return null;
	}

	public boolean hasContent() {
		return !content.isEmpty();
	}

	public List<T> getContent() {
		return Collections.unmodifiableList(content);
	}

	public Iterator<T> iterator() {
		return content.iterator();
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Chunk<?>)) {
			return false;
		}

		Chunk<?> that = (Chunk<?>) obj;

		boolean contentEqual = this.content.equals(that.content);
		boolean pageableEqual = this.pageable == null ? that.pageable == null : this.pageable.equals(that.pageable);

		return contentEqual && pageableEqual;
	}

	@Override
	public int hashCode() {

		int result = 17;

		result += 31 * (pageable == null ? 0 : pageable.hashCode());
		result += 31 * content.hashCode();

		return result;
	}
}
