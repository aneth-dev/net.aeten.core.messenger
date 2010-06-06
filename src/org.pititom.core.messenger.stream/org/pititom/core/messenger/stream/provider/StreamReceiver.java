package org.pititom.core.messenger.stream.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pititom.core.ConfigurationException;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.messenger.service.Receiver;
import org.pititom.core.stream.args4j.InputStreamOptionHandler;

public class StreamReceiver<Message> extends Receiver<Message> {
	@Option(name = "-is", aliases = "--input-stream", required = true)
	private InputStream inputStream = null;

	/** @deprecated Reserved to configuration building */
	public StreamReceiver() {}

	public StreamReceiver(String identifier, ObjectInputStream inputStream) {
		super(identifier);
		this.inputStream = inputStream;
	}

	@Override
	protected void doConnect() throws IOException {
		// Does nothing. Streams are already connected.
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.inputStream.close();
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		CmdLineParser.registerHandler(InputStream.class,
				InputStreamOptionHandler.class);

		super.configure(configuration);
		this.connected = true;
	}

	@SuppressWarnings("unchecked")
	public Message receive() throws IOException {
		try {
			return (Message) ((ObjectInputStream) this.inputStream).readObject();
		} catch (Throwable exception) {
			if (this.inputStream.markSupported()) {
				Logger.log(this, LogLevel.ERROR, this.getIdentifier() + " has not been able to read object. Trying to reset the stream…", exception);
				this.inputStream.reset();
				return this.receive();
			}
			throw new IOException(exception);
		}
	}
}
