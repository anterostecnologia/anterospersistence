package br.com.anteros.persistence.proxy;

public class ProxyCreationException extends RuntimeException {

	public ProxyCreationException() {
	}

	public ProxyCreationException(String message) {
		super(message);
	}

	public ProxyCreationException(Throwable cause) {
		super(cause);
	}

	public ProxyCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProxyCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
