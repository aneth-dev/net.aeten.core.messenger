package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Sender;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiInitializer;

@Provider (Sender.class)
public class StreamSender<Message> extends
		Sender.SenderAdapter <Message> {

	@FieldInit
	private volatile OutputStream outputStream;
	private final StreamSenderInitalizer initalizer;

	public StreamSender (@SpiInitializer StreamSenderInitalizer init)
			throws IOException {
		super (init.getIdentifier ());
		initalizer = init;
		connect ();
	}

	public StreamSender (String identifier,
								ObjectOutputStream outputStream) {
		super (identifier);
		this.outputStream = outputStream;
		initalizer = null;
		connected = true;
	}

	@Override
	public void send (MessengerEventData <Message> data) throws IOException {
		ObjectOutputStream objectOutputStream = ((ObjectOutputStream) outputStream);
		if (objectOutputStream == null) {
			throw new IOException ("Stream is closed");
		}
		Message message = data.getMessage ();
		objectOutputStream.writeObject (message);
		objectOutputStream.flush ();
	}

	@Override
	protected void doDisconnect () throws IOException {
		outputStream.close ();
		outputStream = null;
	}

	@Override
	protected void doConnect () throws IOException {
		if (initalizer == null && outputStream == null) {
			throw new IOException ("Unable to re-open output stream " + identifier);
		}
		outputStream = initalizer.getOutputStream ();
	}
}
