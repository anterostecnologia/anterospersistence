package br.com.anteros.persistence.dsl.osql;

public class SQLSerializerException extends RuntimeException {

	public SQLSerializerException(String message) {
		super(message);
	}

	public SQLSerializerException(Throwable cause) {
		super(cause);
	}

	public SQLSerializerException(String message, Throwable cause) {
		super(message, cause);
	}

	public SQLSerializerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
