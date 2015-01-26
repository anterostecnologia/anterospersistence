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
package br.com.anteros.persistence.session.repository.impl;

import java.util.List;

import br.com.anteros.persistence.session.repository.Chunk;
import br.com.anteros.persistence.session.repository.Pageable;

public class SliceImpl<T> extends Chunk<T> {

	private static final long serialVersionUID = 867755909294344406L;

	private final boolean hasNext;

	public SliceImpl(List<T> content, Pageable pageable, boolean hasNext) {

		super(content, pageable);
		this.hasNext = hasNext;
	}

	public SliceImpl(List<T> content) {
		this(content, null, false);
	}

	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public String toString() {

		String contentType = "UNKNOWN";
		List<T> content = getContent();

		if (content.size() > 0) {
			contentType = content.get(0).getClass().getName();
		}

		return String.format("Slice %d containing %s instances", getNumber(), contentType);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SliceImpl<?>)) {
			return false;
		}

		SliceImpl<?> that = (SliceImpl<?>) obj;

		return this.hasNext == that.hasNext && super.equals(obj);
	}

	@Override
	public int hashCode() {

		int result = 17;

		result += 31 * (hasNext ? 1 : 0);
		result += 31 * super.hashCode();

		return result;
	}
}
