package org.pititom.core.data;

public class ParameterException extends Exception {
    private static final long serialVersionUID = 3291925199300260147L;

	public ParameterException() {
	}

	public ParameterException(String message) {
		super(message);
	}

	public ParameterException(Throwable cause) {
		super(cause);
	}

	public ParameterException(String message, Throwable cause) {
		super(message, cause);
	}

}
