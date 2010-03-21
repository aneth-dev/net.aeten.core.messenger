package org.pititom.core.messenger.stream.provider;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.Option;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingTransmitter;
import org.pititom.core.messenger.service.Sender;

public class StreamSender<Message> extends Sender<Message> {
	@Option(name = "-os", aliases = "--output-stream", required = true)
	private OutputStream outputStream = null;

	/** @deprecated Reserved to configuration building */
	public StreamSender() {}

	public StreamSender(String identifier, ObjectOutputStream outputStream) {
		super(identifier);
		this.outputStream = outputStream;
	}

	@Override
	public void send(Message message) {
		try {
			((ObjectOutputStream) this.outputStream).writeObject(message);
			this.outputStream.flush();
		} catch (IOException exception) {
			LoggingTransmitter.getInstance().transmit(new LoggingData(this, LoggingEvent.ERROR, exception));
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.outputStream.close();
	}

}
