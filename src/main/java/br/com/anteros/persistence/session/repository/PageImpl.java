package br.com.anteros.persistence.session.repository;

import java.util.List;

public class PageImpl<T> extends Chunk<T> implements Page<T> {

	private static final long serialVersionUID = 867755909294344406L;

	private final long total;

	public PageImpl(List<T> content, Pageable pageable, long total) {

		super(content, pageable);
		this.total = total;
	}

	public PageImpl(List<T> content) {
		this(content, null, null == content ? 0 : content.size());
	}

	@Override
	public int getTotalPages() {
		return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
	}

	@Override
	public long getTotalElements() {
		return total;
	}

	@Override
	public boolean hasNext() {
		return getNumber() + 1 < getTotalPages();
	}

	@Override
	public boolean isLast() {
		return !hasNext();
	}

	@Override
	public String toString() {

		String contentType = "UNKNOWN";
		List<T> content = getContent();

		if (content.size() > 0) {
			contentType = content.get(0).getClass().getName();
		}

		return String.format("Page %s of %d containing %s instances", getNumber(), getTotalPages(), contentType);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PageImpl<?>)) {
			return false;
		}

		PageImpl<?> that = (PageImpl<?>) obj;

		return this.total == that.total && super.equals(obj);
	}

	@Override
	public int hashCode() {

		int result = 17;

		result += 31 * (int) (total ^ total >>> 32);
		result += 31 * super.hashCode();

		return result;
	}
}
