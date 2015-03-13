package br.com.anteros.persistence.handler;

public class MultiSelectHandlerException extends RuntimeException {

	public MultiSelectHandlerException() {
	}

	public MultiSelectHandlerException(String message) {
		super(message);
	}

	public MultiSelectHandlerException(Throwable cause) {
		super(cause);
	}

	public MultiSelectHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

}
