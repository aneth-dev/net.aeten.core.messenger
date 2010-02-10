package org.pititom.core.messenger.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.pititom.core.ConfigurationException;
import org.pititom.core.args4j.CommandLineParser;
import org.pititom.core.logging.LoggingData;
import org.pititom.core.logging.LoggingEvent;
import org.pititom.core.logging.LoggingForwarder;
import org.pititom.core.messenger.AbstractMessenger;

/**
 *
 * @author Thomas Pérennou
 */
public class StreamMessenger<Message, Acknowledge extends Enum<?>>  extends AbstractMessenger<Message, Acknowledge> {

	@Option(name = "-is", aliases = "--input-stream", required = false)
	private List<InputStream> inputStreamList = null;
	@Option(name = "-os", aliases = "--output-stream", required = false)
	private OutputStream outputStream = null;
	
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream[] objectInputStreamList;
	private final Collection<Reciever> recieverList;

	public StreamMessenger(String name, String hookConfiguration, ObjectOutputStream outputStream, ObjectInputStream... inputStreamList) throws ConfigurationException, IOException {
		super(name);
		super.configure(hookConfiguration);

		this.objectOutputStream = outputStream;
		this.objectInputStreamList = inputStreamList;

		this.recieverList = new ArrayList<Reciever>(inputStreamList.length);

		this.connect();
	}
	public StreamMessenger() {
		this.recieverList = new ArrayList<Reciever>();
	}
	@Override
	protected void doConnect() throws IOException {
		super.doConnect();
		Reciever reciever;
		for (int i = 0; i < this.objectInputStreamList.length; i++) {
			reciever = new Reciever(this, this.objectInputStreamList[i]);
			this.recieverList.add(reciever);
			new Thread(reciever, "Messenger \"" + this.getName() + "\" reciever").start();
		}
	}

	@Override
	protected void doDisconnect() throws IOException {
		super.doDisconnect();
		for (ObjectInputStream inputStream : this.objectInputStreamList) {
			inputStream.close();
		}
		this.objectOutputStream.close();
	}

	private class Reciever implements Runnable {

		private final AbstractMessenger<Message, ? extends Enum<?>> messenger;
		private final ObjectInputStream in;

		public Reciever(AbstractMessenger<Message, ? extends Enum<?>> messenger, ObjectInputStream in) {
			this.messenger = messenger;
			this.in = in;
		}

		@Override
		public void run() {
			while (this.messenger.isConnected()) {
				Object bean;
				try {
					bean = this.in.readObject();
					@SuppressWarnings("unchecked")
					Message message = (Message) bean;

					StreamMessenger.this.doRecieve(message);

				} catch (Exception exception) {
					LoggingForwarder.getInstance().forward(this.messenger, LoggingEvent.ERROR, new LoggingData(exception));
				}
			}
		}
	}

	@Override
	protected void send(Message message) {
		try {
			this.objectOutputStream.writeObject(message);
			this.objectOutputStream.flush();
		} catch (IOException exception) {
			LoggingForwarder.getInstance().forward(this, LoggingEvent.ERROR, new LoggingData(exception));
		}
	}
	
	@Override
	public void configure(String configuration) throws ConfigurationException {
		if (this.getName() != null) {
			throw new ConfigurationException(configuration, "Messenger \"" + this.getName() + "\" is allready configured");
		}
		CommandLineParser commandLineParser = new CommandLineParser(this);
		try {
			commandLineParser.parseArgument(CommandLineParser.splitArguments(configuration));
			this.objectOutputStream = (ObjectOutputStream)outputStream;
			if (this.inputStreamList != null) {
				this.objectInputStreamList = new ObjectInputStream[this.inputStreamList.size()];
				for (int i=0; i<this.inputStreamList.size(); i++) {
					this.objectInputStreamList[i] = (ObjectInputStream)inputStreamList.get(i);
				}
			}
			this.connect();
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
	}

}
