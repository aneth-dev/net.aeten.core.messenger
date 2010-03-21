package org.pititom.core.messenger.stream.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;

import org.kohsuke.args4j.Option;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingTransmitter;
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
	public Iterator<Message> iterator() {
		return new Iterator<Message>() {

			@Override
			public boolean hasNext() {
				return StreamReceiver.this.isConnected();
			}

			@Override
			public Message next() {
				try {
					return (Message) ((ObjectInputStream) StreamReceiver.this.inputStream).readObject();
				} catch (Exception exception) {
					LoggingTransmitter.getInstance().transmit(new LoggingData(StreamReceiver.this.identifier, LoggingEvent.ERROR, exception));
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.inputStream.close();
	}

}
