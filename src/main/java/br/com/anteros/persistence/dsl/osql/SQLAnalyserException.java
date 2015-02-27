package br.com.anteros.persistence.dsl.osql;

public class SQLAnalyserException extends RuntimeException {

	public SQLAnalyserException(String message) {
		super(message);
	}

	public SQLAnalyserException(Throwable cause) {
		super(cause);
	}

	public SQLAnalyserException(String message, Throwable cause) {
		super(message, cause);
	}

	public SQLAnalyserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
