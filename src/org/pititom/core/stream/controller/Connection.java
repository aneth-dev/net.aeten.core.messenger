package org.pititom.core.stream.controller;

import java.io.IOException;

public interface Connection {
	public void connect() throws IOException;

	public void disconnect() throws IOException;

	public boolean isConnected();

}
