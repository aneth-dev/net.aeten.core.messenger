package org.pititom.core.messenger.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pititom.core.ConfigurationException;
import org.pititom.core.Format;
import org.pititom.core.messenger.MessengerEventData;
import org.pititom.core.messenger.Sender;
import org.pititom.core.service.Provider;
import org.pititom.core.stream.args4j.OutputStreamOptionHandler;

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
