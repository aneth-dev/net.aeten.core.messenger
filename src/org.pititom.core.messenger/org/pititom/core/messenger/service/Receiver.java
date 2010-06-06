package org.pititom.core.messenger.service;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Connection;
import org.pititom.core.Identifiable;
import org.pititom.core.args4j.CommandLineParserHelper;
import org.pititom.core.messenger.args4j.ReceiverOptionHandler;

public abstract class Receiver<Message> implements Identifiable, Connection, Configurable {

	static {
		CmdLineParser.registerHandler(Receiver.class, ReceiverOptionHandler.class);
	}

	@Option(name = "-id", aliases = "--identifier", required = true)
	protected String identifier = null;
	
	protected String configuration;
	protected boolean connected = false;

	public Receiver() {}

	public Receiver(String identifier) {
		this.identifier = identifier;
	}


	@Override
	public String getIdentifier() {
		return this.identifier;
	}

    @Override
	public void configure(String configuration) throws ConfigurationException {
		this.configuration = configuration;
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
	
	@Override
	public String toString() {
		return "Receiver \"" + this.getIdentifier() + "\"";
	}

	protected abstract void doConnect() throws IOException;

	protected abstract void doDisconnect() throws IOException;

	public abstract Message receive() throws IOException;
}
