package org.pititom.core.messenger;

import java.io.IOException;

import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Connection;
import org.pititom.core.Identifiable;
import org.pititom.core.args4j.CommandLineParserHelper;

abstract class Helper implements Identifiable, Connection, Configurable<String> {
	@Option(name = "-id", aliases = "--identifier", required = true)
	protected volatile String identifier = null;
	
	protected volatile String configuration;
	protected volatile boolean connected = false;
	
	public Helper() {}

	public Helper(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

    @Override
	public void configure(String conf) throws ConfigurationException {
		this.configuration = conf;
		CommandLineParserHelper.configure(this, this.configuration);
	}

	
	@Override
	public boolean isConnected() {
		return this.connected;
	}
	
	@Override
	public final synchronized void connect() throws IOException {
		if (!this.isConnected()) {
			try {
				this.doConnect();
				this.connected = true;
			} catch (Exception exception) {
				this.connected = false;
				throw new IOException(exception);
			}
		}
	}

	@Override
	public final synchronized void disconnect() throws IOException {
		if (this.isConnected()) {
			try {
				this.doDisconnect();
				this.connected = false;
			} catch (IOException exception) {
				this.connected = true;
				throw exception;
			}
		}
	}

	protected abstract void doConnect() throws IOException;

	protected abstract void doDisconnect() throws IOException;

}
