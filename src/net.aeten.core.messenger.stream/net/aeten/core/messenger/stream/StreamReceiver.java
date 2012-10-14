package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicReference;

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
	private final AtomicReference<InputStream> inputStream;
	private final StreamReceiverInitalizer initalizer;

	public StreamReceiver(@SpiInitializer StreamReceiverInitalizer init) throws IOException {
		super(init.getIdentifier());
		inputStream = new AtomicReference<>();
		initalizer = init;
	}

	public StreamReceiver(String identifier, InputStream inputStream) {
		super(identifier);
		this.inputStream = new AtomicReference<>(inputStream);
		initalizer = null;
	}

	@Override
	protected void doConnect() throws IOException {
		InputStream in = inputStream.get();
		if (initalizer == null && in == null) {
			throw new IOException("Unable to re-open input stream " + identifier);
		}
		inputStream.compareAndSet(null, (ObjectInputStream) initalizer.getInputStream());
	}

	@Override
	public final void disconnect() throws IOException {
		InputStream in = inputStream.get();
		in.close();
		synchronized(this) {
			inputStream.compareAndSet(in, null);
			connected = false;
		}
	}
	
	@Override
	protected void doDisconnect() throws IOException {
		// Not used (disconnect overrides)
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void receive(MessengerEventData<Message> data) throws IOException {
		try {
			Message message = (Message) ((ObjectInputStream) inputStream.get()).readObject();
			data.setMessage(message);
		} catch (IOException | ClassNotFoundException exception) {
			if (inputStream.get().markSupported()) {
				Logger.log(this, LogLevel.ERROR, getIdentifier() + " has not been able to read object. Trying to reset the stream…", exception);
				inputStream.get().reset();
				receive(data);
			}
			throw new IOException(exception);
		}
	}
}
