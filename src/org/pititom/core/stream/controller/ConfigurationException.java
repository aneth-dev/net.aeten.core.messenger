package org.pititom.core.stream.controller;

public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 534021741705234307L;

	public ConfigurationException() {
	}

	public ConfigurationException(String packet) {
		super(packet);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String packet, Throwable cause) {
		super(packet, cause);
	}

}
