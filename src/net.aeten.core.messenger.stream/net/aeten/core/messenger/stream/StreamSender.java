package net.aeten.core.messenger.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Format;
import net.aeten.core.messenger.MessengerEventData;
import net.aeten.core.messenger.Sender;
import net.aeten.core.service.Provider;
import net.aeten.core.stream.args4j.OutputStreamOptionHandler;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@Provider(Sender.class)
@Format("args")
public class StreamSender<Message> extends Sender.Helper<Message> {
	
	static {
		CmdLineParser.registerHandler(OutputStream.class, OutputStreamOptionHandler.class);
	}
	
	@Option(name = "-os", aliases = "--output-stream", required = true)
	private OutputStream outputStream = null;

	/** @deprecated Reserved to configuration building */
	@Deprecated
    public StreamSender() {}

	public StreamSender(String identifier, ObjectOutputStream outputStream) {
		super(identifier);
		this.outputStream = outputStream;
	}

	@Override
	public void send(MessengerEventData<Message> data) throws IOException {
		((ObjectOutputStream) this.outputStream).writeObject(data.getMessage());
		this.outputStream.flush();
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.outputStream.close();
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
	public void configure(String conf) throws ConfigurationException {
		super.configure(conf);
		this.connected = true;
	}

}
