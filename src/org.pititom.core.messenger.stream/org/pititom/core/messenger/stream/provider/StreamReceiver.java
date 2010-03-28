package org.pititom.core.messenger.stream.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.kohsuke.args4j.Option;
import org.pititom.core.ConfigurationException;
import org.pititom.core.logging.LogLevel;
import org.pititom.core.logging.Logger;
import org.pititom.core.messenger.service.Receiver;

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
	protected void doDisconnect() throws IOException {
		this.inputStream.close();
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		super.configure(configuration);
		this.connected = true;
	}
	
	public Message recieve() {
		try {
			return (Message) ((ObjectInputStream) StreamReceiver.this.inputStream).readObject();
		} catch (Exception exception) {
			Logger.log(StreamReceiver.this.identifier, LogLevel.ERROR, exception);
			return null;
		}
	}

}
