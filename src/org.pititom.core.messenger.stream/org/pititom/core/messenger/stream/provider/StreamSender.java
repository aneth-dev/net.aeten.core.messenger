package org.pititom.core.messenger.stream.provider;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pititom.core.ConfigurationException;
import org.pititom.core.messenger.service.Sender;
import org.pititom.core.stream.args4j.OutputStreamOptionHandler;

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
	public void send(Message message) throws IOException {
		((ObjectOutputStream) this.outputStream).writeObject(message);
		this.outputStream.flush();
	}

	@Override
	protected void doDisconnect() throws IOException {
		this.outputStream.close();
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		CmdLineParser.registerHandler(OutputStream.class,
				OutputStreamOptionHandler.class);

		super.configure(configuration);
		this.connected = true;
	}

}
