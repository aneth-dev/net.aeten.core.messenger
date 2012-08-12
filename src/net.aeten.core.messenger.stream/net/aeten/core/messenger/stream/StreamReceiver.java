package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Receiver;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;


@Provider(Receiver.class)
public class StreamReceiver<Message> extends Receiver.ReceiverAdapter<Message> {

	@FieldInit
	private volatile InputStream inputStream;
	private final StreamReceiverInitalizer initalizer;

	public StreamReceiver(@SpiInitializer StreamReceiverInitalizer init) throws IOException {
		super(init.getIdentifier());
		initalizer = init;
		connect();
	}

	public StreamReceiver(String identifier, ObjectInputStream inputStream) {
		super(identifier);
		this.inputStream = inputStream;
		initalizer = null;
		connected = true;
	}

	@Override
	protected void doConnect() throws IOException {
		if (initalizer == null && inputStream == null) {
			throw new IOException("Unable to re-open input stream " + identifier);
		}
		inputStream = initalizer.getInputStream();
	}

	@Override
	protected void doDisconnect() throws IOException {
		inputStream.close();
		inputStream = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive(MessengerEventData<Message> data) throws IOException {
		try {
			Message message = (Message) ((ObjectInputStream) this.inputStream).readObject();
			data.setMessage(message);
		} catch (IOException | ClassNotFoundException exception) {
			if (this.inputStream.markSupported()) {
				Logger.log(this, LogLevel.ERROR, this.getIdentifier() + " has not been able to read object. Trying to reset the stream…", exception);
				this.inputStream.reset();
				this.receive(data);
			}
			throw new IOException(exception);
		}
	}
}
