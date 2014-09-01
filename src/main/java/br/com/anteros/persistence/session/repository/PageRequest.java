package br.com.anteros.persistence.session.repository;


public class PageRequest extends AbstractPageRequest {

	private static final long serialVersionUID = -4541509938956089562L;

	public PageRequest(int page, int size) {
		super(page, size);
	}

	public Pageable next() {
		return new PageRequest(getPageNumber() + 1, getPageSize());
	}

	public PageRequest previous() {
		return getPageNumber() == 0 ? this : new PageRequest(getPageNumber() - 1, getPageSize());
	}

	public Pageable first() {
		return new PageRequest(0, getPageSize());
	}

	@Override
	public String toString() {
		return String.format("Page request [number: %d, size %d]", getPageNumber(), getPageSize());
	}
}
