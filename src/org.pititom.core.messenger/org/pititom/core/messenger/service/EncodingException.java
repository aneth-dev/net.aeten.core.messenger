package org.pititom.core.messenger.service;

public class EncodingException extends Exception {

	private static final long serialVersionUID = 7072317631168087097L;

	public EncodingException(String message) {
		super(message);
	}

	public EncodingException(Throwable cause) {
		super(cause);
	}

	public EncodingException(String message, Throwable cause) {
		super(message, cause);
	}

}
