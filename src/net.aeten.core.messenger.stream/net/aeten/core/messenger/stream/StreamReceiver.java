package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Format;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Receiver;
import net.aeten.core.service.Provider;
import net.aeten.core.stream.args4j.InputStreamOptionHandler;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@Provider(Receiver.class)
@Format("args")
public class StreamReceiver<Message> extends Receiver.Helper<Message> {
	static {
		CmdLineParser.registerHandler(InputStream.class, InputStreamOptionHandler.class);
	}
	
	@Option(name = "-is", aliases = "--input-stream", required = true)
	private InputStream inputStream = null;

	/** @deprecated Reserved to configuration building */
	@Deprecated
    public StreamReceiver() {}

	public StreamReceiver(String identifier, ObjectInputStream inputStream) {
		super(identifier);
		this.inputStream = inputStream;
	}

	@Override
	protected void doConnect() throws IOException {
		if (this.configuration != null) {
			try {
				this.configure(this.configuration);
			} catch (ConfigurationException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.inputStream.close();
	}

	@Override
	public void configure(String conf) throws ConfigurationException {
		super.configure(conf);
		this.connected = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive(MessengerEventData<Message> data) throws IOException {
		try {
			Message message = (Message) ((ObjectInputStream) this.inputStream).readObject();
			data.setMessage(message);
		} catch (Throwable exception) {
			if (this.inputStream.markSupported()) {
				Logger.log(this, LogLevel.ERROR, this.getIdentifier() + " has not been able to read object. Trying to reset the stream…", exception);
				this.inputStream.reset();
				this.receive(data);
			}
			throw new IOException(exception);
		}
	}
}
