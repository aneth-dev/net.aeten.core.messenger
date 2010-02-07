package org.pititom.core.logging;

/**
 *
 * @author Thomas Pérennou
 */
public class LoggingData {
	private final String message;
	private final Exception exception;

	public LoggingData(String message, Exception exception) {
		this.message = message;
		this.exception = exception;
	}

	public LoggingData(Exception exception) {
		this(exception.toString(), exception);
	}

	public LoggingData(String message) {
		this(message, null);
	}

	public Exception getException() {
		return exception;
	}

	public String getMessage() {
		return message;
	}

}
