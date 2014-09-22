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
