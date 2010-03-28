package org.pititom.core.logging;

import org.pititom.core.event.EventData;

/**
 *
 * @author Thomas Pérennou
 */
public class LoggingData extends EventData<Object, LogLevel> {
	private final String message;
	private final Exception exception;

	public LoggingData(Object source, LogLevel level, String message, Exception exception) {
		super(source, level);
		this.message = message;
		this.exception = exception;
	}

	public LoggingData(Object source, LogLevel level, Exception exception) {
		this(source, level, exception.toString(), exception);
	}

	public LoggingData(Object source, LogLevel level, String message) {
		this(source, level, message, null);
	}

	public Exception getException() {
		return exception;
	}

	public String getMessage() {
		return message;
	}

}
