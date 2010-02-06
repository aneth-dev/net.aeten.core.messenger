package org.pititom.core.logging;

/**
 *
 * @author Thomas Pérennou
 */
public class LoggingData {
	private final Object source;
	private final String message;
	private final Exception exception;

	public LoggingData(Object source, String message, Exception exception) {
		this.source = source;
		this.message = message;
		this.exception = exception;
	}

	public LoggingData(Object source, Exception exception) {
		this(source, exception.toString(), exception);
	}

	public LoggingData(Object source, String message) {
		this(source, message, null);
	}

	public Exception getException() {
		return exception;
	}

	public String getMessage() {
		return message;
	}

	public Object getSource() {
		return source;
	}

}
